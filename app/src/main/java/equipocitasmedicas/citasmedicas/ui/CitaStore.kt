package equipocitasmedicas.citasmedicas.ui

import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Calendar
import java.util.Date




object CitaStore {
    private val citas = mutableListOf<Cita>()

    fun obtenerCitasMedicoDia(medicoId: Long, fecha: Date): List<Cita> {
        val calendario = Calendar.getInstance()
        calendario.time = fecha

        val inicioDia = Calendar.getInstance().apply {
            set(calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH),
                0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val finDia = Calendar.getInstance().apply {
            set(calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH),
                23, 59, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return citas.filter { cita ->
            cita.medicoId == medicoId &&
                    cita.fechaHora >= inicioDia.time &&
                    cita.fechaHora <= finDia.time
        }.sortedBy { it.fechaHora }
    }

    fun obtenerCitasMedicoSemana(medicoId: Long, fecha: Date): List<Cita> {
        val calendario = Calendar.getInstance()
        calendario.time = fecha

        // Inicio de la semana (lunes)
        calendario.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val inicioSemana = Calendar.getInstance().apply {
            time = calendario.time
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Fin de la semana (domingo)
        val finSemana = Calendar.getInstance().apply {
            time = inicioSemana.time
            add(Calendar.DAY_OF_YEAR, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        return citas.filter { cita ->
            cita.medicoId == medicoId &&
                    cita.fechaHora >= inicioSemana.time &&
                    cita.fechaHora <= finSemana.time
        }.sortedBy { it.fechaHora }
    }

    fun agregarCita(cita: Cita) {
        citas.add(cita)
    }

    fun obtenerCitasMedico(medicoId: Long): List<Cita> {
        return citas.filter { it.medicoId == medicoId }
            .sortedBy { it.fechaHora }
    }

    fun obtenerCitasPaciente(pacienteId: Long): List<Cita> {
        return citas.filter { it.pacienteId == pacienteId }
            .sortedBy { it.fechaHora }
    }

    fun actualizarEstadoCita(citaId: Long, nuevoEstado: String): Boolean {
        val index = citas.indexOfFirst { it.id == citaId }
        if (index != -1) {
            val citaActualizada = citas[index].copy(estado = nuevoEstado)
            citas[index] = citaActualizada
            return true
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun eliminarCita(citaId: Long): Boolean {
        return citas.removeIf { it.id == citaId }
    }

    /**
     * Valida si una configuración de disponibilidad es compatible con las citas existentes
     * @param medicoId ID del médico
     * @param diaSemana Día de la semana (Calendar.MONDAY, Calendar.TUESDAY, etc.)
     * @param horaInicio Hora de inicio en formato "HH:mm" (ej: "8:00")
     * @param horaFin Hora de fin en formato "HH:mm" (ej: "17:00")
     * @return ValidationResult con el resultado de la validación
     */
    fun validarDisponibilidad(
        medicoId: Long,
        diaSemana: Int,
        horaInicio: String,
        horaFin: String
    ): ValidationResult {
        // Obtener todas las citas del médico para ese día de la semana
        val citasDelDia = citas.filter { cita ->
            cita.medicoId == medicoId &&
                    Calendar.getInstance().apply { time = cita.fechaHora }.get(Calendar.DAY_OF_WEEK) == diaSemana
        }

        if (citasDelDia.isEmpty()) {
            return ValidationResult(
                isValid = true,
                mensaje = "Disponibilidad válida",
                citasConflicto = emptyList()
            )
        }

        // Parsear horas de disponibilidad
        val (horaInicioNum, minInicioNum) = horaInicio.split(":").map { it.toInt() }
        val (horaFinNum, minFinNum) = horaFin.split(":").map { it.toInt() }

        val minutosInicio = horaInicioNum * 60 + minInicioNum
        val minutosFin = horaFinNum * 60 + minFinNum

        // Verificar conflictos con citas existentes
        val citasConflicto = citasDelDia.filter { cita ->
            val calendario = Calendar.getInstance().apply { time = cita.fechaHora }
            val horaCita = calendario.get(Calendar.HOUR_OF_DAY)
            val minCita = calendario.get(Calendar.MINUTE)
            val minutosCita = horaCita * 60 + minCita

            // La cita está fuera del rango de disponibilidad
            minutosCita < minutosInicio || minutosCita >= minutosFin
        }

        return if (citasConflicto.isNotEmpty()) {
            ValidationResult(
                isValid = false,
                mensaje = "No puedes guardar esta disponibilidad porque tienes citas programadas fuera de este horario",
                citasConflicto = citasConflicto
            )
        } else {
            ValidationResult(
                isValid = true,
                mensaje = "Disponibilidad válida",
                citasConflicto = emptyList()
            )
        }
    }

    /**
     * Valida disponibilidad para una fecha específica
     * @param medicoId ID del médico
     * @param fecha Fecha a validar
     * @param horaInicio Hora de inicio en formato "HH:mm"
     * @param horaFin Hora de fin en formato "HH:mm"
     * @return ValidationResult con el resultado de la validación
     */
    fun validarDisponibilidadPorFecha(
        medicoId: Long,
        fecha: Date,
        horaInicio: String,
        horaFin: String
    ): ValidationResult {
        val citasDelDia = obtenerCitasMedicoDia(medicoId, fecha)

        if (citasDelDia.isEmpty()) {
            return ValidationResult(
                isValid = true,
                mensaje = "Disponibilidad válida",
                citasConflicto = emptyList()
            )
        }

        // Parsear horas de disponibilidad
        val (horaInicioNum, minInicioNum) = horaInicio.split(":").map { it.toInt() }
        val (horaFinNum, minFinNum) = horaFin.split(":").map { it.toInt() }

        val minutosInicio = horaInicioNum * 60 + minInicioNum
        val minutosFin = horaFinNum * 60 + minFinNum

        // Verificar conflictos con citas existentes
        val citasConflicto = citasDelDia.filter { cita ->
            val calendario = Calendar.getInstance().apply { time = cita.fechaHora }
            val horaCita = calendario.get(Calendar.HOUR_OF_DAY)
            val minCita = calendario.get(Calendar.MINUTE)
            val minutosCita = horaCita * 60 + minCita

            // La cita está fuera del rango de disponibilidad
            minutosCita < minutosInicio || minutosCita >= minutosFin
        }

        return if (citasConflicto.isNotEmpty()) {
            ValidationResult(
                isValid = false,
                mensaje = "No puedes guardar esta disponibilidad porque tienes citas programadas fuera de este horario",
                citasConflicto = citasConflicto
            )
        } else {
            ValidationResult(
                isValid = true,
                mensaje = "Disponibilidad válida",
                citasConflicto = emptyList()
            )
        }
    }

    fun agregarDatosPrueba(medicoId: Long) {
        // Evitar duplicados
        if (citas.isNotEmpty()) return

        val hoy = Calendar.getInstance()

        // Cita 1: Completada - 8:00 AM
        val cita1 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Cita 2: Completada - 9:30 AM
        val cita2 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        // Cita 3: Pendiente - 10:30 AM
        val cita3 = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
        }

        citas.addAll(listOf(
            Cita(
                id = 1L,
                medicoId = medicoId,
                pacienteId = 101L,
                nombrePaciente = "Andrea Flores",
                nombreMedico = "Dr. García",
                motivo = "Primera consulta",
                fechaHora = cita1.time,
                estado = "Completada",
                notas = ""
            ),
            Cita(
                id = 2L,
                medicoId = medicoId,
                pacienteId = 102L,
                nombrePaciente = "Mauro Lainez",
                nombreMedico = "Dr. García",
                motivo = "Consulta por dolor de estómago",
                fechaHora = cita2.time,
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
                fechaHora = cita3.time,
                estado = "Pendiente",
                notas = ""
            )
        ))
    }

    fun limpiarDatosPrueba() {
        citas.clear()
    }
}

/**
 * Clase de datos para el resultado de validación
 */
data class ValidationResult(
    val isValid: Boolean,
    val mensaje: String,
    val citasConflicto: List<Cita>
)