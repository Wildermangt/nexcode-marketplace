package com.nexcode.marketplace.ui.carrito

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ItemCarritoBinding
import com.nexcode.marketplace.dominio.ItemCarrito
import com.nexcode.marketplace.ui.comun.CargadorImagenes

/**
 * Lineas del carrito, con los controles de cantidad y el boton de quitar.
 *
 * El adaptador no toca la base de datos: avisa por [alCambiarCantidad] y
 * [alQuitar], y es el fragmento quien decide que guardar.
 */
class AdaptadorCarrito(
    private val alCambiarCantidad: (ItemCarrito, Int) -> Unit,
    private val alQuitar: (ItemCarrito) -> Unit
) : ListAdapter<ItemCarrito, AdaptadorCarrito.Casilla>(COMPARADOR) {

    class Casilla(val vistas: ItemCarritoBinding) : RecyclerView.ViewHolder(vistas.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) = Casilla(
        ItemCarritoBinding.inflate(LayoutInflater.from(padre.context), padre, false)
    )

    override fun onBindViewHolder(casilla: Casilla, posicion: Int) {
        val linea = getItem(posicion)
        val vistas = casilla.vistas

        CargadorImagenes.cargar(vistas.imagen, linea.imagen)
        vistas.nombre.text = linea.nombre
        vistas.precio.text = Formato.moneda(linea.precio) + " c/u"
        vistas.cantidad.text = linea.cantidad.toString()
        vistas.subtotal.text = Formato.moneda(linea.subtotal)

        // No se puede pedir mas de lo que hay en inventario.
        vistas.mas.isEnabled = linea.cantidad < linea.existencias
        vistas.mas.alpha = if (vistas.mas.isEnabled) 1f else 0.3f

        vistas.mas.setOnClickListener { alCambiarCantidad(linea, linea.cantidad + 1) }
        vistas.menos.setOnClickListener { alCambiarCantidad(linea, linea.cantidad - 1) }
        vistas.quitar.setOnClickListener { alQuitar(linea) }
    }

    private companion object {
        val COMPARADOR = object : DiffUtil.ItemCallback<ItemCarrito>() {
            override fun areItemsTheSame(anterior: ItemCarrito, nuevo: ItemCarrito) =
                anterior.productoId == nuevo.productoId

            override fun areContentsTheSame(anterior: ItemCarrito, nuevo: ItemCarrito) =
                anterior == nuevo
        }
    }
}
