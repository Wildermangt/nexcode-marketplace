package com.nexcode.marketplace.ui.comun

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.nexcode.marketplace.R
import com.nexcode.marketplace.core.Animaciones

/**
 * Fila de cinco estrellas.
 *
 * Sirve para las dos cosas que pide la guia: mostrar el promedio de un producto
 * y dejar que el comprador califique de 1 a 5. En modo lectura las estrellas se
 * pintan segun el promedio redondeado; en modo edicion responden al toque y
 * avisan por [alCalificar].
 */
class BarraEstrellas @JvmOverloads constructor(
    contexto: Context,
    atributos: AttributeSet? = null,
    estiloPorDefecto: Int = 0
) : LinearLayout(contexto, atributos, estiloPorDefecto) {

    private val estrellas = ArrayList<ImageView>(TOTAL)

    /** Valor actual, de 0 a 5. */
    var valor: Int = 0
        private set

    /** Cuando es cierto, la barra responde al toque. */
    var editable: Boolean = false
        set(nuevo) {
            field = nuevo
            estrellas.forEach { it.isClickable = nuevo }
        }

    /** Se invoca con la nota elegida por el usuario. */
    var alCalificar: ((Int) -> Unit)? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL

        val atributosPropios = contexto.obtainStyledAttributes(
            atributos, R.styleable.BarraEstrellas, estiloPorDefecto, 0
        )
        val lado = atributosPropios.getDimensionPixelSize(
            R.styleable.BarraEstrellas_ladoEstrella, dp(16)
        )
        val separacion = atributosPropios.getDimensionPixelSize(
            R.styleable.BarraEstrellas_separacionEstrella, dp(2)
        )
        editable = atributosPropios.getBoolean(R.styleable.BarraEstrellas_editable, false)
        val inicial = atributosPropios.getInt(R.styleable.BarraEstrellas_valorInicial, 0)
        atributosPropios.recycle()

        repeat(TOTAL) { indice ->
            val estrella = ImageView(contexto).apply {
                layoutParams = LayoutParams(lado, lado).also { parametros ->
                    if (indice > 0) parametros.marginStart = separacion
                }
                setImageResource(R.drawable.ic_estrella_borde)
                setColorFilter(ContextCompat.getColor(contexto, R.color.nex_ambar))
                isClickable = editable
                setOnClickListener {
                    if (!editable) return@setOnClickListener
                    marcar(indice + 1)
                    Animaciones.latido(this)
                    alCalificar?.invoke(indice + 1)
                }
            }
            estrellas.add(estrella)
            addView(estrella)
        }
        marcar(inicial)
    }

    /** Pinta el promedio de un producto: 4,6 se ve como cinco estrellas llenas. */
    fun mostrarPromedio(promedio: Double) {
        marcar(Math.round(promedio).toInt().coerceIn(0, TOTAL))
    }

    /** Fija la nota sin avisar al oyente; se usa al abrir un voto ya guardado. */
    fun marcar(nuevoValor: Int) {
        valor = nuevoValor.coerceIn(0, TOTAL)
        estrellas.forEachIndexed { indice, estrella ->
            estrella.setImageResource(
                if (indice < valor) R.drawable.ic_estrella else R.drawable.ic_estrella_borde
            )
            estrella.setColorFilter(
                ContextCompat.getColor(
                    context,
                    if (indice < valor) R.color.nex_ambar else R.color.nex_texto_suave
                )
            )
        }
    }

    private fun dp(valor: Int): Int =
        (valor * resources.displayMetrics.density).toInt()

    private companion object {
        const val TOTAL = 5
    }
}
