package equipocitasmedicas.citasmedicas.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Cita
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp
import java.util.Calendar
class DetallePacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private var citaActual: Cita? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_paciente)

        val ivPerfil = findViewById<ImageView>(R.id.ivPerfilDetalle)
        val tvDoctor = findViewById<TextView>(R.id.tvDoctorDetalle)
        val tvEspecialidad = findViewById<TextView>(R.id.tvEspecialidadDetalle)
        val tvFecha = findViewById<TextView>(R.id.tvFechaDetalle)
        val tvHora = findViewById<TextView>(R.id.tvHoraDetalle)
        val tvMotivo = findViewById<TextView>(R.id.tvMotivoDetalle)
        val tvEstado = findViewById<TextView>(R.id.tvEstadoDetalle)
        val btnVerReceta = findViewById<Button>(R.id.btnVerReceta)
        val btnCancelarCita = findViewById<Button>(R.id.btnCancelarCita)
        val btnReprogramar = findViewById<Button>(R.id.btnReprogramar)
        val btnAtras = findViewById<ImageView>(R.id.btnAtras)

        val citaId = intent.getStringExtra("CITA_ID")
        if (citaId.isNullOrEmpty()) {
            tvDoctor.text = "Error: cita no encontrada"
            return
        }

        // Cargar datos desde Firebase
        db.collection("citas").document(citaId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val cita = document.toObject(Cita::class.java)
                    cita?.id = citaId
                    citaActual = cita

                    cita?.let {
                        tvDoctor.text = it.medicoNombre
                        tvEspecialidad.text = it.medicoEspecialidad
                        tvMotivo.text = it.notas.ifEmpty { "Sin motivo especificado" }

                        // Fecha y hora
                        it.fechaHora?.toDate()?.let { fecha ->
                            val formatoFecha = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
                            val formatoHora = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            tvFecha.text = formatoFecha.format(fecha)
                            tvHora.text = formatoHora.format(fecha)
                        }

                        ivPerfil.setImageResource(R.drawable.perfil_default)

                        // Mostrar estado con color
                        val (textoEstado, colorEstado) = when (it.estado.lowercase(Locale.getDefault())) {
                            "pendiente" -> "Pendiente" to R.color.estado_pendiente
                            "confirmada" -> "Confirmada" to R.color.estado_confirmada
                            "completada" -> "Completada" to R.color.estado_completada
                            "cancelada" -> "Cancelada" to R.color.estado_cancelada
                            else -> "Desconocido" to R.color.estado_pendiente
                        }

                        tvEstado.text = textoEstado
                        tvEstado.backgroundTintList =
                            ContextCompat.getColorStateList(this, colorEstado)

                        // ✅ FUNCIONALIDAD #9: No permitir cancelar si está completada o cancelada
                        if (it.estado.lowercase() == "completada" || it.estado.lowercase() == "cancelada") {
                            btnCancelarCita.isEnabled = false
                            btnCancelarCita.alpha = 0.5f
                            btnCancelarCita.text = "No se puede cancelar"
                        }

                        // ✅ Deshabilitar reprogramar si está cancelada o completada
                        if (it.estado.lowercase() == "completada" || it.estado.lowercase() == "cancelada") {
                            btnReprogramar.isEnabled = false
                            btnReprogramar.alpha = 0.5f
                        }
                    }
                } else {
                    tvDoctor.text = "Error: cita no encontrada"
                }
            }
            .addOnFailureListener {
                tvDoctor.text = "Error al cargar datos"
            }

        btnAtras.setOnClickListener { finish() }

        btnVerReceta.setOnClickListener {
            // Abrir receta si existe
        }

        // ✅ FUNCIONALIDAD #7: Botón Reprogramar con modal de confirmación
        btnReprogramar.setOnClickListener {
            if (citaActual == null) {
                Toast.makeText(this, "Error: No se pudo cargar la cita", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mostrarDialogoReprogramar(citaActual!!)
        }

        btnCancelarCita.setOnClickListener {
            if (citaId.isNullOrEmpty()) return@setOnClickListener

            // ✅ Verificar el estado antes de cancelar
            if (citaActual?.estado?.lowercase() == "completada") {
                Toast.makeText(this, "No se puede cancelar una cita completada", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (citaActual?.estado?.lowercase() == "cancelada") {
                Toast.makeText(this, "Esta cita ya está cancelada", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cambiar el estado en Firebase
            db.collection("citas").document(citaId)
                .update("estado", "cancelada")
                .addOnSuccessListener {
                    // Actualizar UI inmediatamente
                    tvEstado.text = "Cancelada"
                    tvEstado.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.estado_cancelada)

                    // Deshabilitar el botón
                    btnCancelarCita.isEnabled = false
                    btnCancelarCita.alpha = 0.5f
                    btnCancelarCita.text = "No se puede cancelar"

                    Toast.makeText(this, "Cita cancelada correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cancelar la cita: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // ✅ FUNCIONALIDAD #7: Modal de confirmación para reprogramar
    private fun mostrarDialogoReprogramar(cita: Cita) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_reprogramar_cita, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etNuevaFecha = dialogView.findViewById<EditText>(R.id.etNuevaFecha)
        val etNuevaHora = dialogView.findViewById<EditText>(R.id.etNuevaHora)
        val btnConfirmarReprogramacion = dialogView.findViewById<Button>(R.id.btnConfirmarReprogramacion)
        val btnCancelarReprogramacion = dialogView.findViewById<Button>(R.id.btnCancelarReprogramacion)

        var nuevaFecha: Calendar? = null
        var nuevaHora: Calendar? = null

        // DatePicker para nueva fecha
        etNuevaFecha.setOnClickListener {
            val now = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth)
                nuevaFecha = cal
                etNuevaFecha.setText("${dayOfMonth}/${month + 1}/$year")
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
        }

        // TimePicker para nueva hora
        etNuevaHora.setOnClickListener {
            val now = Calendar.getInstance()
            TimePickerDialog(this, { _, hourOfDay, minute ->
                val cal = nuevaFecha ?: Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
                cal.set(Calendar.MINUTE, minute)
                nuevaHora = cal
                etNuevaHora.setText(String.format("%02d:%02d", hourOfDay, minute))
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show()
        }

        btnCancelarReprogramacion.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirmarReprogramacion.setOnClickListener {
            if (nuevaFecha == null || nuevaHora == null) {
                Toast.makeText(this, "Por favor selecciona fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Mostrar confirmación final
            val fechaFormateada = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(nuevaFecha!!.time)
            val horaFormateada = SimpleDateFormat("HH:mm", Locale.getDefault()).format(nuevaHora!!.time)

            AlertDialog.Builder(this)
                .setTitle("Confirmar reprogramación")
                .setMessage(
                    "¿Estás seguro de reprogramar esta cita?\n\n" +
                            "Nueva fecha: $fechaFormateada\n" +
                            "Nueva hora: $horaFormateada\n\n" +
                            "La cita anterior será cancelada automáticamente."
                )
                .setPositiveButton("Sí, reprogramar") { _, _ ->
                    reprogramarCita(cita, nuevaHora!!)
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        dialog.show()
    }

    private fun reprogramarCita(citaVieja: Cita, nuevaFechaHora: Calendar) {
        db.collection("citas").document(citaVieja.id)
            .update(
                mapOf(
                    "fechaHora" to Timestamp(nuevaFechaHora.time),
                    "estado" to "pendiente"
                )
            )
            .addOnSuccessListener {
                Toast.makeText(this, "Cita reprogramada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al reprogramar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}