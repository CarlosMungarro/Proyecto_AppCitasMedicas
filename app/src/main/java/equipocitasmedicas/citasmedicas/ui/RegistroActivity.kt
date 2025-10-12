package equipocitasmedicas.citasmedicas.ui

import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import equipocitasmedicas.citasmedicas.R
import equipocitasmedicas.citasmedicas.data.PacienteStore
import equipocitasmedicas.citasmedicas.model.Paciente

class RegistroActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
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

        etRol.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.roles))
        )
        etGenero.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.generos))
        )

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

            val paciente = Paciente(
                id = 0,
                nombreCompleto = nombre,
                correo = correo,
                fechaNacimiento = fecha,
                telefono = tel,
                password = pass,
                rol = rol,
                genero = genero
            )

            val result = PacienteStore.add(paciente)
            result.fold(
                onSuccess = {
                    toast("Paciente registrado.")
                    startActivity(LoginActivity.intent(this))
                    finish()
                },
                onFailure = { e -> toast(e.message ?: "Error al registrar.") }
            )
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}