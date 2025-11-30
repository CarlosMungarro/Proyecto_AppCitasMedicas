package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

    private var duracionCitaMinutos: Int = 30 // Default 30 minutos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendarCitaPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = DoctorAdapter(filteredDoctors) { doctor ->
            selectedDoctor = doctor
            Toast.makeText(this, "Seleccionaste a: ${doctor.nombreCompleto}", Toast.LENGTH_SHORT).show()

            filteredDoctors.clear()
            filteredDoctors.add(doctor)
            adapter.notifyDataSetChanged()

            // Cargar duración de cita configurada por el médico
            cargarDuracionCita(doctor.id)

            mostrarDisponibilidad(doctor.id)
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

    private fun cargarDuracionCita(doctorId: String) {
        db.collection("medicos").document(doctorId).get()
            .addOnSuccessListener { doc ->
                duracionCitaMinutos = doc.getLong("duracionCita")?.toInt() ?: 30
                Log.d("AgendarCita", "Duración de cita: $duracionCitaMinutos minutos")
            }
            .addOnFailureListener {
                duracionCitaMinutos = 30
            }
    }

    private fun loadDoctors() {
        db.collection("medicos").get()
            .addOnSuccessListener { snapshot ->
                doctorsList.clear()
                val doctoresConDisponibilidad = mutableListOf<MedicoPaciente>()

                val totalDoctors = snapshot.size()
                var processedDoctors = 0

                for (doc in snapshot.documents) {
                    val id = doc.id
                    val nombre = doc.getString("nombreCompleto") ?: ""
                    val especialidad = doc.getString("especialidad") ?: ""
                    val consultorio = doc.getString("direccionConsultorio") ?: ""
                    val foto = doc.getString("fotoUrl")

                    // ✅ FUNCIONALIDAD 5: Verificar si tiene disponibilidad antes de agregarlo
                    verificarDisponibilidadMedico(id) { tieneDisponibilidad ->
                        if (tieneDisponibilidad) {
                            doctoresConDisponibilidad.add(
                                MedicoPaciente(id, nombre, especialidad, consultorio, foto)
                            )
                        }

                        processedDoctors++
                        if (processedDoctors == totalDoctors) {
                            doctorsList.addAll(doctoresConDisponibilidad)
                            filteredDoctors.clear()
                            filteredDoctors.addAll(doctorsList)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar médicos", Toast.LENGTH_SHORT).show()
            }
    }

    private fun verificarDisponibilidadMedico(doctorId: String, callback: (Boolean) -> Unit) {
        db.collection("medicos").document(doctorId)
            .collection("disponibilidad")
            .get()
            .addOnSuccessListener { snapshot ->
                var tieneDisponibilidad = false

                for (doc in snapshot.documents) {
                    val activo = doc.getBoolean("activo") == true
                    val rangos = (doc.get("rangos") as? List<Map<String, String>>) ?: emptyList()

                    if (activo && rangos.isNotEmpty()) {
                        tieneDisponibilidad = true
                        break
                    }
                }

                callback(tieneDisponibilidad)
            }
            .addOnFailureListener {
                callback(false)
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

                // ✅ FUNCIONALIDAD 4: Cuando cambia la fecha, actualizar horas disponibles
                if (selectedDoctor != null) {
                    actualizarHorasDisponibles()
                }
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun actualizarHorasDisponibles() {
        if (selectedDoctor == null || selectedDate == null) return

        val doctorId = selectedDoctor!!.id
        val diaSemana = SimpleDateFormat("EEEE", Locale("es", "ES"))
            .format(selectedDate!!.time).lowercase()

        db.collection("medicos").document(doctorId)
            .collection("disponibilidad").document(diaSemana)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists() || doc.getBoolean("activo") != true) {
                    Toast.makeText(this, "El médico no atiende este día", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val rangos = (doc.get("rangos") as? List<Map<String, String>>) ?: emptyList()

                // Obtener citas existentes del médico para ese día
                obtenerCitasDelDia(doctorId, selectedDate!!) { citasOcupadas ->
                    val horasDisponibles = generarHorasDisponibles(rangos, citasOcupadas)
                    mostrarDropdownHoras(horasDisponibles)
                }
            }
    }

    private fun obtenerCitasDelDia(doctorId: String, fecha: Calendar, callback: (List<Int>) -> Unit) {
        val inicioDia = Calendar.getInstance().apply {
            time = fecha.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        val finDia = Calendar.getInstance().apply {
            time = fecha.time
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }

        db.collection("citas")
            .whereEqualTo("medicoId", doctorId)
            .whereGreaterThanOrEqualTo("fechaHora", Timestamp(inicioDia.time))
            .whereLessThanOrEqualTo("fechaHora", Timestamp(finDia.time))
            .get()
            .addOnSuccessListener { snapshot ->
                val horasOcupadas = snapshot.documents.mapNotNull { doc ->
                    val timestamp = doc.getTimestamp("fechaHora")
                    timestamp?.toDate()?.let { date ->
                        val cal = Calendar.getInstance()
                        cal.time = date
                        cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                    }
                }
                callback(horasOcupadas)
            }
            .addOnFailureListener {
                callback(emptyList())
            }
    }

    private fun generarHorasDisponibles(
        rangos: List<Map<String, String>>,
        citasOcupadas: List<Int>
    ): List<String> {
        val horasDisponibles = mutableListOf<String>()

        rangos.forEach { rango ->
            val inicio = parseHoraToMinutes(rango["inicio"])
            val fin = parseHoraToMinutes(rango["fin"])

            var horaActual = inicio
            while (horaActual + duracionCitaMinutos <= fin) {
                // Verificar que no esté ocupada
                if (!citasOcupadas.contains(horaActual)) {
                    val hora = horaActual / 60
                    val minuto = horaActual % 60
                    horasDisponibles.add(String.format("%02d:%02d", hora, minuto))
                }
                horaActual += duracionCitaMinutos
            }
        }

        return horasDisponibles
    }

    private fun mostrarDropdownHoras(horas: List<String>) {
        if (horas.isEmpty()) {
            Toast.makeText(this, "No hay horarios disponibles para este día", Toast.LENGTH_LONG).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Selecciona una hora")
        builder.setItems(horas.toTypedArray()) { _, which ->
            val horaSeleccionada = horas[which]
            binding.etHora.setText(horaSeleccionada)

            // Actualizar selectedTime
            val partes = horaSeleccionada.split(":")
            val cal = selectedDate?.clone() as Calendar
            cal.set(Calendar.HOUR_OF_DAY, partes[0].toInt())
            cal.set(Calendar.MINUTE, partes[1].toInt())
            selectedTime = cal
        }
        builder.show()
    }

    private fun setupTimePicker() {
        binding.etHora.setOnClickListener {
            if (selectedDate == null) {
                Toast.makeText(this, "Primero selecciona una fecha", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedDoctor == null) {
                Toast.makeText(this, "Primero selecciona un médico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ FUNCIONALIDAD 4: Mostrar dropdown en lugar de TimePicker libre
            actualizarHorasDisponibles()
        }
    }

    private fun setupButtons() {
        binding.btnCancelarCita.setOnClickListener { finish() }

        binding.btnConfirmarCita.setOnClickListener {
            mostrarDialogoConfirmacion()
        }
    }

    private fun mostrarDialogoConfirmacion() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para agendar una cita", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDoctor == null || selectedDate == null || selectedTime == null) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val motivo = binding.etMotivo.text.toString().trim()
        if (motivo.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el motivo de la cita", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmar cita")
        builder.setMessage(
            "¿Estás seguro de agendar esta cita?\n\n" +
                    "Médico: ${selectedDoctor?.nombreCompleto}\n" +
                    "Fecha: ${binding.etFecha.text}\n" +
                    "Hora: ${binding.etHora.text}\n" +
                    "Motivo: $motivo"
        )
        builder.setPositiveButton("Sí, confirmar") { _, _ ->
            validarDisponibilidadYConflictos(user.uid, motivo)
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun mostrarDisponibilidad(doctorId: String) {
        val tv = binding.tvDisponibilidad
        val card = binding.cardDisponibilidad
        val recycler = binding.rvMedicos
        val buscar = binding.etBuscarMedico

        recycler.visibility = View.GONE
        buscar.visibility = View.GONE
        card.visibility = View.VISIBLE

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

                    builder.append("$diaCapitalizado:\n")

                    if (!activo || rangos.isEmpty()) {
                        builder.append("  No disponible\n\n")
                    } else {
                        rangos.forEach { rango ->
                            val inicio = rango["inicio"] ?: "--:--"
                            val fin = rango["fin"] ?: "--:--"
                            builder.append("  🕓 $inicio - $fin\n")
                        }
                        builder.append("\n")
                    }
                }

                tv.text = builder.toString()
            }
            .addOnFailureListener {
                tv.text = "Error al cargar disponibilidad."
            }

        binding.btnCambiarMedico.setOnClickListener {
            recycler.visibility = View.VISIBLE
            buscar.visibility = View.VISIBLE
            card.visibility = View.GONE

            filteredDoctors.clear()
            filteredDoctors.addAll(doctorsList)
            adapter.notifyDataSetChanged()

            selectedDoctor = null
            selectedDate = null
            selectedTime = null
            binding.etFecha.text?.clear()
            binding.etHora.text?.clear()
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
