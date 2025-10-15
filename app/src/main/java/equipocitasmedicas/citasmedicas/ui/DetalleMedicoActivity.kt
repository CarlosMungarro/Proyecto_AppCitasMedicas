package equipocitasmedicas.citasmedicas.ui


import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import equipocitasmedicas.citasmedicas.R

class DetalleMedicoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_medico)

        val btnAtras = findViewById<ImageView>(R.id.btnAtras)
        btnAtras.setOnClickListener {
            val intent = Intent(this, MisCitasMedicoActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
