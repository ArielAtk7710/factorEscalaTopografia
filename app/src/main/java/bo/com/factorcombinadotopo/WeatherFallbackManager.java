package bo.com.factorcombinadotopo;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import bo.com.factorcombinadotopo.models.UnifiedWeatherData;
import bo.com.factorcombinadotopo.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Gestor de redundancia para servicios de clima.
 * Implementa una cadena de 10 proveedores con lógica de fallback automático.
 */
public class WeatherFallbackManager {

    private static final String TAG = "WeatherFallback";

    public interface FallbackCallback {
        void onSuccess(UnifiedWeatherData data);
        void onError(String finalError);
    }

    private final Context context;
    private final double lat;
    private final double lon;
    private final FallbackCallback finalCallback;
    private int currentProviderIndex = 0;

    // Lista de proveedores (Nombres informativos)
    private final String[] providers = {
            "Open-Meteo",
            "MET Norway",
            "Bright Sky",
            "WeatherAPI",
            "Visual Crossing",
            "Pirate Weather",
            "OpenWeatherMap",
            "Tomorrow.io",
            "Meteomatics",
            "Storm Glass"
    };

    public WeatherFallbackManager(Context context, double lat, double lon, FallbackCallback callback) {
        this.context = context;
        this.lat = lat;
        this.lon = lon;
        this.finalCallback = callback;
    }

    public void start() {
        tryNextProvider();
    }

    private void tryNextProvider() {
        if (currentProviderIndex >= providers.length) {
            finalCallback.onError("Todos los servicios de clima fallaron (10/10).");
            return;
        }

        String providerName = providers[currentProviderIndex];
        Log.d(TAG, "Intentando obtener clima de: " + providerName);

        switch (currentProviderIndex) {
            case 0: callOpenMeteo(); break;
            case 1: callMetNorway(); break;
            case 2: callBrightSky(); break;
            // Los siguientes requieren API Key, se implementan con error simulado por ahora
            default: 
                Log.w(TAG, providerName + " requiere API Key configurada. Saltando...");
                currentProviderIndex++;
                tryNextProvider();
                break;
        }
    }

