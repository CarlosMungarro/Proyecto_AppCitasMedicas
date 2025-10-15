package equipocitasmedicas.citasmedicas.ui

import equipocitasmedicas.citasmedicas.R
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

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
    private lateinit var btnBack: ImageView      // ✅ corregido: coincide con tu XML
    private lateinit var btnGuardarCambios: Button

    private val medicoId: Long = 1L // ID del médico (ajusta según tu lógica)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disponibilidad) // Asegúrate de que este XML es el correcto

        initViews()
        setupListeners()

        // Datos de prueba (puedes quitarlo luego)
        CitaStore.agregarDatosPrueba(medicoId)
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
        btnBack = findViewById(R.id.btnAtras) // ✅ coincide con el XML (ImageView)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish() // vuelve atrás sin cerrar abruptamente
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

                val resultado = validarHorarioDia(diaSemana, horarios, nombreDia)
                if (!resultado.isValid) {
                    mostrarDialogoConflicto(resultado, nombreDia)
                    return
                }
            }
        }

        guardarDisponibilidadValidada(precio)
    }

    private fun validarHorarioDia(diaSemana: Int, horarios: List<String>, nombreDia: String): ValidationResult {
        if (horarios.isEmpty()) return ValidationResult(true, "", emptyList())

        val horariosLimpios = horarios.map {
            it.replace(" am", "", true)
                .replace(" pm", "", true)
                .trim()
        }.sorted()

        val primerHorario = horariosLimpios.first()
        val ultimoHorario = horariosLimpios.last()
        val horaFin = calcularHoraFin(ultimoHorario, 30)

        return CitaStore.validarDisponibilidad(
            medicoId = medicoId,
            diaSemana = diaSemana,
            horaInicio = primerHorario,
            horaFin = horaFin
        )
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

    private fun mostrarDialogoConflicto(resultado: ValidationResult, dia: String) {
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        val citasTexto = resultado.citasConflicto.joinToString("\n") { cita ->
            "• ${formato.format(cita.fechaHora)} - ${cita.nombrePaciente}\n  ${cita.motivo}"
        }

        AlertDialog.Builder(this)
            .setTitle("⚠️ Conflicto en $dia")
            .setMessage(
                "No puedes guardar esta disponibilidad porque tienes citas programadas que quedarían fuera del horario:\n\n$citasTexto"
            )
            .setPositiveButton("Entendido", null)
            .show()
    }

    private fun guardarDisponibilidadValidada(precio: String) {
        val disponibilidad = mapOf(
            "lunes" to mapOf("activo" to switchLunes.isChecked, "horarios" to obtenerHorariosDia("Lunes")),
            "martes" to mapOf("activo" to switchMartes.isChecked, "horarios" to obtenerHorariosDia("Martes")),
            "miercoles" to mapOf("activo" to switchMiercoles.isChecked, "horarios" to obtenerHorariosDia("Miércoles")),
            "jueves" to mapOf("activo" to switchJueves.isChecked, "horarios" to obtenerHorariosDia("Jueves")),
            "viernes" to mapOf("activo" to switchViernes.isChecked, "horarios" to obtenerHorariosDia("Viernes")),
            "precio" to precio
        )

        Toast.makeText(this, "Disponibilidad guardada correctamente", Toast.LENGTH_SHORT).show()
        println("Disponibilidad: $disponibilidad")
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

        return ids.mapNotNull {
            findViewById<EditText>(it)?.text?.toString()?.takeIf { t -> t.isNotEmpty() }
        }
    }
}