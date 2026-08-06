package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Guarda un archivo de texto en la carpeta pública Documents/FactorEscalaTop
     * Compatible con Android 11+ (Scoped Storage) y versiones anteriores.
     */
    public static void savePublicTxtFile(Context context, String fileName, String content) {
        String relativePath = Environment.DIRECTORY_DOCUMENTS + "/FactorEscalaTop";
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);

                Uri externalUri = MediaStore.Files.getContentUri("external");
                Uri fileUri = context.getContentResolver().insert(externalUri, values);

                if (fileUri != null) {
                    try (OutputStream outputStream = context.getContentResolver().openOutputStream(fileUri)) {
                        if (outputStream != null) {
                            outputStream.write(content.getBytes());
                            showPathToast(context, fileName);
                        }
                    }
                }
            } else {
                // Para versiones muy antiguas (Legacy)
                java.io.File directory = new java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "FactorEscalaTop");
                if (!directory.exists()) directory.mkdirs();
                
                java.io.File file = new java.io.File(directory, fileName);
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                    fos.write(content.getBytes());
                    showPathToast(context, fileName);
                }
            }
        } catch (Exception e) {
            UIUtils.showErrorToast(context, "Error al guardar archivo: " + e.getMessage());
        }
    }

    private static void showPathToast(Context context, String fileName) {
        String amigablePath = "Almacenamiento Interno > Documents > FactorEscalaTop > " + fileName;
        UIUtils.showInfoToastLong(context, amigablePath);
    }

    /**
     * Copia un archivo desde una URI a un archivo de destino de forma segura.
     */
    public static boolean copyUriToFile(Context context, Uri uri, File dest) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[16384]; // Buffer más grande para mapas
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (dest.exists()) dest.delete(); // Borrar archivo parcial en caso de error
            return false;
        } finally {
            try {
                if (is != null) is.close();
                if (os != null) os.close();
            } catch (Exception ignored) {}
        }
    }

    /**
     * Obtiene el nombre de un archivo desde una URI.
     */
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    result = cursor.getString(index);
                }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    /**
     * Calcula el tamaño de un directorio de forma recursiva.
     */
    public static long getFolderSize(File folder) {
        long size = 0;
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else {
                        size += getFolderSize(file);
                    }
                }
            }
        }
        return size;
    }

    /**
     * Elimina todos los archivos de un directorio.
     */
    public static void clearDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        clearDirectory(file);
                    }
                    file.delete();
                }
            }
        }
    }

    /**
     * Formatea un tamaño en bytes a una cadena legible (MB, GB, etc).
     */
    public static String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new java.text.DecimalFormat("#,##0.##").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
