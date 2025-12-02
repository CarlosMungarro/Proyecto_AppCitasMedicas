// app/src/main/java/equipocitasmedicas/citasmedicas/ui/DisponibilidadActivity.kt
package equipocitasmedicas.citasmedicas.ui

import android.app.TimePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import java.util.*
import kotlin.math.min

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

    private lateinit var etDuracion: EditText
    private lateinit var etPrecio: EditText
    private lateinit var btnBack: ImageView
    private lateinit var btnGuardarCambios: Button

    private lateinit var medicoUid: String
    private val db = FirebaseFirestore.getInstance()

    // why: tener una estructura clara por día simplifica render y guardado
    private data class DayUi(
        val name: String,
        val switch: SwitchCompat,
        val container: LinearLayout
    )

    private lateinit var days: List<DayUi>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disponibilidad)

        medicoUid = getSharedPreferences("app_session", MODE_PRIVATE)
            .getString("LOGGED_USER_ID", null)
            ?: run { Toast.makeText(this, "Sesión no encontrada. Inicia sesión.", Toast.LENGTH_SHORT).show(); finish(); return }

        initViews()
        setupDays()
        setupListeners()
        setupVisibilityBySwitch()
        addPlusButtons()      // ＋ por día
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

        etDuracion = findViewById(R.id.etDuracion)
        etPrecio = findViewById(R.id.etPrecio)
        btnBack = findViewById(R.id.btnAtras)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
    }

    private fun setupDays() {
        days = listOf(
            DayUi("lunes", switchLunes, horariosLunes),
            DayUi("martes", switchMartes, horariosMartes),
            DayUi("miercoles", switchMiercoles, horariosMiercoles),
            DayUi("jueves", switchJueves, horariosJueves),
            DayUi("viernes", switchViernes, horariosViernes)
        )
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnGuardarCambios.setOnClickListener { guardarCambios() }
        days.forEach { d ->
            d.switch.setOnCheckedChangeListener { _, checked ->
                d.container.visibility = if (checked) View.VISIBLE else View.GONE
                if (checked && countRangeRows(d.container) == 0) addRangeRow(d.container)
            }
        }
    }

    private fun setupVisibilityBySwitch() {
        days.forEach { d ->
            d.container.visibility = if (d.switch.isChecked) View.VISIBLE else View.GONE
        }
    }

    // ------- UI dinámica: filas de rango + botón ＋ --------

    private fun addPlusButtons() {
        days.forEach { ensurePlusButton(it.container) }
    }

    private fun ensurePlusButton(parent: LinearLayout) {
        // si ya existe, nada
        val tag = "plus_button"
        val existing = parent.findViewWithTag<View>(tag)
        if (existing != null) return

        val btn = Button(this).apply {
            text = "＋"
            textSize = 18f
            isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.END
                topMargin = dp(8)
            }
        }
        btn.setOnClickListener { addRangeRow(parent) }

        parent.addView(btn)
    }

    private fun addRangeRow(parent: LinearLayout, start: String? = null, end: String? = null) {
        // insertar la fila siempre ANTES del botón ＋ (último hijo)
        val insertIndex = maxOf(0, parent.childCount - 1)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
            gravity = Gravity.CENTER_VERTICAL
        }

        val labelInicio = makeSmallLabel("Hora inicio")
        val etInicio = makeTimeEditText().apply { start?.let { setText(it) } }
        val labelFin = makeSmallLabel("Hora fin")
        val etFin = makeTimeEditText().apply { end?.let { setText(it) } }
        val btnDel = makeDeleteButton()

        row.addView(labelInicio)
        row.addView(etInicio, LinearLayout.LayoutParams(0, dp(40), 1f).apply { marginEnd = dp(8) })
        row.addView(labelFin)
        row.addView(etFin, LinearLayout.LayoutParams(0, dp(40), 1f).apply { marginEnd = dp(8) })
        row.addView(btnDel, LinearLayout.LayoutParams(dp(36), dp(36)))

        // events
        etInicio.setOnClickListener { showTimePicker(etInicio) }
        etFin.setOnClickListener { showTimePicker(etFin) }
        btnDel.setOnClickListener {
            parent.removeView(row)
            // deja siempre al menos 1 fila si el día está activo
            val day = days.firstOrNull { it.container == parent }
            if (day?.switch?.isChecked == true && countRangeRows(parent) == 0) addRangeRow(parent)
        }

        parent.addView(row, insertIndex)
    }

    private fun countRangeRows(parent: LinearLayout): Int {
        val plus = parent.findViewWithTag<View>("plus_button")
        val total = parent.childCount
        return if (plus == null) total else maxOf(0, total - 1)
    }

    private fun makeTimeEditText(): EditText = EditText(this).apply {
        isFocusable = false
        isClickable = true
        hint = "HH:mm"
        textSize = 14f
        setPadding(dp(10), dp(6), dp(10), dp(6))
        background = GradientDrawable().apply {
            cornerRadius = dp(8).toFloat()
            setColor(0xFFF8F8F8.toInt())
        }
    }

    private fun makeDeleteButton(): ImageButton = ImageButton(this).apply {
        setImageResource(android.R.drawable.ic_menu_delete)
        background = GradientDrawable().apply {
            cornerRadius = dp(18).toFloat()
            setColor(0xFFE53935.toInt())
        }
        scaleType = ImageView.ScaleType.CENTER
        setColorFilter(0xFFFFFFFF.toInt())
    }

    private fun makeSmallLabel(text: String) = TextView(this).apply {
        this.text = text
        textSize = 12f
        setTextColor(0xFF666666.toInt())
        val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        p.marginEnd = dp(8)
        layoutParams = p
    }

    private fun dp(px: Int) = (px * resources.displayMetrics.density).toInt()

    private fun showTimePicker(target: EditText) {
        val (h, m) = parseOrNow(target.text?.toString())
        TimePickerDialog(this, { _, hour, minute ->
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        }, h, m, true).show()
    }

    private fun parseOrNow(text: String?): Pair<Int, Int> {
        return try {
            if (text.isNullOrBlank()) throw IllegalArgumentException()
            val (h, m) = text.split(":")
            Pair(h.toInt(), m.toInt())
        } catch (_: Exception) {
            val cal = Calendar.getInstance()
            Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }
    }

    // ----------- Guardar / Cargar Firestore ------------

    private fun guardarCambios() {
        val duracion = etDuracion.text.toString().trim()
        val precio = etPrecio.text.toString().trim()
        if (duracion.isEmpty()) { toast("Por favor ingresa la duración de la cita"); return }
        if (precio.isEmpty()) { toast("Por favor ingresa el precio por consulta"); return }

        val duracionInt = duracion.toIntOrNull()
        if (duracionInt == null || duracionInt <= 0) {
            toast("La duración debe ser un número positivo")
            return
        }

        // Validaciones por día
        for (d in days) {
            if (!d.switch.isChecked) continue
            val rangos = readRangesFrom(d.container)
            if (rangos.isEmpty()) { toast("Agrega al menos un rango para ${d.name}"); return }
            if (!validateRanges(rangos)) { toast("Rangos inválidos u overlapeados en ${d.name}"); return }
        }

        val col = db.collection("medicos").document(medicoUid).collection("disponibilidad")
        // Guardar todos los días
        days.forEach { d ->
            val rangos = if (d.switch.isChecked) readRangesFrom(d.container) else emptyList()
            val payload = hashMapOf<String, Any>(
                "activo" to d.switch.isChecked,
                "rangos" to rangos.map { mapOf("inicio" to it.first, "fin" to it.second) }
            )
            col.document(d.name).set(payload)
        }
        // Guardar duración y precio
        col.document("duracion").set(mapOf("minutos" to duracionInt))
        col.document("precio").set(mapOf("valor" to precio))
            .addOnSuccessListener { toast("Disponibilidad, duración y precio guardados") }
            .addOnFailureListener { e -> toast("Error al guardar: ${e.message}") }
    }

    private fun readRangesFrom(parent: LinearLayout): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val childCount = parent.childCount
        val limit = if (parent.findViewWithTag<View>("plus_button") != null) childCount - 1 else childCount
        for (i in 0 until limit) {
            val row = parent.getChildAt(i) as? LinearLayout ?: continue
            // estructura: label, etInicio, label, etFin, btnDel
            if (row.childCount < 5) continue
            val etInicio = row.getChildAt(1) as? EditText
            val etFin = row.getChildAt(3) as? EditText
            val inicio = etInicio?.text?.toString()?.trim().orEmpty()
            val fin = etFin?.text?.toString()?.trim().orEmpty()
            if (inicio.isNotEmpty() && fin.isNotEmpty()) result += inicio to fin
        }
        return result
    }

    private fun validateRanges(rangos: List<Pair<String, String>>): Boolean {
        // formato y orden + que no se solapen
        val toMin = { s: String ->
            try {
                val (h, m) = s.split(":")
                val hh = h.toInt(); val mm = m.toInt()
                if (hh !in 0..23 || mm !in 0..59) -1 else hh * 60 + mm
            } catch (_: Exception) { -1 }
        }
        val minutes = rangos.map { toMin(it.first) to toMin(it.second) }
        if (minutes.any { it.first < 0 || it.second < 0 || it.first >= it.second }) return false
        val sorted = minutes.sortedBy { it.first }
        for (i in 1 until sorted.size) {
            val prev = sorted[i - 1]
            val curr = sorted[i]
            if (curr.first < prev.second) return false // overlap
        }
        return true
    }

    private fun cargarDisponibilidad() {
        val col = db.collection("medicos").document(medicoUid).collection("disponibilidad")
        col.get().addOnSuccessListener { qs ->
            // limpiar contenedores y re-crear botón +
            days.forEach { day ->
                day.container.removeAllViews()
                ensurePlusButton(day.container)
            }

            qs.documents.forEach { doc ->
                if (doc.id == "precio") {
                    etPrecio.setText(doc.getString("valor") ?: "")
                    return@forEach
                }
                if (doc.id == "duracion") {
                    val minutos = doc.getLong("minutos")?.toInt() ?: 30
                    etDuracion.setText(minutos.toString())
                    return@forEach
                }
                val day = days.firstOrNull { it.name == doc.id } ?: return@forEach
                val activo = doc.getBoolean("activo") ?: false
                day.switch.isChecked = activo
                day.container.visibility = if (activo) View.VISIBLE else View.GONE

                val rangos = (doc.get("rangos") as? List<*>)?.mapNotNull { it as? Map<*, *> } ?: emptyList()
                if (rangos.isEmpty()) {
                    if (activo) addRangeRow(day.container)
                } else {
                    rangos.forEach { r ->
                        val inicio = r["inicio"]?.toString()
                        val fin = r["fin"]?.toString()
                        addRangeRow(day.container, inicio, fin)
                    }
                }
            }

            // si algún día activo quedó sin filas, añade 1
            days.forEach { d ->
                if (d.switch.isChecked && countRangeRows(d.container) == 0) addRangeRow(d.container)
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
