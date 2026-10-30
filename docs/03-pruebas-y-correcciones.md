# NEXCODE Marketplace · Pruebas y correcciones

## 1. Pruebas unitarias

Se ejecutan sin emulador con `./gradlew testDebugUnitTest` (JUnit 4). Resultado del 28 de octubre de 2026: **23 pruebas, 0 fallos**.

| Clase de prueba | Pruebas | Qué verifica | Requisitos |
|---|---|---|---|
| `dominio/ModelosTest` | 12 | Buscador sin tildes ni mayúsculas por nombre, marca, referencia, categoría y descripción; existencias; subtotales del carrito y del pedido; un tipo de usuario desconocido queda como comprador; iniciales del perfil | RF-04, RF-05, RF-07, RF-08, RNF-01 |
| `datos/CatalogoMayoristaTest` | 6 | Las 20 referencias son únicas, el precio es costo + 30 % redondeado a la centena, las categorías son válidas y la semilla se convierte bien en producto | RF-15 |
| `core/FormatoTest` | 5 | Moneda colombiana con punto de miles, abreviaturas M y K, número de pedido legible | RF-04, RF-09, RF-14 |

## 2. Casos de uso para probar en el dispositivo

Plan de prueba manual para la sustentación. Al ejecutarlo, anote la fecha y el resultado obtenido de cada caso.

| Caso | Pasos | Resultado esperado |
|---|---|---|
| Registro de vendedor | Registrarse con «Quiero vender» activado | Entra al panel del vendedor y se carga el catálogo inicial |
| Compra | Comprador agrega dos productos, cambia cantidades y finaliza | Se crea el pedido, se descuentan existencias y el carrito queda vacío |
| Existencias | Intentar subir la cantidad por encima de lo disponible | La app avisa cuántas unidades hay y no lo permite |
| Calificación | Calificar un producto con 4 estrellas | El promedio y los votos del producto se actualizan |
| Chat | Comprador escribe y vendedor responde | Los mensajes llegan en tiempo real a ambos |
| Sin red | Abrir la app en modo avión | El catálogo, los pedidos y los mensajes se ven desde SQLite |

## 3. Correcciones de la revisión del 28 de octubre de 2026

| # | Hallazgo | Riesgo | Corrección | Verificación |
|---|---|---|---|---|
| 1 | `android:allowBackup="true"` y las reglas de respaldo solo excluían la sesión: la base SQLite con pedidos, mensajes y datos de usuarios se copiaba en los respaldos de Google y en la transferencia entre teléfonos | Medio: datos personales fuera del dispositivo | `allowBackup="false"` y exclusión del dominio `database` en `reglas_respaldo.xml` y `reglas_extraccion.xml` | La app compila (`assembleDebug`) y las 23 pruebas siguen en verde |
| 2 | El proyecto no tenía pruebas automatizadas | Medio: un cambio podía romper el precio o el buscador sin que nadie lo notara | Se agregaron las 23 pruebas de la sección 1 | `testDebugUnitTest` en verde |
| 3 | Una búsqueda de claves en el repositorio marcó `AIza…` en `DESARROLLO DE GUIA MAKE/INSTRUCTIVO.html` | Ninguno | Se revisó: es parte de una imagen codificada en base64, no una clave de API | No requiere cambios |
| 4 | Las contraseñas del almacén de claves estaban escritas en `app/build.gradle` (y quedaron en el historial de git) | Alto: con el `.jks`, cualquiera podría firmar una APK falsa como si fuera de NEXCODE | Las credenciales se leen de `keystore.properties` (en `.gitignore`); sin ese archivo se compila debug pero no release | `assembleRelease` firma con la misma huella SHA-256 (`86a110d5…`). Pendiente: cambiar la contraseña del `.jks` con `keytool -storepasswd`, porque la anterior sigue en el historial |
| 5 | Sin red, **Mis pedidos** se quedaba cargando sin fin: el `get()` de Firestore esperaba al servidor en lugar de fallar, y nunca se llegaba a la copia de SQLite | Medio: incumple el caso «Sin red» | `RepositorioCarrito.pedidos()` espera a Firestore como máximo 5 s y luego devuelve lo guardado en SQLite | Caso «Sin red» repetido en el emulador: el pedido aparece; con red carga en ~3 s. 23 pruebas en verde |

