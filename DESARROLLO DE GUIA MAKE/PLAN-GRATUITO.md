# Cuidados con el plan gratuito de Make

El plan **Free** de Make da **1.000 operaciones al mes**. Cada vez que el escenario
corre y procesa un correo, gasta operaciones. Si se agotan, el escenario se detiene
y no podrás mostrar la conexión al docente.

## Cuánto gasta esta automatización

Cada ciclo consume: **1 operación** del módulo Gmail **+ 1 operación por cada correo**
que escriba en Sheets.

| Escenario | Gasto |
|---|---|
| Ciclo sin correos nuevos | 1 op |
| Ciclo con 1 correo nuevo | 2 ops |
| Ciclo con 10 correos nuevos | 11 ops |

Con intervalo de **15 minutos** el escenario corre **96 veces al día** = ~96 ops/día
solo en revisiones vacías. **En ~10 días agotas las 1.000 operaciones.**

## Reglas para no excederte

1. **`Límite` = 1, no 10.**
   En el módulo *Watch emails*, baja **Límite** de `10` a **`1`**.
   Así, aunque lleguen 20 correos, solo procesa 1 por ciclo.
   *(Las diapositivas muestran 10; para plan gratuito usa 1.)*

2. **Filtro estrecho en `Consulta`.**
   En vez de `newer_than:1d`, usa algo que solo capture tu correo de prueba:
   ```
   subject:(Prueba de automatización desde Make) newer_than:1d
   ```
   Así los correos de publicidad no queman operaciones.

3. **NO bajes de 15 minutos.**
   El plan gratuito **no permite** menos de 15 min de todas formas, y la guía pide
   exactamente 15. Déjalo así.

4. **Usa `Correr una vez` con moderación.**
   Cada clic gasta operaciones reales. Con 3 o 4 ejecuciones tienes de sobra para
   las capturas. No lo pulses repetidamente para "ver si sirve".

5. **⭐ Desactiva el escenario apenas termines las capturas.**
   Déjalo activo solo el rato que el docente necesita verificarlo.
   Un escenario activo y olvidado consume ~2.880 ops/mes: **el triple de tu cupo.**

6. **Máximo 2 escenarios activos.**
   El plan gratuito lo limita. Si hiciste pruebas sueltas, bórralas
   (*Escenarios → ⋯ → Eliminar*) para no chocar con el límite.

## Cómo revisar tu consumo

En Make: **Organización → Uso** (o *Usage*). Ahí ves las operaciones
consumidas del mes. Revísalo antes y después de las pruebas.

## Configuración recomendada para plan gratuito

| Campo | Diapositiva | **Usa esto** |
|---|---|---|
| Límite | `10` | **`1`** |
| Consulta | `newer_than:1d` | `subject:(Prueba de automatización desde Make) newer_than:1d` |
| Intervalo | 15 min | **15 min** (no bajar) |
| Estado tras entregar | activo | **desactivado** |
