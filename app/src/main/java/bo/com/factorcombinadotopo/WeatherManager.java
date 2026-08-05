package bo.com.factorcombinadotopo;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
        public int messageResId;
        public Object messageArg; // Valor dinámico (km/h, %, mm)
        public double wind120;
        public double gusts;
        public double kp;
        public double rain;
        public double temperature;
        public int rainProbability;
        public int forecastDescResId;

        public SafetyStatus(SafetyLevel level, int messageResId) {
            this.level = level;
            this.messageResId = messageResId;
        }
    }

    private static ApiService apiService;

    private static ApiService getApiService() {
        if (apiService == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(5, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
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

    /**
     * Ejecuta las peticiones de clima e índice Kp en paralelo para reducir latencia.
     */
    public static void checkFlightSafety(double lat, double lon, WeatherCallback callback) {
        AtomicReference<WeatherResponse> weatherRef = new AtomicReference<>(null);
        AtomicReference<Double> kpRef = new AtomicReference<>(null);
        AtomicBoolean weatherFailed = new AtomicBoolean(false);

        String currentParams = "wind_speed_10m,wind_speed_80m,wind_speed_120m,wind_direction_120m,wind_gusts_10m,precipitation,temperature_2m,weather_code";
        String hourlyParams = "precipitation_probability";

        // 1. Lanzar petición de Clima
        getApiService().getDroneWeather(lat, lon, currentParams, hourlyParams, "ecmwf_ifs025")
                .enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherRef.set(response.body());
                    checkCompletion(weatherRef, kpRef, callback);
                } else {
                    weatherFailed.set(true);
                    callback.onError("Error en clima: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                weatherFailed.set(true);
                callback.onError("Fallo de red clima: " + t.getMessage());
            }
        });

        // 2. Lanzar petición de Kp Index simultáneamente
        getApiService().getKpIndex("https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json")
                .enqueue(new Callback<List<List<String>>>() {
            @Override
            public void onResponse(Call<List<List<String>>> call, Response<List<List<String>>> response) {
                double kp = -1;
                if (response.isSuccessful() && response.body() != null && response.body().size() > 1) {
                    List<String> lastEntry = response.body().get(response.body().size() - 1);
                    try {
                        kp = Double.parseDouble(lastEntry.get(1));
                    } catch (Exception ignored) {}
                }
                kpRef.set(kp);
                if (!weatherFailed.get()) checkCompletion(weatherRef, kpRef, callback);
            }

            @Override
            public void onFailure(Call<List<List<String>>> call, Throwable t) {
                kpRef.set(-1.0); // No bloqueamos por Kp
                if (!weatherFailed.get()) checkCompletion(weatherRef, kpRef, callback);
            }
        });
    }

    private static void checkCompletion(AtomicReference<WeatherResponse> weatherRef, 
                                        AtomicReference<Double> kpRef, 
                                        WeatherCallback callback) {
        if (weatherRef.get() != null && kpRef.get() != null) {
            evaluateSafety(weatherRef.get(), kpRef.get(), callback);
        }
    }

    private static void evaluateSafety(WeatherResponse weather, double kp, WeatherCallback callback) {
        WeatherResponse.CurrentWeather cur = weather.current;
        SafetyLevel level = SafetyLevel.GREEN;
        int msgRes = R.string.safety_msg_optimal;
        Object arg = null;

        int rainProb = 0;
        if (weather.hourly != null && weather.hourly.precipitationProbability != null && !weather.hourly.precipitationProbability.isEmpty()) {
            rainProb = weather.hourly.precipitationProbability.get(0);
        }

        // Reglas de Negocio
        if (cur.windSpeed120m > 30) {
            level = SafetyLevel.RED;
            msgRes = R.string.safety_msg_wind_120;
            arg = cur.windSpeed120m;
        } else if (cur.windGusts10m > 40) {
            level = SafetyLevel.RED;
            msgRes = R.string.safety_msg_gusts;
            arg = cur.windGusts10m;
        } else if (rainProb > 60) {
            level = SafetyLevel.RED;
            msgRes = R.string.safety_msg_rain_prob_high;
            arg = rainProb;
        } else if (kp >= 5) {
            level = SafetyLevel.YELLOW;
            msgRes = R.string.safety_msg_kp_high;
            arg = kp;
        } else if (cur.precipitation > 0.5) {
            level = SafetyLevel.RED;
            msgRes = R.string.safety_msg_rain_detected;
            arg = cur.precipitation;
        } else if (cur.precipitation > 0.1 || rainProb > 30) {
            level = SafetyLevel.YELLOW;
            msgRes = R.string.safety_msg_rain_prob_mid;
            arg = rainProb;
        }

        SafetyStatus status = new SafetyStatus(level, msgRes);
        status.messageArg = arg;
        status.wind120 = cur.windSpeed120m;
        status.gusts = cur.windGusts10m;
        status.kp = kp;
        status.rain = cur.precipitation;
        status.temperature = cur.temperature2m;
        status.rainProbability = rainProb;
        status.forecastDescResId = getWeatherDescRes(cur.weatherCode);
        
        callback.onSuccess(status);
    }

    private static int getWeatherDescRes(int code) {
        switch (code) {
            case 0: return R.string.weather_desc_0;
            case 1: case 2: case 3: return R.string.weather_desc_1_3;
            case 45: case 48: return R.string.weather_desc_45_48;
            case 51: case 53: case 55: return R.string.weather_desc_51_55;
            case 61: case 63: case 65: return R.string.weather_desc_61_65;
            case 71: case 73: case 75: return R.string.weather_desc_71_75;
            case 80: case 81: case 82: return R.string.weather_desc_80_82;
            case 95: case 96: case 99: return R.string.weather_desc_95_99;
            default: return R.string.weather_desc_unknown;
        }
    }
}
