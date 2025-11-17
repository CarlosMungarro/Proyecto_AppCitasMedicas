package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Cita

class PacienteHomeFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: CitaAdapter
    private val db = FirebaseFirestore.getInstance()
    private var pacienteUid: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_paciente_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val btnAgregar = view.findViewById<FloatingActionButton>(R.id.btnAgregarCita)
        rv = view.findViewById(R.id.rvCitas)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para ver tus citas", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return
        }
        pacienteUid = user.uid

        adapter = CitaAdapter(mutableListOf()) { cita ->
            val intent = Intent(requireContext(), DetallePacienteActivity::class.java)
            intent.putExtra("CITA_ID", cita.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        btnAgregar.setOnClickListener {
            startActivity(Intent(requireContext(), AgendarCitaPacienteActivity::class.java))
        }

        cargarCitas()
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) cargarCitas()
    }

    private fun cargarCitas() {
        db.collection("citas")
            .whereEqualTo("pacienteId", pacienteUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val citas = snapshot.documents.mapNotNull { doc ->
                    val cita = doc.toObject(Cita::class.java)
                    cita?.id = doc.id
                    cita
                }
                adapter.updateCitas(citas)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar citas: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}