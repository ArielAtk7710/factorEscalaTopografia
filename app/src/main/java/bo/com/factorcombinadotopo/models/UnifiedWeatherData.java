package bo.com.factorcombinadotopo.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de datos unificado para normalizar las respuestas de diferentes proveedores de clima.
 */
public class UnifiedWeatherData {
    public double temperature;
    public double apparentTemperature;
    public int weatherCode;
    public int isDay;
    public double windSpeed10m;
    public double windSpeed120m;
    public int windDirection10m;
    public double windGusts10m;
    public double precipitation;
    public int cloudCover;
    public double visibility;
    public int rainProbability;
    public String providerName;

    public List<Hourly> hourlyList = new ArrayList<>();
    public List<Daily> dailyList = new ArrayList<>();

    public static class Hourly {
        public String time;
        public double temperature;
        public int rainProb;
        public int weatherCode;
        public double windSpeed;
        public double windGusts;
        public int cloudCover;
    }

    public static class Daily {
        public String date;
        public int weatherCode;
        public double tempMax;
        public double tempMin;
        public int rainProb;
    }
}
