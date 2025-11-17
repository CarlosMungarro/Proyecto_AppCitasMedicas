package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitasMedico
import equipocitasmedicas.citasmedicas.model.ItemCitasMedico
import java.util.*

class MedicoHomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: CitasMedicoAdapter
    private lateinit var tvFechaActual: TextView
    private lateinit var btnDia: Button
    private lateinit var btnSemana: Button
    private lateinit var progress: ProgressBar

    private var todasLasCitas: List<CitasMedico> = emptyList()
    private var listenerCitas: ListenerRegistration? = null
    private var filtroActual = "hoy"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_medico_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvCitasMedico)
        tvFechaActual = view.findViewById(R.id.tvFechaActual)
        btnDia = view.findViewById(R.id.btnDia)
        btnSemana = view.findViewById(R.id.btnSemana)
        progress = view.findViewById(R.id.progressBar)

        adapter = CitasMedicoAdapter(mutableListOf()) { cita ->
            val db = FirebaseFirestore.getInstance()
            db.collection("pacientes").document(cita.pacienteId).get()
                .addOnSuccessListener { pacienteDoc ->
                    if (pacienteDoc != null && pacienteDoc.exists()) {
                        val fechaNacimiento = pacienteDoc.getString("fechaNacimiento") ?: "--"
                        val genero = pacienteDoc.getString("genero") ?: "--"
                        val telefono = pacienteDoc.getString("telefono") ?: "--"

                        val intent = Intent(requireContext(), DetalleMedicoActivity::class.java).apply {
                            putExtra("citaId", cita.id)
                            putExtra("nombrePaciente", cita.pacienteNombre)
                            putExtra("motivo", cita.notas)
                            putExtra("estado", cita.estado)
                            putExtra("fechaHora", cita.fechaHora?.toDate()?.time ?: 0L)
                            putExtra("fechaNacimiento", fechaNacimiento)
                            putExtra("genero", genero)
                            putExtra("telefono", telefono)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(requireContext(), "No se encontró información del paciente", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error al obtener datos del paciente", Toast.LENGTH_SHORT).show()
                }
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        actualizarFecha()

        btnDia.setOnClickListener {
            filtroActual = "hoy"
            mostrarCitasHoy()
            btnDia.setBackgroundColor(resources.getColor(R.color.azulLetrasHeader))
            btnSemana.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }
        btnSemana.setOnClickListener {
            filtroActual = "semana"
            mostrarCitasEstaSemana()
            btnSemana.setBackgroundColor(resources.getColor(R.color.azulLetrasHeader))
            btnDia.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }

        obtenerCitasEnTiempoReal()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerCitas?.remove()
    }

    private fun obtenerCitasEnTiempoReal() {
        progress.visibility = View.VISIBLE
        val usuarioActual = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        val medicoIdActual = usuarioActual?.uid ?: ""

        if (medicoIdActual.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontró el usuario médico actual", Toast.LENGTH_SHORT).show()
            progress.visibility = View.GONE
            return
        }

        val db = FirebaseFirestore.getInstance()
        listenerCitas = db.collection("citas")
            .whereEqualTo("medicoId", medicoIdActual)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    progress.visibility = View.GONE
                    Toast.makeText(requireContext(), "Error al cargar las citas: ${error.message}", Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }

                val lista = mutableListOf<CitasMedico>()
                snapshot?.forEach { documento ->
                    val cita = CitasMedico(
                        id = documento.id,
                        estado = documento.getString("estado") ?: "pendiente",
                        fechaHora = documento.getTimestamp("fechaHora"),
                        medicoEspecialidad = documento.getString("medicoEspecialidad") ?: "",
                        medicoId = documento.getString("medicoId") ?: "",
                        medicoNombre = documento.getString("medicoNombre") ?: "",
                        notas = documento.getString("notas") ?: "",
                        pacienteId = documento.getString("pacienteId") ?: "",
                        pacienteNombre = documento.getString("pacienteNombre") ?: ""
                    )
                    lista.add(cita)
                }
                todasLasCitas = lista

                when (filtroActual) {
                    "hoy" -> mostrarCitasHoy()
                    "semana" -> mostrarCitasEstaSemana()
                }
                progress.visibility = View.GONE
            }
    }

    private fun mostrarCitasHoy() {
        val zona = TimeZone.getTimeZone("America/Hermosillo")
        val hoy = Calendar.getInstance(zona)
        val diaHoy = hoy.get(Calendar.DAY_OF_YEAR)
        val anioHoy = hoy.get(Calendar.YEAR)

        val citasHoy = todasLasCitas.filter { cita ->
            cita.fechaHora?.toDate()?.let { fecha ->
                val c = Calendar.getInstance(zona).apply { time = fecha }
                c.get(Calendar.DAY_OF_YEAR) == diaHoy && c.get(Calendar.YEAR) == anioHoy
            } ?: false
        }

        val items = mutableListOf<ItemCitasMedico>()
        if (citasHoy.isNotEmpty()) {
            val fmt = java.text.SimpleDateFormat("EEEE", Locale("es", "MX")).apply { timeZone = zona }
            val nombreDia = fmt.format(citasHoy[0].fechaHora!!.toDate()).replaceFirstChar { it.uppercaseChar() }
            items.add(ItemCitasMedico.Dia(nombreDia))
            citasHoy.forEach { items.add(ItemCitasMedico.Cita(it)) }
        }
        adapter.actualizarItems(items)
    }

    private fun mostrarCitasEstaSemana() {
        val zona = TimeZone.getTimeZone("America/Hermosillo")
        val cal = Calendar.getInstance(zona).apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val inicio = cal.clone() as Calendar
        inicio.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val fin = cal.clone() as Calendar
        fin.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        fin.set(Calendar.HOUR_OF_DAY, 23); fin.set(Calendar.MINUTE, 59); fin.set(Calendar.SECOND, 59)

        val citasSemana = todasLasCitas.filter { c ->
            c.fechaHora?.toDate()?.let { f -> !f.before(inicio.time) && !f.after(fin.time) } ?: false
        }.sortedBy { it.fechaHora }

        val items = mutableListOf<ItemCitasMedico>()
        val fmt = java.text.SimpleDateFormat("EEEE", Locale("es", "MX")).apply { timeZone = zona }
        val porDia = citasSemana.groupBy { fmt.format(it.fechaHora!!.toDate()).replaceFirstChar { ch -> ch.uppercaseChar() } }
        porDia.forEach { (nombreDia, cs) ->
            items.add(ItemCitasMedico.Dia(nombreDia))
            cs.forEach { items.add(ItemCitasMedico.Cita(it)) }
        }
        adapter.actualizarItems(items)
    }

    private fun actualizarFecha() {
        val zona = TimeZone.getTimeZone("America/Hermosillo")
        val cal = Calendar.getInstance(zona)
        val formato = java.text.SimpleDateFormat("EEEE dd 'de' MMMM", Locale("es", "MX")).apply { timeZone = zona }
        val fecha = formato.format(cal.time).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        tvFechaActual.text = "Hoy, $fecha"
    }
}