package com.nexcode.marketplace.datos.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.ItemCarrito
import com.nexcode.marketplace.dominio.LineaPedido
import com.nexcode.marketplace.dominio.Mensaje
import com.nexcode.marketplace.dominio.Pedido
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.dominio.TipoUsuario
import com.nexcode.marketplace.dominio.Usuario

/**
 * Acceso a la base SQL local. Traduce entre las tablas de [BaseDatosSQL] y los
 * modelos de dominio.
 *
 * Todas las operaciones son sincronas y rapidas (SQLite en el propio telefono);
 * quien las llama se encarga de sacarlas del hilo principal cuando el volumen
 * lo amerita, como en la sincronizacion del catalogo completo.
 */
class AlmacenLocal(contexto: Context) {

    private val ayudante = BaseDatosSQL(contexto)

    // -----------------------------------------------------------------------
    //  Usuarios
    // -----------------------------------------------------------------------

    fun guardarUsuario(usuario: Usuario) {
        val valores = ContentValues().apply {
            put("uid", usuario.uid)
            put("nombre", usuario.nombre)
            put("correo", usuario.correo)
            put("tipo_usuario", usuario.tipoUsuario.clave)
            put("fecha_registro", usuario.fechaRegistro)
        }
        ayudante.writableDatabase.insertWithOnConflict(
            BaseDatosSQL.T_USUARIOS, null, valores,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun usuario(uid: String): Usuario? =
        ayudante.readableDatabase.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_USUARIOS} WHERE uid = ?", arrayOf(uid)
        ).use { c -> if (c.moveToFirst()) c.aUsuario() else null }

    // -----------------------------------------------------------------------
    //  Productos: espejo local del catalogo de Firestore
    // -----------------------------------------------------------------------

    /**
     * Reescribe por completo la tabla de productos con lo que acaba de llegar
     * de Firestore. Se hace dentro de una transaccion para que la aplicacion
     * nunca lea un catalogo a medio escribir.
     */
    fun reemplazarProductos(productos: List<Producto>) {
        val base = ayudante.writableDatabase
        base.beginTransaction()
        try {
            base.delete(BaseDatosSQL.T_PRODUCTOS, null, null)
            productos.forEach { base.insert(BaseDatosSQL.T_PRODUCTOS, null, it.aValores()) }
            base.setTransactionSuccessful()
        } finally {
            base.endTransaction()
        }
    }

