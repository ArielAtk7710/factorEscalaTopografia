package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LicenseManager {

    public static class LicenseInfo {
        public String tipo; // DEMO, ESTANDAR, PROFESIONAL
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
    }

    public static LicenseInfo getActiveLicense(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Asegurar que las tablas de licencias estén inicializadas
        dbHelper.seedLicenseTables(db);

        Cursor cursor = db.query(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, null, null, null, null, DatabaseHelper.COL_ACT_ID + " DESC", "1");

        String tipo = "DEMO";
        String codigo = "Demo2026";
        String fechaAct = "2026-09-30 00:00:00";
        String fechaExp = "2026-10-30 23:59:59";

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

        // Calcular expiración y días restantes
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

    public static boolean activateCode(Context context, String code) {
        if (code == null || code.trim().isEmpty()) return false;
        String cleanCode = code.trim();

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // Buscar código en la tabla licencias (case-insensitive)
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

                // Calcular nueva fecha de expiración
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String nowStr = sdf.format(new Date());
                String expStr;

                if (fechaExpFija != null && !fechaExpFija.isEmpty()) {
                    expStr = fechaExpFija;
                } else {
                    long futureMillis = System.currentTimeMillis() + ((long) dias * 24 * 60 * 60 * 1000);
                    expStr = sdf.format(new Date(futureMillis));
                }

                // Actualizar tabla de licencia activa
                db.delete(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, null);

                ContentValues cv = new ContentValues();
                cv.put(DatabaseHelper.COL_ACT_TIPO, tipo);
                cv.put(DatabaseHelper.COL_ACT_CODIGO, codigoOriginal);
                cv.put(DatabaseHelper.COL_ACT_FECHA_ACTIVACION, nowStr);
                cv.put(DatabaseHelper.COL_ACT_FECHA_EXPIRACION, expStr);
                cv.put(DatabaseHelper.COL_ACT_ESTADO, "ACTIVA");

                long rowId = db.insert(DatabaseHelper.TABLE_LICENCIA_ACTIVA, null, cv);
                return rowId != -1;
            }
            cursor.close();
        }

        return false;
    }
}
