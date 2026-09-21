package com.nexcode.marketplace.datos

import android.content.Context
import android.util.Log
import com.nexcode.marketplace.datos.remoto.ServicioFirebase
import com.nexcode.marketplace.dominio.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Carga inicial del catalogo.
 *
 * La primera vez que entra un administrador y la coleccion {@code productos}
 * esta vacia, se suben las veinte referencias mayoristas de
 * [CatalogoMayorista]: por cada una se lee la fotografia incluida en
 * {@code assets/productos}, se sube a Firebase Storage y se guarda el documento
 * en Firestore con la URL resultante.
 *
 * Si Storage todavia no esta habilitado en el proyecto, el producto se guarda
 * igual y su imagen queda apuntando a los recursos de la aplicacion. El
 * catalogo sigue viendose completo y las fotos se pueden migrar despues.
 */
class SembradorCatalogo(
    private val contexto: Context,
    private val productos: RepositorioProductos
) {

    /** Resultado de la siembra, para poder informarlo en pantalla. */
    data class Resultado(
        val sembrados: Int,
        val fotosEnStorage: Int,
        val yaExistia: Boolean = false
    )

    /** @return true si el catalogo remoto todavia no tiene productos */
    suspend fun catalogoVacio(): Boolean = withContext(Dispatchers.IO) {
        try {
            ServicioFirebase.base.collection(ServicioFirebase.PRODUCTOS)
                .limit(1).get().await().isEmpty
        } catch (e: Exception) {
            Log.w(ETIQUETA, "No se pudo consultar el catalogo", e)
            false
        }
    }

    /**
     * Siembra las veinte referencias.
     *
     * @param vendedor cuenta de administrador que quedara como vendedor
     * @param alAvanzar informa cuantas referencias van cargadas, para la barra
     *                  de progreso del panel
     */
    suspend fun sembrar(
        vendedor: Usuario,
        alAvanzar: (Int, Int) -> Unit = { _, _ -> }
    ): Resultado = withContext(Dispatchers.IO) {
        if (!catalogoVacio()) {
            return@withContext Resultado(0, 0, yaExistia = true)
        }

        val coleccion = ServicioFirebase.base.collection(ServicioFirebase.PRODUCTOS)
        val total = CatalogoMayorista.SEMILLAS.size
        val momento = System.currentTimeMillis()
        var enStorage = 0

        CatalogoMayorista.SEMILLAS.forEachIndexed { indice, semilla ->
            val documento = coleccion.document()
            val producto = semilla.aProducto(vendedor.uid, vendedor.nombre, momento - indice)

            // La foto viaja en la aplicacion; se sube a Storage para demostrar
            // el almacenamiento remoto que pide la guia.
            val bytes = leerAsset(semilla.archivoFoto)
            val url = if (bytes != null) {
                productos.subirFotografia(documento.id, bytes)
            } else {
                null
            }
            if (url != null) enStorage++

            val definitivo = producto.copy(
                id = documento.id,
                imagen = url ?: producto.imagen
            )
            documento.set(definitivo.aMapaSemilla()).await()
            alAvanzar(indice + 1, total)
        }

        Resultado(total, enStorage)
    }

    /**
     * Sube a Storage las fotografias que todavia viven dentro de la aplicacion.
     *
     * Se usa cuando el catalogo se sembro antes de habilitar Firebase Storage:
     * recorre los productos cuya imagen empieza por {@code asset://}, sube el
     * archivo correspondiente y reemplaza la ruta por la URL de descarga.
     *
     * @return cuantas fotografias quedaron en Storage
     */
    suspend fun migrarFotosAStorage(
        alAvanzar: (Int, Int) -> Unit = { _, _ -> }
    ): Int = withContext(Dispatchers.IO) {
        val coleccion = ServicioFirebase.base.collection(ServicioFirebase.PRODUCTOS)
        val pendientes = coleccion.get().await().documents.filter {
            it.getString("imagen").orEmpty().startsWith(RUTA_ASSETS)
        }
        var subidas = 0
        pendientes.forEachIndexed { indice, documento ->
            val archivo = documento.getString("imagen").orEmpty().substringAfterLast('/')
            val bytes = leerAsset(archivo)
            if (bytes != null) {
                val url = productos.subirFotografia(documento.id, bytes)
                if (url != null) {
                    documento.reference.update("imagen", url).await()
                    subidas++
                }
            }
            alAvanzar(indice + 1, pendientes.size)
        }
        subidas
    }

    /** Cuantas fotografias siguen sirviendose desde los recursos de la aplicacion. */
    suspend fun fotosPendientes(): Int = withContext(Dispatchers.IO) {
        try {
            ServicioFirebase.base.collection(ServicioFirebase.PRODUCTOS)
                .get().await().documents.count {
                    it.getString("imagen").orEmpty().startsWith(RUTA_ASSETS)
                }
        } catch (e: Exception) {
            0
        }
    }

    private fun leerAsset(archivo: String): ByteArray? = try {
        contexto.assets.open("productos/$archivo").use { it.readBytes() }
    } catch (e: Exception) {
        Log.w(ETIQUETA, "No se encontro la fotografia $archivo", e)
        null
    }

    private fun com.nexcode.marketplace.dominio.Producto.aMapaSemilla() = mapOf(
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

    private companion object {
        const val ETIQUETA = "SembradorCatalogo"
    }
}