## 4. Ejecución de los casos en el dispositivo (29 de octubre de 2026)

Emulador Pixel 6 (API 35) con la APK de publicación `APK/NEXCODE-Marketplace-1.0-2026-10-29.apk`, contra el proyecto real de Firebase. Cuentas de prueba creadas para la ejecución: `vendedor.prueba.2910@nexcode.test` y `comprador.prueba.2910@nexcode.test`. Capturas en `docs/evidencias-pruebas/`.

| Caso | Resultado obtenido | Estado | Capturas |
|---|---|---|---|
| Inicio de sesión con datos errados | Mensaje «El correo o la contraseña no son correctos» | ✅ | 01, 02 |
| Registro con contraseñas distintas | Error «Las contraseñas no coinciden» en el campo | ✅ | 03 |
| Registro de vendedor | Entra al Panel del vendedor con los 20 productos del catálogo | ✅ | 04 |
| Compra | 2 × SSD Kingston + 1 × Ubiquiti UDR = $ 1.651.000 con envío gratis. Pedido NX-X3TAOS registrado, carrito vacío, existencias 39 → 37 y 8 → 7 | ✅ | 05, 07, 08 |
| Existencias | Con 8 unidades del Ubiquiti el botón **+** se deshabilita y no deja pasar de 8. No muestra un mensaje con las unidades disponibles | ✅ (sin mensaje) | 06 |
| Calificación | 4 estrellas: el promedio pasa de 5,0 (1) a 4,5 (2) y la opinión aparece primero | ✅ | 09, 10 |
| Chat | El comprador escribe 9:31, el vendedor lo ve con «1 sin leer» y responde 9:33; el comprador recibe la respuesta | ✅ | 11, 12, 13 |
| Sin red | Catálogo y chat se ven en modo avión. **Mis pedidos** se quedaba cargando (hallazgo 5); tras la corrección se ve el pedido | ✅ tras corregir | 14, 15, 16 |

Observaciones para la revisión de seguridad:

- El interruptor «Quiero vender» creaba una cuenta **administrador** sin aprobación (escalada de privilegios). **Corregido el 29/10** (hallazgo V1): el registro solo crea comprador, se quitó el interruptor y los vendedores se dan de alta en la consola. Ver `docs/seguridad-permisos-y-autorizacion.md`.
- Los botones − / + del carrito se anuncian a los lectores de pantalla como «Eliminar» y «Agregar al carrito», y las estrellas de calificación no tienen descripción.

## 5. Pruebas del agente de IA (asistente de compras)

Escenarios ejecutados sobre la pantalla del asistente (Gemini vía Firebase AI Logic), con el
catálogo real cargado. El guion detallado y los pies de figura están en
`docs/evidencias-pruebas/agente/DESCRIPCIONES-T1-T7.md`. Las respuestas del modelo varían en
cada corrida; se evalúa el comportamiento, no el texto literal.

Ejecución del 30/10/2026 en un teléfono físico (moto g power, Android 11), versión debug,
modelo `gemini-flash-lite-latest` (responde `gemini-3.5-flash-lite`). Las preguntas se
escribieron por adb sin tildes. Los datos de cada respuesta se compararon con
`datos/CatalogoMayorista.kt` y con las calificaciones reales de Firestore.

