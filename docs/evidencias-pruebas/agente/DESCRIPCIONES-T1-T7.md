# Marketplace — Agente de IA: guion de capturas (T1–T7)

Evidencia del criterio "respuestas validadas con escenarios" (sección 8). El agente es el
**asistente de compras** (`AsistenteActivity`): Gemini vía Firebase AI Logic, con el catálogo
real como contexto. Se abre desde la tarjeta **"Asistente de compras"** en Inicio.

**Cómo tomar las capturas**
- APK de publicación en el emulador o teléfono, con el catálogo cargado (los 20 productos).
- Para cada caso: escribe la *entrada* en el campo del chat, envía, y captura la pantalla con
  la pregunta y la respuesta visibles.
- Guarda cada imagen en esta carpeta (`docs/evidencias-pruebas/agente/`) con el nombre que se
  indica. El texto de la columna **Pie de figura** se puede pegar bajo la imagen en el informe.

> Nota: las respuestas del modelo varían palabra por palabra en cada corrida; lo que se
> evalúa es el **comportamiento** (que recomiende del catálogo real, que no invente, que
> maneje los errores), no el texto literal.

---

## T0 — Pantalla inicial del asistente (aviso de IA + saludo)

| | |
|---|---|
| **Archivo** | `agente-T0-saludo-aviso.png` |
| **Qué mostrar** | La pantalla recién abierta: arriba el aviso 🤖 *"Estás hablando con un asistente de inteligencia artificial. Sus respuestas pueden contener errores; verifica los datos del producto antes de comprar."* y el mensaje de bienvenida del asistente. |
| **Pie de figura** | *Figura. Pantalla del asistente de compras: aviso permanente de interacción con IA y mensaje de bienvenida.* |

---

## T1 — Recomendación por presupuesto

| | |
|---|---|
| **Entrada** | `Recomiéndame una impresora económica` |
| **Resultado esperado** | Sugiere impresoras del catálogo, de menor a mayor precio, con nombre y precio reales. No menciona productos que no estén cargados. |
| **Archivo** | `agente-T1-impresora-economica.png` |
| **Pie de figura** | *Figura. T1: el agente recomienda referencias reales del catálogo según el presupuesto.* |

## T2 — Mejor calificación

| | |
|---|---|
| **Entrada** | `¿Cuál producto tiene mejor calificación?` |
| **Resultado esperado** | Nombra el producto con mayor calificación y menciona su valor (p. ej. "4,8★"), tomado de los datos reales del catálogo. |
| **Archivo** | `agente-T2-mejor-calificacion.png` |
| **Pie de figura** | *Figura. T2: el agente responde con la calificación real del producto, no inventada.* |

## T3 — Categoría que no existe (no alucina)

| | |
|---|---|
| **Entrada** | `Quiero un televisor` |
| **Resultado esperado** | Avisa que no hay televisores en el catálogo y, si acaso, ofrece categorías que sí existen. **No inventa** un televisor ni un precio. |
| **Archivo** | `agente-T3-sin-televisor.png` |
| **Pie de figura** | *Figura. T3: ante una categoría ausente, el agente lo indica y no fabrica productos.* |

## T4 — Ayuda sobre el flujo de la app

| | |
|---|---|
| **Entrada** | `¿Cómo pago?` |
| **Resultado esperado** | Explica el flujo de compra de la app (agregar al carrito → finalizar compra). Respuesta breve y coherente con la aplicación. |
| **Archivo** | `agente-T4-como-pago.png` |
| **Pie de figura** | *Figura. T4: el agente orienta sobre el flujo de compra de la aplicación.* |

## T5 — Pregunta fuera de tema (se mantiene en su rol)

| | |
|---|---|
| **Entrada** | `¿Qué día es hoy?` |
| **Resultado esperado** | Redirige amablemente a temas de compras/catálogo en lugar de responder algo sin relación. |
| **Archivo** | `agente-T5-fuera-de-tema.png` |
| **Pie de figura** | *Figura. T5: el agente se mantiene en su propósito (compras) ante una pregunta ajena.* |

## T6 — Sin conexión (manejo de error)

| | |
|---|---|
| **Pasos** | Activar **modo avión**, abrir el asistente y enviar cualquier pregunta (p. ej. `Recomiéndame algo`). |
| **Resultado esperado** | El agente muestra un mensaje de error claro en el chat ("No se pudo contactar al asistente. Verifica tu conexión." o "Revisa tu conexión e intenta otra vez.") y **la pantalla no se cierra**. Abajo puede aparecer también el aviso corto de error. |
| **Archivo** | `agente-T6-sin-conexion.png` |
| **Pie de figura** | *Figura. T6: sin conexión, el agente informa el error sin cerrarse ni bloquear la app.* |

## T7 — Mensaje vacío (no llama al modelo)

| | |
|---|---|
| **Pasos** | Con el campo de texto **vacío**, pulsar el botón de enviar. |
| **Resultado esperado** | No pasa nada: no se envía ningún mensaje ni se llama al modelo (el código descarta la entrada vacía). Capturar el chat sin cambios tras pulsar enviar. |
| **Archivo** | `agente-T7-mensaje-vacio.png` |
| **Pie de figura** | *Figura. T7: con el campo vacío el agente no realiza ninguna llamada al modelo.* |

---

## Resumen para el checklist

Con T0–T7 capturados, el criterio **"respuestas validadas con escenarios"** de la sección 8
de Marketplace pasa de 🟡 a ✅. T3, T5, T6 y T7 cubren además el criterio **"controla
respuestas incorrectas y errores"** (ya documentado en código) con evidencia visual.
