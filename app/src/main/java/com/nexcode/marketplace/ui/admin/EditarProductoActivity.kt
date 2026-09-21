package com.nexcode.marketplace.ui.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputLayout
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.core.Imagenes
import com.nexcode.marketplace.databinding.ActivityEditarProductoBinding
import com.nexcode.marketplace.dominio.Categorias
import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.Producto
import com.nexcode.marketplace.ui.camara.CamaraActivity
import com.nexcode.marketplace.ui.comun.CargadorImagenes
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.comun.ocultarTeclado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Registro y edicion de un producto.
 *
 * El mismo formulario sirve para crear y para modificar: si llega un
 * identificador se cargan los datos existentes y el boton guarda encima. La
 * fotografia puede tomarse con la camara del dispositivo o elegirse de la
 * galeria; en ambos casos se comprime antes de subirla a Firebase Storage.
 */
class EditarProductoActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityEditarProductoBinding

    private var productoActual: Producto? = null

    /** Foto recien elegida y todavia sin subir. */
    private var fotoNueva: Uri? = null

    private var categoria: String = Categorias.LISTA.first()

    private val abrirCamara = registerForActivityResult(
        CamaraActivity.TomarFotoDeProducto()
    ) { uri -> uri?.let { usarFoto(it) } }

    // El selector de fotos del sistema no necesita permiso de lectura.
    private val abrirGaleria = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { usarFoto(it) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityEditarProductoBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        vistas.atras.setOnClickListener { finish() }
        vistas.botonCamara.setOnClickListener { abrirCamara.launch(Unit) }
        vistas.botonGaleria.setOnClickListener {
            abrirGaleria.launch(
                PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    .build()
            )
        }
        vistas.marcoFoto.setOnClickListener { abrirCamara.launch(Unit) }
        vistas.guardar.setOnClickListener { guardar() }

        construirChips()

        val id = intent.getStringExtra(EXTRA_ID)
        if (id.isNullOrBlank()) {
            vistas.titulo.text = getString(R.string.nuevo_producto)
        } else {
            vistas.titulo.text = getString(R.string.editar_producto)
            cargar(id)
        }
    }

    private fun construirChips() {
        val inflador = LayoutInflater.from(this)
        Categorias.LISTA.forEach { nombre ->
            val chip = inflador.inflate(
                R.layout.item_chip_categoria, vistas.grupoCategorias, false
            ) as Chip
            chip.text = nombre
            chip.isChecked = nombre == categoria
            chip.setOnClickListener { categoria = nombre }
            vistas.grupoCategorias.addView(chip)
        }
    }

    private fun cargar(id: String) {
        lifecycleScope.launch {
            val producto = app.repositorioProductos.obtener(id) ?: return@launch
            productoActual = producto

            vistas.entradaNombre.setText(producto.nombre)
            vistas.entradaDescripcion.setText(producto.descripcion)
            vistas.entradaPrecio.setText(producto.precio.toLong().toString())
            vistas.entradaCantidad.setText(producto.cantidad.toString())
            vistas.entradaMarca.setText(producto.marca)
            vistas.entradaReferencia.setText(producto.referencia)

            vistas.grupoEstado.check(
                if (producto.estado == EstadoProducto.NUEVO) {
                    R.id.estadoNuevo
                } else {
                    R.id.estadoUsado
                }
            )

            categoria = producto.categoria.ifBlank { Categorias.LISTA.first() }
            marcarCategoria(categoria)

            if (producto.imagen.isNotBlank()) {
                CargadorImagenes.cargar(vistas.foto, producto.imagen)
                vistas.foto.visibility = View.VISIBLE
                vistas.marcadorFoto.visibility = View.GONE
            }
        }
    }

    private fun marcarCategoria(nombre: String) {
        for (indice in 0 until vistas.grupoCategorias.childCount) {
            val chip = vistas.grupoCategorias.getChildAt(indice) as Chip
            chip.isChecked = chip.text.toString() == nombre
        }
    }

    private fun usarFoto(uri: Uri) {
        fotoNueva = uri
        CargadorImagenes.cargar(vistas.foto, uri)
        vistas.foto.visibility = View.VISIBLE
        vistas.marcadorFoto.visibility = View.GONE
        Animaciones.aparecerConRebote(vistas.foto, 0)
    }

    // -----------------------------------------------------------------------
    //  Guardado
    // -----------------------------------------------------------------------

    private fun guardar() {
        ocultarTeclado()
        if (!validar()) return

        val vendedor = app.usuario ?: return
        val anterior = productoActual

        val producto = Producto(
            id = anterior?.id.orEmpty(),
            nombre = vistas.entradaNombre.text.toString().trim(),
            descripcion = vistas.entradaDescripcion.text.toString().trim(),
            precio = vistas.entradaPrecio.text.toString().trim().toDouble(),
            categoria = categoria,
            estado = if (vistas.grupoEstado.checkedButtonId == R.id.estadoUsado) {
                EstadoProducto.USADO
            } else {
                EstadoProducto.NUEVO
            },
            cantidad = vistas.entradaCantidad.text.toString().trim().toInt(),
            imagen = anterior?.imagen.orEmpty(),
            marca = vistas.entradaMarca.text.toString().trim(),
            referencia = vistas.entradaReferencia.text.toString().trim(),
            vendedorId = anterior?.vendedorId?.ifBlank { vendedor.uid } ?: vendedor.uid,
            vendedorNombre = anterior?.vendedorNombre?.ifBlank { vendedor.nombre }
                ?: vendedor.nombre,
            calificacion = anterior?.calificacion ?: 0.0,
            votos = anterior?.votos ?: 0,
            vendidos = anterior?.vendidos ?: 0,
            fechaRegistro = anterior?.fechaRegistro ?: 0L
        )

        ocupado(true)
        lifecycleScope.launch {
            try {
                // La compresion se hace fuera del hilo principal: una foto de
                // camara puede pesar varios megabytes.
                val bytes = fotoNueva?.let { uri ->
                    withContext(Dispatchers.IO) { Imagenes.comprimir(contentResolver, uri) }
                }
                app.repositorioProductos.guardar(producto, bytes)
                vistas.root.avisar(getString(R.string.producto_guardado))
                finish()
            } catch (e: Exception) {
                ocupado(false)
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            }
        }
    }

    private fun validar(): Boolean {
        limpiarErrores()
        var valido = true

        if (vistas.entradaNombre.text.isNullOrBlank()) {
            valido = marcar(vistas.campoNombre)
        }
        if (vistas.entradaDescripcion.text.isNullOrBlank()) {
            valido = marcar(vistas.campoDescripcion)
        }
        val precio = vistas.entradaPrecio.text.toString().trim().toDoubleOrNull()
        if (precio == null || precio <= 0.0) {
            vistas.campoPrecio.error = getString(R.string.numero_invalido)
            valido = false
        }
        val cantidad = vistas.entradaCantidad.text.toString().trim().toIntOrNull()
        if (cantidad == null || cantidad < 0) {
            vistas.campoCantidad.error = getString(R.string.numero_invalido)
            valido = false
        }

        // Una ficha sin fotografia no sirve en un catalogo.
        if (fotoNueva == null && productoActual?.imagen.isNullOrBlank()) {
            vistas.root.avisar(getString(R.string.foto_requerida))
            Animaciones.sacudir(vistas.marcoFoto)
            valido = false
        }

        return valido
    }

    private fun marcar(campo: TextInputLayout): Boolean {
        campo.error = getString(R.string.campo_obligatorio)
        return false
    }

    private fun limpiarErrores() {
        listOf(
            vistas.campoNombre, vistas.campoDescripcion,
            vistas.campoPrecio, vistas.campoCantidad
        ).forEach { it.error = null }
    }

    private fun ocupado(activo: Boolean) {
        vistas.progreso.visibility = if (activo) View.VISIBLE else View.GONE
        vistas.guardar.isEnabled = !activo
    }

    companion object {
        private const val EXTRA_ID = "productoId"

        /** @param productoId null para registrar uno nuevo */
        fun abrir(contexto: Context, productoId: String?) {
            contexto.startActivity(
                Intent(contexto, EditarProductoActivity::class.java)
                    .putExtra(EXTRA_ID, productoId)
            )
        }
    }
}
