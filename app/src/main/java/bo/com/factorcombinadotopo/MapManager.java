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

import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase Senior para gestionar la configuración y comportamiento de osmdroid MapView.
 * Corrige el renderizado de ArcGIS (Zoom/Y/X), gestiona la caché de mosaicos y 
 * asegura el cumplimiento de atribuciones legales.
 */
public class MapManager {

    private final MapView mapView;
    private final Context context;
    private MyLocationNewOverlay locationOverlay;
    private boolean isFirstFix = true; // No estático para que cada instancia maneje su centrado inicial
    private boolean autoCenterEnabled = true;
    private int currentMapMode = 0; // 0: Predeterminado (OSM), 1: Satélite (ArcGIS)
    private OnMarkerClickListener markerClickListener;

    public interface OnMarkerClickListener {
        void onMarkerLabelClick(String markerTitle);
    }

    public void setOnMarkerClickListener(OnMarkerClickListener listener) {
        this.markerClickListener = listener;
    }
    
    // Almacenamos solo los datos técnicos para persistencia de sesión
    private static class MarkerData {
        double lat, lon;
        String name;
        MarkerData(double lat, double lon, String name) {
            this.lat = lat; this.lon = lon; this.name = name;
        }
    }
    private static final List<MarkerData> sessionMarkers = new ArrayList<>();

    public static final String KEY_MAP_TYPE = "MapType"; // 0: Street, 1: Sat

    /**
     * Proveedor de mosaicos personalizado para ArcGIS World Imagery.
     */
    private static class ArcGISTileSource extends OnlineTileSourceBase {
        public ArcGISTileSource() {
            super("ArcGISWorldImagery", 0, 19, 256, "", new String[]{
                    "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/"});
        }

        @Override
        public String getTileURLString(long pMapTileIndex) {
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

    public void enableFollowLocation(boolean enable) {
        if (locationOverlay != null) {
            if (enable) locationOverlay.enableFollowLocation();
            else locationOverlay.disableFollowLocation();
        }
    }

    private void initConfiguration() {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean showLocation = prefs.getBoolean("ShowLocation", true);
        int mapType = prefs.getInt(KEY_MAP_TYPE, 0); 

        // Configuración crítica de osmdroid
        SharedPreferences globalPrefs = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        org.osmdroid.config.Configuration.getInstance().load(context, globalPrefs);
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(context.getPackageName());
        
        File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
        if (!osmdroidDir.exists()) osmdroidDir.mkdirs();
        
        org.osmdroid.config.Configuration.getInstance().setOsmdroidBasePath(osmdroidDir);
        
        String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
        File cacheDir = new File(osmdroidDir, cacheFolder);
        if (!cacheDir.exists()) cacheDir.mkdirs();
        
        org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(cacheDir);

        mapView.setUseDataConnection(true); 

        if (mapType == 1) {
            setSatelliteMode(true);
        } else {
            mapView.setTileSource(TileSourceFactory.MAPNIK);
            currentMapMode = 0;
        }
        
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(new GeoPoint(-17.0, -65.0));

        CopyrightOverlay copyrightOverlay = new CopyrightOverlay(context);
        mapView.getOverlays().add(copyrightOverlay);

        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
        if (showLocation) {
            locationOverlay.enableMyLocation();
        }

        locationOverlay.disableFollowLocation(); 
        mapView.getOverlays().add(locationOverlay);

        mapView.onResume(); 
        mapView.invalidate();
    }

    public void refreshMap() {
        if (mapView == null) return;
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int mapType = prefs.getInt(KEY_MAP_TYPE, 0);
        
        org.osmdroid.config.Configuration.getInstance().setUserAgentValue(context.getPackageName());
        
        File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
        org.osmdroid.config.Configuration.getInstance().setOsmdroidBasePath(osmdroidDir);
        
        String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
        org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));

        mapView.onResume(); 
        mapView.setUseDataConnection(true);
        
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

