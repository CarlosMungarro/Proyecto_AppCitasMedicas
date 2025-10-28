// app/src/main/java/equipocitasmedicas/citasmedicas/ui/DisponibilidadActivity.kt
package equipocitasmedicas.citasmedicas.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.privacysandbox.tools.core.validator.ValidationResult
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import java.text.SimpleDateFormat
import java.util.*

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
    private lateinit var btnBack: ImageView
    private lateinit var btnGuardarCambios: Button

    private lateinit var medicoUid: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disponibilidad)

        medicoUid = getSharedPreferences("app_session", MODE_PRIVATE)
            .getString("LOGGED_USER_ID", null)
            ?: run {
                Toast.makeText(this, "Sesión no encontrada. Inicia sesión.", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

        initViews()
        setupListeners()
        cargarDisponibilidad()
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
        btnBack = findViewById(R.id.btnAtras)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        switchLunes.setOnCheckedChangeListener { _, isChecked -> horariosLunes.visibility = if (isChecked) View.VISIBLE else View.GONE }
        switchMartes.setOnCheckedChangeListener { _, isChecked -> horariosMartes.visibility = if (isChecked) View.VISIBLE else View.GONE }
        switchMiercoles.setOnCheckedChangeListener { _, isChecked -> horariosMiercoles.visibility = if (isChecked) View.VISIBLE else View.GONE }
        switchJueves.setOnCheckedChangeListener { _, isChecked -> horariosJueves.visibility = if (isChecked) View.VISIBLE else View.GONE }
        switchViernes.setOnCheckedChangeListener { _, isChecked -> horariosViernes.visibility = if (isChecked) View.VISIBLE else View.GONE }

        setupDeleteButtons()
        btnGuardarCambios.setOnClickListener { guardarCambios() }
    }

    private fun setupDeleteButtons() {
        val ids = listOf(
            R.id.btnEliminarLunes1 to R.id.etHoraInicioLunes1,
            R.id.btnEliminarLunes2 to R.id.etHoraInicioLunes2,
            R.id.btnEliminarMartes1 to R.id.etHoraInicioMartes1,
            R.id.btnEliminarMartes2 to R.id.etHoraInicioMartes2,
            R.id.btnEliminarMiercoles1 to R.id.etHoraInicioMiercoles1,
            R.id.btnEliminarMiercoles2 to R.id.etHoraInicioMiercoles2,
            R.id.btnEliminarJueves1 to R.id.etHoraInicioJueves1,
            R.id.btnEliminarJueves2 to R.id.etHoraInicioJueves2,
            R.id.btnEliminarViernes1 to R.id.etHoraInicioViernes1,
            R.id.btnEliminarViernes2 to R.id.etHoraInicioViernes2
        )
        ids.forEach { (btnId, etId) ->
            findViewById<TextView>(btnId)?.setOnClickListener {
                findViewById<EditText>(etId)?.setText("")
            }
        }
    }

    private fun guardarCambios() {
        val precio = etPrecio.text.toString().trim()
        if (precio.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa el precio por consulta", Toast.LENGTH_SHORT).show()
            return
        }

        val diasValidar = listOf(
            Triple(switchLunes, Calendar.MONDAY, "Lunes"),
            Triple(switchMartes, Calendar.TUESDAY, "Martes"),
            Triple(switchMiercoles, Calendar.WEDNESDAY, "Miércoles"),
            Triple(switchJueves, Calendar.THURSDAY, "Jueves"),
            Triple(switchViernes, Calendar.FRIDAY, "Viernes")
        )

        for ((switch, diaSemana, nombreDia) in diasValidar) {
            if (switch.isChecked) {
                val horarios = obtenerHorariosDia(nombreDia)
                if (horarios.isEmpty()) {
                    Toast.makeText(this, "Por favor ingresa al menos un horario para $nombreDia", Toast.LENGTH_SHORT).show()
                    return
                }

            }
        }

        guardarDisponibilidadEnFirebase(precio)
    }

    private fun guardarDisponibilidadEnFirebase(precio: String) {
        val disponibilidadCollection = db.collection("medicos")
            .document(medicoUid)
            .collection("disponibilidad")

        val dias = listOf(
            Triple("lunes", switchLunes, obtenerHorariosDia("Lunes")),
            Triple("martes", switchMartes, obtenerHorariosDia("Martes")),
            Triple("miercoles", switchMiercoles, obtenerHorariosDia("Miércoles")),
            Triple("jueves", switchJueves, obtenerHorariosDia("Jueves")),
            Triple("viernes", switchViernes, obtenerHorariosDia("Viernes"))
        )

        dias.forEach { (nombreDia, switchDia, horarios) ->
            disponibilidadCollection.document(nombreDia).set(
                mapOf(
                    "activo" to switchDia.isChecked,
                    "horarios" to horarios
                )
            )
        }

        disponibilidadCollection.document("precio").set(mapOf("valor" to precio))
            .addOnSuccessListener {
                Toast.makeText(this, "Disponibilidad y precio guardados correctamente", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar precio: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun cargarDisponibilidad() {
        val disponibilidadCollection = db.collection("medicos")
            .document(medicoUid)
            .collection("disponibilidad")

        disponibilidadCollection.get().addOnSuccessListener { querySnapshot ->
            querySnapshot.documents.forEach { doc ->
                when (doc.id) {
                    "lunes" -> {
                        switchLunes.isChecked = doc.getBoolean("activo") ?: false
                        setHorarios("Lunes", doc.get("horarios") as? List<String> ?: emptyList())
                    }
                    "martes" -> {
                        switchMartes.isChecked = doc.getBoolean("activo") ?: false
                        setHorarios("Martes", doc.get("horarios") as? List<String> ?: emptyList())
                    }
                    "miercoles" -> {
                        switchMiercoles.isChecked = doc.getBoolean("activo") ?: false
                        setHorarios("Miércoles", doc.get("horarios") as? List<String> ?: emptyList())
                    }
                    "jueves" -> {
                        switchJueves.isChecked = doc.getBoolean("activo") ?: false
                        setHorarios("Jueves", doc.get("horarios") as? List<String> ?: emptyList())
                    }
                    "viernes" -> {
                        switchViernes.isChecked = doc.getBoolean("activo") ?: false
                        setHorarios("Viernes", doc.get("horarios") as? List<String> ?: emptyList())
                    }
                    "precio" -> {
                        val precioValor = doc.getString("valor") ?: ""
                        etPrecio.setText(precioValor)
                    }
                }
            }
        }
    }

    private fun setHorarios(dia: String, horarios: List<String>) {
        val ids = when (dia) {
            "Lunes" -> listOf(R.id.etHoraInicioLunes1, R.id.etHoraInicioLunes2)
            "Martes" -> listOf(R.id.etHoraInicioMartes1, R.id.etHoraInicioMartes2)
            "Miércoles", "Miercoles" -> listOf(R.id.etHoraInicioMiercoles1, R.id.etHoraInicioMiercoles2)
            "Jueves" -> listOf(R.id.etHoraInicioJueves1, R.id.etHoraInicioJueves2)
            "Viernes" -> listOf(R.id.etHoraInicioViernes1, R.id.etHoraInicioViernes2)
            else -> emptyList()
        }
        ids.forEachIndexed { index, id ->
            if (index < horarios.size) {
                findViewById<EditText>(id)?.setText(horarios[index])
            }
        }
    }

    private fun obtenerHorariosDia(dia: String): List<String> {
        val ids = when (dia) {
            "Lunes" -> listOf(R.id.etHoraInicioLunes1, R.id.etHoraInicioLunes2)
            "Martes" -> listOf(R.id.etHoraInicioMartes1, R.id.etHoraInicioMartes2)
            "Miércoles", "Miercoles" -> listOf(R.id.etHoraInicioMiercoles1, R.id.etHoraInicioMiercoles2)
            "Jueves" -> listOf(R.id.etHoraInicioJueves1, R.id.etHoraInicioJueves2)
            "Viernes" -> listOf(R.id.etHoraInicioViernes1, R.id.etHoraInicioViernes2)
            else -> emptyList()
        }
        return ids.mapNotNull { findViewById<EditText>(it)?.text?.toString()?.takeIf(String::isNotEmpty) }
    }



    private fun calcularHoraFin(horaInicio: String, duracionConsultaMinutos: Int = 30): String {
        val partes = horaInicio.split(":")
        if (partes.size != 2) return "23:59"
        val hora = partes[0].toIntOrNull() ?: return "23:59"
        val minuto = partes[1].toIntOrNull() ?: return "23:59"
        val totalMin = hora * 60 + minuto + duracionConsultaMinutos
        val nuevaHora = totalMin / 60
        val nuevoMinuto = totalMin % 60
        return String.format("%02d:%02d", nuevaHora, nuevoMinuto)
    }




}

