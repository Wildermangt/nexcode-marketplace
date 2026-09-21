package com.nexcode.marketplace.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputLayout
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.databinding.ActivityLoginBinding
import com.nexcode.marketplace.dominio.TipoUsuario
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.comun.mostrar
import com.nexcode.marketplace.ui.comun.ocultarTeclado
import com.nexcode.marketplace.ui.principal.PrincipalActivity
import kotlinx.coroutines.launch

/**
 * Registro e inicio de sesion.
 *
 * Una sola pantalla con dos modos. El interruptor "quiero vender" es lo que
 * decide si la cuenta nace como comprador o como administrador, que es la
 * distincion de la que dependen el panel del vendedor y las reglas de
 * seguridad de Firestore.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityLoginBinding

    private var modoRegistro = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        vistas.grupoModo.addOnButtonCheckedListener { _, boton, marcado ->
            if (marcado) aplicarModo(boton == R.id.modoRegistrar)
        }
        vistas.botonPrincipal.setOnClickListener { enviar() }

        Animaciones.aparecerConRebote(vistas.simbolo, 60)
        Animaciones.entradaEscalonada(240, vistas.titulo, vistas.subtitulo, vistas.tarjeta)
        aplicarModo(false)
    }

    private fun aplicarModo(registro: Boolean) {
        modoRegistro = registro
        vistas.campoNombre.mostrar(registro)
        vistas.campoConfirmar.mostrar(registro)
        vistas.interruptorVendedor.mostrar(registro)
        vistas.titulo.text = getString(
            if (registro) R.string.crear_cuenta else R.string.bienvenido
        )
        vistas.subtitulo.text = getString(
            if (registro) R.string.registro_detalle else R.string.bienvenido_detalle
        )
        vistas.botonPrincipal.text = getString(
            if (registro) R.string.crear_cuenta else R.string.entrar
        )
        limpiarErrores()
    }

    private fun enviar() {
        ocultarTeclado()
        if (!validar()) return

        val correo = vistas.entradaCorreo.text.toString().trim()
        val contrasena = vistas.entradaContrasena.text.toString()

        ocupado(true)
        lifecycleScope.launch {
            try {
                val usuarios = app.repositorioUsuarios
                val usuario = if (modoRegistro) {
                    usuarios.registrar(
                        nombre = vistas.entradaNombre.text.toString(),
                        correo = correo,
                        contrasena = contrasena,
                        tipo = if (vistas.interruptorVendedor.isChecked) {
                            TipoUsuario.ADMINISTRADOR
                        } else {
                            TipoUsuario.COMPRADOR
                        }
                    )
                } else {
                    usuarios.entrar(correo, contrasena)
                }
                app.usuario = usuario
                startActivity(Intent(this@LoginActivity, PrincipalActivity::class.java))
                finish()
            } catch (e: Exception) {
                ocupado(false)
                Animaciones.sacudir(vistas.tarjeta)
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Validacion
    // -----------------------------------------------------------------------

    private fun validar(): Boolean {
        limpiarErrores()
        var valido = true

        if (modoRegistro && vistas.entradaNombre.text.isNullOrBlank()) {
            valido = marcar(vistas.campoNombre, getString(R.string.campo_obligatorio))
        }

        val correo = vistas.entradaCorreo.text.toString().trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            valido = marcar(vistas.campoCorreo, getString(R.string.correo_invalido))
        }

        val contrasena = vistas.entradaContrasena.text.toString()
        if (contrasena.length < 6) {
            valido = marcar(vistas.campoContrasena, getString(R.string.contrasena_corta))
        }

        if (modoRegistro && vistas.entradaConfirmar.text.toString() != contrasena) {
            valido = marcar(vistas.campoConfirmar, getString(R.string.contrasenas_distintas))
        }

        if (!valido) Animaciones.sacudir(vistas.tarjeta)
        return valido
    }

    /** Marca el campo con el error y devuelve false, para encadenar validaciones. */
    private fun marcar(campo: TextInputLayout, mensaje: String): Boolean {
        campo.error = mensaje
        return false
    }

    private fun limpiarErrores() {
        listOf(
            vistas.campoNombre, vistas.campoCorreo,
            vistas.campoContrasena, vistas.campoConfirmar
        ).forEach { it.error = null }
    }

    private fun ocupado(activo: Boolean) {
        vistas.progreso.visibility = if (activo) View.VISIBLE else View.GONE
        vistas.botonPrincipal.isEnabled = !activo
        vistas.grupoModo.isEnabled = !activo
    }
}
