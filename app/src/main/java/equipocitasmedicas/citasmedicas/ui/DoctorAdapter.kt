package equipocitasmedicas.citasmedicas.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.DoctorItem

class DoctorAdapter(
    private val doctors: List<DoctorItem>,
    private val onClick: (DoctorItem) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivDoctor: ImageView = itemView.findViewById(R.id.ivDoctor)
        val tvNombre: TextView = itemView.findViewById(R.id.tvNombreDoctor)
        val tvEspecialidad: TextView = itemView.findViewById(R.id.tvEspecialidadDoctor)
        val tvConsultorio: TextView = itemView.findViewById(R.id.tvConsultorioDoctor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]
        holder.tvNombre.text = doctor.nombreCompleto
        holder.tvEspecialidad.text = doctor.especialidad
        holder.tvConsultorio.text = doctor.consultorio

        // Cargar imagen con Glide
        Glide.with(holder.itemView)
            .load(doctor.fotoUrl ?: R.drawable.perfil_default)
            .circleCrop()
            .into(holder.ivDoctor)

        holder.itemView.setOnClickListener {
            onClick(doctor)
        }
    }

    override fun getItemCount(): Int = doctors.size
}
