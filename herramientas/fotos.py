# -*- coding: utf-8 -*-
"""Genera las 20 fotografias de catalogo de NEXCODE Marketplace.

Cada imagen es una ilustracion vectorial del equipo sobre un fondo suave con la
paleta de la marca. Se guardan como JPEG de 900x900 en assets/productos para que
el sembrador las suba a Firebase Storage la primera vez que arranca la app.
"""
from PIL import Image, ImageDraw, ImageFont, ImageFilter
from pathlib import Path
from catalogo import CATALOGO

LADO = 900
FUENTE_NEGRA = r"C:\Windows\Fonts\seguibl.ttf"
FUENTE_SEMI = r"C:\Windows\Fonts\segoeuib.ttf"
FUENTE_NORMAL = r"C:\Windows\Fonts\segoeui.ttf"

AZUL_PROFUNDO = (16, 64, 128)
AZUL = (16, 128, 208)
VERDE = (64, 224, 160)
GRAFITO = (38, 46, 60)
GRAFITO_CLARO = (62, 72, 90)
PLATA = (176, 186, 198)
PLATA_CLARA = (214, 222, 232)
BLANCO = (250, 251, 253)
NEGRO_MATE = (28, 32, 40)
TEXTO = (14, 28, 51)
TEXTO_SUAVE = (132, 148, 168)


def fuente(ruta, tam):
    return ImageFont.truetype(ruta, tam)


def fondo():
    """Fondo con degradado vertical y un halo circular de marca."""
    img = Image.new("RGB", (LADO, LADO), BLANCO)
    d = ImageDraw.Draw(img)
    for y in range(LADO):
        t = y / LADO
        c = (int(252 - 12 * t), int(253 - 8 * t), int(255 - 4 * t))
        d.line([(0, y), (LADO, y)], fill=c)
    halo = Image.new("RGB", (LADO, LADO), (255, 255, 255))
    hd = ImageDraw.Draw(halo)
    hd.ellipse([120, 110, 780, 770], fill=(228, 240, 252))
    halo = halo.filter(ImageFilter.GaussianBlur(70))
    return Image.blend(img, halo, 0.55)


def sombra(img, caja, radio=40, desenfoque=26, opacidad=52):
    """Sombra suave debajo del equipo, para que no flote sobre el fondo."""
    capa = Image.new("L", (LADO, LADO), 0)
    ImageDraw.Draw(capa).rounded_rectangle(caja, radius=radio, fill=opacidad)
    capa = capa.filter(ImageFilter.GaussianBlur(desenfoque))
    img.paste(Image.new("RGB", (LADO, LADO), (120, 140, 170)), (0, 0), capa)


# --------------------------------------------------------------------------
#  Un dibujante por familia de producto
# --------------------------------------------------------------------------

def portatil(img, d):
    sombra(img, (170, 640, 740, 700))
    # Pantalla
    d.rounded_rectangle([200, 210, 700, 560], radius=14, fill=GRAFITO)
    d.rounded_rectangle([216, 226, 684, 534], radius=6, fill=(24, 30, 42))
    for i, c in enumerate([(30, 44, 66), (28, 40, 60)]):
        d.rectangle([216, 226 + i * 154, 684, 380 + i * 154], fill=c)
    d.rounded_rectangle([260, 270, 470, 292], radius=11, fill=AZUL)
    d.rounded_rectangle([260, 312, 600, 330], radius=9, fill=(58, 78, 108))
    d.rounded_rectangle([260, 350, 520, 368], radius=9, fill=(58, 78, 108))
    d.rounded_rectangle([260, 400, 400, 440], radius=12, fill=VERDE)
    d.ellipse([444, 232, 456, 244], fill=(70, 82, 100))
    # Base
    d.polygon([(160, 640), (740, 640), (700, 566), (200, 566)], fill=PLATA)
    d.polygon([(200, 566), (700, 566), (696, 574), (204, 574)], fill=PLATA_CLARA)
    d.rounded_rectangle([386, 618, 514, 630], radius=6, fill=(150, 162, 176))


