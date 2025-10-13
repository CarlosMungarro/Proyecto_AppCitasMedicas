package equipocitasmedicas.citasmedicas.ui
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import equipocitasmedicas.citasmedicas.R


class DisponibilidadActivity : AppCompatActivity() {

    private lateinit var switchLunes: SwitchCompat
    private lateinit var switchMartes: SwitchCompat
    private lateinit var switchMiercoles: SwitchCompat
    private lateinit var switchJueves: SwitchCompat
    private lateinit var switchViernes: SwitchCompat

    private lateinit var horariosLunes: LinearLayout
    private lateinit var horariosMartes: LinearLayout
    private lateinit var horariosMiercoles: LinearLayout
    private lateinit var horariosJueves: LinearLayout
    private lateinit var horariosViernes: LinearLayout

    private lateinit var etPrecio: EditText
    private lateinit var btnBack: TextView
    private lateinit var btnGuardarCambios: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disponibilidad)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        switchLunes = findViewById(R.id.switchLunes)
        switchMartes = findViewById(R.id.switchMartes)
        switchMiercoles = findViewById(R.id.switchMiercoles)
        switchJueves = findViewById(R.id.switchJueves)
        switchViernes = findViewById(R.id.switchViernes)

        horariosLunes = findViewById(R.id.horariosLunes)
        horariosMartes = findViewById(R.id.horariosMartes)
        horariosMiercoles = findViewById(R.id.horariosMiercoles)
        horariosJueves = findViewById(R.id.horariosJueves)
        horariosViernes = findViewById(R.id.horariosViernes)

        etPrecio = findViewById(R.id.etPrecio)
        btnBack = findViewById(R.id.btnBack)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        switchLunes.setOnCheckedChangeListener { _, isChecked ->
            horariosLunes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchMartes.setOnCheckedChangeListener { _, isChecked ->
            horariosMartes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchMiercoles.setOnCheckedChangeListener { _, isChecked ->
            horariosMiercoles.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchJueves.setOnCheckedChangeListener { _, isChecked ->
            horariosJueves.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        switchViernes.setOnCheckedChangeListener { _, isChecked ->
            horariosViernes.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        setupDeleteButtons()

        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }
    }

    private fun setupDeleteButtons() {
        findViewById<TextView>(R.id.btnEliminarLunes1).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioLunes1).setText("")
        }
        findViewById<TextView>(R.id.btnEliminarLunes2).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioLunes2).setText("")
        }

        findViewById<TextView>(R.id.btnEliminarMartes1).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioMartes1).setText("")
        }
        findViewById<TextView>(R.id.btnEliminarMartes2).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioMartes2).setText("")
        }

        findViewById<TextView>(R.id.btnEliminarMiercoles1).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioMiercoles1).setText("")
        }
        findViewById<TextView>(R.id.btnEliminarMiercoles2).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioMiercoles2).setText("")
        }

        findViewById<TextView>(R.id.btnEliminarJueves1).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioJueves1).setText("")
        }
        findViewById<TextView>(R.id.btnEliminarJueves2).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioJueves2).setText("")
        }

        findViewById<TextView>(R.id.btnEliminarViernes1).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioViernes1).setText("")
        }
        findViewById<TextView>(R.id.btnEliminarViernes2).setOnClickListener {
            findViewById<EditText>(R.id.etHoraInicioViernes2).setText("")
        }
    }

    private fun guardarCambios() {
        val precio = etPrecio.text.toString().trim()
        if (precio.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el precio por consulta", Toast.LENGTH_SHORT).show()
            return
        }

        val disponibilidad = mutableMapOf<String, Any>()

        disponibilidad["lunes"] = mapOf(
            "activo" to switchLunes.isChecked,
            "horarios" to obtenerHorariosDia("Lunes")
        )

        disponibilidad["martes"] = mapOf(
            "activo" to switchMartes.isChecked,
            "horarios" to obtenerHorariosDia("Martes")
        )

        disponibilidad["miercoles"] = mapOf(
            "activo" to switchMiercoles.isChecked,
            "horarios" to obtenerHorariosDia("Miercoles")
        )

        disponibilidad["jueves"] = mapOf(
            "activo" to switchJueves.isChecked,
            "horarios" to obtenerHorariosDia("Jueves")
        )

        disponibilidad["viernes"] = mapOf(
            "activo" to switchViernes.isChecked,
            "horarios" to obtenerHorariosDia("Viernes")
        )

        disponibilidad["precio"] = precio

        Toast.makeText(this, "Disponibilidad guardada correctamente", Toast.LENGTH_SHORT).show()

        println("Disponibilidad: $disponibilidad")
    }

    private fun obtenerHorariosDia(dia: String): List<String> {
        val horarios = mutableListOf<String>()

        when (dia) {
            "Lunes" -> {
                val hora1 = findViewById<EditText>(R.id.etHoraInicioLunes1).text.toString()
                val hora2 = findViewById<EditText>(R.id.etHoraInicioLunes2).text.toString()
                if (hora1.isNotEmpty()) horarios.add(hora1)
                if (hora2.isNotEmpty()) horarios.add(hora2)
            }
            "Martes" -> {
                val hora1 = findViewById<EditText>(R.id.etHoraInicioMartes1).text.toString()
                val hora2 = findViewById<EditText>(R.id.etHoraInicioMartes2).text.toString()
                if (hora1.isNotEmpty()) horarios.add(hora1)
                if (hora2.isNotEmpty()) horarios.add(hora2)
            }
            "Miercoles" -> {
                val hora1 = findViewById<EditText>(R.id.etHoraInicioMiercoles1).text.toString()
                val hora2 = findViewById<EditText>(R.id.etHoraInicioMiercoles2).text.toString()
                if (hora1.isNotEmpty()) horarios.add(hora1)
                if (hora2.isNotEmpty()) horarios.add(hora2)
            }
            "Jueves" -> {
                val hora1 = findViewById<EditText>(R.id.etHoraInicioJueves1).text.toString()
                val hora2 = findViewById<EditText>(R.id.etHoraInicioJueves2).text.toString()
                if (hora1.isNotEmpty()) horarios.add(hora1)
                if (hora2.isNotEmpty()) horarios.add(hora2)
            }
            "Viernes" -> {
                val hora1 = findViewById<EditText>(R.id.etHoraInicioViernes1).text.toString()
                val hora2 = findViewById<EditText>(R.id.etHoraInicioViernes2).text.toString()
                if (hora1.isNotEmpty()) horarios.add(hora1)
                if (hora2.isNotEmpty()) horarios.add(hora2)
            }
        }

        return horarios
    }
}