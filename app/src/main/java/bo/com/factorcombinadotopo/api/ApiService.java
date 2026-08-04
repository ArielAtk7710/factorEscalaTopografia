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
            @Query("models") String model
    );

    @GET
    Call<List<List<String>>> getKpIndex(@Url String url);
}
