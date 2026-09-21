# Guia I Corte II – donde esta cada requisito

Tabla de verificacion para el informe: cada punto que pide la guia y el archivo
exacto donde se resolvio.

## Tecnologias

| Requisito | Donde |
|---|---|
| Android Studio | Proyecto Gradle 8.14.5 / AGP 8.7.3 |
| Kotlin **y** Java | Kotlin en `ui/` y `datos/`; Java en `core/Formato.java`, `core/Animaciones.java`, `core/Imagenes.java` y `datos/local/BaseDatosSQL.java` |
| Firebase Authentication | `datos/RepositorioUsuarios.kt` |
| Firebase Cloud Firestore | `datos/remoto/ServicioFirebase.kt` y los cuatro repositorios |
| Firebase Storage | `RepositorioProductos.subirFotografia` y `SembradorCatalogo` |
| Camara del dispositivo | `ui/camara/CamaraActivity.kt` (CameraX) |

## Usuario / Comprador

| Requisito | Donde |
|---|---|
| Registro e inicio de sesion | `ui/login/LoginActivity.kt`; la ficha perdida se rehace en `RepositorioUsuarios.rehacerFicha` |
| Consulta y busqueda de productos | `ui/catalogo/CatalogoFragment.kt` + `Producto.coincideCon` |
| Carrito de compras | `ui/carrito/CarritoFragment.kt` + `datos/RepositorioCarrito.kt` |
| Calificacion de 1 a 5 estrellas | `ui/calificacion/CalificacionActivity.kt` + `ui/comun/BarraEstrellas.kt` |
| Chat con vendedor / administrador | `ui/chat/ChatFragment.kt`, `ChatActivity.kt`, `datos/RepositorioChat.kt` |

## Administrador / Vendedor

| Requisito | Donde |
|---|---|
| Inicio de sesion | Mismo `LoginActivity`; el interruptor "quiero vender" crea la cuenta como administrador |
| Registrar, editar y eliminar productos | `ui/admin/EditarProductoActivity.kt` y `AdminActivity.kt` |
| Tomar o cargar fotografias | `CamaraActivity` (tomar) y `PickVisualMedia` (cargar), en `EditarProductoActivity` |
| Gestionar productos y calificaciones | `AdminActivity` (inventario y metricas); promedio recalculado en `RepositorioProductos.calificar` |
| Responder chats | Bandeja en `ChatFragment` (modo vendedor) y conversacion en `ChatActivity` |

## Campos del producto

Todos en `dominio/Modelos.kt`, clase `Producto`:

nombre · imagen · precio · descripcion · categoria · **estado (nuevo o usado)** ·
cantidad disponible · calificacion · vendedor.

Adicionales que no pedia la guia pero hacen falta en un catalogo mayorista:
marca, referencia (SKU), votos, unidades vendidas y fecha de registro.

## Firebase

| Requisito | Donde |
|---|---|
| Authentication: registro e inicio de sesion | `RepositorioUsuarios`; el registro escribe credencial y ficha, y se recupera si la segunda falla |
| Firestore: coleccion `usuarios` con la estructura de la guia | `RepositorioUsuarios.registrar` |
| Firestore: coleccion `productos` con la estructura de la guia | `RepositorioProductos.aMapa` |
| Storage: fotografias de los productos | Carpeta `productos/` del bucket |

La estructura de documentos respeta exactamente los nombres de campo que dibuja
la guia (`nombre`, `descripcion`, `precio`, `categoria`, `estado`, `cantidad`,
`imagen`, `vendedorId`, `calificacion`).

## Bases de datos

| Requisito | Donde |
|---|---|
| Conectar bases de datos SQL con Firebase | `datos/local/AlmacenLocal.kt`: cada instantanea de Firestore reescribe SQLite |
| Tabla de productos | `BaseDatosSQL.T_PRODUCTOS` |
| Tabla de usuarios y login | `BaseDatosSQL.T_USUARIOS` |
| Registros | `T_CARRITO`, `T_PEDIDOS`, `T_PEDIDO_LINEAS`, `T_MENSAJES` |
| Conectividad completa | El catalogo se abre sin red desde SQLite; Firestore ademas mantiene su propia cache persistente |

## Funcionalidades

| Requisito | Estado |
|---|---|
| Login y registro | Si |
| Navegacion entre pantallas | Barra inferior de cinco secciones + actividades |
| Catalogo y busqueda | Busqueda por nombre, marca, referencia, categoria y descripcion, sin distinguir tildes |
| Detalle del producto | Si |
| Carrito de compras | Si, con control de existencias |
| Gestion de productos | Si |
| Camara para fotografiar productos | Si, CameraX |
| Calificacion con cinco estrellas | Si, un voto por persona, promedio recalculado |
| Chat usuario–vendedor | Si, en tiempo real |
| Perfil de usuario | Si |
| Conexion y almacenamiento en Firebase | Si |

## Pantallas minimas

Las once de la guia estan listadas en el README, mas dos adicionales
(bienvenida e historial de pedidos).
