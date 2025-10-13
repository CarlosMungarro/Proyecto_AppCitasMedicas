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
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.data.PacienteStore

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        val etUsuario = findViewById<EditText>(R.id.et_usuario)
        val etPass = findViewById<EditText>(R.id.et_contra)
        val btnLogin = findViewById<Button>(R.id.btn_iniciar_sesion)
        val tvRegistrate = findViewById<TextView>(R.id.txt_registrarse)

        tvRegistrate.setOnClickListener {
            toast("Ir a registro...")
            startActivity(Intent(this@LoginActivity, RegistroActivity::class.java))
        }

        btnLogin.setOnClickListener {
            val correo = etUsuario.text.toString().trim()
            val pass = etPass.text.toString()

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                toast("Usuario debe ser correo válido.")
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                toast("Ingresa tu contraseña.")
                return@setOnClickListener
            }

            val paciente = PacienteStore.login(correo, pass)
            if (paciente != null) {

                //Guardar los datos de la sesion
                val sharedPreferences = getSharedPreferences("app_session", Context.MODE_PRIVATE)
                with(sharedPreferences.edit()) {
                    putLong("LOGGED_USER_ID", paciente.id)
                    putString("LOGGED_USER_ROLE", paciente.rol)
                    apply()
                }

                toast("Bienvenido, ${paciente.nombreCompleto}")

                //Navegar a la pantalla de [pantalla]
                val intent = Intent(this, MisCitasPacienteActivity::class.java)
                startActivity(intent)
                finish()

                // Cierra LoginActivity para que el usuario no pueda volver con el botón de atrás
                finish()
            } else {
                toast("Credenciales incorrectas.")
            }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    // El companion object para el intent no es estrictamente necesario aquí,
    // pero es una buena práctica si otras actividades necesitan iniciar el login.
    companion object {
        fun intent(ctx: Context): Intent = Intent(ctx, LoginActivity::class.java)
    }
}