package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.databinding.ActivityAgendarCitaPacienteBinding
import equipocitasmedicas.citasmedicas.model.Cita
import equipocitasmedicas.citasmedicas.model.MedicoPaciente
import java.text.SimpleDateFormat
import java.util.*

class AgendarCitaPacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendarCitaPacienteBinding
    private val db = FirebaseFirestore.getInstance()
    private val doctorsList = mutableListOf<MedicoPaciente>()
    private val filteredDoctors = mutableListOf<MedicoPaciente>()
    private lateinit var adapter: DoctorAdapter
    private var selectedDoctor: MedicoPaciente? = null
    private var selectedDate: Calendar? = null
    private var selectedTime: Calendar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendarCitaPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DoctorAdapter(filteredDoctors) { doctor ->
            selectedDoctor = doctor
            Toast.makeText(this, "Seleccionaste a: ${doctor.nombreCompleto}", Toast.LENGTH_SHORT).show()

            //Ocultar los demás médicos y mostrar solo el seleccionado
            filteredDoctors.clear()
            filteredDoctors.add(doctor)
            adapter.notifyDataSetChanged()

            //Mostrar disponibilidad
            mostrarDisponibilidad(doctor.id)

            //Ocultar el campo de búsqueda (opcional)
            binding.etBuscarMedico.visibility = View.GONE
        }

        binding.rvMedicos.layoutManager = LinearLayoutManager(this)
        binding.rvMedicos.adapter = adapter

        loadDoctors()
        setupSearch()
        setupDatePicker()
        setupTimePicker()
        setupButtons()
    }

    private fun loadDoctors() {
        db.collection("medicos").get()
            .addOnSuccessListener { snapshot ->
                doctorsList.clear()
                for (doc in snapshot.documents) {
                    val id = doc.id
                    val nombre = doc.getString("nombreCompleto") ?: ""
                    val especialidad = doc.getString("especialidad") ?: ""
                    val consultorio = doc.getString("direccionConsultorio") ?: ""
                    val foto = doc.getString("fotoUrl")
                    doctorsList.add(MedicoPaciente(id, nombre, especialidad, consultorio, foto))
                }
                filteredDoctors.clear()
                filteredDoctors.addAll(doctorsList)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar médicos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupSearch() {
        binding.etBuscarMedico.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase(Locale.getDefault())
                filteredDoctors.clear()
                filteredDoctors.addAll(doctorsList.filter {
                    it.nombreCompleto.lowercase(Locale.getDefault()).contains(query) ||
                            it.especialidad.lowercase(Locale.getDefault()).contains(query)
                })
                adapter.notifyDataSetChanged()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupDatePicker() {
        binding.etFecha.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                selectedDate = cal
                binding.etFecha.setText("${dayOfMonth}/${month + 1}/$year")
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun setupTimePicker() {
        binding.etHora.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val cal = selectedDate ?: Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                selectedTime = cal
                binding.etHora.setText(String.format("%02d:%02d", hourOfDay, minute))
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }
    }

    private fun setupButtons() {
        binding.btnCancelarCita.setOnClickListener { finish() }

        binding.btnConfirmarCita.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Debes iniciar sesión para agendar una cita", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDoctor == null || selectedDate == null || selectedTime == null) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val motivo = binding.etMotivo.text.toString().trim()
            if (motivo.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa el motivo de la cita", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            validarDisponibilidadYConflictos(user.uid, motivo)
        }
    }

    private fun mostrarDisponibilidad(doctorId: String) {
        val tv = binding.tvDisponibilidad
        val card = binding.cardDisponibilidad
        val recycler = binding.rvMedicos
        val buscar = binding.etBuscarMedico

        //Ocultamos lista y buscador, y mostramos el card
        recycler.visibility = View.GONE
        buscar.visibility = View.GONE
        card.visibility = View.VISIBLE

        //Cargar información del médico seleccionado
        binding.tvNombreMedico.text = selectedDoctor?.nombreCompleto ?: "Médico"
        binding.tvEspecialidadMedico.text = selectedDoctor?.especialidad ?: ""

        db.collection("medicos").document(doctorId).collection("disponibilidad")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    tv.text = "El médico no tiene disponibilidad registrada."
                    return@addOnSuccessListener
                }

                val diasOrden = listOf("lunes", "martes", "miercoles", "jueves", "viernes")
                val builder = StringBuilder()

                for (dia in diasOrden) {
                    val doc = snapshot.documents.find { it.id.equals(dia, ignoreCase = true) } ?: continue
                    val diaCapitalizado = doc.id.replaceFirstChar { it.uppercase() }
                    val activo = doc.getBoolean("activo") == true
                    val rangos = (doc.get("rangos") as? List<Map<String, String>>) ?: emptyList()

                    //Agregamos sangría con espacios y saltos de línea
                    builder.append("$diaCapitalizado:\n")

                    if (!activo || rangos.isEmpty()) {
                        builder.append("  No disponible\n\n")
                    } else {
                        rangos.forEach { rango ->
                            val inicio = rango["inicio"] ?: "--:--"
                            val fin = rango["fin"] ?: "--:--"
                            builder.append("  \uD83D\uDD53 $inicio - $fin\n")
                        }
                        builder.append("\n")
                    }
                }

                tv.text = builder.toString()
            }
            .addOnFailureListener {
                tv.text = "Error al cargar disponibilidad."
            }

        //Listener del botón Cambiar Médico
        binding.btnCambiarMedico.setOnClickListener {
            // Mostrar lista y buscador
            recycler.visibility = View.VISIBLE
            buscar.visibility = View.VISIBLE
            card.visibility = View.GONE

            // Restaurar la lista original
            filteredDoctors.clear()
            filteredDoctors.addAll(doctorsList)
            adapter.notifyDataSetChanged()

            // Quitar selección
            selectedDoctor = null
        }
    }






    private fun validarDisponibilidadYConflictos(pacienteId: String, motivo: String) {
        val doctorId = selectedDoctor!!.id
        val citaCalendar = selectedTime!!
        val diaSemana = SimpleDateFormat("EEEE", Locale("es", "ES")).format(citaCalendar.time).lowercase()

        db.collection("medicos").document(doctorId)
            .collection("disponibilidad").document(diaSemana)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists() || doc.getBoolean("activo") != true) {
                    Toast.makeText(this, "El médico no atiende ese día", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val rangos = (doc.get("rangos") as? List<Map<String, String>>)?.map {
                    it["inicio"] to it["fin"]
                } ?: emptyList()
                val horaSeleccion = citaCalendar.get(Calendar.HOUR_OF_DAY) * 60 + citaCalendar.get(Calendar.MINUTE)

                val disponible = rangos.any { range ->
                    val inicio = parseHoraToMinutes(range.first)
                    val fin = parseHoraToMinutes(range.second)
                    horaSeleccion in inicio until fin
                }

                if (!disponible) {
                    Toast.makeText(this, "El médico no está disponible a esa hora", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                db.collection("citas")
                    .whereEqualTo("medicoId", doctorId)
                    .get()
                    .addOnSuccessListener { citas ->
                        val conflicto = citas.any {
                            val horaCita = (it.getTimestamp("fechaHora")?.toDate())?.let { d ->
                                val c = Calendar.getInstance(); c.time = d
                                c.get(Calendar.HOUR_OF_DAY) * 60 + c.get(Calendar.MINUTE)
                            } ?: -1
                            horaCita == horaSeleccion
                        }

                        if (conflicto) {
                            Toast.makeText(this, "El médico ya tiene una cita a esa hora", Toast.LENGTH_LONG).show()
                        } else {
                            registrarCita(pacienteId, motivo, citaCalendar)
                        }
                    }
            }
    }

    private fun registrarCita(pacienteId: String, motivo: String, citaCalendar: Calendar) {
        val doctor = selectedDoctor!!
        db.collection("pacientes").document(pacienteId).get()
            .addOnSuccessListener { docPac ->
                val pacienteNombre = docPac.getString("nombreCompleto") ?: "Paciente sin nombre"
                val cita = Cita(
                    pacienteId = pacienteId,
                    pacienteNombre = pacienteNombre,
                    medicoId = doctor.id,
                    medicoNombre = doctor.nombreCompleto,
                    medicoEspecialidad = doctor.especialidad,
                    fechaHora = Timestamp(citaCalendar.time),
                    estado = "pendiente",
                    notas = motivo
                )

                db.collection("citas").add(cita)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Cita agendada correctamente", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al agendar cita: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    private fun parseHoraToMinutes(hora: String?): Int {
        if (hora.isNullOrEmpty()) return -1
        val parts = hora.split(":")
        return try { parts[0].toInt() * 60 + parts[1].toInt() } catch (e: Exception) { -1 }
    }
}
