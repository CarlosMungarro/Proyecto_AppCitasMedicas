package equipocitasmedicas.citasmedicas.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.ItemCitasMedico
import equipocitasmedicas.citasmedicas.model.CitasMedico
import java.text.SimpleDateFormat
import java.util.*

class CitasMedicoAdapter(
    private val items: MutableList<ItemCitasMedico>,
    private val onClickCita: ((CitasMedico) -> Unit)? = null
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TIPO_DIA = 0
        private const val TIPO_CITA = 1
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is ItemCitasMedico.Dia -> TIPO_DIA
        is ItemCitasMedico.Cita -> TIPO_CITA
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TIPO_DIA) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_header_dia, parent, false)
            DiaViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_cita_medico, parent, false)
            CitaViewHolder(view, onClickCita)
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ItemCitasMedico.Dia -> (holder as DiaViewHolder).bind(item)
            is ItemCitasMedico.Cita -> (holder as CitaViewHolder).bind(item)
        }
    }

    fun actualizarItems(nuevosItems: List<ItemCitasMedico>) {
        items.clear()
        items.addAll(nuevosItems)
        notifyDataSetChanged()
    }

    class DiaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDia = itemView.findViewById<TextView>(R.id.tvNombreDia)
        fun bind(dia: ItemCitasMedico.Dia) {
            tvDia.text = dia.nombreDia
        }
    }

    class CitaViewHolder(itemView: View, val onClick: ((CitasMedico) -> Unit)?) :
        RecyclerView.ViewHolder(itemView) {

        private val tvNombrePaciente = itemView.findViewById<TextView>(R.id.tvNombrePaciente)
        private val tvNotas = itemView.findViewById<TextView>(R.id.tvMotivoCita)
        private val tvHora = itemView.findViewById<TextView>(R.id.tvHoraCita)
        private val tvEstado = itemView.findViewById<TextView>(R.id.tvEstadoCita)

        fun bind(citaItem: ItemCitasMedico.Cita) {
            val cita = citaItem.cita
            tvNombrePaciente.text = cita.pacienteNombre
            tvNotas.text = cita.notas
            tvEstado.text = cita.estado

            // Mostrar solo la hora en la zona horaria de Hermosillo
            cita.fechaHora?.toDate()?.let { fecha ->
                val formatoHora = java.text.SimpleDateFormat("HH:mm", Locale("es", "MX"))
                formatoHora.timeZone = TimeZone.getTimeZone("America/Hermosillo")
                tvHora.text = formatoHora.format(fecha)
            }

            itemView.setOnClickListener { onClick?.invoke(cita) }
        }
    }
}



