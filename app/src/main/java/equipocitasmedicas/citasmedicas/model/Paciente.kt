package equipocitasmedicas.citasmedicas.model

data class Paciente (
    val id: Long,
    val nombreCompleto: String,
    val correo: String,
    val fechaNacimiento: String,
    val telefono: String,
    val password: String,
    val rol: String,
    val genero: String)
{
}