package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "puntos.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_PUNTOS = "puntos";
    public static final String TABLE_LIBRETA = "libreta_campo";

    // Columnas Puntos Rápidos
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
    public static final String COLUMN_NOTAS = "notas";

    // Columnas Libreta de Campo
    public static final String COL_LIB_ID = "id";
    public static final String COL_LIB_ESTACION = "estacion_id";
    public static final String COL_LIB_ALT_INS = "alt_instrumento";
    public static final String COL_LIB_PUNTO_REF = "punto_ref";
    public static final String COL_LIB_ALT_PRI = "alt_prisma";
    public static final String COL_LIB_PUNTO_AUX = "punto_aux";
    public static final String COL_LIB_TIPO_REG = "tipo_registro";
    public static final String COL_LIB_ESTE = "este";
    public static final String COL_LIB_NORTE = "norte";
    public static final String COL_LIB_COTA = "cota";
    public static final String COL_LIB_OBS = "observaciones";
    public static final String COL_LIB_FECHA = "fecha";

    // 💡 Modificación: Se eliminó DEFAULT CURRENT_TIMESTAMP para forzar que siempre use la hora local enviada desde la App
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
                    COLUMN_FECHA + " TEXT, " +
                    COLUMN_NOTAS + " TEXT" +
                    ");";

    private static final String TABLE_LIBRETA_CREATE =
            "CREATE TABLE " + TABLE_LIBRETA + " (" +
                    COL_LIB_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_LIB_ESTACION + " TEXT, " +
                    COL_LIB_ALT_INS + " TEXT, " +
                    COL_LIB_PUNTO_REF + " TEXT, " +
                    COL_LIB_ALT_PRI + " TEXT, " +
                    COL_LIB_PUNTO_AUX + " TEXT, " +
                    COL_LIB_TIPO_REG + " TEXT, " +
                    COL_LIB_ESTE + " TEXT, " +
                    COL_LIB_NORTE + " TEXT, " +
                    COL_LIB_COTA + " TEXT, " +
                    COL_LIB_OBS + " TEXT, " +
                    COL_LIB_FECHA + " TEXT" +
                    ");";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        db.execSQL(TABLE_LIBRETA_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_NOTAS + " TEXT DEFAULT ''");
        }
        if (oldVersion < 4) {
            db.execSQL(TABLE_LIBRETA_CREATE);
        }
    }

    public long insertarPunto(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_PUNTOS, null, values);
    }

    public long insertarLibreta(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.insert(TABLE_LIBRETA, null, values);
    }

    public Cursor obtenerPuntos() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PUNTOS, null, null, null, null, null, COLUMN_ID + " DESC");
    }

    public void eliminarPunto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PUNTOS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public Cursor obtenerLibreta() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_LIBRETA, null, null, null, null, null, COL_LIB_ID + " DESC");
    }

    public void eliminarLibreta(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LIBRETA, COL_LIB_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void seedExampleData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PUNTOS, null);
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();

        if (count == 0) {
            String timeStampLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            // Ejemplo 1: La Paz
            insertPuntoHelper(db, "Punto_Test_LP_01", "-16º 30' 00.000''", "-68º 09' 00.000''", "3600.000", "3565.42", "495.2", "590745.230", "8175432.100", "19", "S", "0.99965432", "0.99943210", "0.99908642", timeStampLocal);

            // Ejemplo 2: Cochabamba
            insertPuntoHelper(db, "Punto_Test_CBBA_02", "-17º 23' 15.500''", "-66º 09' 30.200''", "2550.000", "2532.15", "562.8", "802145.450", "8076543.220", "19", "S", "0.99971234", "0.99965432", "0.99936666", timeStampLocal);

            // Ejemplo 3: Santa Cruz
            insertPuntoHelper(db, "Punto_Test_SCZ_03", "-17º 48' 02.100''", "-63º 10' 45.300''", "420.000", "435.80", "720.5", "481023.120", "8031456.780", "20", "S", "0.99960234", "0.99993412", "0.99953648", timeStampLocal);

            // Ejemplo 4: Potosí
            insertPuntoHelper(db, "Punto_Test_POT_04", "-19º 35' 12.400''", "-65º 45' 20.100''", "4060.000", "4020.15", "465.3", "211456.900", "7832145.600", "20", "S", "0.99984321", "0.99936123", "0.99920444", timeStampLocal);

            // 2 Entradas de Libreta
            ContentValues l1 = new ContentValues();
            l1.put(COL_LIB_ESTACION, "STATION_01");
            l1.put(COL_LIB_ALT_INS, "1.550");
            l1.put(COL_LIB_PUNTO_REF, "REF_01");
            l1.put(COL_LIB_ALT_PRI, "1.600");
            l1.put(COL_LIB_PUNTO_AUX, "AUX_01");
            l1.put(COL_LIB_TIPO_REG, "Radiación");
            l1.put(COL_LIB_ESTE, "590745.23");
            l1.put(COL_LIB_NORTE, "8175432.10");
            l1.put(COL_LIB_COTA, "3600.50");
            l1.put(COL_LIB_OBS, "Punto de control principal");
            l1.put(COL_LIB_FECHA, timeStampLocal);
            db.insert(TABLE_LIBRETA, null, l1);

            ContentValues l2 = new ContentValues();
            l2.put(COL_LIB_ESTACION, "STATION_01");
            l2.put(COL_LIB_ALT_INS, "1.550");
            l2.put(COL_LIB_PUNTO_REF, "REF_01");
            l2.put(COL_LIB_ALT_PRI, "0.000");
            l2.put(COL_LIB_PUNTO_AUX, "AUX_02");
            l2.put(COL_LIB_TIPO_REG, "Nivelación");
            l2.put(COL_LIB_ESTE, "590750.45");
            l2.put(COL_LIB_NORTE, "8175440.30");
            l2.put(COL_LIB_COTA, "3601.20");
            l2.put(COL_LIB_OBS, "Esquina de acera");
            l2.put(COL_LIB_FECHA, timeStampLocal);
            db.insert(TABLE_LIBRETA, null, l2);
        }
    }

    private void insertPuntoHelper(SQLiteDatabase db, String nom, String lat, String lon, String alt, String altO, String pres, String este, String norte, String zona, String hem, String fe, String fa, String fc, String fecha) {
        ContentValues v = new ContentValues();
        v.put(COLUMN_NOMBRE, nom);
        v.put(COLUMN_LATITUD, lat);
        v.put(COLUMN_LONGITUD, lon);
        v.put(COLUMN_ALTURA, alt);
        v.put(COLUMN_ALTURA_ORTO, altO);
        v.put(COLUMN_PRESION, pres);
        v.put(COLUMN_ESTE, este);
        v.put(COLUMN_NORTE, norte);
        v.put(COLUMN_ZONA, zona);
        v.put(COLUMN_HEMISFERIO, hem);
        v.put(COLUMN_FACTOR_ESCALA, fe);
        v.put(COLUMN_FACTOR_ALTURA, fa);
        v.put(COLUMN_FACTOR_COMBINADO, fc);
        v.put(COLUMN_FECHA, fecha);
        db.insert(TABLE_PUNTOS, null, v);
    }
}