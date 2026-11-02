# NEXCODE Marketplace — Manual de usuario

Aplicación Android de comercio electrónico. Requiere Android 7.0 o superior y conexión a
Internet. Las fotos y el texto de este manual corresponden a la versión 1.0.

## 1. Instalación

1. Instalar el APK (`entregables/` o la sección *Releases* del repositorio) o ejecutar el
   proyecto desde Android Studio en un emulador o teléfono.
2. Abrir la aplicación; aparece la pantalla de bienvenida y luego el inicio de sesión.

## 2. Crear una cuenta

### Comprador
1. En la pantalla de registro, escribir nombre, correo y contraseña.
2. Dejar **desactivado** el interruptor *"Quiero vender"*.
3. Pulsar **Registrarse**. La aplicación inicia sesión y abre el inicio.

### Vendedor (administrador)
1. En el registro, activar el interruptor *"Quiero vender: crear cuenta de administrador"*.
2. Al entrar con esa cuenta, si el catálogo está vacío la aplicación carga sola las 20
   referencias y, si Firebase Storage está habilitado, sube sus fotografías.

> Consejo: crea una cuenta de vendedor y otra de comprador para probar todo el flujo.

## 3. Comprar (perfil comprador)

| Acción | Cómo se hace |
|---|---|
| Buscar un producto | Escribir en la barra de búsqueda del catálogo (ignora mayúsculas y tildes). |
| Filtrar | Usar los filtros por categoría y precio del catálogo. |
| Ver el detalle | Tocar una tarjeta del catálogo. |
| Agregar al carrito | Botón *Agregar al carrito* en el detalle del producto. |
| Ver y editar el carrito | Pestaña **Carrito**: cambiar cantidades o quitar artículos; muestra el total. |
| Calificar un producto | Pantalla de calificación con estrellas. |
| Escribir al vendedor | Pestaña **Chat**. |
| Ver pedidos | Historial de pedidos desde el perfil. |

## 4. Vender (perfil administrador)

| Acción | Cómo se hace |
|---|---|
| Abrir el panel | Pestaña / botón **Panel del vendedor**. |
| Crear o editar un producto | *Registrar / editar producto*: nombre, precio, categoría, stock y foto. |
| Tomar la foto | Botón de cámara (CameraX) o elegir de la galería. |
| Subir fotos a Storage | Tarjeta *"Subir las fotografías a Firebase Storage"* del panel. |

## 5. Cerrar sesión

Desde **Perfil → Cerrar sesión**.

## 6. Problemas frecuentes

| Síntoma | Solución |
|---|---|
| La cuenta no deja entrar tras un registro con mala señal | Volver a iniciar sesión: la ficha se rehace sola y la cuenta vuelve como comprador. |
| El catálogo aparece sin fotos | Las fotos se muestran desde los recursos locales; cuando Firebase Storage esté habilitado, subirlas desde el panel del vendedor. |
| No carga nada | Revisar la conexión a Internet; todas las comunicaciones usan HTTPS. |
