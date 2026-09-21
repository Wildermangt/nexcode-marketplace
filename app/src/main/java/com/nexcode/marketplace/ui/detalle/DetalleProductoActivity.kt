package com.nexcode.marketplace.ui.detalle

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.core.Formato
import com.nexcode.marketplace.databinding.ActivityDetalleProductoBinding
import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.ui.calificacion.CalificacionActivity
import com.nexcode.marketplace.ui.chat.ChatActivity
import com.nexcode.marketplace.ui.comun.CargadorImagenes
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import kotlinx.coroutines.launch

/**
 * Ficha completa de un producto: fotografia, precio, estado, existencias,
 * descripcion, vendedor y opiniones.
 *
 * Desde aqui se agrega al carrito, se abre la pantalla de calificacion y se
 * escribe al vendedor.
 */
class DetalleProductoActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityDetalleProductoBinding
    private lateinit var opiniones: AdaptadorOpiniones

    private var producto: Producto? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityDetalleProductoBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.atras.ajustarASistema()

        opiniones = AdaptadorOpiniones()
        vistas.listaOpiniones.layoutManager = LinearLayoutManager(this)
        vistas.listaOpiniones.adapter = opiniones

        vistas.atras.setOnClickListener { finish() }

        val id = intent.getStringExtra(EXTRA_ID).orEmpty()
        // Se pinta primero con la copia local para que la pantalla abra al
        // instante, y luego se refresca con Firestore.
        app.repositorioProductos.productoLocal(id)?.let { pintar(it) }
        cargar(id)
    }

    override fun onResume() {
        super.onResume()
        producto?.let { cargar(it.id) }
    }

    private fun cargar(id: String) {
        lifecycleScope.launch {
            app.repositorioProductos.obtener(id)?.let { pintar(it) }
            opiniones.submitList(app.repositorioProductos.opiniones(id))
            vistas.sinOpiniones.visibility =
                if (opiniones.itemCount == 0) View.VISIBLE else View.GONE
        }
    }

    private fun pintar(nuevo: Producto) {
        producto = nuevo

        CargadorImagenes.cargar(vistas.imagen, nuevo.imagen)
        vistas.marca.text = nuevo.marca.ifBlank { nuevo.categoria }
        vistas.nombre.text = nuevo.nombre
        vistas.descripcion.text = nuevo.descripcion
        vistas.categoria.text = nuevo.categoria
        vistas.referencia.text = nuevo.referencia.ifBlank { Formato.numeroPedido(nuevo.id) }
        vistas.vendedor.text = nuevo.vendedorNombre.ifBlank { getString(R.string.marca) }
        vistas.precio.text = Formato.moneda(nuevo.precio)

        vistas.estrellas.mostrarPromedio(nuevo.calificacion)
        vistas.promedio.text = getString(
            R.string.promedio_de, Formato.decimal(nuevo.calificacion)
        ) + " (" + nuevo.votos + ")"

        val esNuevo = nuevo.estado == EstadoProducto.NUEVO
        vistas.estado.text = nuevo.estado.etiqueta.uppercase()
        vistas.estado.setBackgroundResource(
            if (esNuevo) R.drawable.fondo_pastilla_verde else R.drawable.fondo_pastilla_ambar
        )
        vistas.estado.setTextColor(
            getColor(if (esNuevo) R.color.nex_verde_oscuro else R.color.nex_ambar)
        )

        vistas.existencias.text = if (nuevo.hayExistencias) {
            getString(R.string.disponibles, nuevo.cantidad)
        } else {
            getString(R.string.sin_existencias)
        }
        vistas.agregar.isEnabled = nuevo.hayExistencias

        vistas.agregar.setOnClickListener { agregar(nuevo) }
        vistas.calificarEnlace.setOnClickListener {
            CalificacionActivity.abrir(this, nuevo.id, nuevo.nombre)
        }
        vistas.filaCalificacion.setOnClickListener {
            CalificacionActivity.abrir(this, nuevo.id, nuevo.nombre)
        }
        // El chat es siempre comprador -> vendedor. Si quien mira la ficha es
        // el propio vendedor no hay a quien escribirle: el boton se oculta y
        // sus conversaciones las atiende desde la bandeja del panel.
        val esComprador = app.usuario?.esAdministrador == false
        vistas.escribir.visibility = if (esComprador) View.VISIBLE else View.GONE
        vistas.escribir.setOnClickListener {
            ChatActivity.abrirComoComprador(this, nuevo.nombre)
        }
    }

    private fun agregar(producto: Producto) {
        val cantidad = app.repositorioCarrito.agregar(producto)
        if (cantidad == 0) {
            vistas.root.avisar(getString(R.string.sin_existencias))
            return
        }
        Animaciones.latido(vistas.agregar)
        vistas.root.avisar(getString(R.string.agregado_carrito))
    }

    companion object {
        private const val EXTRA_ID = "productoId"

        fun abrir(contexto: Context, productoId: String) {
            contexto.startActivity(
                Intent(contexto, DetalleProductoActivity::class.java)
                    .putExtra(EXTRA_ID, productoId)
            )
        }
    }
}
