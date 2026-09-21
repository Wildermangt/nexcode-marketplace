package com.nexcode.marketplace.datos

import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.nexcode.marketplace.datos.local.AlmacenLocal
import com.nexcode.marketplace.datos.remoto.ServicioFirebase
import com.nexcode.marketplace.dominio.Conversacion
import com.nexcode.marketplace.dominio.Mensaje
import com.nexcode.marketplace.dominio.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Chat entre el comprador y el vendedor.
 *
 * Cada comprador tiene una unica conversacion con la tienda, asi que el
 * identificador del chat es su propio UID. La estructura en Firestore es:
 *
 * <pre>
 * chats/{uidComprador}                 -> cabecera con el ultimo mensaje
 *   mensajes/{idMensaje}               -> texto, autor y fecha
 * </pre>
 *
 * Los mensajes tambien se copian a la base SQL local para que la conversacion
 * se pueda abrir sin conexion.
 */
class RepositorioChat(private val local: AlmacenLocal) {

    private val base get() = ServicioFirebase.base
    private val chats get() = base.collection(ServicioFirebase.CHATS)

    fun mensajesLocales(chatId: String): List<Mensaje> = local.mensajes(chatId)

    /** Escucha en tiempo real los mensajes de una conversacion. */
    fun escucharMensajes(
        chatId: String,
        alCambiar: (List<Mensaje>) -> Unit
    ): ListenerRegistration =
        chats.document(chatId)
            .collection(ServicioFirebase.MENSAJES)
            .orderBy("fecha", Query.Direction.ASCENDING)
            .addSnapshotListener { instantanea, error ->
                if (error != null) return@addSnapshotListener
                val mensajes = instantanea?.documents.orEmpty().map { documento ->
                    Mensaje(
                        id = documento.id,
                        autorId = documento.getString("autorId").orEmpty(),
                        autorNombre = documento.getString("autorNombre").orEmpty(),
                        texto = documento.getString("texto").orEmpty(),
                        fecha = documento.getLong("fecha") ?: 0L,
                        esDelVendedor = documento.getBoolean("esDelVendedor") ?: false
                    )
                }
                local.reemplazarMensajes(chatId, mensajes)
                alCambiar(mensajes)
            }

    /** Lista de conversaciones que ve el panel del vendedor. */
    fun escucharConversaciones(alCambiar: (List<Conversacion>) -> Unit): ListenerRegistration =
        chats.addSnapshotListener { instantanea, error ->
            if (error != null) return@addSnapshotListener
            val conversaciones = instantanea?.documents.orEmpty().map { documento ->
                Conversacion(
                    id = documento.id,
                    compradorId = documento.getString("compradorId").orEmpty(),
                    compradorNombre = documento.getString("compradorNombre").orEmpty(),
                    ultimoMensaje = documento.getString("ultimoMensaje").orEmpty(),
                    fecha = documento.getLong("fecha") ?: 0L,
                    sinLeerVendedor = (documento.getLong("sinLeerVendedor") ?: 0L).toInt()
                )
            }.sortedByDescending { it.fecha }
            alCambiar(conversaciones)
        }

    /**
     * Envia un mensaje y actualiza la cabecera de la conversacion, que es lo
     * que ordena la lista del vendedor.
     *
     * @param chatId conversacion destino: siempre el UID del comprador
     */
    suspend fun enviar(
        chatId: String,
        autor: Usuario,
        compradorNombre: String,
        texto: String
    ) = withContext(Dispatchers.IO) {
        val limpio = texto.trim()
        if (limpio.isEmpty()) return@withContext

        val ahora = System.currentTimeMillis()
        val esVendedor = autor.esAdministrador

        chats.document(chatId).set(
            mapOf(
                "compradorId" to chatId,
                "compradorNombre" to compradorNombre,
                "ultimoMensaje" to limpio,
                "fecha" to ahora,
                "sinLeerVendedor" to if (esVendedor) 0 else 1
            )
        ).await()

        chats.document(chatId).collection(ServicioFirebase.MENSAJES).add(
            mapOf(
                "autorId" to autor.uid,
                "autorNombre" to autor.nombre,
                "texto" to limpio,
                "fecha" to ahora,
                "esDelVendedor" to esVendedor
            )
        ).await()
    }

    /** El vendedor marca la conversacion como atendida. */
    suspend fun marcarLeido(chatId: String) = withContext(Dispatchers.IO) {
        runCatching { chats.document(chatId).update("sinLeerVendedor", 0).await() }
        Unit
    }
}
