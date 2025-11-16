// app/src/main/java/equipocitasmedicas/citasmedicas/ui/RegistroActivity.kt
package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipocitasmedicas.citasmedicas.R
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etEspecialidad: EditText
    private lateinit var etDireccionConsultorio: EditText
    private lateinit var etCedula: EditText // <-- nuevo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etNombre = findViewById<EditText>(R.id.et_nombre_completo)
        val etCorreo = findViewById<EditText>(R.id.et_correo)
        val etFecha = findViewById<EditText>(R.id.et_fecha)
        val etTel = findViewById<EditText>(R.id.et_telefono)
        val etPass = findViewById<EditText>(R.id.et_contra)
        val etPass2 = findViewById<EditText>(R.id.conf_contra)
        val etRol = findViewById<AutoCompleteTextView>(R.id.etRol)
        val etGenero = findViewById<AutoCompleteTextView>(R.id.etGenero)
        etEspecialidad = findViewById(R.id.etEspecialidad)
        etDireccionConsultorio = findViewById(R.id.etDireccionConsultorio)
        etCedula = findViewById(R.id.etCedula) // puede no existir si no actualizaste layout

        val btnGuardar = findViewById<Button>(R.id.btn_registrarse)
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_login)

        etRol.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.roles))
        )
        etGenero.setAdapter(
            ArrayAdapter(this, android.R.layout.simple_list_item_1, resources.getStringArray(R.array.generos))
        )

        etRol.setOnItemClickListener { parent, _, position, _ ->
            val selectedRole = parent.getItemAtPosition(position).toString()
            toggleMedicoFields(selectedRole)
        }

        etFecha.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                etFecha.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).apply {
                datePicker.maxDate = System.currentTimeMillis()
            }.show()
        }

        tvGoLogin.setOnClickListener {
            auth.signOut()
            startActivity(LoginActivity.intent(this).apply { putExtra("forceLogin", true) })
            finish()
        }

        btnGuardar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val fecha = etFecha.text.toString().trim()
            val tel = etTel.text.toString().trim()
            val pass = etPass.text.toString()
            val pass2 = etPass2.text.toString()
            val rol = etRol.text.toString().trim()
            val genero = etGenero.text.toString().trim()
            val especialidad = etEspecialidad.text.toString().trim()
            val direccionConsultorio = etDireccionConsultorio.text.toString().trim()
            val cedula = etCedula.text?.toString()?.trim().orEmpty()

            val isMedico = rol.equals("Médico", ignoreCase = true)

            if (nombre.isEmpty() || correo.isEmpty() || fecha.isEmpty() ||
                tel.isEmpty() || pass.isEmpty() || pass2.isEmpty() || rol.isEmpty() || genero.isEmpty()
            ) { toast("Completa todos los campos."); return@setOnClickListener }

            if (isMedico && (especialidad.isEmpty() || direccionConsultorio.isEmpty())) {
                toast("Completa especialidad y dirección del consultorio."); return@setOnClickListener
            }
            if (!isAgeAtLeast(fecha, 15)) { toast("Debes tener 15 años o más para registrarte."); return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) { toast("Correo inválido."); return@setOnClickListener }
            if (pass != pass2) { toast("Las contraseñas no coinciden."); return@setOnClickListener }
            if (!fecha.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) { toast("Fecha debe ser YYYY-MM-DD."); return@setOnClickListener }

            auth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(this) { task ->
                    if (!task.isSuccessful) { toast(task.exception?.localizedMessage ?: "El registro falló."); return@addOnCompleteListener }
                    val uid = auth.currentUser?.uid ?: run { toast("No se pudo obtener UID."); return@addOnCompleteListener }

                    if (isMedico) {
                        val medico = hashMapOf(
                            "id" to uid,
                            "nombreCompleto" to nombre,
                            "correo" to correo,
                            "fechaNacimiento" to fecha,
                            "telefono" to tel,
                            "rol" to rol,
                            "genero" to genero,
                            "especialidad" to especialidad,
                            "direccionConsultorio" to direccionConsultorio
                        ).apply {
                            if (cedula.isNotEmpty()) put("cedula", cedula) // opcional
                        }

                        db.collection("medicos").document(uid).set(medico)
                            .addOnSuccessListener { toast("Médico registrado correctamente."); finishRegistration() }
                            .addOnFailureListener { e -> Log.e("FirestoreError", "Error guardando médico", e); toast("Error al guardar datos: ${e.message}") }

                    } else {
                        val paciente = hashMapOf(
                            "id" to uid,
                            "nombreCompleto" to nombre,
                            "correo" to correo,
                            "fechaNacimiento" to fecha,
                            "telefono" to tel,
                            "rol" to rol,
                            "genero" to genero
                        )
                        db.collection("pacientes").document(uid).set(paciente)
                            .addOnSuccessListener { toast("Paciente registrado correctamente."); finishRegistration() }
                            .addOnFailureListener { e -> Log.e("FirestoreError", "Error guardando paciente", e); toast("Error al guardar datos: ${e.message}") }
                    }
                }
        }
    }

    private fun isAgeAtLeast(fechaYYYYMMDD: String, minYears: Int): Boolean {
        val parts = fechaYYYYMMDD.split("-")
        if (parts.size != 3) return false
        val y = parts[0].toIntOrNull() ?: return false
        val m = parts[1].toIntOrNull() ?: return false
        val d = parts[2].toIntOrNull() ?: return false

        val today = Calendar.getInstance()
        val birth = Calendar.getInstance().apply {
            set(Calendar.YEAR, y)
            set(Calendar.MONTH, m - 1)
            set(Calendar.DAY_OF_MONTH, d)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
        if (today.get(Calendar.MONTH) < birth.get(Calendar.MONTH) ||
            (today.get(Calendar.MONTH) == birth.get(Calendar.MONTH) && today.get(Calendar.DAY_OF_MONTH) < birth.get(Calendar.DAY_OF_MONTH))
        ) {
            age--
        }
        return age >= minYears
    }

    private fun toggleMedicoFields(role: String) {
        val show = role.equals("Médico", ignoreCase = true)
        etEspecialidad.visibility = if (show) View.VISIBLE else View.GONE
        etDireccionConsultorio.visibility = if (show) View.VISIBLE else View.GONE
        etCedula.visibility = if (show) View.VISIBLE else View.GONE
        if (!show) { etEspecialidad.text.clear(); etDireccionConsultorio.text.clear(); etCedula.text?.clear() }
    }

    private fun finishRegistration() {
        auth.signOut()
        startActivity(LoginActivity.intent(this).apply { putExtra("forceLogin", true) })
        finish()
    }
    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
