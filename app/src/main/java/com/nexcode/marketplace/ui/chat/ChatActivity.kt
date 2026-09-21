package com.nexcode.marketplace.ui.chat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.R
import com.nexcode.marketplace.databinding.ActivityChatBinding
import com.nexcode.marketplace.dominio.Mensaje
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import kotlinx.coroutines.launch

/**
 * Conversacion a pantalla completa.
 *
 * La abre el vendedor desde su bandeja y el comprador desde la ficha de un
 * producto, en cuyo caso el mensaje llega ya redactado con el nombre del
 * articulo por el que pregunta.
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityChatBinding
    private lateinit var adaptador: AdaptadorMensajes
    private var escucha: ListenerRegistration? = null

    /** Identificador de la conversacion: siempre el UID del comprador. */
    private lateinit var chatId: String

    private lateinit var nombreComprador: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityChatBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        val usuario = app.usuario
        if (usuario == null) {
            finish()
            return
        }

        chatId = intent.getStringExtra(EXTRA_CHAT) ?: usuario.uid
        nombreComprador = intent.getStringExtra(EXTRA_NOMBRE)
            ?: if (usuario.esAdministrador) "Comprador" else usuario.nombre

        vistas.atras.setOnClickListener { finish() }
        vistas.enviar.setOnClickListener { enviar() }

        if (usuario.esAdministrador) {
            vistas.titulo.text = nombreComprador
            vistas.subtitulo.text = getString(R.string.tipo_comprador)
            vistas.inicial.text = nombreComprador.trim().take(1).uppercase()
            lifecycleScope.launch { app.repositorioChat.marcarLeido(chatId) }
        } else {
            vistas.titulo.text = getString(R.string.chat_titulo)
            vistas.subtitulo.text = getString(R.string.marca)
            vistas.inicial.text = "N"
        }

        adaptador = AdaptadorMensajes(usuario.uid)
        vistas.listaMensajes.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        vistas.listaMensajes.adapter = adaptador

        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_chat)
        vistas.vacio.tituloVacio.text = getString(R.string.chat_vacio)
        vistas.vacio.detalleVacio.text = getString(R.string.lema)

        // Consulta sobre un producto concreto: se redacta el mensaje inicial.
        intent.getStringExtra(EXTRA_PRODUCTO)?.let { producto ->
            if (vistas.entradaMensaje.text.isNullOrBlank()) {
                vistas.entradaMensaje.setText(
                    "Hola, quiero informacion sobre: $producto"
                )
            }
        }

        pintar(app.repositorioChat.mensajesLocales(chatId))
    }

    override fun onStart() {
        super.onStart()
        escucha = app.repositorioChat.escucharMensajes(chatId) { pintar(it) }
    }

    override fun onStop() {
        super.onStop()
        escucha?.remove()
        escucha = null
    }

    private fun pintar(lista: List<Mensaje>) {
        adaptador.submitList(lista) {
            if (lista.isNotEmpty()) vistas.listaMensajes.scrollToPosition(lista.size - 1)
        }
        vistas.vacio.root.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun enviar() {
        val usuario = app.usuario ?: return
        val texto = vistas.entradaMensaje.text.toString()
        if (texto.isBlank()) return

        vistas.entradaMensaje.setText("")
        lifecycleScope.launch {
            try {
                app.repositorioChat.enviar(chatId, usuario, nombreComprador, texto)
            } catch (e: Exception) {
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    companion object {
        private const val EXTRA_CHAT = "chatId"
        private const val EXTRA_NOMBRE = "nombreComprador"
        private const val EXTRA_PRODUCTO = "producto"

        /** El comprador escribe a la tienda por un producto concreto. */
        fun abrirComoComprador(contexto: Context, producto: String) {
            contexto.startActivity(
                Intent(contexto, ChatActivity::class.java)
                    .putExtra(EXTRA_PRODUCTO, producto)
            )
        }

        /** El vendedor abre una conversacion de su bandeja. */
        fun abrirComoVendedor(contexto: Context, chatId: String, nombreComprador: String) {
            contexto.startActivity(
                Intent(contexto, ChatActivity::class.java)
                    .putExtra(EXTRA_CHAT, chatId)
                    .putExtra(EXTRA_NOMBRE, nombreComprador)
            )
        }
    }
}
