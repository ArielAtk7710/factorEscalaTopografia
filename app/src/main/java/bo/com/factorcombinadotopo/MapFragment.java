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
import android.database.sqlite.SQLiteDatabase;
import android.widget.ImageView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import android.graphics.drawable.Drawable;
import androidx.core.graphics.drawable.DrawableCompat;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Polyline;

import java.util.Locale;

/**
 * Fragmento principal para visualización de mapas.
 * Utiliza el ViewModel compartido para sincronizar la posición en tiempo real.
 */
public class MapFragment extends Fragment {

    private MapView mapView;
    private MapManager mapManager;
    private TextView txtLat, txtLon;
    private LinearLayout layoutCoords;
    private SurveyViewModel viewModel;
    private android.location.Location lastGpsLocation;
    private long lastClickTime = 0; // Para lógica Debounce
    private static final int PERMISSION_REQUEST_CODE = 200;
    private static boolean hasCenteredOnce = false; // Memoria de centrado inicial único
    private AlertDialog activeProgressDialog;

    // Lógica de polígonos y distancias
    private boolean isDrawingArea = false;
    private boolean isMeasuringDistance = false;
    private final List<GeoPoint> polygonPoints = new ArrayList<>();
    private final List<GeoPoint> distancePoints = new ArrayList<>();
    private final List<String> segmentDistances = new ArrayList<>();
    private Polyline drawingPreview;
    private View cardPolygonControls;
    private TextView txtMeasurementTitle;
    private final List<Marker> tempVertexMarkers = new ArrayList<>();

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
            layoutCoords = view.findViewById(R.id.layout_map_coords);

            mapManager = new MapManager(requireContext(), mapView);
            mapManager.setAutoCenterEnabled(true); 
            mapManager.setOnMarkerClickListener(this::onMarkerClickInternal);

            view.findViewById(R.id.fab_center_location).setOnClickListener(v -> {
                if (mapManager != null) mapManager.centerOnCurrentLocation();
            });

