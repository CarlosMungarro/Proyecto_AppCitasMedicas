package equipocitasmedicas.citasmedicas.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.databinding.ActivityConfigurarPerfilBinding
import equipocitasmedicas.citasmedicas.model.Paciente
import equipocitasmedicas.citasmedicas.model.Medico

class ConfigurarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurarPerfilBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val selectImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                binding.imgPerfil.setImageURI(it)
                saveProfileImageUri(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val currentUser = auth.currentUser
        if (currentUser == null) {
            toast("Sesión no encontrada. Por favor inicia sesión.")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupListeners()
        loadProfileImage()
        loadUserData(currentUser.uid)
    }

    private fun setupListeners() {
        binding.btnAtras.setOnClickListener { finish() }
        binding.btnGuardar.setOnClickListener { saveUserData() }
        binding.btnEditarFoto.setOnClickListener { selectImageLauncher.launch("image/*") }
        binding.btnCerrarSesion.setOnClickListener { logout() }
        binding.btnCambiarContrasena.setOnClickListener { showChangePasswordDialog() }
    }

    private fun saveProfileImageUri(uri: Uri) {
        val sp = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        sp.edit().putString("PROFILE_IMAGE_URI_${auth.currentUser?.uid}", uri.toString()).apply()
    }

    private fun loadProfileImage() {
        val sp = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val uriString = sp.getString("PROFILE_IMAGE_URI_${auth.currentUser?.uid}", null)
        uriString?.let { binding.imgPerfil.setImageURI(Uri.parse(it)) }
    }

    private fun logout() {
        auth.signOut()
        val sp = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        sp.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun loadUserData(uid: String) {
        db.collection("pacientes").document(uid).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val paciente = snapshot.toObject(Paciente::class.java)
                    paciente?.let { populatePacienteData(it) }
                } else {
                    db.collection("medicos").document(uid).get()
                        .addOnSuccessListener { docMedico ->
                            if (docMedico.exists()) {
                                val medico = docMedico.toObject(Medico::class.java)
                                medico?.let { populateMedicoData(it) }
                            } else {
                                toast("Usuario no encontrado")
                            }
                        }
                }
            }
            .addOnFailureListener { toast("Error al cargar datos") }
    }

    private fun populatePacienteData(p: Paciente) {
        binding.editNombreCompleto.setText(p.nombreCompleto)
        binding.editFechaNacimiento.setText(p.fechaNacimiento)
        binding.editGenero.setText(p.genero)
        binding.editTelefono.setText(p.telefono)
        binding.editEmail.setText(auth.currentUser?.email)

        binding.editEspecialidad.visibility = View.GONE
        binding.editDireccionConsultorio.visibility = View.GONE
    }

    private fun populateMedicoData(m: Medico) {
        binding.editNombreCompleto.setText(m.nombreCompleto)
        binding.editFechaNacimiento.setText(m.fechaNacimiento)
        binding.editGenero.setText(m.genero)
        binding.editTelefono.setText(m.telefono)
        binding.editEmail.setText(auth.currentUser?.email)

        binding.editEspecialidad.visibility = View.VISIBLE
        binding.editEspecialidad.setText(m.especialidad)
        binding.editDireccionConsultorio.visibility = View.VISIBLE
        binding.editDireccionConsultorio.setText(m.direccionConsultorio)
    }

    private fun saveUserData() {
        val uid = auth.currentUser?.uid ?: return
        val nombre = binding.editNombreCompleto.text.toString().trim()
        val fecha = binding.editFechaNacimiento.text.toString().trim()
        val genero = binding.editGenero.text.toString().trim()
        val telefono = binding.editTelefono.text.toString().trim()

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
                    val especialidad = binding.editEspecialidad.text.toString().trim()
                    val direccion = binding.editDireccionConsultorio.text.toString().trim()
                    val medicoUpdate = mapOf(
                        "nombreCompleto" to nombre,
                        "fechaNacimiento" to fecha,
                        "genero" to genero,
                        "telefono" to telefono,
                        "especialidad" to especialidad,
                        "direccionConsultorio" to direccion
                    )
                    db.collection("medicos").document(uid).update(medicoUpdate)
                        .addOnSuccessListener { toast("Médico actualizado") }
                        .addOnFailureListener { toast("Error al actualizar médico") }
                }
            }

        val profileUpdates = userProfileChangeRequest { displayName = nombre }
        auth.currentUser?.updateProfile(profileUpdates)
    }

    /** 🔒 Cambiar contraseña con un AlertDialog **/
    private fun showChangePasswordDialog() {
        val editText = EditText(this)
        editText.hint = "Nueva contraseña (mínimo 6 caracteres)"

        AlertDialog.Builder(this)
            .setTitle("Cambiar contraseña")
            .setMessage("Introduce tu nueva contraseña:")
            .setView(editText)
            .setPositiveButton("Cambiar") { _, _ ->
                val newPassword = editText.text.toString().trim()
                if (newPassword.length < 6) {
                    toast("La contraseña debe tener al menos 6 caracteres")
                    return@setPositiveButton
                }
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnSuccessListener {
                        toast("Contraseña actualizada correctamente")
                    }
                    ?.addOnFailureListener {
                        toast("Error al actualizar contraseña: ${it.message}")
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}