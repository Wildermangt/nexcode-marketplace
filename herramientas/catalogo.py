# -*- coding: utf-8 -*-
"""Catalogo NEXCODE Marketplace: 20 referencias del portafolio mayorista
Ingram Micro Colombia. "costo" es el precio de distribuidor en pesos;
"precio" es el precio de venta al publico con el 30 % de margen aplicado."""

CATALOGO = [
    dict(sku="HP-PB450G10-I7",  nombre="Portatil HP ProBook 450 G10 Core i7",
         marca="HP", categoria="Computo", forma="portatil", estado="Nuevo", cantidad=12,
         costo=3850000, calificacion=4.6,
         desc="Portatil corporativo de 15,6 pulgadas Full HD con Intel Core i7-1355U, 16 GB DDR4 y SSD NVMe de 512 GB. Teclado retroiluminado, lector de huella y Windows 11 Pro."),
    dict(sku="LEN-TPE14G5-R5", nombre="Portatil Lenovo ThinkPad E14 Gen 5 Ryzen 5",
         marca="Lenovo", categoria="Computo", forma="portatil", estado="Nuevo", cantidad=9,
         costo=3250000, calificacion=4.5,
         desc="ThinkPad de 14 pulgadas con AMD Ryzen 5 7530U, 16 GB de RAM y SSD de 512 GB. Chasis con certificacion militar MIL-STD-810H y garantia de un ano en sitio."),
    dict(sku="DEL-LAT3540-I5", nombre="Portatil Dell Latitude 3540 Core i5",
         marca="Dell", categoria="Computo", forma="portatil", estado="Usado", cantidad=5,
         costo=2180000, calificacion=4.1,
         desc="Equipo reacondicionado grado A de 15,6 pulgadas con Core i5-1335U, 8 GB de RAM y SSD de 256 GB. Bateria con mas del 85 % de salud y seis meses de garantia."),
    dict(sku="DEL-P2422H",     nombre="Monitor Dell P2422H 23,8 pulgadas IPS",
         marca="Dell", categoria="Monitores", forma="monitor", estado="Nuevo", cantidad=24,
         costo=620000, calificacion=4.7,
         desc="Monitor profesional IPS Full HD con marcos ultradelgados, base ajustable en altura, giro y pivote, HDMI, DisplayPort y concentrador USB."),
    dict(sku="LG-27UP550",     nombre="Monitor LG 27UP550 27 pulgadas 4K UHD",
         marca="LG", categoria="Monitores", forma="monitor", estado="Nuevo", cantidad=15,
         costo=1180000, calificacion=4.6,
         desc="Panel IPS de 27 pulgadas con resolucion 4K UHD, cobertura del 95 % de DCI-P3, HDR10 y entrada USB-C con 60 W de carga."),
    dict(sku="EPS-L3560",      nombre="Multifuncional Epson EcoTank L3560",
         marca="Epson", categoria="Impresion", forma="impresora", estado="Nuevo", cantidad=18,
         costo=780000, calificacion=4.4,
         desc="Multifuncional de tanque de tinta con Wi-Fi Direct, impresion, escaneo y copia. Rinde hasta 7.500 paginas a color con el juego de botellas incluido."),
    dict(sku="HP-M404DN",      nombre="Impresora HP LaserJet Pro M404dn",
         marca="HP", categoria="Impresion", forma="impresora", estado="Nuevo", cantidad=7,
         costo=1420000, calificacion=4.5,
         desc="Impresora laser monocromatica de 40 ppm con duplex automatico y puerto de red Gigabit. Pensada para grupos de trabajo de alto volumen."),
    dict(sku="KIN-NV2-1TB",    nombre="SSD Kingston NV2 1 TB NVMe PCIe 4.0",
         marca="Kingston", categoria="Almacenamiento", forma="ssd", estado="Nuevo", cantidad=40,
         costo=245000, calificacion=4.8,
         desc="Unidad de estado solido M.2 2280 PCIe 4.0 NVMe con lecturas de hasta 3.500 MB/s. Ideal para actualizar portatiles y equipos de escritorio."),
    dict(sku="WD-RED-4TB",     nombre="Disco duro WD Red Plus 4 TB NAS",
         marca="Western Digital", categoria="Almacenamiento", forma="disco", estado="Nuevo", cantidad=16,
         costo=465000, calificacion=4.6,
         desc="Disco de 3,5 pulgadas a 5.400 rpm con 128 MB de cache, optimizado con tecnologia NASware para operacion continua 24/7 en servidores de archivos."),
    dict(sku="KIN-FURY-16GB",  nombre="Memoria Kingston Fury Beast 16 GB DDR5",
         marca="Kingston", categoria="Componentes", forma="ram", estado="Nuevo", cantidad=32,
         costo=290000, calificacion=4.7,
         desc="Modulo DIMM DDR5 de 16 GB a 5.200 MHz con perfil XMP 3.0 y disipador de bajo perfil. Compatible con plataformas Intel y AMD de ultima generacion."),
    dict(sku="LOG-MK540",      nombre="Combo teclado y mouse Logitech MK540",
         marca="Logitech", categoria="Perifericos", forma="teclado", estado="Nuevo", cantidad=28,
         costo=185000, calificacion=4.3,
         desc="Combo inalambrico de 2,4 GHz con receptor Unifying, teclas silenciosas de perfil bajo y hasta 36 meses de autonomia en el teclado."),
    dict(sku="LOG-ZONE-VIBE",  nombre="Diadema Logitech Zone Vibe 100",
         marca="Logitech", categoria="Perifericos", forma="diadema", estado="Nuevo", cantidad=21,
         costo=340000, calificacion=4.2,
         desc="Diadema Bluetooth ligera de 185 g con microfono de brazo abatible, cancelacion de ruido en la llamada y 18 horas de conversacion."),
    dict(sku="LOG-C920E",      nombre="Camara web Logitech C920e Full HD",
         marca="Logitech", categoria="Perifericos", forma="camara", estado="Nuevo", cantidad=26,
         costo=265000, calificacion=4.5,
         desc="Camara de videoconferencia 1080p a 30 fps con dos microfonos omnidireccionales, correccion automatica de luz y tapa de privacidad."),
    dict(sku="APC-BX1500M",    nombre="UPS APC Back-UPS 1500VA BX1500M",
         marca="APC", categoria="Energia", forma="ups", estado="Nuevo", cantidad=11,
         costo=890000, calificacion=4.6,
         desc="Sistema de respaldo de 1500 VA / 900 W con diez tomas, regulacion automatica de voltaje, pantalla LCD y puerto USB de apagado seguro."),
    dict(sku="UBI-UDR",        nombre="Ubiquiti UniFi Dream Router UDR",
         marca="Ubiquiti", categoria="Redes", forma="router", estado="Nuevo", cantidad=8,
         costo=780000, calificacion=4.8,
         desc="Router todo en uno con controlador UniFi integrado, Wi-Fi 6 de doble banda, cuatro puertos Gigabit, dos con PoE y ranura microSD para grabacion."),
    dict(sku="TPL-EAP245",     nombre="Punto de acceso TP-Link EAP245 AC1750",
         marca="TP-Link", categoria="Redes", forma="ap", estado="Nuevo", cantidad=19,
         costo=320000, calificacion=4.4,
         desc="Access point de techo AC1750 con MU-MIMO, alimentacion PoE 802.3af y gestion centralizada desde el controlador Omada."),
    dict(sku="CIS-CBS110-24T", nombre="Switch Cisco CBS110-24T 24 puertos",
         marca="Cisco", categoria="Redes", forma="switch", estado="Usado", cantidad=4,
         costo=430000, calificacion=4.0,
         desc="Switch no administrable de 24 puertos Gigabit en chasis metalico para rack. Equipo retirado de operacion, probado puerto por puerto, con seis meses de garantia."),
    dict(sku="FOR-FG40F",      nombre="Firewall Fortinet FortiGate 40F",
         marca="Fortinet", categoria="Seguridad", forma="firewall", estado="Nuevo", cantidad=6,
         costo=2150000, calificacion=4.7,
         desc="Appliance de seguridad perimetral con 5 Gbps de inspeccion de firewall, cinco puertos GE RJ45 y soporte para SD-WAN e inspeccion SSL."),
    dict(sku="SAM-TABA9PLUS",  nombre="Tablet Samsung Galaxy Tab A9+ 64 GB",
         marca="Samsung", categoria="Movilidad", forma="tablet", estado="Usado", cantidad=7,
         costo=610000, calificacion=4.2,
         desc="Tablet reacondicionada de 11 pulgadas a 90 Hz con Snapdragon 695, 4 GB de RAM, 64 GB ampliables y bateria de 7.040 mAh. Incluye cargador original."),
    dict(sku="EPS-PLE20",      nombre="Videoproyector Epson PowerLite E20",
         marca="Epson", categoria="Video", forma="proyector", estado="Nuevo", cantidad=10,
         costo=1560000, calificacion=4.3,
         desc="Proyector 3LCD XGA con 3.400 lumenes de brillo en color y en blanco, entrada HDMI y lampara con hasta 12.000 horas en modo economico."),
]


def precio_venta(costo):
    """Precio de lista = costo del distribuidor + 30 % de margen,
    redondeado a la centena de peso mas cercana."""
    return int(round(costo * 1.30 / 100.0) * 100)


for _p in CATALOGO:
    _p["precio"] = precio_venta(_p["costo"])
    _p["archivo"] = _p["sku"].lower().replace("-", "_") + ".jpg"


if __name__ == "__main__":
    for p in CATALOGO:
        print(f'{p["sku"]:<16} {p["costo"]:>10,} -> {p["precio"]:>10,}  {p["nombre"][:44]}')
    print("productos:", len(CATALOGO))
