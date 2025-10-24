
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
import equipocitasmedicas.citasmedicas.R

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        auth = FirebaseAuth.getInstance()

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
                            saveSession(currentUser.uid, currentUser.email ?: correo)
                            toast("Bienvenido, ${currentUser.email}")
                            startActivity(Intent(this, MisCitasPacienteActivity::class.java))
                            finish()
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

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            saveSession(currentUser.uid, currentUser.email ?: "")
            startActivity(Intent(this, MisCitasPacienteActivity::class.java))
            finish()
        }
    }

    // ---- Helpers ----
    private fun saveSession(uid: String, email: String) {
        val sp = getSharedPreferences("app_session", Context.MODE_PRIVATE)
        with(sp.edit()) {
            putString("LOGGED_USER_ID", uid)
            putString("LOGGED_USER_EMAIL", email)
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
