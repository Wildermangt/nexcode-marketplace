# NEXCODE Marketplace

Aplicacion movil de marketplace para **Optativa II – Diseno de Aplicaciones para
Dispositivos Moviles**, Universidad de San Buenaventura.
Guia I, Corte II – ING. Jairo Armando Salcedo Aranda.

Autores: Jeferson Wilderman Gonzalez Tenjo y Jose Leandro Ocampo Camacho.

---

## Que es

Una tienda movil con dos papeles de usuario sobre un mismo catalogo:

- **Comprador**: se registra, busca productos, los agrega al carrito, compra,
  califica de 1 a 5 estrellas y escribe al vendedor por chat.
- **Administrador / vendedor**: entra al panel, registra, edita y elimina
  productos, les toma o carga fotografias, ve el inventario y responde los chats.

El catalogo de partida son **20 referencias del portafolio mayorista de Ingram
Micro Colombia** (computo, monitores, impresion, almacenamiento, componentes,
perifericos, energia, redes, seguridad, movilidad y video). De cada referencia se
guarda el **costo de distribuidor** y el **precio de venta al publico, que es ese
costo mas un 30 % de margen** redondeado a la centena de peso.

---

## Tecnologias

| Capa | Tecnologia |
|---|---|
| Lenguajes | **Kotlin** (pantallas y repositorios) y **Java** (utilidades y base SQL) |
| Interfaz | Vistas XML + Material 3 |
| Autenticacion | Firebase Authentication (correo y contrasena) |
| Base de datos remota | Cloud Firestore |
| Fotografias | Firebase Storage |
| Base de datos local | SQLite con `SQLiteOpenHelper` (sin Room, para dejar el SQL a la vista) |
| Camara | CameraX 1.4.2 |
| Imagenes | Glide 4.16 |
| Compilacion | Gradle 8.14.5, AGP 8.7.3, JDK 21, `compileSdk` 35, `minSdk` 24 |

---

## Como se conecta el SQL con Firebase

La guia pide conectar una base de datos SQL con Firebase. El reparto es:

```
Firestore  ──(escucha en tiempo real)──►  AlmacenLocal (SQLite)  ──►  pantallas
    ▲                                            │
    └──────── pedidos, calificaciones, chat ◄────┘
```

- **Firestore es la fuente de verdad.** Cada cambio del catalogo llega por
  `addSnapshotListener` y se vuelca completo en la tabla `productos` de SQLite
  dentro de una transaccion.
- **SQLite es el espejo local.** Es lo que se pinta al abrir la aplicacion, de
  modo que el catalogo aparece al instante y sigue disponible sin red.
- **El carrito vive solo en SQLite** hasta que se finaliza la compra; entonces
  se convierte en un documento de `pedidos`, se descuentan las existencias de
  cada producto y la tabla local queda vacia.
- Pedidos y mensajes tambien se copian a SQLite para poder consultarlos sin
  conexion.

Tablas: `usuarios`, `productos`, `carrito`, `pedidos`, `pedido_lineas`,
`mensajes` (ver `datos/local/BaseDatosSQL.java`).

---

## Estructura de Firestore

```
usuarios/{uid}
  ├── nombre, correo, tipoUsuario, fechaRegistro

productos/{id}
  ├── nombre, descripcion, precio, categoria, estado, cantidad,
  ├── imagen, marca, referencia, vendedorId, vendedorNombre,
  ├── calificacion, votos, vendidos, fechaRegistro
  └── calificaciones/{uid}   → usuarioNombre, estrellas, comentario, fecha

pedidos/{id}
  ├── compradorId, compradorNombre, total, fecha, estado
  └── lineas[]  → productoId, nombre, precio, cantidad

chats/{uidComprador}
  ├── compradorId, compradorNombre, ultimoMensaje, fecha, sinLeerVendedor
  └── mensajes/{id}  → autorId, autorNombre, texto, fecha, esDelVendedor
```

Storage: `productos/{idProducto}.jpg`

