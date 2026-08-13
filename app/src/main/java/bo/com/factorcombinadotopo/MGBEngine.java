package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Motor de alto rendimiento para el Modelo Geoidal Bolivia (MGBol08).
 * Implementación pura en Java para máxima compatibilidad.
 */
public class MGBEngine {

    private static final String TAG = "MGBEngine";
    private static final String DEFAULT_FILE = "mgb/mgb08.bin";
    
    private static final double LAT_NORTH = -9.0;
    private static final double LAT_SOUTH = -23.0;
    private static final double LON_WEST = -70.0;
    private static final double LON_EAST = -56.0;
    private static final double STEP = 1.0 / 60.0;
    private static final int GRID_SIZE = 841;

    private float[] dataN;
    private volatile boolean estaCargada = false;

    private static MGBEngine instance;

    public static synchronized MGBEngine getInstance() {
        if (instance == null) {
            instance = new MGBEngine();
        }
        return instance;
    }

    private MGBEngine() {}

    public boolean estaLista() {
        return estaCargada;
    }

    public synchronized void cargarGrillaSincrona(Context context) {
        if (estaCargada) return;

        try {
            if (cargarViaNIO(context)) {
                estaCargada = true;
                return;
            }
        } catch (Exception e) {
            Log.w(TAG, "Carga NIO fallida, activando fallback convencional: " + e.getMessage());
        }

        cargarViaStream(context);
    }

    private boolean cargarViaNIO(Context context) throws IOException {
        try (AssetFileDescriptor afd = context.getAssets().openFd(DEFAULT_FILE);
             FileInputStream fis = afd.createInputStream();
             FileChannel channel = fis.getChannel()) {
            
            MappedByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, afd.getStartOffset(), afd.getLength());
            buffer.order(ByteOrder.BIG_ENDIAN);
            
            int numRecords = buffer.getInt();
            if (numRecords != (GRID_SIZE * GRID_SIZE)) {
                Log.w(TAG, "Header de registros sospechoso: " + numRecords);
            }

            float[] localData = new float[numRecords];
            for (int i = 0; i < numRecords; i++) {
                buffer.position(4 + (i * 24) + 16); 
                localData[i] = (float) buffer.getDouble();
            }
            
            this.dataN = localData;
            Log.d(TAG, "MGBol08 Engine [NIO]: Cargado exitosamente (" + numRecords + " puntos)");
            return true;
        }
    }

    private void cargarViaStream(Context context) {
        try (InputStream is = context.getAssets().open(DEFAULT_FILE);
             DataInputStream dis = new DataInputStream(new BufferedInputStream(is))) {
            
            int numRecords = dis.readInt();
            float[] localData = new float[numRecords];
            
            for (int i = 0; i < numRecords; i++) {
                dis.readDouble(); // Lat
                dis.readDouble(); // Lon
                localData[i] = (float) dis.readDouble(); // N
            }
            
            this.dataN = localData;
            this.estaCargada = true;
            Log.d(TAG, "MGBol08 Engine [Stream]: Cargado exitosamente via Fallback (" + numRecords + " puntos)");
        } catch (Exception e) {
            Log.e(TAG, "Error fatal: No se pudo cargar la grilla por ningún método.", e);
            estaCargada = false;
            dataN = null;
        }
    }

    public double getGeoidUndulation(double lat, double lon) {
        float[] currentData = dataN;
        if (!estaCargada || currentData == null) return 0.0;

        if (lat > LAT_NORTH || lat < LAT_SOUTH || lon < LON_WEST || lon > LON_EAST) {
            return 0.0; 
        }

        double dRow = (LAT_NORTH - lat) / STEP;
        double dCol = (lon - LON_WEST) / STEP;

        int row = (int) Math.floor(dRow);
        int col = (int) Math.floor(dCol);

        if (row >= GRID_SIZE - 1) row = GRID_SIZE - 2;
        if (col >= GRID_SIZE - 1) col = GRID_SIZE - 2;

        float n00 = currentData[row * GRID_SIZE + col];
        float n01 = currentData[row * GRID_SIZE + (col + 1)];
        float n10 = currentData[(row + 1) * GRID_SIZE + col];
        float n11 = currentData[(row + 1) * GRID_SIZE + (col + 1)];

        double t = dRow - row;
        double u = dCol - col;

        return (1 - t) * (1 - u) * n00 +
               (1 - t) * u * n01 +
               t * (1 - u) * n10 +
               t * u * n11;
    }
}
