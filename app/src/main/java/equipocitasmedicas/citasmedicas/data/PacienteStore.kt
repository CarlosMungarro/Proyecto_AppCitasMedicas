package equipocitasmedicas.citasmedicas.data

import equipocitasmedicas.citasmedicas.model.Paciente
import java.util.concurrent.atomic.AtomicLong

object PacienteStore {
    private val pacientesPorUid = mutableMapOf<String, Paciente>()
    val pacientes: List<Paciente> get() = pacientesPorUid.values.toList()

    init { seed() }

    fun add(uid: String, p: Paciente): Result<String> {
        if (uid.isBlank()) return Result.failure(IllegalArgumentException("UID inválido"))
        if (pacientesPorUid.values.any { it.correo.equals(p.correo, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado."))
        }
        val nuevo = p.copy(id = uid)
        pacientesPorUid[uid] = nuevo
        return Result.success(uid)
    }

    fun findByUid(uid: String): Paciente? = pacientesPorUid[uid]

    fun findByCorreo(correo: String): Paciente? =
        pacientesPorUid.values.firstOrNull { it.correo.equals(correo, ignoreCase = true) }

    fun update(pacienteActualizado: Paciente): Boolean {
        val uid = pacienteActualizado.id
        if (uid.isBlank() || !pacientesPorUid.containsKey(uid)) return false
        pacientesPorUid[uid] = pacienteActualizado
        return true
    }

    fun count(): Int = pacientesPorUid.size

    private fun seed() {
        if (pacientesPorUid.isNotEmpty()) return
        // NOTA: estos UIDs de demo NO existen en FirebaseAuth; solo sirven para pruebas locales.
        pacientesPorUid["UID_DEMO_PACIENTE"] = Paciente(
            id = "UID_DEMO_PACIENTE",
            nombreCompleto = "Paciente Demo",
            correo = "demo@correo.com",
            fechaNacimiento = "1990-01-01",
            telefono = "555-000-1111",
            rol = "Paciente",
            genero = "Femenino"
        )
        pacientesPorUid["UID_DEMO_MEDICO"] = Paciente(
            id = "UID_DEMO_MEDICO",
            nombreCompleto = "Medico Demo",
            correo = "medico@correo.com",
            fechaNacimiento = "1985-05-10",
            telefono = "555-222-3333",
            rol = "Médico",
            genero = "Masculino",
            especialidad = "Cardiologo",
            direccionConsultorio = "Av kioki 123"
        )
    }
}


