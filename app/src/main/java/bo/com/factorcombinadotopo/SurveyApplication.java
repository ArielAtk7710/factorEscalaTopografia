package bo.com.factorcombinadotopo;

import android.app.Application;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase de aplicación global para centralizar la inicialización asíncrona en Java.
 */
public class SurveyApplication extends Application {

    private final ExecutorService applicationExecutor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Boolean> _isReady = new MutableLiveData<>(false);
    public final LiveData<Boolean> isReady = _isReady;

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Iniciar inicialización en segundo plano usando pool de hilos de Java
        applicationExecutor.execute(() -> {
            try {
                initializeCriticalServices();
                _isReady.postValue(true);
            } catch (Exception e) {
                Log.e("SurveyApp", "Error during initialization", e);
            }
        });
    }

    private void initializeCriticalServices() {
        // 1. Configurar osmdroid
        IConfigurationProvider osmConfig = Configuration.getInstance();
        osmConfig.load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        osmConfig.setUserAgentValue(getPackageName());
        
        File osmdroidDir = new File(getExternalFilesDir(null), "osmdroid");
        if (!osmdroidDir.exists()) {
            boolean created = osmdroidDir.mkdirs();
            if (!created) Log.w("SurveyApp", "Could not create osmdroid directory");
        }
        osmConfig.setOsmdroidBasePath(osmdroidDir);
        osmConfig.setOsmdroidTileCache(new File(osmdroidDir, "tiles_street")); // Carpeta por defecto

        // Optimización para Uso Offline (500 MB por capa)
        osmConfig.setTileFileSystemCacheMaxBytes(500L * 1024 * 1024); 
        osmConfig.setTileFileSystemCacheTrimBytes(450L * 1024 * 1024); 
        osmConfig.setTileDownloadThreads((short) 8); // Descarga acelerada
        osmConfig.setExpirationExtendedDuration(30L * 24 * 60 * 60 * 1000); // 30 días de persistencia offline

        // 2. Cargar Motor Geoidal
        MGBEngine.getInstance().cargarGrillaSincrona(this);

        // 3. Sembrar Base de Datos
        DatabaseHelper.getInstance(this).seedExampleData();
    }
}
