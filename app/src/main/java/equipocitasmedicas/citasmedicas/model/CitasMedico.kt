package equipocitasmedicas.citasmedicas.model

import com.google.firebase.Timestamp


data class CitasMedico(
    val id: String = "", // 🔹 ID del documento
    val estado: String = "pendiente",
    val fechaHora: com.google.firebase.Timestamp? = null,
    val medicoEspecialidad: String = "",
    val medicoId: String = "",
    val medicoNombre: String = "",
    val notas: String = "",
    val pacienteId: String = "",
    val pacienteNombre: String = ""
)


