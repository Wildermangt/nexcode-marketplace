package com.nexcode.marketplace.dominio

/**
 * Modelo de dominio de NEXCODE Marketplace.
 *
 * Estas clases no conocen ni Firestore ni SQLite: son las que viajan entre la
 * capa de datos y las pantallas. Cada repositorio se encarga de convertirlas
 * desde y hacia su propio formato de almacenamiento.
 */

/** Papel que cumple una persona dentro de la tienda. */
enum class TipoUsuario(val clave: String) {
    COMPRADOR("comprador"),
    ADMINISTRADOR("administrador");

    companion object {
        fun desde(clave: String?): TipoUsuario =
            entries.firstOrNull { it.clave == clave } ?: COMPRADOR
    }
}

/** Estado comercial del articulo, tal como lo exige la guia: nuevo o usado. */
enum class EstadoProducto(val clave: String, val etiqueta: String) {
    NUEVO("nuevo", "Nuevo"),
    USADO("usado", "Usado");

    companion object {
        fun desde(clave: String?): EstadoProducto =
            entries.firstOrNull { it.clave == clave } ?: NUEVO
    }
}

data class Usuario(
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val tipoUsuario: TipoUsuario = TipoUsuario.COMPRADOR,
    val fechaRegistro: Long = 0L
) {
    val esAdministrador: Boolean get() = tipoUsuario == TipoUsuario.ADMINISTRADOR

    /** Iniciales para el avatar circular del perfil. */
    val iniciales: String
        get() = nombre.trim().split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifEmpty { correo.take(1).uppercase() }
}

data class Producto(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val precio: Double = 0.0,
    val categoria: String = "",
    val estado: EstadoProducto = EstadoProducto.NUEVO,
    val cantidad: Int = 0,
    val imagen: String = "",
    val marca: String = "",
    val referencia: String = "",
    val vendedorId: String = "",
    val vendedorNombre: String = "",
    val calificacion: Double = 0.0,
    val votos: Int = 0,
    val vendidos: Int = 0,
    val fechaRegistro: Long = 0L
) {
    val hayExistencias: Boolean get() = cantidad > 0

    /**
     * Coincidencia usada por el buscador: nombre, marca, referencia,
     * categoria y descripcion, sin distinguir mayusculas ni acentos.
     */
    fun coincideCon(texto: String): Boolean {
        if (texto.isBlank()) return true
        val aguja = texto.normalizar()
        return listOf(nombre, marca, referencia, categoria, descripcion)
            .any { it.normalizar().contains(aguja) }
    }
}

data class ItemCarrito(
    val productoId: String = "",
    val nombre: String = "",
    val imagen: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 1,
    val existencias: Int = 0
) {
    val subtotal: Double get() = precio * cantidad
}

data class Calificacion(
    val usuarioId: String = "",
    val usuarioNombre: String = "",
    val estrellas: Int = 0,
    val comentario: String = "",
    val fecha: Long = 0L
)

data class Mensaje(
    val id: String = "",
    val autorId: String = "",
    val autorNombre: String = "",
    val texto: String = "",
    val fecha: Long = 0L,
    val esDelVendedor: Boolean = false
)

/** Cabecera de una conversacion, tal como la ve el panel del vendedor. */
data class Conversacion(
    val id: String = "",
    val compradorId: String = "",
    val compradorNombre: String = "",
    val ultimoMensaje: String = "",
    val fecha: Long = 0L,
    val sinLeerVendedor: Int = 0
)

data class LineaPedido(
    val productoId: String = "",
    val nombre: String = "",
    val precio: Double = 0.0,
    val cantidad: Int = 0
) {
    val subtotal: Double get() = precio * cantidad
}

data class Pedido(
    val id: String = "",
    val compradorId: String = "",
    val compradorNombre: String = "",
    val lineas: List<LineaPedido> = emptyList(),
    val total: Double = 0.0,
    val fecha: Long = 0L,
    val estado: String = "Registrado"
) {
    val unidades: Int get() = lineas.sumOf { it.cantidad }
}

/** Categorias del portafolio mayorista. La primera equivale a "sin filtro". */
object Categorias {
    const val TODAS = "Todas"

    val LISTA = listOf(
        "Computo", "Monitores", "Impresion", "Almacenamiento", "Componentes",
        "Perifericos", "Energia", "Redes", "Seguridad", "Movilidad", "Video"
    )

    fun conTodas(): List<String> = listOf(TODAS) + LISTA
}

/** Minusculas y sin tildes: lo que necesita una busqueda tolerante. */
fun String.normalizar(): String {
    val origen = "áàäâãéèëêíìïîóòöôõúùüûñçÁÀÄÂÃÉÈËÊÍÌÏÎÓÒÖÔÕÚÙÜÛÑÇ"
    val destino = "aaaaaeeeeiiiiooooouuuuncAAAAAEEEEIIIIOOOOOUUUUNC"
    val salida = StringBuilder(length)
    for (c in this) {
        val i = origen.indexOf(c)
        salida.append(if (i >= 0) destino[i] else c)
    }
    return salida.toString().lowercase()
}
