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
    }
}
