# NEXCODE Marketplace · Planeación y requisitos

Optativa II – Desarrollo de Aplicaciones para Dispositivos Móviles · Universidad de San Buenaventura, Bogotá
Equipo Nexcode: Jeferson Wilderman González Tenjo (Product Owner · Developer) y José Leandro Ocampo Camacho (Scrum Master · Developer)

## 1. Problema

Los distribuidores pequeños de tecnología en Colombia venden con catálogos en PDF, hojas de cálculo y mensajes de WhatsApp. El comprador no sabe si un producto sigue disponible, el precio cambia según quién responda y las preguntas se pierden entre chats personales. El vendedor, por su parte, actualiza el inventario a mano y no tiene un registro de lo vendido ni de lo que opinan sus clientes.

## 2. Justificación

Una aplicación móvil con catálogo en línea, carrito con control de existencias y chat con el vendedor concentra en un solo lugar lo que hoy está disperso:

- **El inventario es único y se actualiza en tiempo real.** Firestore descuenta las existencias al comprar, así que no se venden productos agotados.
- **Precios coherentes.** El precio de venta se calcula a partir del costo de distribuidor (costo + 30 %) y es el mismo para todos.
- **El catálogo funciona sin red.** La copia en SQLite permite consultarlo aunque la conexión sea mala, algo frecuente en datos móviles.
- **Hay trazabilidad.** Pedidos, calificaciones y conversaciones quedan registrados y el vendedor ve métricas de su inventario.

## 3. Objetivo

Desarrollar una aplicación Android de comercio electrónico con dos perfiles (comprador y vendedor) que permita consultar un catálogo de tecnología, comprar con control de inventario, calificar productos y conversar con el vendedor, usando Firebase como respaldo en la nube.

## 4. Alcance

**Incluye:**

- Registro e inicio de sesión con correo y contraseña, con dos perfiles: comprador y administrador/vendedor.
- Catálogo inicial de 20 referencias del portafolio mayorista de Ingram Micro Colombia.
- Búsqueda por nombre, marca, referencia, categoría y descripción, sin distinguir tildes, y filtros por categoría.
- Detalle del producto, carrito con control de existencias y confirmación de compra que genera un pedido.
- Historial de pedidos del comprador.
- Calificación de 1 a 5 estrellas con comentario y promedio por producto.
- Chat en tiempo real entre comprador y vendedor.
- Panel del vendedor para crear, editar y eliminar productos, con foto desde la cámara (CameraX) o la galería.
- Copia local en SQLite para consultar el catálogo, los pedidos y los mensajes sin conexión.

**No incluye:**

- Pagos en línea: el pedido queda registrado, pero el cobro se acuerda por fuera de la app.
- Envíos y seguimiento de la entrega.
- Varios vendedores con tiendas separadas: hay un solo catálogo.
- Versión para iOS o web.

## 5. Usuarios

| Perfil | Necesidad principal |
|---|---|
| Comprador | Encontrar productos de tecnología disponibles, comprarlos y resolver dudas con el vendedor |
| Administrador / vendedor | Mantener el catálogo y el inventario al día, ver qué se vende y responder a los clientes |

## 6. Requisitos funcionales

La columna *Dónde se implementa* sirve como trazabilidad entre cada requisito y el código de la aplicación Android.

