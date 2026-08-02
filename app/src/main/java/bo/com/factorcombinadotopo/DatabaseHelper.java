package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "puntos.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_PUNTOS = "puntos";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_ALTURA = "altura";
    public static final String COLUMN_ALTURA_ORTO = "altura_ortometrica";
    public static final String COLUMN_PRESION = "presion";
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
                    COLUMN_ALTURA_ORTO + " TEXT, " +
                    COLUMN_PRESION + " TEXT, " +
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

    public void seedExampleData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PUNTOS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            // Ejemplo 1: La Paz
            ContentValues v1 = new ContentValues();
            v1.put(COLUMN_NOMBRE, "Punto_Test_LP_01");
            v1.put(COLUMN_LATITUD, "-16º 30' 00.00''");
            v1.put(COLUMN_LONGITUD, "-68º 09' 00.00''");
            v1.put(COLUMN_ALTURA, "3600.00 m");
            v1.put(COLUMN_ESTE, "590745.00 m");
            v1.put(COLUMN_NORTE, "8175432.00 m");
            v1.put(COLUMN_ZONA, "19");
            v1.put(COLUMN_HEMISFERIO, "S");
            v1.put(COLUMN_FACTOR_ESCALA, "0.99965432");
            v1.put(COLUMN_FACTOR_ALTURA, "0.99943210");
            v1.put(COLUMN_FACTOR_COMBINADO, "0.99908642");
            db.insert(TABLE_PUNTOS, null, v1);

            // Ejemplo 2: Cochabamba
            ContentValues v2 = new ContentValues();
            v2.put(COLUMN_NOMBRE, "Punto_Test_CBBA_02");
            v2.put(COLUMN_LATITUD, "-17º 23' 15.50''");
            v2.put(COLUMN_LONGITUD, "-66º 09' 30.20''");
            v2.put(COLUMN_ALTURA, "2550.00 m");
            v2.put(COLUMN_ESTE, "802145.00 m");
            v2.put(COLUMN_NORTE, "8076543.00 m");
            v2.put(COLUMN_ZONA, "19");
            v2.put(COLUMN_HEMISFERIO, "S");
            v2.put(COLUMN_FACTOR_ESCALA, "0.99971234");
            v2.put(COLUMN_FACTOR_ALTURA, "0.99965432");
            v2.put(COLUMN_FACTOR_COMBINADO, "0.99936666");
            db.insert(TABLE_PUNTOS, null, v2);
        }
    }
}