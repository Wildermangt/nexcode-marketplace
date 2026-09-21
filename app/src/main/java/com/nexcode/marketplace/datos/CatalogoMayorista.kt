package com.nexcode.marketplace.datos

import com.nexcode.marketplace.dominio.EstadoProducto
import com.nexcode.marketplace.dominio.Producto

/**
 * Catalogo mayorista de partida: veinte referencias del portafolio que
 * distribuye Ingram Micro Colombia (computo, impresion, redes, energia y
 * perifericos).
 *
 * Para cada referencia se guarda el costo de distribuidor y el precio de venta
 * al publico, que es el costo mas el **30 % de margen** redondeado a la centena
 * de peso. La propiedad [Semilla.precio] es la que termina en Firestore.
 *
 * Las fotografias viajan dentro de la aplicacion, en {@code assets/productos}.
 * [SembradorCatalogo] las sube a Firebase Storage la primera vez y reemplaza la
 * ruta local por la URL de descarga.
 *
 * ARCHIVO GENERADO: se produce con {@code emitir_kotlin.py} a partir del
 * catalogo de referencia. No se edita a mano.
 */
data class Semilla(
    val referencia: String,
    val nombre: String,
    val marca: String,
    val categoria: String,
    val estado: EstadoProducto,
    val cantidad: Int,
    /** Costo del distribuidor, en pesos. */
    val costo: Double,
    /** Precio de venta: costo + 30 %. */
    val precio: Double,
    val calificacion: Double,
    val descripcion: String,
    val archivoFoto: String
) {
    /** Convierte la semilla en el producto que se guardara en Firestore. */
    fun aProducto(vendedorId: String, vendedorNombre: String, momento: Long) = Producto(
        nombre = nombre,
        descripcion = descripcion,
        precio = precio,
        categoria = categoria,
        estado = estado,
        cantidad = cantidad,
        imagen = "$RUTA_ASSETS/$archivoFoto",
        marca = marca,
        referencia = referencia,
        vendedorId = vendedorId,
        vendedorNombre = vendedorNombre,
        calificacion = calificacion,
        votos = 0,
        vendidos = 0,
        fechaRegistro = momento
    )
}

/** Prefijo que reconoce el cargador de imagenes para leer desde los recursos. */
const val RUTA_ASSETS = "asset://productos"

/** Margen aplicado sobre el costo del distribuidor. */
const val MARGEN = 0.30

object CatalogoMayorista {

