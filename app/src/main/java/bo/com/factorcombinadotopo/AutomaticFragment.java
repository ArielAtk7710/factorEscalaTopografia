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
import android.widget.ImageView;
import com.google.android.material.button.MaterialButton;
import android.widget.EditText;
import android.widget.TextView;
import android.content.res.ColorStateList;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento para cálculos automáticos en tiempo real.
 * Observa el ViewModel compartido para obtener actualizaciones de ubicación.
 */
public class AutomaticFragment extends Fragment {

    private TextView txtLat, txtLon, txtAlt, txtAltOrto, txtPresion, txtPresionHpa;
    private TextView txtEste, txtNorte, txtRefSystem;
    private TextView txtFa, txtFe, txtFc, txtPrecision, txtSat, txtTemp;
    private TextView txtGeoidUndulation, txtGeoidModel;

    // Campos de Auditoría GNSS
    private TextView txtLatRaw, txtLonRaw, txtLatFiltered, txtLonFiltered, txtGnssDelta;
    private MaterialButton btnResetGnss;
    private View btnGnssInfo, layoutPrecisionContainer;
    private boolean isStaticModeActive = false;

    private View layoutSatContainer;
    private Button btnGuardarPunto;

    private SurveyViewModel viewModel;
    private TopoCalculoManager.TopoResult lastResult;
    private GnssStatus lastGnssStatus;

    private boolean isGpsCurrentlyEnabled = true;

    private final GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            if (!isAdded()) return;
            lastGnssStatus = status;
            int satellitesInUse = 0;
            int satelliteCount = status.getSatelliteCount();
            for (int i = 0; i < satelliteCount; i++) {
                if (status.usedInFix(i)) satellitesInUse++;
            }
        // Actualizar UI del satélite en uso inmediatamente
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
        txtPrecision = view.findViewById(R.id.txt_precision);
        txtSat = view.findViewById(R.id.txt_sat);
        txtTemp = view.findViewById(R.id.txt_temp);
        txtGeoidUndulation = view.findViewById(R.id.txt_geoid_undulation);
        txtGeoidModel = view.findViewById(R.id.txt_geoid_model);

        // Bindings Auditoría
        txtLatRaw = view.findViewById(R.id.txt_lat_raw);
        txtLonRaw = view.findViewById(R.id.txt_lon_raw);
        txtLatFiltered = view.findViewById(R.id.txt_lat_filtered);
        txtLonFiltered = view.findViewById(R.id.txt_lon_filtered);
        txtGnssDelta = view.findViewById(R.id.txt_gnss_delta);
        btnResetGnss = view.findViewById(R.id.btn_reset_gnss);
        btnGnssInfo = view.findViewById(R.id.btn_gnss_info);
        layoutPrecisionContainer = view.findViewById(R.id.layout_precision_container);
        
        layoutSatContainer = view.findViewById(R.id.layout_sat_container);
        btnGuardarPunto = view.findViewById(R.id.btn_guardar_punto_auto);

        setupViewModelObservers();

        btnGuardarPunto.setOnClickListener(v -> showSavePointDialog());
        
