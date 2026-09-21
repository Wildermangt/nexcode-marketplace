package com.nexcode.marketplace.ui.principal

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.nexcode.marketplace.R
import com.nexcode.marketplace.databinding.ActivityPrincipalBinding
import com.nexcode.marketplace.ui.carrito.CarritoFragment
import com.nexcode.marketplace.ui.catalogo.CatalogoFragment
import com.nexcode.marketplace.ui.chat.ChatFragment
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.inicio.InicioFragment
import com.nexcode.marketplace.ui.login.LoginActivity
import com.nexcode.marketplace.ui.perfil.PerfilFragment
import kotlinx.coroutines.launch

/**
 * Contenedor de la aplicacion.
 *
 * Mantiene las cinco secciones (inicio, catalogo, carrito, chat y perfil) y la
 * insignia del carrito. Tambien es la pantalla que dispara la carga inicial del
 * catalogo cuando entra un administrador y Firestore todavia esta vacio.
 */
class PrincipalActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityPrincipalBinding

    /** Se conservan las instancias para no perder el estado al cambiar de pestana. */
    private val secciones = mutableMapOf<Int, Fragment>()

    private var seccionActual = R.id.nav_inicio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityPrincipalBinding.inflate(layoutInflater)
        setContentView(vistas.root)

        if (app.usuario == null) {
            irALogin()
            return
        }

        // La barra inferior deja sitio para los botones del sistema.
        ViewCompat.setOnApplyWindowInsetsListener(vistas.barraInferior) { vista, ventana ->
            val barras = ventana.getInsets(WindowInsetsCompat.Type.systemBars())
            vista.updatePadding(bottom = barras.bottom)
            ventana
        }

        vistas.barraInferior.setOnItemSelectedListener { elemento ->
            mostrarSeccion(elemento.itemId)
            true
        }

        seccionActual = savedInstanceState?.getInt(CLAVE_SECCION) ?: R.id.nav_inicio
        vistas.barraInferior.selectedItemId = seccionActual

        sembrarSiHaceFalta()
    }

    override fun onResume() {
        super.onResume()
        actualizarInsignia()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CLAVE_SECCION, seccionActual)
    }

    /** Permite que una seccion pida abrir otra, por ejemplo "ir al catalogo". */
    fun abrirSeccion(id: Int) {
        vistas.barraInferior.selectedItemId = id
    }

    /** Refresca el numero de unidades que se ve sobre el icono del carrito. */
    fun actualizarInsignia() {
        val unidades = app.repositorioCarrito.unidades()
        val insignia = vistas.barraInferior.getOrCreateBadge(R.id.nav_carrito)
        insignia.isVisible = unidades > 0
        insignia.number = unidades
        insignia.backgroundColor = getColor(R.color.nex_verde_oscuro)
        insignia.badgeTextColor = getColor(R.color.nex_texto_sobre_marca)
    }

    private fun mostrarSeccion(id: Int) {
        val fragmento = secciones.getOrPut(id) {
            when (id) {
                R.id.nav_catalogo -> CatalogoFragment()
                R.id.nav_carrito -> CarritoFragment()
                R.id.nav_chat -> ChatFragment()
                R.id.nav_perfil -> PerfilFragment()
                else -> InicioFragment()
            }
        }
        seccionActual = id
        supportFragmentManager.commit {
            setCustomAnimations(R.anim.fragmento_entra, R.anim.fragmento_sale)
            replace(vistas.contenedor.id, fragmento)
        }
    }

    /**
     * Carga inicial del catalogo mayorista.
     *
     * Solo la ejecuta una cuenta de administrador y solo si la coleccion de
     * productos esta vacia; con eso la demostracion arranca con las veinte
     * referencias y sus fotografias ya en Firebase.
     */
    private fun sembrarSiHaceFalta() {
        val vendedor = app.usuario ?: return
        if (!vendedor.esAdministrador) return

        lifecycleScope.launch {
            try {
                if (!app.sembrador.catalogoVacio()) return@launch
                vistas.root.avisar(getString(R.string.sembrando))
                val resultado = app.sembrador.sembrar(vendedor)
                if (resultado.sembrados > 0) {
                    vistas.root.avisar(
                        getString(R.string.catalogo_sembrado, resultado.sembrados)
                    )
                }
            } catch (e: Exception) {
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    fun irALogin() {
        startActivity(
            Intent(this, LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()
    }

    private companion object {
        const val CLAVE_SECCION = "seccion"
    }
}
