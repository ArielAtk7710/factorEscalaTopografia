package bo.com.factorcombinadotopo;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import org.osmdroid.views.MapView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Fragmento para cálculos automáticos en tiempo real.
 * Observa el ViewModel compartido para obtener actualizaciones de ubicación.
 */
public class AutomaticFragment extends Fragment {

    private TextView txtLat, txtLon, txtAlt, txtAltOrto, txtPresion, txtPresionHpa;
    private TextView txtEste, txtNorte, txtRefSystem;
    private TextView txtFa, txtFe, txtFc, txtPresicion, txtSat, txtTemp;
    private TextView txtGeoidUndulation, txtGeoidModel;

    private SwitchMaterial switchMapa;
    private CardView cardMapa;
    private MapView miniMapView;
    private Button btnGuardarPunto;

    private MapManager miniMapManager;
    private SurveyViewModel viewModel;
    private TopoCalculoManager.TopoResult lastResult;

    private boolean isGpsCurrentlyEnabled = true;

    private final GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            if (!isAdded()) return;
            int satellitesInUse = 0;
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                if (status.usedInFix(i)) satellitesInUse++;
            }
            if (txtSat != null) txtSat.setText(String.valueOf(satellitesInUse));
        }
    };

    private final BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                checkGpsState();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_automatic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Usar ViewModel compartido de la Actividad
        viewModel = new ViewModelProvider(requireActivity()).get(SurveyViewModel.class);

        // Bindings
        txtLat = view.findViewById(R.id.txt_lat);
        txtLon = view.findViewById(R.id.txt_lon);
        txtAlt = view.findViewById(R.id.txt_alt);
        txtAltOrto = view.findViewById(R.id.txt_alt_orto);
        txtPresion = view.findViewById(R.id.txt_presion);
        txtPresionHpa = view.findViewById(R.id.txt_presion_hpa);
        txtEste = view.findViewById(R.id.txt_este);
        txtNorte = view.findViewById(R.id.txt_norte);
        txtRefSystem = view.findViewById(R.id.txt_ref_system);
        txtFa = view.findViewById(R.id.txt_fa);
        txtFe = view.findViewById(R.id.txt_fe);
        txtFc = view.findViewById(R.id.txt_fc);
        txtPresicion = view.findViewById(R.id.txt_presicion);
        txtSat = view.findViewById(R.id.txt_sat);
        txtTemp = view.findViewById(R.id.txt_temp);
        txtGeoidUndulation = view.findViewById(R.id.txt_geoid_undulation);
        txtGeoidModel = view.findViewById(R.id.txt_geoid_model);
        
        switchMapa = view.findViewById(R.id.switch_mapa);
        cardMapa = view.findViewById(R.id.card_mapa);
        miniMapView = view.findViewById(R.id.mini_map_view);
        btnGuardarPunto = view.findViewById(R.id.btn_guardar_punto_auto);

        view.findViewById(R.id.fab_mini_toggle_map_type).setOnClickListener(v -> {
            if (miniMapManager != null) {
                miniMapManager.toggleMapType();
                UIUtils.showInfoToast(requireContext(), "Mapa: " + miniMapManager.getCurrentMapModeName());
            }
        });

        view.findViewById(R.id.fab_mini_center_location).setOnClickListener(v -> {
            if (miniMapManager != null) miniMapManager.centerOnCurrentLocation();
        });

        setupMiniMap();
        setupViewModelObservers();

        btnGuardarPunto.setOnClickListener(v -> showSavePointDialog());
        switchMapa.setOnCheckedChangeListener((bv, isChecked) -> handleMapState(isChecked));
    }

    private void setupMiniMap() {
        miniMapManager = new MapManager(requireContext(), miniMapView);
        miniMapManager.setAutoCenterEnabled(true);
    }

    private void setupViewModelObservers() {
        // Observar Ubicación Raw para el mapa y la precisión
        viewModel.getRawLocation().observe(getViewLifecycleOwner(), loc -> {
            if (loc == null) return;
            if (switchMapa.isChecked()) miniMapManager.updateMyLocation(loc);
            
            String level = getPrecisionLevel(loc.getAccuracy());
            txtPresicion.setText(String.format(Locale.US, "± %.0f m - %s", loc.getAccuracy(), level));
        });

        // Observar Resultados de Cálculo
        viewModel.getCalculationResult().observe(getViewLifecycleOwner(), res -> {
            if (res != null) updateUI(res);
        });

        viewModel.getUsesMgb().observe(getViewLifecycleOwner(), uses -> {
            txtGeoidModel.setText(uses ? getString(R.string.opt_mgb) : getString(R.string.opt_egm96));
        });

        viewModel.getAmbientTemperature().observe(getViewLifecycleOwner(), temp -> {
            txtTemp.setText(String.format(Locale.getDefault(), "%.1f°C", temp));
        });

        viewModel.getErrorResult().observe(getViewLifecycleOwner(), err -> {
            if (err != null) UIUtils.showErrorToast(requireContext(), err);
        });
    }

    private void updateUI(TopoCalculoManager.TopoResult res) {
        lastResult = res;
        txtLat.setText(GeoUtils.formatLatLon(res.lat));
        txtLon.setText(GeoUtils.formatLatLon(res.lon));
        txtAlt.setText(String.format(Locale.getDefault(), "%.3f m", res.altEllipsoidal));
        txtAltOrto.setText(String.format(Locale.getDefault(), "%.3f m", res.altOrto));
        txtEste.setText(String.format(Locale.getDefault(), "%.3f", res.este));
        txtNorte.setText(String.format(Locale.getDefault(), "%.3f", res.norte));
        txtRefSystem.setText(GeoUtils.getUtmZoneFormatted(res.lat, res.lon));
        
        txtFa.setText(GeoUtils.formatFactor(res.elevationFactor));
        txtFe.setText(GeoUtils.formatFactor(res.scaleFactor));
        txtFc.setText(GeoUtils.formatFactor(res.combinedFactor));
        
        txtGeoidUndulation.setText(String.format(Locale.getDefault(), "%.2f m", res.geoidN));
        txtPresion.setText(String.format(Locale.getDefault(), "%.3f", res.pressureMmHg));
        txtPresionHpa.setText(String.format(Locale.US, "%.3f %s", res.pressureHpa, getString(R.string.unit_hpa)));
    }

    private String getPrecisionLevel(float accuracy) {
        if (accuracy < 2.0f) return getString(R.string.precision_excellent);
        if (accuracy < 5.0f) return getString(R.string.precision_good);
        if (accuracy < 10.0f) return getString(R.string.precision_medium);
        return getString(R.string.precision_low);
    }

    private void checkGpsState() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        boolean isEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isEnabled) resetUIData();
        isGpsCurrentlyEnabled = isEnabled;
    }

    @SuppressLint("MissingPermission")
    private void restartGnssCallback() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.unregisterGnssStatusCallback(gnssCallback);
            lm.registerGnssStatusCallback(gnssCallback, new Handler(Looper.getMainLooper()));
        } catch (Exception ignored) {}
    }

    private void resetUIData() {
        txtLat.setText("0"); txtLon.setText("0"); txtAlt.setText("-- m");
        txtAltOrto.setText("-- m"); txtEste.setText("0.00"); txtNorte.setText("0.00");
        txtPresicion.setText("± -- m"); txtSat.setText("0");
    }

    private void handleMapState(boolean visible) {
        cardMapa.setVisibility(visible ? View.VISIBLE : View.GONE);
        if (visible) {
            miniMapManager.onResume();
            // Centrado inmediato si ya tenemos ubicación previa
            Location loc = viewModel.getRawLocation().getValue();
            if (loc != null) {
                miniMapManager.updateMyLocation(loc);
            }
            miniMapView.invalidate();
        } else {
            miniMapManager.onPause();
        }
    }

    private void showSavePointDialog() {
        if (lastResult == null) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_gps_no_signal));
            return;
        }

        View dv = getLayoutInflater().inflate(R.layout.dialog_save_point, null);
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);

        EditText etN = dv.findViewById(R.id.et_point_name);
        EditText etObs = dv.findViewById(R.id.et_point_notes);

        dv.findViewById(R.id.btn_dialog_save).setOnClickListener(v -> {
            String name = etN.getText().toString().trim();
            if (name.isEmpty()) {
                etN.setError(getString(R.string.hint_point_name));
                return;
            }
            ejecutarGuardado(name, etObs.getText().toString());
            d.dismiss();
        });

        dv.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void ejecutarGuardado(String name, String notes) {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        ContentValues v = new ContentValues();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        v.put(DatabaseHelper.COLUMN_FECHA, time);
        v.put(DatabaseHelper.COLUMN_NOMBRE, name);
        v.put(DatabaseHelper.COLUMN_LATITUD, GeoUtils.formatLatLon(lastResult.lat));
        v.put(DatabaseHelper.COLUMN_LONGITUD, GeoUtils.formatLatLon(lastResult.lon));
        v.put(DatabaseHelper.COLUMN_ESTE, GeoUtils.formatCoord(lastResult.este));
        v.put(DatabaseHelper.COLUMN_NORTE, GeoUtils.formatCoord(lastResult.norte));
        v.put(DatabaseHelper.COLUMN_ZONA, String.valueOf(lastResult.zona));
        v.put(DatabaseHelper.COLUMN_HEMISFERIO, String.valueOf(lastResult.hemisferio));
        v.put(DatabaseHelper.COLUMN_ALTURA, GeoUtils.formatCoord(lastResult.altEllipsoidal));
        v.put(DatabaseHelper.COLUMN_ALTURA_ORTO, GeoUtils.formatCoord(lastResult.altOrto));
        v.put(DatabaseHelper.COLUMN_PRESION, GeoUtils.formatCoord(lastResult.pressureMmHg));
        v.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, GeoUtils.formatFactor(lastResult.scaleFactor));
        v.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, GeoUtils.formatFactor(lastResult.elevationFactor));
        v.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, GeoUtils.formatFactor(lastResult.combinedFactor));
        v.put(DatabaseHelper.COLUMN_MODELO_GEOIDAL, txtGeoidModel.getText().toString());
        v.put(DatabaseHelper.COLUMN_TIPO_REGISTRO, getString(R.string.label_reg_auto));
        
        String precision = txtPresicion.getText().toString();
        v.put(DatabaseHelper.COLUMN_PRECISION, precision.contains("--") ? "Ninguno" : precision);
        
        String satellites = txtSat.getText().toString();
        v.put(DatabaseHelper.COLUMN_SATELITES, (satellites.contains("--") || satellites.isEmpty()) ? "Ninguno" : satellites);
        
        String temp = txtTemp.getText().toString();
        v.put(DatabaseHelper.COLUMN_TEMPERATURA, temp.contains("--") ? "Ninguno" : temp);
        
        v.put(DatabaseHelper.COLUMN_NOTAS, notes.isEmpty() ? getString(R.string.label_no_observations) : notes);

        TopographyRepository.getInstance(requireContext()).runOnBackground(() -> {
            db.insertarPunto(v);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_saved_format, name));
                }
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        checkGpsState();
        requireContext().registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        restartGnssCallback();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (miniMapManager != null) {
            miniMapManager.onDestroy();
        }
        if (miniMapView != null) {
            miniMapView.onDetach();
        }
    }
}
