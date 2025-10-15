package equipocitasmedicas.citasmedicas.ui

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import equipocitasmedicas.citasmedicas.R

class DetalleMedicoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_medico)

        val btnAtras = findViewById<ImageView>(R.id.btnAtras)
        val btnReprogramar = findViewById<Button>(R.id.btnReprogramar)
        val btnCancelar = findViewById<Button>(R.id.btnCancelar)
        val btnAgregarArchivo = findViewById<Button>(R.id.btnAgregarArchivo)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)

        val tvDatosCita = findViewById<TextView>(R.id.tvDatosCita)
        val tvNombre = findViewById<TextView>(R.id.tvNombre)
        val tvEdad = findViewById<TextView>(R.id.tvEdad)
        val tvGenero = findViewById<TextView>(R.id.tvGenero)
        val tvTelefono = findViewById<TextView>(R.id.tvTelefono)
        val tvMotivo = findViewById<TextView>(R.id.tvMotivoTexto)

        // 🟢 Recibir datos del intent
        val nombre = intent.getStringExtra("nombrePaciente") ?: "Desconocido"
        val fechaHora = intent.getStringExtra("fechaHora") ?: "-"
        val motivo = intent.getStringExtra("motivo") ?: "-"
        val genero = intent.getStringExtra("genero") ?: "-"
        val telefono = intent.getStringExtra("telefono") ?: "-"
        val edad = intent.getStringExtra("edad") ?: "-"

        tvDatosCita.text = "Cita: $fechaHora"
        tvNombre.text = "Nombre: $nombre"
        tvEdad.text = "Edad: $edad"
        tvGenero.text = "Género: $genero"
        tvTelefono.text = "Teléfono: $telefono"
        tvMotivo.text = motivo

        btnAtras.setOnClickListener { finish() }
        btnReprogramar.setOnClickListener { /* lógica */ }
        btnCancelar.setOnClickListener { /* lógica */ }
        btnAgregarArchivo.setOnClickListener { /* lógica */ }
        btnFinalizar.setOnClickListener { /* lógica */ }
    }
}
