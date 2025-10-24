package equipocitasmedicas.citasmedicas.data

// Ajusta el import a tu DoctorItem real

object CitasStore {
//    private val seq = AtomicLong(1000L)
//
//    fun addCita(
//        pacienteUid: String,
//        doctor: DoctorItem,
//        fecha: String,     // ej: "2025-10-22"
//        hora: String,      // ej: "09:30"
//        motivo: String
//    ): CitaItem {
//        val fechaHora = parseFechaHora(fecha, hora)
//            ?: throw IllegalArgumentException("Fecha/Hora inválidas (usa YYYY-MM-DD y HH:mm)")
//
//        // ⚠️ Necesitamos datos del paciente para la tarjeta UI
//        val paciente: Paciente = PacienteStore.findByUid(pacienteUid)
//            ?: throw IllegalStateException("Paciente no encontrado")
//
//        // Usa el UID del doctor si existe; si no, usa su id Long como string
//        val medicoUid: String = when {
//            hasDoctorUid(doctor) -> getDoctorUid(doctor)
//            else -> getDoctorIdAsString(doctor)
//        }
//
//        val cita = Cita(
//            id = seq.incrementAndGet(),
//            medicoId = medicoUid,
//            pacienteId = pacienteUid,
//            nombrePaciente = paciente.nombreCompleto,
//            nombreMedico = getDoctorNombre(doctor),
//            motivo = motivo,
//            fechaNacimiento = paciente.fechaNacimiento,
//            telefono = paciente.telefono,
//            genero = paciente.genero,
//            fechaHora = fechaHora,
//            estado = "Pendiente",
//            notas = ""
//        )
//
//        CitaStore.agregarCita(cita)
//        return cita.toItem()
//    }
//
//    fun getCitasByPaciente(pacienteUid: String): List<CitaItem> =
//        CitaStore.obtenerCitasPaciente(pacienteUid).map { it.toItem() }
//
//    // ---------- helpers ----------
//    private fun parseFechaHora(fecha: String, hora: String): Date? {
//        // Cambia formato si tu UI usa otro patrón
//        val df = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
//        return runCatching { df.parse("$fecha $hora") }.getOrNull()
//    }
//
//    // Estas 3 funciones dependen de tu DoctorItem real. Adáptalas si el nombre/propiedades difieren.
//    private fun hasDoctorUid(d: DoctorItem): Boolean {
//        // si tu DoctorItem tiene `uid: String`
//        return try {
//            val uid = d.javaClass.getDeclaredField("uid").let { f ->
//                f.isAccessible = true; f.get(d) as? String
//            }
//            !uid.isNullOrBlank()
//        } catch (_: Throwable) { false }
//    }
//
//    private fun getDoctorUid(d: DoctorItem): String {
//        return try {
//            val f = d.javaClass.getDeclaredField("uid"); f.isAccessible = true
//            (f.get(d) as? String).orEmpty()
//        } catch (_: Throwable) {
//            ""
//        }
//    }
//
//    private fun getDoctorIdAsString(d: DoctorItem): String {
//        return try {
//            val f = d.javaClass.getDeclaredField("id"); f.isAccessible = true
//            val id = f.get(d)
//            when (id) {
//                is Long -> id.toString()
//                is Int -> id.toString()
//                is String -> id
//                else -> ""
//            }
//        } catch (_: Throwable) { "" }
//    }
//
//    private fun getDoctorNombre(d: DoctorItem): String {
//        // intenta campos comunes: nombreCompleto / nombre / displayName
//        return try {
//            val tryFields = listOf("nombreCompleto", "nombre", "displayName")
//            for (n in tryFields) {
//                val f = d.javaClass.getDeclaredField(n)
//                f.isAccessible = true
//                val v = f.get(d) as? String
//                if (!v.isNullOrBlank()) return v
//            }
//            "Médico"
//        } catch (_: Throwable) { "Médico" }
//    }
}