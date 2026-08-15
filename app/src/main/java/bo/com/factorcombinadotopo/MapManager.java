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
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.CopyrightOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
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
    private boolean isInitialized = false;
    private int currentMapMode = 0; // 0: Predeterminado (OSM), 1: Satélite (ArcGIS)
    private final Object overlayLock = new Object();
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
        
        // Cargar modo inicial
        if (mapType == 1) {
            currentMapMode = 1;
            mapView.setTileSource(new ArcGISTileSource());
        } else {
            currentMapMode = 0;
            mapView.setTileSource(TileSourceFactory.MAPNIK);
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

        isInitialized = true;
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

            File osmdroidDir = new File(context.getExternalFilesDir(null), "osmdroid");
            String cacheFolder = (enableSatellite) ? "tiles_sat" : "tiles_street";
            File cacheDir = new File(osmdroidDir, cacheFolder);
            if (!cacheDir.exists()) cacheDir.mkdirs();
            
            // Actualizar configuración global
            org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(cacheDir);

            if (enableSatellite) {
                currentMapMode = 1;
                mapView.setTileSource(new ArcGISTileSource());
            } else {
                currentMapMode = 0;
                mapView.setTileSource(TileSourceFactory.MAPNIK);
            }

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
        if (location == null || !isInitialized) return;
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
            
            // Hacer que la etiqueta sea clicable y evitar que cierre por toque accidental
            mView.setOnClickListener(v -> {
                if (markerClickListener != null) {
                    markerClickListener.onMarkerLabelClick(marker.getTitle());
                }
            });
            
            // Asegurar que el click en el texto también funcione
            if (txt != null) {
                txt.setOnClickListener(v -> mView.performClick());
            }
        }
    }

    /**
     * Redibuja todos los marcadores de la sesión actual en el mapa.
     */
    public void restoreMarkers() {
        if (mapView == null) return;
        synchronized (overlayLock) {
            // Limpiar cualquier marcador visual residual pero conservar la lista técnica
            removeVisualMarkers();
            for (MarkerData data : sessionMarkers) {
                addMarkerToView(data.lat, data.lon, data.name);
            }
        }
        mapView.invalidate();
    }

    /**
     * Limpia visualmente el mapa y VACÍA la sesión (Escoba).
     */
    public void clearManualMarkers() {
        synchronized (overlayLock) {
            removeVisualMarkers();
            clearSession();
        }
        mapView.invalidate();
    }

    /**
     * Vacía la lista de marcadores de la sesión actual.
     */
    public static void clearSession() {
        sessionMarkers.clear();
    }

    /**
     * Añade un polígono al mapa con un color celeste transparente y etiqueta de área.
     */
    public void addPolygon(List<GeoPoint> points, String areaText) {
        if (mapView == null || points == null || points.size() < 3) return;

        Polygon polygon = new Polygon(mapView);
        polygon.setPoints(points);
        // Celeste transparente (#4000BFFF)
        polygon.getFillPaint().setColor(0x4000BFFF);
        polygon.getOutlinePaint().setColor(0xFF00BFFF);
        polygon.getOutlinePaint().setStrokeWidth(3.0f);

        synchronized (overlayLock) {
            mapView.getOverlays().add(polygon);

            // Añadir pines azules en los vértices para mayor claridad técnica
            Drawable vertexIcon = ContextCompat.getDrawable(context, R.drawable.ic_map_needle_pin);
            if (vertexIcon != null) {
                vertexIcon = DrawableCompat.wrap(vertexIcon).mutate();
                DrawableCompat.setTint(vertexIcon, ContextCompat.getColor(context, R.color.accent_primary));
            }

            for (int i = 0; i < points.size(); i++) {
                Marker vertexMarker = new Marker(mapView);
                vertexMarker.setPosition(points.get(i));
                vertexMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                vertexMarker.setIcon(vertexIcon);
                vertexMarker.setInfoWindow(null); // No queremos info windows en cada vértice
                mapView.getOverlays().add(vertexMarker);
            }

            // Calcular centroide para la etiqueta de área
            double sumLat = 0, sumLon = 0;
            for (GeoPoint p : points) {
                sumLat += p.getLatitude();
                sumLon += p.getLongitude();
            }
            GeoPoint centroid = new GeoPoint(sumLat / points.size(), sumLon / points.size());

            Marker areaLabel = new Marker(mapView);
            areaLabel.setPosition(centroid);
            areaLabel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            areaLabel.setIcon(null); // Sin icono de pin para el texto central
            areaLabel.setTitle(areaText);
            areaLabel.setInfoWindow(new LabelInfoWindow(R.layout.layout_marker_label, mapView));
            
            mapView.getOverlays().add(areaLabel);
            areaLabel.showInfoWindow();
        }
        mapView.invalidate();
    }

    /**
     * Añade una ruta de medición de distancia con etiquetas en cada segmento.
     */
    public void addDistancePath(List<GeoPoint> points, List<String> segmentTexts, String totalText) {
        if (mapView == null || points == null || points.size() < 2) return;

        Polyline line = new Polyline(mapView);
        line.setPoints(points);
        // Rojo suave (#FF7070)
        int redSoft = ContextCompat.getColor(context, R.color.accent_red_soft);
        line.getOutlinePaint().setColor(redSoft);
        line.getOutlinePaint().setStrokeWidth(5.0f);

        synchronized (overlayLock) {
            mapView.getOverlays().add(line);

            // Añadir pines rojos en los vértices
            Drawable vertexIcon = ContextCompat.getDrawable(context, R.drawable.ic_map_needle_pin);
            if (vertexIcon != null) {
                vertexIcon = DrawableCompat.wrap(vertexIcon).mutate();
                DrawableCompat.setTint(vertexIcon, redSoft);
            }

            for (int i = 0; i < points.size(); i++) {
                Marker vertexMarker = new Marker(mapView);
                vertexMarker.setPosition(points.get(i));
                vertexMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                vertexMarker.setIcon(vertexIcon);
                vertexMarker.setInfoWindow(null);
                mapView.getOverlays().add(vertexMarker);

                // Si es el último punto, añadir etiqueta de TOTAL
                if (i == points.size() - 1 && totalText != null) {
                    Marker totalLabel = new Marker(mapView);
                    totalLabel.setPosition(points.get(i));
                    totalLabel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                    totalLabel.setIcon(null);
                    totalLabel.setTitle("TOTAL: " + totalText);
                    totalLabel.setInfoWindow(new LabelInfoWindow(R.layout.layout_marker_label, mapView));
                    mapView.getOverlays().add(totalLabel);
                    totalLabel.showInfoWindow();
                }
            }

            // Etiquetas de segmentos (en el punto medio de cada tramo)
            if (segmentTexts != null) {
                for (int i = 0; i < segmentTexts.size(); i++) {
                    GeoPoint p1 = points.get(i);
                    GeoPoint p2 = points.get(i + 1);
                    GeoPoint mid = new GeoPoint((p1.getLatitude() + p2.getLatitude()) / 2.0, (p1.getLongitude() + p2.getLongitude()) / 2.0);

                    Marker segLabel = new Marker(mapView);
                    segLabel.setPosition(mid);
                    segLabel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    segLabel.setIcon(null);
                    segLabel.setTitle(segmentTexts.get(i));
                    segLabel.setInfoWindow(new LabelInfoWindow(R.layout.layout_marker_label, mapView));
                    mapView.getOverlays().add(segLabel);
                    segLabel.showInfoWindow();
                }
            }
        }
        mapView.invalidate();
    }

    private void removeVisualMarkers() {
        // Eliminar de forma segura buscando marcadores de tipo Pin Naranja, Polígonos y Polilíneas
        List<org.osmdroid.views.overlay.Overlay> overlays = mapView.getOverlays();
        synchronized (overlayLock) {
            for (int i = overlays.size() - 1; i >= 0; i--) {
                org.osmdroid.views.overlay.Overlay o = overlays.get(i);
                if (o instanceof Marker) {
                    Marker m = (Marker) o;
                    if (m.getTitle() != null && !m.getTitle().isEmpty()) {
                        m.closeInfoWindow();
                        overlays.remove(i);
                    }
                } else if (o instanceof Polygon || o instanceof Polyline) {
                    overlays.remove(i);
                }
            }
        }
    }

    /**
     * Añade un marcador a la sesión y lo dibuja.
     */
    public void addManualMarker(IGeoPoint point, String name) {
        if (point == null || !isInitialized) return;
        synchronized (overlayLock) {
            sessionMarkers.add(new MarkerData(point.getLatitude(), point.getLongitude(), name));
            addMarkerToView(point.getLatitude(), point.getLongitude(), name);
        }
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
