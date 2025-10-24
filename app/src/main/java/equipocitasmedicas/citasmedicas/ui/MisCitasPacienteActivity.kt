package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Cita

class MisCitasPacienteActivity : AppCompatActivity() {

    private lateinit var rvCitas: RecyclerView
    private lateinit var adapter: CitaAdapter
    private val db = FirebaseFirestore.getInstance()
    private var pacienteUid: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mis_citas_paciente)

        val btnAgregar = findViewById<FloatingActionButton>(R.id.btnAgregarCita)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver tus citas", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        pacienteUid = user.uid

        rvCitas = findViewById(R.id.rvCitas)
        rvCitas.layoutManager = LinearLayoutManager(this)

        adapter = CitaAdapter(mutableListOf()) { citaSeleccionada ->
            startActivity(
                Intent(this, DetallePacienteActivity::class.java)
                    .putExtra("CITA_ID", citaSeleccionada.pacienteId)
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

        cargarCitas()
    }

    private fun cargarCitas() {
        db.collection("citas")
            .whereEqualTo("pacienteId", pacienteUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val citas = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Cita::class.java)
                }
                adapter.updateCitas(citas)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        cargarCitas()
    }
}