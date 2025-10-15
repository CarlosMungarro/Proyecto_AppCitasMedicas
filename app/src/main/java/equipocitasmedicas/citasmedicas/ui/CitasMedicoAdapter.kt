package equipocitasmedicas.citasmedicas.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import equipocitasmedicas.citasmedicas.R

import java.text.SimpleDateFormat
import java.util.*

class CitasMedicoAdapter(
    private var listaCitas: List<Cita>,
    private val onItemClick: (Cita) -> Unit
) : RecyclerView.Adapter<CitasMedicoAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layoutIzquierda: View = view.findViewById(R.id.layoutIzquierda)
        val layoutDerecha: View = view.findViewById(R.id.layoutDerecha)
        val tvHoraCita: TextView = view.findViewById(R.id.tvHoraCita)
        val tvEstadoCita: TextView = view.findViewById(R.id.tvEstadoCita)
        val tvNombrePaciente: TextView = view.findViewById(R.id.tvNombrePaciente)
        val tvMotivoCita: TextView = view.findViewById(R.id.tvMotivoCita)

        fun bind(cita: Cita) {
            val formatoHora = SimpleDateFormat("h:mm a", Locale.getDefault())
            tvHoraCita.text = formatoHora.format(cita.fechaHora)

            // Nombre del paciente
            tvNombrePaciente.text = cita.nombrePaciente

            tvMotivoCita.text = cita.motivo

            when (cita.estado.lowercase()) {
                "completada" -> {
                    tvEstadoCita.text = "Completada"
                    tvEstadoCita.setBackgroundColor(Color.parseColor("#00ACC1"))
                    layoutIzquierda.setBackgroundColor(Color.parseColor("#4DD0E1"))
                    layoutDerecha.setBackgroundColor(Color.parseColor("#1E88E5"))
                }
                "pendiente" -> {
                    tvEstadoCita.text = "Pendiente"
                    tvEstadoCita.setBackgroundColor(Color.parseColor("#FFA726"))
                    layoutIzquierda.setBackgroundColor(Color.parseColor("#FFB74D"))
                    layoutDerecha.setBackgroundColor(Color.parseColor("#1976D2"))
                }
                "cancelada" -> {
                    tvEstadoCita.text = "Cancelada"
                    tvEstadoCita.setBackgroundColor(Color.parseColor("#E53935"))
                    layoutIzquierda.setBackgroundColor(Color.parseColor("#EF5350"))
                    layoutDerecha.setBackgroundColor(Color.parseColor("#C62828"))
                }
                else -> {
                    tvEstadoCita.text = cita.estado
                    tvEstadoCita.setBackgroundColor(Color.parseColor("#757575"))
                    layoutIzquierda.setBackgroundColor(Color.parseColor("#9E9E9E"))
                    layoutDerecha.setBackgroundColor(Color.parseColor("#616161"))
                }
            }

            // Click en el item
            itemView.setOnClickListener {
                onItemClick(cita)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita_medico, parent, false)
        return CitaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        holder.bind(listaCitas[position])
    }

    override fun getItemCount(): Int = listaCitas.size

    fun actualizarCitas(nuevasCitas: List<Cita>) {
        listaCitas = nuevasCitas
        notifyDataSetChanged()
    }
}