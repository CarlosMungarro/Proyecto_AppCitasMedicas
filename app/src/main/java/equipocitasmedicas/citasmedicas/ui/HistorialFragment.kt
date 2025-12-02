package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Cita
import java.util.*

class HistorialFragment : Fragment() {

    private lateinit var rv: RecyclerView
    private lateinit var adapter: CitaAdapter
    private val db = FirebaseFirestore.getInstance()
    private var pacienteUid: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_historial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        rv = view.findViewById(R.id.rvCitas)
        rv.layoutManager = LinearLayoutManager(requireContext())

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Debes iniciar sesión para ver tu historial", Toast.LENGTH_SHORT).show()
            return
        }
        pacienteUid = user.uid

        adapter = CitaAdapter(mutableListOf()) { cita ->
            val intent = Intent(requireContext(), DetallePacienteActivity::class.java)
            intent.putExtra("CITA_ID", cita.id)
            startActivity(intent)
        }
        rv.adapter = adapter

        cargarHistorial()
    }

    override fun onResume() {
        super.onResume()
        if (isAdded) cargarHistorial()
    }

    private fun cargarHistorial() {
        val ahora = Calendar.getInstance()
        val timestampAhora = com.google.firebase.Timestamp(ahora.time)

        // Obtener todas las citas del paciente y filtrar en memoria para evitar problemas de índice
        db.collection("citas")
            .whereEqualTo("pacienteId", pacienteUid)
            .get()
            .addOnSuccessListener { snapshot ->
                val citas = snapshot.documents.mapNotNull { doc ->
                    val cita = doc.toObject(Cita::class.java)
                    cita?.id = doc.id
                    cita
                }
                // Filtrar solo las citas pasadas (fechaHora < ahora)
                val citasPasadas = citas.filter { cita ->
                    cita.fechaHora?.let { it.toDate().before(ahora.time) } ?: false
                }
                // Ordenar por fecha descendente (más recientes primero)
                val citasOrdenadas = citasPasadas.sortedByDescending { it.fechaHora?.toDate()?.time ?: 0L }
                adapter.updateCitas(citasOrdenadas)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error al cargar historial: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

