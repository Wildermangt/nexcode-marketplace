# PASO A PASO — Automatización con Make, Gmail y Google Sheets

**Guía II · Corte II · Optativa II** — ING. Jairo Armando Salcedo Aranda
**Referencia visual:** Ensigna.pdf, páginas **279 a 294** (copias en `pasos-ensigna/`)

**Resultado esperado:** `Gmail → Make → Google Sheets → Registro automático del correo.`

---

## PASO 0 · Crear la hoja de Google Sheets (prerrequisito)

Antes de tocar Make, la hoja debe existir, porque Make la buscará por nombre.

1. Entra a <https://sheets.google.com> con la **misma cuenta de Gmail** que vas a conectar.
2. Crea una hoja nueva y renómbrala exactamente: **`CONEXIÓN A MAKE`**
3. En la fila 1 escribe los 5 encabezados, uno por columna:

| A | B | C | D | E |
|---|---|---|---|---|
| Fecha | Remitente | Asunto | Correo | Contenido |

4. Deja el nombre de la pestaña como **`Hoja 1`** (así aparece en la diapositiva 292).

> Atajo: en `recursos/CONEXION-A-MAKE.csv` están los encabezados listos.
> En Sheets: **Archivo → Importar → Subir**, y luego renombra la hoja a `CONEXIÓN A MAKE`.

📸 **Evidencia 3** → guarda la captura en `evidencias/03-sheets/`

---

## PASO 1 · Entrar a Make *(diapositiva 279)*

1. Ingresa a <https://www.make.com> e inicia sesión.
2. En el menú izquierdo abre **Escenarios**.
3. Si es tu primera vez verás *"Todavía no has creado ningún escenario"*.
4. Pulsa **+ Crear escenario** (arriba a la derecha) o **Open Scenario Builder**.

---

## PASO 2 · Planear la automatización *(diapositiva 280)*

1. Se abre el lienzo **New scenario** con un botón morado **(+)** en el centro.
2. Puedes cerrar el panel **"Construye con Maia"** de la izquierda: no lo necesitas,
   vamos a armar el escenario a mano.
3. Haz clic en el **(+)** morado → se despliega el buscador de aplicaciones.

---

## PASO 3 · Agregar Gmail como disparador *(diapositiva 281)*

1. En el buscador escribe **Gmail** y selecciónalo.
2. En la lista de módulos, bajo **Desencadenantes**, elige:

   > **Revisar correos electrónicos** *(Watch emails)*
   > *"Se activa cuando se recibe un nuevo correo electrónico en tu bandeja de entrada."*

⚠️ **Importante:** debe ser **Watch emails**, NO *"Enviar un correo electrónico"*.
La guía exige que el disparador **detecte** correos nuevos.

---

## PASO 4 · Crear la conexión con tu correo *(diapositiva 282)*

1. Se abre el panel del módulo **Gmail** con el campo **Conexión ***.
2. Haz clic en **Crear una conexión**.

---

## PASO 5 · Nombrar la conexión y autorizar *(diapositiva 283)*

1. En **Nombre de la conexión** escribe: **`CONEXIÓN CON GMAIL`**
2. Deja **Alcances adicionales** sin tocar.
3. Pulsa **Iniciar sesión con Google**.
4. Verás el mensaje *"Creando una conexión…"*.

---

## PASO 6 · Ingresar a la cuenta de Gmail *(diapositiva 284)*

1. Se abre la ventana de Google: **Iniciar sesión en Make**.
2. Selecciona tu cuenta institucional.
3. Revisa los permisos que Google concederá a Make (nombre, imagen, correo).
4. Pulsa **Continuar**.
5. Vuelves a Make con la conexión ya creada y seleccionada.

📸 **Evidencia 2** → guarda la captura en `evidencias/02-gmail/`

---

## PASO 7 · Configurar el módulo *Watch emails* *(diapositiva 291)*

Con la conexión lista, completa los campos del módulo Gmail:

| Campo | Valor |
|---|---|
| **Conexión** | `CONEXIÓN CON GMAIL` |
| **Carpeta** | `Recibidos` / *INBOX* |
| **Tipo de filtro** | `Filtro de Gmail` |
| **Consulta** | `newer_than:1d` |
| **Marcar los mensajes como leídos al recuperarlos** | `No` |
| **Límite** | `10` *(debe ser ≤ 500)* |

Pulsa **Ahorrar** *(Save)*.

> El módulo queda etiquetado como **"Nuevos correos — Watch emails"**.

