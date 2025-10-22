package equipocitasmedicas.citasmedicas.model

import java.util.Date

data class CitaItem(
    val id: Long,
    val nombrePaciente: String,
    val nombreMedico: String,
    val motivo: String,
    val fechaHora: Date,
    val fechaNacimiento: String,
    val telefono: String,
    val genero: String,
    val estado: String
)