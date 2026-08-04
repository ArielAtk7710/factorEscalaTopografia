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

        @SerializedName("weather_code")
        public int weatherCode;

        @SerializedName("wind_speed_10m")
        public double windSpeed10m;

        @SerializedName("wind_speed_80m")
        public double windSpeed80m;

        @SerializedName("wind_speed_120m")
        public double windSpeed120m;

        @SerializedName("wind_direction_120m")
        public int windDirection120m;

        @SerializedName("wind_gusts_10m")
        public double windGusts10m;

        @SerializedName("precipitation")
        public double precipitation;
    }

    public static class Hourly {
        @SerializedName("precipitation_probability")
        public java.util.List<Integer> precipitationProbability;
    }
}