    public void setSatelliteMode(boolean enableSatellite) {
        if (mapView == null) return;
        try {
            SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putInt(KEY_MAP_TYPE, enableSatellite ? 1 : 0).apply();

            if (mapView.getTileProvider() != null) {
                mapView.getTileProvider().clearTileCache();
            }

            if (enableSatellite) {
                currentMapMode = 1;
                mapView.setTileSource(new ArcGISTileSource());
            } else {
                currentMapMode = 0;
                mapView.setTileSource(TileSourceFactory.MAPNIK);
            }

            File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
            org.osmdroid.config.Configuration.getInstance().setOsmdroidBasePath(osmdroidDir);
            
            String cacheFolder = (enableSatellite) ? "tiles_sat" : "tiles_street";
            File cacheDir = new File(osmdroidDir, cacheFolder);
            if (!cacheDir.exists()) cacheDir.mkdirs();
            
            org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(cacheDir);

            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void toggleMapType() {
        setSatelliteMode(currentMapMode == 0);
    }

    public String getCurrentMapModeName() {
        return (currentMapMode == 0) ? "Predeterminado" : "Satélite";
    }

    public void invalidate() {
        if (mapView != null) mapView.invalidate();
    }

    public void centerOnCurrentLocation() {
        if (mapView == null || locationOverlay == null || locationOverlay.getMyLocation() == null) return;
        mapView.getController().animateTo(locationOverlay.getMyLocation());
    }

    /**
     * Centra el mapa en una ubicación específica de forma inmediata.
     */
    public void centerToLocation(Location location) {
        if (mapView == null || location == null) return;
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().setCenter(point);
    }

    public void updateMyLocation(Location location) {
        if (location == null) return;
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (isFirstFix && autoCenterEnabled) {
            mapView.getController().animateTo(point);
            isFirstFix = false;
        }
    }

    private class LabelInfoWindow extends MarkerInfoWindow {
        public LabelInfoWindow(int layoutResId, MapView mapView) {
            super(layoutResId, mapView);
        }
        @Override
        public void onOpen(Object item) {
            Marker marker = (Marker) item;
            TextView txt = mView.findViewById(R.id.txt_marker_name);
            if (txt != null) {
                txt.setText(marker.getTitle());
            }
            
            // Hacer que la etiqueta sea clicable
            mView.setOnClickListener(v -> {
                if (markerClickListener != null) {
                    markerClickListener.onMarkerLabelClick(marker.getTitle());
                }
            });
        }
    }

    /**
     * Redibuja todos los marcadores de la sesión actual en el mapa.
     */
    public void restoreMarkers() {
        if (mapView == null) return;
        // Limpiar cualquier marcador visual residual pero conservar la lista técnica
        removeVisualMarkers();
        for (MarkerData data : sessionMarkers) {
            addMarkerToView(data.lat, data.lon, data.name);
        }
        mapView.invalidate();
    }

    /**
     * Limpia visualmente el mapa y VACÍA la sesión (Escoba).
     */
    public void clearManualMarkers() {
        removeVisualMarkers();
        clearSession();
        mapView.invalidate();
    }

    /**
     * Vacía la lista de marcadores de la sesión actual.
     */
    public static void clearSession() {
        sessionMarkers.clear();
    }

    private void removeVisualMarkers() {
        // Eliminar de forma segura buscando marcadores de tipo Pin Naranja
        List<org.osmdroid.views.overlay.Overlay> overlays = mapView.getOverlays();
        for (int i = overlays.size() - 1; i >= 0; i--) {
            if (overlays.get(i) instanceof Marker) {
                Marker m = (Marker) overlays.get(i);
                if (m.getTitle() != null && !m.getTitle().isEmpty()) {
                    m.closeInfoWindow(); // Cerrar la etiqueta/nombre antes de borrar el pin
                    mapView.getOverlays().remove(i);
                }
            }
        }
    }

    /**
     * Añade un marcador a la sesión y lo dibuja.
     */
    public void addManualMarker(IGeoPoint point, String name) {
        if (point == null) return;
        sessionMarkers.add(new MarkerData(point.getLatitude(), point.getLongitude(), name));
        addMarkerToView(point.getLatitude(), point.getLongitude(), name);
        mapView.invalidate();
    }

    private void addMarkerToView(double lat, double lon, String name) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(lat, lon));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Punta de la aguja
        marker.setTitle(name);
        
        Drawable icon = ContextCompat.getDrawable(context, R.drawable.ic_map_needle_pin);
        if (icon != null) {
            // Ya es naranja en el XML, no necesita tintado extra para transparencia
            marker.setIcon(icon);
        }

        marker.setInfoWindow(new LabelInfoWindow(R.layout.layout_marker_label, mapView));
        mapView.getOverlays().add(marker);
        marker.showInfoWindow();
    }

    public void onResume() {
        if (mapView != null) {
            mapView.onResume();
            restoreMarkers();
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

    /**
     * Libera recursos críticos para evitar fugas de memoria (Memory Leaks).
     */
    public void onDestroy() {
        if (mapView != null) {
            mapView.onDetach();
        }
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            locationOverlay.disableFollowLocation();
        }
    }
}
