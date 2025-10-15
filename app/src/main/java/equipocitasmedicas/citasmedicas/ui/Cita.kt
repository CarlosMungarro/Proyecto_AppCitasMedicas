package equipocitasmedicas.citasmedicas.ui
import java.util.Date

data class Cita(
    val id: Long,
    val medicoId: Long,
    val pacienteId: Long,
    val nombrePaciente: String,
    val nombreMedico: String,
    val motivo: String,
    val fechaNacimiento: String,
    val telefono: String,
    val genero: String,
    val fechaHora: Date,
    val estado: String,
    val notas: String = ""
)