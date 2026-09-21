package com.nexcode.marketplace.ui.comun

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nexcode.marketplace.NexcodeMarketplace
import com.nexcode.marketplace.R

/**
 * Atajos que se repiten en casi todas las pantallas: acceder al contenedor de
 * dependencias, avisar con un mensaje y respetar las barras del sistema.
 */

/** Contenedor de dependencias de la aplicacion. */
val Activity.app: NexcodeMarketplace get() = application as NexcodeMarketplace

val Fragment.app: NexcodeMarketplace get() = requireActivity().app

/** Aviso corto con el estilo de la marca. */
fun View.avisar(texto: String) {
    Snackbar.make(this, texto, Snackbar.LENGTH_LONG)
        .setBackgroundTint(context.getColor(R.color.nex_texto))
        .setTextColor(context.getColor(R.color.nex_texto_sobre_marca))
        .setActionTextColor(context.getColor(R.color.nex_verde))
        .show()
}

fun View.avisar(texto: String, accion: String, alPulsar: () -> Unit) {
    Snackbar.make(this, texto, Snackbar.LENGTH_LONG)
        .setBackgroundTint(context.getColor(R.color.nex_texto))
        .setTextColor(context.getColor(R.color.nex_texto_sobre_marca))
        .setActionTextColor(context.getColor(R.color.nex_verde))
        .setAction(accion) { alPulsar() }
        .show()
}

/**
 * Deja espacio para la barra de estado y la de navegacion.
 *
 * Desde Android 15 las aplicaciones se dibujan de borde a borde de forma
 * obligatoria; sin este relleno el encabezado quedaria debajo del reloj.
 */
fun View.ajustarASistema(arriba: Boolean = true, abajo: Boolean = false) {
    val rellenoArriba = paddingTop
    val rellenoAbajo = paddingBottom
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, ventana ->
        val barras = ventana.getInsets(WindowInsetsCompat.Type.systemBars())
        vista.setPadding(
            vista.paddingLeft,
            if (arriba) rellenoArriba + barras.top else vista.paddingTop,
            vista.paddingRight,
            if (abajo) rellenoAbajo + barras.bottom else vista.paddingBottom
        )
        ventana
    }
}

fun View.mostrar(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

fun Activity.ocultarTeclado() {
    val vista = currentFocus ?: View(this)
    val gestor = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    gestor.hideSoftInputFromWindow(vista.windowToken, 0)
}
