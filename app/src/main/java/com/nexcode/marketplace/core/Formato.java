package com.nexcode.marketplace.core;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Utilidades de presentacion de datos: moneda, cantidades y fechas.
 *
 * <p>Pertenece a la capa transversal (core) porque tanto las pantallas de
 * compra como el panel del vendedor necesitan dar formato a los mismos
 * valores. Escrita en Java, como pide la guia de la asignatura.</p>
 */
public final class Formato {

    /** Configuracion regional de Colombia: separador de miles con punto. */
    private static final Locale COLOMBIA = new Locale("es", "CO");

    private static final DecimalFormat PESOS;
    private static final DecimalFormat DECIMAL_UNO;
    private static final SimpleDateFormat FECHA_LARGA =
            new SimpleDateFormat("d 'de' MMMM 'de' yyyy", COLOMBIA);
    private static final SimpleDateFormat FECHA_CORTA =
            new SimpleDateFormat("d MMM yyyy", COLOMBIA);
    private static final SimpleDateFormat HORA =
            new SimpleDateFormat("h:mm a", COLOMBIA);
    private static final SimpleDateFormat DIA_Y_HORA =
            new SimpleDateFormat("d MMM, h:mm a", COLOMBIA);

    static {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(COLOMBIA);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        PESOS = new DecimalFormat("#,##0", simbolos);
        DECIMAL_UNO = new DecimalFormat("0.0", simbolos);
    }

    private Formato() {
        // Clase de utilidades: no se instancia.
    }

    /** Devuelve el valor como moneda colombiana, por ejemplo {@code $ 5.005.000}. */
    public static String moneda(double valor) {
        return "$ " + PESOS.format(Math.round(valor));
    }

    /** Numero entero con separador de miles: {@code 1.240}. */
    public static String entero(long valor) {
        return PESOS.format(valor);
    }

    /** Un solo decimal, usado para el promedio de estrellas: {@code 4,6}. */
    public static String decimal(double valor) {
        return DECIMAL_UNO.format(valor);
    }

    /**
     * Abrevia cifras grandes de inventario para que quepan en las tarjetas
     * del panel: {@code 5,0 M} en lugar de {@code 5.005.000}.
     */
    public static String monedaCorta(double valor) {
        if (valor >= 1_000_000d) {
            return "$ " + DECIMAL_UNO.format(valor / 1_000_000d) + " M";
        }
        if (valor >= 1_000d) {
            return "$ " + DECIMAL_UNO.format(valor / 1_000d) + " K";
        }
        return moneda(valor);
    }

    /** Fecha larga: {@code 9 de septiembre de 2026}. */
    public static String fechaLarga(long milisegundos) {
        return FECHA_LARGA.format(new Date(milisegundos));
    }

    /** Fecha corta: {@code 9 sept 2026}. */
    public static String fechaCorta(long milisegundos) {
        return FECHA_CORTA.format(new Date(milisegundos));
    }

    /** Solo la hora: {@code 7:42 p. m.}. Se usa en las burbujas del chat. */
    public static String hora(long milisegundos) {
        return HORA.format(new Date(milisegundos));
    }

    /**
     * Marca de tiempo relativa para listas de conversaciones: la hora si el
     * mensaje es de hoy y la fecha con hora si es anterior.
     */
    public static String cuando(long milisegundos) {
        long ahora = System.currentTimeMillis();
        if (ahora - milisegundos < 24L * 60L * 60L * 1000L) {
            return hora(milisegundos);
        }
        return DIA_Y_HORA.format(new Date(milisegundos));
    }

    /**
     * Numero de pedido legible a partir del identificador de Firestore:
     * {@code NX-4F8A2C}.
     */
    public static String numeroPedido(String id) {
        if (id == null || id.isEmpty()) {
            return "NX-000000";
        }
        String limpio = id.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        String cola = limpio.length() > 6 ? limpio.substring(limpio.length() - 6) : limpio;
        return "NX-" + cola;
    }
}
