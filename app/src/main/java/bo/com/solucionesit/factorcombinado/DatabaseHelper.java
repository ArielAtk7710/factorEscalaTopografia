package bo.com.solucionesit.factorcombinado;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "puntos.db";
    private static final int DATABASE_VERSION = 1;
    public static final String TABLE_PUNTOS = "puntos";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_ALTURA = "altura";
    public static final String COLUMN_ESTE = "este";
    public static final String COLUMN_NORTE = "norte";
    public static final String COLUMN_ZONA = "zona";
    public static final String COLUMN_HEMISFERIO = "hemisferio";
    public static final String COLUMN_FACTOR_ESCALA = "factor_escala";
    public static final String COLUMN_FACTOR_ALTURA = "factor_altura";
    public static final String COLUMN_FACTOR_COMBINADO = "factor_combinado";
    public static final String COLUMN_FECHA = "fecha";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PUNTOS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_NOMBRE + " TEXT, " +
                    COLUMN_LATITUD + " TEXT, " +
                    COLUMN_LONGITUD + " TEXT, " +
                    COLUMN_ALTURA + " TEXT, " +
                    COLUMN_ESTE + " TEXT, " +
                    COLUMN_NORTE + " TEXT, " +
                    COLUMN_ZONA + " TEXT, " +
                    COLUMN_HEMISFERIO + " TEXT, " +
                    COLUMN_FACTOR_ESCALA + " TEXT, " +
                    COLUMN_FACTOR_ALTURA + " TEXT, " +
                    COLUMN_FACTOR_COMBINADO + " TEXT, " +
                    COLUMN_FECHA + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PUNTOS);
        onCreate(db);
    }

    public long insertarPunto(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_PUNTOS, null, values);
    }

    public Cursor obtenerPuntos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PUNTOS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    public void eliminarPunto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PUNTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }
}