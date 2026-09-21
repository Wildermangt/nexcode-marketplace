package com.nexcode.marketplace.datos

import com.nexcode.marketplace.datos.local.AlmacenLocal
import com.nexcode.marketplace.datos.remoto.ServicioFirebase
import com.nexcode.marketplace.dominio.ItemCarrito
import com.nexcode.marketplace.dominio.LineaPedido
import com.nexcode.marketplace.dominio.Pedido
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.dominio.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Carrito de compras y pedidos.
 *
 * El carrito vive solo en la base SQL local: mientras el comprador decide, no
 * tiene sentido ocupar la nube. Al finalizar la compra, ese carrito se
 * convierte en un documento de la coleccion {@code pedidos} de Firestore, se
 * descuentan las existencias de cada producto y la tabla local queda vacia.
 */
class RepositorioCarrito(
    private val local: AlmacenLocal,
    private val productos: RepositorioProductos
) {

    private val base get() = ServicioFirebase.base

    private val uid: String get() = ServicioFirebase.uidActual.orEmpty()

    fun items(): List<ItemCarrito> = if (uid.isBlank()) emptyList() else local.carrito(uid)

    fun unidades(): Int = if (uid.isBlank()) 0 else local.unidadesEnCarrito(uid)

    fun subtotal(): Double = items().sumOf { it.subtotal }

    /** @return la cantidad que quedo del producto en el carrito, 0 si no hay existencias */
    fun agregar(producto: Producto, unidades: Int = 1): Int =
        if (uid.isBlank()) 0 else local.agregarAlCarrito(uid, producto, unidades)

    fun cambiarCantidad(productoId: String, cantidad: Int) {
        if (uid.isNotBlank()) local.cambiarCantidad(uid, productoId, cantidad)
    }

    fun quitar(productoId: String) {
        if (uid.isNotBlank()) local.quitarDelCarrito(uid, productoId)
    }

    fun vaciar() {
        if (uid.isNotBlank()) local.vaciarCarrito(uid)
    }

    /**
     * Registra el pedido en Firestore, descuenta el inventario y vacia el
     * carrito local.
     *
     * @return el pedido tal como quedo guardado
     * @throws IllegalStateException si el carrito esta vacio
     */
    suspend fun finalizarCompra(comprador: Usuario): Pedido = withContext(Dispatchers.IO) {
        val lineas = items().map {
            LineaPedido(
                productoId = it.productoId,
                nombre = it.nombre,
                precio = it.precio,
                cantidad = it.cantidad
            )
        }
        check(lineas.isNotEmpty()) { "El carrito esta vacio" }

        val documento = base.collection(ServicioFirebase.PEDIDOS).document()
        val pedido = Pedido(
            id = documento.id,
            compradorId = comprador.uid,
            compradorNombre = comprador.nombre,
            lineas = lineas,
            total = lineas.sumOf { it.subtotal },
            fecha = System.currentTimeMillis(),
            estado = "Registrado"
        )

        documento.set(
            mapOf(
                "compradorId" to pedido.compradorId,
                "compradorNombre" to pedido.compradorNombre,
                "total" to pedido.total,
                "fecha" to pedido.fecha,
                "estado" to pedido.estado,
                "lineas" to pedido.lineas.map { linea ->
                    mapOf(
                        "productoId" to linea.productoId,
                        "nombre" to linea.nombre,
                        "precio" to linea.precio,
                        "cantidad" to linea.cantidad
                    )
                }
            )
        ).await()

        pedido.lineas.forEach { productos.descontarExistencias(it.productoId, it.cantidad) }

        local.guardarPedido(pedido)
        vaciar()
        pedido
    }

    /**
     * Historial de pedidos. Se lee de Firestore y se refresca la copia local;
     * si no hay red se devuelve directamente lo guardado en SQLite.
     */
    suspend fun pedidos(compradorId: String): List<Pedido> = withContext(Dispatchers.IO) {
        try {
            val documentos = base.collection(ServicioFirebase.PEDIDOS)
                .whereEqualTo("compradorId", compradorId)
                .get().await().documents

            val pedidos = documentos.map { documento ->
                @Suppress("UNCHECKED_CAST")
                val crudas = documento.get("lineas") as? List<Map<String, Any?>> ?: emptyList()
                Pedido(
                    id = documento.id,
                    compradorId = documento.getString("compradorId").orEmpty(),
                    compradorNombre = documento.getString("compradorNombre").orEmpty(),
                    lineas = crudas.map { linea ->
                        LineaPedido(
                            productoId = linea["productoId"] as? String ?: "",
                            nombre = linea["nombre"] as? String ?: "",
                            precio = (linea["precio"] as? Number)?.toDouble() ?: 0.0,
                            cantidad = (linea["cantidad"] as? Number)?.toInt() ?: 0
                        )
                    },
                    total = documento.getDouble("total") ?: 0.0,
                    fecha = documento.getLong("fecha") ?: 0L,
                    estado = documento.getString("estado") ?: "Registrado"
                )
            }.sortedByDescending { it.fecha }

            pedidos.forEach { local.guardarPedido(it) }
            pedidos
        } catch (e: Exception) {
            local.pedidos(compradorId)
        }
    }
}
