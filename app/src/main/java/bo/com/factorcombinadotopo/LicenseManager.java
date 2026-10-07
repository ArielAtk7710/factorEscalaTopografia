package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LicenseManager {

    private static final String PREFS_NAME = "prefs_license";
    private static final String KEY_LIC_TIPO = "lic_tipo";
    private static final String KEY_LIC_CODIGO = "lic_codigo";
    private static final String KEY_LIC_FECHA_ACT = "lic_fecha_act";
    private static final String KEY_LIC_FECHA_EXP = "lic_fecha_exp";

    private static final String[] LICENSE_BIN_PATHS = {
            "GMB/LicenceP.bin",
            "GMB/licenceP.bin",
            "gmb/LicenceP.bin",
            "gmb/licenceP.bin",
            "mgb/LicenceP.bin",
            "mgb/licenceP.bin",
            "LicenceP.bin",
            "licenceP.bin"
    };

    public static class LicenseInfo {
        public String tipo; // DEMO, PROFESIONAL
        public String codigo;
        public String fechaActivacion;
        public String fechaExpiracion;
        public boolean isExpired;
        public int diasRestantes;

        public LicenseInfo(String tipo, String codigo, String fechaActivacion, String fechaExpiracion, boolean isExpired, int diasRestantes) {
            this.tipo = (tipo != null) ? tipo.toUpperCase(Locale.ROOT) : "DEMO";
            this.codigo = codigo;
            this.fechaActivacion = fechaActivacion;
            this.fechaExpiracion = fechaExpiracion;
            this.isExpired = isExpired;
            this.diasRestantes = diasRestantes;
        }

        public boolean isProfessional() {
            return "PROFESIONAL".equalsIgnoreCase(tipo);
        }
    }

    private static void saveToPreferences(Context context, String tipo, String codigo, String fechaAct, String fechaExp) {
        if (context == null) return;
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LIC_TIPO, tipo)
                .putString(KEY_LIC_CODIGO, codigo)
                .putString(KEY_LIC_FECHA_ACT, fechaAct)
                .putString(KEY_LIC_FECHA_EXP, fechaExp)
                .apply();
    }

    private static void restoreLicenseToDbIfMissing(SQLiteDatabase db, String tipoPref, String codigoPref, String actPref, String expPref) {
        try {
            db.delete(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, null);
            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_ACT_TIPO, tipoPref);
            cv.put(DatabaseHelper.COL_ACT_CODIGO, codigoPref);
            cv.put(DatabaseHelper.COL_ACT_FECHA_ACTIVACION, actPref);
            cv.put(DatabaseHelper.COL_ACT_FECHA_EXPIRACION, expPref);
            cv.put(DatabaseHelper.COL_ACT_ESTADO, "ACTIVA");
            db.insert(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static LicenseInfo getActiveLicense(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Asegurar que las tablas de licencias estén inicializadas
        dbHelper.seedLicenseTables(db);

        Cursor cursor = db.query(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, null, null, null, null, DatabaseHelper.COL_ACT_ID + " DESC", "1");

        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date currentDate = new Date();
        String defaultNow = sdfNow.format(currentDate);
        String defaultExp = sdfNow.format(new Date(currentDate.getTime() + (365L * 24 * 60 * 60 * 1000)));

        String tipo = "DEMO";
        String codigo = "Demo2026";
        String fechaAct = defaultNow;
        String fechaExp = defaultExp;

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idxTipo = cursor.getColumnIndex(DatabaseHelper.COL_ACT_TIPO);
                int idxCod = cursor.getColumnIndex(DatabaseHelper.COL_ACT_CODIGO);
                int idxAct = cursor.getColumnIndex(DatabaseHelper.COL_ACT_FECHA_ACTIVACION);
                int idxExp = cursor.getColumnIndex(DatabaseHelper.COL_ACT_FECHA_EXPIRACION);

                if (idxTipo != -1 && cursor.getString(idxTipo) != null) tipo = cursor.getString(idxTipo);
                if (idxCod != -1 && cursor.getString(idxCod) != null) codigo = cursor.getString(idxCod);
                if (idxAct != -1 && cursor.getString(idxAct) != null) fechaAct = cursor.getString(idxAct);
                if (idxExp != -1 && cursor.getString(idxExp) != null) fechaExp = cursor.getString(idxExp);
            }
            cursor.close();
        }

        // Respaldo / Auto-Restauración vía SharedPreferences si SQLite volvió a DEMO pero hay PROFESIONAL guardado
        if (!"PROFESIONAL".equalsIgnoreCase(tipo) && context != null) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String prefTipo = prefs.getString(KEY_LIC_TIPO, null);
            if ("PROFESIONAL".equalsIgnoreCase(prefTipo)) {
                tipo = "PROFESIONAL";
                codigo = prefs.getString(KEY_LIC_CODIGO, codigo);
                fechaAct = prefs.getString(KEY_LIC_FECHA_ACT, fechaAct);
                fechaExp = prefs.getString(KEY_LIC_FECHA_EXP, "2099-12-31 23:59:59");
                restoreLicenseToDbIfMissing(db, tipo, codigo, fechaAct, fechaExp);
            }
        }

        boolean isProfessional = "PROFESIONAL".equalsIgnoreCase(tipo);
        if (isProfessional) {
            return new LicenseInfo("PROFESIONAL", codigo, fechaAct, "2099-12-31 23:59:59", false, -1);
        }

        // Calcular expiración y días restantes para DEMO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        boolean isExpired = false;
        int diasRestantes = 0;

        try {
            Date expDate = sdf.parse(fechaExp);
            Date now = new Date();
            if (expDate != null) {
                if (now.after(expDate)) {
                    isExpired = true;
                    diasRestantes = 0;
                } else {
                    long diffMillis = expDate.getTime() - now.getTime();
                    diasRestantes = (int) Math.max(0, diffMillis / (1000 * 60 * 60 * 24));
                }
            }
        } catch (Exception ignored) {}

        return new LicenseInfo(tipo, codigo, fechaAct, fechaExp, isExpired, diasRestantes);
    }

    public static boolean isLicenseExpired(Context context) {
        return getActiveLicense(context).isExpired;
    }

    public static boolean isValidProfessionalCode(Context context, String code) {
        if (code == null || code.trim().isEmpty()) return false;
        String target = code.trim();

        for (String assetPath : LICENSE_BIN_PATHS) {
            try (InputStream is = context.getAssets().open(assetPath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && line.equalsIgnoreCase(target)) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
                // Probar siguiente ruta
            }
        }
        return false;
    }

    public static boolean activateCode(Context context, String code) {
        if (code == null || code.trim().isEmpty()) return false;
        String cleanCode = code.trim();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 1. Verificar si el código pertenece al binario de Licencias Profesionales (LicenceP.bin)
        if (isValidProfessionalCode(context, cleanCode)) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String nowStr = sdf.format(new Date());

            db.delete(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, null);

            ContentValues cv = new ContentValues();
            cv.put(DatabaseHelper.COL_ACT_TIPO, "PROFESIONAL");
            cv.put(DatabaseHelper.COL_ACT_CODIGO, cleanCode);
            cv.put(DatabaseHelper.COL_ACT_FECHA_ACTIVACION, nowStr);
            cv.put(DatabaseHelper.COL_ACT_FECHA_EXPIRACION, "2099-12-31 23:59:59"); // Uso eterno
            cv.put(DatabaseHelper.COL_ACT_ESTADO, "ACTIVA");

            long rowId = db.insert(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, cv);
            if (rowId != -1) {
                saveToPreferences(context, "PROFESIONAL", cleanCode, nowStr, "2099-12-31 23:59:59");
                return true;
            }
            return false;
        }

        // 2. Buscar código en la tabla licencias (DEMO)
        Cursor cursor = db.query(DatabaseHelper.TABLE_LICENCIAS, null,
                "LOWER(" + DatabaseHelper.COL_LIC_CODIGO + ") = ?",
                new String[]{cleanCode.toLowerCase(Locale.ROOT)}, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int idxTipo = cursor.getColumnIndex(DatabaseHelper.COL_LIC_TIPO);
                int idxCod = cursor.getColumnIndex(DatabaseHelper.COL_LIC_CODIGO);
                int idxDias = cursor.getColumnIndex(DatabaseHelper.COL_LIC_DIAS);
                int idxExpFija = cursor.getColumnIndex(DatabaseHelper.COL_LIC_FECHA_EXP_FIJA);

                String tipo = (idxTipo != -1) ? cursor.getString(idxTipo) : "DEMO";
                String codigoOriginal = (idxCod != -1) ? cursor.getString(idxCod) : cleanCode;
                int dias = (idxDias != -1) ? cursor.getInt(idxDias) : 30;
                String fechaExpFija = (idxExpFija != -1) ? cursor.getString(idxExpFija) : null;
                cursor.close();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String nowStr = sdf.format(new Date());
                String expStr;

                if (fechaExpFija != null && !fechaExpFija.isEmpty()) {
                    expStr = fechaExpFija;
                } else {
                    long futureMillis = System.currentTimeMillis() + ((long) dias * 24 * 60 * 60 * 1000);
                    expStr = sdf.format(new Date(futureMillis));
                }

                db.delete(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, null);

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_ACT_TIPO, tipo);
                cv.put(DatabaseHelper.COL_ACT_CODIGO, codigoOriginal);
                cv.put(DatabaseHelper.COL_ACT_FECHA_ACTIVACION, nowStr);
                cv.put(DatabaseHelper.COL_ACT_FECHA_EXPIRACION, expStr);
                cv.put(DatabaseHelper.COL_ACT_ESTADO, "ACTIVA");

                long rowId = db.insert(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, cv);
                if (rowId != -1) {
                    saveToPreferences(context, tipo, codigoOriginal, nowStr, expStr);
                    return true;
                }
                return false;
            }
            cursor.close();
        }

        return false;
    }
}