    fun productos(): List<Producto> =
        ayudante.readableDatabase.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_PRODUCTOS} ORDER BY fecha_registro DESC", null
        ).use { it.mapear { c -> c.aProducto() } }

    fun producto(id: String): Producto? =
        ayudante.readableDatabase.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_PRODUCTOS} WHERE id = ?", arrayOf(id)
        ).use { c -> if (c.moveToFirst()) c.aProducto() else null }

    // -----------------------------------------------------------------------
    //  Carrito: solo vive en el telefono hasta que se confirma la compra
    // -----------------------------------------------------------------------

    fun carrito(usuarioId: String): List<ItemCarrito> =
        ayudante.readableDatabase.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_CARRITO} WHERE usuario_id = ? ORDER BY nombre",
            arrayOf(usuarioId)
        ).use { it.mapear { c -> c.aItemCarrito() } }

    /**
     * Suma unidades de un producto al carrito sin pasarse de las existencias.
     * @return la cantidad que quedo en el carrito
     */
    fun agregarAlCarrito(usuarioId: String, producto: Producto, unidades: Int = 1): Int {
        val actual = unidadesEnCarrito(usuarioId, producto.id)
        val nueva = minOf(actual + unidades, producto.cantidad)
        if (nueva <= 0) return 0

        val valores = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("producto_id", producto.id)
            put("nombre", producto.nombre)
            put("imagen", producto.imagen)
            put("precio", producto.precio)
            put("cantidad", nueva)
            put("existencias", producto.cantidad)
        }
        ayudante.writableDatabase.insertWithOnConflict(
            BaseDatosSQL.T_CARRITO, null, valores,
            android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        )
        return nueva
    }

    fun cambiarCantidad(usuarioId: String, productoId: String, cantidad: Int) {
        if (cantidad <= 0) {
            quitarDelCarrito(usuarioId, productoId)
            return
        }
        ayudante.writableDatabase.update(
            BaseDatosSQL.T_CARRITO,
            ContentValues().apply { put("cantidad", cantidad) },
            "usuario_id = ? AND producto_id = ?",
            arrayOf(usuarioId, productoId)
        )
    }

    fun quitarDelCarrito(usuarioId: String, productoId: String) {
        ayudante.writableDatabase.delete(
            BaseDatosSQL.T_CARRITO, "usuario_id = ? AND producto_id = ?",
            arrayOf(usuarioId, productoId)
        )
    }

    fun vaciarCarrito(usuarioId: String) {
        ayudante.writableDatabase.delete(
            BaseDatosSQL.T_CARRITO, "usuario_id = ?", arrayOf(usuarioId)
        )
    }

    /** Numero de unidades en el carrito: alimenta la insignia de la barra inferior. */
    fun unidadesEnCarrito(usuarioId: String): Int =
        ayudante.readableDatabase.rawQuery(
            "SELECT TOTAL(cantidad) FROM ${BaseDatosSQL.T_CARRITO} WHERE usuario_id = ?",
            arrayOf(usuarioId)
        ).use { c -> if (c.moveToFirst()) c.getDouble(0).toInt() else 0 }

    private fun unidadesEnCarrito(usuarioId: String, productoId: String): Int =
        ayudante.readableDatabase.rawQuery(
            "SELECT cantidad FROM ${BaseDatosSQL.T_CARRITO} " +
                "WHERE usuario_id = ? AND producto_id = ?",
            arrayOf(usuarioId, productoId)
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    // -----------------------------------------------------------------------
    //  Pedidos
    // -----------------------------------------------------------------------

    fun guardarPedido(pedido: Pedido) {
        val base = ayudante.writableDatabase
        base.beginTransaction()
        try {
            base.insertWithOnConflict(
                BaseDatosSQL.T_PEDIDOS, null,
                ContentValues().apply {
                    put("id", pedido.id)
                    put("comprador_id", pedido.compradorId)
                    put("comprador_nombre", pedido.compradorNombre)
                    put("total", pedido.total)
                    put("fecha", pedido.fecha)
                    put("estado", pedido.estado)
                },
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
            )
            base.delete(BaseDatosSQL.T_PEDIDO_LINEAS, "pedido_id = ?", arrayOf(pedido.id))
            pedido.lineas.forEach { linea ->
                base.insert(
                    BaseDatosSQL.T_PEDIDO_LINEAS, null,
                    ContentValues().apply {
                        put("pedido_id", pedido.id)
                        put("producto_id", linea.productoId)
                        put("nombre", linea.nombre)
                        put("precio", linea.precio)
                        put("cantidad", linea.cantidad)
                    }
                )
            }
            base.setTransactionSuccessful()
        } finally {
            base.endTransaction()
        }
    }

    fun pedidos(compradorId: String): List<Pedido> {
        val base = ayudante.readableDatabase
        val cabeceras = base.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_PEDIDOS} WHERE comprador_id = ? ORDER BY fecha DESC",
            arrayOf(compradorId)
        ).use { c ->
            c.mapear { cur ->
                Pedido(
                    id = cur.texto("id"),
                    compradorId = cur.texto("comprador_id"),
                    compradorNombre = cur.texto("comprador_nombre"),
                    total = cur.real("total"),
                    fecha = cur.entero("fecha"),
                    estado = cur.texto("estado")
                )
            }
        }
        return cabeceras.map { it.copy(lineas = lineas(it.id)) }
    }

    private fun lineas(pedidoId: String): List<LineaPedido> =
        ayudante.readableDatabase.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_PEDIDO_LINEAS} WHERE pedido_id = ?",
            arrayOf(pedidoId)
        ).use { c ->
            c.mapear { cur ->
                LineaPedido(
                    productoId = cur.texto("producto_id"),
                    nombre = cur.texto("nombre"),
                    precio = cur.real("precio"),
                    cantidad = cur.entero("cantidad").toInt()
                )
            }
        }

    // -----------------------------------------------------------------------
    //  Mensajes: copia local de la conversacion para abrirla sin conexion
    // -----------------------------------------------------------------------

    fun reemplazarMensajes(chatId: String, mensajes: List<Mensaje>) {
        val base = ayudante.writableDatabase
        base.beginTransaction()
        try {
            base.delete(BaseDatosSQL.T_MENSAJES, "chat_id = ?", arrayOf(chatId))
            mensajes.forEach { mensaje ->
                base.insert(
                    BaseDatosSQL.T_MENSAJES, null,
                    ContentValues().apply {
                        put("id", mensaje.id)
                        put("chat_id", chatId)
                        put("autor_id", mensaje.autorId)
                        put("autor_nombre", mensaje.autorNombre)
                        put("texto", mensaje.texto)
                        put("fecha", mensaje.fecha)
                        put("del_vendedor", if (mensaje.esDelVendedor) 1 else 0)
                    }
                )
            }
            base.setTransactionSuccessful()
        } finally {
            base.endTransaction()
        }
    }

    fun mensajes(chatId: String): List<Mensaje> =
        ayudante.readableDatabase.rawQuery(
            "SELECT * FROM ${BaseDatosSQL.T_MENSAJES} WHERE chat_id = ? ORDER BY fecha",
            arrayOf(chatId)
        ).use { c ->
            c.mapear { cur ->
                Mensaje(
                    id = cur.texto("id"),
                    autorId = cur.texto("autor_id"),
                    autorNombre = cur.texto("autor_nombre"),
                    texto = cur.texto("texto"),
                    fecha = cur.entero("fecha"),
                    esDelVendedor = cur.entero("del_vendedor") == 1L
                )
            }
        }

    /** Al cerrar sesion se borra lo que pertenece a esa persona. */
    fun limpiarSesion(usuarioId: String) {
        vaciarCarrito(usuarioId)
        ayudante.writableDatabase.delete(
            BaseDatosSQL.T_PEDIDOS, "comprador_id = ?", arrayOf(usuarioId)
        )
    }

    // -----------------------------------------------------------------------
    //  Conversion entre cursores y modelos
    // -----------------------------------------------------------------------

    private fun Producto.aValores() = ContentValues().apply {
        put("id", id)
        put("nombre", nombre)
        put("descripcion", descripcion)
        put("precio", precio)
        put("categoria", categoria)
        put("estado", estado.clave)
        put("cantidad", cantidad)
        put("imagen", imagen)
        put("marca", marca)
        put("referencia", referencia)
        put("vendedor_id", vendedorId)
        put("vendedor_nombre", vendedorNombre)
        put("calificacion", calificacion)
        put("votos", votos)
        put("vendidos", vendidos)
        put("fecha_registro", fechaRegistro)
    }

    private fun Cursor.aProducto() = Producto(
        id = texto("id"),
        nombre = texto("nombre"),
        descripcion = texto("descripcion"),
        precio = real("precio"),
        categoria = texto("categoria"),
        estado = EstadoProducto.desde(texto("estado")),
        cantidad = entero("cantidad").toInt(),
        imagen = texto("imagen"),
        marca = texto("marca"),
        referencia = texto("referencia"),
        vendedorId = texto("vendedor_id"),
        vendedorNombre = texto("vendedor_nombre"),
        calificacion = real("calificacion"),
        votos = entero("votos").toInt(),
        vendidos = entero("vendidos").toInt(),
        fechaRegistro = entero("fecha_registro")
    )

    private fun Cursor.aUsuario() = Usuario(
        uid = texto("uid"),
        nombre = texto("nombre"),
        correo = texto("correo"),
        tipoUsuario = TipoUsuario.desde(texto("tipo_usuario")),
        fechaRegistro = entero("fecha_registro")
    )

    private fun Cursor.aItemCarrito() = ItemCarrito(
        productoId = texto("producto_id"),
        nombre = texto("nombre"),
        imagen = texto("imagen"),
        precio = real("precio"),
        cantidad = entero("cantidad").toInt(),
        existencias = entero("existencias").toInt()
    )
}

// ---------------------------------------------------------------------------
//  Ayudas de lectura de cursores: acortan el codigo y evitan indices sueltos
// ---------------------------------------------------------------------------

private fun Cursor.texto(columna: String): String =
    getColumnIndex(columna).let { if (it < 0 || isNull(it)) "" else getString(it) }

private fun Cursor.real(columna: String): Double =
    getColumnIndex(columna).let { if (it < 0 || isNull(it)) 0.0 else getDouble(it) }

private fun Cursor.entero(columna: String): Long =
    getColumnIndex(columna).let { if (it < 0 || isNull(it)) 0L else getLong(it) }

private inline fun <T> Cursor.mapear(bloque: (Cursor) -> T): List<T> {
    val salida = ArrayList<T>(count)
    while (moveToNext()) {
        salida.add(bloque(this))
    }
    return salida
}
