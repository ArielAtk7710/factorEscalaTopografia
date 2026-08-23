package bo.com.factorcombinadotopo;

import android.app.Application;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import bo.com.factorcombinadotopo.gnss.GnssFilter;
import bo.com.factorcombinadotopo.gnss.GnssMeasurement;

/**
 * ViewModel para gestionar el estado de los cálculos topográficos en tiempo real en Java.
 */
public class SurveyViewModel extends AndroidViewModel {

    private final TopographyRepository repository;
    private final GnssFilter gnssFilter = new GnssFilter();
    
    private final MutableLiveData<Location> rawLocation = new MutableLiveData<>();
    private final MutableLiveData<GnssMeasurement> filteredLocation = new MutableLiveData<>();
    private final MutableLiveData<TopoCalculoManager.TopoResult> calculationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> usesMgb = new MutableLiveData<>();
    private final MutableLiveData<Double> ambientTemperature = new MutableLiveData<>();
    private final MutableLiveData<WeatherManager.SafetyStatus> weatherStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWeatherLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorResult = new MutableLiveData<>();

    private Location lastWeatherLocation;
    private long lastWeatherTimestamp = 0;
    private long lastWeatherErrorTimestamp = 0;
    private static final float WEATHER_UPDATE_DISTANCE_THRESHOLD = 1000f; // 1 km
    private static final long WEATHER_UPDATE_TIME_THRESHOLD = 30 * 60 * 1000; // 30 min
    private static final long WEATHER_ERROR_BACKOFF = 5 * 60 * 1000; // 5 min si hubo error

    public SurveyViewModel(@NonNull Application application) {
        super(application);
        this.repository = TopographyRepository.getInstance(application);
    }

    public LiveData<Location> getRawLocation() {
        return rawLocation;
    }

    public LiveData<GnssMeasurement> getFilteredLocation() {
        return filteredLocation;
    }

    public LiveData<TopoCalculoManager.TopoResult> getCalculationResult() {
        return calculationResult;
    }

    public LiveData<Boolean> getUsesMgb() {
        return usesMgb;
    }

    public LiveData<Double> getAmbientTemperature() {
        return ambientTemperature;
    }

    public LiveData<WeatherManager.SafetyStatus> getWeatherStatus() {
        return weatherStatus;
    }

    public LiveData<Boolean> isWeatherLoading() {
        return isWeatherLoading;
    }

    public LiveData<String> getErrorResult() {
        return errorResult;
    }

    /**
     * Procesa una nueva ubicación disparando el cálculo asíncrono.
     */
    public void processNewLocation(Location loc) {
        if (loc == null || Double.isNaN(loc.getLatitude()) || Double.isNaN(loc.getLongitude())) {
            return;
        }
        
        rawLocation.postValue(loc);

        // 🛡️ CAPA GNSS AVANZADA: Filtrar antes de calcular
        GnssMeasurement filtered = gnssFilter.filter(loc);
        filteredLocation.postValue(filtered);

        // Crear objeto de ubicación filtrada para el motor de cálculo
        Location processedLoc = new Location(loc);
        processedLoc.setLatitude(filtered.latitude);
        processedLoc.setLongitude(filtered.longitude);
        processedLoc.setAltitude(filtered.altitude);
        // La precisión filtrada es informativa, no afecta al motor IGM
        
        repository.calculateCompleteAsync(processedLoc, new TopographyRepository.CalculationCallback() {
            @Override
            public void onResult(TopoCalculoManager.TopoResult result, boolean isMgb) {
                calculationResult.postValue(result);
                usesMgb.postValue(isMgb);
                ambientTemperature.postValue(repository.getCurrentAmbientTemp());
            }

            @Override
            public void onError(Exception e) {
                errorResult.postValue(e.getMessage());
            }
        });

        // ❌ CARGA AUTOMÁTICA DESACTIVADA: El clima ahora se cargará bajo demanda 
        // cuando el usuario entre a la pestaña de "Clima Vuelo Dron".
        // shouldRefreshWeatherAuto(processedLoc);
    }

    public void startStaticMeasurement() {
        gnssFilter.reset();
    }

    private void shouldRefreshWeatherAuto(Location loc) {
        long currentTime = System.currentTimeMillis();
        
        // 1. Si hubo un error reciente (Sin red), esperar 5 min antes de reintentar
        if (currentTime - lastWeatherErrorTimestamp < WEATHER_ERROR_BACKOFF) {
            return;
        }

        // 2. Si es la primera vez
        if (lastWeatherLocation == null || lastWeatherTimestamp == 0) {
            refreshWeather(loc);
            return;
        }

        // 3. Si ya hay una carga en curso, no hacer nada
        if (Boolean.TRUE.equals(isWeatherLoading.getValue())) return;

        // 3. Verificar distancia
        float distance = loc.distanceTo(lastWeatherLocation);
        
        // 4. Verificar tiempo
        long timeDiff = currentTime - lastWeatherTimestamp;

        if (distance > WEATHER_UPDATE_DISTANCE_THRESHOLD || timeDiff > WEATHER_UPDATE_TIME_THRESHOLD) {
            refreshWeather(loc);
        }
    }

    public void refreshWeather(Location loc) {
        if (loc == null) return;
        isWeatherLoading.postValue(true);
        // Simular captura de PDOP (1.8 constante por ahora como en el fragmento)
        WeatherManager.checkFlightSafety(getApplication(), loc.getLatitude(), loc.getLongitude(), 1.8, new WeatherManager.WeatherCallback() {
            @Override
            public void onSuccess(WeatherManager.SafetyStatus status) {
                lastWeatherLocation = loc;
                lastWeatherTimestamp = System.currentTimeMillis();
                weatherStatus.postValue(status);
                isWeatherLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                lastWeatherErrorTimestamp = System.currentTimeMillis();
                errorResult.postValue(error);
                isWeatherLoading.postValue(false);
            }
        });
    }
}