| ID | Requisito | Prioridad | Dónde se implementa |
|---|---|---|---|
| RF-01 | El usuario se registra con nombre, correo y contraseña, y elige si es comprador o vendedor | Alta | `ui/login/LoginActivity.kt`, `datos/RepositorioUsuarios.kt` |
| RF-02 | El usuario inicia y cierra sesión | Alta | `LoginActivity`, `ui/perfil/PerfilFragment.kt` |
| RF-03 | Si la ficha del usuario se perdió durante el registro, se reconstruye al iniciar sesión como comprador | Media | `RepositorioUsuarios.rehacerFicha()` |
| RF-04 | El comprador consulta el catálogo con imagen, nombre, marca, precio, estado y existencias | Alta | `ui/catalogo/CatalogoFragment.kt`, `ui/inicio/InicioFragment.kt` |
| RF-05 | El comprador busca productos por texto y filtra por categoría | Alta | `CatalogoFragment`, `Producto.coincideCon` en `dominio/Modelos.kt` |
| RF-06 | El comprador ve el detalle completo de un producto | Alta | `ui/detalle/DetalleProductoActivity.kt` |
| RF-07 | El comprador agrega productos al carrito, cambia cantidades y los quita, sin superar las existencias | Alta | `ui/carrito/CarritoFragment.kt`, `datos/RepositorioCarrito.kt` |
| RF-08 | Al confirmar la compra se crea un pedido y se descuentan las existencias | Alta | `RepositorioCarrito`, colección `pedidos` |
| RF-09 | El comprador consulta su historial de pedidos | Media | `ui/pedidos/PedidosActivity.kt` |
| RF-10 | El comprador califica un producto de 1 a 5 estrellas con comentario y se recalcula el promedio | Media | `ui/calificacion/CalificacionActivity.kt`, `RepositorioProductos.calificar` |
| RF-11 | Comprador y vendedor conversan por chat en tiempo real | Media | `ui/chat/ChatFragment.kt`, `ChatActivity.kt`, `datos/RepositorioChat.kt` |
| RF-12 | El vendedor crea, edita y elimina productos | Alta | `ui/admin/AdminActivity.kt`, `EditarProductoActivity.kt` |
| RF-13 | El vendedor toma una foto con la cámara o la carga de la galería para el producto | Media | `ui/camara/CamaraActivity.kt`, `PickVisualMedia` en `EditarProductoActivity` |
| RF-14 | El vendedor ve el inventario con métricas de productos, unidades y valor total | Baja | `AdminActivity` |
| RF-15 | Si el catálogo está vacío, se carga el catálogo mayorista inicial con sus fotografías | Baja | `datos/SembradorCatalogo.kt`, `CatalogoMayorista.kt` |

## 7. Requisitos no funcionales

| ID | Requisito | Cómo se cumple |
|---|---|---|
| RNF-01 | **Seguridad:** solo el vendedor modifica el catálogo y cada usuario edita solo su perfil | `firestore.rules` y `storage.rules` validan la sesión y el tipo de usuario |
| RNF-02 | **Disponibilidad sin red:** el catálogo, los pedidos y los mensajes se consultan sin conexión | Copia en SQLite (`datos/local/AlmacenLocal.kt`) y caché persistente de Firestore |
| RNF-03 | **Compatibilidad:** funciona desde Android 7.0 | `minSdk 24`, `targetSdk 35` |
| RNF-04 | **Rendimiento:** el catálogo aparece al abrir la app, antes de que responda la red | La lista se pinta desde SQLite y luego se actualiza con `addSnapshotListener` |
| RNF-05 | **Usabilidad y accesibilidad:** navegación por barra inferior de cinco secciones e imágenes con descripción para lectores de pantalla | Material 3; todas las `ImageView` e `ImageButton` tienen `contentDescription` |
| RNF-06 | **Integridad de datos:** no se puede comprar más de lo disponible | El carrito valida existencias y la compra las descuenta en Firestore |
| RNF-07 | **Mantenibilidad:** código separado en `dominio`, `datos` y `ui`, con dependencias explícitas | Contenedor manual en `NexcodeMarketplace.kt` (sin Hilt) |
| RNF-08 | **Privacidad:** los datos locales no se copian en las copias de seguridad de Android | `android:allowBackup="false"` en el manifiesto |

## 8. Historias de usuario y Sprints

Las historias de usuario, sus tareas y los tres Sprints de dos semanas se gestionan en Azure DevOps, en el proyecto **Nexcode - Marketplace** (https://dev.azure.com/wildermangt). Cada historia sigue el formato *Como [rol] quiero [acción] para [beneficio]* y se relaciona con los requisitos anteriores; por ejemplo, *«Como comprador quiero buscar productos por texto»* corresponde a RF-05.

## 9. Diseño de pantallas

Los mockups de las pantallas principales están en [`02-mockups.md`](02-mockups.md).
