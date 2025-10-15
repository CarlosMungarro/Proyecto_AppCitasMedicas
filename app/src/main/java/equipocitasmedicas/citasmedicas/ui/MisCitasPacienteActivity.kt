    package equipocitasmedicas.citasmedicas.ui

    import android.content.Intent
    import android.os.Bundle
    import androidx.appcompat.app.AppCompatActivity
    import com.google.android.material.bottomnavigation.BottomNavigationView
    import com.google.android.material.floatingactionbutton.FloatingActionButton

    import equipocitasmedicas.citasmedicas.data.CitasStore
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import equipocitasmedicas.citasmedicas.R
    import equipocitasmedicas.citasmedicas.model.CitaItem


    class MisCitasPacienteActivity : AppCompatActivity() {

        private lateinit var rvCitas: RecyclerView
        private lateinit var adapter: CitaAdapter
        private var pacienteId: Long = -1L

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_mis_citas_paciente)

            val btnAgregar = findViewById<FloatingActionButton>(R.id.btnAgregarCita)
            val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

            // Obtener paciente logueado
            pacienteId = getSharedPreferences("app_session", MODE_PRIVATE)
                .getLong("LOGGED_USER_ID", -1L)

            // RecyclerView
            rvCitas = findViewById(R.id.rvCitas)
            rvCitas.layoutManager = LinearLayoutManager(this)

            // Adapter con lista mutable
            adapter = CitaAdapter(CitasStore.getCitasByPaciente(pacienteId).toMutableList()) { citaSeleccionada ->
                val intent = Intent(this, DetallePacienteActivity::class.java)
                intent.putExtra("CITA_ID", citaSeleccionada.id)
                startActivity(intent)
            }
            rvCitas.adapter = adapter


            // Botón flotante: agregar cita
            btnAgregar.setOnClickListener {
                startActivity(Intent(this, AgendarCitaPacienteActivity::class.java))
            }

            // Navegación inferior
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_citas -> true
                    R.id.nav_perfil -> {
                        startActivity(Intent(this, ConfigurarPerfilActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }

        override fun onResume() {
            super.onResume()
            // Refrescar la lista de citas al volver a la actividad
            val nuevasCitaItems: List<CitaItem> = CitasStore.getCitasByPaciente(pacienteId)
            adapter.updateCitas(nuevasCitaItems)
        }
    }