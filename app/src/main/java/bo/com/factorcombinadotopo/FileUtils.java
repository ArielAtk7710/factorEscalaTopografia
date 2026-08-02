package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
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
}
