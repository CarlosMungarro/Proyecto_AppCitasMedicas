
package equipocitasmedicas.citasmedicas.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import equipocitasmedicas.citasmedicas.R

class DetallePacienteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_paciente) // Asegúrate de que este sea tu XML

        // Vinculamos los elementos del layout con el código
        val ivPerfil = findViewById<ImageView>(R.id.ivPerfilDetalle)
        val tvDoctor = findViewById<TextView>(R.id.tvDoctorDetalle)
        val tvEspecialidad = findViewById<TextView>(R.id.tvEspecialidadDetalle)
        val tvFecha = findViewById<TextView>(R.id.tvFechaDetalle)
        val tvHora = findViewById<TextView>(R.id.tvHoraDetalle)
        val tvMotivo = findViewById<TextView>(R.id.tvMotivoDetalle)

        val btnVerReceta = findViewById<Button>(R.id.btnVerReceta)
        val btnCancelarCita = findViewById<Button>(R.id.btnCancelarCita)

        // Obtenemos los datos enviados desde el Adapter (Intent extras)
        val doctor = intent.getStringExtra("doctor") ?: "Dr. Desconocido"
        val especialidad = intent.getStringExtra("especialidad") ?: "Especialidad"
        val fecha = intent.getStringExtra("fecha") ?: "Fecha"
        val hora = intent.getStringExtra("hora") ?: "Hora"
        val motivo = intent.getStringExtra("motivo") ?: "Motivo"

        // Mostramos los datos en los TextView
        tvDoctor.text = doctor
        tvEspecialidad.text = especialidad
        tvFecha.text = fecha
        tvHora.text = hora
        tvMotivo.text = motivo

        // Opcional: configurar botones
        btnVerReceta.setOnClickListener {
            // Aquí puedes abrir la receta en PDF o nueva actividad
        }

        btnCancelarCita.setOnClickListener {
            // Aquí puedes cancelar la cita
        }

        val btnAtras = findViewById<ImageView>(R.id.btnAtras)
        btnAtras.setOnClickListener {
            finish()
        }
    }
}


