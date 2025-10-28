package equipocitasmedicas.citasmedicas.model

// Clase sellada para usar en el RecyclerView con headers por día
sealed class ItemCitasMedico {

    // Subclase que representa un header con el nombre del día
    data class Dia(val nombreDia: String) : ItemCitasMedico()

    // Subclase que representa una cita real
    data class Cita(val cita: CitasMedico) : ItemCitasMedico()
}