        if (btnResetGnss != null) {
            btnResetGnss.setOnClickListener(v -> {
                isStaticModeActive = !isStaticModeActive;
                viewModel.startStaticMeasurement();
                
                if (isStaticModeActive) {
                    btnResetGnss.setText("ESTÁTICO ACTIVO");
                    btnResetGnss.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.state_success)));
                    UIUtils.showSuccessToast(requireContext(), "Iniciando promedio estático...");
                } else {
                    btnResetGnss.setText("INICIAR ESTÁTICO");
                    btnResetGnss.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.state_error)));
                    UIUtils.showInfoToast(requireContext(), "Modo estático desactivado.");
                }
            });
        }

        if (btnGnssInfo != null) {
            btnGnssInfo.setOnClickListener(v -> showStaticModeHelpDialog());
        }

        if (layoutPrecisionContainer != null) {
            layoutPrecisionContainer.setOnClickListener(v -> showPrecisionDetailsDialog());
        }

        if (layoutSatContainer != null) {
            layoutSatContainer.setOnClickListener(v -> showSatelliteDetailsDialog());
        }
    }

    private void setupViewModelObservers() {
        // Observar Ubicación Raw para la precisión
        viewModel.getRawLocation().observe(getViewLifecycleOwner(), loc -> {
            if (loc == null) return;
            String level = getPrecisionLevel(loc.getAccuracy());
            txtPrecision.setText(String.format(Locale.US, "± %.0f m - %s", loc.getAccuracy(), level));
            
            // Actualizar campos RAW en Auditoría
            if (txtLatRaw != null) txtLatRaw.setText(String.format(Locale.US, "%.7f", loc.getLatitude()));
            if (txtLonRaw != null) txtLonRaw.setText(String.format(Locale.US, "%.7f", loc.getLongitude()));
        });

        // Observar Ubicación Filtrada
        viewModel.getFilteredLocation().observe(getViewLifecycleOwner(), gnss -> {
            if (gnss == null) return;
            if (txtLatFiltered != null) txtLatFiltered.setText(String.format(Locale.US, "%.7f", gnss.latitude));
            if (txtLonFiltered != null) txtLonFiltered.setText(String.format(Locale.US, "%.7f", gnss.longitude));
            
            // Calcular Delta de posición respecto al raw actual
            Location raw = viewModel.getRawLocation().getValue();
            if (raw != null && txtGnssDelta != null) {
                float[] results = new float[1];
                Location.distanceBetween(raw.getLatitude(), raw.getLongitude(), gnss.latitude, gnss.longitude, results);
                txtGnssDelta.setText(String.format(Locale.US, "%.3f m", results[0]));
            }
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
            if (err != null) {
                // Solo mostrar errores si NO son de clima para no saturar la vista Automática.
                // Los errores de clima se mostrarán en su propio fragmento.
                if (!err.contains(getString(R.string.msg_weather_offline)) && !err.contains("clima")) {
                    UIUtils.showErrorToast(requireContext(), err);
                }
            }
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
        boolean isEnabled = lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
        txtPrecision.setText("± -- m"); txtSat.setText("0");
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

    private void showStaticModeHelpDialog() {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.msg_static_mode_desc)).append("\n\n");
        sb.append("<b>").append(getString(R.string.label_how_to_use)).append("</b>\n");
        sb.append(getString(R.string.msg_static_step_1)).append("\n");
        sb.append(getString(R.string.msg_static_step_2)).append("\n");
        sb.append(getString(R.string.msg_static_step_3)).append("\n\n");
        sb.append("<b>").append(getString(R.string.label_what_it_does)).append("</b>\n");
        sb.append(getString(R.string.msg_static_benefit));

        UIUtils.showProInfoDialog(requireContext(), 
                getString(R.string.title_static_mode_help), 
                android.text.Html.fromHtml(sb.toString(), android.text.Html.FROM_HTML_MODE_LEGACY), 
                R.drawable.ic_precision);
    }

    private void showPrecisionDetailsDialog() {
        Location loc = viewModel.getRawLocation().getValue();
        if (loc == null) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_gps_no_signal));
            return;
        }

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_dialog_precision_info, null);
        dialog.setContentView(view);

        // Vincular filas usando el componente reutilizable
        setPrecisionRow(view.findViewById(R.id.row_h_acc), R.string.label_h_accuracy, 
                String.format(Locale.US, "± %.2f m", loc.getAccuracy()), R.drawable.ic_precision);
        
        String vAcc = loc.hasVerticalAccuracy() ? String.format(Locale.US, "± %.2f m", loc.getVerticalAccuracyMeters()) : "N/A";
        setPrecisionRow(view.findViewById(R.id.row_v_acc), R.string.label_v_accuracy, vAcc, R.drawable.ic_precision);

        String sAcc = loc.hasSpeedAccuracy() ? String.format(Locale.US, "± %.2f m/s", loc.getSpeedAccuracyMetersPerSecond()) : "N/A";
        setPrecisionRow(view.findViewById(R.id.row_speed_acc), R.string.label_speed_accuracy, sAcc, R.drawable.ic_temp_pro);

        String bAcc = loc.hasBearingAccuracy() ? String.format(Locale.US, "± %.2f °", loc.getBearingAccuracyDegrees()) : "N/A";
        setPrecisionRow(view.findViewById(R.id.row_bearing_acc), R.string.label_bearing_accuracy, bAcc, R.drawable.ic_compass_needle);

        setPrecisionRow(view.findViewById(R.id.row_provider), R.string.label_provider_source, 
                loc.getProvider().toUpperCase(), R.drawable.ic_shield_pro);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault());
        setPrecisionRow(view.findViewById(R.id.row_timestamp), R.string.label_fix_timestamp, 
                sdf.format(new Date(loc.getTime())), R.drawable.ic_calendar);

        view.findViewById(R.id.btn_precision_info_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void setPrecisionRow(View container, int labelRes, String value, int iconRes) {
        if (container == null) return;
        TextView label = container.findViewById(R.id.txt_detail_label);
        TextView txtValue = container.findViewById(R.id.txt_detail_value);
        ImageView icon = container.findViewById(R.id.img_detail_icon);
        
        if (label != null) label.setText(labelRes);
        if (txtValue != null) {
            txtValue.setText(value);
            txtValue.setTextColor(requireContext().getColor(R.color.accent_light)); // Azul de la paleta
        }
        if (icon != null) {
            icon.setImageResource(iconRes);
            icon.setColorFilter(requireContext().getColor(R.color.accent_light));
        }
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
        
        String precision = txtPrecision.getText().toString();
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

    private void showSatelliteDetailsDialog() {
        if (!isAdded()) return;
        
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View view = getLayoutInflater().inflate(R.layout.layout_dialog_satellite_info, null);
        dialog.setContentView(view);

        TextView txtUsed = view.findViewById(R.id.txt_sat_used);
        TextView txtTotal = view.findViewById(R.id.txt_sat_total);
        RecyclerView rv = view.findViewById(R.id.rv_satellites);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (lastGnssStatus != null) {
            int usedCount = 0;
            int totalCount = lastGnssStatus.getSatelliteCount();
            List<SatelliteInfo> satList = new ArrayList<>();

            for (int i = 0; i < totalCount; i++) {
                SatelliteInfo info = new SatelliteInfo();
                info.svid = lastGnssStatus.getSvid(i);
                info.constellation = getConstellationName(lastGnssStatus.getConstellationType(i));
                info.signal = lastGnssStatus.getCn0DbHz(i);
                info.usedInFix = lastGnssStatus.usedInFix(i);
                if (info.usedInFix) usedCount++;
                satList.add(info);
            }

            txtUsed.setText(String.valueOf(usedCount));
            txtTotal.setText(String.valueOf(totalCount));
            rv.setAdapter(new SatelliteAdapter(satList));
        }

        view.findViewById(R.id.btn_close_sat_info).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private String getConstellationName(int type) {
        switch (type) {
            case GnssStatus.CONSTELLATION_GPS: return "GPS";
            case GnssStatus.CONSTELLATION_GLONASS: return "GLONASS";
            case GnssStatus.CONSTELLATION_BEIDOU: return "BEIDOU";
            case GnssStatus.CONSTELLATION_GALILEO: return "GALILEO";
            case GnssStatus.CONSTELLATION_QZSS: return "QZSS";
            case GnssStatus.CONSTELLATION_SBAS: return "SBAS";
            default: return "Desconocida";
        }
    }

    private static class SatelliteInfo {
        int svid;
        String constellation;
        float signal;
        boolean usedInFix;
    }

    private static class SatelliteAdapter extends RecyclerView.Adapter<SatelliteAdapter.ViewHolder> {
        private final List<SatelliteInfo> list;
        SatelliteAdapter(List<SatelliteInfo> list) { this.list = list; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_satellite_info, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            SatelliteInfo si = list.get(pos);
            h.txtId.setText(String.format(Locale.US, "%02d", si.svid));
            h.txtConst.setText(si.constellation);
            h.txtSignal.setText(String.format(Locale.US, "%.1f", si.signal));
            h.txtStatus.setText(si.usedInFix ? "En uso" : h.itemView.getContext().getString(R.string.label_visible_status));
            h.txtStatus.setTextColor(si.usedInFix ? 0xFF10B981 : 0xFF9FA2A3); // flight_green vs text_secondary
        }
        @Override public int getItemCount() { return list.size(); }
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtId, txtConst, txtStatus, txtSignal;
            ViewHolder(View v) {
                super(v);
                txtId = v.findViewById(R.id.txt_sat_id);
                txtConst = v.findViewById(R.id.txt_sat_constellation);
                txtStatus = v.findViewById(R.id.txt_sat_status);
                txtSignal = v.findViewById(R.id.txt_sat_signal);
            }
        }
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
        // Limpieza de referencias
        layoutSatContainer = null;
        btnGuardarPunto = null;
    }
}