def monitor(img, d):
    sombra(img, (250, 700, 650, 748))
    d.rounded_rectangle([140, 180, 760, 580], radius=16, fill=GRAFITO)
    d.rounded_rectangle([156, 196, 744, 546], radius=6, fill=(20, 26, 38))
    for i in range(4):
        d.rectangle([156, 196 + i * 88, 744, 240 + i * 88],
                    fill=(24 + i * 8, 44 + i * 22, 78 + i * 34))
    d.ellipse([330, 300, 570, 452], fill=(16, 96, 176))
    d.ellipse([378, 330, 522, 424], fill=(38, 168, 226))
    d.rounded_rectangle([420, 560, 480, 660], radius=8, fill=(96, 106, 120))
    d.rounded_rectangle([310, 660, 590, 700], radius=14, fill=PLATA)


def impresora(img, d):
    sombra(img, (190, 640, 720, 700))
    d.rounded_rectangle([210, 300, 700, 520], radius=22, fill=(238, 241, 246))
    d.rounded_rectangle([210, 500, 700, 650], radius=22, fill=GRAFITO_CLARO)
    d.rounded_rectangle([240, 330, 500, 380], radius=10, fill=(214, 220, 230))
    d.rounded_rectangle([540, 330, 660, 400], radius=10, fill=AZUL_PROFUNDO)
    d.rounded_rectangle([556, 346, 644, 384], radius=6, fill=(120, 200, 240))
    # Hoja saliendo
    d.polygon([(300, 300), (620, 300), (620, 190), (300, 190)], fill=BLANCO)
    d.polygon([(300, 190), (620, 190), (620, 196), (300, 196)], fill=PLATA_CLARA)
    for i in range(4):
        d.rounded_rectangle([330, 216 + i * 20, 590 - i * 44, 226 + i * 20],
                            radius=5, fill=(196, 206, 220))
    d.rounded_rectangle([250, 560, 660, 600], radius=10, fill=(46, 54, 68))
    for i in range(3):
        d.ellipse([620 + 0, 530, 640, 550], fill=VERDE if i == 0 else AZUL)


def ssd(img, d):
    sombra(img, (150, 560, 760, 610))
    d.rounded_rectangle([150, 380, 760, 520], radius=10, fill=(24, 92, 60))
    d.rounded_rectangle([160, 390, 750, 510], radius=8, fill=(30, 112, 74))
    for i in range(3):
        d.rounded_rectangle([200 + i * 170, 408, 330 + i * 170, 492], radius=6, fill=NEGRO_MATE)
        d.rounded_rectangle([212 + i * 170, 420, 318 + i * 170, 448], radius=3, fill=(58, 66, 80))
    for i in range(9):
        d.rounded_rectangle([160 + i * 14, 490, 170 + i * 14, 520], radius=2, fill=(226, 190, 90))
    d.rounded_rectangle([600, 340, 760, 380], radius=8, fill=PLATA_CLARA)


def disco(img, d):
    sombra(img, (200, 660, 700, 712))
    d.rounded_rectangle([200, 240, 700, 660], radius=14, fill=(198, 206, 216))
    d.rounded_rectangle([216, 256, 684, 644], radius=10, fill=(176, 186, 198))
    d.ellipse([300, 320, 600, 620], fill=(150, 160, 174))
    d.ellipse([420, 440, 480, 500], fill=(210, 218, 228))
    for x, y in [(240, 280), (652, 280), (240, 604), (652, 604)]:
        d.ellipse([x - 12, y - 12, x + 12, y + 12], fill=(140, 150, 164))
    d.rounded_rectangle([260, 290, 520, 316], radius=8, fill=(196, 40, 48))


