package equipocitasmedicas.citasmedicas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitaItem

class CitaAdapter(
    private var citas: MutableList<CitaItem>,
    private val onCitaClick: (CitaItem) -> Unit
) : RecyclerView.Adapter<CitaAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPerfil: ImageView = itemView.findViewById(R.id.ivPerfil)
        val tvDoctor: TextView = itemView.findViewById(R.id.tvDoctor)
        val tvEspecialidad: TextView = itemView.findViewById(R.id.tvEspecialidad)
        val tvFecha: TextView = itemView.findViewById(R.id.tvFecha)
        val tvHora: TextView = itemView.findViewById(R.id.tvHora)

        fun bind(cita: CitaItem) {
            tvDoctor.text = cita.doctor.nombreCompleto
            tvEspecialidad.text = cita.doctor.especialidad
            tvFecha.text = cita.fecha
            tvHora.text = cita.hora

            // Aquí puedes poner una imagen por defecto o según tu modelo
            ivPerfil.setImageResource(R.drawable.perfil_default)

            itemView.setOnClickListener {
                onCitaClick(cita)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false) // <- inflamos tu layout correcto
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
