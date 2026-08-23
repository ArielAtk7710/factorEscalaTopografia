package bo.com.factorcombinadotopo.api;

import java.util.List;

import bo.com.factorcombinadotopo.models.WeatherResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {

    @GET("https://api.open-meteo.com/v1/forecast")
    Call<WeatherResponse> getDroneWeather(
            @Query("latitude") double lat,
            @Query("longitude") double lon,
            @Query("current") String currentParams,
            @Query("hourly") String hourlyParams,
            @Query("daily") String dailyParams,
            @Query("timezone") String timezone,
            @Query("models") String model
    );

    @GET
    Call<java.util.List<bo.com.factorcombinadotopo.models.KpIndex>> getKpIndex(@Url String url);

    @GET("https://api.open-meteo.com/v1/elevation")
    Call<bo.com.factorcombinadotopo.models.ElevationResponse> getElevation(
            @Query("latitude") double lat,
            @Query("longitude") double lon
    );

    @GET
    Call<com.google.gson.JsonObject> getGenericWeather(@Url String url);
}
