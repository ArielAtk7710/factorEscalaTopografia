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
        
        // 1. Configuración global de osmdroid (Debe ir en el hilo principal para estabilidad)
        org.osmdroid.config.IConfigurationProvider osmConfig = org.osmdroid.config.Configuration.getInstance();
        osmConfig.load(this, android.preference.PreferenceManager.getDefaultSharedPreferences(this));
        osmConfig.setUserAgentValue(getPackageName());

        // Iniciar inicialización pesada en segundo plano
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
        // 1. Configurar directorios de osmdroid
        org.osmdroid.config.IConfigurationProvider osmConfig = org.osmdroid.config.Configuration.getInstance();
        
        File extDir = getExternalFilesDir(null);
        if (extDir != null) {
            File osmdroidDir = new File(extDir, "osmdroid");
            if (!osmdroidDir.exists()) osmdroidDir.mkdirs();
            
            osmConfig.setOsmdroidBasePath(osmdroidDir);
            osmConfig.setOsmdroidTileCache(new File(osmdroidDir, "tiles_street"));
        }

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
