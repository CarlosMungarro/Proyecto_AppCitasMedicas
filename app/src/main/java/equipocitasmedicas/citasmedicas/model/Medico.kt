package equipocitasmedicas.citasmedicas.model

data class Medico(
    val id: String = "",
    var nombreCompleto: String = "",
    var correo: String = "",
    var fechaNacimiento: String = "",
    var telefono: String = "",
    var genero: String = "",
    var especialidad: String = "",
    var direccionConsultorio: String = "",
    var cedula: String? = null // opcional
)