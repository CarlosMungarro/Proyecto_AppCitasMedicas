
package equipocitasmedicas.citasmedicas.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R

class LoginActivity : AppCompatActivity() {


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etUsuario = findViewById<EditText>(R.id.et_usuario)
        val etPass = findViewById<EditText>(R.id.et_contra)
        val btnLogin = findViewById<Button>(R.id.btn_iniciar_sesion)
        val tvRegistrate = findViewById<TextView>(R.id.txt_registrarse)
        val tvOlvidasteContra = findViewById<TextView>(R.id.tv_olvidaste_contra)

        //Registro
        tvRegistrate.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegistroActivity::class.java))
        }

        //Iniciar sesión
        btnLogin.setOnClickListener {
            val correo = etUsuario.text.toString().trim()
            val pass = etPass.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                toast("El correo ingresado no es válido.")
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                toast("Ingresa tu contraseña.")
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        if (currentUser != null) {
                            verificarRolYRedirigir(currentUser.uid, currentUser.email ?: correo)
                        } else {
                            toast("No se pudo obtener el usuario.")
                        }
                    } else {
                        toast("Usuario o contraseña incorrectos.")
                    }
                }
                .addOnFailureListener {
                    toast(it.localizedMessage ?: "Error inesperado al iniciar sesión.")
                }
        }

        //Recuperar contraseña
        tvOlvidasteContra.setOnClickListener {
            mostrarDialogoRecuperarContra()
        }
    }

    // ---- Verificar rol y redirigir ----
    private fun verificarRolYRedirigir(uid: String, email: String) {
        // Primero busca en la colección de médicos
        db.collection("medicos").document(uid).get()
            .addOnSuccessListener { medicoDoc ->
                if (medicoDoc.exists()) {
                    // Es un médico
                    val rol = "Médico"
                    saveSession(uid, email, rol)
                    toast("Bienvenido Dr./Dra., $email")
                    startActivity(Intent(this, MisCitasMedicoActivity::class.java))
                    finish()
                } else {
                    // Si no es médico, busca en pacientes
                    db.collection("pacientes").document(uid).get()
                        .addOnSuccessListener { pacienteDoc ->
                            if (pacienteDoc.exists()) {
                                // Es un paciente
                                val rol = "Paciente"
                                saveSession(uid, email, rol)
                                toast("Bienvenido, $email")
                                startActivity(Intent(this, MisCitasPacienteActivity::class.java))
                                finish()
                            } else {
                                toast("No se encontró información del usuario.")
                            }
                        }
                        .addOnFailureListener { e ->
                            toast("Error al verificar datos: ${e.localizedMessage}")
                        }
                }
            }
            .addOnFailureListener { e ->
                toast("Error al verificar rol: ${e.localizedMessage}")
            }
    }

    // ---- Helpers ----
    private fun saveSession(uid: String, email: String, rol: String) {
        val sp = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        with(sp.edit()) {
            putString("LOGGED_USER_ID", uid)
            putString("LOGGED_USER_EMAIL", email)
            putString("LOGGED_USER_ROL", rol)
            apply()
        }
    }

    private fun toast(msg: String) =
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun mostrarDialogoRecuperarContra() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Recuperar contraseña")

        val input = EditText(this)
        input.hint = "Ingresa tu correo electrónico"
        builder.setView(input)

        builder.setPositiveButton("Enviar") { dialog, _ ->
            val correo = input.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                toast("Correo no válido.")
            } else {
                auth.sendPasswordResetEmail(correo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toast("Correo de recuperación enviado.")
                        } else {
                            toast("Error al enviar correo: ${task.exception?.localizedMessage}")
                        }
                    }
            }
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    companion object {
        fun intent(ctx: Context): Intent = Intent(ctx, LoginActivity::class.java)
    }
}