
package equipocitasmedicas.citasmedicas.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.data.PacienteStore
import equipocitasmedicas.citasmedicas.model.Paciente
import java.text.Normalizer

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        auth = Firebase.auth

        val etUsuario = findViewById<EditText>(R.id.et_usuario)
        val etPass = findViewById<EditText>(R.id.et_contra)
        val btnLogin = findViewById<Button>(R.id.btn_iniciar_sesion)
        val tvRegistrate = findViewById<TextView>(R.id.txt_registrarse)

        tvRegistrate.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistroActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val correo = etUsuario.text.toString().trim()
            val pass = etPass.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { toast("Usuario debe ser correo válido."); return@setOnClickListener }
            if (pass.isEmpty()) { toast("Ingresa tu contraseña."); return@setOnClickListener }

            auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid ?: run {
                            toast("No se pudo obtener el usuario."); return@addOnCompleteListener
                        }
                        val perfil = PacienteStore.findByUid(uid) ?: run {
                            toast("Perfil no encontrado, completa tu registro.")
                            startActivity(Intent(this, RegistroActivity::class.java).apply {
                                putExtra("prefill_email", auth.currentUser?.email ?: correo)
                            })
                            return@addOnCompleteListener
                        }

                        saveSession(perfil)
                        toast("Bienvenido, ${perfil.nombreCompleto}")
                        routeByRole(perfil)
                        finish()
                    } else {
                        toast("Usuario y/o contraseña equivocados.")
                    }
                }
                .addOnFailureListener {
                    toast(it.localizedMessage ?: "Error inesperado al iniciar sesión.")
                }
        }
    }

    public override fun onStart() {
        super.onStart()

        val forceLogin = intent?.getBooleanExtra("forceLogin", false) == true
        if (forceLogin) return

        val currentUser = auth.currentUser ?: return
        val perfil = PacienteStore.findByUid(currentUser.uid) ?: run {
            // Si hay sesión sin perfil, redirige a completar registro
            startActivity(Intent(this, RegistroActivity::class.java).apply {
                putExtra("prefill_email", currentUser.email ?: "")
            })
            finish()
            return
        }

        saveSession(perfil)
        routeByRole(perfil)
        finish()
    }

    // ---- Helpers ----

    private fun saveSession(perfil: Paciente) {
        val sp = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        with(sp.edit()) {
            putString("LOGGED_USER_ID", perfil.id)   // UID Firebase
            putString("LOGGED_USER_ROLE", perfil.rol)
            apply()
        }
    }

    private fun routeByRole(perfil: Paciente) {
        when (normalizeRole(perfil.rol)) {
            "medico" -> startActivity(Intent(this, MisCitasMedicoActivity::class.java))
            "paciente" -> startActivity(Intent(this, MisCitasPacienteActivity::class.java))
            else -> {
                toast("Rol no reconocido: ${perfil.rol}")
                // Opcional: enviar a una pantalla genérica o a ConfigurarPerfilActivity
            }
        }
    }

    private fun normalizeRole(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val lower = raw.trim().lowercase()
        val noAccents = Normalizer.normalize(lower, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
        // Mapear variantes comunes
        return when (noAccents) {
            "medico", "doctor", "dr", "doctora" -> "medico"
            "paciente" -> "paciente"
            else -> noAccents
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    companion object {
        fun intent(ctx: Context): Intent = Intent(ctx, LoginActivity::class.java)
    }
}
