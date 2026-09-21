package com.nexcode.marketplace.ui.carrito

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.FragmentCarritoBinding
import com.nexcode.marketplace.dominio.ItemCarrito
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.pedidos.PedidosActivity
import com.nexcode.marketplace.ui.principal.PrincipalActivity
import kotlinx.coroutines.launch

/**
 * Carrito de compras.
 *
 * Las lineas se leen de la base SQL local. Al finalizar la compra se crea el
 * pedido en Firestore, se descuentan las existencias de cada producto y el
 * carrito queda vacio.
 */
class CarritoFragment : Fragment() {

    private var _vistas: FragmentCarritoBinding? = null
    private val vistas get() = _vistas!!

    private lateinit var adaptador: AdaptadorCarrito

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estado: Bundle?
    ): View {
        _vistas = FragmentCarritoBinding.inflate(inflador, contenedor, false)
        return vistas.root
    }

    override fun onViewCreated(vista: View, estado: Bundle?) {
        super.onViewCreated(vista, estado)
        vistas.encabezado.ajustarASistema()

        adaptador = AdaptadorCarrito(
            alCambiarCantidad = { linea, cantidad -> cambiar(linea, cantidad) },
            alQuitar = { quitar(it) }
        )
        vistas.lista.layoutManager = LinearLayoutManager(requireContext())
        vistas.lista.adapter = adaptador

        vistas.vaciar.setOnClickListener { confirmarVaciado() }
        vistas.pagar.setOnClickListener { finalizar() }

        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_carrito)
        vistas.vacio.tituloVacio.text = getString(R.string.carrito_vacio)
        vistas.vacio.detalleVacio.text = getString(R.string.carrito_vacio_detalle)
        vistas.vacio.accionVacio.text = getString(R.string.ir_catalogo)
        vistas.vacio.accionVacio.visibility = View.VISIBLE
        vistas.vacio.accionVacio.setOnClickListener {
            (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_catalogo)
        }
    }

    override fun onResume() {
        super.onResume()
        refrescar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vistas = null
    }

    private fun refrescar() {
        val lineas = app.repositorioCarrito.items()
        adaptador.submitList(lineas)

        val vacio = lineas.isEmpty()
        vistas.vacio.root.visibility = if (vacio) View.VISIBLE else View.GONE
        vistas.resumen.visibility = if (vacio) View.GONE else View.VISIBLE
        vistas.vaciar.visibility = if (vacio) View.GONE else View.VISIBLE

        val subtotal = lineas.sumOf { it.subtotal }
        vistas.subtotal.text = Formato.moneda(subtotal)
        vistas.total.text = Formato.moneda(subtotal)
        vistas.conteo.text = getString(R.string.productos_encontrados, lineas.size)

        (activity as? PrincipalActivity)?.actualizarInsignia()
    }

    private fun cambiar(linea: ItemCarrito, cantidad: Int) {
        if (cantidad > linea.existencias) {
            vistas.root.avisar(getString(R.string.disponibles, linea.existencias))
            return
        }
        app.repositorioCarrito.cambiarCantidad(linea.productoId, cantidad)
        refrescar()
    }

    private fun quitar(linea: ItemCarrito) {
        app.repositorioCarrito.quitar(linea.productoId)
        refrescar()
    }

    private fun confirmarVaciado() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.vaciar_carrito)
            .setMessage(R.string.carrito_vacio_detalle)
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                app.repositorioCarrito.vaciar()
                refrescar()
            }
            .show()
    }

    private fun finalizar() {
        val comprador = app.usuario ?: return
        ocupado(true)
        lifecycleScope.launch {
            try {
                val pedido = app.repositorioCarrito.finalizarCompra(comprador)
                ocupado(false)
                refrescar()
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.compra_realizada)
                    .setMessage(
                        getString(R.string.compra_detalle, Formato.numeroPedido(pedido.id))
                    )
                    .setNegativeButton(R.string.ir_catalogo) { _, _ ->
                        (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_catalogo)
                    }
                    .setPositiveButton(R.string.mis_pedidos) { _, _ ->
                        startActivity(
                            android.content.Intent(requireContext(), PedidosActivity::class.java)
                        )
                    }
                    .show()
            } catch (e: Exception) {
                ocupado(false)
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    private fun ocupado(activo: Boolean) {
        vistas.progreso.visibility = if (activo) View.VISIBLE else View.GONE
        vistas.pagar.isEnabled = !activo
    }
}
