package equipocitasmedicas.citasmedicas.model

import equipocitasmedicas.citasmedicas.ui.Cita

fun Cita.toItem(): equipocitasmedicas.citasmedicas.model.Cita = Cita(
//    id = id,
//    nombrePaciente = nombrePaciente,
//    nombreMedico = nombreMedico,
//    motivo = motivo,
//    fechaHora = fechaHora,
//    fechaNacimiento = fechaNacimiento,
//    telefono = telefono,
//    genero = genero,
//    estado = estado
)

//// Opcional: para mapear listas de forma compacta
//fun List<Cita>.toItems(): List<equipocitasmedicas.citasmedicas.model.Cita> = map { it.toItem() }