package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.databinding.ActivityMisCitasMedicoBinding
import equipocitasmedicas.citasmedicas.model.CitasMedico
import equipocitasmedicas.citasmedicas.model.ItemCitasMedico
import java.util.*

class MisCitasMedicoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMisCitasMedicoBinding
    private lateinit var adapter: CitasMedicoAdapter
    private var todasLasCitas: List<CitasMedico> = listOf()
    private var listenerCitas: ListenerRegistration? = null
    private var filtroActual = "hoy"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMisCitasMedicoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        obtenerCitasEnTiempoReal()
        configurarBottomNavigation()
        actualizarFecha()

        // 🔹 Mostrar por defecto las citas de hoy
        filtroActual = "hoy"
        binding.btnDia.setBackgroundColor(resources.getColor(R.color.azulLetrasHeader))
        binding.btnSemana.setBackgroundColor(resources.getColor(android.R.color.darker_gray))

        binding.btnDia.setOnClickListener {
            filtroActual = "hoy"
            mostrarCitasHoy()
            binding.btnDia.setBackgroundColor(resources.getColor(R.color.azulLetrasHeader))
            binding.btnSemana.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }

        binding.btnSemana.setOnClickListener {
            filtroActual = "semana"
            mostrarCitasEstaSemana()
            binding.btnSemana.setBackgroundColor(resources.getColor(R.color.azulLetrasHeader))
            binding.btnDia.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
        }
    }

    private fun setupRecyclerView() {
        adapter = CitasMedicoAdapter(mutableListOf()) { cita ->
            val db = FirebaseFirestore.getInstance()
            db.collection("pacientes").document(cita.pacienteId).get()
                .addOnSuccessListener { pacienteDoc ->
                    if (pacienteDoc != null && pacienteDoc.exists()) {
                        val fechaNacimiento = pacienteDoc.getString("fechaNacimiento") ?: "--"
                        val genero = pacienteDoc.getString("genero") ?: "--"
                        val telefono = pacienteDoc.getString("telefono") ?: "--"

                        val intent = Intent(this, DetalleMedicoActivity::class.java).apply {
                            putExtra("citaId", cita.id) // 🔹 Agregar ID del documento
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
                        Toast.makeText(this, "No se encontró información del paciente", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al obtener datos del paciente", Toast.LENGTH_SHORT).show()
                }
        }


        binding.rvCitasMedico.layoutManager = LinearLayoutManager(this)
        binding.rvCitasMedico.adapter = adapter
    }

    private fun obtenerCitasEnTiempoReal() {
        binding.progressBar.visibility = View.VISIBLE
        val db = FirebaseFirestore.getInstance()
        listenerCitas = db.collection("citas")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "Error al cargar las citas: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return@addSnapshotListener
                }

                val listaCitas = mutableListOf<CitasMedico>()
                snapshot?.forEach { documento ->
                    val cita = CitasMedico(
                        id = documento.id, // 🔹 Guardar ID del documento
                        estado = documento.getString("estado") ?: "pendiente",
                        fechaHora = documento.getTimestamp("fechaHora"),
                        medicoEspecialidad = documento.getString("medicoEspecialidad") ?: "",
                        medicoId = documento.getString("medicoId") ?: "",
                        medicoNombre = documento.getString("medicoNombre") ?: "",
                        notas = documento.getString("notas") ?: "",
                        pacienteId = documento.getString("pacienteId") ?: "",
                        pacienteNombre = documento.getString("pacienteNombre") ?: ""
                    )

                    listaCitas.add(cita)
                }

                todasLasCitas = listaCitas

                // 🔹 Mostrar citas según el filtro activo
                when (filtroActual) {
                    "hoy" -> mostrarCitasHoy()
                    "semana" -> mostrarCitasEstaSemana()
                }

                binding.progressBar.visibility = View.GONE
            }
    }

    private fun mostrarCitasHoy() {
        val zonaHoraria = TimeZone.getTimeZone("America/Hermosillo")
        val hoy = Calendar.getInstance(zonaHoraria)
        val diaHoy = hoy.get(Calendar.DAY_OF_YEAR)
        val anioHoy = hoy.get(Calendar.YEAR)

        val citasHoy = todasLasCitas.filter { cita ->
            cita.fechaHora?.toDate()?.let { fecha ->
                val calCita = Calendar.getInstance(zonaHoraria)
                calCita.time = fecha
                calCita.get(Calendar.DAY_OF_YEAR) == diaHoy &&
                        calCita.get(Calendar.YEAR) == anioHoy
            } ?: false
        }

        val items = mutableListOf<ItemCitasMedico>()
        if (citasHoy.isNotEmpty()) {
            val formatoDia = java.text.SimpleDateFormat("EEEE", Locale("es", "MX"))
            formatoDia.timeZone = zonaHoraria
            val nombreDia = formatoDia.format(citasHoy[0].fechaHora!!.toDate())
                .replaceFirstChar { it.uppercaseChar() }

            items.add(ItemCitasMedico.Dia(nombreDia))
            citasHoy.forEach { cita ->
                items.add(ItemCitasMedico.Cita(cita))
            }
        }

        adapter.actualizarItems(items)
    }

    private fun mostrarCitasEstaSemana() {
        val zonaHoraria = TimeZone.getTimeZone("America/Hermosillo")
        val calendario = Calendar.getInstance(zonaHoraria)
        calendario.firstDayOfWeek = Calendar.MONDAY
        calendario.set(Calendar.HOUR_OF_DAY, 0)
        calendario.set(Calendar.MINUTE, 0)
        calendario.set(Calendar.SECOND, 0)
        calendario.set(Calendar.MILLISECOND, 0)

        val inicioSemana = calendario.clone() as Calendar
        inicioSemana.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val finSemana = calendario.clone() as Calendar
        finSemana.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        finSemana.set(Calendar.HOUR_OF_DAY, 23)
        finSemana.set(Calendar.MINUTE, 59)
        finSemana.set(Calendar.SECOND, 59)

        val citasSemana = todasLasCitas.filter { cita ->
            cita.fechaHora?.toDate()?.let { fecha ->
                !fecha.before(inicioSemana.time) && !fecha.after(finSemana.time)
            } ?: false
        }.sortedBy { it.fechaHora }

        val items = mutableListOf<ItemCitasMedico>()
        val formatoDia = java.text.SimpleDateFormat("EEEE", Locale("es", "MX"))
        formatoDia.timeZone = zonaHoraria

        val citasPorDia = citasSemana.groupBy { cita ->
            formatoDia.format(cita.fechaHora!!.toDate()).replaceFirstChar { it.uppercaseChar() }
        }

        citasPorDia.forEach { (nombreDia, citasDelDia) ->
            items.add(ItemCitasMedico.Dia(nombreDia))
            citasDelDia.forEach { cita ->
                items.add(ItemCitasMedico.Cita(cita))
            }
        }

        adapter.actualizarItems(items)
    }

    private fun actualizarFecha() {
        val zonaHoraria = TimeZone.getTimeZone("America/Hermosillo")
        val calendario = Calendar.getInstance(zonaHoraria)
        val formato = java.text.SimpleDateFormat("EEEE dd 'de' MMMM", Locale("es", "MX"))
        formato.timeZone = zonaHoraria
        val fechaTexto = formato.format(calendario.time)
        val fechaCapitalizada = fechaTexto.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        binding.tvFechaActual.text = "Hoy, $fechaCapitalizada"
    }

    private fun configurarBottomNavigation() {
        binding.bottomNavigationMedico.selectedItemId = R.id.nav_citas
        binding.bottomNavigationMedico.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_citas -> true
                R.id.nav_perfil -> {
                    startActivity(Intent(this, ConfigurarPerfilActivity::class.java))
                    true
                }
                R.id.nav_reloj -> {
                    startActivity(Intent(this, DisponibilidadActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listenerCitas?.remove()
    }
}





