package com.nexcode.marketplace.datos.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;

/**
 * Base de datos SQL local de NEXCODE Marketplace.
 *
 * <p>La guia pide conectar una base de datos SQL con Firebase. Firestore es la
 * fuente de verdad en la nube; esta base SQLite es su espejo dentro del
 * telefono y cumple tres papeles:</p>
 *
 * <ul>
 *   <li><b>Catalogo sin conexion</b>: cada vez que Firestore envia productos se
 *       reescribe la tabla {@code productos}, de modo que la aplicacion abre y
 *       muestra el catalogo aunque no haya red.</li>
 *   <li><b>Carrito</b>: vive solo en el telefono hasta que el comprador
 *       finaliza la compra; entonces se convierte en un pedido en Firestore.</li>
 *   <li><b>Historial</b>: pedidos y ultimos mensajes quedan consultables sin
 *       depender del servidor.</li>
 * </ul>
 *
 * <p>Se usa {@link SQLiteOpenHelper} directamente, sin Room, para que el SQL
 * quede a la vista en el informe de la asignatura.</p>
 */
public class BaseDatosSQL extends SQLiteOpenHelper {

    public static final String NOMBRE = "nexcode_marketplace.db";
    public static final int VERSION = 1;

    // --- Tablas y columnas -------------------------------------------------

    public static final String T_USUARIOS = "usuarios";
    public static final String T_PRODUCTOS = "productos";
    public static final String T_CARRITO = "carrito";
    public static final String T_PEDIDOS = "pedidos";
    public static final String T_PEDIDO_LINEAS = "pedido_lineas";
    public static final String T_MENSAJES = "mensajes";

    private static final String CREAR_USUARIOS =
            "CREATE TABLE " + T_USUARIOS + " ("
                    + "uid TEXT PRIMARY KEY, "
                    + "nombre TEXT NOT NULL, "
                    + "correo TEXT NOT NULL, "
                    + "tipo_usuario TEXT NOT NULL, "
                    + "fecha_registro INTEGER NOT NULL)";

    private static final String CREAR_PRODUCTOS =
            "CREATE TABLE " + T_PRODUCTOS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "nombre TEXT NOT NULL, "
                    + "descripcion TEXT NOT NULL DEFAULT '', "
                    + "precio REAL NOT NULL DEFAULT 0, "
                    + "categoria TEXT NOT NULL DEFAULT '', "
                    + "estado TEXT NOT NULL DEFAULT 'nuevo', "
                    + "cantidad INTEGER NOT NULL DEFAULT 0, "
                    + "imagen TEXT NOT NULL DEFAULT '', "
                    + "marca TEXT NOT NULL DEFAULT '', "
                    + "referencia TEXT NOT NULL DEFAULT '', "
                    + "vendedor_id TEXT NOT NULL DEFAULT '', "
                    + "vendedor_nombre TEXT NOT NULL DEFAULT '', "
                    + "calificacion REAL NOT NULL DEFAULT 0, "
                    + "votos INTEGER NOT NULL DEFAULT 0, "
                    + "vendidos INTEGER NOT NULL DEFAULT 0, "
                    + "fecha_registro INTEGER NOT NULL DEFAULT 0)";

    /**
     * El carrito se guarda por usuario: si dos personas usan el mismo telefono,
     * cada una conserva el suyo. La clave compuesta impide lineas repetidas.
     */
    private static final String CREAR_CARRITO =
            "CREATE TABLE " + T_CARRITO + " ("
                    + "usuario_id TEXT NOT NULL, "
                    + "producto_id TEXT NOT NULL, "
                    + "nombre TEXT NOT NULL, "
                    + "imagen TEXT NOT NULL DEFAULT '', "
                    + "precio REAL NOT NULL DEFAULT 0, "
                    + "cantidad INTEGER NOT NULL DEFAULT 1, "
                    + "existencias INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (usuario_id, producto_id))";

    private static final String CREAR_PEDIDOS =
            "CREATE TABLE " + T_PEDIDOS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "comprador_id TEXT NOT NULL, "
                    + "comprador_nombre TEXT NOT NULL DEFAULT '', "
                    + "total REAL NOT NULL DEFAULT 0, "
                    + "fecha INTEGER NOT NULL DEFAULT 0, "
                    + "estado TEXT NOT NULL DEFAULT 'Registrado')";

    private static final String CREAR_PEDIDO_LINEAS =
            "CREATE TABLE " + T_PEDIDO_LINEAS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "pedido_id TEXT NOT NULL, "
                    + "producto_id TEXT NOT NULL, "
                    + "nombre TEXT NOT NULL, "
                    + "precio REAL NOT NULL DEFAULT 0, "
                    + "cantidad INTEGER NOT NULL DEFAULT 0, "
                    + "FOREIGN KEY (pedido_id) REFERENCES " + T_PEDIDOS + "(id) ON DELETE CASCADE)";

    private static final String CREAR_MENSAJES =
            "CREATE TABLE " + T_MENSAJES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "chat_id TEXT NOT NULL, "
                    + "autor_id TEXT NOT NULL, "
                    + "autor_nombre TEXT NOT NULL DEFAULT '', "
                    + "texto TEXT NOT NULL, "
                    + "fecha INTEGER NOT NULL DEFAULT 0, "
                    + "del_vendedor INTEGER NOT NULL DEFAULT 0)";

    // Indices: el catalogo se filtra por categoria y el chat se lee por conversacion.
    private static final String INDICE_CATEGORIA =
            "CREATE INDEX idx_productos_categoria ON " + T_PRODUCTOS + " (categoria)";
    private static final String INDICE_CHAT =
            "CREATE INDEX idx_mensajes_chat ON " + T_MENSAJES + " (chat_id, fecha)";
    private static final String INDICE_PEDIDO =
            "CREATE INDEX idx_lineas_pedido ON " + T_PEDIDO_LINEAS + " (pedido_id)";

    public BaseDatosSQL(Context contexto) {
        super(contexto.getApplicationContext(), NOMBRE, null, VERSION);
    }

    @Override
    public void onCreate(@NonNull SQLiteDatabase base) {
        base.execSQL(CREAR_USUARIOS);
        base.execSQL(CREAR_PRODUCTOS);
        base.execSQL(CREAR_CARRITO);
        base.execSQL(CREAR_PEDIDOS);
        base.execSQL(CREAR_PEDIDO_LINEAS);
        base.execSQL(CREAR_MENSAJES);
        base.execSQL(INDICE_CATEGORIA);
        base.execSQL(INDICE_CHAT);
        base.execSQL(INDICE_PEDIDO);
    }

    @Override
    public void onUpgrade(@NonNull SQLiteDatabase base, int anterior, int nueva) {
        // El contenido local es una copia de Firestore: se puede reconstruir.
        // Por eso una migracion destructiva es segura en esta base.
        base.execSQL("DROP TABLE IF EXISTS " + T_MENSAJES);
        base.execSQL("DROP TABLE IF EXISTS " + T_PEDIDO_LINEAS);
        base.execSQL("DROP TABLE IF EXISTS " + T_PEDIDOS);
        base.execSQL("DROP TABLE IF EXISTS " + T_CARRITO);
        base.execSQL("DROP TABLE IF EXISTS " + T_PRODUCTOS);
        base.execSQL("DROP TABLE IF EXISTS " + T_USUARIOS);
        onCreate(base);
    }

    @Override
    public void onConfigure(@NonNull SQLiteDatabase base) {
        super.onConfigure(base);
        base.setForeignKeyConstraintsEnabled(true);
    }
}
