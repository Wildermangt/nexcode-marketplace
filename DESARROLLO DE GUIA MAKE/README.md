# DESARROLLO DE GUÍA MAKE

Taller: **Automatización con Make, Gmail y Google Sheets**
Guía II · Corte II · Optativa II — ING. Jairo Armando Salcedo Aranda

## Objetivo

Crear una automatización en Make que detecte nuevos correos de Gmail y registre
automáticamente su información en Google Sheets.

```
Gmail  →  Make  →  Google Sheets  →  Registro automático del correo
```

## Contenido de esta carpeta

| Archivo / carpeta | Para qué sirve |
|---|---|
| **`INSTRUCTIVO.pdf`** | 📄 **ENTREGABLE FINAL** — 12 páginas con las capturas |
| `INSTRUCTIVO.md` | Fuente del instructivo (editable, para regenerar el PDF) |
| `PASO-A-PASO.md` | Guía genérica de los 14 pasos, mapeada a las diapositivas 279-294 |
| `CHECKLIST-ENTREGABLES.md` | Los 6 entregables y qué debe verse en cada captura |
| `pasos-ensigna/` | Las 16 diapositivas del docente (págs. 279-294) en PNG + `INDICE.md` |
| `recursos/CONEXION-A-MAKE.csv` | Encabezados listos para importar a Google Sheets |
| `recursos/blueprint-make.json` | Escenario de Make listo para importar (Import Blueprint) |
| `recursos/generar-pdf.sh` | Regenera el PDF si editas el instructivo |
| `EJECUCION.md` | Bitácora de qué se ejecutó |
| **`PLAN-GRATUITO.md`** | Cómo no agotar las 1.000 operaciones del plan Free |
| `capturas/` | Las 21 capturas del montaje real, en orden |
| `evidencias/` | Carpetas por entregable (ya cubiertas por `capturas/`) |

## Resumen en 4 movimientos

1. **Hoja** `CONEXIÓN A MAKE` con encabezados: `Fecha · Remitente · Asunto · Correo · Contenido`
2. **Escenario** en Make: `Gmail — Watch emails` → `Google Sheets — Add a Row`
3. **Probar**: enviar correo de prueba → **Correr una vez** → verificar la fila en Sheets
4. **Activar** el escenario cada **15 minutos**

## Los dos módulos que importan

| Módulo | App | Papel |
|---|---|---|
| **Watch emails** (Revisar correos electrónicos) | Gmail | Disparador: detecta correos nuevos |
| **Add a Row** (Agregar una fila) | Google Sheets | Acción: escribe la fila |

## Mapeo de campos

| Columna en Sheets | Variable de Gmail |
|---|---|
| Fecha | `Fecha` (Date) |
| Remitente | `De (nombre)` (From name) |
| Asunto | `Sujeto` (Subject) |
| Correo | `De (correo electrónico)` (From address) |
| Contenido | `Texto completo` (Text content) |
