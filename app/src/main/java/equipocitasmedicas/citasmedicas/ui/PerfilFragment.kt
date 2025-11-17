package equipocitasmedicas.citasmedicas.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.model.Medico
import equipocitasmedicas.citasmedicas.model.Paciente

class PerfilFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var imgPerfil: ImageView
    private lateinit var btnGuardar: View
    private lateinit var btnEditarFoto: View
    private lateinit var btnCerrarSesion: View
    private lateinit var btnCambiarContrasena: View

    private lateinit var editNombre: EditText
    private lateinit var editFecha: EditText
    private lateinit var editGenero: EditText
    private lateinit var editTelefono: EditText
    private lateinit var editEmail: EditText
    private lateinit var editEspecialidad: EditText
    private lateinit var editDireccion: EditText
    private var editCedula: EditText? = null

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { imgPerfil.setImageURI(it); saveProfileImageUri(it) }
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_perfil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imgPerfil = view.findViewById(R.id.imgPerfil)
        btnGuardar = view.findViewById(R.id.btnGuardar)
        btnEditarFoto = view.findViewById(R.id.btnEditarFoto)
        btnCerrarSesion = view.findViewById(R.id.btnCerrarSesion)
        btnCambiarContrasena = view.findViewById(R.id.btnCambiarContrasena)

        editNombre = view.findViewById(R.id.editNombreCompleto)
        editFecha = view.findViewById(R.id.editFechaNacimiento)
        editGenero = view.findViewById(R.id.editGenero)
        editTelefono = view.findViewById(R.id.editTelefono)
        editEmail = view.findViewById(R.id.editEmail)
        editEspecialidad = view.findViewById(R.id.editEspecialidad)
        editDireccion = view.findViewById(R.id.editDireccionConsultorio)
        editCedula = view.findViewById(R.id.editCedula)

        val currentUser = auth.currentUser ?: run {
            toast("Sesión no encontrada. Por favor inicia sesión.")
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            activity?.finish()
            return
        }

        btnEditarFoto.setOnClickListener { selectImageLauncher.launch("image/*") }
        btnGuardar.setOnClickListener { saveUserData() }
        btnCerrarSesion.setOnClickListener { logout() }
        btnCambiarContrasena.setOnClickListener { showChangePasswordDialog() }

        loadProfileImage()
        loadUserData(currentUser.uid)
    }

    private fun saveProfileImageUri(uri: Uri) {
        val sp = requireContext().getSharedPreferences("app_session", Context.MODE_PRIVATE)
        sp.edit().putString("PROFILE_IMAGE_URI_${auth.currentUser?.uid}", uri.toString()).apply()
    }

    private fun loadProfileImage() {
        val sp = requireContext().getSharedPreferences("app_session", Context.MODE_PRIVATE)
        sp.getString("PROFILE_IMAGE_URI_${auth.currentUser?.uid}", null)
            ?.let { imgPerfil.setImageURI(Uri.parse(it)) }
    }

    private fun logout() {
        auth.signOut()
        requireContext().getSharedPreferences("app_session", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        activity?.finish()
    }

    private fun loadUserData(uid: String) {
        db.collection("pacientes").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(Paciente::class.java)?.let { populatePacienteData(it) }
                } else {
                    db.collection("medicos").document(uid).get()
                        .addOnSuccessListener { docMedico ->
                            if (docMedico.exists()) {
                                docMedico.toObject(Medico::class.java)?.let { populateMedicoData(it) }
                            } else toast("Usuario no encontrado")
                        }
                }
            }
            .addOnFailureListener { toast("Error al cargar datos") }
    }

    private fun populatePacienteData(p: Paciente) {
        editNombre.setText(p.nombreCompleto)
        editFecha.setText(p.fechaNacimiento)
        editGenero.setText(p.genero)
        editTelefono.setText(p.telefono)
        editEmail.setText(auth.currentUser?.email)
        editEspecialidad.visibility = View.GONE
        editDireccion.visibility = View.GONE
        editCedula?.visibility = View.GONE
    }

    private fun populateMedicoData(m: Medico) {
        editNombre.setText(m.nombreCompleto)
        editFecha.setText(m.fechaNacimiento)
        editGenero.setText(m.genero)
        editTelefono.setText(m.telefono)
        editEmail.setText(auth.currentUser?.email)
        editEspecialidad.visibility = View.VISIBLE
        editEspecialidad.setText(m.especialidad)
        editDireccion.visibility = View.VISIBLE
        editDireccion.setText(m.direccionConsultorio)
        editCedula?.visibility = View.VISIBLE
        editCedula?.setText(m.cedula.orEmpty())
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val nombre = editNombre.text.toString().trim()
        val fecha = editFecha.text.toString().trim()
        val genero = editGenero.text.toString().trim()
        val telefono = editTelefono.text.toString().trim()

        db.collection("pacientes").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val pacienteUpdate = mapOf(
                        "nombreCompleto" to nombre,
                        "fechaNacimiento" to fecha,
                        "genero" to genero,
                        "telefono" to telefono
                    )
                    db.collection("pacientes").document(uid).update(pacienteUpdate)
                        .addOnSuccessListener { toast("Paciente actualizado") }
                        .addOnFailureListener { toast("Error al actualizar paciente") }
                } else {
                    val especialidad = editEspecialidad.text.toString().trim()
                    val direccion = editDireccion.text.toString().trim()
                    val cedula = editCedula?.text?.toString()?.trim().orEmpty()
                    val medicoUpdate = mutableMapOf<String, Any>(
                        "nombreCompleto" to nombre,
                        "fechaNacimiento" to fecha,
                        "genero" to genero,
                        "telefono" to telefono,
                        "especialidad" to especialidad,
                        "direccionConsultorio" to direccion
                    )
                    medicoUpdate["cedula"] = cedula
                    db.collection("medicos").document(uid).update(medicoUpdate)
                        .addOnSuccessListener { toast("Médico actualizado") }
                        .addOnFailureListener { toast("Error al actualizar médico") }
                }
            }

        val profileUpdates = userProfileChangeRequest { displayName = nombre }
        auth.currentUser?.updateProfile(profileUpdates)
    }

    private fun showChangePasswordDialog() {
        val editText = EditText(requireContext()).apply { hint = "Nueva contraseña (mínimo 6 caracteres)" }
        AlertDialog.Builder(requireContext())
            .setTitle("Cambiar contraseña")
            .setMessage("Introduce tu nueva contraseña:")
            .setView(editText)
            .setPositiveButton("Cambiar") { _, _ ->
                val newPassword = editText.text.toString().trim()
                if (newPassword.length < 6) { toast("La contraseña debe tener al menos 6 caracteres"); return@setPositiveButton }
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnSuccessListener { toast("Contraseña actualizada correctamente") }
                    ?.addOnFailureListener { toast("Error al actualizar contraseña: ${it.message}") }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}