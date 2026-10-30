# NEXCODE Marketplace — Seguridad: permisos, autorización y hallazgos

## 1. Permisos declarados y justificación

El `AndroidManifest.xml` declara solo tres permisos:

| Permiso | Para qué se usa | ¿Peligroso? |
|---|---|---|
| `INTERNET` | Consumir Firebase (Auth, Firestore, Storage). | No |
| `ACCESS_NETWORK_STATE` | Saber si hay conexión antes de sincronizar. | No |
| `CAMERA` | Tomar la foto del producto en el panel del vendedor (CameraX). | Sí (se pide en tiempo de ejecución) |

No se piden permisos de ubicación, contactos, almacenamiento externo ni micrófono. El
único peligroso, `CAMERA`, se solicita en tiempo de ejecución justo antes de abrir la
cámara y la aplicación funciona como comprador sin concederlo.

## 2. Control de acceso y autorización

- **Autenticación**: Firebase Auth con correo y contraseña.
- **Reglas de Firestore** (`firestore.rules`): cada documento de usuario y de carrito se
  restringe por `uid`; la escritura del catálogo exige rol de administrador.
- **Separación de roles**: el panel del vendedor solo es accesible para cuentas creadas
  como administrador.

### Pruebas de autorización entre usuarios (Firestore Rules Playground, 29/10/2026)

Ejecutadas desde la consola de Firebase (*Rules → Playground*) del proyecto
`nexcode-marketplace`. Los pasos reutilizables están en
`04-seguridad/Pruebas-Autorizacion-Firestore-Playground.md` (carpeta del proyecto final, junto a los tres repositorios).

| # | Caso | Resultado |
|---|---|---|
| CTRL | El dueño lee su propio pedido | ✅ Permitido |
| A1 | Usuario B lee el pedido de otro comprador | ❌ Denegado |
| A2 | Usuario B lee el chat de otro comprador | ❌ Denegado |
| A3 | Comprador intenta editar la ficha completa del producto (p. ej. `precio`) | ❌ Denegado |

La primera corrida (con las reglas anteriores) destapó **dos vulnerabilidades reales**, que
se corrigieron el mismo día (ver sección 4, V1 y V2) y se volvieron a probar hasta quedar
denegadas. Las reglas corregidas se publicaron el **29/10/2026, 11:36 p.m.** y el re-test dio:

| Vulnerabilidad | Re-test (Playground) | Captura |
|---|---|---|
| V1 escalada a admin | ❌ Denegado (falla `!= 'administrador'`) | `mk-auth-V1-denegado.png` |
| V2 inyección en chat | ❌ Denegado limpio | `mk-auth-V2-denegado.png` |
| V3 inflar stock del producto | ❌ Denegado sobre producto real (`5hFKv5qDtmxaVUSYHRMU`) | `mk-auth-V3-denegado-producto-real.png` |

Las capturas están en `docs/evidencias-pruebas/` (o la carpeta de seguridad del proyecto).

## 2.b Vulnerabilidades de autorización encontradas y corregidas

El valor de esta sección para la sustentación es mostrar que la prueba **encontró fallos
reales y se corrigieron**, no esconderlos.

- **V1 — Escalada de privilegios (crítica).** Con las reglas anteriores, cualquier cuenta
  podía crear su ficha `usuarios/{uid}` con `tipoUsuario: "administrador"` (el interruptor
  "Quiero vender" lo hacía desde el cliente). Un administrador puede editar el catálogo y
  los pedidos y leer todos los chats → cualquiera podía tomar ese control.
- **V2 — Inyección en chat ajeno.** Al crear un mensaje solo se validaba que `autorId` fuera
  el propio `uid`, no que la persona perteneciera a esa conversación: un usuario podía
  escribir mensajes en el chat de otro comprador.

Corrección y verificación en la sección 4.

## 3. Almacenamiento y comunicaciones

- `allowBackup="false"`: la aplicación queda excluida de los respaldos automáticos.
- La base SQLite local solo guarda el catálogo (datos públicos), no credenciales.
- Todas las URL son HTTPS.

## 4. Hallazgos y correcciones