def ram(img, d):
    sombra(img, (140, 570, 770, 620))
    d.rounded_rectangle([140, 350, 770, 520], radius=8, fill=(30, 36, 46))
    d.polygon([(140, 350), (770, 350), (740, 300), (170, 300)], fill=(196, 44, 52))
    d.polygon([(170, 300), (740, 300), (720, 268), (190, 268)], fill=(230, 74, 82))
    for i in range(7):
        d.rounded_rectangle([176 + i * 86, 368, 236 + i * 86, 500], radius=4, fill=(52, 60, 74))
    for i in range(26):
        d.rectangle([150 + i * 24, 512, 164 + i * 24, 528], fill=(220, 186, 92))
    d.rounded_rectangle([400, 388, 620, 412], radius=6, fill=(230, 74, 82))


def teclado(img, d):
    sombra(img, (110, 560, 700, 606))
    d.rounded_rectangle([110, 330, 700, 560], radius=16, fill=(52, 60, 74))
    for fila in range(4):
        for col in range(12):
            x = 132 + col * 46
            y = 352 + fila * 50
            d.rounded_rectangle([x, y, x + 38, y + 40], radius=6,
                                fill=(84, 94, 112) if fila else AZUL)
    d.rounded_rectangle([290, 552, 520, 570], radius=6, fill=(70, 80, 96))
    # Mouse
    d.ellipse([700, 380, 840, 600], fill=(58, 66, 82))
    d.ellipse([712, 392, 828, 520], fill=(78, 88, 106))
    d.rounded_rectangle([760, 400, 780, 456], radius=9, fill=VERDE)


def diadema(img, d):
    sombra(img, (240, 690, 660, 736))
    d.arc([230, 180, 670, 620], start=180, end=360, fill=(46, 54, 68), width=44)
    d.rounded_rectangle([206, 380, 306, 600], radius=44, fill=(38, 46, 60))
    d.rounded_rectangle([594, 380, 694, 600], radius=44, fill=(38, 46, 60))
    d.ellipse([222, 420, 290, 560], fill=(70, 80, 98))
    d.ellipse([610, 420, 678, 560], fill=(70, 80, 98))
    d.arc([254, 560, 500, 700], start=20, end=150, fill=(38, 46, 60), width=16)
    d.ellipse([470, 636, 512, 678], fill=VERDE)
    d.rounded_rectangle([300, 196, 600, 226], radius=15, fill=AZUL)


def camara(img, d):
    sombra(img, (240, 620, 660, 668))
    d.rounded_rectangle([240, 300, 660, 560], radius=110, fill=(40, 48, 62))
    d.ellipse([370, 330, 530, 490], fill=(20, 26, 36))
    d.ellipse([392, 352, 508, 468], fill=(30, 60, 110))
    d.ellipse([412, 372, 488, 448], fill=(16, 96, 176))
    d.ellipse([432, 392, 468, 428], fill=(10, 20, 34))
    d.ellipse([440, 398, 452, 410], fill=(190, 220, 250))
    d.ellipse([288, 400, 318, 430], fill=(70, 80, 98))
    d.ellipse([582, 400, 612, 430], fill=(70, 80, 98))
    d.ellipse([620, 336, 640, 356], fill=VERDE)
    d.polygon([(300, 560), (600, 560), (640, 620), (260, 620)], fill=(52, 60, 76))


def ups(img, d):
    sombra(img, (250, 700, 650, 748))
    d.rounded_rectangle([250, 180, 650, 700], radius=20, fill=(40, 46, 58))
    d.rounded_rectangle([266, 196, 634, 340], radius=12, fill=(28, 34, 44))
    d.rounded_rectangle([300, 226, 600, 312], radius=8, fill=(24, 90, 70))
    d.rounded_rectangle([326, 250, 470, 288], radius=6, fill=VERDE)
    d.rounded_rectangle([492, 250, 574, 288], radius=6, fill=(60, 160, 120))
    for fila in range(3):
        for col in range(3):
            x = 300 + col * 116
            y = 380 + fila * 100
            d.rounded_rectangle([x, y, x + 84, y + 74], radius=10, fill=(24, 30, 40))
            d.rounded_rectangle([x + 22, y + 16, x + 32, y + 44], radius=4, fill=(120, 132, 150))
            d.rounded_rectangle([x + 52, y + 16, x + 62, y + 44], radius=4, fill=(120, 132, 150))
    d.rounded_rectangle([300, 660, 420, 682], radius=8, fill=AZUL)


