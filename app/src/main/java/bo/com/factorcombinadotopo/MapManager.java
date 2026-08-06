package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;

/**
 * Clase Senior para gestionar la configuración y comportamiento de osmdroid MapView.
 * Corrige el renderizado de ArcGIS (Zoom/Y/X), gestiona la caché de mosaicos y 
 * asegura el cumplimiento de atribuciones legales.
 */
public class MapManager {

    private final MapView mapView;
    private final Context context;
    private MyLocationNewOverlay locationOverlay;
    private boolean isFirstFix = true;
    private boolean autoCenterEnabled = true;
    private int currentMapMode = 0; // 0: Predeterminado (OSM), 1: Satélite (ArcGIS)

    /**
     * Proveedor de mosaicos personalizado para ArcGIS World Imagery.
     * Corrige el orden de las coordenadas de X/Y a Y/X requerido por Esri.
     */
    private static class ArcGISTileSource extends OnlineTileSourceBase {
        public ArcGISTileSource() {
            super("ArcGISWorldImagery", 0, 19, 256, "", new String[]{
                    "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"});
        }

        @Override
        public String getTileURLString(long pMapTileIndex) {
            // Formato ArcGIS: BaseURL + Zoom + / + Y + / + X
            return getBaseUrl() 
                    + MapTileIndex.getZoom(pMapTileIndex) + "/" 
                    + MapTileIndex.getY(pMapTileIndex) + "/" 
                    + MapTileIndex.getX(pMapTileIndex);
        }
    }

    public MapManager(Context context, MapView mapView) {
        this.context = context;
        this.mapView = mapView;
        initConfiguration();
    }

    public void setAutoCenterEnabled(boolean enabled) {
        this.autoCenterEnabled = enabled;
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

    private void initConfiguration() {
        // La configuración base ya se hizo en MainActivity para asegurar el primer inicio.
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int mapMode = prefs.getInt("MapMode", 0); // 0: Online, 1: Offline, 2: Hybrid
        boolean showLocation = prefs.getBoolean("ShowLocation", true);

        // Gestión de conexión: Bloquear internet solo en modo Offline estricto
        mapView.setUseDataConnection(mapMode != 1); 

        // Configuración inicial de visualización basada en el modo
        if (mapMode == 2) {
            // Si es híbrido, empezamos con satélite por defecto pero permitimos offline
            setSatelliteMode(true);
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
        }
        
        mapView.setMultiTouchControls(true);

        // Cámara Inicial: Centrar en Bolivia (-17.0, -65.0) con zoom de terreno
        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(new GeoPoint(-17.0, -65.0));

        // Atribución Legal (Copyright) - Requerido por Google Play y Esri
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(context);
        mapView.getOverlays().add(copyrightOverlay);

        // Capa de Ubicación (Punto azul)
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
        if (showLocation) {
            locationOverlay.enableMyLocation();
        }
        locationOverlay.disableFollowLocation(); // Evitar saltos de cámara bruscos
        mapView.getOverlays().add(locationOverlay);

        // Forzar arranque de hilos de renderizado (Fix para el primer inicio)
        mapView.onResume(); 
        mapView.invalidate();
    }

    /**
     * Refresca el estado del mapa y reconecta con el proveedor de mosaicos.
     * Útil al regresar de otras pestañas.
     */
    public void refreshMap() {
        if (mapView == null) return;
        
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int mapMode = prefs.getInt("MapMode", 0);
        
        // Forzar reinicio de hilos de conexión y renderizado
        mapView.onResume(); 
        mapView.setUseDataConnection(mapMode != 1);
        
        // Re-asignar TileSource para forzar recarga de mosaicos
        if (mapMode == 2) {
            if (currentMapMode != 1) setSatelliteMode(true);
        } else if (mapMode == 1) {
            // Modo offline puro
            mapView.setTileSource(TileSourceFactory.MAPNIK);
        } else {
            if (currentMapMode != 0) setSatelliteMode(false);
        }
        
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
        
        mapView.invalidate();
    }

    /**
     * Establece el modo satelital con corrección de coordenadas y limpieza de caché.
     */
    public void setSatelliteMode(boolean enableSatellite) {
        try {
            // Limpiar la caché de los mosaicos cargados actualmente para evitar el efecto "barajado"
            if (mapView.getTileProvider() != null) {
                mapView.getTileProvider().clearTileCache();
            }

            if (enableSatellite) {
                currentMapMode = 1;
                ArcGISTileSource satelliteSource = new ArcGISTileSource();
                mapView.setTileSource(satelliteSource);
            } else {
                currentMapMode = 0;
                mapView.setTileSource(TileSourceFactory.MAPNIK);
            }

            mapView.invalidate(); // Refrescar renderizado
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Alterna entre el modo callejero y satelital.
     */
    public void toggleMapType() {
        setSatelliteMode(currentMapMode == 0);
    }

    public String getCurrentMapModeName() {
        return (currentMapMode == 0) ? "Predeterminado" : "Satélite";
    }

    /**
     * Fuerza el redibujado de la vista del mapa.
     */
    public void invalidate() {
        if (mapView != null) mapView.invalidate();
    }

    /**
     * Centra el mapa en la posición real capturada por el sensor.
     */
    public void centerOnCurrentLocation() {
        if (locationOverlay != null && locationOverlay.getMyLocation() != null) {
            mapView.getController().animateTo(locationOverlay.getMyLocation());
        }
    }

    /**
     * Actualiza la ubicación interna y centra la cámara solo en el primer fix.
     */
    public void updateMyLocation(Location location) {
        if (location == null) return;
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (isFirstFix && autoCenterEnabled) {
            mapView.getController().animateTo(point);
            isFirstFix = false;
        }
    }

    public void onResume() {
        if (mapView != null) mapView.onResume();
        if (locationOverlay != null) {
            locationOverlay.enableMyLocation();
        }
    }

    public void onPause() {
        if (mapView != null) mapView.onPause();
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            locationOverlay.disableFollowLocation();
        }
    }
}
