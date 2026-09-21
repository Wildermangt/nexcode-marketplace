package com.nexcode.marketplace.datos

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.nexcode.marketplace.datos.local.AlmacenLocal
import com.nexcode.marketplace.datos.remoto.ServicioFirebase
import com.nexcode.marketplace.dominio.TipoUsuario
import com.nexcode.marketplace.dominio.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Registro, inicio de sesion y ficha del usuario.
 *
 * Firebase Authentication guarda la credencial; el documento
 * {@code usuarios/{uid}} de Firestore guarda el nombre y el tipo de usuario,
 * que es lo que decide si la persona ve la tienda o el panel del vendedor.
 * Una copia queda en la base SQL local para poder pintar el perfil sin red.
 */
class RepositorioUsuarios(private val local: AlmacenLocal) {

    private val autenticacion get() = ServicioFirebase.autenticacion
    private val base get() = ServicioFirebase.base

    val haySesion: Boolean get() = ServicioFirebase.haySesion

    /** Crea la credencial y su ficha en Firestore. */
    suspend fun registrar(
        nombre: String,
        correo: String,
        contrasena: String,
        tipo: TipoUsuario
    ): Usuario = withContext(Dispatchers.IO) {
        val credencial = try {
            autenticacion.createUserWithEmailAndPassword(correo.trim(), contrasena).await()
        } catch (e: Exception) {
            throw ErrorDeAcceso(mensajeDe(e), e)
        }
        val uid = credencial.user?.uid
            ?: throw ErrorDeAcceso("No se pudo crear la cuenta")

        val usuario = Usuario(
            uid = uid,
            nombre = nombre.trim(),
            correo = correo.trim(),
            tipoUsuario = tipo,
            fechaRegistro = System.currentTimeMillis()
        )

        // El nombre se guarda tambien en la credencial: si la escritura de
        // Firestore no llega, es lo unico con lo que se puede rehacer la ficha
        // en el siguiente inicio de sesion.
        try {
            credencial.user?.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(usuario.nombre).build()
            )?.await()
        } catch (e: Exception) {
            // El nombre es un adorno de la credencial; la ficha manda.
        }

        local.guardarUsuario(usuario)

        try {
            base.collection(ServicioFirebase.USUARIOS).document(uid).set(usuario.aMapa()).await()
        } catch (e: Exception) {
            // La credencial ya existe, asi que no se puede repetir el registro:
            // se avisa que entre de nuevo, que es cuando la ficha se rehace.
            throw ErrorDeAcceso(
                "Tu cuenta se creo, pero no se pudo guardar tu perfil. " +
                    "Revisa la conexion y entra con el mismo correo y contrasena.",
                e
            )
        }
        usuario
    }

    suspend fun entrar(correo: String, contrasena: String): Usuario =
        withContext(Dispatchers.IO) {
            try {
                autenticacion.signInWithEmailAndPassword(correo.trim(), contrasena).await()
            } catch (e: Exception) {
                throw ErrorDeAcceso(mensajeDe(e), e)
            }
            perfilActual() ?: throw ErrorDeAcceso("No se encontro la ficha del usuario")
        }

    /**
     * Ficha del usuario con sesion abierta. Si Firestore no responde se
     * devuelve la copia guardada en SQLite.
     */
    suspend fun perfilActual(): Usuario? = withContext(Dispatchers.IO) {
        val uid = ServicioFirebase.uidActual ?: return@withContext null
        val documento = try {
            base.collection(ServicioFirebase.USUARIOS).document(uid).get().await()
        } catch (e: Exception) {
            // Sin red la copia de SQLite es lo unico disponible; no se rehace
            // nada aqui para no pisar la ficha que si existe en el servidor.
            return@withContext local.usuario(uid)
        }

        if (documento.exists()) {
            val usuario = Usuario(
                uid = uid,
                nombre = documento.getString("nombre").orEmpty(),
                correo = documento.getString("correo")
                    ?: autenticacion.currentUser?.email.orEmpty(),
                tipoUsuario = TipoUsuario.desde(documento.getString("tipoUsuario")),
                fechaRegistro = documento.getLong("fechaRegistro") ?: 0L
            )
            local.guardarUsuario(usuario)
            usuario
        } else {
            rehacerFicha(uid)
        }
    }

    /**
     * Vuelve a crear la ficha de una cuenta que quedo a medias: la credencial
     * existe en Authentication pero su documento de Firestore nunca se escribio
     * (registro interrumpido por la red). Sin esto la persona no puede entrar
     * nunca mas con ese correo, porque la credencial ya esta tomada.
     *
     * Se parte de la copia local si la hay y, si no, de lo que guarda la propia
     * credencial. El tipo vuelve a ser comprador: nadie gana el panel de
     * vendedor por haber perdido su ficha.
     */
    private suspend fun rehacerFicha(uid: String): Usuario? {
        val credencial = autenticacion.currentUser ?: return null
        val copia = local.usuario(uid)
        val usuario = copia ?: Usuario(
            uid = uid,
            nombre = credencial.displayName?.trim().orEmpty()
                .ifBlank { credencial.email.orEmpty().substringBefore('@') },
            correo = credencial.email.orEmpty(),
            tipoUsuario = TipoUsuario.COMPRADOR,
            fechaRegistro = System.currentTimeMillis()
        )
        return try {
            base.collection(ServicioFirebase.USUARIOS).document(uid).set(usuario.aMapa()).await()
            local.guardarUsuario(usuario)
            usuario
        } catch (e: Exception) {
            // Si tampoco ahora hay red, al menos se entra con la copia local.
            copia
        }
    }

    suspend fun actualizarNombre(nombre: String) = withContext(Dispatchers.IO) {
        val uid = ServicioFirebase.uidActual ?: return@withContext
        base.collection(ServicioFirebase.USUARIOS).document(uid)
            .update("nombre", nombre.trim()).await()
        local.usuario(uid)?.let { local.guardarUsuario(it.copy(nombre = nombre.trim())) }
    }

    fun cerrarSesion() {
        ServicioFirebase.uidActual?.let { local.limpiarSesion(it) }
        ServicioFirebase.cerrarSesion()
    }

    private fun Usuario.aMapa() = mapOf(
        "nombre" to nombre,
        "correo" to correo,
        "tipoUsuario" to tipoUsuario.clave,
        "fechaRegistro" to fechaRegistro
    )

    /**
     * Traduce los errores de Firebase a frases que el comprador entienda.
     * Sin esto la pantalla mostraria textos en ingles con codigos internos.
     */
    private fun mensajeDe(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException ->
            "La contrasena es demasiado debil: usa al menos 6 caracteres"
        is FirebaseAuthUserCollisionException ->
            "Ya existe una cuenta con ese correo"
        is FirebaseAuthInvalidCredentialsException ->
            "El correo o la contrasena no son correctos"
        is FirebaseAuthInvalidUserException ->
            "No existe una cuenta con ese correo"
        else -> e.message ?: "No se pudo completar la operacion"
    }
}

/** Error de acceso ya traducido, listo para mostrarse en pantalla. */
class ErrorDeAcceso(mensaje: String, causa: Throwable? = null) : Exception(mensaje, causa)
