package bo.com.factorcombinadotopo;

import android.content.Context;
import java.util.ArrayList;
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

    public enum SafetyLevel { GREEN, YELLOW, ORANGE, RED }

    public static class HourlyStatus {
        public String time;
        public double wind;
        public double gusts;
        public int rainProb;
        public int cloudCover;
        public int weatherCode;
        public SafetyLevel level;
    }

    public static class DailyForecast {
        public String date;
        public int weatherCode;
        public double tempMax;
        public double tempMin;
        public int rainProb;
    }

    public static class SafetyStatus {
        public SafetyLevel level;
        public int messageResId;
        public Object messageArg; 
        public double wind120;
        public double windSustained;
        public int windDirection;
        public double gusts;
        public double kp;
        public double rain;
        public double temperature;
        public double apparentTemperature;
        public int isDay;
        public int cloudCover;
        public double visibility;
        public int rainProbability;
        public int forecastDescResId;
        
        // Professional Recommendations (Deprecated in favor of safetyAnalysis)
        public int detailedTitleResId;
        public int detailedDescResId;
        public int detailedRecResId;

        public String recommendation;
        public List<HourlyStatus> hourlyList = new ArrayList<>();
        public List<DailyForecast> dailyList = new ArrayList<>();
        
        // New advanced analysis result
        public FlightSafetyResult safetyAnalysis;

        public SafetyStatus(SafetyLevel level, int messageResId) {
            this.level = level;
            this.messageResId = messageResId;
        }
    }

    private static ApiService apiService;

    public static ApiService getApiService() {
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

    public static void checkFlightSafety(Context context, double lat, double lon, double pdop, WeatherCallback callback) {
        // 🛡️ Validación Senior de Conectividad Proactiva (Solo una vez)
        if (!NetworkUtils.isNetworkAvailable(context)) {
            if (NetworkUtils.shouldShowOfflineWarning(context)) {
                callback.onError(context.getString(R.string.msg_weather_offline));
            }
            return;
        }

        AtomicReference<WeatherResponse> weatherRef = new AtomicReference<>(null);
        AtomicReference<Double> kpRef = new AtomicReference<>(null);
        AtomicBoolean weatherFailed = new AtomicBoolean(false);

        // Parametros ampliados para Dron incluyendo Horario y Diario
        String currentParams = "wind_speed_10m,wind_speed_80m,wind_speed_120m,wind_direction_10m,wind_gusts_10m,precipitation,temperature_2m,apparent_temperature,cloud_cover,visibility,weather_code,is_day";
        String hourlyParams = "temperature_2m,precipitation_probability,weather_code,wind_speed_10m,wind_gusts_10m,cloud_cover";
        String dailyParams = "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max";

        getApiService().getDroneWeather(lat, lon, currentParams, hourlyParams, dailyParams, "auto", "ecmwf_ifs025")
                .enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    weatherRef.set(response.body());
                    checkCompletion(context, weatherRef, kpRef, pdop, callback);
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

        getApiService().getKpIndex("https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json")
                .enqueue(new Callback<List<bo.com.factorcombinadotopo.models.KpIndex>>() {
            @Override
            public void onResponse(Call<List<bo.com.factorcombinadotopo.models.KpIndex>> call, Response<List<bo.com.factorcombinadotopo.models.KpIndex>> response) {
                double kp = -1;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bo.com.factorcombinadotopo.models.KpIndex lastEntry = response.body().get(response.body().size() - 1);
                    kp = lastEntry.kp;
                }
                kpRef.set(kp);
                if (!weatherFailed.get()) checkCompletion(context, weatherRef, kpRef, pdop, callback);
            }

            @Override
            public void onFailure(Call<List<bo.com.factorcombinadotopo.models.KpIndex>> call, Throwable t) {
                kpRef.set(-1.0);
                if (!weatherFailed.get()) checkCompletion(context, weatherRef, kpRef, pdop, callback);
            }
        });
    }

    private static void checkCompletion(Context context,
                                        AtomicReference<WeatherResponse> weatherRef, 
                                        AtomicReference<Double> kpRef, 
                                        double pdop,
                                        WeatherCallback callback) {
        if (weatherRef.get() != null && kpRef.get() != null) {
            evaluateSafety(context, weatherRef.get(), kpRef.get(), pdop, callback);
        }
    }

    private static void evaluateSafety(Context context, WeatherResponse weather, double kp, double pdop, WeatherCallback callback) {
        WeatherResponse.CurrentWeather cur = weather.current;
        int rainProb = 0;
        if (weather.hourly != null && weather.hourly.precipitationProbability != null && !weather.hourly.precipitationProbability.isEmpty()) {
            rainProb = weather.hourly.precipitationProbability.get(0);
        }

        // Basic status for legacy UI
        SafetyStatus status = new SafetyStatus(SafetyLevel.GREEN, R.string.safety_msg_optimal);
        status.wind120 = cur.windSpeed120m;
        status.windSustained = cur.windSpeed10m;
        status.windDirection = cur.windDirection10m;
        status.gusts = cur.windGusts10m;
        status.kp = kp;
        status.rain = cur.precipitation;
        status.temperature = cur.temperature2m;
        status.apparentTemperature = cur.apparentTemperature;
        status.isDay = cur.isDay;
        status.cloudCover = cur.cloudCover;
        status.visibility = cur.visibility;
        status.rainProbability = rainProb;
        status.forecastDescResId = getWeatherDescRes(cur.weatherCode);

        // Procesar Horarios
        if (weather.hourly != null && weather.hourly.time != null) {
            for (int i = 0; i < Math.min(24, weather.hourly.time.size()); i++) {
                HourlyStatus hs = new HourlyStatus();
                String fullTime = weather.hourly.time.get(i);
                hs.time = fullTime.substring(fullTime.length() - 5); 
                hs.wind = weather.hourly.windSpeed10m.get(i);
                hs.gusts = weather.hourly.windGusts10m.get(i);
                hs.rainProb = weather.hourly.precipitationProbability.get(i);
                hs.cloudCover = weather.hourly.cloudCover.get(i);
                hs.weatherCode = weather.hourly.weatherCode.get(i);
                
                if (hs.wind > 35 || hs.gusts > 45 || hs.rainProb > 30) hs.level = SafetyLevel.RED;
                else if (hs.wind > 25 || hs.rainProb > 10) hs.level = SafetyLevel.YELLOW;
                else hs.level = SafetyLevel.GREEN;
                
                status.hourlyList.add(hs);
            }
        }

        // Procesar Pronóstico Diario
        if (weather.daily != null && weather.daily.time != null) {
            for (int i = 0; i < weather.daily.time.size(); i++) {
                DailyForecast df = new DailyForecast();
                df.date = weather.daily.time.get(i);
                df.weatherCode = weather.daily.weatherCode.get(i);
                df.tempMax = weather.daily.tempMax.get(i);
                df.tempMin = weather.daily.tempMin.get(i);
                df.rainProb = weather.daily.rainProbMax.get(i);
                status.dailyList.add(df);
            }
        }

        // 💡 ADVANCED ANALYSIS INTEGRATION
        FlightSafetyAnalyzer analyzer = new FlightSafetyAnalyzer(context);
        status.safetyAnalysis = analyzer.analyze(status, status.hourlyList, pdop);
        
        // Sync legacy fields with advanced analysis
        switch(status.safetyAnalysis.nivel) {
            case ROJO: status.level = SafetyLevel.RED; break;
            case NARANJA: status.level = SafetyLevel.ORANGE; break;
            case AMARILLO: status.level = SafetyLevel.YELLOW; break;
            default: status.level = SafetyLevel.GREEN;
        }
        
        status.recommendation = status.safetyAnalysis.ventanaOptima;
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
