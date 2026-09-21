package com.nexcode.marketplace.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.databinding.ActivitySplashBinding
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.login.LoginActivity
import com.nexcode.marketplace.ui.principal.PrincipalActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Pantalla de bienvenida.
 *
 * Mientras se muestra la marca aprovecha para resolver dos cosas: si hay sesion
 * abierta en Firebase Authentication y, en ese caso, cual es la ficha del
 * usuario. Asi la pantalla siguiente ya sabe si debe abrir la tienda o el panel
 * del vendedor.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var vistas: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(vistas.root)

        Animaciones.aparecerConRebote(vistas.simbolo, 80)
        Animaciones.entradaEscalonada(
            420, vistas.marca, vistas.marcaSecundaria, vistas.lema
        )

        lifecycleScope.launch {
            val usuarios = app.repositorioUsuarios
            val perfil = if (usuarios.haySesion) usuarios.perfilActual() else null
            app.usuario = perfil

            // Tiempo minimo en pantalla para que la animacion se alcance a ver.
            delay(1500)

            val destino = if (perfil != null) {
                Intent(this@SplashActivity, PrincipalActivity::class.java)
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java)
            }
            startActivity(destino)
            finish()
            overridePendingTransition(
                com.nexcode.marketplace.R.anim.aparecer,
                com.nexcode.marketplace.R.anim.desaparecer
            )
        }
    }
}
