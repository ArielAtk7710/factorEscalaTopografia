package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
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
    private static boolean isFirstFix = true; // Estático para que solo centre una vez por sesión de app
    private boolean autoCenterEnabled = true;
    private int currentMapMode = 0; // 0: Predeterminado (OSM), 1: Satélite (ArcGIS)
    
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

        File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
        String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
        org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));

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

        // 3. Capa de Ubicación (Chincheta Blanca sobre Círculo Azul)
        locationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), mapView);
        if (showLocation) {
            locationOverlay.enableMyLocation();
        }

        // Personalización del Icono de Ubicación (Reemplazo del Punto Azul)
        Drawable personDrawable = ContextCompat.getDrawable(context, R.drawable.ic_user_location_pin);
        if (personDrawable != null) {
            Bitmap personBitmap = drawableToBitmap(personDrawable);
            locationOverlay.setPersonIcon(personBitmap);
            locationOverlay.setPersonAnchor(0.5f, 0.5f); // Centro del círculo azul
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
        
        File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
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
            String cacheFolder = (enableSatellite) ? "tiles_sat" : "tiles_street";
            org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));

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

    public void updateMyLocation(Location location) {
        if (location == null) return;
        GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (isFirstFix && autoCenterEnabled) {
            mapView.getController().animateTo(point);
            isFirstFix = false;
        }
    }

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
     * Helper interno para convertir vectores a bitmaps (usado para la Chincheta de Ubicación).
     */
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
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
