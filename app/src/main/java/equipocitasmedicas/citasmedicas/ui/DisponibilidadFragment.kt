package equipocitasmedicas.citasmedicas.ui

import android.app.TimePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import java.util.*

class DisponibilidadFragment : Fragment() {

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
    private lateinit var btnGuardarCambios: Button

    private lateinit var medicoUid: String
    private val db = FirebaseFirestore.getInstance()

    private data class DayUi(val name: String, val switch: SwitchCompat, val container: LinearLayout)
    private lateinit var days: List<DayUi>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_disponibilidad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        medicoUid = requireContext().getSharedPreferences("app_session", android.content.Context.MODE_PRIVATE)
            .getString("LOGGED_USER_ID", null)
            ?: run { toast("Sesión no encontrada. Inicia sesión."); activity?.finish(); return }

        switchLunes = view.findViewById(R.id.switchLunes)
        switchMartes = view.findViewById(R.id.switchMartes)
        switchMiercoles = view.findViewById(R.id.switchMiercoles)
        switchJueves = view.findViewById(R.id.switchJueves)
        switchViernes = view.findViewById(R.id.switchViernes)

        horariosLunes = view.findViewById(R.id.horariosLunes)
        horariosMartes = view.findViewById(R.id.horariosMartes)
        horariosMiercoles = view.findViewById(R.id.horariosMiercoles)
        horariosJueves = view.findViewById(R.id.horariosJueves)
        horariosViernes = view.findViewById(R.id.horariosViernes)

        etDuracion = view.findViewById(R.id.etDuracion)
        etPrecio = view.findViewById(R.id.etPrecio)
        btnGuardarCambios = view.findViewById(R.id.btnGuardarCambios)

        days = listOf(
            DayUi("lunes", switchLunes, horariosLunes),
            DayUi("martes", switchMartes, horariosMartes),
            DayUi("miercoles", switchMiercoles, horariosMiercoles),
            DayUi("jueves", switchJueves, horariosJueves),
            DayUi("viernes", switchViernes, horariosViernes)
        )

        btnGuardarCambios.setOnClickListener { guardarCambios() }
        days.forEach { d ->
            d.switch.setOnCheckedChangeListener { _, checked ->
                d.container.visibility = if (checked) View.VISIBLE else View.GONE
                if (checked && countRangeRows(d.container) == 0) addRangeRow(d.container)
            }
            ensurePlusButton(d.container)
        }

        cargarDisponibilidad()
    }

    // -------- (misma lógica que tu Activity) --------
    private fun ensurePlusButton(parent: LinearLayout) {
        val tag = "plus_button"
        val existing = parent.findViewWithTag<View>(tag)
        if (existing != null) return
        val btn = Button(requireContext()).apply {
            text = "＋"; textSize = 18f; isAllCaps = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { gravity = Gravity.END; topMargin = dp(8) }
        }
        btn.setOnClickListener { addRangeRow(parent) }
        parent.addView(btn)
    }

    private fun addRangeRow(parent: LinearLayout, start: String? = null, end: String? = null) {
        val insertIndex = maxOf(0, parent.childCount - 1)
        val row = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
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

        etInicio.setOnClickListener { showTimePicker(etInicio) }
        etFin.setOnClickListener { showTimePicker(etFin) }
        btnDel.setOnClickListener {
            parent.removeView(row)
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

    private fun makeTimeEditText(): EditText = EditText(requireContext()).apply {
        isFocusable = false; isClickable = true; hint = "HH:mm"; textSize = 14f
        setPadding(dp(10), dp(6), dp(10), dp(6))
        background = GradientDrawable().apply { cornerRadius = dp(8).toFloat(); setColor(0xFFF8F8F8.toInt()) }
    }

    private fun makeDeleteButton(): ImageButton = ImageButton(requireContext()).apply {
        setImageResource(android.R.drawable.ic_menu_delete)
        background = GradientDrawable().apply { cornerRadius = dp(18).toFloat(); setColor(0xFFE53935.toInt()) }
        scaleType = ImageView.ScaleType.CENTER; setColorFilter(0xFFFFFFFF.toInt())
    }

    private fun makeSmallLabel(text: String) = TextView(requireContext()).apply {
        this.text = text; textSize = 12f; setTextColor(0xFF666666.toInt())
        val p = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        p.marginEnd = dp(8); layoutParams = p
    }

    private fun dp(px: Int) = (px * resources.displayMetrics.density).toInt()

    private fun showTimePicker(target: EditText) {
        val (h, m) = parseOrNow(target.text?.toString())
        TimePickerDialog(requireContext(), { _, hour, minute ->
            target.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute))
        }, h, m, true).show()
    }

    private fun parseOrNow(text: String?): Pair<Int, Int> = try {
        if (text.isNullOrBlank()) throw IllegalArgumentException()
        val (h, m) = text.split(":"); Pair(h.toInt(), m.toInt())
    } catch (_: Exception) {
        val cal = Calendar.getInstance(); Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
    }

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

        for (d in days) {
            if (!d.switch.isChecked) continue
            val rangos = readRangesFrom(d.container)
            if (rangos.isEmpty()) { toast("Agrega al menos un rango para ${d.name}"); return }
            if (!validateRanges(rangos)) { toast("Rangos inválidos u overlapeados en ${d.name}"); return }
        }

        val col = db.collection("medicos").document(medicoUid).collection("disponibilidad")
        days.forEach { d ->
            val rangos = if (d.switch.isChecked) readRangesFrom(d.container) else emptyList()
            val payload = hashMapOf<String, Any>(
                "activo" to d.switch.isChecked,
                "rangos" to rangos.map { mapOf("inicio" to it.first, "fin" to it.second) }
            )
            col.document(d.name).set(payload)
        }
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
        val toMin = { s: String ->
            try {
                val (h, m) = s.split(":"); val hh = h.toInt(); val mm = m.toInt()
                if (hh !in 0..23 || mm !in 0..59) -1 else hh * 60 + mm
            } catch (_: Exception) { -1 }
        }
        val minutes = rangos.map { toMin(it.first) to toMin(it.second) }
        if (minutes.any { it.first < 0 || it.second < 0 || it.first >= it.second }) return false
        val sorted = minutes.sortedBy { it.first }
        for (i in 1 until sorted.size) if (sorted[i].first < sorted[i - 1].second) return false
        return true
    }

    private fun cargarDisponibilidad() {
        val col = db.collection("medicos").document(medicoUid).collection("disponibilidad")
        col.get().addOnSuccessListener { qs ->
            days.forEach { d -> d.container.removeAllViews(); ensurePlusButton(d.container) }
            qs.documents.forEach { doc ->
                if (doc.id == "precio") { etPrecio.setText(doc.getString("valor") ?: ""); return@forEach }
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
                if (rangos.isEmpty()) { if (activo) addRangeRow(day.container) } else {
                    rangos.forEach { r -> addRangeRow(day.container, r["inicio"]?.toString(), r["fin"]?.toString()) }
                }
            }
            days.forEach { d -> if (d.switch.isChecked && countRangeRows(d.container) == 0) addRangeRow(d.container) }
        }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}