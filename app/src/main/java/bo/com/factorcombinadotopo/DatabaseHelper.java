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
    private static final int DATABASE_VERSION = 8;

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
    public static final String COLUMN_MODELO_GEOIDAL = "modelo_geoidal";
    public static final String COLUMN_MODELO_DEM = "modelo_dem";
    public static final String COLUMN_TIPO_REGISTRO = "tipo_registro";
    public static final String COLUMN_PRECISION = "precision";
    public static final String COLUMN_SATELITES = "satelites";
    public static final String COLUMN_TEMPERATURA = "temperatura";
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
                    COLUMN_MODELO_GEOIDAL + " TEXT, " +
                    COLUMN_MODELO_DEM + " TEXT, " +
                    COLUMN_TIPO_REGISTRO + " TEXT, " +
                    COLUMN_PRECISION + " TEXT, " +
                    COLUMN_SATELITES + " TEXT, " +
                    COLUMN_TEMPERATURA + " TEXT, " +
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
        if (oldVersion < 5) {
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_MODELO_GEOIDAL + " TEXT DEFAULT 'EGM96 (Global)'");
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_TIPO_REGISTRO + " TEXT DEFAULT 'Registro Automático'");
        }
        if (oldVersion < 7) {
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_PRECISION + " TEXT DEFAULT 'N/A'");
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_SATELITES + " TEXT DEFAULT 'N/A'");
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_TEMPERATURA + " TEXT DEFAULT 'N/A'");
        }
        if (oldVersion < 8) {
            db.execSQL("ALTER TABLE " + TABLE_PUNTOS + " ADD COLUMN " + COLUMN_MODELO_DEM + " TEXT DEFAULT 'GPS Dispositivo'");
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

            // Ejemplo 1: Punto de Referencia La Paz (Imagen)
            insertPuntoHelper(db, "GPS_LP_CONTROL", "-16º 29' 45.120''", "-68º 08' 22.450''", "3962.670", "3928.15", "458.2", "595393.080", "8177115.220", "19", "S", "0.99965432", "0.99943210", "0.99909005", "MGBol08", "GPS Dispositivo", "Registro Automático", "±2m", "12", "15.0°C", timeStampLocal);

            // Ejemplo 2: Estación CBBA
            insertPuntoHelper(db, "BASE_AIRPORT_CBBA", "-17º 26' 10.300''", "-66º 10' 15.200''", "2550.000", "2532.15", "562.8", "802145.450", "8076543.220", "19", "S", "0.99971234", "0.99965432", "0.99936666", "EGM96 (Global)", "GPS Dispositivo", "Registro Automático", "±5m", "8", "22.5°C", timeStampLocal);

            // Ejemplo 3: Punto Topográfico SCZ
            insertPuntoHelper(db, "PUNTO_MIRA_SCZ", "-17º 48' 02.100''", "-63º 10' 45.300''", "420.000", "435.80", "720.5", "481023.120", "8031456.780", "20", "S", "0.99960234", "0.99993412", "0.99953648", "EGM96 (Global)", "GPS Dispositivo", "Registro Automático", "±10m", "5", "28.0°C", timeStampLocal);

            // Ejemplo 4: Control Minero Potosí
            insertPuntoHelper(db, "MINA_CONTROL_POT", "-19º 35' 12.400''", "-65º 45' 20.100''", "4060.000", "4020.15", "465.3", "211456.900", "7832145.600", "20", "S", "0.99984321", "0.99936123", "0.99920444", "EGM96 (Global)", "GPS Dispositivo", "Registro Automático", "±3m", "10", "12.0°C", timeStampLocal);

            // 2 Entradas de Libreta de Apuntes
            ContentValues l1 = new ContentValues();
            l1.put(COL_LIB_ESTACION, "EST_01");
            l1.put(COL_LIB_ALT_INS, "1.545");
            l1.put(COL_LIB_PUNTO_REF, "GPS_LP_CONTROL");
            l1.put(COL_LIB_ALT_PRI, "1.600");
            l1.put(COL_LIB_PUNTO_AUX, "AUX_101");
            l1.put(COL_LIB_TIPO_REG, "Radiación");
            l1.put(COL_LIB_ESTE, "595400.12");
            l1.put(COL_LIB_NORTE, "8177120.45");
            l1.put(COL_LIB_COTA, "3962.80");
            l1.put(COL_LIB_OBS, "Borde de calzada norte");
            l1.put(COL_LIB_FECHA, timeStampLocal);
            db.insert(TABLE_LIBRETA, null, l1);

            ContentValues l2 = new ContentValues();
            l2.put(COL_LIB_ESTACION, "EST_01");
            l2.put(COL_LIB_ALT_INS, "1.545");
            l2.put(COL_LIB_PUNTO_REF, "GPS_LP_CONTROL");
            l2.put(COL_LIB_ALT_PRI, "0.000");
            l2.put(COL_LIB_PUNTO_AUX, "AUX_102");
            l2.put(COL_LIB_TIPO_REG, "Nivelación");
            l2.put(COL_LIB_ESTE, "595412.30");
            l2.put(COL_LIB_NORTE, "8177135.60");
            l2.put(COL_LIB_COTA, "3963.10");
            l2.put(COL_LIB_OBS, "Punto sobre roca fija");
            l2.put(COL_LIB_FECHA, timeStampLocal);
            db.insert(TABLE_LIBRETA, null, l2);
        }
    }

    private void insertPuntoHelper(SQLiteDatabase db, String nom, String lat, String lon, String alt, String altO, String pres, String este, String norte, String zona, String hem, String fe, String fa, String fc, String model, String dem, String tipoReg, String precision, String sats, String temp, String fecha) {
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
        v.put(COLUMN_MODELO_GEOIDAL, model);
        v.put(COLUMN_MODELO_DEM, dem);
        v.put(COLUMN_TIPO_REGISTRO, tipoReg);
        v.put(COLUMN_PRECISION, precision);
        v.put(COLUMN_SATELITES, sats);
        v.put(COLUMN_TEMPERATURA, temp);
        v.put(COLUMN_FECHA, fecha);
        db.insert(TABLE_PUNTOS, null, v);
    }
}