Las reglas de seguridad estan en `firestore.rules` y `storage.rules`. La idea es
que cualquiera con sesion iniciada puede mirar la tienda, pero solo las cuentas
de tipo `administrador` tocan el catalogo; el comprador tiene dos excepciones
acotadas, para descontar inventario al comprar y para actualizar el promedio de
estrellas al calificar.

---

## Registro e inicio de sesion

Crear una cuenta son **dos escrituras en dos servicios distintos**: primero la
credencial en Authentication y despues la ficha `usuarios/{uid}` en Firestore.
Entre una y otra puede caerse la red, y entonces la cuenta queda a medias: el
correo ya esta tomado, asi que no se puede repetir el registro, pero sin ficha
tampoco se puede entrar.

`RepositorioUsuarios` cierra ese hueco por tres lados:

1. **El nombre se guarda tambien en la credencial** (`updateProfile`), que es lo
   unico que sobrevive si Firestore no responde.
2. **La copia de SQLite se escribe antes de salir a la red**, no despues.
3. **`rehacerFicha()`**: al iniciar sesion, si el documento no existe se
   reconstruye a partir de la copia local o, en su defecto, de la propia
   credencial. La cuenta vuelve siempre como **comprador**: nadie gana el panel
   del vendedor por haber perdido su ficha.

`perfilActual()` distingue los dos casos que antes compartian un mismo `catch`:
*no hay red* (se usa la copia de SQLite y no se toca nada, para no pisar la ficha
que si existe en el servidor) y *la ficha no existe* (se rehace).

Si la escritura de la ficha falla durante el registro, la pantalla lo dice —
*"Tu cuenta se creo, pero no se pudo guardar tu perfil"*— y basta con volver a
entrar con el mismo correo para que quede reparada.

---

## Arquitectura del codigo

```
com.nexcode.marketplace
├── NexcodeMarketplace.kt        Application: contenedor manual de dependencias
├── core/                        Java: Formato, Animaciones, Imagenes
├── dominio/Modelos.kt           Producto, Usuario, ItemCarrito, Pedido, ...
├── datos/
│   ├── local/BaseDatosSQL.java  esquema SQL
│   ├── local/AlmacenLocal.kt    acceso a SQLite
│   ├── remoto/ServicioFirebase  Auth + Firestore + Storage
│   ├── Repositorio*.kt          usuarios, productos, carrito, chat
│   ├── CatalogoMayorista.kt     las 20 semillas (archivo generado)
│   └── SembradorCatalogo.kt     carga inicial y subida de fotos a Storage
└── ui/
    ├── splash, login, principal
    ├── inicio, catalogo, detalle, carrito, calificacion
    ├── chat, perfil, pedidos
    ├── admin (panel y formulario de producto)
    ├── camara (CameraX)
    └── comun (CargadorImagenes, BarraEstrellas, extensiones)
```

No se usa Hilt a proposito: el contenedor manual deja a la vista quien depende de
quien, que es lo que hay que explicar en el informe IEEE.

---

## Pantallas

| # | Pantalla | Clase |
|---|---|---|
| 1 | Login / Registro | `ui.login.LoginActivity` |
| 2 | Inicio | `ui.inicio.InicioFragment` |
| 3 | Catalogo con busqueda y filtros | `ui.catalogo.CatalogoFragment` |
| 4 | Detalle del producto | `ui.detalle.DetalleProductoActivity` |
| 5 | Carrito | `ui.carrito.CarritoFragment` |
| 6 | Calificacion | `ui.calificacion.CalificacionActivity` |
| 7 | Chat | `ui.chat.ChatFragment` / `ChatActivity` |
| 8 | Perfil | `ui.perfil.PerfilFragment` |
| 9 | Panel administrador / vendedor | `ui.admin.AdminActivity` |
| 10 | Registrar / editar producto | `ui.admin.EditarProductoActivity` |
| 11 | Camara / carga de imagen | `ui.camara.CamaraActivity` |

Extra: bienvenida (`ui.splash.SplashActivity`) e historial de pedidos
(`ui.pedidos.PedidosActivity`).

---

## Como ejecutarlo

1. Abrir la carpeta `NexcodeMarketplace` en Android Studio y esperar la
   sincronizacion de Gradle.
