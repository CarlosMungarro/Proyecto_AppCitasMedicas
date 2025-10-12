package equipocitasmedicas.citasmedicas.data

import equipocitasmedicas.citasmedicas.model.Paciente
import java.util.concurrent.atomic.AtomicLong

object PacienteStore {
    private val seq = AtomicLong(1)
    private val _pacientes = mutableListOf<Paciente>()
    val pacientes: List<Paciente> get() = _pacientes

    init { seed() } // Por qué: tener usuarios demo de arranque.

    fun add(p: Paciente): Result<Long> {
        if (_pacientes.any { it.correo.equals(p.correo, ignoreCase = true) }) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado."))
        }
        val nuevo = p.copy(id = seq.getAndIncrement())
        _pacientes.add(nuevo)
        return Result.success(nuevo.id)
    }

    fun login(correo: String, password: String): Paciente? =
        _pacientes.firstOrNull { it.correo.equals(correo, true) && it.password == password }


    fun findById(id: Long): Paciente? {
        return _pacientes.firstOrNull { it.id == id }
    }


    fun update(pacienteActualizado: Paciente): Boolean {
        val index = _pacientes.indexOfFirst { it.id == pacienteActualizado.id }
        if (index != -1) {
            _pacientes[index] = pacienteActualizado
            return true // Éxito
        }
        return false // No se encontró al usuario
    }

    fun count(): Int = _pacientes.size

    private fun seed() {
        if (_pacientes.isNotEmpty()) return
        _pacientes += Paciente(
            id = seq.getAndIncrement(),
            nombreCompleto = "Paciente Demo",
            correo = "demo@correo.com",
            fechaNacimiento = "1990-01-01",
            telefono = "555-000-1111",
            password = "123456",
            rol = "Paciente",
            genero = "Femenino"
        )


        _pacientes += Paciente(
            id = seq.getAndIncrement(),
            nombreCompleto = "Medico Demo",
            correo = "medico@correo.com",
            fechaNacimiento = "1985-05-10",
            telefono = "555-222-3333",
            password = "123456",
            rol = "Médico",
            genero = "Masculino",
            especialidad = "Cardiologo",
            direccionConsultorio = "Av kioki 123"
        )
    }
}


