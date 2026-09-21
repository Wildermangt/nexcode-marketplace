package com.nexcode.marketplace.ui.catalogo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.ListenerRegistration
import com.nexcode.marketplace.R
import com.nexcode.marketplace.databinding.FragmentCatalogoBinding
import com.nexcode.marketplace.dominio.Categorias
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.detalle.DetalleProductoActivity
import com.nexcode.marketplace.ui.principal.PrincipalActivity

/**
 * Catalogo completo con busqueda y filtro por categoria.
 *
 * La lista llega de Firestore en tiempo real; el filtrado se hace en memoria
 * sobre esa lista, que es lo razonable para un catalogo de este tamano y evita
 * una consulta nueva por cada letra que se escribe.
 */
class CatalogoFragment : Fragment() {

    private var _vistas: FragmentCatalogoBinding? = null
    private val vistas get() = _vistas!!

    private lateinit var adaptador: AdaptadorProductos
    private var escucha: ListenerRegistration? = null

    private var catalogo: List<Producto> = emptyList()
    private var categoria: String = Categorias.TODAS
    private var busqueda: String = ""

    override fun onCreateView(
        inflador: LayoutInflater,
        contenedor: ViewGroup?,
        estado: Bundle?
    ): View {
        _vistas = FragmentCatalogoBinding.inflate(inflador, contenedor, false)
        return vistas.root
    }

    override fun onViewCreated(vista: View, estado: Bundle?) {
        super.onViewCreated(vista, estado)
        vistas.encabezado.ajustarASistema()

        adaptador = AdaptadorProductos(
            alAbrir = { DetalleProductoActivity.abrir(requireContext(), it.id) },
            alAgregar = { agregar(it) }
        )
        vistas.lista.layoutManager = GridLayoutManager(requireContext(), 2)
        vistas.lista.adapter = adaptador
        vistas.lista.itemAnimator?.changeDuration = 0

        construirChips()

        vistas.entradaBuscar.doAfterTextChanged {
            busqueda = it?.toString().orEmpty()
            aplicarFiltros()
        }

        vistas.refrescar.setColorSchemeColors(
            requireContext().getColor(R.color.nex_azul),
            requireContext().getColor(R.color.nex_verde_oscuro)
        )
        vistas.refrescar.setOnRefreshListener {
            mostrar(app.repositorioProductos.catalogoLocal())
            vistas.refrescar.isRefreshing = false
        }

        // Se pinta de inmediato lo que hay en la base SQL local y luego llega
        // la version fresca de Firestore.
        mostrar(app.repositorioProductos.catalogoLocal())
    }

    override fun onStart() {
        super.onStart()
        escucha = app.repositorioProductos.escucharCatalogo(
            alCambiar = { productos -> if (_vistas != null) mostrar(productos) },
            alFallar = { if (_vistas != null) vistas.root.avisar(getString(R.string.sin_conexion)) }
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
        Categorias.conTodas().forEach { nombre ->
            val chip = inflador.inflate(R.layout.item_chip_categoria, vistas.grupoCategorias, false) as Chip
            chip.text = nombre
            chip.isChecked = nombre == categoria
            chip.setOnClickListener {
                categoria = nombre
                aplicarFiltros()
            }
            vistas.grupoCategorias.addView(chip)
        }
    }

    private fun mostrar(productos: List<Producto>) {
        catalogo = productos
        aplicarFiltros()
    }

    private fun aplicarFiltros() {
        val filtrados = catalogo
            .filter { categoria == Categorias.TODAS || it.categoria == categoria }
            .filter { it.coincideCon(busqueda) }

        adaptador.submitList(filtrados)
        vistas.conteo.text = getString(R.string.productos_encontrados, filtrados.size)

        val vacio = filtrados.isEmpty()
        vistas.vacio.root.visibility = if (vacio) View.VISIBLE else View.GONE
        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_buscar)
        vistas.vacio.tituloVacio.text = getString(
            if (catalogo.isEmpty()) R.string.catalogo_vacio else R.string.sin_resultados
        )
        vistas.vacio.detalleVacio.text = if (catalogo.isEmpty()) {
            getString(R.string.lema)
        } else {
            getString(R.string.buscar_producto)
        }
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
