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
import equipocitasmedicas.citasmedicas.data.CitasStore
import equipocitasmedicas.citasmedicas.model.CitaItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MisCitasMedicoActivity : AppCompatActivity() {
    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: CitasMedicoAdapter
    private lateinit var tvFechaActual: TextView
    private lateinit var btnDia: Button
    private lateinit var btnSemana: Button
    private var medicoId: Long = -1
    private var filtroDia: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas_medico)

        // Obtener ID del médico desde SharedPreferences
        val sharedPref = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        medicoId = sharedPref.getLong("LOGGED_USER_ID", -1)

        if (medicoId == -1L) {
            Toast.makeText(this, "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Agregar datos de prueba
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
            Toast.makeText(this, "Cita con ${cita.nombrePaciente}", Toast.LENGTH_SHORT).show()
        }

        rvCitas.layoutManager = LinearLayoutManager(this)
        rvCitas.adapter = adapter
    }

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

        Log.d("BottomNav", "=== INICIO DEBUG ===")
        Log.d("BottomNav", "Total items: ${bottomNav.menu.size()}")

        // Mostrar info de cada item
        for (i in 0 until bottomNav.menu.size()) {
            val item = bottomNav.menu.getItem(i)
            Log.d("BottomNav", "Item $i: Título='${item.title}', ID=${item.itemId}")
        }

        bottomNav.setOnItemSelectedListener { item ->
            Log.d("BottomNav", "Click en item con título: '${item.title}'")

            var indice = -1
            for (i in 0 until bottomNav.menu.size()) {
                if (bottomNav.menu.getItem(i).itemId == item.itemId) {
                    indice = i
                    break
                }
            }

            Log.d("BottomNav", "Índice del item: $indice")
            Toast.makeText(this, "Presionaste item #$indice: ${item.title}", Toast.LENGTH_LONG).show()

            when (indice) {
                0 -> {
                    Log.d("BottomNav", "→ Primer item (Citas)")
                    Toast.makeText(this, "Ya estás en Citas", Toast.LENGTH_SHORT).show()
                    true
                }
                1 -> {
                    Log.d("BottomNav", "→ Segundo item (Disponibilidad)")
                    Toast.makeText(this, "Abriendo Disponibilidad...", Toast.LENGTH_LONG).show()
                    try {
                        val intent = Intent(this, DisponibilidadActivity::class.java)
                        startActivity(intent)
                        Log.d("BottomNav", "✓ Activity iniciada")
                        true
                    } catch (e: Exception) {
                        Log.e("BottomNav", "✗ ERROR", e)
                        Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        false
                    }
                }
                else -> {
                    Log.d("BottomNav", "→ Índice desconocido")
                    false
                }
            }
        }

        if (bottomNav.menu.size() > 0) {
            bottomNav.selectedItemId = bottomNav.menu.getItem(0).itemId
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