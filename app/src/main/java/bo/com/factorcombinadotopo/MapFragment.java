package bo.com.factorcombinadotopo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;

import java.util.Locale;

/**
 * Fragmento principal para visualización de mapas usando OpenStreetMap.
 * Incluye mira central naranja y visualización de coordenadas en tiempo real.
 */
public class MapFragment extends Fragment implements LocationHelper.LocationUpdateListener {

    private MapView mapView;
    private MapManager mapManager;
    private LocationHelper locationHelper;
    private TextView txtLat, txtLon;
    private static final int PERMISSION_REQUEST_CODE = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            mapView = view.findViewById(R.id.map_view);
            txtLat = view.findViewById(R.id.txt_map_lat);
            txtLon = view.findViewById(R.id.txt_map_lon);

            mapManager = new MapManager(requireContext(), mapView);
            // Activamos el centrado automático inicial (se ejecutará solo la 1ra vez)
            mapManager.setAutoCenterEnabled(true); 

            locationHelper = new LocationHelper(requireContext(), this);

            view.findViewById(R.id.fab_center_location).setOnClickListener(v -> {
                if (mapManager != null) mapManager.centerOnCurrentLocation();
            });

            view.findViewById(R.id.fab_toggle_map_type).setOnClickListener(v -> {
                if (mapManager != null) {
                    mapManager.toggleMapType();
                    UIUtils.showInfoToast(requireContext(), "Modo: " + mapManager.getCurrentMapModeName());
                }
            });

            setupMapListener();
            checkLocationPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMapListener() {
        if (mapView == null) return;

        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                updateCenterCoordinates();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                updateCenterCoordinates();
                return true;
            }
        });

        // Inicializar con el centro actual
        updateCenterCoordinates();
    }

    private void updateCenterCoordinates() {
        if (mapView == null || txtLat == null || txtLon == null) return;
        
        IGeoPoint center = mapView.getMapCenter();
        txtLat.setText(String.format(Locale.getDefault(), "LAT: %.6f", center.getLatitude()));
        txtLon.setText(String.format(Locale.getDefault(), "LON: %.6f", center.getLongitude()));
    }

    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            locationHelper.startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.startLocationUpdates();
            } else {
                UIUtils.showWarningToast(requireContext(), getString(R.string.warn_location_denied));
            }
        }
    }

    @Override
    public void onLocationUpdated(Location location) {
        if (isAdded() && mapManager != null) {
            // Actualizamos el marcador pero NO centramos (la lógica interna de updateMyLocation respetará autoCenterEnabled)
            mapManager.updateMyLocation(location);
        }
    }

    private void handleMapState(boolean active) {
        if (!isAdded() || mapManager == null) return;
        try {
            if (active) {
                mapManager.onResume();
            } else {
                mapManager.onPause();
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void onResume() {
        super.onResume();
        handleMapState(true);
        if (mapManager != null) {
            mapManager.invalidate(); // Forzar renderizado de mosaicos
        }
        if (locationHelper != null) locationHelper.getLastLocation();
    }

    @Override
    public void onPause() {
        super.onPause();
        handleMapState(false);
        if (locationHelper != null) locationHelper.stopLocationUpdates();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) {
            mapView.onDetach();
        }
    }
}
