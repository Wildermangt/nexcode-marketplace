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
- El catálogo nunca se ve sin ilustrar: si Firebase Storage no está habilitado, las fotos
  se leen desde los *assets* locales (`asset://`) y luego se migran a Storage desde el
  panel del vendedor.

## Dificultades encontradas y cómo se resolvieron

| Dificultad | Solución aplicada |
|---|---|
| Sincronizar el catálogo entre SQLite (local) y Firestore (remoto) | `AlmacenLocal` guarda una copia local y los repositorios consultan primero el remoto y respaldan en local; el catálogo se siembra una sola vez con `SembradorCatalogo`. |
| Cuentas creadas con la red a medias que luego no dejaban entrar | Al volver a iniciar sesión, la ficha del usuario se rehace sola y la cuenta vuelve como comprador. |
| Mostrar fotos antes de habilitar Storage | `CargadorImagenes` entiende el prefijo `asset://` y pinta la imagen desde los recursos; cuando Storage queda disponible, el panel del vendedor sube las fotos y reemplaza las rutas. |
| Imágenes del catálogo homogéneas | Scripts `descargar_fotos.py` y `montar_fotos.py` normalizan las 20 fotos a 900×900 sobre el fondo de la marca. |

## Mejoras futuras

- **Agente de inteligencia artificial**: es la carencia principal frente a la rúbrica.
  Marketplace no integra un modelo de IA como sí lo hacen HuellitasPlus (Gemini vía
  Firebase AI Logic) y Control de Gastos (Gemini por voz). Propuesta: un asistente de
  compras que recomiende productos o responda dudas del catálogo con Gemini a través de
  Firebase AI Logic (para no exponer la clave en el APK), con un aviso visible de que el
  usuario habla con una IA.
- **Pasarela de pago real** en lugar de la simulación del carrito.
- **Notificaciones push** del estado del pedido con Firebase Cloud Messaging.
- **Pruebas instrumentadas de interfaz** (Espresso) además de las 23 pruebas unitarias.
- **Seguridad**: retirar la contraseña del keystore del README (ver
  `docs/seguridad-permisos-y-autorizacion.md`) y documentar pruebas de autorización entre
  usuarios.
