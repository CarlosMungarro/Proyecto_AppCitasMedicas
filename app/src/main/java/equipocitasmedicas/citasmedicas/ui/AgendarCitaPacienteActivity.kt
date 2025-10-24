package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.databinding.ActivityAgendarCitaPacienteBinding
import equipocitasmedicas.citasmedicas.model.Cita
import equipocitasmedicas.citasmedicas.model.MedicoPaciente
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

            val citaCalendar = selectedTime ?: selectedDate!!
            val pacienteId = user.uid
            val pacienteNombre = user.displayName ?: user.email ?: "Paciente sin nombre"

            val cita = Cita(
                pacienteId = pacienteId,
                pacienteNombre = pacienteNombre,
                medicoId = selectedDoctor!!.id,
                medicoNombre = selectedDoctor!!.nombreCompleto,
                medicoEspecialidad = selectedDoctor!!.especialidad,
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
}