def router(img, d):
    sombra(img, (230, 620, 670, 668))
    d.rounded_rectangle([230, 250, 670, 620], radius=54, fill=(246, 248, 251))
    d.rounded_rectangle([246, 266, 654, 604], radius=44, fill=BLANCO)
    d.ellipse([368, 330, 532, 494], outline=AZUL, width=18)
    d.ellipse([408, 370, 492, 454], fill=AZUL)
    d.ellipse([432, 394, 468, 430], fill=BLANCO)
    d.rounded_rectangle([300, 540, 600, 566], radius=13, fill=(226, 233, 242))
    d.rounded_rectangle([300, 540, 380, 566], radius=13, fill=VERDE)


def ap(img, d):
    sombra(img, (240, 640, 660, 690))
    d.ellipse([200, 200, 700, 700], fill=(238, 241, 246))
    d.ellipse([220, 220, 680, 680], fill=BLANCO)
    d.ellipse([300, 300, 600, 600], outline=(224, 230, 240), width=10)
    d.ellipse([380, 380, 520, 520], fill=(240, 244, 250))
    for r, c in [(70, VERDE), (110, AZUL), (150, AZUL_PROFUNDO)]:
        d.arc([450 - r, 450 - r, 450 + r, 450 + r], start=210, end=330, fill=c, width=14)
    d.ellipse([436, 486, 464, 514], fill=AZUL_PROFUNDO)


def switch(img, d):
    sombra(img, (110, 590, 790, 640))
    d.rounded_rectangle([110, 340, 790, 590], radius=12, fill=(64, 72, 86))
    d.rounded_rectangle([120, 350, 780, 580], radius=8, fill=(84, 92, 108))
    for fila in range(2):
        for col in range(12):
            x = 176 + col * 50
            y = 392 + fila * 96
            d.rounded_rectangle([x, y, x + 38, y + 62], radius=4, fill=(30, 36, 46))
            d.rounded_rectangle([x + 8, y + 8, x + 30, y + 34], radius=2, fill=(58, 66, 80))
            d.ellipse([x + 14, y + 44, x + 24, y + 54],
                      fill=VERDE if (col + fila) % 3 else (226, 186, 90))
    d.rounded_rectangle([120, 350, 160, 580], radius=8, fill=(46, 54, 66))
    d.rounded_rectangle([740, 350, 780, 580], radius=8, fill=(46, 54, 66))


def firewall(img, d):
    sombra(img, (140, 600, 760, 650))
    d.rounded_rectangle([140, 330, 760, 600], radius=16, fill=(58, 66, 80))
    d.rounded_rectangle([152, 342, 748, 588], radius=10, fill=(74, 82, 98))
    d.rounded_rectangle([180, 372, 420, 412], radius=8, fill=(196, 44, 52))
    for i in range(5):
        x = 190 + i * 108
        d.rounded_rectangle([x, 460, x + 78, 546], radius=6, fill=(30, 36, 46))
        d.rounded_rectangle([x + 14, 476, x + 64, 512], radius=3, fill=(58, 66, 80))
        d.ellipse([x + 30, 522, x + 46, 538], fill=VERDE)
    for i in range(4):
        d.ellipse([640, 372 + i * 22, 654, 386 + i * 22],
                  fill=VERDE if i < 2 else (226, 186, 90))


