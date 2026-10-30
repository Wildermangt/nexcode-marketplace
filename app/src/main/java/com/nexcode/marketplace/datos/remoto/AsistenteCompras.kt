package com.nexcode.marketplace.datos.remoto

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.ServerException

/**
 * Único punto de contacto con Gemini a través de Firebase AI Logic.
 *
 * Se usa Firebase AI Logic (no la API REST directa) a propósito: la clave la
 * gestiona Firebase y NO queda compilada dentro del APK, de modo que no se puede
 * extraer con apktool. Además está dentro del nivel gratuito.
 *
 * Requisitos en la consola de Firebase del proyecto `nexcode-marketplace`:
 * habilitar "AI Logic" (botón Get started) con el backend de Gemini (Google AI).
 */
class AsistenteCompras {

    // Alias que Google mantiene apuntando al modelo vigente: los nombres fijos
    // (p. ej. gemini-2.5-flash) se retiran para proyectos nuevos y devuelven 404.
    private val modelo by lazy {
        Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-flash-lite-latest")
    }

    /**
     * Envía la pregunta del usuario junto con el catálogo real como contexto y
     * devuelve la respuesta del asistente.
     *
     * @param catalogo texto con los productos reales (nombre, categoría, precio, etc.)
     * @param pregunta lo que escribió el usuario
     * @return respuesta del asistente, nunca vacía
     * @throws AsistenteError con un mensaje claro para mostrar al usuario si algo falla
     */
    suspend fun responder(catalogo: String, pregunta: String): String {
        val instruccion = """
            Eres el asistente de compras de NEXCODE, una tienda de tecnología en Colombia.
            Responde únicamente con productos de este catálogo. Si lo que piden no está,
            dilo con claridad y NO inventes productos, precios ni referencias.
            Los precios están en pesos colombianos (COP). Sé breve, amable y directo.
            Responde en texto plano: el chat no muestra formato, así que no uses
            asteriscos, almohadillas ni viñetas de Markdown.

            CÓMO SE COMPRA EN LA APP (úsalo si preguntan por compras, pagos o pedidos):
            1. En Inicio o Catálogo, toca "Agregar al carrito" en el producto.
            2. Abre la pestaña Carrito, ajusta las cantidades con − y + y toca "Finalizar compra".
            3. El pedido queda registrado (se ve en Perfil > Mis pedidos) y el vendedor
               te escribe por la pestaña Chat para acordar el pago y la entrega.
            La app no cobra en línea ni pide datos de tarjeta.

            CATÁLOGO (nombre | marca | categoría | estado | precio COP | calificación | existencias):
            $catalogo

            PREGUNTA DEL CLIENTE:
            $pregunta
        """.trimIndent()

        try {
            val respuesta = modelo.generateContent(instruccion)
            val texto = respuesta.text?.trim().orEmpty()
            return texto.ifEmpty {
                "No encontré una respuesta. ¿Puedes reformular la pregunta?"
            }
        } catch (e: QuotaExceededException) {
            throw AsistenteError("El asistente alcanzó su límite por hoy. Intenta más tarde.")
        } catch (e: ServerException) {
            Log.w(ETIQUETA, "Gemini rechazó la solicitud", e)
            throw AsistenteError("El servicio de IA no está disponible en este momento.")
        } catch (e: Exception) {
            Log.w(ETIQUETA, "Fallo al contactar a Gemini", e)
            throw AsistenteError("No se pudo contactar al asistente. Verifica tu conexión.")
        }
    }

    private companion object {
        const val ETIQUETA = "AsistenteCompras"
    }
}

/** Error con un mensaje listo para mostrar al usuario. */
class AsistenteError(mensaje: String) : Exception(mensaje)
