# NEXCODE Marketplace — Plan del agente de inteligencia artificial

Plan para incorporar a Marketplace un agente de IA y cerrar la sección 8 de la rúbrica (hoy
en ❌, es el mayor hueco de nota del proyecto). Sigue el mismo enfoque que ya funciona en
HuellitasPlus: **Gemini a través de Firebase AI Logic**, que tiene nivel gratuito y **no
pone la clave dentro del APK** (la gestiona Firebase), respetando la regla de "todo gratis".

## 1. Propósito del agente

**Asistente de compras de NEXCODE.** Ayuda al comprador a encontrar productos del catálogo y
a resolver dudas de compra en lenguaje natural. Ejemplos:

- "Necesito una impresora para la casa por menos de 400 mil" → recomienda referencias del
  catálogo que cumplan.
- "¿Cuál laptop tiene mejor calificación?" → responde con los datos reales de los productos.
- "¿Cómo agrego algo al carrito?" → explica el flujo de la app.

No inventa productos: se le entrega el catálogo real como contexto y solo recomienda de ahí.

## 2. Cómo cubre los 8 criterios de la rúbrica (sección 8)

| Criterio | Cómo se cumple |
|---|---|
| Propósito documentado | Este documento (sección 1). |
| Modelo o servicio de IA integrado | Gemini vía Firebase AI Logic (`com.google.firebase:firebase-ai`). |
| Recibe entradas y responde | Pantalla de chat del asistente (reutiliza la UI de chat existente). |
| Integrado con las funcionalidades | Se le pasa el catálogo real (`RepositorioProductos`) y enlaza a la ficha del producto. |
| Validado con escenarios de prueba | Plan de pruebas de la sección 7. |
| Controla respuestas incorrectas y errores | Manejo de excepciones de la sección 6. |
| Protege las claves API | Firebase AI Logic: la clave no viaja en el APK (sección 5). |
| Informa que se habla con una IA | Aviso visible en la pantalla (sección 5). |

## 3. Integración con la arquitectura actual

Marketplace usa un contenedor manual de dependencias (`NexcodeMarketplace.kt`) y un objeto
`ServicioFirebase`. El agente encaja así, sin romper nada:

```
datos/remoto/AsistenteCompras.kt     ← nueva clase: habla con Firebase AI Logic
datos/RepositorioAsistente.kt        ← arma el contexto (catálogo) y llama a AsistenteCompras
ui/asistente/AsistenteActivity.kt    ← pantalla de chat del asistente (reutiliza
ui/asistente/AsistenteViewModel.kt     AdaptadorMensajes y los layouts de ui/chat)
```

- Se registra `AsistenteCompras` y `RepositorioAsistente` en el contenedor manual, junto a
  los repositorios que ya existen.
- Se añade una entrada "Asistente" en la navegación (por ejemplo, un botón en Inicio o en el
  catálogo) que abre `AsistenteActivity`.
- El `RepositorioAsistente` toma los productos de `RepositorioProductos` y los resume (nombre,
  categoría, precio, calificación, stock) para dárselos al modelo como contexto.

## 4. Dependencia y configuración (gratis)

En `app/build.gradle` (ya se usa la BOM de Firebase):

```gradle
// Firebase AI Logic (Gemini) — misma que usa HuellitasPlus
implementation 'com.google.firebase:firebase-ai'
implementation 'com.google.guava:guava:33.3.1-android'        // API de futuros desde Java/Kotlin
implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.8.1' // si se usa con corrutinas
```

No se agrega ninguna clave en `local.properties` ni en `BuildConfig`: Firebase AI Logic usa
la configuración de `google-services.json` y enruta por el backend de Google. En la consola
de Firebase hay que **habilitar Firebase AI Logic** para el proyecto `nexcode-marketplace`
(botón "Get started" en la sección AI Logic; queda en nivel gratuito).

## 5. Esbozo de código

### AsistenteCompras.kt (capa de datos)

