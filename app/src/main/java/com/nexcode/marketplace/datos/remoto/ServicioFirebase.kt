package com.nexcode.marketplace.datos.remoto

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.MemoryCacheSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.storage.FirebaseStorage

/**
 * Punto unico de acceso a Firebase.
 *
 * Concentrar aqui las tres instancias evita que cada pantalla llame por su
 * cuenta a {@code getInstance()} y deja en un solo lugar la configuracion de
 * la cache de Firestore.
 */
object ServicioFirebase {

    val autenticacion: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val almacenamiento: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    val base: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance().apply {
            // La cache persistente es la que permite abrir el catalogo sin red
            // mientras la base SQL local se pone al dia.
            firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setLocalCacheSettings(
                    PersistentCacheSettings.newBuilder()
                        .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                        .build()
                )
                .build()
        }
    }

    // --- Nombres de las colecciones, tal como los define la guia ---
    const val USUARIOS = "usuarios"
    const val PRODUCTOS = "productos"
    const val CALIFICACIONES = "calificaciones"
    const val PEDIDOS = "pedidos"
    const val CHATS = "chats"
    const val MENSAJES = "mensajes"

    /** Carpeta de Storage donde viven las fotografias de los productos. */
    const val CARPETA_FOTOS = "productos"

    val uidActual: String? get() = autenticacion.currentUser?.uid

    val haySesion: Boolean get() = autenticacion.currentUser != null

    fun cerrarSesion() = autenticacion.signOut()

    /**
     * Cache en memoria: se usa unicamente en pruebas instrumentadas, donde no
     * interesa dejar rastro en el disco del emulador.
     */
    fun usarCacheEnMemoria() {
        base.firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(MemoryCacheSettings.newBuilder().build())
            .build()
    }
}
