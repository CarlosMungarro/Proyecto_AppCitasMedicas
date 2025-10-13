package equipocitasmedicas.citasmedicas.model


data class DoctorItem(
    val nombreCompleto: String,
    val especialidad: String,
    val consultorio: String,
    val fotoUrl: String? = null // foto por defecto es null
)