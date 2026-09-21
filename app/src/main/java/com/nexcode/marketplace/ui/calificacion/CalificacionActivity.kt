package com.nexcode.marketplace.ui.calificacion

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones
import com.nexcode.marketplace.databinding.ActivityCalificacionBinding
import com.nexcode.marketplace.dominio.Calificacion
import com.nexcode.marketplace.ui.comun.ajustarASistema
import com.nexcode.marketplace.ui.comun.app
import com.nexcode.marketplace.ui.comun.avisar
import com.nexcode.marketplace.ui.comun.ocultarTeclado
import com.nexcode.marketplace.ui.detalle.AdaptadorOpiniones
import kotlinx.coroutines.launch

/**
 * Calificacion de un producto de 1 a 5 estrellas.
 *
 * El voto se guarda en {@code productos/{id}/calificaciones/{uid}}: al usar el
 * UID como identificador del documento, una persona actualiza su opinion en
 * lugar de agregar otra. Despues el repositorio recalcula el promedio del
 * producto, que es lo que se ve en el catalogo.
 */
class CalificacionActivity : AppCompatActivity() {

    private lateinit var vistas: ActivityCalificacionBinding
    private lateinit var opiniones: AdaptadorOpiniones

    private val productoId by lazy { intent.getStringExtra(EXTRA_ID).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vistas = ActivityCalificacionBinding.inflate(layoutInflater)
        setContentView(vistas.root)
        vistas.encabezado.ajustarASistema()

        vistas.nombreProducto.text = intent.getStringExtra(EXTRA_NOMBRE).orEmpty()
        vistas.atras.setOnClickListener { finish() }

        opiniones = AdaptadorOpiniones()
        vistas.listaOpiniones.layoutManager = LinearLayoutManager(this)
        vistas.listaOpiniones.adapter = opiniones

        vistas.estrellas.alCalificar = { nota -> vistas.etiquetaNota.text = etiqueta(nota) }
        vistas.enviar.setOnClickListener { enviar() }

        vistas.vacio.iconoVacio.setImageResource(R.drawable.ic_estrella_borde)
        vistas.vacio.tituloVacio.text = getString(R.string.sin_opiniones)
        vistas.vacio.detalleVacio.text = getString(R.string.calificacion_detalle)

        cargar()
    }

    private fun cargar() {
        val usuario = app.usuario ?: return
        lifecycleScope.launch {
            // Si esta persona ya habia calificado, se muestra su voto anterior.
            app.repositorioProductos.miOpinion(productoId, usuario.uid)?.let { mia ->
                vistas.estrellas.marcar(mia.estrellas)
                vistas.etiquetaNota.text = etiqueta(mia.estrellas)
                vistas.entradaComentario.setText(mia.comentario)
            }
            val lista = app.repositorioProductos.opiniones(productoId)
            opiniones.submitList(lista)
            vistas.vacio.root.visibility = if (lista.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun enviar() {
        val usuario = app.usuario ?: return
        val nota = vistas.estrellas.valor
        if (nota == 0) {
            Animaciones.sacudir(vistas.estrellas)
            vistas.root.avisar(getString(R.string.calificacion_detalle))
            return
        }

        ocultarTeclado()
        vistas.enviar.isEnabled = false
        lifecycleScope.launch {
            try {
                app.repositorioProductos.calificar(
                    productoId,
                    Calificacion(
                        usuarioId = usuario.uid,
                        usuarioNombre = usuario.nombre,
                        estrellas = nota,
                        comentario = vistas.entradaComentario.text.toString().trim(),
                        fecha = System.currentTimeMillis()
                    )
                )
                vistas.root.avisar(getString(R.string.gracias_calificacion))
                cargar()
            } catch (e: Exception) {
                vistas.root.avisar(e.message ?: getString(R.string.error_generico))
            } finally {
                vistas.enviar.isEnabled = true
            }
        }
    }

    /** Texto que acompana a la nota elegida, para que el voto tenga sentido. */
    private fun etiqueta(nota: Int) = when (nota) {
        1 -> "Muy malo"
        2 -> "Malo"
        3 -> "Aceptable"
        4 -> "Muy bueno"
        else -> "Excelente"
    }

    companion object {
        private const val EXTRA_ID = "productoId"
        private const val EXTRA_NOMBRE = "productoNombre"

        fun abrir(contexto: Context, productoId: String, nombre: String) {
            contexto.startActivity(
                Intent(contexto, CalificacionActivity::class.java)
                    .putExtra(EXTRA_ID, productoId)
                    .putExtra(EXTRA_NOMBRE, nombre)
            )
        }
    }
}
