package com.nexcode.marketplace.ui.inicio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.databinding.FragmentInicioBinding
import com.nexcode.marketplace.dominio.Categorias
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.ui.admin.AdminActivity
import com.nexcode.marketplace.ui.catalogo.AdaptadorProductos
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.detalle.DetalleProductoActivity
import com.nexcode.marketplace.ui.principal.PrincipalActivity

/**
 * Portada de la tienda: saludo, categorias, mas vendidos y recien llegados.
 *
 * Es una vista de lectura; cualquier accion mas larga (buscar, filtrar) lleva
 * al catalogo, que es la pantalla preparada para eso.
 */
class InicioFragment : Fragment() {

    private var _vistas: FragmentInicioBinding? = null
    private val vistas get() = _vistas!!

    private lateinit var destacados: AdaptadorProductos
    private lateinit var novedades: AdaptadorProductos
    private var escucha: ListenerRegistration? = null

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estado: Bundle?
    ): View {
        _vistas = FragmentInicioBinding.inflate(inflador, contenedor, false)
        return vistas.root
    }

    override fun onViewCreated(vista: View, estado: Bundle?) {
        super.onViewCreated(vista, estado)
        vistas.encabezado.ajustarASistema()

        val usuario = app.usuario
        vistas.saludo.text = getString(R.string.bienvenido) + ", " +
            (usuario?.nombre?.substringBefore(' ') ?: "")

        destacados = AdaptadorProductos(
            alAbrir = { DetalleProductoActivity.abrir(requireContext(), it.id) },
            alAgregar = { agregar(it) },
            anchoFijoDp = 172
        )
        novedades = AdaptadorProductos(
            alAbrir = { DetalleProductoActivity.abrir(requireContext(), it.id) },
            alAgregar = { agregar(it) }
        )
        vistas.listaDestacados.adapter = destacados
        vistas.listaNovedades.layoutManager = GridLayoutManager(requireContext(), 2)
        vistas.listaNovedades.adapter = novedades

        construirChips()

        vistas.buscador.setOnClickListener {
            (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_catalogo)
        }
        vistas.verTodoDestacados.setOnClickListener {
            (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_catalogo)
        }

        val esVendedor = app.esAdministrador
        vistas.tarjetaPanel.visibility = if (esVendedor) View.VISIBLE else View.GONE
        vistas.tarjetaPanel.setOnClickListener {
            startActivity(Intent(requireContext(), AdminActivity::class.java))
        }

        Animaciones.entradaEscalonada(
            120, vistas.saludo, vistas.tituloMarca, vistas.buscador, vistas.tarjetaPanel
        )

        mostrar(app.repositorioProductos.catalogoLocal())
    }

    override fun onStart() {
        super.onStart()
        escucha = app.repositorioProductos.escucharCatalogo(
            alCambiar = { if (_vistas != null) mostrar(it) }
        )
    }

    override fun onStop() {
        super.onStop()
        escucha?.remove()
        escucha = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _vistas = null
    }

    private fun construirChips() {
        val inflador = LayoutInflater.from(requireContext())
        Categorias.LISTA.forEach { nombre ->
            val chip = inflador.inflate(
                R.layout.item_chip_categoria, vistas.grupoCategorias, false
            ) as Chip
            chip.text = nombre
            chip.isCheckable = false
            chip.setOnClickListener {
                (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_catalogo)
            }
            vistas.grupoCategorias.addView(chip)
        }
    }

    private fun mostrar(catalogo: List<Producto>) {
        // Mas vendidos: los que mas unidades han salido; si aun no hay ventas,
        // manda la calificacion, para que la portada nunca se vea vacia.
        destacados.submitList(
            catalogo.sortedWith(
                compareByDescending<Producto> { it.vendidos }
                    .thenByDescending { it.calificacion }
            ).take(8)
        )
        novedades.submitList(catalogo.sortedByDescending { it.fechaRegistro }.take(6))

        vistas.resumenPanel.text = getString(R.string.productos_encontrados, catalogo.size)

        val vacio = catalogo.isEmpty()
        vistas.vacio.root.visibility = if (vacio) View.VISIBLE else View.GONE
        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_tienda)
        vistas.vacio.tituloVacio.text = getString(R.string.catalogo_vacio)
        vistas.vacio.detalleVacio.text = getString(
            if (app.esAdministrador) R.string.sembrar_catalogo else R.string.lema
        )
    }

    private fun agregar(producto: Producto) {
        val cantidad = app.repositorioCarrito.agregar(producto)
        if (cantidad == 0) {
            vistas.root.avisar(getString(R.string.sin_existencias))
            return
        }
        (activity as? PrincipalActivity)?.actualizarInsignia()
        vistas.root.avisar(
            getString(R.string.agregado_carrito),
            getString(R.string.nav_carrito)
        ) {
            (activity as? PrincipalActivity)?.abrirSeccion(R.id.nav_carrito)
        }
    }
}
