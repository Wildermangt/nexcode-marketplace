package com.nexcode.marketplace.core

import org.junit.Assert.assertEquals
import org.junit.Test

/** Formatos de moneda y de número de pedido que se ven en las pantallas. */
class FormatoTest {

    @Test
    fun moneda_usaPuntoDeMilesYRedondea() {
        assertEquals("$ 5.005.000", Formato.moneda(5_005_000.0))
        assertEquals("$ 1.000", Formato.moneda(999.6))
    }

    @Test
    fun entero_usaPuntoDeMiles() {
        assertEquals("1.240", Formato.entero(1240))
    }

    @Test
    fun monedaCorta_abreviaMillonesYMiles() {
        assertEquals("$ 5,0 M", Formato.monedaCorta(5_005_000.0))
        assertEquals("$ 12,5 K", Formato.monedaCorta(12_500.0))
        assertEquals("$ 900", Formato.monedaCorta(900.0))
    }

    @Test
    fun numeroPedido_tomaLosUltimosSeisCaracteresEnMayuscula() {
        assertEquals("NX-4F8A2C", Formato.numeroPedido("abc-xyz4f8a2c"))
        assertEquals("NX-AB1", Formato.numeroPedido("ab1"))
    }

    @Test
    fun numeroPedido_sinIdentificador_devuelveCeros() {
        assertEquals("NX-000000", Formato.numeroPedido(null))
        assertEquals("NX-000000", Formato.numeroPedido(""))
    }
}
