package com.nexcode.marketplace.datos

import com.nexcode.marketplace.dominio.Categorias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.roundToLong

/** El catálogo de partida respeta las reglas de precio e inventario del negocio. */
class CatalogoMayoristaTest {

    private val semillas = CatalogoMayorista.SEMILLAS

    @Test
    fun hayVeinteReferencias() {
        assertEquals(20, semillas.size)
    }

    @Test
    fun referenciasUnicas() {
        assertEquals(semillas.size, semillas.map { it.referencia }.toSet().size)
    }

    @Test
    fun precio_esCostoMasMargenRedondeadoALaCentena() {
        for (s in semillas) {
            val esperado = (s.costo * (1 + MARGEN) / 100).roundToLong() * 100.0
            assertEquals("Precio de ${s.referencia}", esperado, s.precio, 0.0)
        }
    }

    @Test
    fun categoriasPertenecenALaLista() {
        for (s in semillas) {
            assertTrue("Categoría de ${s.referencia}: ${s.categoria}", s.categoria in Categorias.LISTA)
        }
    }

    @Test
    fun existenciasYCalificacionEnRango() {
        for (s in semillas) {
            assertTrue("Existencias de ${s.referencia}", s.cantidad >= 0)
            assertTrue("Calificación de ${s.referencia}", s.calificacion in 0.0..5.0)
        }
    }

    @Test
    fun aProducto_conservaLosDatosYApuntaALaFotoLocal() {
        val s = semillas.first()
        val p = s.aProducto(vendedorId = "v1", vendedorNombre = "Tienda", momento = 1L)
        assertEquals(s.nombre, p.nombre)
        assertEquals(s.precio, p.precio, 0.0)
        assertEquals("v1", p.vendedorId)
        assertEquals("$RUTA_ASSETS/${s.archivoFoto}", p.imagen)
        assertEquals(0, p.vendidos)
    }
}