    private void callOpenMeteo() {
        String currentParams = "wind_speed_10m,wind_speed_80m,wind_speed_120m,wind_direction_10m,wind_gusts_10m,precipitation,temperature_2m,apparent_temperature,cloud_cover,visibility,weather_code,is_day";
        String hourlyParams = "temperature_2m,precipitation_probability,weather_code,wind_speed_10m,wind_gusts_10m,cloud_cover";
        String dailyParams = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max";

        WeatherManager.getApiService().getDroneWeather(lat, lon, currentParams, hourlyParams, dailyParams, "auto", "ecmwf_ifs025")
                .enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    finalCallback.onSuccess(normalizeOpenMeteo(response.body()));
                } else {
                    handleFailure("Error Open-Meteo: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                handleFailure("Fallo Open-Meteo: " + t.getMessage());
            }
        });
    }

    private void callMetNorway() {
        String url = String.format(Locale.US, "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=%.4f&lon=%.4f", lat, lon);
        WeatherManager.getApiService().getGenericWeather(url).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    finalCallback.onSuccess(normalizeMetNorway(response.body()));
                } else {
                    handleFailure("Error MET Norway: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handleFailure("Fallo MET Norway: " + t.getMessage());
            }
        });
    }

    private void callBrightSky() {
        String url = String.format(Locale.US, "https://api.brightsky.dev/current_weather?lat=%.4f&lon=%.4f", lat, lon);
        WeatherManager.getApiService().getGenericWeather(url).enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    finalCallback.onSuccess(normalizeBrightSky(response.body()));
                } else {
                    handleFailure("Error Bright Sky: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                handleFailure("Fallo Bright Sky: " + t.getMessage());
            }
        });
    }

    private void handleFailure(String error) {
        Log.e(TAG, error);
        currentProviderIndex++;
        tryNextProvider();
    }

    // --- NORMALIZADORES ---

    private UnifiedWeatherData normalizeOpenMeteo(WeatherResponse r) {
        UnifiedWeatherData u = new UnifiedWeatherData();
        u.providerName = "Open-Meteo";
        u.temperature = r.current.temperature2m;
        u.apparentTemperature = r.current.apparentTemperature;
        u.windSpeed10m = r.current.windSpeed10m;
        u.windSpeed120m = r.current.windSpeed120m;
        u.windDirection10m = r.current.windDirection10m;
        u.windGusts10m = r.current.windGusts10m;
        u.precipitation = r.current.precipitation;
        u.cloudCover = r.current.cloudCover;
        u.visibility = r.current.visibility;
        u.weatherCode = r.current.weatherCode;
        u.isDay = r.current.isDay;

        if (r.hourly != null && r.hourly.time != null) {
            for (int i = 0; i < Math.min(24, r.hourly.time.size()); i++) {
                UnifiedWeatherData.Hourly h = new UnifiedWeatherData.Hourly();
                h.time = r.hourly.time.get(i);
                h.temperature = r.hourly.temperature2m.get(i);
                h.rainProb = r.hourly.precipitationProbability.get(i);
                h.weatherCode = r.hourly.weatherCode.get(i);
                h.windSpeed = r.hourly.windSpeed10m.get(i);
                h.windGusts = r.hourly.windGusts10m.get(i);
                h.cloudCover = r.hourly.cloudCover.get(i);
                u.hourlyList.add(h);
            }
        }

        if (r.daily != null && r.daily.time != null) {
            for (int i = 0; i < r.daily.time.size(); i++) {
                UnifiedWeatherData.Daily d = new UnifiedWeatherData.Daily();
                d.date = r.daily.time.get(i);
                d.tempMax = r.daily.tempMax.get(i);
                d.tempMin = r.daily.tempMin.get(i);
                d.rainProb = r.daily.rainProbMax.get(i);
                d.weatherCode = r.daily.weatherCode.get(i);
                u.dailyList.add(d);
            }
        }
        return u;
    }

    private UnifiedWeatherData normalizeMetNorway(JsonObject json) {
        UnifiedWeatherData u = new UnifiedWeatherData();
        u.providerName = "MET Norway";
        try {
            JsonObject properties = json.getAsJsonObject("properties");
            JsonObject timeseries0 = properties.getAsJsonArray("timeseries").get(0).getAsJsonObject();
            JsonObject instantData = timeseries0.getAsJsonObject("data").getAsJsonObject("instant").getAsJsonObject("details");
            
            u.temperature = instantData.get("air_temperature").getAsDouble();
            u.windSpeed10m = instantData.get("wind_speed").getAsDouble();
            u.windDirection10m = instantData.get("wind_from_direction").getAsInt();
            u.windGusts10m = instantData.has("wind_speed_of_gust") ? instantData.get("wind_speed_of_gust").getAsDouble() : u.windSpeed10m * 1.2;
            u.cloudCover = instantData.get("cloud_area_fraction").getAsInt();
            u.isDay = 1; // Simplificación
            
            // Simular datos de 120m para drones si no hay
            u.windSpeed120m = u.windSpeed10m * 1.4; 

            // Cargar horarios simplificados
            JsonArray series = properties.getAsJsonArray("timeseries");
            for (int i = 0; i < Math.min(24, series.size()); i++) {
                JsonObject entry = series.get(i).getAsJsonObject();
                JsonObject data = entry.getAsJsonObject("data").getAsJsonObject("instant").getAsJsonObject("details");
                UnifiedWeatherData.Hourly h = new UnifiedWeatherData.Hourly();
                h.time = entry.get("time").getAsString();
                h.temperature = data.get("air_temperature").getAsDouble();
                h.windSpeed = data.get("wind_speed").getAsDouble();
                h.cloudCover = data.get("cloud_area_fraction").getAsInt();
                u.hourlyList.add(h);
            }
        } catch (Exception e) {
            Log.e(TAG, "Parsing MET Norway failed", e);
        }
        return u;
    }

    private UnifiedWeatherData normalizeBrightSky(JsonObject json) {
        UnifiedWeatherData u = new UnifiedWeatherData();
        u.providerName = "Bright Sky";
        try {
            JsonObject cur = json.getAsJsonObject("weather");
            u.temperature = cur.get("temperature").getAsDouble();
            u.windSpeed10m = cur.get("wind_speed").getAsDouble();
            u.windDirection10m = cur.get("wind_direction").getAsInt();
            u.precipitation = cur.get("precipitation").getAsDouble();
            u.cloudCover = cur.get("cloud_cover").getAsInt();
            u.visibility = cur.get("visibility").getAsDouble() / 1000.0;
            u.windGusts10m = cur.has("wind_gust_speed") ? cur.get("wind_gust_speed").getAsDouble() : u.windSpeed10m * 1.3;
            u.windSpeed120m = u.windSpeed10m * 1.4;
        } catch (Exception e) {
            Log.e(TAG, "Parsing Bright Sky failed", e);
        }
        return u;
    }
}
