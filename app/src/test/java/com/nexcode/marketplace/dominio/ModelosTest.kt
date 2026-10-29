package com.nexcode.marketplace.dominio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reglas del modelo de dominio que usan el buscador, el carrito y el registro. */
class ModelosTest {

    private val portatil = Producto(
        nombre = "Portátil HP ProBook 450",
        marca = "HP",
        referencia = "HP-PB450G10-I7",
        categoria = "Computo",
        descripcion = "Intel Core i7 con 16 GB de RAM",
        cantidad = 3
    )

    @Test
    fun normalizar_quitaTildesYPasaAMinusculas() {
        assertEquals("portatil camara nino", "Portátil CÁMARA Niño".normalizar())
    }

    @Test
    fun buscador_conTextoVacio_muestraTodo() {
        assertTrue(portatil.coincideCon(""))
        assertTrue(portatil.coincideCon("   "))
    }

    @Test
    fun buscador_ignoraTildesYMayusculas() {
        assertTrue(portatil.coincideCon("PORTATIL"))
        assertTrue(portatil.coincideCon("portátil"))
    }

    @Test
    fun buscador_revisaMarcaReferenciaCategoriaYDescripcion() {
        assertTrue(portatil.coincideCon("hp"))
        assertTrue(portatil.coincideCon("pb450g10"))
        assertTrue(portatil.coincideCon("computo"))
        assertTrue(portatil.coincideCon("16 gb"))
    }

    @Test
    fun buscador_noEncuentraLoQueNoEsta() {
        assertFalse(portatil.coincideCon("impresora"))
    }

    @Test
    fun existencias_ceroSignificaAgotado() {
        assertTrue(portatil.hayExistencias)
        assertFalse(portatil.copy(cantidad = 0).hayExistencias)
    }

    @Test
    fun subtotales_multiplicanPrecioPorCantidad() {
        assertEquals(150_000.0, ItemCarrito(precio = 50_000.0, cantidad = 3).subtotal, 0.0)
        assertEquals(80_000.0, LineaPedido(precio = 40_000.0, cantidad = 2).subtotal, 0.0)
    }

    @Test
    fun pedido_sumaLasUnidadesDeTodasLasLineas() {
        val pedido = Pedido(lineas = listOf(LineaPedido(cantidad = 2), LineaPedido(cantidad = 5)))
        assertEquals(7, pedido.unidades)
    }

    @Test
    fun tipoUsuario_desconocidoOVacio_quedaComoComprador() {
        // Una ficha dañada nunca debe dar acceso al panel del vendedor.
        assertEquals(TipoUsuario.COMPRADOR, TipoUsuario.desde(null))
        assertEquals(TipoUsuario.COMPRADOR, TipoUsuario.desde("superusuario"))
        assertEquals(TipoUsuario.ADMINISTRADOR, TipoUsuario.desde("administrador"))
    }

    @Test
    fun estadoProducto_desconocido_quedaComoNuevo() {
        assertEquals(EstadoProducto.USADO, EstadoProducto.desde("usado"))
        assertEquals(EstadoProducto.NUEVO, EstadoProducto.desde("otro"))
    }

    @Test
    fun iniciales_usanLasDosPrimerasPalabrasOElCorreo() {
        assertEquals("JG", Usuario(nombre = "jeferson gonzález tenjo").iniciales)
        assertEquals("A", Usuario(nombre = "  ", correo = "ana@correo.com").iniciales)
    }

    @Test
    fun categorias_conTodas_empiezaPorElFiltroSinRestriccion() {
        val lista = Categorias.conTodas()
        assertEquals(Categorias.TODAS, lista.first())
        assertEquals(Categorias.LISTA.size + 1, lista.size)
    }
}
