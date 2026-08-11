package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repositorio central para cálculos topográficos.
 * Maneja la concurrencia y la lógica de decisión de modelos geoidales en Java.
 */
public class TopographyRepository {

    private final Context context;
    private final ExecutorService executor = Executors.newFixedThreadPool(3);
    private static TopographyRepository instance;

    private long lastTempRequestTime = 0;
    private double currentAmbientTemp = 15.0;

    public static synchronized TopographyRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TopographyRepository(context.getApplicationContext());
        }
        return instance;
    }

    private TopographyRepository(Context context) {
        this.context = context;
    }

    public interface CalculationCallback {
        void onResult(TopoCalculoManager.TopoResult result, boolean usesMgb);
        void onError(Exception e);
    }

    /**
     * Realiza el cálculo topográfico completo de forma asíncrona.
     */
    public void calculateCompleteAsync(Location loc, CalculationCallback callback) {
        executor.execute(() -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                int geoidModelPref = prefs.getInt(MainActivity.KEY_GEOID_MODEL, 1);
                float pressureOffset = prefs.getFloat(MainActivity.KEY_PRESSURE_OFFSET, 0f);

                boolean insideBolivia = (loc.getLatitude() <= -9.0 && loc.getLatitude() >= -23.0 && 
                                        loc.getLongitude() >= -70.0 && loc.getLongitude() <= -56.0);

                double geoidN;
                boolean isMgb = false;

                if (insideBolivia && geoidModelPref == 1 && MGBEngine.getInstance().estaLista()) {
                    double nMgb = MGBEngine.getInstance().getGeoidUndulation(loc.getLatitude(), loc.getLongitude());
                    if (nMgb != 0.0) {
                        geoidN = nMgb;
                        isMgb = true;
                    } else {
                        geoidN = EGM96Engine.getEGM96Undulation(loc.getLatitude(), loc.getLongitude());
                    }
                } else {
                    geoidN = EGM96Engine.getEGM96Undulation(loc.getLatitude(), loc.getLongitude());
                }

                TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateAll(
                        loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), geoidN, (double) pressureOffset);

                callback.onResult(res, isMgb);

                // Actualizar temperatura de forma independiente
                fetchTemperatureIfNeeded(loc.getLatitude(), loc.getLongitude());

            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    private synchronized void fetchTemperatureIfNeeded(double lat, double lon) {
        if (System.currentTimeMillis() - lastTempRequestTime < 600000) return;
        lastTempRequestTime = System.currentTimeMillis();

        try {
            String urlStr = String.format(Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&current=temperature_2m",
                    lat, lon);

            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) res.append(line);
                    
                    JSONObject json = new JSONObject(res.toString());
                    currentAmbientTemp = json.getJSONObject("current").getDouble("temperature_2m");
                    Log.d("TopographyRepo", "Temperatura actualizada: " + currentAmbientTemp);
                }
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e("TopographyRepo", "Error fetchTemperature", e);
            lastTempRequestTime = 0; 
        }
    }

    public double getCurrentAmbientTemp() {
        return currentAmbientTemp;
    }
}
