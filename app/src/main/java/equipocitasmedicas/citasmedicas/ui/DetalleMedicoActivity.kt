package equipocitasmedicas.citasmedicas.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.databinding.ActivityDetalleMedicoBinding
import java.text.SimpleDateFormat
import java.util.*

class DetalleMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleMedicoBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 🔹 Obtener datos de la cita desde el Intent
        val citaId = intent.getStringExtra("citaId") ?: ""  // ID del documento en Firestore
        val nombrePaciente = intent.getStringExtra("nombrePaciente") ?: "--"
        val motivo = intent.getStringExtra("motivo") ?: "--"
        var estado = intent.getStringExtra("estado") ?: "--"
        val fechaHoraMillis = intent.getLongExtra("fechaHora", 0L)
        val fechaNacimiento = intent.getStringExtra("fechaNacimiento") ?: "--"
        val genero = intent.getStringExtra("genero") ?: "--"
        val telefono = intent.getStringExtra("telefono") ?: "--"

        // Fecha y hora
        val fechaTexto = if (fechaHoraMillis > 0L) {
            val fecha = Date(fechaHoraMillis)
            val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale("es", "MX"))
            val formatoHora = SimpleDateFormat("HH:mm", Locale("es", "MX"))
            "Cita hora: ${formatoHora.format(fecha)} / Fecha: ${formatoFecha.format(fecha)}"
        } else {
            "Cita hora: --:-- / Fecha: --/--/----"
        }
        binding.tvDatosCita.text = fechaTexto

        // Motivo
        binding.tvMotivoTexto.text = motivo

        // Función para actualizar estado en UI
        fun actualizarEstadoUI(estadoNuevo: String) {
            val (textoEstado, colorEstado) = when (estadoNuevo.lowercase(Locale.getDefault())) {
                "pendiente" -> "Pendiente" to R.color.estado_pendiente
                "confirmada" -> "Confirmada" to R.color.estado_confirmada
                "completada" -> "Completada" to R.color.estado_completada
                "cancelada" -> "Cancelada" to R.color.estado_cancelada
                else -> "Desconocido" to R.color.estado_pendiente
            }
            binding.tvEstado.text = textoEstado
            binding.tvEstado.backgroundTintList =
                ContextCompat.getColorStateList(this, colorEstado)
        }

        actualizarEstadoUI(estado)

        // Datos del paciente
        binding.tvNombre.text = "Nombre: $nombrePaciente"
        binding.tvGenero.text = "Género: $genero"
        binding.tvTelefono.text = "Teléfono: $telefono"

        // Calcular edad si hay fecha de nacimiento
        val edadTexto = if (fechaNacimiento != "--") {
            try {
                val formatoNacimiento = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val nacimiento = formatoNacimiento.parse(fechaNacimiento)
                nacimiento?.let {
                    val today = Calendar.getInstance()
                    val birth = Calendar.getInstance()
                    birth.time = it
                    var edad = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
                    if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) edad--
                    "$edad años"
                } ?: "--"
            } catch (e: Exception) { "--" }
        } else "--"
        binding.tvEdad.text = "Edad: $edadTexto"

        // 🔹 Botón Cancelar Cita con Firestore
        binding.btnCancelar.setOnClickListener {
            if (estado.lowercase() != "cancelada") {
                if (citaId.isNotEmpty()) {
                    db.collection("citas").document(citaId)
                        .update("estado", "cancelada")
                        .addOnSuccessListener {
                            estado = "cancelada"
                            actualizarEstadoUI(estado)
                            Toast.makeText(this, "Cita cancelada correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al cancelar cita: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "ID de cita no encontrado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "La cita ya está cancelada", Toast.LENGTH_SHORT).show()
            }
        }
        // 🔹 Botón Finalizar Cita con Firestore
        binding.btnFinalizar.setOnClickListener {
            if (estado.lowercase() != "completada") {
                if (citaId.isNotEmpty()) {
                    db.collection("citas").document(citaId)
                        .update("estado", "completada")
                        .addOnSuccessListener {
                            estado = "completada"
                            actualizarEstadoUI(estado)
                            Toast.makeText(this, "Cita finalizada correctamente", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al finalizar cita: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "ID de cita no encontrado", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "La cita ya está finalizada", Toast.LENGTH_SHORT).show()
            }
        }


        // Botón regresar
        binding.btnAtras.setOnClickListener { finish() }
    }
}







