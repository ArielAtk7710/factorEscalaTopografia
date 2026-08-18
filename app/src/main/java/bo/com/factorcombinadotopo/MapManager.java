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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.content.ContextCompat;
import android.view.ViewGroup;

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

        // Configuración crítica de osmdroid (No recargar prefs globales aquí para evitar bloqueos)
        org.osmdroid.config.IConfigurationProvider config = org.osmdroid.config.Configuration.getInstance();
        config.setUserAgentValue(context.getPackageName());
        
        File extDir = context.getExternalFilesDir(null);
        if (extDir != null) {
            File osmdroidDir = new File(extDir, "osmdroid");
            if (!osmdroidDir.exists()) osmdroidDir.mkdirs();
            config.setOsmdroidBasePath(osmdroidDir);
            
            String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
            File cacheDir = new File(osmdroidDir, cacheFolder);
            if (!cacheDir.exists()) cacheDir.mkdirs();
            config.setOsmdroidTileCache(cacheDir);
        }

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
        // Se elimina mapView.onResume() de aquí, se gestiona en el ciclo de vida del fragmento
        mapView.invalidate();
    }

    public void refreshMap() {
        if (mapView == null) return;
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int mapType = prefs.getInt(KEY_MAP_TYPE, 0);
        
        org.osmdroid.config.IConfigurationProvider config = org.osmdroid.config.Configuration.getInstance();
        config.setUserAgentValue(context.getPackageName());
        
        File extDir = context.getExternalFilesDir(null);
        if (extDir != null) {
            File osmdroidDir = new File(extDir, "osmdroid");
            String cacheFolder = (mapType == 1) ? "tiles_sat" : "tiles_street";
            config.setOsmdroidTileCache(new File(osmdroidDir, cacheFolder));
        }

        updateNetworkState(NetworkUtils.isNetworkAvailable(context));
        
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
     * Activa o desactiva la conexión de datos del mapa.
     * Mejora el rendimiento offline al evitar intentos de descarga fallidos.
     */
    public void updateNetworkState(boolean isOnline) {
        if (mapView != null) {
            mapView.setUseDataConnection(isOnline);
        }
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
            
            // Actualizar configuración global de caché
            org.osmdroid.config.Configuration.getInstance().setOsmdroidTileCache(cacheDir);

            // Cambiar fuente y limpiar memoria para forzar recarga desde el nuevo almacén
            if (enableSatellite) {
                currentMapMode = 1;
                mapView.setTileSource(new ArcGISTileSource());
            } else {
                currentMapMode = 0;
                mapView.setTileSource(TileSourceFactory.MAPNIK);
            }
            
            mapView.getTileProvider().clearTileCache();
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
        try {
            if (mapView == null || locationOverlay == null || locationOverlay.getMyLocation() == null) return;
            IMapController controller = mapView.getController();
            if (controller != null) {
                controller.animateTo(locationOverlay.getMyLocation());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Centra el mapa en una ubicación específica de forma inmediata.
     */
    public void centerToLocation(Location location) {
        try {
            if (mapView == null || location == null) return;
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            IMapController controller = mapView.getController();
            if (controller != null) {
                controller.setCenter(point);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateMyLocation(Location location) {
        try {
            if (location == null || !isInitialized || mapView == null) return;
            GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
            IMapController controller = mapView.getController();
            if (isFirstFix && autoCenterEnabled && controller != null) {
                controller.animateTo(point);
                isFirstFix = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        try {
            synchronized (overlayLock) {
                // Limpiar cualquier marcador visual residual pero conservar la lista técnica
                removeVisualMarkers();
                for (MarkerData data : sessionMarkers) {
                    addMarkerToView(data.lat, data.lon, data.name);
                }
            }
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
     * Crea un Bitmap a partir del layout de etiqueta naranja para uso como icono permanente.
     */
    private Drawable createLabelDrawable(String text) {
        try {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_marker_label, null);
            TextView tv = view.findViewById(R.id.txt_marker_name);
            if (tv != null) tv.setText(text);

            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

            Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);

            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Añade un polígono al mapa con un color celeste transparente y etiqueta de área.
     */
    public void addPolygon(List<GeoPoint> points, String areaText) {
        if (mapView == null || points == null || points.size() < 3) return;

        Polygon polygon = new Polygon(mapView);
        polygon.setPoints(points);
        polygon.setInfoWindow(null); // Eliminar burbuja gris predeterminada
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
                DrawableCompat.setTint(vertexIcon, ContextCompat.getColor(context, R.color.color_blue_intense));
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
            
            // Usar icono permanente en lugar de InfoWindow volátil
            Drawable labelIcon = createLabelDrawable(areaText);
            if (labelIcon != null) {
                areaLabel.setIcon(labelIcon);
            } else {
                areaLabel.setTitle(areaText);
                areaLabel.setInfoWindow(new LabelInfoWindow(R.layout.layout_marker_label, mapView));
            }
            
            areaLabel.setInfoWindow(null); // Desactivar reacción al toque
            mapView.getOverlays().add(areaLabel);
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
        line.setInfoWindow(null); // Eliminar burbuja gris predeterminada
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

                // Si es el último punto, añadir etiqueta de TOTAL permanente
                if (i == points.size() - 1 && totalText != null) {
                    Marker totalLabel = new Marker(mapView);
                    totalLabel.setPosition(points.get(i));
                    totalLabel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                    
                    Drawable totalIcon = createLabelDrawable("TOTAL: " + totalText);
                    if (totalIcon != null) {
                        totalLabel.setIcon(totalIcon);
                    } else {
                        totalLabel.setTitle("TOTAL: " + totalText);
                    }
                    totalLabel.setInfoWindow(null);
                    mapView.getOverlays().add(totalLabel);
                }
            }

            // Etiquetas de segmentos permanentes (en el punto medio de cada tramo)
            if (segmentTexts != null) {
                for (int i = 0; i < segmentTexts.size(); i++) {
                    GeoPoint p1 = points.get(i);
                    GeoPoint p2 = points.get(i + 1);
                    GeoPoint mid = new GeoPoint((p1.getLatitude() + p2.getLatitude()) / 2.0, (p1.getLongitude() + p2.getLongitude()) / 2.0);

                    Marker segLabel = new Marker(mapView);
                    segLabel.setPosition(mid);
                    segLabel.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
                    
                    Drawable segIcon = createLabelDrawable(segmentTexts.get(i));
                    if (segIcon != null) {
                        segLabel.setIcon(segIcon);
                    } else {
                        segLabel.setTitle(segmentTexts.get(i));
                    }
                    segLabel.setInfoWindow(null);
                    mapView.getOverlays().add(segLabel);
                }
            }
        }
        mapView.invalidate();
    }

    private void removeVisualMarkers() {
        if (mapView == null) return;
        try {
            // Eliminar todos los marcadores técnicos, polígonos y rutas sin excepción
            List<org.osmdroid.views.overlay.Overlay> overlays = mapView.getOverlays();
            synchronized (overlayLock) {
                for (int i = overlays.size() - 1; i >= 0; i--) {
                    org.osmdroid.views.overlay.Overlay o = overlays.get(i);
                    if (o instanceof Marker) {
                        ((Marker) o).closeInfoWindow(); // Cerrar etiquetas para evitar fugas de memoria
                        overlays.remove(i);
                    } else if (o instanceof Polygon || o instanceof Polyline) {
                        overlays.remove(i);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Añade un marcador a la sesión y lo dibuja.
     */
    public void addManualMarker(IGeoPoint point, String name) {
        if (point == null || !isInitialized || mapView == null) return;
        try {
            synchronized (overlayLock) {
                sessionMarkers.add(new MarkerData(point.getLatitude(), point.getLongitude(), name));
                addMarkerToView(point.getLatitude(), point.getLongitude(), name);
            }
            mapView.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        // Se elimina mapView.onDetach() de aquí para evitar cierre doble (ya se llama en el Fragmento)
        if (locationOverlay != null) {
            locationOverlay.disableMyLocation();
            locationOverlay.disableFollowLocation();
        }
    }
}
