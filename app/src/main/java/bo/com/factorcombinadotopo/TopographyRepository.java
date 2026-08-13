package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;
import bo.com.factorcombinadotopo.models.ElevationResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
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

    /**
     * Permite ejecutar tareas en segundo plano usando el pool de hilos del repositorio.
     */
    public void runOnBackground(Runnable task) {
        executor.execute(task);
    }

    public interface CalculationCallback {
        void onResult(TopoCalculoManager.TopoResult result, boolean usesMgb);
        void onError(Exception e);
    }

    public interface ElevationCallback {
        void onResult(double elevation);
        void onError(String error);
    }

    /**
     * Obtiene la elevación ortométrica desde la API de Open-Meteo.
     */
    public void fetchElevationAsync(double lat, double lon, ElevationCallback callback) {
        // 🛡️ Validación de Red antes de peticionar
        if (!NetworkUtils.isNetworkAvailable(context)) {
            callback.onError("Sin conexión. No se puede obtener la altura online.");
            return;
        }

        WeatherManager.getApiService().getElevation(lat, lon).enqueue(new Callback<ElevationResponse>() {
            @Override
            public void onResponse(Call<ElevationResponse> call, Response<ElevationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onResult(response.body().getFirstElevation());
                } else {
                    callback.onError("Error API Elevation: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ElevationResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    /**
     * Realiza el cálculo topográfico partiendo de una altura ortométrica (H).
     * Reconstruye la altura elipsoidal (h = H + N) para el motor IGM.
     */
    public void calculateFromOrthometricAsync(double lat, double lon, double altOrtoApi, CalculationCallback callback) {
        executor.execute(() -> {
            try {
                // Obtener ondulación N forzando MGBol08 si es posible
                double geoidN = 0.0;
                boolean isMgb = false;
                
                if (MGBEngine.getInstance().estaLista()) {
                    double nMgb = MGBEngine.getInstance().getGeoidUndulation(lat, lon);
                    if (nMgb != 0.0) {
                        geoidN = nMgb;
                        isMgb = true;
                    }
                }
                
                if (!isMgb) {
                    geoidN = EGM96Engine.getEGM96Undulation(lat, lon);
                }

                // Reconstruir h (Elipsoidal) = H (Ortométrica) + N (Ondulación)
                double reconstructedHEl = altOrtoApi + geoidN;

                SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
                float pressureOffset = prefs.getFloat(MainActivity.KEY_PRESSURE_OFFSET, 0f);

                TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateAll(
                        lat, lon, reconstructedHEl, geoidN, (double) pressureOffset);

                callback.onResult(res, isMgb);

                fetchTemperatureIfNeeded(lat, lon);

            } catch (Exception e) {
                callback.onError(e);
            }
        });
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
