package bo.com.factorcombinadotopo;

import android.app.Application;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * ViewModel para gestionar el estado de los cálculos topográficos en tiempo real en Java.
 */
public class SurveyViewModel extends AndroidViewModel {

    private final TopographyRepository repository;
    private final MutableLiveData<Location> rawLocation = new MutableLiveData<>();
    private final MutableLiveData<TopoCalculoManager.TopoResult> calculationResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> usesMgb = new MutableLiveData<>();
    private final MutableLiveData<Double> ambientTemperature = new MutableLiveData<>();
    private final MutableLiveData<WeatherManager.SafetyStatus> weatherStatus = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isWeatherLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorResult = new MutableLiveData<>();

    public SurveyViewModel(@NonNull Application application) {
        super(application);
        this.repository = TopographyRepository.getInstance(application);
    }

    public LiveData<Location> getRawLocation() {
        return rawLocation;
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

        repository.calculateCompleteAsync(loc, new TopographyRepository.CalculationCallback() {
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

        // Disparar actualización de clima en segundo plano
        refreshWeather(loc);
    }

    public void refreshWeather(Location loc) {
        if (loc == null) return;
        isWeatherLoading.postValue(true);
        // Simular captura de PDOP (1.8 constante por ahora como en el fragmento)
        WeatherManager.checkFlightSafety(getApplication(), loc.getLatitude(), loc.getLongitude(), 1.8, new WeatherManager.WeatherCallback() {
            @Override
            public void onSuccess(WeatherManager.SafetyStatus status) {
                weatherStatus.postValue(status);
                isWeatherLoading.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorResult.postValue(error);
                isWeatherLoading.postValue(false);
            }
        });
    }
}
