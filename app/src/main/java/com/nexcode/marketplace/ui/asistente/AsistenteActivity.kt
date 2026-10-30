package com.nexcode.marketplace.ui.asistente

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexcode.marketplace.R
import com.nexcode.marketplace.databinding.ActivityAsistenteBinding
import com.nexcode.marketplace.datos.remoto.AsistenteError
import com.nexcode.marketplace.dominio.Mensaje
import com.nexcode.marketplace.ui.chat.AdaptadorMensajes
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import kotlinx.coroutines.launch

/**
 * Asistente de compras con inteligencia artificial.
 *
 * Reutiliza el {@link AdaptadorMensajes} del chat humano: los mensajes del
 * usuario llevan su propio UID (burbuja a la derecha) y los del asistente un UID
 * fijo (burbuja a la izquierda, con el nombre "Asistente NEXCODE").
 *
 * La conversación se guarda en memoria; el modelo recibe en cada pregunta el
 * catálogo real como contexto, por lo que solo recomienda productos existentes.
 */
class AsistenteActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityAsistenteBinding
    private lateinit var adaptador: AdaptadorMensajes

    /** Conversación visible, en memoria. */
    private val mensajes = mutableListOf<Mensaje>()
    private var enviando = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityAsistenteBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        val usuario = app.usuario
        if (usuario == null) {
            finish()
            return
        }

        vistas.atras.setOnClickListener { finish() }
        vistas.enviar.setOnClickListener { enviar() }

        adaptador = AdaptadorMensajes(usuario.uid)
        vistas.listaMensajes.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        vistas.listaMensajes.adapter = adaptador

        // Saludo inicial del asistente.
        if (mensajes.isEmpty()) {
            agregar(deAsistente(getString(R.string.asistente_saludo)))
        }
    }

    private fun enviar() {
        if (enviando) return
        val usuario = app.usuario ?: return
        val texto = vistas.entradaMensaje.text.toString().trim()
        if (texto.isEmpty()) return

        vistas.entradaMensaje.setText("")
        agregar(
            Mensaje(
                id = nuevoId(),
                autorId = usuario.uid,
                autorNombre = usuario.nombre,
                texto = texto,
                fecha = System.currentTimeMillis()
            )
        )

        if (!app.repositorioAsistente.hayCatalogo()) {
            agregar(deAsistente(getString(R.string.asistente_sin_catalogo)))
            return
        }

        enviando = true
        vistas.cargando.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val respuesta = app.repositorioAsistente.preguntar(texto)
                agregar(deAsistente(respuesta))
            } catch (e: AsistenteError) {
                agregar(deAsistente(e.message ?: getString(R.string.asistente_error_generico)))
            } catch (e: Exception) {
                vistas.root.avisar(getString(R.string.asistente_error_generico))
            } finally {
                enviando = false
                vistas.cargando.visibility = View.GONE
            }
        }
    }

    private fun agregar(mensaje: Mensaje) {
        mensajes.add(mensaje)
        adaptador.submitList(mensajes.toList()) {
            vistas.listaMensajes.scrollToPosition(mensajes.size - 1)
        }
    }

    private fun deAsistente(texto: String) = Mensaje(
        id = nuevoId(),
        autorId = UID_ASISTENTE,
        autorNombre = "Asistente NEXCODE",
        texto = texto,
        fecha = System.currentTimeMillis()
    )

    private fun nuevoId() = "m-" + System.nanoTime()

    companion object {
        /** UID ficticio del asistente: distinto de cualquier usuario real. */
        private const val UID_ASISTENTE = "asistente-ia"

        fun abrir(contexto: Context) {
            contexto.startActivity(Intent(contexto, AsistenteActivity::class.java))
        }
    }
}
