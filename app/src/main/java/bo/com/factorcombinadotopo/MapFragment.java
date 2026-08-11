package bo.com.factorcombinadotopo;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.views.MapView;

import java.util.Locale;

/**
 * Fragmento principal para visualización de mapas.
 * Utiliza el ViewModel compartido para sincronizar la posición en tiempo real.
 */
public class MapFragment extends Fragment {

    private MapView mapView;
    private MapManager mapManager;
    private TextView txtLat, txtLon, txtAlt;
    private View sepAlt;
    private LinearLayout layoutCoords;
    private SurveyViewModel viewModel;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private final BroadcastReceiver gpsStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                checkGpsStatus();
            }
        }
    };

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
            txtAlt = view.findViewById(R.id.txt_map_alt);
            sepAlt = view.findViewById(R.id.sep_map_alt);
            layoutCoords = view.findViewById(R.id.layout_map_coords);

            mapManager = new MapManager(requireContext(), mapView);
            mapManager.setAutoCenterEnabled(true); 

            view.findViewById(R.id.fab_center_location).setOnClickListener(v -> {
                if (mapManager != null) mapManager.centerOnCurrentLocation();
            });

            view.findViewById(R.id.fab_toggle_map_type).setOnClickListener(v -> {
                if (mapManager != null) {
                    mapManager.toggleMapType();
                    UIUtils.showInfoToast(requireContext(), "Modo: " + mapManager.getCurrentMapModeName());
                }
            });

            view.findViewById(R.id.fab_mark_point).setOnClickListener(v -> {
                if (mapManager != null && mapView != null) {
                    mapManager.addManualMarker(mapView.getMapCenter());
                }
            });

            setupMapListener();
            
            // Vincular con el ViewModel compartido de la Actividad
            viewModel = new ViewModelProvider(requireActivity()).get(SurveyViewModel.class);
            setupViewModelObservers();
            
            checkLocationPermissions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupViewModelObservers() {
        viewModel.getRawLocation().observe(getViewLifecycleOwner(), location -> {
            if (isAdded() && mapManager != null && location != null) {
                mapManager.updateMyLocation(location);
                if (txtAlt != null) {
                    txtAlt.setText(String.format(Locale.getDefault(), "ALT: %.1fm", location.getAltitude()));
                }
            }
        });
    }

    private void setupMapListener() {
        if (mapView == null) return;
        mapView.addMapListener(new MapListener() {
            @Override public boolean onScroll(ScrollEvent event) { updateCenterCoordinates(); return true; }
            @Override public boolean onZoom(ZoomEvent event) { updateCenterCoordinates(); return true; }
        });
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapManager != null) {
            mapManager.onResume();
            mapManager.refreshMap();
            mapManager.invalidate(); 
        }
        requireContext().registerReceiver(gpsStatusReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        checkGpsStatus();
    }

    private void checkGpsStatus() {
        if (layoutCoords == null) return;
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        
        GradientDrawable bg = (GradientDrawable) layoutCoords.getBackground();
        if (bg != null) {
            int color = isEnabled ? ContextCompat.getColor(requireContext(), R.color.flight_green) 
                                  : ContextCompat.getColor(requireContext(), R.color.flight_red);
            bg.setStroke(2, color);
        }

        // Toggle visibilidad de altitud según estado GPS
        if (txtAlt != null && sepAlt != null) {
            int vis = isEnabled ? View.VISIBLE : View.GONE;
            txtAlt.setVisibility(vis);
            sepAlt.setVisibility(vis);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapManager != null) mapManager.onPause();
        requireContext().unregisterReceiver(gpsStatusReceiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mapView != null) mapView.onDetach();
    }
}
