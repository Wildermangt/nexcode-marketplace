package com.nexcode.marketplace.core;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Preparacion de las fotografias antes de subirlas a Firebase Storage.
 *
 * <p>Una foto de camara moderna pesa varios megabytes; subirla tal cual haria
 * lento el catalogo y consumiria la cuota gratuita del proyecto. Aqui se
 * reduce a un lado maximo de 1.280 px y se recomprime en JPEG, que es lo que
 * termina viajando por la red.</p>
 */
public final class Imagenes {

    /** Lado maximo, en pixeles, de la imagen que se sube. */
    private static final int LADO_MAXIMO = 1280;

    /** Calidad de la recompresion JPEG. */
    private static final int CALIDAD = 82;

    private Imagenes() {
    }

    /**
     * Lee la imagen apuntada por {@code origen}, la reduce y la devuelve lista
     * para subir.
     *
     * @return los bytes del JPEG resultante
     * @throws IOException si el contenido no se puede leer o decodificar
     */
    public static byte[] comprimir(ContentResolver resolver, Uri origen) throws IOException {
        Bitmap mapa = decodificarReducido(resolver, origen);
        if (mapa == null) {
            throw new IOException("No se pudo decodificar la imagen: " + origen);
        }
        mapa = enderezar(resolver, origen, mapa);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        mapa.compress(Bitmap.CompressFormat.JPEG, CALIDAD, salida);
        mapa.recycle();
        return salida.toByteArray();
    }

    /**
     * Decodifica la imagen pidiendole a Android que ya la cargue reducida.
     * Asi nunca se reserva memoria para la foto en tamano completo.
     */
    private static Bitmap decodificarReducido(ContentResolver resolver, Uri origen)
            throws IOException {
        BitmapFactory.Options medida = new BitmapFactory.Options();
        medida.inJustDecodeBounds = true;
        try (InputStream entrada = resolver.openInputStream(origen)) {
            BitmapFactory.decodeStream(entrada, null, medida);
        }

        int escala = 1;
        int lado = Math.max(medida.outWidth, medida.outHeight);
        while (lado / escala > LADO_MAXIMO * 2) {
            escala *= 2;
        }

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inSampleSize = escala;
        Bitmap mapa;
        try (InputStream entrada = resolver.openInputStream(origen)) {
            mapa = BitmapFactory.decodeStream(entrada, null, opciones);
        }
        if (mapa == null) {
            return null;
        }

        int mayor = Math.max(mapa.getWidth(), mapa.getHeight());
        if (mayor <= LADO_MAXIMO) {
            return mapa;
        }
        float factor = LADO_MAXIMO / (float) mayor;
        Bitmap ajustado = Bitmap.createScaledBitmap(
                mapa,
                Math.round(mapa.getWidth() * factor),
                Math.round(mapa.getHeight() * factor),
                true);
        if (ajustado != mapa) {
            mapa.recycle();
        }
        return ajustado;
    }

    /**
     * Aplica la rotacion que la camara dejo anotada en los metadatos EXIF.
     * Sin esto, las fotos tomadas en vertical se ven acostadas en el catalogo.
     */
    private static Bitmap enderezar(ContentResolver resolver, Uri origen, Bitmap mapa) {
        int orientacion = ExifInterface.ORIENTATION_NORMAL;
        try (InputStream entrada = resolver.openInputStream(origen)) {
            if (entrada != null) {
                orientacion = new ExifInterface(entrada).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            }
        } catch (IOException ignorada) {
            // Si no hay metadatos EXIF se deja la imagen como esta.
        }

        Matrix matriz = new Matrix();
        switch (orientacion) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                matriz.postRotate(90f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matriz.postRotate(180f);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matriz.postRotate(270f);
                break;
            default:
                return mapa;
        }
        Bitmap girado = Bitmap.createBitmap(
                mapa, 0, 0, mapa.getWidth(), mapa.getHeight(), matriz, true);
        if (girado != mapa) {
            mapa.recycle();
        }
        return girado;
    }
}
