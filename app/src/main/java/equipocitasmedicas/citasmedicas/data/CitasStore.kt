package equipocitasmedicas.citasmedicas.data

import equipocitasmedicas.citasmedicas.model.CitaItem
import equipocitasmedicas.citasmedicas.model.DoctorItem
import java.util.concurrent.atomic.AtomicLong

object CitasStore {
    private val seq = AtomicLong(1)
    private val _citaItems = mutableListOf<CitaItem>()
    val citaItems: List<CitaItem> get() = _citaItems

    fun addCita(pacienteId: Long, doctor: DoctorItem, fecha: String, hora: String, motivo: String): CitaItem {
        val nueva = CitaItem(seq.getAndIncrement(), pacienteId, doctor, fecha, hora, motivo)
        _citaItems.add(nueva)
        return nueva
    }

    fun getCitasByPaciente(pacienteId: Long): List<CitaItem> {
        return _citaItems.filter { it.pacienteId == pacienteId }
    }
}