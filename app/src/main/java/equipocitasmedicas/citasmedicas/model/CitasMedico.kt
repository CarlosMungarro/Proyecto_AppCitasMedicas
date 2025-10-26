package equipocitasmedicas.citasmedicas.model

import java.security.Timestamp

data class CitasMedico(
    val pacienteId: String = "",
    val pacienteNombre: String = "",
    val medicoId: String = "",
    val medicoNombre: String = "",
    val medicoEspecialidad: String = "",
    val fechaHora: Timestamp? = null,
    val estado: String = "pendiente",
    val notas: String = ""
)