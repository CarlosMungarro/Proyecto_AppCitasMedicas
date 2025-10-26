package equipocitasmedicas.citasmedicas.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import equipocitasmedicas.citasmedicas.R
import java.util.Calendar

class RegistroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etEspecialidad: EditText
    private lateinit var etDireccionConsultorio: EditText

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
        val btnGuardar = findViewById<Button>(R.id.btn_registrarse)
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_login)

        // Adapter para dropdown
        etRol.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.roles)
            )
        )
        etGenero.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1,
                resources.getStringArray(R.array.generos)
            )
        )

        etRol.setOnItemClickListener { parent, _, position, _ ->
            val selectedRole = parent.getItemAtPosition(position).toString()
            toggleMedicoFields(selectedRole)
        }

        etFecha.setOnClickListener {
            val calendario = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, y, m, d ->
                    etFecha.setText(String.format("%04d-%02d-%02d", y, m + 1, d))
                },
                calendario.get(Calendar.YEAR),
                calendario.get(Calendar.MONTH),
                calendario.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }

        // Ir a login
        tvGoLogin.setOnClickListener {
            auth.signOut()
            startActivity(LoginActivity.intent(this).apply { putExtra("forceLogin", true) })
            finish()
        }

        // Botón registrar
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

            val isMedico = rol.equals("Médico", ignoreCase = true)

            // Validaciones
            if (nombre.isEmpty() || correo.isEmpty() || fecha.isEmpty() ||
                tel.isEmpty() || pass.isEmpty() || pass2.isEmpty() || rol.isEmpty() || genero.isEmpty()
            ) {
                toast("Completa todos los campos.")
                return@setOnClickListener
            }

            if (isMedico && (especialidad.isEmpty() || direccionConsultorio.isEmpty())) {
                toast("Completa especialidad y dirección del consultorio.")
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                toast("Correo inválido.")
                return@setOnClickListener
            }

            if (pass != pass2) {
                toast("Las contraseñas no coinciden.")
                return@setOnClickListener
            }

            if (!fecha.matches(Regex("""\d{4}-\d{2}-\d{2}"""))) {
                toast("Fecha debe ser YYYY-MM-DD.")
                return@setOnClickListener
            }

            // Crear usuario en FirebaseAuth
            auth.createUserWithEmailAndPassword(correo, pass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid == null) {
                            toast("No se pudo obtener UID.")
                            return@addOnCompleteListener
                        }

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
                            )

                            db.collection("medicos")
                                .document(uid)
                                .set(medico)
                                .addOnSuccessListener {
                                    toast("Médico registrado correctamente.")
                                    finishRegistration()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreError", "Error guardando médico", e)
                                    toast("Error al guardar datos: ${e.message}")
                                }

                        } else { // Paciente
                            val paciente = hashMapOf(
                                "id" to uid,
                                "nombreCompleto" to nombre,
                                "correo" to correo,
                                "fechaNacimiento" to fecha,
                                "telefono" to tel,
                                "rol" to rol,
                                "genero" to genero
                            )

                            db.collection("pacientes")
                                .document(uid)
                                .set(paciente)
                                .addOnSuccessListener {
                                    toast("Paciente registrado correctamente.")
                                    finishRegistration()
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreError", "Error guardando paciente", e)
                                    toast("Error al guardar datos: ${e.message}")
                                }
                        }

                    } else {
                        toast(task.exception?.localizedMessage ?: "El registro falló.")
                    }
                }
        }
    }

    private fun toggleMedicoFields(role: String) {
        if (role.equals("Médico", ignoreCase = true)) {
            etEspecialidad.visibility = View.VISIBLE
            etDireccionConsultorio.visibility = View.VISIBLE
        } else {
            etEspecialidad.visibility = View.GONE
            etDireccionConsultorio.visibility = View.GONE
            etEspecialidad.text.clear()
            etDireccionConsultorio.text.clear()
        }
    }

    private fun finishRegistration() {
        auth.signOut()
        startActivity(LoginActivity.intent(this).apply { putExtra("forceLogin", true) })
        finish()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}