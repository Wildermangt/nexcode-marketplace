package com.nexcode.marketplace.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.R
import com.nexcode.marketplace.databinding.FragmentChatBinding
import com.nexcode.marketplace.dominio.Mensaje
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import kotlinx.coroutines.launch

/**
 * Pestana de chat.
 *
 * La misma pestana sirve a los dos papeles: el comprador ve su conversacion
 * con la tienda y el administrador ve la bandeja con todas las conversaciones,
 * de la que abre cada una en [ChatActivity].
 */
class ChatFragment : Fragment() {

    private var _vistas: FragmentChatBinding? = null
    private val vistas get() = _vistas!!

    private var mensajes: AdaptadorMensajes? = null
    private var conversaciones: AdaptadorConversaciones? = null
    private var escucha: ListenerRegistration? = null

    private val esVendedor get() = app.esAdministrador

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estado: Bundle?
    ): View {
        _vistas = FragmentChatBinding.inflate(inflador, contenedor, false)
        return vistas.root
    }

    override fun onViewCreated(vista: View, estado: Bundle?) {
        super.onViewCreated(vista, estado)
        vistas.encabezado.ajustarASistema()

        if (esVendedor) prepararBandeja() else prepararConversacion()
    }

    override fun onStart() {
        super.onStart()
        val usuario = app.usuario ?: return
        escucha = if (esVendedor) {
            app.repositorioChat.escucharConversaciones { lista ->
                if (_vistas == null) return@escucharConversaciones
                conversaciones?.submitList(lista)
                vistas.vacioBandeja.root.visibility =
                    if (lista.isEmpty()) View.VISIBLE else View.GONE
            }
        } else {
            app.repositorioChat.escucharMensajes(usuario.uid) { lista -> pintar(lista) }
        }
    }

    override fun onStop() {
        super.onStop()
        escucha?.remove()
        escucha = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vistas = null
    }

    // -----------------------------------------------------------------------
    //  Comprador
    // -----------------------------------------------------------------------

    private fun prepararConversacion() {
        val usuario = app.usuario ?: return
        vistas.grupoConversacion.visibility = View.VISIBLE
        vistas.grupoBandeja.visibility = View.GONE
        vistas.titulo.text = getString(R.string.chat_titulo)
        vistas.subtitulo.text = getString(R.string.marca)

        mensajes = AdaptadorMensajes(usuario.uid)
        vistas.listaMensajes.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        vistas.listaMensajes.adapter = mensajes

        vistas.vacioConversacion.iconoVacio.setImageResource(R.drawable.ic_chat)
        vistas.vacioConversacion.tituloVacio.text = getString(R.string.chat_vacio)
        vistas.vacioConversacion.detalleVacio.text = getString(R.string.lema)

        vistas.enviar.setOnClickListener { enviar() }
        pintar(app.repositorioChat.mensajesLocales(usuario.uid))
    }

    private fun pintar(lista: List<Mensaje>) {
        if (_vistas == null) return
        mensajes?.submitList(lista) {
            if (lista.isNotEmpty()) {
                vistas.listaMensajes.scrollToPosition(lista.size - 1)
            }
        }
        vistas.vacioConversacion.root.visibility =
            if (lista.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun enviar() {
        val usuario = app.usuario ?: return
        val texto = vistas.entradaMensaje.text.toString()
        if (texto.isBlank()) return

        vistas.entradaMensaje.setText("")
        lifecycleScope.launch {
            try {
                app.repositorioChat.enviar(
                    chatId = usuario.uid,
                    autor = usuario,
                    compradorNombre = usuario.nombre,
                    texto = texto
                )
            } catch (e: Exception) {
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    // -----------------------------------------------------------------------
    //  Vendedor
    // -----------------------------------------------------------------------

    private fun prepararBandeja() {
        vistas.grupoConversacion.visibility = View.GONE
        vistas.grupoBandeja.visibility = View.VISIBLE
        vistas.titulo.text = getString(R.string.chat_admin_titulo)
        vistas.subtitulo.text = getString(R.string.panel_vendedor)

        conversaciones = AdaptadorConversaciones { conversacion ->
            ChatActivity.abrirComoVendedor(
                requireContext(), conversacion.id, conversacion.compradorNombre
            )
        }
        vistas.listaConversaciones.layoutManager = LinearLayoutManager(requireContext())
        vistas.listaConversaciones.adapter = conversaciones

        vistas.vacioBandeja.iconoVacio.setImageResource(R.drawable.ic_chat)
        vistas.vacioBandeja.tituloVacio.text = getString(R.string.sin_chats)
        vistas.vacioBandeja.detalleVacio.text = getString(R.string.lema)
    }
}
