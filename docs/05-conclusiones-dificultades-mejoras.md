# NEXCODE Marketplace — Conclusiones, dificultades y mejoras

## Conclusiones

- Se construyó una aplicación Android funcional de comercio electrónico con catálogo,
  búsqueda y filtros, carrito, calificaciones, chat, perfil y panel de vendedor, sobre
  Firebase (Authentication, Firestore y Storage) y una base local SQLite para consulta
  sin conexión.
- La autenticación por correo y contraseña con Firebase Auth, junto con reglas de
  Firestore por `uid` y rol de administrador, separa lo que puede hacer un comprador de
  lo que puede hacer un vendedor.
- El contenedor manual de dependencias (`NexcodeMarketplace.kt`), sin Hilt, mantiene a la
  vista qué clase depende de cuál, lo que facilita explicar la arquitectura en la
  sustentación.
- El **asistente de compras con IA** (Gemini vía Firebase AI Logic, sin clave en el APK)
  recomienda productos usando el catálogo real como contexto, no inventa referencias ni
  precios y explica el flujo de compra. Pasó los 8 escenarios del plan T0–T7 en un teléfono
  físico (`03-pruebas-y-correcciones.md`, sección 5).
- Las pruebas de autorización en el Rules Playground encontraron y cerraron tres
  vulnerabilidades: escalada a administrador, escritura en chats ajenos y manipulación del
  stock y los votos (`seguridad-permisos-y-autorizacion.md`).
- El catálogo nunca se ve sin ilustrar: si Firebase Storage no está habilitado, las fotos
  se leen desde los *assets* locales (`asset://`) y luego se migran a Storage desde el
  panel del vendedor.

## Dificultades encontradas y cómo se resolvieron

| Dificultad | Solución aplicada |
|---|---|
| Sincronizar el catálogo entre SQLite (local) y Firestore (remoto) | `AlmacenLocal` guarda una copia local y los repositorios consultan primero el remoto y respaldan en local; el catálogo se siembra una sola vez con `SembradorCatalogo`. |
| Cuentas creadas con la red a medias que luego no dejaban entrar | Al volver a iniciar sesión, la ficha del usuario se rehace sola y la cuenta vuelve como comprador. |
| Mostrar fotos antes de habilitar Storage | `CargadorImagenes` entiende el prefijo `asset://` y pinta la imagen desde los recursos; cuando Storage queda disponible, el panel del vendedor sube las fotos y reemplaza las rutas. |
| El asistente de IA no respondía: App Check estaba en modo «aplicar» sin el SDK en la app (401) y el modelo `gemini-2.5-flash` ya no existe para proyectos nuevos (404) | App Check de Firebase AI Logic en «no aplicar» y modelo `gemini-flash-lite-latest`. Se agregó un registro del error original en Logcat para diagnosticar más rápido |
| Ante «¿Cómo pago?» el agente remitía a una tienda física, porque solo conocía el catálogo | El prompt describe el flujo real de compra: carrito → Finalizar compra → Mis pedidos → acuerdo de pago por chat |
| La contraseña del keystore estaba publicada en el README | Se quitó, se lee de `keystore.properties` (fuera de git) y se cambió con `keytool`, conservando el mismo certificado |
| Imágenes del catálogo homogéneas | Scripts `descargar_fotos.py` y `montar_fotos.py` normalizan las 20 fotos a 900×900 sobre el fondo de la marca. |

## Mejoras futuras

- **App Check con Play Integrity** al publicar en Google Play, para que solo la app firmada
  use Firestore y Gemini (hoy está en «no aplicar», porque la app se distribuye como APK).
- **Historial de conversación en el asistente**, para que entienda preguntas de
  seguimiento («¿y una más barata?»).
- **Pasarela de pago real** en lugar de la simulación del carrito.
- **Notificaciones push** del estado del pedido con Firebase Cloud Messaging.
- **Pruebas instrumentadas de interfaz** (Espresso) además de las 23 pruebas unitarias.
