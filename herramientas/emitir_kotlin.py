# -*- coding: utf-8 -*-
"""Emite el archivo Kotlin con el catalogo semilla, a partir de catalogo.py.

Se genera en lugar de escribirse a mano para que los precios de venta salgan
siempre del mismo calculo (+30 % sobre el costo del distribuidor).
"""
import sys
from pathlib import Path
from catalogo import CATALOGO

CABECERA = '''package com.nexcode.marketplace.datos

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
'''

PIE = '''    )

    /** Valor del inventario a precio de venta, usado en el panel del vendedor. */
    fun valorInventario(): Double = SEMILLAS.sumOf { it.precio * it.cantidad }
}
'''

PLANTILLA = '''        Semilla(
            referencia = "{sku}",
            nombre = "{nombre}",
            marca = "{marca}",
            categoria = "{categoria}",
            estado = EstadoProducto.{estado},
            cantidad = {cantidad},
            costo = {costo}.0,
            precio = {precio}.0,
            calificacion = {calificacion},
            descripcion = "{desc}",
            archivoFoto = "{archivo}"
        ),
'''

if __name__ == "__main__":
    destino = Path(sys.argv[1])
    piezas = [CABECERA]
    for p in CATALOGO:
        piezas.append(PLANTILLA.format(
            sku=p["sku"], nombre=p["nombre"], marca=p["marca"],
            categoria=p["categoria"], estado=p["estado"].upper(),
            cantidad=p["cantidad"], costo=p["costo"], precio=p["precio"],
            calificacion=p["calificacion"], desc=p["desc"], archivo=p["archivo"],
        ))
    piezas.append(PIE)
    destino.write_text("".join(piezas), encoding="utf-8")
    print("escrito:", destino, len(CATALOGO), "semillas")
