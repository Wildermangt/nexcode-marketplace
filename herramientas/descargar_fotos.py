# -*- coding: utf-8 -*-
"""Descarga la fotografia real de cada referencia del catalogo.

Para cada SKU se busca la imagen del producto y se conserva la primera
candidata que venga de un dominio de confianza (el fabricante o un mayorista
conocido) y tenga resolucion suficiente. El archivo crudo se guarda con el
nombre de la referencia; el montaje final lo hace montar_fotos.py.
"""
import html
import io
import re
import subprocess
import sys
import time
from pathlib import Path

from catalogo import CATALOGO

AGENTE = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
          "(KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36")

# Dominios en los que confiamos: fabricantes y mayoristas.
CONFIABLES = [
    "hp.com", "hp-hosting", "ssl-product-images", "lenovo.com", "static.lenovo",
    "p1-ofp.static.pub", "p2-ofp.static.pub", "dell.com", "i.dell.com",
    "lg.com", "lgcdn", "epson.com", "epson.com.co", "mediaserver.epson",
    "kingston.com", "westerndigital.com", "sandisk.com", "logitech.com",
    "cisco.com", "fortinet.com", "samsung.com", "tp-link.com", "ui.com",
    "apc.com", "schneider-electric.com", "se.com", "corsair.com",
    "newegg.com", "bhphotovideo.com", "cdw.com", "insight.com",
    "provantage.com", "ingrammicro.com", "m.media-amazon.com", "static.bhphoto",
    "connection.com", "shi.com", "compuindia", "pcconnection.com",
]

# Consulta de busqueda por referencia.
CONSULTAS = {
    "HP-PB450G10-I7": "HP ProBook 450 G10 notebook product photo",
    "LEN-TPE14G5-R5": "Lenovo ThinkPad E14 Gen 5 AMD laptop product photo",
    "DEL-LAT3540-I5": "Dell Latitude 3540 laptop open front view white background no text",
    "DEL-P2422H": "Dell P2422H monitor front view white background isolated",
    "LG-27UP550": "LG 27UP550N-W monitor front view white background isolated",
    "EPS-L3560": "Epson EcoTank L3560 multifunction printer product photo",
    "HP-M404DN": "HP LaserJet Pro M404dn printer product photo",
    "KIN-NV2-1TB": "Kingston NV2 SNV2S 1TB SSD isolated white background",
    "WD-RED-4TB": "WD Red Plus 4TB NAS hard drive product photo",
    "KIN-FURY-16GB": "Kingston Fury Beast DDR5 16GB memory module product photo",
    "LOG-MK540": "Logitech MK540 Advanced wireless keyboard mouse combo product photo",
    "LOG-ZONE-VIBE": "Logitech Zone Vibe 100 headset graphite white background",
    "LOG-C920E": "Logitech C920e webcam isolated white background",
    "APC-BX1500M": "APC Back-UPS BX1500M 1500VA product photo",
    "UBI-UDR": "UniFi Dream Router UDR product photo cylinder white",
    "TPL-EAP245": "TP-Link EAP245 single access point white background",
    "CIS-CBS110-24T": "Cisco CBS110-24T 24 port gigabit switch product photo",
    "FOR-FG40F": "Fortinet FortiGate 40F FG-40F appliance photo isolated",
    "SAM-TABA9PLUS": "Samsung Galaxy Tab A9+ SM-X210 graphite tablet white background",
    "EPS-PLE20": "Epson PowerLite E20 projector product photo",
}

# Dominios que para una referencia concreta devolvieron laminas tecnicas
# (medidas, callouts) en vez de una fotografia limpia del equipo.
EVITAR = {
    "UBI-UDR": ["ui.com"],
    "FOR-FG40F": ["fortinet.com"],
}

# URL directa ya localizada en la pagina oficial del fabricante.
DIRECTAS = {
    "APC-BX1500M": "https://download.schneider-electric.com/files?p_Doc_Ref=SPD_NCAO-AGKHKJ_FL_V&p_File_Type=rendition_369_jpg&default_image=DefaultProductImage.png",
}


def bajar(url, destino, tiempo=45):
    """Descarga con curl. Devuelve True si el archivo quedo con contenido."""
    orden = ["curl", "-sL", "-A", AGENTE, "-m", str(tiempo),
             "--retry", "1", "-o", str(destino), url]
    try:
        subprocess.run(orden, check=False, capture_output=True, timeout=tiempo + 20)
    except subprocess.TimeoutExpired:
        return False
    return destino.exists() and destino.stat().st_size > 4000


def candidatas(consulta):
    """URLs de imagen que devuelve el buscador, en orden de aparicion."""
    salida = Path("busqueda.html")
    url = ("https://www.bing.com/images/search?q="
           + consulta.replace(" ", "+") + "&form=HDRSC2&first=1")
    if not bajar(url, salida):
        return []
    texto = salida.read_text(encoding="utf-8", errors="ignore")
    crudas = re.findall(r"murl&quot;:&quot;(.*?)&quot;", texto)
    vistas, limpias = set(), []
    for cruda in crudas:
        u = html.unescape(cruda)
        if u not in vistas:
            vistas.add(u)
            limpias.append(u)
    return limpias


def confiable(url):
    return any(dominio in url.lower() for dominio in CONFIABLES)


def util(ruta):
    """Comprueba que sea una imagen razonable para un catalogo."""
    try:
        from PIL import Image
        with Image.open(ruta) as im:
            im.verify()
        with Image.open(ruta) as im:
            ancho, alto = im.size
    except Exception:
        return None
    if min(ancho, alto) < 350:
        return None
    proporcion = max(ancho, alto) / min(ancho, alto)
    if proporcion > 2.6:          # pancartas y banners, no fotos de producto
        return None
    return (ancho, alto)


def resolver(sku, destino_dir):
    destino = destino_dir / (sku.lower().replace("-", "_") + ".raw")

    # 1) URL oficial, si ya se localizo a mano.
    if sku in DIRECTAS and bajar(DIRECTAS[sku], destino):
        medida = util(destino)
        if medida:
            return DIRECTAS[sku], medida

    # 2) Buscador: primero los dominios de confianza, luego el resto.
    urls = candidatas(CONSULTAS[sku])
    vetados = EVITAR.get(sku, [])
    urls = [u for u in urls if not any(v in u.lower() for v in vetados)]
    orden = [u for u in urls if confiable(u)] + [u for u in urls if not confiable(u)]
    for url in orden[:14]:
        if bajar(url, destino, tiempo=30):
            medida = util(destino)
            if medida:
                return url, medida
    if destino.exists():
        destino.unlink()
    return None, None


if __name__ == "__main__":
    destino_dir = Path(sys.argv[1] if len(sys.argv) > 1 else "fotos_reales")
    destino_dir.mkdir(parents=True, exist_ok=True)

    solo = sys.argv[2:] if len(sys.argv) > 2 else None
    registro = {}
    for p in CATALOGO:
        sku = p["sku"]
        if solo and sku not in solo:
            continue
        url, medida = resolver(sku, destino_dir)
        if url:
            print(f"OK   {sku:<16} {medida[0]}x{medida[1]}  {url[:88]}")
            registro[sku] = url
        else:
            print(f"FALLA {sku}")
        time.sleep(1)

    Path(destino_dir / "origenes.txt").write_text(
        "\n".join(f"{k}\t{v}" for k, v in registro.items()), encoding="utf-8")
    print(f"\n{len(registro)} de {len(solo) if solo else len(CATALOGO)} descargadas")
