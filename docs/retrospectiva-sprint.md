# NEXCODE Marketplace — Retrospectiva del sprint

Sprint del 28 de octubre al 2 de noviembre de 2026.
Formato: qué se hizo, qué salió bien, qué mejorar y acciones con responsable.

## Qué se hizo

| Fecha | Entrega | Dónde quedó |
|---|---|---|
| 28/10 | Planeación, requisitos trazados al código y mockups de las pantallas | `feature/planeacion-y-diseno` → `main` |
| 28/10 | Base SQLite fuera de los respaldos y `allowBackup` desactivado | `fix/respaldo-datos-locales` → `main` |
| 28/10 | 23 pruebas unitarias del modelo, el catálogo y los formatos; documentación de pruebas y correcciones | `test/pruebas-unitarias` → `main` |
| 30/10 | Cierre de la escalada a administrador y credenciales de firma fuera del repositorio | `feature/asistente-ia-y-seguridad` |
| 30/10 | Asistente de compras con IA (Gemini vía Firebase AI Logic) | `feature/asistente-ia-y-seguridad` |
| 30/10 | «Mis pedidos» ya no se queda cargando sin red | `feature/asistente-ia-y-seguridad` |
| 30/10 | Pruebas en dispositivo (plan T0–T7), manual de usuario y pruebas de autorización en el Playground | `feature/asistente-ia-y-seguridad` → PR #1, fusionado el 02/11 |
| 30/10 | Botón «Hablar con Leandro» en el chat: llamada de voz con el agente de Retell AI | `feature/agente-leandro`, **sin fusionar** |

## Qué salió bien

- **El asistente de compras pasó los 8 escenarios del plan T0–T7** en un teléfono físico y
  responde con el catálogo real, sin inventar productos ni precios.
- **Las pruebas de autorización encontraron y cerraron tres vulnerabilidades**: escalada a
  administrador, escritura en chats ajenos y manipulación del stock y de los votos.
- **La contraseña del keystore salió del README**: ahora se lee de un archivo fuera de git y
  se cambió conservando el mismo certificado.
- **Un tipo de rama por propósito** (`feature/`, `fix/`, `test/`) hizo fácil ver qué entraba
  en cada integración.
- El diagnóstico del asistente quedó más rápido gracias al registro del error original en
  Logcat.

## Qué mejorar

- **El asistente estuvo caído un tiempo** por dos causas a la vez: App Check en modo
  «aplicar» sin el SDK en la app (401) y un modelo que ya no existe para proyectos nuevos
  (404). Revisar la consola de Firebase antes de depurar el código habría ahorrado tiempo.
- **La integración del agente de voz no se cerró.** El botón «Hablar con Leandro» (Retell AI,
  con la llamada creada por un servidor en Cloudflare Workers) está en `feature/agente-leandro`
  sin PR ni revisión.
- **La clave de API de Retell llegó inválida** porque se copió de una página traducida
  automáticamente por el navegador.
- **Firma del APK y `google-services.json`**: en un clon nuevo faltaban los archivos locales
  y la APK salió con otra firma, así que no se pudo instalar sobre la versión anterior.
- **El antivirus AVG intercepta HTTPS** en uno de los equipos y bloqueó `git` y Gradle hasta
  configurar su certificado.

## Acciones

| # | Acción | Responsable | Para |
|---|---|---|---|
| 1 | Abrir el PR de `feature/agente-leandro` con el botón de voz probado en teléfono físico | Leo / revisa Jeferson | Próximo sprint |
| 2 | Lista de verificación de Firebase antes de depurar el asistente: modo de App Check, modelo disponible y cuota | Equipo | Desde ya |
| 3 | Copiar claves solo desde la consola original con la traducción del navegador desactivada | Equipo | Desde ya |
| 4 | Documentar qué archivos locales hacen falta para compilar (keystore, `keystore.properties`, `google-services.json`) y de dónde se obtienen, sin su contenido | Jeferson | Próximo sprint |
| 5 | Anotar cómo hacer que `git` y Gradle confíen en el certificado del antivirus sin desactivarlo | Leo | Próximo sprint |
| 6 | Historial de conversación en el asistente para preguntas de seguimiento | Equipo | Backlog |
| 7 | App Check con Play Integrity al publicar y pruebas instrumentadas con Espresso | Equipo | Backlog |
