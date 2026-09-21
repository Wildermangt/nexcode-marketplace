# -*- coding: utf-8 -*-
"""Monta las fotografias descargadas en el formato del catalogo.

Cada foto real se recorta de sus margenes vacios y se centra sobre el fondo
suave de NEXCODE en un cuadrado de 900x900. No se rotula nada encima: la
tarjeta del catalogo recorta la imagen por el centro, asi que cualquier texto
en los bordes quedaria invisible, y la marca y la referencia ya las escribe la
propia interfaz.
"""
import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFilter

from catalogo import CATALOGO

LADO = 900
MARGEN = 74          # aire alrededor del producto

def fondo():
    """Mismo fondo de marca que usaba el catalogo ilustrado."""
    img = Image.new("RGB", (LADO, LADO), (250, 251, 253))
    d = ImageDraw.Draw(img)
    for y in range(LADO):
        t = y / LADO
        d.line([(0, y), (LADO, y)],
               fill=(int(252 - 12 * t), int(253 - 8 * t), int(255 - 4 * t)))
    halo = Image.new("RGB", (LADO, LADO), (255, 255, 255))
    ImageDraw.Draw(halo).ellipse([120, 100, 780, 760], fill=(228, 240, 252))
    return Image.blend(img, halo.filter(ImageFilter.GaussianBlur(70)), 0.55)


def sobre_blanco(ruta):
    """Abre la imagen y aplana cualquier transparencia sobre blanco."""
    im = Image.open(ruta)
    if im.mode in ("RGBA", "LA", "P"):
        im = im.convert("RGBA")
        lienzo = Image.new("RGBA", im.size, (255, 255, 255, 255))
        im = Image.alpha_composite(lienzo, im)
    return im.convert("RGB")


def recortar_margenes(im, tolerancia=14):
    """
    Quita el marco liso que casi todas las fotos de catalogo traen alrededor.
    Se compara contra el color de la esquina superior izquierda.
    """
    referencia = Image.new("RGB", im.size, im.getpixel((0, 0)))
    diferencia = ImageChops.difference(im, referencia).convert("L")
    caja = diferencia.point(lambda p: 255 if p > tolerancia else 0).getbbox()
    if not caja:
        return im
    ancho, alto = im.size
    # Si el recorte se come casi todo, la heuristica fallo: se deja como estaba.
    if (caja[2] - caja[0]) < ancho * 0.15 or (caja[3] - caja[1]) < alto * 0.15:
        return im
    return im.crop(caja)


def sombra(base, caja):
    capa = Image.new("L", base.size, 0)
    ImageDraw.Draw(capa).rounded_rectangle(caja, radius=30, fill=46)
    capa = capa.filter(ImageFilter.GaussianBlur(24))
    base.paste(Image.new("RGB", base.size, (120, 140, 170)), (0, 0), capa)


def montar(producto, origen_dir, destino_dir):
    crudo = origen_dir / (producto["sku"].lower().replace("-", "_") + ".raw")
    if not crudo.exists():
        return False

    foto = recortar_margenes(sobre_blanco(crudo))

    # El producto ocupa el cuadro completo, dejando solo el margen de aire.
    alto_util = LADO - MARGEN * 2
    ancho_util = LADO - MARGEN * 2
    factor = min(ancho_util / foto.width, alto_util / foto.height)
    if factor < 1:
        foto = foto.resize(
            (max(1, int(foto.width * factor)), max(1, int(foto.height * factor))),
            Image.LANCZOS)

    lienzo = fondo()
    x = (LADO - foto.width) // 2
    y = (LADO - foto.height) // 2
    sombra(lienzo, (x + 30, y + foto.height - 20, x + foto.width - 30, y + foto.height + 26))
    lienzo.paste(foto, (x, y))

    lienzo.save(destino_dir / producto["archivo"], "JPEG", quality=90, optimize=True)
    return True


def hoja_contacto(destino_dir, salida, columnas=4, celda=300):
    """Mosaico con las 20 fotos, para revisarlas de un vistazo."""
    filas = (len(CATALOGO) + columnas - 1) // columnas
    hoja = Image.new("RGB", (columnas * celda, filas * celda), (255, 255, 255))
    for i, p in enumerate(CATALOGO):
        ruta = destino_dir / p["archivo"]
        if not ruta.exists():
            continue
        with Image.open(ruta) as im:
            hoja.paste(im.resize((celda, celda), Image.LANCZOS),
                       ((i % columnas) * celda, (i // columnas) * celda))
    hoja.save(salida, "JPEG", quality=86)


if __name__ == "__main__":
    origen_dir = Path(sys.argv[1])
    destino_dir = Path(sys.argv[2])
    destino_dir.mkdir(parents=True, exist_ok=True)

    hechas = 0
    for p in CATALOGO:
        if montar(p, origen_dir, destino_dir):
            hechas += 1
            print("  ", p["archivo"])
    print(f"{hechas} fotografias montadas en {destino_dir}")

    if len(sys.argv) > 3:
        hoja_contacto(destino_dir, sys.argv[3])
        print("hoja de contacto:", sys.argv[3])