```kotlin
package com.nexcode.marketplace.datos.remoto

import com.google.firebase.ai.FirebaseAI
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.QuotaExceededException
import com.google.firebase.ai.type.RequestTimeoutException
import com.google.firebase.ai.type.ServerException

/** Único punto de contacto con Gemini (Firebase AI Logic). La clave la gestiona Firebase. */
class AsistenteCompras {

    private val modelo: GenerativeModel = FirebaseAI
        .getInstance(GenerativeBackend.googleAI())
        .generativeModel("gemini-2.5-flash")   // modelo liviano, apto para free tier

    /**
     * @param catalogo texto con los productos reales (nombre, categoría, precio, rating, stock)
     * @param pregunta lo que escribió el usuario
     * @return respuesta del asistente
     * @throws AsistenteError con un mensaje claro si algo falla
     */
    suspend fun responder(catalogo: String, pregunta: String): String {
        val instruccion = """
            Eres el asistente de compras de NEXCODE, una tienda de tecnología.
            Responde solo con productos de este catálogo; si algo no está, dilo.
            No inventes precios ni referencias. Sé breve y claro.

            CATÁLOGO:
            $catalogo

            PREGUNTA DEL CLIENTE:
            $pregunta
        """.trimIndent()
        try {
            val respuesta = modelo.generateContent(content { text(instruccion) })
            return respuesta.text?.trim().orEmpty()
                .ifEmpty { "No encontré una respuesta. ¿Puedes reformular la pregunta?" }
        } catch (e: QuotaExceededException) {
            throw AsistenteError("El asistente alcanzó su límite por hoy. Intenta más tarde.")
        } catch (e: RequestTimeoutException) {
            throw AsistenteError("La respuesta tardó demasiado. Revisa tu conexión e intenta otra vez.")
        } catch (e: ServerException) {
            throw AsistenteError("El servicio de IA no está disponible en este momento.")
        } catch (e: Exception) {
            throw AsistenteError("No se pudo contactar al asistente. Verifica tu conexión.")
        }
    }
}

class AsistenteError(mensaje: String) : Exception(mensaje)
```

### Aviso de IA en la pantalla (criterio "informa al usuario")

En `AsistenteActivity` (o su layout), un texto fijo y visible, por ejemplo una banda bajo el
título:

```
🤖 Estás hablando con un asistente de inteligencia artificial.
Sus respuestas pueden contener errores; verifica los datos del producto antes de comprar.
```

## 6. Manejo de errores (criterio "controla respuestas incorrectas y errores")

| Situación | Qué hace el agente |
|---|---|
| Sin conexión / timeout | Mensaje: "Revisa tu conexión e intenta otra vez". |
| Cuota agotada (`QuotaExceededException`) | Mensaje: "El asistente alcanzó su límite por hoy". |
| Servicio caído (`ServerException`) | Mensaje: "El servicio de IA no está disponible". |
| Respuesta vacía | "No encontré una respuesta. ¿Puedes reformular?". |
| Pregunta fuera de tema | La instrucción del sistema le pide responder solo sobre el catálogo/compras. |

El `ViewModel` captura `AsistenteError` y muestra el mensaje en la UI sin cerrar la pantalla.

## 7. Plan de pruebas del agente (criterio "validado con escenarios")

Guardar como `docs/evidencias-pruebas/agente/` con captura de cada caso.

| # | Entrada | Resultado esperado |
|---|---|---|
| T1 | "Recomiéndame una impresora económica" | Sugiere impresoras del catálogo ordenadas por precio |
| T2 | "¿Cuál producto tiene mejor calificación?" | Nombra el de mayor rating con su valor real |
| T3 | "Quiero un televisor" (no hay en catálogo) | Avisa que no hay esa categoría, no inventa |
| T4 | "¿Cómo pago?" | Explica el flujo del carrito de la app |
| T5 | Pregunta sin relación ("¿qué día es hoy?") | Redirige a temas de compras |
| T6 | Modo avión / sin red | Muestra el mensaje de error de conexión, no se cierra |
| T7 | Mensaje vacío | No llama al modelo; pide escribir algo |

## 8. Trabajo en Azure Boards (Sprint 2)

Sugerencia de PBI y Tasks para dejar trazabilidad:

- **PBI**: "Como comprador quiero un asistente de IA que me recomiende productos".
  - Task: Habilitar Firebase AI Logic y agregar la dependencia `firebase-ai`.
  - Task: Implementar `AsistenteCompras` y `RepositorioAsistente` (contexto del catálogo).
  - Task: Pantalla `AsistenteActivity` reutilizando la UI de chat + aviso de IA.
  - Task: Manejo de errores y mensajes al usuario.
  - Task: Ejecutar el plan de pruebas (T1–T7) y adjuntar evidencias.

## 9. Esfuerzo estimado

Medio día de desarrollo: la UI de chat y los repositorios ya existen; lo nuevo es la clase
`AsistenteCompras`, el contexto del catálogo y una pantalla sencilla. Sin costo: Firebase AI
Logic en nivel gratuito y sin clave en el APK.

## 10. Verificación final contra la rúbrica

Al terminar, la sección 8 pasaría de 8 criterios en ❌ a ✅ (o 🟡 en los que falte evidencia),
lo que sube la nota estimada de Marketplace de ≈3,0–3,8 a un rango comparable al de
HuellitasPlus y Control de Gastos.
