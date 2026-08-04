package bo.com.factorcombinadotopo;

import java.util.List;

import bo.com.factorcombinadotopo.api.ApiService;
import bo.com.factorcombinadotopo.models.WeatherResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherManager {

    public enum SafetyLevel { GREEN, YELLOW, RED }

    public static class SafetyStatus {
        public SafetyLevel level;
        public String message;
        public double wind120;
        public double gusts;
        public double kp;
        public double rain;
        public double temperature;
        public int rainProbability;
        public String forecastDesc;

        public SafetyStatus(SafetyLevel level, String message) {
            this.level = level;
            this.message = message;
        }
    }

    private static ApiService apiService;

    private static ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.open-meteo.com/")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }

    public interface WeatherCallback {
        void onSuccess(SafetyStatus status);
        void onError(String error);
    }

    public static void checkFlightSafety(double lat, double lon, WeatherCallback callback) {
        String currentParams = "wind_speed_10m,wind_speed_80m,wind_speed_120m,wind_direction_120m,wind_gusts_10m,precipitation,temperature_2m,weather_code";
        String hourlyParams = "precipitation_probability";
        
        getApiService().getDroneWeather(lat, lon, currentParams, hourlyParams, "ecmwf_ifs025").enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fetchKpIndex(response.body(), callback);
                } else {
                    callback.onError("Error en clima: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                callback.onError("Fallo de red clima: " + t.getMessage());
            }
        });
    }

    private static void fetchKpIndex(WeatherResponse weather, WeatherCallback callback) {
        getApiService().getKpIndex("https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json").enqueue(new Callback<List<List<String>>>() {
            @Override
            public void onResponse(Call<List<List<String>>> call, Response<List<List<String>>> response) {
                double kp = 0;
                if (response.isSuccessful() && response.body() != null && response.body().size() > 1) {
                    // La última entrada suele ser la más reciente
                    List<String> lastEntry = response.body().get(response.body().size() - 1);
                    try {
                        kp = Double.parseDouble(lastEntry.get(1));
                    } catch (Exception ignored) {}
                }
                evaluateSafety(weather, kp, callback);
            }

            @Override
            public void onFailure(Call<List<List<String>>> call, Throwable t) {
                // Fallar el Kp no bloquea el reporte de viento, pero avisamos
                evaluateSafety(weather, -1, callback);
            }
        });
    }

    private static void evaluateSafety(WeatherResponse weather, double kp, WeatherCallback callback) {
        WeatherResponse.CurrentWeather cur = weather.current;
        SafetyLevel level = SafetyLevel.GREEN;
        String msg = "Condiciones óptimas para volar";

        int rainProb = 0;
        if (weather.hourly != null && weather.hourly.precipitationProbability != null && !weather.hourly.precipitationProbability.isEmpty()) {
            rainProb = weather.hourly.precipitationProbability.get(0);
        }

        // Reglas de Negocio
        if (cur.windSpeed120m > 30) {
            level = SafetyLevel.RED;
            msg = "Alerta Roja: Viento severo en altura de vuelo (" + cur.windSpeed120m + " km/h)";
        } else if (cur.windGusts10m > 40) {
            level = SafetyLevel.RED;
            msg = "Alerta Roja: Ráfagas peligrosas detectadas (" + cur.windGusts10m + " km/h)";
        } else if (rainProb > 60) {
            level = SafetyLevel.RED;
            msg = "Alerta Roja: Alta probabilidad de lluvia (" + rainProb + "%). No volar.";
        } else if (kp >= 5) {
            level = SafetyLevel.YELLOW;
            msg = "Alerta Amarilla: Posible inestabilidad en señal GPS por tormenta solar (Kp " + kp + ")";
        } else if (cur.precipitation > 0.5) {
            level = SafetyLevel.RED;
            msg = "Alerta Roja: Lluvia detectada (" + cur.precipitation + " mm). Riesgo de cortocircuito.";
        } else if (cur.precipitation > 0.1 || rainProb > 30) {
            level = SafetyLevel.YELLOW;
            msg = "Alerta Amarilla: Riesgo de lluvia (" + rainProb + "%). Vuelo bajo supervisión.";
        }

        SafetyStatus status = new SafetyStatus(level, msg);
        status.wind120 = cur.windSpeed120m;
        status.gusts = cur.windGusts10m;
        status.kp = kp;
        status.rain = cur.precipitation;
        status.temperature = cur.temperature2m;
        status.rainProbability = rainProb;
        status.forecastDesc = getWeatherDesc(cur.weatherCode);
        
        callback.onSuccess(status);
    }

    private static String getWeatherDesc(int code) {
        switch (code) {
            case 0: return "Cielo despejado";
            case 1: case 2: case 3: return "Nubosidad parcial";
            case 45: case 48: return "Niebla";
            case 51: case 53: case 55: return "Llovizna";
            case 61: case 63: case 65: return "Lluvia moderada";
            case 71: case 73: case 75: return "Nieve";
            case 80: case 81: case 82: return "Chubascos";
            case 95: case 96: case 99: return "Tormenta";
            default: return "Clima variable";
        }
    }
}
