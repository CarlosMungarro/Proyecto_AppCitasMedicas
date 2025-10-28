package equipocitasmedicas.citasmedicas.ui

import android.os.Bundle
import android.widget.Button
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

class DetallePacienteActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

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
        val btnAtras = findViewById<ImageView>(R.id.btnAtras)

        val citaId = intent.getStringExtra("CITA_ID")
        if (citaId.isNullOrEmpty()) {
            tvDoctor.text = "Error: cita no encontrada"
            return
        }

        // 🔹 Cargar datos desde Firebase
        db.collection("citas").document(citaId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val cita = document.toObject(Cita::class.java)
                    cita?.let {
                        tvDoctor.text = it.medicoNombre
                        tvEspecialidad.text = it.medicoEspecialidad
                        tvMotivo.text = it.notas.ifEmpty { "Sin motivo especificado" }

                        // 🔹 Fecha y hora
                        it.fechaHora?.toDate()?.let { fecha ->
                            val formatoFecha = SimpleDateFormat("dd 'de' MMMM, yyyy", Locale.getDefault())
                            val formatoHora = SimpleDateFormat("hh:mm a", Locale.getDefault())
                            tvFecha.text = formatoFecha.format(fecha)
                            tvHora.text = formatoHora.format(fecha)
                        }

                        ivPerfil.setImageResource(R.drawable.perfil_default)

                        // 🔹 Mostrar estado con color
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
            // 🔹 Abrir receta si existe
        }

        btnCancelarCita.setOnClickListener {
            if (citaId.isNullOrEmpty()) return@setOnClickListener

            // Cambiar el estado en Firebase
            db.collection("citas").document(citaId)
                .update("estado", "cancelada")
                .addOnSuccessListener {
                    // Actualizar UI inmediatamente
                    tvEstado.text = "Cancelada"
                    tvEstado.backgroundTintList =
                        ContextCompat.getColorStateList(this, R.color.estado_cancelada)

                    Toast.makeText(this, "Cita cancelada correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cancelar la cita: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

    }
}






