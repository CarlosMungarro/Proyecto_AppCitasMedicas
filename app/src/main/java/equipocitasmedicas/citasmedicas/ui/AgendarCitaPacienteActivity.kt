package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.data.CitasStore
import equipocitasmedicas.citasmedicas.databinding.ActivityAgendarCitaPacienteBinding
import equipocitasmedicas.citasmedicas.model.DoctorItem
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

class AgendarCitaPacienteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgendarCitaPacienteBinding
    private var doctorSeleccionado: DoctorItem? = null
    private var fechaSeleccionada: LocalDate? = null
    private var horaSeleccionada: LocalTime? = null

    // Lista de doctores (fotoUrl opcional)
    private val listaDoctores = listOf(
        DoctorItem("Dr. Juan Pérez", "Cardiología", "Consultorio 101"),
        DoctorItem("Dra. Ana Gómez", "Pediatría", "Consultorio 202"),
        DoctorItem("Dr. Luis Martínez", "Dermatología", "Consultorio 303"),
        DoctorItem("Dra. Carmen Rodríguez", "Ginecología", "Consultorio 404"),
        DoctorItem("Dr. Andrés López", "Odontología", "Consultorio 505")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgendarCitaPacienteBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RecyclerView de doctores
        binding.rvMedicos.layoutManager = LinearLayoutManager(this)
        binding.rvMedicos.adapter = DoctorAdapter(listaDoctores) { doctor ->
            doctorSeleccionado = doctor
            Toast.makeText(this, "Doctor seleccionado: ${doctor.nombreCompleto}", Toast.LENGTH_SHORT).show()
        }

        // Picker de fecha
        binding.etFecha.setOnClickListener {
            val hoy = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    binding.etFecha.setText("$dayOfMonth/${month + 1}/$year")
                },
                hoy.get(Calendar.YEAR),
                hoy.get(Calendar.MONTH),
                hoy.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.etHora.setOnClickListener {
            val ahora = Calendar.getInstance()
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    binding.etHora.setText(String.format("%02d:%02d", hour, minute))
                },
                ahora.get(Calendar.HOUR_OF_DAY),
                ahora.get(Calendar.MINUTE),
                true
            ).show()
        }

        // Confirmar cita
        binding.btnConfirmarCita.setOnClickListener {
            val motivo = binding.etMotivo.text.toString()

            if (doctorSeleccionado != null && fechaSeleccionada != null && horaSeleccionada != null) {
                val pacienteId = getSharedPreferences("app_session", MODE_PRIVATE)
                    .getLong("LOGGED_USER_ID", -1L)
                CitasStore.addCita(
                    pacienteId,
                    doctorSeleccionado!!,
                    fechaSeleccionada.toString(),
                    horaSeleccionada.toString(),
                    motivo
                )
                Toast.makeText(this, "Cita agendada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Completa todos los campos y selecciona un doctor", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancelar cita
        binding.btnCancelarCita.setOnClickListener {
            finish()
        }
    }
}