            view.findViewById(R.id.fab_toggle_map_type).setOnClickListener(v -> {
                if (mapManager != null) {
                    mapManager.toggleMapType();
                    UIUtils.showInfoToast(requireContext(), getString(R.string.label_map_prefix) + mapManager.getCurrentMapModeName());
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

            view.findViewById(R.id.fab_clear_map).setOnClickListener(v -> {
                if (mapManager != null) {
                    mapManager.clearManualMarkers();
                    cancelDrawing();
                    UIUtils.showInfoToast(requireContext(), "Mapa visualmente limpio. Los registros permanecen seguros.");
                }
            });

            // Lógica de Medición de Áreas y Distancias
            cardPolygonControls = view.findViewById(R.id.card_polygon_controls);
            txtMeasurementTitle = view.findViewById(R.id.txt_measurement_title);
            view.findViewById(R.id.fab_draw_polygon).setOnClickListener(v -> startDrawingMode());
            view.findViewById(R.id.fab_measure_distance).setOnClickListener(v -> startDistanceMode());
            view.findViewById(R.id.btn_cancel_polygon).setOnClickListener(v -> cancelDrawing());
            view.findViewById(R.id.btn_finish_polygon).setOnClickListener(v -> finishMeasurement());

            // Hacer que el panel superior de coordenadas también actúe como botón de búsqueda
            if (layoutCoords != null) {
                layoutCoords.setOnClickListener(v -> showGoToCoordsDialog());
            }

            setupMapListener();
            setupMapEvents();
            
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
                
                // Centrado automático solo la primera vez por sesión
                if (!hasCenteredOnce) {
                    mapManager.centerToLocation(location);
                    hasCenteredOnce = true;
                }
                
                mapManager.updateMyLocation(location);
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
                        getString(R.string.label_height_info), 
                        getString(R.string.msg_gps_toggle_info));
            });
        }

        dv.findViewById(R.id.btn_dialog_save).setOnClickListener(v -> {
            // 🛡️ Debounce: Evitar múltiples guardados por clics rápidos
            if (android.os.SystemClock.elapsedRealtime() - lastClickTime < 1000) return;
            lastClickTime = android.os.SystemClock.elapsedRealtime();

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
        if (!isAdded() || getContext() == null) return;
        Context context = getContext();

        // 1. Mostrar Spin de Carga
        View progressView = getLayoutInflater().inflate(R.layout.layout_dialog_progress, null);
        TextView txtProgress = progressView.findViewById(R.id.txt_progress_label);
        if (txtProgress != null) txtProgress.setText(getString(R.string.msg_saving_data));

        activeProgressDialog = new AlertDialog.Builder(context)
                .setView(progressView)
                .setCancelable(false)
                .create();
        if (activeProgressDialog.getWindow() != null) activeProgressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        activeProgressDialog.show();

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

            TopographyRepository.getInstance(context).calculateCompleteAsync(targetLoc, new TopographyRepository.CalculationCallback() {
                @Override
                public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                    if (!isAdded()) {
                        if (activeProgressDialog != null && activeProgressDialog.isShowing()) activeProgressDialog.dismiss();
                        return;
                    }
                    persistirPuntoMapa(res, name, notes, isMgb, "Altura GPS dispositivo", "Métrica (Mapa)", activeProgressDialog);
                }
                @Override public void onError(Exception e) { handleGuardadoError(e, activeProgressDialog); }
            });

        } else {
            // MODO SIN GPS: Consultar API de Elevación
            TopographyRepository repo = TopographyRepository.getInstance(context);
            repo.fetchElevationAsync(lat, lon, new TopographyRepository.ElevationCallback() {
                @Override
                public void onResult(double elevationOrto) {
                    if (!isAdded()) {
                        if (activeProgressDialog != null && activeProgressDialog.isShowing()) activeProgressDialog.dismiss();
                        return;
                    }
                    // La API devolvió la cota. Reconstruir elipsoidal y calcular forzando MGBol08
                    repo.calculateFromOrthometricAsync(lat, lon, elevationOrto, new TopographyRepository.CalculationCallback() {
                        @Override
                        public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                            if (!isAdded()) {
                                if (activeProgressDialog != null && activeProgressDialog.isShowing()) activeProgressDialog.dismiss();
                                return;
                            }
                            persistirPuntoMapa(res, name, notes, true, "GLO-90, Copernicus", "Digital (DEM)", activeProgressDialog);
                        }
                        @Override public void onError(Exception e) { handleGuardadoError(e, activeProgressDialog); }
                    });
                }

                @Override
                public void onError(String error) {
                    if (!isAdded()) {
                        if (activeProgressDialog != null && activeProgressDialog.isShowing()) activeProgressDialog.dismiss();
                        return;
                    }
                    
                    // Informar al usuario del respaldo
                    UIUtils.showWarningToast(requireContext(), getString(R.string.msg_offline_warning));
                    
                    // Fallback Local por error de red
                    android.location.Location targetLoc = new android.location.Location("map");
                    targetLoc.setLatitude(lat);
                    targetLoc.setLongitude(lon);
                    targetLoc.setAltitude(lastGpsLocation != null ? lastGpsLocation.getAltitude() : 0.0);
                    
                    String fallbackNotes = notes + "\n(Nota: Se guardó con ubicación GPS por falta de conexión a la red)";
                    
                    repo.calculateCompleteAsync(targetLoc, new TopographyRepository.CalculationCallback() {
                        @Override
                        public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                            if (!isAdded()) {
                                if (activeProgressDialog != null && activeProgressDialog.isShowing()) activeProgressDialog.dismiss();
                                return;
                            }
                            persistirPuntoMapa(res, name, fallbackNotes, isMgb, "Altura GPS dispositivo (Sin Red)", "Métrica (Offline)", activeProgressDialog);
                        }
                        @Override public void onError(Exception e) { handleGuardadoError(e, activeProgressDialog); }
                    });
                }
            });
        }
    }

    private void persistirPuntoMapa(TopoCalculoManager.TopoResult res, String name, String notes, boolean isMgb, String dem, String prec, AlertDialog dialog) {
        if (!isAdded() || getContext() == null) {
            if (dialog != null && dialog.isShowing()) dialog.dismiss();
            return;
        }
        Context context = getContext();
        DatabaseHelper db = DatabaseHelper.getInstance(context);
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
        v.put(DatabaseHelper.COLUMN_TEMPERATURA, String.format(Locale.getDefault(), "%.1f°C", TopographyRepository.getInstance(context).getCurrentAmbientTemp()));
        v.put(DatabaseHelper.COLUMN_NOTAS, notes.isEmpty() ? getString(R.string.label_no_observations) : notes);

        TopographyRepository.getInstance(context).runOnBackground(() -> {
            db.insertarPunto(v);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) {
                    if (activeProgressDialog != null && activeProgressDialog.isShowing()) {
                        activeProgressDialog.dismiss();
                    }
                    UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_saved_format, name));
                    if (mapManager != null) {
                        mapManager.addManualMarker(new GeoPoint(res.lat, res.lon), name);
                    }
                }
                activeProgressDialog = null;
            }, 500);
        });
    }

    private void handleGuardadoError(Exception e, AlertDialog dialog) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (isAdded() && dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                UIUtils.showErrorToast(requireContext(), getString(R.string.err_technical_prefix) + e.getMessage());
            }
        });
    }

    private void showPointSelectionDialog() {
        if (!isAdded() || getContext() == null) return;
        Context context = getContext();

        new Thread(() -> {
            DatabaseHelper db = DatabaseHelper.getInstance(context);
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

            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                if (allPoints.isEmpty()) {
                    UIUtils.showInfoToast(requireContext(), getString(R.string.msg_no_points_registered));
                    return;
                }

                // 🎨 NUEVO DISEÑO PREMIUM TRANSPARENTE
                View dv = getLayoutInflater().inflate(R.layout.layout_dialog_point_selection, null);
                AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
                AlertDialog d = b.create();
                if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                d.setView(dv);

                androidx.recyclerview.widget.RecyclerView rv = dv.findViewById(R.id.rv_point_selection);
                rv.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
                
                boolean[] selected = new boolean[allPoints.size()];
                PointSelectionAdapter adapter = new PointSelectionAdapter(allPoints, selected);
                rv.setAdapter(adapter);

                dv.findViewById(R.id.btn_selection_show).setOnClickListener(v -> {
                    if (mapManager != null) {
                        mapManager.clearManualMarkers();
                        for (int i = 0; i < selected.length; i++) {
                            if (selected[i]) {
                                PointRef p = allPoints.get(i);
                                mapManager.addManualMarker(new GeoPoint(p.lat, p.lon), p.nombre);
                            }
                        }
                    }
                    d.dismiss();
                });

                dv.findViewById(R.id.btn_selection_clear_all).setOnClickListener(v -> {
                    if (mapManager != null) mapManager.clearManualMarkers();
                    d.dismiss();
                });

                dv.findViewById(R.id.btn_selection_close).setOnClickListener(v -> d.dismiss());
                
                d.show();
            });
        }).start();
    }

    /**
     * Adaptador interno para la selección de puntos con estilo Premium.
     */
    private static class PointSelectionAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<PointSelectionAdapter.ViewHolder> {
        private final List<PointRef> points;
        private final boolean[] selected;

        PointSelectionAdapter(List<PointRef> points, boolean[] selected) {
            this.points = points;
            this.selected = selected;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_point_selection, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            PointRef p = points.get(position);
            holder.cb.setText(p.nombre);
            holder.cb.setChecked(selected[position]);
            holder.cb.setOnCheckedChangeListener((bv, isChecked) -> selected[position] = isChecked);
        }

        @Override
        public int getItemCount() { return points.size(); }

        static class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.CheckBox cb;
            ViewHolder(View v) { super(v); cb = (android.widget.CheckBox) v; }
        }
    }

    private void onMarkerClickInternal(String title) {
        if (!isAdded()) return;
        
        new Thread(() -> {
            DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c = db.query(DatabaseHelper.TABLE_PUNTOS, null, 
                    DatabaseHelper.COLUMN_NOMBRE + " = ?", new String[]{title}, 
                    null, null, null, "1");
            
            if (c != null && c.moveToFirst()) {
                ContentValues v = new ContentValues();
                // Extraer todos los campos necesarios para la UI con safe defaults
                v.put("name", title);
                v.put("lat", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUD)));
                v.put("lon", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUD)));
                v.put("alt_e", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALTURA)));
                v.put("alt_o", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALTURA_ORTO)));
                v.put("este", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTE)));
                v.put("norte", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NORTE)));
                
                String zona = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONA));
                String hem = c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HEMISFERIO));
                v.put("zona", (zona != null ? zona : "") + " " + (hem != null ? hem : ""));
                
                v.put("fe", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_ESCALA)));
                v.put("fa", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_ALTURA)));
                v.put("fc", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_COMBINADO)));
                v.put("geoid", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODELO_GEOIDAL)));
                v.put("type", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO_REGISTRO)));
                v.put("date", c.getString(c.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA)));
                c.close();
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) showPointDetailsDialog(v);
                });
            } else {
                if (c != null) c.close();
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) UIUtils.showInfoToast(requireContext(), getString(R.string.msg_point_details_unavailable));
                });
            }
        }).start();
    }

    private void showPointDetailsDialog(ContentValues p) {
        if (!isAdded()) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_dialog_point_details, null);
        dialog.setContentView(view);

        ((TextView)view.findViewById(R.id.txt_det_point_name)).setText(p.getAsString("name"));

        setDetailRow(view.findViewById(R.id.row_lat), "Latitud:", p.getAsString("lat"), R.drawable.ic_visibility_pro);
        setDetailRow(view.findViewById(R.id.row_lon), "Longitud:", p.getAsString("lon"), R.drawable.ic_visibility_pro);
        setDetailRow(view.findViewById(R.id.row_alt_ellip), "Alt. Elipsoidal:", p.getAsString("alt_e") + " m", R.drawable.ic_precision);
        setDetailRow(view.findViewById(R.id.row_alt_orto), "Alt. Ortométrica:", p.getAsString("alt_o") + " m", R.drawable.ic_precision);
        
        setDetailRow(view.findViewById(R.id.row_este), "Este (X):", p.getAsString("este") + " m", R.drawable.ic_manual);
        setDetailRow(view.findViewById(R.id.row_norte), "Norte (Y):", p.getAsString("norte") + " m", R.drawable.ic_manual);
        setDetailRow(view.findViewById(R.id.row_zona), "Zona / Hemisferio:", p.getAsString("zona"), R.drawable.ic_info);
        
        setDetailRow(view.findViewById(R.id.row_fe), "Factor Escala (k):", p.getAsString("fe"), R.drawable.ic_auto);
        setDetailRow(view.findViewById(R.id.row_fa), "Factor Altura (ha):", p.getAsString("fa"), R.drawable.ic_auto);
        setDetailRow(view.findViewById(R.id.row_fc), "Factor Combinado (K):", p.getAsString("fc"), R.drawable.ic_auto);
        
        setDetailRow(view.findViewById(R.id.row_geoid), "Modelo Geoidal:", p.getAsString("geoid"), R.drawable.ic_shield_pro);
        setDetailRow(view.findViewById(R.id.row_type), "Tipo Registro:", p.getAsString("type"), R.drawable.ic_register);
        setDetailRow(view.findViewById(R.id.row_date), "Fecha:", p.getAsString("date"), R.drawable.ic_calendar);

        view.findViewById(R.id.btn_det_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setDetailRow(View row, String label, String value, int iconRes) {
        if (row == null) return;
        TextView txtLabel = row.findViewById(R.id.txt_detail_label);
        TextView txtValue = row.findViewById(R.id.txt_detail_value);
        ImageView imgIcon = row.findViewById(R.id.img_detail_icon);
        
        if (txtLabel != null) txtLabel.setText(label);
        if (txtValue != null) {
            txtValue.setText(value);
            txtValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.accent_light));
        }
        if (imgIcon != null) imgIcon.setImageResource(iconRes);
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
                    mapView.getController().setZoom(18.5);
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

    private void setupMapEvents() {
        MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                if (isDrawingArea) {
                    addPointToPolygon(p);
                    return true;
                }
                if (isMeasuringDistance) {
                    addPointToDistancePath(p);
                    return true;
                }
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) { return false; }
        };

        MapEventsOverlay eventsOverlay = new MapEventsOverlay(mReceive);
        mapView.getOverlays().add(0, eventsOverlay); // Al fondo para no tapar otros overlays
    }

    private void startDrawingMode() {
        cancelDrawing(); // Limpiar estados previos
        isDrawingArea = true;
        polygonPoints.clear();
        if (cardPolygonControls != null) cardPolygonControls.setVisibility(View.VISIBLE);
        if (txtMeasurementTitle != null) txtMeasurementTitle.setText("MEDICIÓN DE ÁREA (POLÍGONO)");
        UIUtils.showInfoToast(requireContext(), "Toque el mapa para añadir vértices de área");
        
        if (drawingPreview == null) {
            drawingPreview = new Polyline(mapView);
            drawingPreview.getOutlinePaint().setColor(0xFF00BFFF);
            drawingPreview.getOutlinePaint().setStrokeWidth(4.0f);
        } else {
            drawingPreview.setPoints(new ArrayList<>());
        }
        mapView.getOverlays().add(drawingPreview);
    }

    private void startDistanceMode() {
        cancelDrawing(); // Limpiar estados previos
        isMeasuringDistance = true;
        distancePoints.clear();
        segmentDistances.clear();
        if (cardPolygonControls != null) cardPolygonControls.setVisibility(View.VISIBLE);
        if (txtMeasurementTitle != null) txtMeasurementTitle.setText("MEDICIÓN DE DISTANCIA (REGLA)");
        UIUtils.showInfoToast(requireContext(), "Toque el mapa para medir distancias");

        if (drawingPreview == null) {
            drawingPreview = new Polyline(mapView);
            drawingPreview.getOutlinePaint().setColor(ContextCompat.getColor(requireContext(), R.color.accent_red_soft));
            drawingPreview.getOutlinePaint().setStrokeWidth(5.0f);
        } else {
            drawingPreview.setPoints(new ArrayList<>());
            drawingPreview.getOutlinePaint().setColor(ContextCompat.getColor(requireContext(), R.color.accent_red_soft));
        }
        mapView.getOverlays().add(drawingPreview);
    }

    private void addPointToPolygon(GeoPoint p) {
        polygonPoints.add(p);
        drawingPreview.addPoint(p);
        
        // Marcador visual para el vértice usando el icono de aguja en azul
        Marker v = new Marker(mapView);
        v.setPosition(p);
        v.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Punta de la aguja
        
        Drawable d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_needle_pin);
        if (d != null) {
            d = DrawableCompat.wrap(d).mutate();
            DrawableCompat.setTint(d, ContextCompat.getColor(requireContext(), R.color.color_blue_intense));
            v.setIcon(d);
        }
        
        v.setTitle("Vértice " + polygonPoints.size());
        v.setInfoWindow(null);
        
        mapView.getOverlays().add(v);
        tempVertexMarkers.add(v);
        mapView.invalidate();
    }

    private void addPointToDistancePath(GeoPoint p) {
        if (!distancePoints.isEmpty()) {
            GeoPoint last = distancePoints.get(distancePoints.size() - 1);
            double dist = last.distanceToAsDouble(p);
            segmentDistances.add(GeoUtils.formatDistance(dist));
        }
        
        distancePoints.add(p);
        drawingPreview.addPoint(p);

        // Marcador visual para el vértice usando el icono de aguja en rojo suave
        Marker v = new Marker(mapView);
        v.setPosition(p);
        v.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM); // Punta de la aguja
        
        Drawable d = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_needle_pin);
        if (d != null) {
            d = DrawableCompat.wrap(d).mutate();
            DrawableCompat.setTint(d, ContextCompat.getColor(requireContext(), R.color.accent_red_soft));
            v.setIcon(d);
        }
        
        v.setTitle("Punto " + distancePoints.size());
        v.setInfoWindow(null);
        
        mapView.getOverlays().add(v);
        tempVertexMarkers.add(v);
        mapView.invalidate();
    }

    private void cancelDrawing() {
        isDrawingArea = false;
        isMeasuringDistance = false;
        polygonPoints.clear();
        distancePoints.clear();
        segmentDistances.clear();
        if (cardPolygonControls != null) cardPolygonControls.setVisibility(View.GONE);
        if (txtMeasurementTitle != null) txtMeasurementTitle.setText("MEDICIÓN");
        
        if (drawingPreview != null) {
            mapView.getOverlays().remove(drawingPreview);
            drawingPreview = null;
        }
        
        for (Marker m : tempVertexMarkers) {
            mapView.getOverlays().remove(m);
        }
        tempVertexMarkers.clear();
        mapView.invalidate();
    }

    private void finishMeasurement() {
        if (isDrawingArea) {
            finishDrawingArea();
        } else if (isMeasuringDistance) {
            finishDistanceMeasurement();
        }
    }

    private void finishDrawingArea() {
        if (polygonPoints.size() < 3) {
            UIUtils.showWarningToast(requireContext(), "Se requieren al menos 3 puntos");
            return;
        }

        double areaM2 = GeoUtils.calculateArea(polygonPoints);
        String areaText = GeoUtils.formatArea(areaM2);

        if (mapManager != null) {
            mapManager.addPolygon(new ArrayList<>(polygonPoints), areaText);
        }

        cancelDrawing(); 
        UIUtils.showSuccessToast(requireContext(), "Área medida: " + areaText);
    }

    private void finishDistanceMeasurement() {
        if (distancePoints.size() < 2) {
            UIUtils.showWarningToast(requireContext(), "Se requieren al menos 2 puntos");
            return;
        }

        double totalDist = 0;
        for (int i = 0; i < distancePoints.size() - 1; i++) {
            totalDist += distancePoints.get(i).distanceToAsDouble(distancePoints.get(i + 1));
        }
        String totalText = GeoUtils.formatDistance(totalDist);

        if (mapManager != null) {
            mapManager.addDistancePath(new ArrayList<>(distancePoints), new ArrayList<>(segmentDistances), totalText);
        }

        cancelDrawing();
        UIUtils.showSuccessToast(requireContext(), "Distancia Total: " + totalText);
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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapManager != null) mapManager.onPause();
        requireContext().unregisterReceiver(gpsStatusReceiver);
    }

    @Override
    public void onDestroyView() {
        if (activeProgressDialog != null && activeProgressDialog.isShowing()) {
            activeProgressDialog.dismiss();
        }
        activeProgressDialog = null;
        super.onDestroyView();
        if (mapManager != null) mapManager.onDestroy();
        if (mapView != null) mapView.onDetach();
    }
}
