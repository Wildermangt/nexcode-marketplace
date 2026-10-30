# NEXCODE Marketplace — Análisis estático del APK (Kali Linux)

Prueba de seguridad de caja blanca sobre la APK de publicación, realizada el 29 de
octubre de 2026 en un laboratorio aislado.

## Entorno y método

| Componente | Detalle |
|---|---|
| Atacante | Kali Linux 2026.2 (VirtualBox), red Host-Only |
| APK analizada | `APK/NEXCODE-Marketplace-1.0-2026-10-29.apk` (13 MB) |
| Herramientas | `apktool`, `jadx`, `unzip` + `strings`/`grep` sobre `classes*.dex` |

Se decompiló la APK, se revisó el manifiesto (banderas, permisos, componentes
exportados) y se buscaron secretos en recursos y en el bytecode.

## Resultado general

| Control | Resultado |
|---|---|
| Firma | Esquema **v2** válido · huella SHA-256 `86:a1:10:d5…f2:ea:95` |
| `android:allowBackup` | **false** ✔ (corrección del 28/10 presente en la APK) |
| `android:debuggable` | ausente ✔ |
| Tráfico en claro | sin `usesCleartextTraffic`; bloqueado por defecto con `targetSdk 35` ✔ |
| Ofuscación (R8) | **desactivada** (`minifyEnabled = false`): los nombres de clases y métodos propios quedan legibles |
| minSdk / targetSdk | 24 / 35 |

## Permisos

`INTERNET` y `CAMERA` (fotos de producto). Sin permisos de ubicación, contactos,
almacenamiento amplio ni `QUERY_ALL_PACKAGES`.

## Componentes exportados

Solo `SplashActivity` (launcher) y los componentes estándar de Firebase Auth, Google
Play Services y `profileinstaller`. Sin componentes propios expuestos de más.

## Búsqueda de secretos

| Qué se buscó | Resultado |
|---|---|
| Claves `AIza…` | **Una sola**, en `resources.arsc`: la clave de Firebase (`AIzaSyCoCz…gU6k`), pública por diseño |
| Contraseñas de firma | No aparecen en el APK ✔ (la corrección que las movió a `keystore.properties` está aplicada) |
| URLs de backend/webhook propios | Ninguna; la app habla directamente con Firebase |

## Hallazgos y recomendaciones

| ID | Hallazgo | Severidad | Recomendación |
|---|---|---|---|
| APK-01 | El APK **no está ofuscado** (`minifyEnabled = false`): con `jadx` se leen los nombres de clases, métodos y lógica de negocio (`dominio/Pedido`, `RepositorioCarrito`, etc.). No filtra secretos, pero facilita la ingeniería inversa | BAJA | Activar R8 (`minifyEnabled = true` con `proguard-rules.pro`) en la variante de publicación, como ya hace Control de Gastos |
| APK-02 | Clave de API de Firebase en los recursos (inevitable, debe restringirse) | BAJA | En Google Cloud Console, restringir la clave por **aplicación Android** (paquete `com.nexcode.marketplace` + SHA-256 de firma) y por **API** |
| APK-03 | La seguridad real de los datos depende de las **reglas de Firestore**, no del cliente. Una cuenta «administrador» puede leer **todas** las conversaciones de la tienda, incluidas las de otros clientes (ver `firestore.rules`), y el registro deja crear cuentas admin sin aprobación | MEDIA | Revisar el modelo de roles: aprobar las cuentas de vendedor, y acotar en las reglas qué conversaciones ve cada administrador |

**Conclusión:** la APK aplica configuraciones seguras por defecto y no filtra las
contraseñas de firma (ya corregidas). Quedan dos mejoras: activar la ofuscación con R8 y
revisar el modelo de roles/permisos en las reglas de Firestore, que es donde se decide el
acceso real a los datos.

> Nota: el análisis de las reglas de Firestore y del modelo de roles corresponde a la
> capa de servidor; aquí se señala porque el cliente por sí solo no puede garantizar esos
> controles.
