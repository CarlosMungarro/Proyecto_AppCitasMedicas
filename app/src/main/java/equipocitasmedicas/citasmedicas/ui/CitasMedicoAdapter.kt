package equipocitasmedicas.citasmedicas.ui

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.CitasMedico
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.*

class CitasMedicoAdapter(
    private var citas: List<CitasMedico>,
    private val onClick: (CitasMedico) -> Unit
) : RecyclerView.Adapter<CitasMedicoAdapter.CitaViewHolder>() {

    inner class CitaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombrePaciente: TextView = itemView.findViewById(R.id.tvNombrePaciente)
        val tvHora: TextView = itemView.findViewById(R.id.tvHoraCita)
        val tvMotivo: TextView = itemView.findViewById(R.id.tvMotivoCita)
        val tvEstado: TextView = itemView.findViewById(R.id.tvEstadoCita)
        val cardCita: CardView = itemView.findViewById(R.id.cardCita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CitaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita_medico, parent, false)
        return CitaViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CitaViewHolder, position: Int) {
        val cita = citas[position]

        // Nombre del paciente
        holder.tvNombrePaciente.text = cita.pacienteNombre

        // Hora de la cita
        val hora = if (cita.fechaHora != null) {
            // Si fechaHora ya es un Date
            val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatoHora.format(cita.fechaHora)
        } else {
            "Sin hora"
        }
        holder.tvHora.text = hora

        // Motivo/Notas
        holder.tvMotivo.text = if (cita.notas.isNotEmpty()) cita.notas else "Sin motivo especificado"

        // Estado de la cita
        holder.tvEstado.text = when(cita.estado) {
            "pendiente" -> "Pendiente"
            "confirmada" -> "Confirmada"
            "completada" -> "Completada"
            "cancelada" -> "Cancelada"
            else -> cita.estado.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

        // Color según el estado
        val colorEstado = when(cita.estado) {
            "pendiente" -> R.color.estado_pendiente
            "confirmada" -> R.color.estado_confirmada
            "completada" -> R.color.estado_completada
            "cancelada" -> R.color.estado_cancelada
            else -> R.color.estado_pendiente
        }
        holder.tvEstado.setTextColor(
            ContextCompat.getColor(holder.itemView.context, colorEstado)
        )

        // Click listener
        holder.cardCita.setOnClickListener { onClick(cita) }
    }

    override fun getItemCount(): Int = citas.size

    fun actualizarCitas(nuevasCitas: List<CitasMedico>) {
        citas = nuevasCitas
        notifyDataSetChanged()
    }
}

