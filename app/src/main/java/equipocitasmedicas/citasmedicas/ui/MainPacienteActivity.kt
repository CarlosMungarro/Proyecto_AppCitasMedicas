package equipocitasmedicas.citasmedicas.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import equipocitasmedicas.citasmedicas.R

class MainPacienteActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_paciente)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                replace(R.id.fragmentContainer, PacienteHomeFragment())
            }
        }

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottom.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.commit { replace(R.id.fragmentContainer, PacienteHomeFragment()) }
                    true
                }
                R.id.nav_perfil -> {
                    supportFragmentManager.commit { replace(R.id.fragmentContainer, PerfilFragment()) }
                    true
                }
                else -> false
            }
        }
    }
}