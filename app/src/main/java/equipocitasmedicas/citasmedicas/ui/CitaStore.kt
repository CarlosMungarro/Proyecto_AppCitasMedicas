package equipocitasmedicas.citasmedicas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.*

object CitaStore {
    private val citas = mutableListOf<Cita>()

    fun obtenerCitasMedicoDia(medicoId: Long, fecha: Date): List<Cita> {
        val calendario = Calendar.getInstance()
        calendario.time = fecha

        val inicioDia = Calendar.getInstance().apply {
            set(calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val finDia = Calendar.getInstance().apply {
            set(calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH), 23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return citas.filter {
            it.medicoId == medicoId && it.fechaHora >= inicioDia.time && it.fechaHora <= finDia.time
        }.sortedBy { it.fechaHora }
    }

    fun obtenerCitasMedicoSemana(medicoId: Long, fecha: Date): List<Cita> {
        val calendario = Calendar.getInstance()
        calendario.time = fecha
        calendario.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val inicioSemana = Calendar.getInstance().apply {
            time = calendario.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val finSemana = Calendar.getInstance().apply {
            time = inicioSemana.time
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return citas.filter {
            it.medicoId == medicoId && it.fechaHora >= inicioSemana.time && it.fechaHora <= finSemana.time
        }.sortedBy { it.fechaHora }
    }

    fun agregarCita(cita: Cita) {
        citas.add(cita)
    }

    fun obtenerCitasMedico(medicoId: Long): List<Cita> =
        citas.filter { it.medicoId == medicoId }.sortedBy { it.fechaHora }

    fun obtenerCitasPaciente(pacienteId: Long): List<Cita> =
        citas.filter { it.pacienteId == pacienteId }.sortedBy { it.fechaHora }

    fun actualizarEstadoCita(citaId: Long, nuevoEstado: String): Boolean {
        val index = citas.indexOfFirst { it.id == citaId }
        if (index != -1) {
            citas[index] = citas[index].copy(estado = nuevoEstado)
            return true
        }
        return false
    }

    // ✅ FUNCIÓN NUEVA: Validar disponibilidad
    fun validarDisponibilidad(
        medicoId: Long,
        diaSemana: Int,
        horaInicio: String,
        horaFin: String
    ): ValidationResult {
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())

        val horaInicioDate = formato.parse(horaInicio)
        val horaFinDate = formato.parse(horaFin)

        // Tomamos solo las citas del médico para ese día
        val citasDia = citas.filter { cita ->
            val cal = Calendar.getInstance().apply { time = cita.fechaHora }
            cita.medicoId == medicoId && cal.get(Calendar.DAY_OF_WEEK) == diaSemana
        }

        val conflictos = citasDia.filter { cita ->
            val horaCita = formato.format(cita.fechaHora)
            val horaCitaDate = formato.parse(horaCita)

            horaCitaDate.after(horaInicioDate) && horaCitaDate.before(horaFinDate)
        }

        return if (conflictos.isNotEmpty()) {
            ValidationResult(
                isValid = false,
                mensaje = "Hay citas fuera del rango permitido",
                citasConflicto = conflictos
            )
        } else {
            ValidationResult(
                isValid = true,
                mensaje = "Sin conflictos",
                citasConflicto = emptyList()
            )
        }
    }

    fun agregarDatosPrueba(medicoId: Long) {
        if (citas.isNotEmpty()) return

        val cita1Hora = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
        }

        val cita2Hora = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 30)
        }

        val cita3Hora = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
        }

        citas.addAll(
            listOf(
                Cita(
                    id = 1L,
                    medicoId = medicoId,
                    pacienteId = 101L,
                    nombrePaciente = "Andrea Flores",
                    nombreMedico = "Dr. García",
                    motivo = "Primera consulta",
                    fechaNacimiento = "12/04/1995",
                    telefono = "6441234567",
                    genero = "Femenino",
                    fechaHora = cita1Hora.time,
                    estado = "Completada",
                    notas = ""
                ),
                Cita(
                    id = 2L,
                    medicoId = medicoId,
                    pacienteId = 102L,
                    nombrePaciente = "Mauro Lainez",
                    nombreMedico = "Dr. García",
                    motivo = "Dolor de estómago",
                    fechaNacimiento = "25/10/1990",
                    telefono = "6442345678",
                    genero = "Masculino",
                    fechaHora = cita2Hora.time,
                    estado = "Completada",
                    notas = ""
                ),
                Cita(
                    id = 3L,
                    medicoId = medicoId,
                    pacienteId = 103L,
                    nombrePaciente = "Ian Ortiz",
                    nombreMedico = "Dr. García",
                    motivo = "Rayos X",
                    fechaNacimiento = "05/08/2002",
                    telefono = "6443456789",
                    genero = "Masculino",
                    fechaHora = cita3Hora.time,
                    estado = "Pendiente",
                    notas = ""
                )
            )
        )
    }

    fun limpiarDatosPrueba() {
        citas.clear()
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val mensaje: String,
    val citasConflicto: List<Cita>
)