| ID | Hallazgo | Riesgo | Corrección |
|---|---|---|---|
| S-01 | La contraseña del keystore estaba en texto plano en el README (y en `app/build.gradle` antes del 29/10) | ALTO: con el `.jks` y la contraseña se pueden firmar APK como si fueran del equipo | **Cerrado 30/10/2026.** Se quitó la contraseña del README y de `build.gradle`: ahora se lee de `keystore.properties` (excluido por `.gitignore`). Se **cambió la contraseña** del almacén y de la llave (`keytool -storepasswd`, almacén PKCS12): la vieja ya no abre el `.jks`, y el certificado sigue siendo el mismo (SHA-256 `86:A1:10:D5…EA:95`), así que el APK se instala encima de versiones anteriores. El `.jks` nunca se subió al repositorio (`git log --all -- '*.jks'` sin resultados), así que la contraseña vieja que sigue en el historial no basta para firmar |
| S-02 | El agente de IA (asistente de compras) usa Gemini vía Firebase AI Logic, por lo que **no hay clave de IA en el APK** | — | No aplica (ventaja de seguridad: la clave la gestiona Firebase, confirmado en `docs/04`) |
| V1 | **Escalada de privilegios:** cualquier cuenta podía registrarse como `administrador` desde el cliente (interruptor "Quiero vender") | CRÍTICO: control total del catálogo, pedidos y chats | **Reglas:** `create` en `usuarios` rechaza `tipoUsuario == 'administrador'` y `update` no deja cambiar el propio `tipoUsuario`. **App:** el registro siempre crea comprador; se eliminó el interruptor. Los vendedores se dan de alta a mano en la consola de Firebase. Re-probado en el Playground → ❌ Denegado |
| V2 | **Inyección en chat ajeno:** crear un mensaje solo exigía que `autorId` fuera el propio `uid` | MEDIO: un usuario podía escribir en el chat de otro comprador | **Reglas:** crear mensaje exige ser el dueño del chat (el `comprador`) o un administrador. Re-probado → ❌ Denegado |
| V3 | **Manipulación del producto:** un comprador podía escribir cualquier valor en `cantidad`/`vendidos` o `calificacion`/`votos` (p. ej. inflar el stock) | MEDIO | **Reglas:** el comprador solo puede **bajar** el stock (con `vendidos` coherente) y subir los votos de a uno (calificación 0–5); lo demás, solo el administrador |
| S-03 | Lectura de la colección `usuarios`: cualquier cuenta podía leer la ficha de otro usuario | BAJO (solo nombre y tipo) | **Reglas:** `read` en `usuarios/{uid}` solo para el dueño o un administrador. No rompe ninguna pantalla: el nombre del vendedor/comprador viaja desnormalizado en el producto y en el chat |

## 4.b Restricción de la clave de API de Firebase (30/10/2026)

La "Android key (auto created by Firebase)" del proyecto `nexcode-marketplace` se restringió en
Google Cloud Console a **Apps de Android**, con el nombre de paquete `com.nexcode.marketplace`
y las dos huellas SHA-1 de firma (debug `35:90:BD:…:5C:6F` y release
`3C:39:2C:…:AF:A8`). La restricción por API se dejó sin cambios (las 25 APIs que Firebase
configura al crear la clave, entre ellas **"Firebase AI Logic API"**). Captura:
`key-restriccion-marketplace.png`.

**IMPORTANTE — la restricción por app NO es compatible con este asistente (revertida).** El
asistente usa `GenerativeBackend.googleAI()` (Gemini Developer API), que llama a
`generativelanguage.googleapis.com` **sin** enviar la identidad de la app (el paquete y la
huella llegan vacíos). Con la restricción "Apps de Android" activa, Google rechaza la petición
con `requests from this android client application <empty> are blocked`
(`API_KEY_ANDROID_APP_BLOCKED`) y el asistente responde "no pude responder en este momento".
Verificado en dispositivo el 30/10.

Por eso **se quitó la restricción de aplicación** de la Android key y se dejó solo la
restricción por API (las 25 APIs, incluida *Firebase AI Logic API*). La protección correcta de
esta clave para un cliente Android que usa Firebase AI Logic es **Firebase App Check** (Play
Integrity), no la restricción por paquete/SHA; queda como mejora pendiente. El hallazgo APK-02
(clave de Firebase sin restringir por app) se mantiene como riesgo BAJO documentado, a cerrar
con App Check.

## 5. Análisis estático

Ver `docs/04-analisis-estatico-apk.md` (análisis del APK con Kali Linux).

## Nota

Este documento complementa el informe de BadStore (`seguridad/`), que es un laboratorio
sobre una aplicación deliberadamente vulnerable y no sobre esta app.
