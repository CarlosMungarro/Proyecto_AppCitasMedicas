package equipocitasmedicas.citasmedicas.model

data class MedicoPaciente(
    val id: String = "",
    val nombreCompleto: String = "",
    val especialidad: String = "",
    val direccionConsultorio: String = "",
    val fotoUrl: String? = null
)