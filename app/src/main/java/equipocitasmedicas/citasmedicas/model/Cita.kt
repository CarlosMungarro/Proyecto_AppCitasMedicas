package equipocitasmedicas.citasmedicas.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class Cita(
    val pacienteId: String = "",
    val pacienteNombre: String = "",
    val medicoId: String = "",
    val medicoNombre: String = "",
    val medicoEspecialidad: String = "",
    val fechaHora: Timestamp? = null,
    val estado: String = "pendiente",
    val notas: String = "",
    @get:Exclude
    var id: String = "" // Solo para uso local
)