2. Ejecutar en un emulador o telefono con Android 7.0 o superior.
3. **Crear la cuenta de vendedor**: en la pantalla de registro, activar el
   interruptor *"Quiero vender: crear cuenta de administrador"*.
4. Al entrar con esa cuenta, si el catalogo esta vacio la aplicacion carga sola
   las 20 referencias y sube sus fotografias a Storage. Tambien se puede
   disparar a mano desde el panel del vendedor.
5. Crear una segunda cuenta, esta vez de comprador, para probar carrito,
   calificaciones y chat.

Si una cuenta se creo con la red a medias y la aplicacion no dejaba entrar con
ella, basta con iniciar sesion de nuevo: la ficha se rehace sola y la cuenta
vuelve como comprador (ver *Registro e inicio de sesion*).

Desde la linea de comandos:

```bash
./gradlew assembleDebug        # APK de depuracion
./gradlew assembleRelease      # APK firmada con nexcode-marketplace.jks
```

Firma de publicacion: almacen `nexcode-marketplace.jks`, alias `nexcode`,
contrasena `Nexcode2026`.

---

## Proyecto Firebase

- **ID del proyecto**: `nexcode-marketplace`
- **Paquete Android**: `com.nexcode.marketplace`
- Consola: <https://console.firebase.google.com/project/nexcode-marketplace>

Para desplegar las reglas:

```bash
firebase deploy --only firestore:rules --project nexcode-marketplace
firebase deploy --only storage --project nexcode-marketplace
```

---

## Las fotografias del catalogo

Las 20 fotografias son **imagenes reales de cada equipo**, una por referencia.
Se obtuvieron con `herramientas/descargar_fotos.py`, que por cada SKU busca la
imagen del producto y se queda con la primera candidata que venga de un dominio
de confianza (el fabricante o un mayorista) y tenga resolucion suficiente. El
origen exacto de cada una queda anotado en `herramientas/origenes-fotos.txt`.

`herramientas/montar_fotos.py` las normaliza: recorta el marco vacio, centra el
equipo sobre el fondo suave de NEXCODE y las deja en 900x900 JPEG dentro de
`app/src/main/assets/productos`. No se les escribe texto encima porque la
tarjeta del catalogo recorta la imagen por el centro y la marca y la referencia
ya las pinta la propia interfaz.

Para regenerarlas:

```bash
cd herramientas
python descargar_fotos.py ../fotos_crudas          # todas
python descargar_fotos.py ../fotos_crudas HP-M404DN  # una sola
python montar_fotos.py ../fotos_crudas ../app/src/main/assets/productos hoja.jpg
```

El tercer argumento de `montar_fotos.py` es opcional: genera un mosaico con las
20 para revisarlas de un vistazo.

### Como llegan a Firebase Storage

`SembradorCatalogo` sube cada fotografia a Storage al crear el catalogo y guarda
en el producto la URL de descarga.

Si Storage todavia no esta habilitado, el producto se guarda igual y su campo
`imagen` queda como `asset://productos/archivo.jpg`; `CargadorImagenes` sabe leer
ese prefijo desde los recursos, de modo que **el catalogo nunca se ve sin
ilustrar**. Cuando Storage quede disponible, el panel del vendedor muestra la
tarjeta *"Subir las fotografias a Firebase Storage"*, que las migra una por una
y reemplaza las rutas locales por las URL de descarga.

Para reemplazar una foto por otra: editar el producto desde el panel del
vendedor y cargarla con la camara o desde la galeria.

---

## Compilar desde el repositorio

Abre la carpeta del proyecto en **Android Studio**. Android Studio regenera
`local.properties` con la ruta del SDK de tu equipo.

### Qué no está versionado

| Excluido | Motivo |
|---|---|
| `*.jks`, `keystore.properties` | Claves de firma de publicación |
| `local.properties` | Ruta del SDK, propia de cada equipo |
| `google-services.json` | Configuración de Firebase del proyecto original |
| `build/`, `.gradle/` | Artefactos de compilación |
| `*.apk` | Publicados en [Releases](../../releases) |

Si el proyecto usa Firebase, crea tu propio proyecto en la consola de Firebase y
coloca tu `google-services.json` en `app/`.