---

## PASO 8 · Agregar Google Sheets *(diapositivas 289 y 292)*

1. Haz clic en el semicírculo a la derecha del módulo Gmail para **encadenar** un módulo.
2. Busca y selecciona **Google Sheets** *(Hojas de cálculo de Google)*.
3. Elige el módulo de acción: **Add a Row** *(Agregar una fila)*.
4. En **Conexión** pulsa **Agregar** y autoriza con la misma cuenta de Google.

---

## PASO 9 · Configurar *Add a Row* *(diapositiva 292)*

| Campo | Valor |
|---|---|
| **Conexión** | tu conexión de Google |
| **Método de búsqueda** | `Buscar por ruta` |
| **Conducir** *(Drive)* | `Mi impulso` / *My Drive* |
| **Nombre de la hoja de cálculo** | `CONEXIÓN A MAKE` |
| **Nombre de la hoja** | `Hoja 1` |
| **La tabla contiene encabezados** | `Sí` |
| **Utilice los encabezados de columna como identificadores** | activado |

---

## PASO 10 · Mapear los datos de Gmail a las columnas ⭐

Este es el paso clave de la guía: **relacionar los datos de Gmail con las columnas**.
Haz clic en cada campo y elige la variable del módulo **1. Gmail** en el panel lateral.

| Columna en Sheets | Variable de Gmail (módulo 1) |
|---|---|
| **Fecha** | `Fecha` *(Date)* |
| **Remitente** | `De (nombre)` *(From name)* |
| **Asunto** | `Sujeto` *(Subject)* |
| **Correo** | `De (correo electrónico)` *(From address)* |
| **Contenido** | `Texto completo` *(Text content)* |

Pulsa **Ahorrar** *(Save)* y luego el ícono de **guardar escenario** (💾) de la barra inferior.

📸 **Evidencia 1** → captura del escenario completo `Gmail → Google Sheets` en `evidencias/01-escenario/`

---

## PASO 11 · Enviar un correo de prueba y ejecutar *(diapositivas 285 y 286)*

1. Desde otra cuenta (o desde ti mismo) envía un correo con asunto:
   **`Prueba de automatización desde Make`**
2. Vuelve a Make y pulsa **▷ Correr una vez** (barra inferior).
3. Al terminar, haz clic en la burbuja **✓ 1** sobre el módulo Gmail para ver el **Resumen**:
   - `1 operación` · `Se ha utilizado 1 crédito`
   - `Inicialización` ✅ · `Operación 1` ✅
   - Dentro de **Paquete 1** verás: *ID del mensaje, Fecha, Sujeto, De (nombre),
     De (correo electrónico), Texto completo…*

📸 **Evidencia 4** → captura de la ejecución en `evidencias/04-ejecucion/`

---

## PASO 12 · Verificar el registro en Sheets *(diapositiva 293)*

1. Abre la hoja **`CONEXIÓN A MAKE`**.
2. Debe aparecer una **fila nueva** con los datos del correo distribuidos en las 5 columnas.

📸 **Evidencia 5** → captura de los datos registrados en `evidencias/05-datos-registrados/`

---

## PASO 13 · Revisar la red creada *(diapositiva 288)* — opcional

En Make, entra a **Red** *(Grid)* en el menú izquierdo. Verás el grafo del escenario:
tu cuenta de correo conectada por un **Trigger** al escenario, con **Apps used: Gmail**.
Sirve como evidencia extra de que la conexión existe.

---

## PASO 14 · Activar la automatización cada 15 minutos ⭐

1. En la barra inferior del editor, activa el interruptor **Cada 15 minutos**
   *(o abre el reloj del módulo disparador → **Programación** → `Cada 15 minutos`)*.
2. Activa el escenario con el switch **ON / SCHEDULING** (queda en verde).
3. Guarda.

> Sin este paso la automatización queda inactiva y **no** cumple el punto 7 de la guía.

---

## Verificación final

- [ ] Hoja `CONEXIÓN A MAKE` con los 5 encabezados
- [ ] Escenario `Gmail → Google Sheets` guardado
- [ ] Módulo **Watch emails** configurado
- [ ] Módulo **Add a Row** con los 5 campos mapeados
- [ ] **Correr una vez** ejecutado sin errores
- [ ] Fila registrada automáticamente en Sheets
- [ ] Escenario **activo** cada **15 minutos**
- [ ] Las 5 capturas guardadas en `evidencias/`
- [ ] El docente verifica la conexión
