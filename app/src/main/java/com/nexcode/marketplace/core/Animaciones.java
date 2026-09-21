package com.nexcode.marketplace.core;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.TextView;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.nexcode.marketplace.R;

/**
 * Colección de animaciones reutilizables de la capa de presentación.
 *
 * <p>Se agrupan aquí para que las pantallas no repitan código y para que todas
 * las transiciones de la aplicación compartan la misma duración y curva de
 * aceleración; eso es lo que hace que la interfaz se sienta uniforme.</p>
 */
public final class Animaciones {

    /** Duración estándar de una entrada. */
    public static final long DURACION = 420L;

    private Animaciones() {
    }

    /** Convierte medidas en dp a píxeles del dispositivo. */
    public static float px(View vista, float dp) {
        return dp * vista.getResources().getDisplayMetrics().density;
    }

    /**
     * Entrada suave: la vista sube unos dp mientras aparece.
     *
     * @param retraso milisegundos de espera antes de comenzar
     */
    public static void entrada(View vista, long retraso) {
        vista.setAlpha(0f);
        vista.setTranslationY(px(vista, 28f));
        vista.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(retraso)
                .setDuration(DURACION)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }

    /**
     * Entrada escalonada de varias vistas: cada una arranca 70 ms después de la
     * anterior. Es el efecto que da sensación de "cascada" al abrir una pantalla.
     */
    public static void entradaEscalonada(long retrasoInicial, View... vistas) {
        long retraso = retrasoInicial;
        for (View vista : vistas) {
            if (vista != null) {
                entrada(vista, retraso);
                retraso += 70L;
            }
        }
    }

    /** Aparición con escala, pensada para iconos y marcas de confirmación. */
    public static void aparecerConRebote(View vista, long retraso) {
        vista.setAlpha(0f);
        vista.setScaleX(0.4f);
        vista.setScaleY(0.4f);
        vista.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setStartDelay(retraso)
                .setDuration(520)
                .setInterpolator(new OvershootInterpolator(1.6f))
                .start();
    }

    /** Sacudida horizontal para señalar un campo con error. */
    public static void sacudir(View vista) {
        vista.startAnimation(AnimationUtils.loadAnimation(vista.getContext(), R.anim.sacudir));
    }

    /** Pequeño latido, usado al confirmar una acción sobre un elemento. */
    public static void latido(View vista) {
        vista.animate()
                .scaleX(1.08f).scaleY(1.08f)
                .setDuration(140)
                .withEndAction(() -> vista.animate()
                        .scaleX(1f).scaleY(1f)
                        .setDuration(220)
                        .setInterpolator(new OvershootInterpolator(2.5f))
                        .start())
                .start();
    }

    /**
     * Anima un valor monetario desde cero hasta su valor final.
     * Se usa en la tarjeta de saldo: el dinero "cuenta" hacia arriba.
     */
    public static void contarMoneda(TextView texto, double valorFinal, long duracion) {
        ValueAnimator animador = ValueAnimator.ofFloat(0f, (float) valorFinal);
        animador.setDuration(duracion);
        animador.setInterpolator(new DecelerateInterpolator(1.6f));
        animador.addUpdateListener(a -> texto.setText(
                Formato.moneda((Float) a.getAnimatedValue())));
        animador.start();
    }

    /** Cambia el contenido de un texto con un fundido corto. */
    public static void cambiarTexto(TextView texto, String nuevo) {
        texto.animate()
                .alpha(0f)
                .setDuration(120)
                .withEndAction(() -> {
                    texto.setText(nuevo);
                    texto.animate().alpha(1f).setDuration(180).start();
                })
                .start();
    }

    /** Muestra u oculta una vista con un fundido, sin saltos de diseño. */
    public static void mostrar(View vista, boolean visible) {
        if (visible && vista.getVisibility() == View.VISIBLE) {
            return;
        }
        if (!visible && vista.getVisibility() != View.VISIBLE) {
            return;
        }
        if (visible) {
            vista.setAlpha(0f);
            vista.setVisibility(View.VISIBLE);
            vista.animate().alpha(1f).setDuration(200).start();
        } else {
            vista.animate()
                    .alpha(0f)
                    .setDuration(160)
                    .withEndAction(() -> vista.setVisibility(View.GONE))
                    .start();
        }
    }

    /** Rota 180 grados un icono, por ejemplo al desplegar un panel. */
    public static void girar(View vista, boolean desplegado) {
        vista.animate()
                .rotation(desplegado ? 180f : 0f)
                .setDuration(260)
                .setInterpolator(new FastOutSlowInInterpolator())
                .start();
    }
}
