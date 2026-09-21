package com.nexcode.marketplace.ui.pedidos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ActivityPedidosBinding
import com.nexcode.marketplace.databinding.ItemPedidoBinding
import com.nexcode.marketplace.dominio.Pedido
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import kotlinx.coroutines.launch

/**
 * Historial de pedidos del comprador.
 *
 * Los pedidos se leen de Firestore; si no hay red, el repositorio devuelve la
 * copia guardada en la base SQL local.
 */
class PedidosActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityPedidosBinding
    private lateinit var adaptador: AdaptadorPedidos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityPedidosBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        vistas.atras.setOnClickListener { finish() }

        adaptador = AdaptadorPedidos()
        vistas.lista.layoutManager = LinearLayoutManager(this)
        vistas.lista.adapter = adaptador

        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_paquete)
        vistas.vacio.tituloVacio.text = getString(R.string.sin_pedidos)
        vistas.vacio.detalleVacio.text = getString(R.string.carrito_vacio_detalle)

        cargar()
    }

    private fun cargar() {
        val usuario = app.usuario ?: return
        vistas.progreso.visibility = View.VISIBLE
        lifecycleScope.launch {
            val pedidos = app.repositorioCarrito.pedidos(usuario.uid)
            vistas.progreso.visibility = View.GONE
            adaptador.submitList(pedidos)
            vistas.vacio.root.visibility = if (pedidos.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}

/** Tarjetas del historial: numero, fecha, lineas, unidades y total. */
class AdaptadorPedidos : ListAdapter<Pedido, AdaptadorPedidos.Casilla>(COMPARADOR) {

    class Casilla(val vistas: ItemPedidoBinding) : RecyclerView.ViewHolder(vistas.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) = Casilla(
        ItemPedidoBinding.inflate(LayoutInflater.from(padre.context), padre, false)
    )

    override fun onBindViewHolder(casilla: Casilla, posicion: Int) {
        val pedido = getItem(posicion)
        val vistas = casilla.vistas

        vistas.numero.text = Formato.numeroPedido(pedido.id)
        vistas.fecha.text = Formato.fechaCorta(pedido.fecha)
        vistas.estado.text = pedido.estado.uppercase()
        vistas.total.text = Formato.moneda(pedido.total)
        vistas.unidades.text = pedido.unidades.toString() + " unidades"
        vistas.detalle.text = pedido.lineas.joinToString("\n") {
            it.cantidad.toString() + " x " + it.nombre
        }
    }

    private companion object {
        val COMPARADOR = object : DiffUtil.ItemCallback<Pedido>() {
            override fun areItemsTheSame(anterior: Pedido, nuevo: Pedido) =
                anterior.id == nuevo.id

            override fun areContentsTheSame(anterior: Pedido, nuevo: Pedido) =
                anterior == nuevo
        }
    }
}
