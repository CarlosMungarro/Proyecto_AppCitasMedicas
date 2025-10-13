package equipocitasmedicas.citasmedicas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitaItem

class CitaAdapter(private var citaItems: MutableList<CitaItem>) :
    RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDoctor: TextView = itemView.findViewById(R.id.tvDoctor)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citaItems[position]
        holder.tvDoctor.text = cita.doctor.nombreCompleto
        holder.tvFecha.text = cita.fecha
        holder.tvHora.text = cita.hora
    }

    override fun getItemCount(): Int = citaItems.size

    // Función para actualizar la lista de citas
    fun updateCitas(nuevasCitaItems: List<CitaItem>) {
        citaItems.clear()
        citaItems.addAll(nuevasCitaItems)
        notifyDataSetChanged()
    }
}