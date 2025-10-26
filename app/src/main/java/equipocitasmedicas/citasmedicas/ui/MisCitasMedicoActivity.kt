package equipocitasmedicas.citasmedicas.ui


import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitasMedico
import java.text.SimpleDateFormat
import java.util.*

class MisCitasMedicoActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: CitasMedicoAdapter
    private lateinit var tvFechaActual: TextView
    private lateinit var btnDia: Button
    private lateinit var btnSemana: Button
    private lateinit var progressBar: ProgressBar
    private var medicoUid: String = ""
    private var filtroDia: Boolean = true

    private val db = FirebaseFirestore.getInstance()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas_medico)

        val uid = getSharedPreferences("app_session", Context.MODE_PRIVATE)
            .getString("LOGGED_USER_ID", null)

        if (uid.isNullOrBlank()) {
            Toast.makeText(this, "Sesión no encontrada. Inicia sesión.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        medicoUid = uid

        initViews()
        setupRecyclerView()
        setupButtons()
        setupBottomNavigation()
        setupFAB()
        actualizarFecha()
        cargarCitasDesdeFirebase()
    }

    private fun initViews() {
        rvCitas = findViewById(R.id.rvCitasMedico)
        tvFechaActual = findViewById(R.id.tvFechaActual)
        btnDia = findViewById(R.id.btnDia)
        btnSemana = findViewById(R.id.btnSemana)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupRecyclerView() {
        adapter = CitasMedicoAdapter(emptyList()) { cita ->
            val intent = Intent(this, DetalleMedicoActivity::class.java)

            // Agregar extras uno por uno
            intent.putExtra("nombrePaciente", cita.pacienteNombre)
            intent.putExtra("motivo", cita.notas)
            intent.putExtra("estado", cita.estado)
            intent.putExtra("pacienteId", cita.pacienteId)
            intent.putExtra("fechaHora", cita.fechaHora?.toString() ?: "Sin fecha")

            startActivity(intent)
        }
        rvCitas.layoutManager = LinearLayoutManager(this)
        rvCitas.adapter = adapter
    }
    @RequiresApi(Build.VERSION_CODES.M)
    private fun actualizarBotones() {
        if (filtroDia) {
            btnDia.backgroundTintList = getColorStateList(R.color.azul_boton_plus)
            btnSemana.backgroundTintList = getColorStateList(android.R.color.darker_gray)
        } else {
            btnDia.backgroundTintList = getColorStateList(android.R.color.darker_gray)
            btnSemana.backgroundTintList = getColorStateList(R.color.azul_boton_plus)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun setupButtons() {
        btnDia.setOnClickListener {
            filtroDia = true
            actualizarBotones()
            cargarCitasDesdeFirebase()
        }
        btnSemana.setOnClickListener {
            filtroDia = false
            actualizarBotones()
            cargarCitasDesdeFirebase()
        }
    }

    private fun actualizarFecha() {
        val calendario = Calendar.getInstance()
        val formato = SimpleDateFormat("EEEE dd 'de' MMMM", Locale("es", "ES"))
        val fechaTexto = formato.format(calendario.time)
        val fechaCapitalizada = fechaTexto.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        tvFechaActual.text = "Hoy, $fechaCapitalizada"
    }

    private fun cargarCitasDesdeFirebase() {
        progressBar.visibility = View.VISIBLE

        val calendario = Calendar.getInstance()
        val inicioRango: Date
        val finRango: Date

        if (filtroDia) {
            // Rango del día actual (00:00 a 23:59)
            calendario.set(Calendar.HOUR_OF_DAY, 0)
            calendario.set(Calendar.MINUTE, 0)
            calendario.set(Calendar.SECOND, 0)
            inicioRango = calendario.time

            calendario.set(Calendar.HOUR_OF_DAY, 23)
            calendario.set(Calendar.MINUTE, 59)
            calendario.set(Calendar.SECOND, 59)
            finRango = calendario.time
        } else {
            // Rango de la semana actual (lunes a domingo)
            calendario.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            calendario.set(Calendar.HOUR_OF_DAY, 0)
            calendario.set(Calendar.MINUTE, 0)
            calendario.set(Calendar.SECOND, 0)
            inicioRango = calendario.time

            calendario.add(Calendar.DAY_OF_YEAR, 6)
            calendario.set(Calendar.HOUR_OF_DAY, 23)
            calendario.set(Calendar.MINUTE, 59)
            calendario.set(Calendar.SECOND, 59)
            finRango = calendario.time
        }

        // Convertir Date a Timestamp para Firestore
        val inicioTimestamp = Timestamp(inicioRango)
        val finTimestamp = Timestamp(finRango)

        // Consulta a Firebase Firestore
        db.collection("citas")
            .whereEqualTo("medicoId", medicoUid)
            .whereGreaterThanOrEqualTo("fechaHora", inicioTimestamp)
            .whereLessThanOrEqualTo("fechaHora", finTimestamp)
            .orderBy("fechaHora", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                progressBar.visibility = View.GONE
                val citas = mutableListOf<CitasMedico>()

                for (document in documents) {
                    try {
                        val cita = document.toObject(CitasMedico::class.java)
                        citas.add(cita)
                    } catch (e: Exception) {
                        Log.e("Firebase", "Error al parsear cita: ${e.message}")
                    }
                }

                adapter.actualizarCitas(citas)

                if (citas.isEmpty()) {
                    Toast.makeText(
                        this,
                        if (filtroDia) "No hay citas para hoy" else "No hay citas esta semana",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { exception ->
                progressBar.visibility = View.GONE
                Log.e("Firebase", "Error al cargar citas: ${exception.message}")
                Toast.makeText(
                    this,
                    "Error al cargar las citas: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationMedico)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
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

    private fun setupFAB() {
        val fab = findViewById<FloatingActionButton>(R.id.btnAgregarCitaMedico)
        fab.setOnClickListener {
            Toast.makeText(this, "Agregar nueva cita", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        cargarCitasDesdeFirebase()
    }
}

