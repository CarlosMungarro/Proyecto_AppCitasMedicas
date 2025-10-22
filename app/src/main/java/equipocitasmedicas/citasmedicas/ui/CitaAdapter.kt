// app/src/main/java/equipocitasmedicas/citasmedicas/ui/CitaAdapter.kt
package equipocitasmedicas.citasmedicas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitaItem
import java.text.SimpleDateFormat
import java.util.Locale

class CitaAdapter(
    private var citas: MutableList<CitaItem>,
    private val onCitaClick: (CitaItem) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    // Por qué: un solo formateador reutilizable
    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPerfil: ImageView = itemView.findViewById(R.id.ivPerfil)
        private val tvDoctor: TextView = itemView.findViewById(R.id.tvDoctor)
        private val tvEspecialidad: TextView = itemView.findViewById(R.id.tvEspecialidad)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvHora: TextView = itemView.findViewById(R.id.tvHora)

        fun bind(cita: CitaItem) {
            tvDoctor.text = cita.nombreMedico
            // No tenemos especialidad en CitaItem; mostramos guion o vacio
            tvEspecialidad.text = "-"

            tvFecha.text = dateFmt.format(cita.fechaHora)
            tvHora.text  = timeFmt.format(cita.fechaHora)

            ivPerfil.setImageResource(R.drawable.perfil_default)

            itemView.setOnClickListener { onCitaClick(cita) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return CitaViewHolder(view)
    }

    override fun getItemCount(): Int = citas.size

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(citas[position])
    }

    fun updateCitas(nuevasCitas: List<CitaItem>) {
        citas.clear()
        citas.addAll(nuevasCitas)
        notifyDataSetChanged()
    }
}
