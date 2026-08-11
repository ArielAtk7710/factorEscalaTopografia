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
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.ContentValues;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import android.database.Cursor;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.api.IMapController;

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
    private android.location.Location lastGpsLocation;
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
                    showSavePointDialogMap();
                }
            });

            view.findViewById(R.id.fab_view_list).setOnClickListener(v -> {
                showPointSelectionDialog();
            });

            view.findViewById(R.id.fab_go_to_coords).setOnClickListener(v -> {
                showGoToCoordsDialog();
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
                this.lastGpsLocation = location;
                mapManager.updateMyLocation(location);
                if (txtAlt != null) {
                    txtAlt.setText(String.format(Locale.getDefault(), "ALT: %.1fm", location.getAltitude()));
                }
            }
        });
    }

    private void showSavePointDialogMap() {
        if (!isAdded()) return;

        View dv = getLayoutInflater().inflate(R.layout.dialog_save_point_map, null);
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);

        EditText etN = dv.findViewById(R.id.et_point_name);
        EditText etObs = dv.findViewById(R.id.et_point_notes);
        MaterialButtonToggleGroup toggleGps = dv.findViewById(R.id.toggle_gps_selection);
        View btnInfo = dv.findViewById(R.id.btn_gps_info);

        if (btnInfo != null) {
            btnInfo.setOnClickListener(v -> {
                UIUtils.showPopupInfo(requireContext(), dv, 
                        "Información de Altura", 
                        getString(R.string.msg_gps_toggle_info));
            });
        }

        dv.findViewById(R.id.btn_dialog_save).setOnClickListener(v -> {
            String name = etN.getText().toString().trim();
            if (name.isEmpty()) { etN.setError(getString(R.string.hint_point_name)); return; }

            boolean useGps = toggleGps.getCheckedButtonId() == R.id.btn_toggle_gps_on;
            ejecutarGuardadoMapa(name, etObs.getText().toString(), useGps);
            d.dismiss();
        });

        dv.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void ejecutarGuardadoMapa(String name, String notes, boolean useGps) {
        // 1. Mostrar Spin de Carga
        View progressView = getLayoutInflater().inflate(R.layout.layout_dialog_progress, null);
        TextView txtProgress = progressView.findViewById(R.id.txt_progress_label);
        if (txtProgress != null) txtProgress.setText("Guardando datos...");

        AlertDialog progressDialog = new AlertDialog.Builder(requireContext())
                .setView(progressView)
                .setCancelable(false)
                .create();
        if (progressDialog.getWindow() != null) progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();

        // 2. Obtener Coordenada del Centro (Cruz Naranja)
        IGeoPoint center = mapView.getMapCenter();
        double lat = center.getLatitude();
        double lon = center.getLongitude();

        if (useGps) {
            // MODO CON GPS: Usar altura del sensor y marcar como Copernicus
            android.location.Location targetLoc = new android.location.Location("map");
            targetLoc.setLatitude(lat);
            targetLoc.setLongitude(lon);
            targetLoc.setAltitude(lastGpsLocation != null ? lastGpsLocation.getAltitude() : 0.0);

            TopographyRepository.getInstance(requireContext()).calculateCompleteAsync(targetLoc, new TopographyRepository.CalculationCallback() {
                @Override
                public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                    persistirPuntoMapa(res, name, notes, isMgb, "Copernicus DEM GLO-90", "Métrica (Mapa)", progressDialog);
                }
                @Override public void onError(Exception e) { handleGuardadoError(e, progressDialog); }
            });

        } else {
            // MODO SIN GPS: Consultar API de Elevación
            TopographyRepository repo = TopographyRepository.getInstance(requireContext());
            repo.fetchElevationAsync(lat, lon, new TopographyRepository.ElevationCallback() {
                @Override
                public void onResult(double elevationOrto) {
                    // La API devolvió la cota. Reconstruir elipsoidal y calcular forzando MGBol08
                    repo.calculateFromOrthometricAsync(lat, lon, elevationOrto, new TopographyRepository.CalculationCallback() {
                        @Override
                        public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                            persistirPuntoMapa(res, name, notes, true, "Open-Meteo API", "Digital (DEM)", progressDialog);
                        }
                        @Override public void onError(Exception e) { handleGuardadoError(e, progressDialog); }
                    });
                }

                @Override
                public void onError(String error) {
                    // Fallback Local por error de red
                    android.location.Location targetLoc = new android.location.Location("map");
                    targetLoc.setLatitude(lat);
                    targetLoc.setLongitude(lon);
                    targetLoc.setAltitude(lastGpsLocation != null ? lastGpsLocation.getAltitude() : 0.0);
                    
                    String fallbackNotes = notes + "\n(Nota: Se guardó con ubicación GPS por falta de conexión a la red)";
                    
                    repo.calculateCompleteAsync(targetLoc, new TopographyRepository.CalculationCallback() {
                        @Override
                        public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                            persistirPuntoMapa(res, name, fallbackNotes, isMgb, "GPS Dispositivo (Fallback)", "Métrica (Offline)", progressDialog);
                        }
                        @Override public void onError(Exception e) { handleGuardadoError(e, progressDialog); }
                    });
                }
            });
        }
    }

    private void persistirPuntoMapa(TopoCalculoManager.TopoResult res, String name, String notes, boolean isMgb, String dem, String prec, AlertDialog dialog) {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        ContentValues v = new ContentValues();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        v.put(DatabaseHelper.COLUMN_FECHA, time);
        v.put(DatabaseHelper.COLUMN_NOMBRE, name);
        v.put(DatabaseHelper.COLUMN_LATITUD, GeoUtils.formatLatLon(res.lat));
        v.put(DatabaseHelper.COLUMN_LONGITUD, GeoUtils.formatLatLon(res.lon));
        v.put(DatabaseHelper.COLUMN_ESTE, GeoUtils.formatCoord(res.este));
        v.put(DatabaseHelper.COLUMN_NORTE, GeoUtils.formatCoord(res.norte));
        v.put(DatabaseHelper.COLUMN_ZONA, String.valueOf(res.zona));
        v.put(DatabaseHelper.COLUMN_HEMISFERIO, String.valueOf(res.hemisferio));
        v.put(DatabaseHelper.COLUMN_ALTURA, GeoUtils.formatCoord(res.altEllipsoidal));
        v.put(DatabaseHelper.COLUMN_ALTURA_ORTO, GeoUtils.formatCoord(res.altOrto));
        v.put(DatabaseHelper.COLUMN_PRESION, GeoUtils.formatCoord(res.pressureMmHg));
        v.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, GeoUtils.formatFactor(res.scaleFactor));
        v.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, GeoUtils.formatFactor(res.elevationFactor));
        v.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, GeoUtils.formatFactor(res.combinedFactor));
        
        // Forzar MGBol08 si es modo API o si el repo lo detectó
        v.put(DatabaseHelper.COLUMN_MODELO_GEOIDAL, isMgb ? "MGBol08" : "EGM96 (Global)");
        v.put(DatabaseHelper.COLUMN_MODELO_DEM, dem);
        v.put(DatabaseHelper.COLUMN_TIPO_REGISTRO, getString(R.string.label_reg_map));
        v.put(DatabaseHelper.COLUMN_PRECISION, prec);
        v.put(DatabaseHelper.COLUMN_SATELITES, "Ninguno");
        v.put(DatabaseHelper.COLUMN_TEMPERATURA, String.format(Locale.getDefault(), "%.1f°C", TopographyRepository.getInstance(requireContext()).getCurrentAmbientTemp()));
        v.put(DatabaseHelper.COLUMN_NOTAS, notes.isEmpty() ? getString(R.string.label_no_observations) : notes);

        db.insertarPunto(v);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            dialog.dismiss();
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_saved_format, name));
        }, 500);
    }

    private void handleGuardadoError(Exception e, AlertDialog dialog) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (dialog != null) dialog.dismiss();
            UIUtils.showErrorToast(requireContext(), "Error técnico: " + e.getMessage());
        });
    }

    private void showPointSelectionDialog() {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        Cursor cursor = db.obtenerPuntos();
        if (cursor == null) return;

        List<PointRef> allPoints = new ArrayList<>();
        while (cursor.moveToNext()) {
            PointRef p = new PointRef();
            p.nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE));
            try {
                p.lat = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUD)));
                p.lon = Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUD)));
                allPoints.add(p);
            } catch (Exception ignored) {}
        }
        cursor.close();

        if (allPoints.isEmpty()) {
            UIUtils.showInfoToast(requireContext(), "No hay puntos registrados");
            return;
        }

        String[] names = new String[allPoints.size()];
        boolean[] checked = new boolean[allPoints.size()];
        for (int i = 0; i < allPoints.size(); i++) names[i] = allPoints.get(i).nombre;

        new AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar puntos para ver")
            .setMultiChoiceItems(names, checked, (dialog, which, isChecked) -> checked[which] = isChecked)
            .setPositiveButton("Mostrar", (dialog, which) -> {
                if (mapManager != null) {
                    mapManager.clearManualMarkers();
                    for (int i = 0; i < checked.length; i++) {
                        if (checked[i]) {
                            PointRef p = allPoints.get(i);
                            mapManager.addManualMarker(new GeoPoint(p.lat, p.lon), p.nombre);
                        }
                    }
                }
            })
            .setNegativeButton("Cerrar", null)
            .setNeutralButton("Limpiar Todo", (dialog, which) -> {
                if (mapManager != null) mapManager.clearManualMarkers();
            })
            .show();
    }

    private void showGoToCoordsDialog() {
        if (!isAdded()) return;

        View dv = getLayoutInflater().inflate(R.layout.dialog_go_to_coords, null);
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);

        EditText etLat = dv.findViewById(R.id.et_go_lat);
        EditText etLon = dv.findViewById(R.id.et_go_lon);

        dv.findViewById(R.id.btn_go_to_pos).setOnClickListener(v -> {
            String latStr = etLat.getText().toString().trim();
            String lonStr = etLon.getText().toString().trim();

            if (latStr.isEmpty() || lonStr.isEmpty()) {
                UIUtils.showWarningToast(requireContext(), "Ingrese coordenadas válidas");
                return;
            }

            try {
                double lat = Double.parseDouble(latStr);
                double lon = Double.parseDouble(lonStr);

                if (mapView != null) {
                    mapView.getController().animateTo(new GeoPoint(lat, lon));
                    UIUtils.showInfoToast(requireContext(), "Navegando a posición...");
                }
                d.dismiss();
            } catch (Exception e) {
                UIUtils.showErrorToast(requireContext(), "Formato numérico inválido");
            }
        });

        dv.findViewById(R.id.btn_go_cancel).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private static class PointRef {
        String nombre;
        double lat, lon;
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
