package com.nexcode.marketplace.datos

import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.datos.remoto.AsistenteCompras
import com.nexcode.marketplace.dominio.Producto

/**
 * Orquesta el asistente de compras: arma el contexto con el catálogo real y
 * delega la conversación en {@link AsistenteCompras}.
 *
 * No conoce la interfaz: solo recibe la pregunta del usuario y devuelve la
 * respuesta en texto. Así la pantalla queda simple y el agente se puede probar
 * por separado.
 */
class RepositorioAsistente(
    private val repositorioProductos: RepositorioProductos,
    private val asistente: AsistenteCompras = AsistenteCompras()
) {

    /** ¿Hay catálogo cargado para que el asistente tenga de qué hablar? */
    fun hayCatalogo(): Boolean = repositorioProductos.catalogoLocal().isNotEmpty()

    /**
     * Procesa la pregunta del usuario contra el catálogo local.
     *
     * @throws com.nexcode.marketplace.datos.remoto.AsistenteError si el servicio falla
     */
    suspend fun preguntar(pregunta: String): String {
        val catalogo = repositorioProductos.catalogoLocal()
        return asistente.responder(describir(catalogo), pregunta)
    }

    /** Convierte el catálogo en un texto compacto que el modelo pueda leer. */
    private fun describir(productos: List<Producto>): String {
        if (productos.isEmpty()) return "(catálogo vacío)"
        return productos.joinToString("\n") { p ->
            "- ${p.nombre} | ${p.marca} | ${p.categoria} | ${p.estado.etiqueta} | " +
                "${Formato.moneda(p.precio)} | ${"%.1f".format(p.calificacion)}★ | " +
                "${if (p.hayExistencias) "${p.cantidad} disp." else "agotado"}"
        }
    }
}
