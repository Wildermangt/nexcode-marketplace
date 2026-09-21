package com.nexcode.marketplace.ui.catalogo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ItemProductoBinding
import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.ui.comun.CargadorImagenes

/**
 * Rejilla de productos del catalogo.
 *
 * Usa [ListAdapter] para que, cuando Firestore envie una version nueva del
 * catalogo, solo se repinten las tarjetas que de verdad cambiaron; asi la
 * lista no parpadea mientras el comprador la esta mirando.
 */
class AdaptadorProductos(
    private val alAbrir: (Producto) -> Unit,
    private val alAgregar: (Producto) -> Unit,
    /**
     * Ancho en dp de cada tarjeta. Solo se indica en las listas horizontales
     * (los destacados de la pantalla de inicio); en la rejilla del catalogo se
     * deja nulo para que la tarjeta ocupe su columna completa.
     */
    private val anchoFijoDp: Int? = null
) : ListAdapter<Producto, AdaptadorProductos.Casilla>(COMPARADOR) {

    class Casilla(val vistas: ItemProductoBinding) : RecyclerView.ViewHolder(vistas.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int): Casilla {
        val vistas = ItemProductoBinding.inflate(
            LayoutInflater.from(padre.context), padre, false
        )
        anchoFijoDp?.let { ancho ->
            val densidad = padre.resources.displayMetrics.density
            vistas.root.layoutParams.width = (ancho * densidad).toInt()
        }
        return Casilla(vistas)
    }

    override fun onBindViewHolder(casilla: Casilla, posicion: Int) {
        val producto = getItem(posicion)
        val vistas = casilla.vistas
        val contexto = vistas.root.context

        CargadorImagenes.cargar(vistas.imagen, producto.imagen)
        vistas.marca.text = producto.marca.ifBlank { producto.categoria }
        vistas.nombre.text = producto.nombre
        vistas.precio.text = Formato.moneda(producto.precio)

        vistas.estrellas.mostrarPromedio(producto.calificacion)
        vistas.promedio.text = if (producto.votos > 0) {
            Formato.decimal(producto.calificacion) + " (" + producto.votos + ")"
        } else {
            Formato.decimal(producto.calificacion)
        }

        // Estado: nuevo o usado, con la pastilla del color correspondiente.
        vistas.estado.text = producto.estado.etiqueta.uppercase()
        val esNuevo = producto.estado == EstadoProducto.NUEVO
        vistas.estado.setBackgroundResource(
            if (esNuevo) R.drawable.fondo_pastilla_verde else R.drawable.fondo_pastilla_ambar
        )
        vistas.estado.setTextColor(
            contexto.getColor(if (esNuevo) R.color.nex_verde_oscuro else R.color.nex_ambar)
        )

        val hay = producto.hayExistencias
        vistas.agotado.visibility = if (hay) View.GONE else View.VISIBLE
        vistas.agregar.isEnabled = hay
        vistas.agregar.alpha = if (hay) 1f else 0.35f
        vistas.existencias.text = if (hay) {
            contexto.getString(R.string.disponibles, producto.cantidad)
        } else {
            contexto.getString(R.string.sin_existencias)
        }

        vistas.tarjeta.setOnClickListener { alAbrir(producto) }
        vistas.agregar.setOnClickListener { alAgregar(producto) }
    }

    private companion object {
        val COMPARADOR = object : DiffUtil.ItemCallback<Producto>() {
            override fun areItemsTheSame(anterior: Producto, nuevo: Producto) =
                anterior.id == nuevo.id

            override fun areContentsTheSame(anterior: Producto, nuevo: Producto) =
                anterior == nuevo
        }
    }
}
