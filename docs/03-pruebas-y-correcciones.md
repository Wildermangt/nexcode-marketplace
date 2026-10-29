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
