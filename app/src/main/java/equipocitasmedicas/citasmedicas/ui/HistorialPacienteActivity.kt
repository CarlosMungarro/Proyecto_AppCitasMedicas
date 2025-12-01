package equipocitasmedicas.citasmedicas.ui
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Cita

class HistorialPacienteActivity : AppCompatActivity() {

    private lateinit var rvCitasHistorial: RecyclerView
    private lateinit var adapter: CitaAdapter
    private val db = FirebaseFirestore.getInstance()
    private var pacienteUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_historial_paciente)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationHistorial)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        pacienteUid = user.uid

        rvCitasHistorial = findViewById(R.id.rvCitasHistorial)
        rvCitasHistorial.layoutManager = LinearLayoutManager(this)

        adapter = CitaAdapter(mutableListOf()) { citaSeleccionada ->
            val intent = Intent(this, DetallePacienteActivity::class.java)
            intent.putExtra("CITA_ID", citaSeleccionada.id)
            startActivity(intent)
        }
        rvCitasHistorial.adapter = adapter

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_agendas -> {
                    startActivity(Intent(this, MisCitasPacienteActivity::class.java))
                    finish()
                    true
                }
                R.id.nav_historial -> true
                R.id.nav_perfil -> {
                    startActivity(Intent(this, ConfigurarPerfilActivity::class.java))
                    true
                }
                else -> false
            }
        }

        bottomNav.selectedItemId = R.id.nav_historial
        cargarHistorialCitas()
    }

    private fun cargarHistorialCitas() {
        db.collection("citas")
            .whereEqualTo("pacienteId", pacienteUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val citasHistorial = snapshot.documents.mapNotNull { doc ->
                    val cita = doc.toObject(Cita::class.java)
                    cita?.id = doc.id

                    if (cita?.estado?.lowercase() == "completada" ||
                        cita?.estado?.lowercase() == "cancelada") {
                        cita
                    } else null
                }.sortedByDescending { it.fechaHora }

                adapter.updateCitas(citasHistorial)

                if (citasHistorial.isEmpty()) {
                    Toast.makeText(this, "No tienes citas en el historial", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar historial: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        cargarHistorialCitas()
    }
}