def tablet(img, d):
    sombra(img, (250, 690, 650, 738))
    d.rounded_rectangle([250, 130, 650, 690], radius=34, fill=(46, 54, 68))
    d.rounded_rectangle([266, 156, 634, 664], radius=22, fill=(18, 24, 34))
    for i in range(5):
        d.rectangle([266, 156 + i * 102, 634, 258 + i * 102],
                    fill=(16 + i * 6, 60 + i * 26, 118 + i * 22))
    d.rounded_rectangle([306, 220, 594, 250], radius=15, fill=(255, 255, 255))
    for fila in range(3):
        for col in range(3):
            d.rounded_rectangle([310 + col * 100, 300 + fila * 100,
                                 380 + col * 100, 370 + fila * 100],
                                radius=16, fill=(255, 255, 255, 40) if False else (60, 130, 200))
    d.rounded_rectangle([390, 622, 510, 646], radius=12, fill=(70, 80, 98))


def proyector(img, d):
    sombra(img, (200, 620, 700, 668))
    d.polygon([(660, 300), (860, 180), (860, 560), (660, 440)], fill=(226, 240, 252))
    d.rounded_rectangle([200, 300, 680, 620], radius=26, fill=(240, 243, 248))
    d.rounded_rectangle([200, 300, 680, 340], radius=26, fill=(216, 222, 232))
    d.ellipse([560, 350, 700, 490], fill=(52, 60, 76))
    d.ellipse([578, 368, 682, 472], fill=(24, 30, 42))
    d.ellipse([598, 388, 662, 452], fill=(38, 120, 200))
    d.ellipse([614, 404, 638, 428], fill=(198, 226, 250))
    for i in range(3):
        d.rounded_rectangle([250, 380 + i * 46, 340 + i * 40, 400 + i * 46],
                            radius=10, fill=(198, 208, 222))
    d.ellipse([250, 560, 274, 584], fill=VERDE)
    d.rounded_rectangle([240, 620, 290, 646], radius=8, fill=(180, 190, 204))


DIBUJANTES = {
    "portatil": portatil, "monitor": monitor, "impresora": impresora, "ssd": ssd,
    "disco": disco, "ram": ram, "teclado": teclado, "diadema": diadema,
    "camara": camara, "ups": ups, "router": router, "ap": ap, "switch": switch,
    "firewall": firewall, "tablet": tablet, "proyector": proyector,
}


def etiquetas(img, p):
    """Marca, categoria y SKU rotulados sobre la ilustracion."""
    d = ImageDraw.Draw(img, "RGBA")
    f_marca = fuente(FUENTE_NEGRA, 42)
    f_chip = fuente(FUENTE_SEMI, 24)
    f_sku = fuente(FUENTE_SEMI, 22)

    d.text((56, 52), p["marca"].upper(), font=f_marca, fill=AZUL_PROFUNDO)

    texto = p["categoria"].upper()
    ancho = d.textlength(texto, font=f_chip)
    d.rounded_rectangle([844 - ancho - 40, 56, 844, 104], radius=24,
                        fill=(16, 128, 208, 30))
    d.text((844 - ancho - 20, 68), texto, font=f_chip, fill=AZUL)

    d.text((56, 806), p["sku"], font=f_sku, fill=TEXTO_SUAVE)
    if p["estado"] == "Usado":
        et = "REACONDICIONADO"
        a = d.textlength(et, font=f_chip)
        d.rounded_rectangle([844 - a - 40, 792, 844, 840], radius=24,
                            fill=(245, 165, 36, 40))
        d.text((844 - a - 20, 804), et, font=f_chip, fill=(190, 120, 20))
    else:
        d.rounded_rectangle([740, 792, 844, 840], radius=24, fill=(64, 224, 160, 46))
        d.text((762, 804), "NUEVO", font=f_chip, fill=(23, 169, 123))


def generar(destino):
    destino = Path(destino)
    destino.mkdir(parents=True, exist_ok=True)
    for p in CATALOGO:
        img = fondo()
        DIBUJANTES[p["forma"]](img, ImageDraw.Draw(img))
        etiquetas(img, p)
        img.save(destino / p["archivo"], "JPEG", quality=88, optimize=True)
        print("  ", p["archivo"])
    print(f"{len(CATALOGO)} fotografias en {destino}")


if __name__ == "__main__":
    import sys
    generar(sys.argv[1] if len(sys.argv) > 1 else "salida")