| # | Entrada / acción | Resultado obtenido | ¿Pasa? | Captura |
|---|---|---|---|---|
| T0 | Abrir el asistente | Aviso 🤖 de IA fijo arriba y saludo | ✅ | `agente/agente-T0-saludo-aviso.png` |
| T1 | «Recomiendame una impresora economica» | Epson EcoTank L3560, $ 1.014.000, 18 disponibles. Es la más barata de Impresión (la HP M404dn cuesta $ 1.846.000) | ✅ | `agente/agente-T1-impresora-economica.png` |
| T2 | «Cual producto tiene mejor calificacion?» | Ubiquiti UniFi Dream Router UDR, 4,8★. Es la mayor calificación real (el SSD Kingston bajó a 4,5 con dos votos de usuarios) | ✅ | `agente/agente-T2-mejor-calificacion.png` |
| T3 | «Quiero un televisor» (no existe) | «No tenemos televisores en nuestro catálogo», ofrece otros productos. No inventa ninguno | ✅ | `agente/agente-T3-sin-televisor.png` |
| T4 | «Como pago?» | Explica los 3 pasos: agregar al carrito → Carrito → Finalizar compra → pedido en Perfil > Mis pedidos y acuerdo de pago por Chat | ✅ (tras corrección 5.2) | `agente/agente-T4-como-pago.png` |
| T5 | «Que dia es hoy?» | Dice que no tiene acceso a la fecha y redirige a productos | ✅ | `agente/agente-T5-fuera-de-tema.png` |
| T6 | Modo avión → enviar | «No se pudo contactar al asistente. Verifica tu conexión.» La pantalla no se cierra | ✅ | `agente/agente-T6-sin-conexion.png` |
| T7 | Campo vacío → enviar (dos veces) | No se agrega ningún mensaje ni se llama al modelo | ✅ | `agente/agente-T7-mensaje-vacio.png` |

### 5.1 Fallo encontrado al ejecutar el plan y su corrección (30/10/2026)

Al enviar la primera pregunta, el asistente respondía «El servicio de IA no está disponible
en este momento». La app manejó bien el error (mensaje claro en el chat, sin cerrarse), pero
el agente no funcionaba. Al reproducir la llamada a Firebase AI Logic se hallaron dos causas:

| Causa | Evidencia | Corrección |
|---|---|---|
| App Check estaba en modo **aplicar** para Firebase AI Logic (desde el 29/10), y la app no integra el SDK de App Check, así que todas las solicitudes llegaban sin token | Respuesta `401 UNAUTHENTICATED: Firebase App Check token is invalid` | Se pasó App Check de Firebase AI Logic a **no aplicado**. El proveedor de App Check para Android (Play Integrity) solo valida apps instaladas desde Google Play, y la entrega se distribuye como APK |
| El modelo fijo `gemini-2.5-flash` ya no se ofrece a proyectos nuevos | Respuesta `404 NOT_FOUND: … no longer available to new users` | `AsistenteCompras.kt` usa ahora el alias `gemini-flash-lite-latest`, que Google mantiene apuntando al modelo vigente (el mismo que usa HuellitasPlus) |

Además, `AsistenteCompras` ahora registra en Logcat la excepción original (`Log.w`) antes de
traducirla al mensaje para el usuario, porque sin ese registro la causa no se podía ver en
el dispositivo.

**Mejora pendiente:** integrar App Check (Play Integrity) cuando la app se publique en Google
Play y volver a ponerlo en modo aplicar.

### 5.2 Ajustes al prompt tras la primera corrida (30/10/2026)

| Hallazgo | Corrección en `AsistenteCompras.kt` |
|---|---|
| T4 falló: ante «¿Cómo pago?» el agente decía que no podía ayudar con pagos y remitía a «atención al cliente en la tienda», porque el prompt solo le daba el catálogo | Se agregó al prompt la sección «Cómo se compra en la app» (carrito → Finalizar compra → Mis pedidos → acuerdo de pago por Chat; la app no cobra en línea) |
| Las respuestas mostraban asteriscos de Markdown (`**Epson…**`), porque la burbuja del chat muestra texto plano | Se indica al modelo que responda en texto plano, sin Markdown |

Después de los ajustes se repitieron T1–T5 y todos pasaron (tabla de arriba).

