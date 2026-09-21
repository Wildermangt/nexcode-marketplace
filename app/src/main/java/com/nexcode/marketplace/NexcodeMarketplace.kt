package com.nexcode.marketplace

import android.app.Application
import com.nexcode.marketplace.datos.RepositorioCarrito
import com.nexcode.marketplace.datos.RepositorioChat
import com.nexcode.marketplace.datos.RepositorioProductos
import com.nexcode.marketplace.datos.RepositorioUsuarios
import com.nexcode.marketplace.datos.SembradorCatalogo
import com.nexcode.marketplace.datos.local.AlmacenLocal
import com.nexcode.marketplace.dominio.Usuario

/**
 * Clase de aplicacion y contenedor de dependencias.
 *
 * No se usa Hilt a proposito: con un contenedor manual queda a la vista quien
 * depende de quien, que es lo que hay que explicar en el informe. Las
 * pantallas obtienen los repositorios con {@code (application as
 * NexcodeMarketplace).repositorioProductos}.
 */
class NexcodeMarketplace : Application() {

    /** Base SQL local: espejo del catalogo, carrito, pedidos y mensajes. */
    val almacenLocal: AlmacenLocal by lazy { AlmacenLocal(this) }

    val repositorioUsuarios: RepositorioUsuarios by lazy {
        RepositorioUsuarios(almacenLocal)
    }

    val repositorioProductos: RepositorioProductos by lazy {
        RepositorioProductos(almacenLocal)
    }

    val repositorioCarrito: RepositorioCarrito by lazy {
        RepositorioCarrito(almacenLocal, repositorioProductos)
    }

    val repositorioChat: RepositorioChat by lazy {
        RepositorioChat(almacenLocal)
    }

    val sembrador: SembradorCatalogo by lazy {
        SembradorCatalogo(this, repositorioProductos)
    }

    /**
     * Ficha del usuario con sesion abierta. Se guarda aqui para que todas las
     * pantallas la compartan sin volver a consultarla a Firestore.
     */
    @Volatile
    var usuario: Usuario? = null

    val esAdministrador: Boolean get() = usuario?.esAdministrador == true
}
