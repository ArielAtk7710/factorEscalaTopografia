package bo.com.factorcombinadotopo;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

        // 🔗 NUEVO SISTEMA DE RESPALDO (10 SERVICIOS)
        WeatherFallbackManager fallbackManager = new WeatherFallbackManager(context, lat, lon, new WeatherFallbackManager.FallbackCallback() {
            @Override
            public void onSuccess(bo.com.factorcombinadotopo.models.UnifiedWeatherData data) {
                // Obtener KP en paralelo como antes
                fetchKpAndEvaluate(context, data, pdop, callback);
            }

            @Override
            public void onError(String finalError) {
                callback.onError(finalError);
            }
        });
        fallbackManager.start();
    }

    private static void fetchKpAndEvaluate(Context context, bo.com.factorcombinadotopo.models.UnifiedWeatherData weather, double pdop, WeatherCallback callback) {
        getApiService().getKpIndex("https://services.swpc.noaa.gov/products/noaa-planetary-k-index.json")
                .enqueue(new Callback<List<bo.com.factorcombinadotopo.models.KpIndex>>() {
            @Override
            public void onResponse(Call<List<bo.com.factorcombinadotopo.models.KpIndex>> call, Response<List<bo.com.factorcombinadotopo.models.KpIndex>> response) {
                double kp = -1;
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    bo.com.factorcombinadotopo.models.KpIndex lastEntry = response.body().get(response.body().size() - 1);
                    kp = lastEntry.kp;
                }
                evaluateSafetyUnified(context, weather, kp, pdop, callback);
            }

            @Override
            public void onFailure(Call<List<bo.com.factorcombinadotopo.models.KpIndex>> call, Throwable t) {
                evaluateSafetyUnified(context, weather, -1.0, pdop, callback);
            }
        });
    }

    private static void evaluateSafetyUnified(Context context, bo.com.factorcombinadotopo.models.UnifiedWeatherData weather, double kp, double pdop, WeatherCallback callback) {
        // Basic status for legacy UI
        SafetyStatus status = new SafetyStatus(SafetyLevel.GREEN, R.string.safety_msg_optimal);
        status.wind120 = weather.windSpeed120m;
        status.windSustained = weather.windSpeed10m;
        status.windDirection = weather.windDirection10m;
        status.gusts = weather.windGusts10m;
        status.kp = kp;
        status.rain = weather.precipitation;
        status.temperature = weather.temperature;
        status.apparentTemperature = weather.apparentTemperature != 0 ? weather.apparentTemperature : weather.temperature;
        status.isDay = weather.isDay;
        status.cloudCover = weather.cloudCover;
        status.visibility = weather.visibility;
        status.rainProbability = weather.rainProbability;
        status.forecastDescResId = getWeatherDescRes(weather.weatherCode);

        // Procesar Horarios
        for (bo.com.factorcombinadotopo.models.UnifiedWeatherData.Hourly h : weather.hourlyList) {
            HourlyStatus hs = new HourlyStatus();
            hs.time = h.time.contains("T") ? h.time.substring(h.time.indexOf("T") + 1, h.time.indexOf("T") + 6) : h.time;
            hs.wind = h.windSpeed;
            hs.gusts = h.windGusts;
            hs.rainProb = h.rainProb;
            hs.cloudCover = h.cloudCover;
            hs.weatherCode = h.weatherCode;
            
            if (hs.wind > 35 || hs.gusts > 45 || hs.rainProb > 30) hs.level = SafetyLevel.RED;
            else if (hs.wind > 25 || hs.rainProb > 10) hs.level = SafetyLevel.YELLOW;
            else hs.level = SafetyLevel.GREEN;
            
            status.hourlyList.add(hs);
        }

        // Procesar Pronóstico Diario
        for (bo.com.factorcombinadotopo.models.UnifiedWeatherData.Daily d : weather.dailyList) {
            DailyForecast df = new DailyForecast();
            df.date = d.date;
            df.weatherCode = d.weatherCode;
            df.tempMax = d.tempMax;
            df.tempMin = d.tempMin;
            df.rainProb = d.rainProb;
            status.dailyList.add(df);
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
        
        // Notificar al usuario qué proveedor proporcionó los datos en el detalle
        status.safetyAnalysis.detalle += "\n\nFuente: " + weather.providerName;
        
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
