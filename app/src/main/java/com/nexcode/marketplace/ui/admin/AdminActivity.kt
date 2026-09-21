package com.nexcode.marketplace.ui.admin

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ActivityAdminBinding
import com.nexcode.marketplace.databinding.ItemProductoAdminBinding
import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.ui.comun.CargadorImagenes
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import kotlinx.coroutines.launch

/**
 * Panel del administrador / vendedor.
 *
 * Reune lo que la guia pide del lado de quien vende: ver el inventario,
 * registrar, editar y eliminar productos, y la carga inicial del catalogo
 * mayorista con sus fotografias.
 */
class AdminActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityAdminBinding
    private lateinit var adaptador: AdaptadorProductosAdmin
    private var escucha: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        vistas.atras.setOnClickListener { finish() }

        adaptador = AdaptadorProductosAdmin(
            alEditar = { EditarProductoActivity.abrir(this, it.id) },
            alEliminar = { confirmarEliminacion(it) }
        )
        vistas.lista.layoutManager = LinearLayoutManager(this)
        vistas.lista.adapter = adaptador

        vistas.nuevo.setOnClickListener { EditarProductoActivity.abrir(this, null) }
        vistas.tarjetaSembrar.setOnClickListener { sembrar() }
        vistas.tarjetaMigrar.setOnClickListener { migrarFotos() }

        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_tienda)
        vistas.vacio.tituloVacio.text = getString(R.string.catalogo_vacio)
        vistas.vacio.detalleVacio.text = getString(R.string.sembrar_catalogo)

        pintar(app.repositorioProductos.catalogoLocal())
    }

    override fun onStart() {
        super.onStart()
        escucha = app.repositorioProductos.escucharCatalogo(alCambiar = { pintar(it) })
        revisarFotos()
    }

    override fun onStop() {
        super.onStop()
        escucha?.remove()
        escucha = null
    }

    private fun pintar(catalogo: List<Producto>) {
        adaptador.submitList(catalogo)

        vistas.metricaProductos.text = catalogo.size.toString()
        vistas.metricaUnidades.text = Formato.entero(catalogo.sumOf { it.cantidad }.toLong())
        vistas.metricaInventario.text =
            Formato.monedaCorta(catalogo.sumOf { it.precio * it.cantidad })

        val vacio = catalogo.isEmpty()
        vistas.vacio.root.visibility = if (vacio) View.VISIBLE else View.GONE
        vistas.tarjetaSembrar.visibility = if (vacio) View.VISIBLE else View.GONE
    }

    /**
     * Muestra el aviso de migracion solo si quedan fotos sirviendose desde la
     * aplicacion, que es lo que pasa cuando el catalogo se sembro antes de
     * habilitar Firebase Storage.
     */
    private fun revisarFotos() {
        lifecycleScope.launch {
            val pendientes = app.sembrador.fotosPendientes()
            vistas.tarjetaMigrar.visibility = if (pendientes > 0) View.VISIBLE else View.GONE
            vistas.detalleMigrar.text = getString(R.string.migrar_fotos_detalle, pendientes)
        }
    }

    /** Sube a Storage las fotografias que aun viven dentro de la aplicacion. */
    private fun migrarFotos() {
        vistas.progresoMigrar.visibility = View.VISIBLE
        vistas.tarjetaMigrar.isEnabled = false
        lifecycleScope.launch {
            try {
                val subidas = app.sembrador.migrarFotosAStorage { hechas, total ->
                    runOnUiThread {
                        vistas.progresoMigrar.max = total
                        vistas.progresoMigrar.setProgressCompat(hechas, true)
                    }
                }
                vistas.root.avisar(getString(R.string.fotos_migradas, subidas))
                revisarFotos()
            } catch (e: Exception) {
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            } finally {
                vistas.progresoMigrar.visibility = View.GONE
                vistas.tarjetaMigrar.isEnabled = true
            }
        }
    }

    /**
     * Carga las veinte referencias mayoristas. La barra avanza a medida que
     * cada fotografia termina de subir a Storage.
     */
    private fun sembrar() {
        val vendedor = app.usuario ?: return
        vistas.progresoSiembra.visibility = View.VISIBLE
        vistas.tarjetaSembrar.isEnabled = false

        lifecycleScope.launch {
            try {
                val resultado = app.sembrador.sembrar(vendedor) { hechos, total ->
                    runOnUiThread {
                        vistas.progresoSiembra.max = total
                        vistas.progresoSiembra.setProgressCompat(hechos, true)
                    }
                }
                vistas.progresoSiembra.visibility = View.GONE
                vistas.tarjetaSembrar.isEnabled = true

                if (resultado.yaExistia) {
                    vistas.root.avisar(getString(R.string.catalogo_sembrado, 0))
                } else {
                    vistas.root.avisar(
                        getString(R.string.catalogo_sembrado, resultado.sembrados)
                    )
                }
            } catch (e: Exception) {
                vistas.progresoSiembra.visibility = View.GONE
                vistas.tarjetaSembrar.isEnabled = true
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    private fun confirmarEliminacion(producto: Producto) {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.eliminar_producto)
            .setMessage(getString(R.string.eliminar_pregunta, producto.nombre))
            .setNegativeButton(R.string.cancelar, null)
            .setPositiveButton(R.string.eliminar) { _, _ ->
                lifecycleScope.launch {
                    try {
                        app.repositorioProductos.eliminar(producto)
                        vistas.root.avisar(getString(R.string.producto_eliminado))
                    } catch (e: Exception) {
                        vistas.root.avisar(e.message ?: getString(R.string.error_generico))
                    }
                }
            }
            .show()
    }
}

/** Lista del inventario con los botones de editar y eliminar. */
class AdaptadorProductosAdmin(
    private val alEditar: (Producto) -> Unit,
    private val alEliminar: (Producto) -> Unit
) : ListAdapter<Producto, AdaptadorProductosAdmin.Casilla>(COMPARADOR) {

    class Casilla(val vistas: ItemProductoAdminBinding) : RecyclerView.ViewHolder(vistas.root)

    override fun onCreateViewHolder(padre: ViewGroup, tipo: Int) = Casilla(
        ItemProductoAdminBinding.inflate(LayoutInflater.from(padre.context), padre, false)
    )

    override fun onBindViewHolder(casilla: Casilla, posicion: Int) {
        val producto = getItem(posicion)
        val vistas = casilla.vistas
        val contexto = vistas.root.context

        CargadorImagenes.cargar(vistas.imagen, producto.imagen)
        vistas.nombre.text = producto.nombre
        vistas.precio.text = Formato.moneda(producto.precio)
        vistas.existencias.text = contexto.getString(R.string.disponibles, producto.cantidad)

        val esNuevo = producto.estado == EstadoProducto.NUEVO
        vistas.estado.text = producto.estado.etiqueta.uppercase()
        vistas.estado.setBackgroundResource(
            if (esNuevo) R.drawable.fondo_pastilla_verde else R.drawable.fondo_pastilla_ambar
        )
        vistas.estado.setTextColor(
            contexto.getColor(if (esNuevo) R.color.nex_verde_oscuro else R.color.nex_ambar)
        )

        vistas.editar.setOnClickListener { alEditar(producto) }
        vistas.eliminar.setOnClickListener { alEliminar(producto) }
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
