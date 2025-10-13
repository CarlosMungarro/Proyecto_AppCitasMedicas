package equipocitasmedicas.citasmedicas.model

data class CitaItem(
    val id: Long,
    val pacienteId: Long,
    val doctor: DoctorItem,
    val fecha: String,
    val hora: String,
    val motivo: String
)