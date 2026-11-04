package com.nexcode.marketplace.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.BuildConfig
import com.nexcode.marketplace.R
import com.nexcode.marketplace.datos.remoto.LlamadaConLeandro
import com.nexcode.marketplace.databinding.FragmentChatBinding
import com.nexcode.marketplace.dominio.Mensaje
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        // Al salir de la pantalla se cuelga. Se usa un alcance propio porque el
        // del fragmento se cancela ahora mismo y no dejaria terminar el colgado.
        llamada?.let { activa -> CoroutineScope(Dispatchers.Main).launch { activa.colgar() } }
        llamada = null
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
        prepararLlamadaLeandro()
    }

    // -----------------------------------------------------------------------
    //  Llamada de voz con Leandro (Retell AI)
    // -----------------------------------------------------------------------

    private var llamada: LlamadaConLeandro? = null

    /** Se registra al crear el fragmento, como exige la API de permisos. */
    private val permisoMicrofono =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
            if (concedido) {
                lifecycleScope.launch { llamada?.iniciar() }
            } else {
                _vistas?.root?.avisar(getString(R.string.leandro_sin_microfono))
            }
        }

    private fun prepararLlamadaLeandro() {
        val nueva = LlamadaConLeandro(requireContext(), BuildConfig.LEANDRO_URL)
        llamada = nueva

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                nueva.estado.collect { estado ->
                    vistas.llamarLeandro.isEnabled = estado != LlamadaConLeandro.Estado.Conectando
                    vistas.llamarLeandro.setText(
                        when (estado) {
                            LlamadaConLeandro.Estado.EnCurso -> R.string.leandro_colgar
                            LlamadaConLeandro.Estado.Conectando -> R.string.leandro_conectando
                            else -> R.string.leandro_hablar
                        }
                    )
                    val motivo = (estado as? LlamadaConLeandro.Estado.Fallo)?.motivo
                    vistas.estadoLeandro.visibility = if (motivo == null) View.GONE else View.VISIBLE
                    vistas.estadoLeandro.text = motivo
                }
            }
        }

        vistas.llamarLeandro.setOnClickListener {
            when {
                nueva.estado.value == LlamadaConLeandro.Estado.EnCurso ->
                    lifecycleScope.launch { nueva.colgar() }
                ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED ->
                    lifecycleScope.launch { nueva.iniciar() }
                else -> permisoMicrofono.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
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