    val SEMILLAS: List<Semilla> = listOf(
        Semilla(
            referencia = "HP-PB450G10-I7",
            nombre = "Portatil HP ProBook 450 G10 Core i7",
            marca = "HP",
            categoria = "Computo",
            estado = EstadoProducto.NUEVO,
            cantidad = 12,
            costo = 3850000.0,
            precio = 5005000.0,
            calificacion = 4.6,
            descripcion = "Portatil corporativo de 15,6 pulgadas Full HD con Intel Core i7-1355U, 16 GB DDR4 y SSD NVMe de 512 GB. Teclado retroiluminado, lector de huella y Windows 11 Pro.",
            archivoFoto = "hp_pb450g10_i7.jpg"
        ),
        Semilla(
            referencia = "LEN-TPE14G5-R5",
            nombre = "Portatil Lenovo ThinkPad E14 Gen 5 Ryzen 5",
            marca = "Lenovo",
            categoria = "Computo",
            estado = EstadoProducto.NUEVO,
            cantidad = 9,
            costo = 3250000.0,
            precio = 4225000.0,
            calificacion = 4.5,
            descripcion = "ThinkPad de 14 pulgadas con AMD Ryzen 5 7530U, 16 GB de RAM y SSD de 512 GB. Chasis con certificacion militar MIL-STD-810H y garantia de un ano en sitio.",
            archivoFoto = "len_tpe14g5_r5.jpg"
        ),
        Semilla(
            referencia = "DEL-LAT3540-I5",
            nombre = "Portatil Dell Latitude 3540 Core i5",
            marca = "Dell",
            categoria = "Computo",
            estado = EstadoProducto.USADO,
            cantidad = 5,
            costo = 2180000.0,
            precio = 2834000.0,
            calificacion = 4.1,
            descripcion = "Equipo reacondicionado grado A de 15,6 pulgadas con Core i5-1335U, 8 GB de RAM y SSD de 256 GB. Bateria con mas del 85 % de salud y seis meses de garantia.",
            archivoFoto = "del_lat3540_i5.jpg"
        ),
        Semilla(
            referencia = "DEL-P2422H",
            nombre = "Monitor Dell P2422H 23,8 pulgadas IPS",
            marca = "Dell",
            categoria = "Monitores",
            estado = EstadoProducto.NUEVO,
            cantidad = 24,
            costo = 620000.0,
            precio = 806000.0,
            calificacion = 4.7,
            descripcion = "Monitor profesional IPS Full HD con marcos ultradelgados, base ajustable en altura, giro y pivote, HDMI, DisplayPort y concentrador USB.",
            archivoFoto = "del_p2422h.jpg"
        ),
        Semilla(
            referencia = "LG-27UP550",
            nombre = "Monitor LG 27UP550 27 pulgadas 4K UHD",
            marca = "LG",
            categoria = "Monitores",
            estado = EstadoProducto.NUEVO,
            cantidad = 15,
            costo = 1180000.0,
            precio = 1534000.0,
            calificacion = 4.6,
            descripcion = "Panel IPS de 27 pulgadas con resolucion 4K UHD, cobertura del 95 % de DCI-P3, HDR10 y entrada USB-C con 60 W de carga.",
            archivoFoto = "lg_27up550.jpg"
        ),
        Semilla(
            referencia = "EPS-L3560",
            nombre = "Multifuncional Epson EcoTank L3560",
            marca = "Epson",
            categoria = "Impresion",
            estado = EstadoProducto.NUEVO,
            cantidad = 18,
            costo = 780000.0,
            precio = 1014000.0,
            calificacion = 4.4,
            descripcion = "Multifuncional de tanque de tinta con Wi-Fi Direct, impresion, escaneo y copia. Rinde hasta 7.500 paginas a color con el juego de botellas incluido.",
            archivoFoto = "eps_l3560.jpg"
        ),
        Semilla(
            referencia = "HP-M404DN",
            nombre = "Impresora HP LaserJet Pro M404dn",
            marca = "HP",
            categoria = "Impresion",
            estado = EstadoProducto.NUEVO,
            cantidad = 7,
            costo = 1420000.0,
            precio = 1846000.0,
            calificacion = 4.5,
            descripcion = "Impresora laser monocromatica de 40 ppm con duplex automatico y puerto de red Gigabit. Pensada para grupos de trabajo de alto volumen.",
            archivoFoto = "hp_m404dn.jpg"
        ),
        Semilla(
            referencia = "KIN-NV2-1TB",
            nombre = "SSD Kingston NV2 1 TB NVMe PCIe 4.0",
            marca = "Kingston",
            categoria = "Almacenamiento",
            estado = EstadoProducto.NUEVO,
            cantidad = 40,
            costo = 245000.0,
            precio = 318500.0,
            calificacion = 4.8,
            descripcion = "Unidad de estado solido M.2 2280 PCIe 4.0 NVMe con lecturas de hasta 3.500 MB/s. Ideal para actualizar portatiles y equipos de escritorio.",
            archivoFoto = "kin_nv2_1tb.jpg"
        ),
        Semilla(
            referencia = "WD-RED-4TB",
            nombre = "Disco duro WD Red Plus 4 TB NAS",
            marca = "Western Digital",
            categoria = "Almacenamiento",
            estado = EstadoProducto.NUEVO,
            cantidad = 16,
            costo = 465000.0,
            precio = 604500.0,
            calificacion = 4.6,
            descripcion = "Disco de 3,5 pulgadas a 5.400 rpm con 128 MB de cache, optimizado con tecnologia NASware para operacion continua 24/7 en servidores de archivos.",
            archivoFoto = "wd_red_4tb.jpg"
        ),
        Semilla(
            referencia = "KIN-FURY-16GB",
            nombre = "Memoria Kingston Fury Beast 16 GB DDR5",
            marca = "Kingston",
            categoria = "Componentes",
            estado = EstadoProducto.NUEVO,
            cantidad = 32,
            costo = 290000.0,
            precio = 377000.0,
            calificacion = 4.7,
            descripcion = "Modulo DIMM DDR5 de 16 GB a 5.200 MHz con perfil XMP 3.0 y disipador de bajo perfil. Compatible con plataformas Intel y AMD de ultima generacion.",
            archivoFoto = "kin_fury_16gb.jpg"
        ),
        Semilla(
            referencia = "LOG-MK540",
            nombre = "Combo teclado y mouse Logitech MK540",
            marca = "Logitech",
            categoria = "Perifericos",
            estado = EstadoProducto.NUEVO,
            cantidad = 28,
            costo = 185000.0,
            precio = 240500.0,
            calificacion = 4.3,
            descripcion = "Combo inalambrico de 2,4 GHz con receptor Unifying, teclas silenciosas de perfil bajo y hasta 36 meses de autonomia en el teclado.",
            archivoFoto = "log_mk540.jpg"
        ),
        Semilla(
            referencia = "LOG-ZONE-VIBE",
            nombre = "Diadema Logitech Zone Vibe 100",
            marca = "Logitech",
            categoria = "Perifericos",
            estado = EstadoProducto.NUEVO,
            cantidad = 21,
            costo = 340000.0,
            precio = 442000.0,
            calificacion = 4.2,
            descripcion = "Diadema Bluetooth ligera de 185 g con microfono de brazo abatible, cancelacion de ruido en la llamada y 18 horas de conversacion.",
            archivoFoto = "log_zone_vibe.jpg"
        ),
        Semilla(
            referencia = "LOG-C920E",
            nombre = "Camara web Logitech C920e Full HD",
            marca = "Logitech",
            categoria = "Perifericos",
            estado = EstadoProducto.NUEVO,
            cantidad = 26,
            costo = 265000.0,
            precio = 344500.0,
            calificacion = 4.5,
            descripcion = "Camara de videoconferencia 1080p a 30 fps con dos microfonos omnidireccionales, correccion automatica de luz y tapa de privacidad.",
            archivoFoto = "log_c920e.jpg"
        ),
        Semilla(
            referencia = "APC-BX1500M",
            nombre = "UPS APC Back-UPS 1500VA BX1500M",
            marca = "APC",
            categoria = "Energia",
            estado = EstadoProducto.NUEVO,
            cantidad = 11,
            costo = 890000.0,
            precio = 1157000.0,
            calificacion = 4.6,
            descripcion = "Sistema de respaldo de 1500 VA / 900 W con diez tomas, regulacion automatica de voltaje, pantalla LCD y puerto USB de apagado seguro.",
            archivoFoto = "apc_bx1500m.jpg"
        ),
        Semilla(
            referencia = "UBI-UDR",
            nombre = "Ubiquiti UniFi Dream Router UDR",
            marca = "Ubiquiti",
            categoria = "Redes",
            estado = EstadoProducto.NUEVO,
            cantidad = 8,
            costo = 780000.0,
            precio = 1014000.0,
            calificacion = 4.8,
            descripcion = "Router todo en uno con controlador UniFi integrado, Wi-Fi 6 de doble banda, cuatro puertos Gigabit, dos con PoE y ranura microSD para grabacion.",
            archivoFoto = "ubi_udr.jpg"
        ),
        Semilla(
            referencia = "TPL-EAP245",
            nombre = "Punto de acceso TP-Link EAP245 AC1750",
            marca = "TP-Link",
            categoria = "Redes",
            estado = EstadoProducto.NUEVO,
            cantidad = 19,
            costo = 320000.0,
            precio = 416000.0,
            calificacion = 4.4,
            descripcion = "Access point de techo AC1750 con MU-MIMO, alimentacion PoE 802.3af y gestion centralizada desde el controlador Omada.",
            archivoFoto = "tpl_eap245.jpg"
        ),
        Semilla(
            referencia = "CIS-CBS110-24T",
            nombre = "Switch Cisco CBS110-24T 24 puertos",
            marca = "Cisco",
            categoria = "Redes",
            estado = EstadoProducto.USADO,
            cantidad = 4,
            costo = 430000.0,
            precio = 559000.0,
            calificacion = 4.0,
            descripcion = "Switch no administrable de 24 puertos Gigabit en chasis metalico para rack. Equipo retirado de operacion, probado puerto por puerto, con seis meses de garantia.",
            archivoFoto = "cis_cbs110_24t.jpg"
        ),
        Semilla(
            referencia = "FOR-FG40F",
            nombre = "Firewall Fortinet FortiGate 40F",
            marca = "Fortinet",
            categoria = "Seguridad",
            estado = EstadoProducto.NUEVO,
            cantidad = 6,
            costo = 2150000.0,
            precio = 2795000.0,
            calificacion = 4.7,
            descripcion = "Appliance de seguridad perimetral con 5 Gbps de inspeccion de firewall, cinco puertos GE RJ45 y soporte para SD-WAN e inspeccion SSL.",
            archivoFoto = "for_fg40f.jpg"
        ),
        Semilla(
            referencia = "SAM-TABA9PLUS",
            nombre = "Tablet Samsung Galaxy Tab A9+ 64 GB",
            marca = "Samsung",
            categoria = "Movilidad",
            estado = EstadoProducto.USADO,
            cantidad = 7,
            costo = 610000.0,
            precio = 793000.0,
            calificacion = 4.2,
            descripcion = "Tablet reacondicionada de 11 pulgadas a 90 Hz con Snapdragon 695, 4 GB de RAM, 64 GB ampliables y bateria de 7.040 mAh. Incluye cargador original.",
            archivoFoto = "sam_taba9plus.jpg"
        ),
        Semilla(
            referencia = "EPS-PLE20",
            nombre = "Videoproyector Epson PowerLite E20",
            marca = "Epson",
            categoria = "Video",
            estado = EstadoProducto.NUEVO,
            cantidad = 10,
            costo = 1560000.0,
            precio = 2028000.0,
            calificacion = 4.3,
            descripcion = "Proyector 3LCD XGA con 3.400 lumenes de brillo en color y en blanco, entrada HDMI y lampara con hasta 12.000 horas en modo economico.",
            archivoFoto = "eps_ple20.jpg"
        ),
    )

    /** Valor del inventario a precio de venta, usado en el panel del vendedor. */
    fun valorInventario(): Double = SEMILLAS.sumOf { it.precio * it.cantidad }
}
