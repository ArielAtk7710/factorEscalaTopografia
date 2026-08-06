package bo.com.factorcombinadotopo.models;

import com.google.gson.annotations.SerializedName;

public class WeatherResponse {
    @SerializedName("current")
    public CurrentWeather current;

    @SerializedName("hourly")
    public Hourly hourly;

    public static class CurrentWeather {
        @SerializedName("temperature_2m")
        public double temperature2m;

        @SerializedName("apparent_temperature")
        public double apparentTemperature;

        @SerializedName("weather_code")
        public int weatherCode;

        @SerializedName("wind_speed_10m")
        public double windSpeed10m;

        @SerializedName("wind_speed_80m")
        public double windSpeed80m;

        @SerializedName("wind_speed_120m")
        public double windSpeed120m;

        @SerializedName("wind_direction_10m")
        public int windDirection10m;

        @SerializedName("wind_gusts_10m")
        public double windGusts10m;

        @SerializedName("precipitation")
        public double precipitation;

        @SerializedName("cloud_cover")
        public int cloudCover;

        @SerializedName("visibility")
        public double visibility;
    }

    public static class Hourly {
        @SerializedName("time")
        public java.util.List<String> time;

        @SerializedName("temperature_2m")
        public java.util.List<Double> temperature2m;

        @SerializedName("precipitation_probability")
        public java.util.List<Integer> precipitationProbability;

        @SerializedName("weather_code")
        public java.util.List<Integer> weatherCode;

        @SerializedName("wind_speed_10m")
        public java.util.List<Double> windSpeed10m;

        @SerializedName("wind_gusts_10m")
        public java.util.List<Double> windGusts10m;

        @SerializedName("cloud_cover")
        public java.util.List<Integer> cloudCover;
    }
}
