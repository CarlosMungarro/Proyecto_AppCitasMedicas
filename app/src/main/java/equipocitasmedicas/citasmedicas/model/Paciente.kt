package equipocitasmedicas.citasmedicas.model

// en model/Paciente.kt
data class Paciente(
    val id: Long,
    var nombreCompleto: String,
    var correo: String,
    var fechaNacimiento: String,
    var telefono: String,
    val password: String, // La contraseña no deberia ser editable aqui
    val rol: String,
    var genero: String,
    // Campos adicionales para el medico (pueden ser nulos para pacientes)
    var especialidad: String? = null,
    var direccionConsultorio: String? = null
)
{
}