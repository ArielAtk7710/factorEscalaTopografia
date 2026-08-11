package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
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
    private static boolean isFirstFix = true; // Estático para que solo centre una vez por sesión de app
    private boolean autoCenterEnabled = true;
    private int currentMapMode = 0; // 0: Predeterminado (OSM), 1: Satélite (ArcGIS)
    
    private final java.util.List<Marker> manualMarkers = new java.util.ArrayList<>();

    public static final String KEY_MAP_TYPE = "MapType"; // 0: Street, 1: Sat

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
        boolean showLocation = prefs.getBoolean("ShowLocation", true);
        
        // Recuperar el último tipo de mapa usado (Callejero o Satélite)
        int mapType = prefs.getInt(KEY_MAP_TYPE, 0); 

        // Configurar ruta de caché específica para cada tipo (Street vs Sat)
        File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
        String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
        org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));

        // Conexión siempre activa para modo Online Pro
        mapView.setUseDataConnection(true); 

        // Aplicar el tipo de mapa guardado
        if (mapType == 1) {
            setSatelliteMode(true);
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            currentMapMode = 0;
        }
        
        mapView.setMultiTouchControls(true);

        // Cámara Inicial: Centrar en Bolivia (-17.0, -65.0) con zoom de terreno
        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(new GeoPoint(-17.0, -65.0));

        // 1. Atribución Legal (Copyright)
        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(context);
        mapView.getOverlays().add(copyrightOverlay);

        // 3. Capa de Ubicación (Punto azul)
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
        if (showLocation) {
            locationOverlay.enableMyLocation();
        }
        locationOverlay.disableFollowLocation(); 
        mapView.getOverlays().add(locationOverlay);

        // Forzar arranque de hilos de renderizado
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
        int mapType = prefs.getInt(KEY_MAP_TYPE, 0);
        
        // Actualizar ruta de caché dinámica para respetar la separación
        File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
        String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
        org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));

        // Forzar reinicio de hilos de conexión y renderizado
        mapView.onResume(); 
        mapView.setUseDataConnection(true);
        
        // Re-asignar TileSource según selección
        if (mapType == 1) {
            if (currentMapMode != 1) setSatelliteMode(true);
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
        if (mapView == null) return;
        try {
            // Guardar preferencia para persistencia
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_MAP_TYPE, enableSatellite ? 1 : 0).apply();

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

            // Actualizar la ruta de la caché global inmediatamente al cambiar modo
            File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
            String cacheFolder = (enableSatellite) ? "tiles_sat" : "tiles_street";
            org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));

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
        if (mapView == null || locationOverlay == null || locationOverlay.getMyLocation() == null) return;
        mapView.getController().animateTo(locationOverlay.getMyLocation());
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

    /**
     * Ventana de información personalizada para mostrar solo el nombre en un cuadro naranja.
     */
    private static class LabelInfoWindow extends MarkerInfoWindow {
        public LabelInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }
        @Override
        public void onOpen(Object item) {
            Marker marker = (Marker) item;
            TextView txt = mView.findViewById(R.id.txt_marker_name);
            if (txt != null) txt.setText(marker.getTitle());
        }
    }

    /**
     * Elimina todos los marcadores manuales (pines naranjas) del mapa.
     */
    public void clearManualMarkers() {
        if (mapView == null) return;
        for (Marker m : manualMarkers) {
            mapView.getOverlays().remove(m);
        }
        manualMarkers.clear();
        mapView.invalidate();
    }

    /**
     * Añade un marcador manual color naranja con etiqueta de nombre.
     */
    public void addManualMarker(IGeoPoint point, String name) {
        if (mapView == null || point == null) return;

        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(point.getLatitude(), point.getLongitude()));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(name);
        
        // Configurar Icono Naranja
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_map_pin);
        if (icon != null) {
            icon.setTint(ContextCompat.getColor(context, R.color.accent_orange));
            marker.setIcon(icon);
        }

        // Configurar Etiqueta Superior Naranja
        marker.setInfoWindow(new LabelInfoWindow(R.layout.layout_marker_label, mapView));
        
        mapView.getOverlays().add(marker);
        manualMarkers.add(marker); // Registrar para poder borrarlo después
        marker.showInfoWindow(); // Mostrar nombre automáticamente
        mapView.invalidate();
    }

    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
            mapView.invalidate();
        }
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
