package equipocitasmedicas.citasmedicas.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import equipocitasmedicas.citasmedicas.R
import java.text.SimpleDateFormat
import java.util.*

class MisCitasMedicoActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: CitasMedicoAdapter
    private lateinit var tvFechaActual: TextView
    private lateinit var btnDia: Button
    private lateinit var btnSemana: Button
    private var medicoId: Long = -1
    private var filtroDia: Boolean = true

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas_medico)

        val sharedPref = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        medicoId = sharedPref.getLong("LOGGED_USER_ID", -1)

        if (medicoId == -1L) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Datos de prueba
        CitaStore.agregarDatosPrueba(medicoId)

        initViews()
        setupRecyclerView()
        setupButtons()
        setupBottomNavigation()
        setupFAB()
        actualizarFecha()
        cargarCitas()
    }

    private fun initViews() {
        rvCitas = findViewById(R.id.rvCitasMedico)
        tvFechaActual = findViewById(R.id.tvFechaActual)
        btnDia = findViewById(R.id.btnDia)
        btnSemana = findViewById(R.id.btnSemana)
    }

    private fun setupRecyclerView() {
        adapter = CitasMedicoAdapter(emptyList()) { cita ->
            // ✅ Pasamos todos los datos al detalle
            val intent = Intent(this, DetalleMedicoActivity::class.java).apply {
                putExtra("nombrePaciente", cita.nombrePaciente)
                putExtra("fechaHora", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(cita.fechaHora))
                putExtra("motivo", cita.motivo)
                putExtra("genero", cita.genero)
                putExtra("telefono", cita.telefono)
                putExtra("edad", cita.fechaNacimiento) // o calcula edad si prefieres
            }
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
            cargarCitas()
        }

        btnSemana.setOnClickListener {
            filtroDia = false
            actualizarBotones()
            cargarCitas()
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

    private fun cargarCitas() {
        val calendario = Calendar.getInstance()
        val citas = if (filtroDia) {
            CitaStore.obtenerCitasMedicoDia(medicoId, calendario.time)
        } else {
            CitaStore.obtenerCitasMedicoSemana(medicoId, calendario.time)
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
        cargarCitas()
    }
}
