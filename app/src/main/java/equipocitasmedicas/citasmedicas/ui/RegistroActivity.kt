package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
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
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = Firebase.auth
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        val etNombre = findViewById<EditText>(R.id.et_nombre_completo)
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etFecha  = findViewById<EditText>(R.id.et_fecha)
        val etTel    = findViewById<EditText>(R.id.et_telefono)
        val etPass   = findViewById<EditText>(R.id.et_contra)
        val etPass2  = findViewById<EditText>(R.id.conf_contra)
        val etRol    = findViewById<AutoCompleteTextView>(R.id.etRol)
        val etGenero = findViewById<AutoCompleteTextView>(R.id.etGenero)
        val btnGuardar = findViewById<Button>(R.id.btn_registrarse)
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_login)

        intent.getStringExtra("prefill_email")?.let(etCorreo::setText)

        etRol.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.roles)))
        etGenero.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.generos)))

        etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d -> etFecha.setText(String.format("%04d-%02d-%02d", y, m + 1, d)) },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }

        tvGoLogin.setOnClickListener {
            // Evita que LoginActivity te saque por sesión activa
            FirebaseAuth.getInstance().signOut()
            startActivity(LoginActivity.intent(this).apply {
                putExtra("forceLogin", true) // desactiva auto-redirect en onStart
            })
            finish()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val fecha  = etFecha.text.toString().trim()
            val tel    = etTel.text.toString().trim()
            val pass   = etPass.text.toString()
            val pass2  = etPass2.text.toString()
            val rol    = etRol.text.toString().trim()
            val genero = etGenero.text.toString().trim()

            if (nombre.isEmpty() || correo.isEmpty() || fecha.isEmpty() ||
                tel.isEmpty() || pass.isEmpty() || pass2.isEmpty() || rol.isEmpty() || genero.isEmpty()
            ) { toast("Completa todos los campos."); return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { toast("Correo inválido."); return@setOnClickListener }
            if (pass != pass2) { toast("Las contraseñas no coinciden."); return@setOnClickListener }
            if (!fecha.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) { toast("Fecha debe ser YYYY-MM-DD."); return@setOnClickListener }

            auth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid == null) { toast("No se pudo obtener UID."); return@addOnCompleteListener }

                        val paciente = Paciente(
                            id = uid,
                            nombreCompleto = nombre,
                            correo = correo,
                            fechaNacimiento = fecha,
                            telefono = tel,
                            rol = rol,
                            genero = genero
                        )

                        PacienteStore.add(uid, paciente)
                            .onSuccess {
                                toast("Paciente registrado.")
                                startActivity(LoginActivity.intent(this))
                                FirebaseAuth.getInstance().signOut() // opcional: fuerza login explícito
                                startActivity(LoginActivity.intent(this).apply { putExtra("forceLogin", true) })
                                finish()
                            }
                            .onFailure { e ->
                                toast(e.message ?: "Error al registrar perfil.")
                            }
                    } else {
                        toast(task.exception?.localizedMessage ?: "El registro falló.")
                    }
                }
                .addOnFailureListener {
                    toast(it.localizedMessage ?: "Error inesperado en registro.")
                }
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}