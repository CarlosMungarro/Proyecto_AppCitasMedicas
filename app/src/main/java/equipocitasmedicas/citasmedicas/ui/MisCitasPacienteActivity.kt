package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitaItem

class MisCitasPacienteActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: CitaAdapter
    private var pacienteUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas_paciente)

        val btnAgregar = findViewById<FloatingActionButton>(R.id.btnAgregarCita)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        pacienteUid = getSharedPreferences("app_session", MODE_PRIVATE)
            .getString("LOGGED_USER_ID", null) ?: ""

        if (pacienteUid.isBlank()) {
            Toast.makeText(this, "Sesión no encontrada. Inicia sesión.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        rvCitas = findViewById(R.id.rvCitas)
        rvCitas.layoutManager = LinearLayoutManager(this)

        // Si tu adapter usa CitaItem, mapear desde Cita a CitaItem:
        val lista = CitaStore.obtenerCitasPaciente(pacienteUid).map { cita ->
            CitaItem(
                id = cita.id,
                nombrePaciente = cita.nombrePaciente,
                nombreMedico = cita.nombreMedico,
                motivo = cita.motivo,
                fechaHora = cita.fechaHora,
                fechaNacimiento = cita.fechaNacimiento,
                telefono = cita.telefono,
                genero = cita.genero,
                estado = cita.estado
            )
        }.toMutableList()

        adapter = CitaAdapter(lista) { citaSeleccionada ->
            startActivity(
                Intent(this, DetallePacienteActivity::class.java).putExtra("CITA_ID", citaSeleccionada.id)
            )
        }
        rvCitas.adapter = adapter

        btnAgregar.setOnClickListener {
            startActivity(Intent(this, AgendarCitaPacienteActivity::class.java))
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_reloj -> true
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
        val nuevas = CitaStore.obtenerCitasPaciente(pacienteUid).map { cita ->
            CitaItem(
                id = cita.id,
                nombrePaciente = cita.nombrePaciente,
                nombreMedico = cita.nombreMedico,
                motivo = cita.motivo,
                fechaHora = cita.fechaHora,
                fechaNacimiento = cita.fechaNacimiento,
                telefono = cita.telefono,
                genero = cita.genero,
                estado = cita.estado
            )
        }
        adapter.updateCitas(nuevas)
    }
}