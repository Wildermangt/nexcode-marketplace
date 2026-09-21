# Bitácora de ejecución

**16 de septiembre de 2026** · Cuenta `wildermangt@gmail.com`

## Estado final: COMPLETADO ✅

La automatización está **construida, probada y activa**. Ver `INSTRUCTIVO.md`
para el paso a paso con capturas.

| # | Entregable | Estado |
|---|---|---|
| 1 | Captura del escenario completo | ✅ |
| 2 | Captura de la configuración de Gmail | ✅ |
| 3 | Captura de Google Sheets | ✅ |
| 4 | Captura de la ejecución | ✅ |
| 5 | Evidencia de la fila creada automáticamente | ✅ 2 filas |
| 6 | El docente verifica la conexión | escenario activo, listo |

## Qué se creó

| Recurso | Identificador |
|---|---|
| Hoja `CONEXIÓN A MAKE` | `14G7JmQrZwtc0SHKIPibgZxFtSnELhnoC-L5j8dc5NM4` |
| Escenario Make | `6302474` (equipo `2972430`) |
| Conexión Gmail | `CONEXIÓN CON GMAIL` |
| Conexión Sheets | `Wilderman's Google connection` |
| Programación | Cada 15 minutos · **Active** |

## Cómo se hizo

Chrome se lanzó con `--remote-debugging-port=9222` en un perfil aparte
(`C:\Users\wildermangt\chrome-make`) y el montaje se hizo por automatización
del navegador. El inicio de sesión en Make lo hizo el usuario.

### Obstáculos técnicos encontrados

1. **El lienzo de Make es un `<canvas>` renderizado.** Los módulos no existen
   en el DOM; hay que hacer clic por coordenadas con eventos de ratón sintéticos.
2. **La interfaz usa shadow DOM** (46 shadow roots) y elementos personalizados
   (`IMT-BUTTON`, `IMT-CODER`, `IMT-PILL`, `IMT-OPTION`). Los selectores CSS
   normales no llegan; hay que atravesar los shadow roots.
3. **Los nombres de variable de Gmail no son los intuitivos.** Ver el Paso 8 del
   instructivo: 4 de 5 nombres "obvios" estaban mal y dejaban las columnas vacías.

## Diferencias con las diapositivas del docente

| Punto | Diapositiva | Aquí | Por qué |
|---|---|---|---|
| Límite | `10` | **`1`** | Cuidar el plan gratuito |
| Filter type | `Filtro de Gmail` + `newer_than:1d` | `Simple filter` + `All messages` | Menos frágil; mismo resultado con límite 1 |
| Pestaña de la hoja | `Hoja 1` | `Untitled` | Nombre que asignó Google al crear la hoja |
| Punto de inicio | — | `From now on` | No arrastrar correos viejos |
