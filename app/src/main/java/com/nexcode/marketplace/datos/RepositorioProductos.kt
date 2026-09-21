package com.nexcode.marketplace.datos

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.datos.local.AlmacenLocal
import com.nexcode.marketplace.datos.remoto.ServicioFirebase
import com.nexcode.marketplace.dominio.Calificacion
import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.Producto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Catalogo de productos.
 *
 * Firestore es la fuente de verdad y notifica cada cambio en tiempo real; cada
 * vez que llega una version nueva del catalogo se vuelca en la base SQL local,
 * que es la que responde cuando el telefono no tiene red.
 *
 * Las fotografias van a Firebase Storage y en el documento solo queda su URL
 * de descarga. Si Storage no esta disponible, la imagen se conserva como
 * referencia a los recursos incluidos en la aplicacion ({@code asset://...}),
 * de modo que el catalogo nunca queda sin ilustrar.
 */
class RepositorioProductos(private val local: AlmacenLocal) {

    private val base get() = ServicioFirebase.base
    private val coleccion get() = base.collection(ServicioFirebase.PRODUCTOS)

    /** Ultimo catalogo guardado en el telefono. Sirve para pintar de inmediato. */
    fun catalogoLocal(): List<Producto> = local.productos()

    fun productoLocal(id: String): Producto? = local.producto(id)

    /**
     * Escucha el catalogo en tiempo real.
     *
     * @param alCambiar recibe la lista completa cada vez que Firestore avisa
     * @param alFallar se invoca si la escucha se interrumpe
     */
    fun escucharCatalogo(
        alCambiar: (List<Producto>) -> Unit,
        alFallar: (Exception) -> Unit = {}
    ): ListenerRegistration =
        coleccion.addSnapshotListener { instantanea, error ->
            if (error != null) {
                Log.w(ETIQUETA, "No se pudo escuchar el catalogo", error)
                alFallar(error)
                return@addSnapshotListener
            }
            val productos = instantanea?.documents.orEmpty()
                .mapNotNull { it.aProducto() }
                .sortedByDescending { it.fechaRegistro }
            local.reemplazarProductos(productos)
            alCambiar(productos)
        }

    suspend fun obtener(id: String): Producto? = withContext(Dispatchers.IO) {
        try {
            coleccion.document(id).get().await().aProducto()
        } catch (e: Exception) {
            local.producto(id)
        }
    }

    /**
     * Crea o actualiza un producto. Si {@code fotoNueva} trae bytes, primero se
     * sube a Storage y se guarda la URL resultante.
     *
     * @return el identificador del documento
     */
    suspend fun guardar(producto: Producto, fotoNueva: ByteArray?): String =
        withContext(Dispatchers.IO) {
            val documento = if (producto.id.isBlank()) {
                coleccion.document()
            } else {
                coleccion.document(producto.id)
            }

            val imagen = if (fotoNueva != null) {
                subirFotografia(documento.id, fotoNueva) ?: producto.imagen
            } else {
                producto.imagen
            }

            val completo = producto.copy(
                id = documento.id,
                imagen = imagen,
                fechaRegistro = if (producto.fechaRegistro > 0L) {
                    producto.fechaRegistro
                } else {
                    System.currentTimeMillis()
                }
            )
            documento.set(completo.aMapa()).await()
            documento.id
        }

    suspend fun eliminar(producto: Producto) = withContext(Dispatchers.IO) {
        coleccion.document(producto.id).delete().await()
        // La foto solo se borra si vive en Storage; las de los recursos no.
        if (producto.imagen.startsWith("https://")) {
            runCatching {
                ServicioFirebase.almacenamiento
                    .getReference("${ServicioFirebase.CARPETA_FOTOS}/${producto.id}.jpg")
                    .delete().await()
            }
        }
        Unit
    }

    /**
     * Sube una fotografia y devuelve su URL de descarga, o {@code null} si
     * Storage no esta disponible (por ejemplo, si aun no se habilito en la
     * consola de Firebase). Devolver null permite seguir guardando el producto.
     */
    suspend fun subirFotografia(productoId: String, bytes: ByteArray): String? =
        withContext(Dispatchers.IO) {
            try {
                val referencia = ServicioFirebase.almacenamiento
                    .getReference("${ServicioFirebase.CARPETA_FOTOS}/$productoId.jpg")
                referencia.putBytes(bytes).await()
                referencia.downloadUrl.await().toString()
            } catch (e: Exception) {
                Log.w(ETIQUETA, "No se pudo subir la fotografia de $productoId", e)
                null
            }
        }

    // -----------------------------------------------------------------------
    //  Calificaciones
    // -----------------------------------------------------------------------

    /**
     * Guarda la opinion del comprador y recalcula el promedio del producto.
     *
     * El documento de la calificacion se identifica con el UID, de modo que
     * una misma persona actualiza su voto en lugar de sumar otro.
     */
    suspend fun calificar(productoId: String, calificacion: Calificacion) =
        withContext(Dispatchers.IO) {
            val opiniones = coleccion.document(productoId)
                .collection(ServicioFirebase.CALIFICACIONES)

            opiniones.document(calificacion.usuarioId).set(
                mapOf(
                    "usuarioNombre" to calificacion.usuarioNombre,
                    "estrellas" to calificacion.estrellas,
                    "comentario" to calificacion.comentario,
                    "fecha" to calificacion.fecha
                )
            ).await()

            val todas = opiniones.get().await().documents
            val votos = todas.size
            val promedio = if (votos == 0) {
                0.0
            } else {
                todas.sumOf { (it.getLong("estrellas") ?: 0L).toDouble() } / votos
            }
            coleccion.document(productoId).update(
                mapOf("calificacion" to promedio, "votos" to votos)
            ).await()
        }

    suspend fun opiniones(productoId: String): List<Calificacion> =
        withContext(Dispatchers.IO) {
            try {
                coleccion.document(productoId)
                    .collection(ServicioFirebase.CALIFICACIONES)
                    .get().await().documents.map { documento ->
                        Calificacion(
                            usuarioId = documento.id,
                            usuarioNombre = documento.getString("usuarioNombre").orEmpty(),
                            estrellas = (documento.getLong("estrellas") ?: 0L).toInt(),
                            comentario = documento.getString("comentario").orEmpty(),
                            fecha = documento.getLong("fecha") ?: 0L
                        )
                    }.sortedByDescending { it.fecha }
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun miOpinion(productoId: String, usuarioId: String): Calificacion? =
        opiniones(productoId).firstOrNull { it.usuarioId == usuarioId }

    /** Descuenta existencias y suma unidades vendidas al confirmar un pedido. */
    suspend fun descontarExistencias(productoId: String, unidades: Int) =
        withContext(Dispatchers.IO) {
            runCatching {
                base.runTransaction { transaccion ->
                    val referencia = coleccion.document(productoId)
                    val actual = transaccion.get(referencia)
                    val disponibles = (actual.getLong("cantidad") ?: 0L).toInt()
                    val vendidos = (actual.getLong("vendidos") ?: 0L).toInt()
                    transaccion.update(
                        referencia,
                        mapOf(
                            "cantidad" to maxOf(0, disponibles - unidades),
                            "vendidos" to vendidos + unidades
                        )
                    )
                    null
                }.await()
            }
            Unit
        }

    // -----------------------------------------------------------------------
    //  Conversion
    // -----------------------------------------------------------------------

    private fun Producto.aMapa() = mapOf(
        "nombre" to nombre,
        "descripcion" to descripcion,
        "precio" to precio,
        "categoria" to categoria,
        "estado" to estado.clave,
        "cantidad" to cantidad,
        "imagen" to imagen,
        "marca" to marca,
        "referencia" to referencia,
        "vendedorId" to vendedorId,
        "vendedorNombre" to vendedorNombre,
        "calificacion" to calificacion,
        "votos" to votos,
        "vendidos" to vendidos,
        "fechaRegistro" to fechaRegistro
    )

    private fun DocumentSnapshot.aProducto(): Producto? {
        if (!exists()) return null
        return Producto(
            id = id,
            nombre = getString("nombre").orEmpty(),
            descripcion = getString("descripcion").orEmpty(),
            precio = getDouble("precio") ?: 0.0,
            categoria = getString("categoria").orEmpty(),
            estado = EstadoProducto.desde(getString("estado")),
            cantidad = (getLong("cantidad") ?: 0L).toInt(),
            imagen = getString("imagen").orEmpty(),
            marca = getString("marca").orEmpty(),
            referencia = getString("referencia").orEmpty(),
            vendedorId = getString("vendedorId").orEmpty(),
            vendedorNombre = getString("vendedorNombre").orEmpty(),
            calificacion = getDouble("calificacion") ?: 0.0,
            votos = (getLong("votos") ?: 0L).toInt(),
            vendidos = (getLong("vendidos") ?: 0L).toInt(),
            fechaRegistro = getLong("fechaRegistro") ?: 0L
        )
    }

    private companion object {
        const val ETIQUETA = "RepositorioProductos"
    }
}
