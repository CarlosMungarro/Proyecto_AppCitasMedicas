// app/src/main/java/equipocitasmedicas/citasmedicas/ui/CitaAdapter.kt
package equipocitasmedicas.citasmedicas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Cita
import java.text.SimpleDateFormat
import java.util.Locale

class CitaAdapter(
    private var citas: MutableList<Cita>,
    private val onCitaClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    private val dateFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPerfil: ImageView = itemView.findViewById(R.id.ivPerfil)
        private val tvDoctor: TextView = itemView.findViewById(R.id.tvDoctor)
        private val tvEspecialidad: TextView = itemView.findViewById(R.id.tvEspecialidad)
        private val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        private val tvHora: TextView = itemView.findViewById(R.id.tvHora)

        fun bind(cita: Cita) {
            tvDoctor.text = cita.medicoNombre
            tvEspecialidad.text = if (cita.medicoEspecialidad.isNotBlank()) cita.medicoEspecialidad else "-"

            cita.fechaHora?.toDate()?.let { date ->
                tvFecha.text = dateFmt.format(date)
                tvHora.text = timeFmt.format(date)
            } ?: run {
                tvFecha.text = "-"
                tvHora.text = "-"
            }

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

    fun updateCitas(nuevasCitas: List<Cita>) {
        citas.clear()
        citas.addAll(nuevasCitas)
        notifyDataSetChanged()
    }
}
