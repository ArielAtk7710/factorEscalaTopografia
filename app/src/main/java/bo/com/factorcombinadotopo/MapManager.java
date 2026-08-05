package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Environment;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;

/**
 * Clase auxiliar para gestionar la configuración y comportamiento de osmdroid MapView.
 */
public class MapManager {

    private final MapView mapView;
    private final Context context;
    private MyLocationNewOverlay locationOverlay;
    private TilesOverlay hybridLabelsOverlay;
    private boolean isFirstFix = true;
    private boolean autoCenterEnabled = true;
    private int currentMapMode = 0; // 0: Predeterminado, 1: Satélite

    public MapManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        initConfiguration();
    }

    public void setAutoCenterEnabled(boolean enabled) {
        this.autoCenterEnabled = enabled;
    }

    private void initConfiguration() {
        // Configurar User-Agent (Requerido por los servidores de OSM)
        Configuration.getInstance().setUserAgentValue(context.getPackageName());

        // Cargar preferencias de mapas
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int mapMode = prefs.getInt("MapMode", 0); // 0: Online, 1: Offline, 2: Hybrid
        boolean showLocation = prefs.getBoolean("ShowLocation", true);

        // Configuración de conectividad
        if (mapMode == 1) { // Offline
            mapView.setUseDataConnection(false);
        } else {
            mapView.setUseDataConnection(true);
        }

        // Cargar mapa local si existe
        File mapsDir = context.getExternalFilesDir("Mapas");
        if (mapsDir != null && mapsDir.exists()) {
            File[] files = mapsDir.listFiles();
            if (files != null && files.length > 0) {
                // Si hay un archivo, osmdroid lo buscará automáticamente en su sistema de proveedores
                // solo necesitamos asegurar que la ruta de la caché apunte ahí para archivos offline.
                Configuration.getInstance().setOsmdroidTileCache(mapsDir);
            }
        }

        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);

        // Configurar el overlay de "Mi Ubicación" (punto azul)
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
        if (showLocation) {
            locationOverlay.enableMyLocation();
        } else {
            locationOverlay.disableMyLocation();
        }
        
        // IMPORTANTE: Desactivar seguimiento automático para permitir navegación libre
        locationOverlay.disableFollowLocation(); 
        mapView.getOverlays().add(locationOverlay);
    }

    /**
     * Activa o desactiva el seguimiento automático de la ubicación.
     */
    public void enableFollowLocation(boolean enable) {
        if (locationOverlay != null) {
            if (enable) locationOverlay.enableFollowLocation();
            else locationOverlay.disableFollowLocation();
        }
    }

    /**
     * Establece el modo híbrido (Satélite + Etiquetas).
     */
    public void setHybridMode() {
        try {
            // 1. Base: Satélite ArcGIS
            mapView.setTileSource(new XYTileSource(
                    "ArcGIS_Satellite", 0, 19, 256, ".jpg", new String[]{
                    "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"}));

            // 2. Capa de etiquetas (Límites y Calles)
            if (hybridLabelsOverlay != null) {
                mapView.getOverlays().remove(hybridLabelsOverlay);
            }

            XYTileSource labelsSource = new XYTileSource(
                    "ArcGIS_Hybrid_Labels", 0, 19, 256, ".png", new String[]{
                    "https://services.arcgisonline.com/ArcGIS/rest/services/Reference/World_Boundaries_and_Places/MapServer/tile/"});

            hybridLabelsOverlay = new TilesOverlay(new org.osmdroid.tileprovider.MapTileProviderBasic(context, labelsSource), context);
            hybridLabelsOverlay.setLoadingBackgroundColor(Color.TRANSPARENT);
            
            // Insertar en la posición 0 (debajo del marcador de ubicación pero arriba del satélite)
            mapView.getOverlays().add(0, hybridLabelsOverlay);
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Alterna entre vista de mapa predeterminado (callejero) y satelital (fotos reales).
     */
    public void toggleMapType() {
        try {
            currentMapMode = (currentMapMode + 1) % 2;
            
            switch (currentMapMode) {
                case 0: // Predeterminado (OSM)
                    mapView.setTileSource(TileSourceFactory.MAPNIK);
                    break;
                case 1: // Satélite (ArcGIS World Imagery - Fotos Reales)
                    mapView.setTileSource(new XYTileSource(
                            "ArcGIS_Satellite", 0, 19, 256, ".jpg", new String[]{
                            "https://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"}));
                    break;
            }
            mapView.invalidate(); // Refrescar el mapa para forzar la carga de la nueva capa
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retorna el nombre de la capa actual para feedback al usuario.
     */
    public String getCurrentMapModeName() {
        return (currentMapMode == 0) ? "Predeterminado" : "Satélite";
    }

    /**
     * Centra el mapa manualmente en la posición actual del usuario.
     */
    public void centerOnCurrentLocation() {
        if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
            mapView.getController().animateTo(locationOverlay.getMyLocation());
        }
    }

    /**
     * Actualiza la ubicación y centra la cámara si es la primera vez.
     */
    public void updateMyLocation(Location location) {
        if (location == null) return;
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (isFirstFix && autoCenterEnabled) {
            mapView.getController().animateTo(point);
            isFirstFix = false;
        }
    }

    /**
     * Centra el mapa en una ubicación específica.
     */
    public void centerOnLocation(Location location) {
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().animateTo(startPoint);
    }

    /**
     * Reanuda las operaciones del mapa.
     */
    public void onResume() {
        if (mapView != null) mapView.onResume();
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    /**
     * Pausa las operaciones del mapa para liberar recursos.
     */
    public void onPause() {
        if (mapView != null) mapView.onPause();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            locationOverlay.disableFollowLocation();
        }
    }
}
