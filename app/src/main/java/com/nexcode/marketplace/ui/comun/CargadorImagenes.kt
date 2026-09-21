package com.nexcode.marketplace.ui.comun

import android.net.Uri
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.nexcode.marketplace.R
import com.nexcode.marketplace.datos.RUTA_ASSETS

/**
 * Punto unico para pintar la fotografia de un producto.
 *
 * Una imagen puede venir de tres sitios distintos y la pantalla no tiene por
 * que saber de cual:
 *
 * <ul>
 *   <li>{@code https://...} — descargada de Firebase Storage;</li>
 *   <li>{@code asset://productos/x.jpg} — incluida en la aplicacion, que es lo
 *       que se usa cuando Storage aun no esta habilitado;</li>
 *   <li>{@code content://} o {@code file://} — la foto que el vendedor acaba de
 *       tomar o de elegir, todavia sin subir.</li>
 * </ul>
 */
object CargadorImagenes {

    private const val PREFIJO_ANDROID_ASSET = "file:///android_asset/"

    /**
     * Convierte la ruta guardada en el producto en algo que Glide sepa cargar.
     */
    private fun modelo(imagen: String): Any = when {
        imagen.isBlank() -> R.drawable.ic_imagen
        imagen.startsWith(RUTA_ASSETS) ->
            PREFIJO_ANDROID_ASSET + "productos/" + imagen.substringAfterLast('/')
        imagen.startsWith("http") -> imagen
        else -> Uri.parse(imagen)
    }

    /** Pinta la fotografia en la vista, con marcador mientras carga. */
    fun cargar(vista: ImageView, imagen: String) {
        val modelo = modelo(imagen)
        // Las fotos que viajan dentro de la aplicacion se decodifican al vuelo:
        // guardarlas en la cache de disco no ahorra nada y, al actualizar la
        // aplicacion, haria que se siguiera viendo la version anterior aunque
        // el archivo del catalogo ya hubiera cambiado.
        val esLocal = modelo is String && modelo.startsWith(PREFIJO_ANDROID_ASSET)
        Glide.with(vista)
            .load(modelo)
            .diskCacheStrategy(
                if (esLocal) DiskCacheStrategy.NONE else DiskCacheStrategy.AUTOMATIC
            )
            .placeholder(R.drawable.fondo_redondeado_claro)
            .error(R.drawable.ic_imagen)
            .centerCrop()
            .into(vista)
    }

    /** Igual que [cargar], pero para una imagen recien elegida por el vendedor. */
    fun cargar(vista: ImageView, uri: Uri) {
        Glide.with(vista)
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .centerCrop()
            .into(vista)
    }
}
