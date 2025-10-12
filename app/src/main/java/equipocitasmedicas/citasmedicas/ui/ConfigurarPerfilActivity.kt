package equipocitasmedicas.citasmedicas.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import equipocitasmedicas.citasmedicas.data.PacienteStore
import equipocitasmedicas.citasmedicas.databinding.ActivityConfigurarPerfilBinding // Asegúrate que el nombre coincida
import equipocitasmedicas.citasmedicas.model.Paciente

class ConfigurarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityConfigurarPerfilBinding
    private var currentUser: Paciente? = null

    // ✅ ÚNICO LAUNCHER NECESARIO: para seleccionar una imagen de la galería
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // Si el usuario selecciona una imagen (uri no es nulo), la mostramos y guardamos
        uri?.let {
            binding.imgPerfil.setImageURI(it)
            saveProfileImageUri(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurarPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val userId = sharedPreferences.getLong("LOGGED_USER_ID", -1L)

        if (userId == -1L) {
            toast("Error: Sesión no encontrada.")
            finish()
            return
        }

        currentUser = PacienteStore.findById(userId)
        if (currentUser == null) {
            toast("Error: No se pudieron cargar los datos del usuario.")
            finish()
            return
        }

        // Configura la UI
        setupUIForRole(currentUser!!.rol)
        populateUserData(currentUser!!)
        loadProfileImage()
        setupListeners()
    }

    private fun setupListeners() {
        binding.btnAtras.setOnClickListener {
            finish()
        }

        binding.btnGuardar.setOnClickListener {
            saveUserData()
        }

        // ✅ LÓGICA SIMPLIFICADA: Al hacer clic, se lanza directamente el selector de imágenes.
        binding.btnEditarFoto.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        binding.btnCerrarSesion.setOnClickListener {
            logout()
        }
    }

    private fun saveProfileImageUri(uri: Uri) {
        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("PROFILE_IMAGE_URI_${currentUser?.id}", uri.toString()).apply()
    }

    private fun loadProfileImage() {
        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString("PROFILE_IMAGE_URI_${currentUser?.id}", null)
        uriString?.let {
            binding.imgPerfil.setImageURI(Uri.parse(it))
        }
    }

    private fun logout() {
        val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        sharedPreferences.edit().clear().apply()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // --- (El resto de tus funciones: saveUserData, populateUserData, etc., se mantienen igual) ---

    private fun saveUserData() {
        currentUser?.let { user ->
            user.nombreCompleto = binding.editNombreCompleto.text.toString().trim()
            user.fechaNacimiento = binding.editFechaNacimiento.text.toString().trim()
            user.genero = binding.editGenero.text.toString().trim()
            user.telefono = binding.editTelefono.text.toString().trim()
            if (user.rol.equals("Médico", ignoreCase = true)) {
                user.especialidad = binding.editEspecialidad.text.toString().trim()
                user.direccionConsultorio = binding.editDireccionConsultorio.text.toString().trim()
            }
            if (PacienteStore.update(user)) {
                toast("¡Perfil actualizado con éxito!")
                finish()
            } else {
                toast("Error al actualizar el perfil.")
            }
        }
    }

    private fun setupUIForRole(role: String) {
        if (role.equals("Médico", ignoreCase = true)) {
            binding.editEspecialidad.visibility = View.VISIBLE
            binding.editDireccionConsultorio.visibility = View.VISIBLE
        } else {
            binding.editEspecialidad.visibility = View.GONE
            binding.editDireccionConsultorio.visibility = View.GONE
        }
    }

    private fun populateUserData(user: Paciente) {
        binding.editNombreCompleto.setText(user.nombreCompleto)
        binding.editFechaNacimiento.setText(user.fechaNacimiento)
        binding.editGenero.setText(user.genero)
        binding.editTelefono.setText(user.telefono)
        binding.editEmail.setText(user.correo)
        if (user.rol.equals("Médico", ignoreCase = true)) {
            binding.editEspecialidad.setText(user.especialidad ?: "")
            binding.editDireccionConsultorio.setText(user.direccionConsultorio ?: "")
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}