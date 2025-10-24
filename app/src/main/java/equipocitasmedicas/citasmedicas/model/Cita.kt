package equipocitasmedicas.citasmedicas.model

import com.google.firebase.Timestamp

data class Cita(
    val pacienteId: String = "",
    val pacienteNombre: String = "",
    val medicoId: String = "",
    val medicoNombre: String = "",
    val medicoEspecialidad: String = "",
    val fechaHora: Timestamp? = null,
    val estado: String = "pendiente",
    val notas: String = ""
)