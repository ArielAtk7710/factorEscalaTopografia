package bo.com.factorcombinadotopo;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationListener;
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
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AutomaticFragment extends Fragment {
    private TextView txt_lat, txt_lon, txt_alt, txt_alt_orto, txt_presion;
    private TextView txt_este, txt_norte, txt_ref_system;
    private TextView txt_fa, txt_fa_ppm, txt_fe, txt_fe_ppm, txt_fc, txt_fc_ppm;
    private TextView txt_presicion, txt_sat, txt_temp;
    private TextView txt_geoid_undulation, txt_geoid_model;
    private SwitchMaterial switchMapa;
    private CardView cardMapa;
    private Button btn_guardar_punto;

    private LocationManager mlocManager;
    private Localizacion localizacion;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TopoCalculoManager.TopoResult lastTopoResult;

    private long lastTempRequestTime = 0;

    private final GnssStatus.Callback gnssCallback = new GnssStatus.Callback() {
        @Override
        public void onSatelliteStatusChanged(@NonNull GnssStatus status) {
            int satellitesInUse = 0;
            for (int i = 0; i < status.getSatelliteCount(); i++) {
                if (status.usedInFix(i)) satellitesInUse++;
            }
            if (txt_sat != null) txt_sat.setText(String.valueOf(satellitesInUse));
        }
    };

    private boolean isGpsCurrentlyEnabled = true;

    private final Runnable manualInfoRunnable = () -> {
        if (isAdded() && !isGpsCurrentlyEnabled) {
            UIUtils.showInfoToast(requireContext(), getString(R.string.msg_manual_entry_hint));
        }
    };

    private final Runnable periodicAlertRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && !isGpsCurrentlyEnabled) {
                UIUtils.showWarningToast(requireContext(), getString(R.string.msg_activate_gps));
                resetUIData();
                gpsCheckHandler.removeCallbacks(manualInfoRunnable);
                gpsCheckHandler.postDelayed(manualInfoRunnable, 5000);
                gpsCheckHandler.postDelayed(this, 20000);
            }
        }
    };

    private final BroadcastReceiver gpsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                checkGpsState(false);
            }
        }
    };

    private void checkGpsState(boolean forceNotification) {
        if (!isAdded() || getActivity() == null) return;
        mlocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (mlocManager == null) return;

        boolean isEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isEnabled && (isGpsCurrentlyEnabled || forceNotification)) {
            isGpsCurrentlyEnabled = false;
            stopAlertCycles();
            gpsCheckHandler.post(periodicAlertRunnable);
        } else if (isEnabled && (!isGpsCurrentlyEnabled || forceNotification)) {
            isGpsCurrentlyEnabled = true;
            stopAlertCycles();
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_gps_activated));
            restartLocationUpdates();
        }
    }

    private void restartLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
            mlocManager.removeUpdates(localizacion);
            mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, localizacion);
            mlocManager.registerGnssStatusCallback(gnssCallback, new Handler(Looper.getMainLooper()));
        }
    }

    private void stopAlertCycles() {
        gpsCheckHandler.removeCallbacks(periodicAlertRunnable);
        gpsCheckHandler.removeCallbacks(manualInfoRunnable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_automatic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Bindings
        txt_lat = view.findViewById(R.id.txt_lat);
        txt_lon = view.findViewById(R.id.txt_lon);
        txt_alt = view.findViewById(R.id.txt_alt);
        txt_alt_orto = view.findViewById(R.id.txt_alt_orto);
        txt_presion = view.findViewById(R.id.txt_presion);
        txt_este = view.findViewById(R.id.txt_este);
        txt_norte = view.findViewById(R.id.txt_norte);
        txt_ref_system = view.findViewById(R.id.txt_ref_system);
        txt_fa = view.findViewById(R.id.txt_fa);
        txt_fa_ppm = view.findViewById(R.id.txt_fa_ppm);
        txt_fe = view.findViewById(R.id.txt_fe);
        txt_fe_ppm = view.findViewById(R.id.txt_fe_ppm);
        txt_fc = view.findViewById(R.id.txt_fc); // Azul box
        txt_fc_ppm = view.findViewById(R.id.txt_fc_ppm);
        txt_presicion = view.findViewById(R.id.txt_presicion);
        txt_sat = view.findViewById(R.id.txt_sat);
        txt_temp = view.findViewById(R.id.txt_temp);
        txt_geoid_undulation = view.findViewById(R.id.txt_geoid_undulation);
        txt_geoid_model = view.findViewById(R.id.txt_geoid_model);
        switchMapa = view.findViewById(R.id.switch_mapa);
        cardMapa = view.findViewById(R.id.card_mapa);
        btn_guardar_punto = view.findViewById(R.id.btn_guardar_punto_auto);

        localizacion = new Localizacion();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        setupLocationCallback();

        btn_guardar_punto.setOnClickListener(v -> showSavePointDialog());
        switchMapa.setOnCheckedChangeListener((bv, isChecked) -> cardMapa.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != 0) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        } else {
            locationStart();
        }
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) processNewLocation(location);
                }
            }
        };
    }

    private void processNewLocation(Location loc) {
        showGPSData(loc);

        // 1. Obtener Ondulación Geoidal N (EGM96) desde GeoidManager

        double geoidN = GeoidManager.getGeoidUndulation(loc.getLatitude(), loc.getLongitude());
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        float offset = prefs.getFloat("PressureOffset", 0f);

        // 2. Ejecutar cálculo topográfico pasando 'geoidN' y 'offset' de presión
        lastTopoResult = TopoCalculoManager.calculateAll(
                loc.getLatitude(),
                loc.getLongitude(),
                loc.getAltitude(),
                geoidN,
                offset
        );

        DecimalFormat df = new DecimalFormat("#0.00");
        DecimalFormat dfUtm = new DecimalFormat("#0.000"); // 3 decimales (milímetros)
        DecimalFormat df8 = new DecimalFormat("#0.00000000");

        // 3. Renderizado de Altura Ortométrica y Modelo Geoidal
        if (txt_alt_orto != null) txt_alt_orto.setText(df.format(lastTopoResult.altOrto) + " m");
        if (txt_geoid_undulation != null) txt_geoid_undulation.setText(df.format(geoidN) + " m");
        if (txt_geoid_model != null) txt_geoid_model.setText("EGM96 (Global)");

        if (txt_presion != null) txt_presion.setText(String.format(Locale.getDefault(), "%.1f", lastTopoResult.pressureMmHg));
        if (txt_presicion != null) txt_presicion.setText("± " + Math.round(loc.getAccuracy()) + " m");

        // Detección automática de Zona UTM y Región
        if (txt_ref_system != null) {
            txt_ref_system.setText(GeoUtils.getUtmZoneFormatted(loc.getLatitude(), loc.getLongitude()));
        }

        // Renderizado en tiempo real de coordenadas UTM
        if (txt_este != null) txt_este.setText(dfUtm.format(lastTopoResult.este) + " m");
        if (txt_norte != null) txt_norte.setText(dfUtm.format(lastTopoResult.norte) + " m");

        txt_fa.setText(df8.format(lastTopoResult.elevationFactor));
        txt_fa_ppm.setText(Math.round((lastTopoResult.elevationFactor - 1.0) * 1000000.0) + " PPM");
        txt_fe.setText(df8.format(lastTopoResult.scaleFactor));
        txt_fe_ppm.setText(Math.round((lastTopoResult.scaleFactor - 1.0) * 1000000.0) + " PPM");
        txt_fc.setText(df8.format(lastTopoResult.combinedFactor));
        if (txt_fc_ppm != null) txt_fc_ppm.setText(Math.round((lastTopoResult.combinedFactor - 1.0) * 1000000.0) + " PPM");

        updateTemperature(loc.getLatitude(), loc.getLongitude());
    }

    private void updateTemperature(double lat, double lon) {
        if (System.currentTimeMillis() - lastTempRequestTime < 600000) return;
        lastTempRequestTime = System.currentTimeMillis();

        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                String urlStr = String.format(Locale.US,
                        "https://api.open-meteo.com/v1/forecast?latitude=%.6f&longitude=%.6f&current=temperature_2m",
                        lat, lon);

                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);

                if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder res = new StringBuilder();
                    String line;
                    while ((line = rd.readLine()) != null) res.append(line);
                    rd.close();

                    JSONObject json = new JSONObject(res.toString());
                    double temp = json.getJSONObject("current").getDouble("temperature_2m");

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded() && txt_temp != null) {
                            txt_temp.setText(String.format(Locale.getDefault(), "%.1f °C", temp));
                        }
                    });
                } else {
                    lastTempRequestTime = 0;
                }
            } catch (Exception e) {
                lastTempRequestTime = 0;
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }

    private void showSavePointDialog() {
        View dv = getLayoutInflater().inflate(R.layout.dialog_save_point, null);
        AlertDialog.Builder b = new AlertDialog.Builder(requireContext());
        AlertDialog d = b.create();
        if (d.getWindow() != null) d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        d.setView(dv);
        EditText etN = dv.findViewById(R.id.et_point_name);
        EditText etObs = dv.findViewById(R.id.et_point_notes);
        dv.findViewById(R.id.btn_dialog_save).setOnClickListener(v -> {
            String name = etN.getText().toString().trim();
            if (name.isEmpty()) { etN.setError(getString(R.string.hint_point_name)); return; }
            guardarPuntoEnRegistro(name, etObs.getText().toString());
            d.dismiss();
        });
        dv.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> d.dismiss());
        d.show();
    }

    private void guardarPuntoEnRegistro(String name, String notes) {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        ContentValues v = new ContentValues();

        // 1. Obtener Hora Local Exacta de la Tablet/Celular
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        String fechaHoraLocal = sdf.format(new Date());

        // 2. Mapear datos utilizando las constantes de DatabaseHelper
        v.put(DatabaseHelper.COLUMN_FECHA, fechaHoraLocal);
        v.put(DatabaseHelper.COLUMN_NOMBRE, name);
        v.put(DatabaseHelper.COLUMN_LATITUD, txt_lat.getText().toString());
        v.put(DatabaseHelper.COLUMN_LONGITUD, txt_lon.getText().toString());

        if (txt_este != null) v.put(DatabaseHelper.COLUMN_ESTE, txt_este.getText().toString());
        if (txt_norte != null) v.put(DatabaseHelper.COLUMN_NORTE, txt_norte.getText().toString());
        
        if (lastTopoResult != null) {
            v.put(DatabaseHelper.COLUMN_ZONA, String.valueOf(lastTopoResult.zona));
            v.put(DatabaseHelper.COLUMN_HEMISFERIO, String.valueOf(lastTopoResult.hemisferio));
        }

        if (txt_fa != null) v.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, txt_fa.getText().toString());
        if (txt_fe != null) v.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, txt_fe.getText().toString());

        v.put(DatabaseHelper.COLUMN_ALTURA, txt_alt.getText().toString());
        v.put(DatabaseHelper.COLUMN_ALTURA_ORTO, txt_alt_orto.getText().toString());
        v.put(DatabaseHelper.COLUMN_PRESION, txt_presion.getText().toString());
        v.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, txt_fc.getText().toString());
        v.put(DatabaseHelper.COLUMN_NOTAS, notes);

        // 3. Guardar
        db.insertarPunto(v);
        UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_saved_format, name));
    }

    private void resetUIData() {
        txt_lat.setText("0"); txt_lon.setText("0"); txt_alt.setText("-- m");
        txt_alt_orto.setText("-- m"); txt_presion.setText("---");
        txt_fc.setText("0.00000000");
        if (txt_fc_ppm != null) txt_fc_ppm.setText("-- PPM");
        txt_sat.setText("0"); txt_temp.setText("-- °C");
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        checkGpsState(true);
        startFusedLocationUpdates();
    }

    private void startFusedLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == 0) {
            LocationRequest req = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).build();
            fusedLocationClient.requestLocationUpdates(req, locationCallback, Looper.getMainLooper());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(gpsReceiver);
        if (fusedLocationClient != null) fusedLocationClient.removeLocationUpdates(locationCallback);
        stopAlertCycles();
    }

    private void locationStart() {
        mlocManager = (LocationManager) requireActivity().getSystemService(Context.LOCATION_SERVICE);
        if (mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) restartLocationUpdates();
    }

    public class Localizacion implements LocationListener {
        @Override public void onLocationChanged(Location loc) { processNewLocation(loc); }
        @Override public void onProviderDisabled(@NonNull String p) {}
        @Override public void onProviderEnabled(@NonNull String p) {}
        @Override public void onStatusChanged(String p, int s, Bundle e) {}
    }

    public void showGPSData(Location loc) {
        DecimalFormat df = new DecimalFormat("#0.000");
        double lat = Math.abs(loc.getLatitude());
        double lon = Math.abs(loc.getLongitude());
        txt_lat.setText((loc.getLatitude() < 0 ? "-" : "") + (int)lat + "º " + (int)((lat-(int)lat)*60) + "' " + df.format(((lat-(int)lat)*60 - (int)((lat-(int)lat)*60))*60) + "''");
        txt_lon.setText((loc.getLongitude() < 0 ? "-" : "") + (int)lon + "º " + (int)((lon-(int)lon)*60) + "' " + df.format(((lon-(int)lon)*60 - (int)((lon-(int)lon)*60))*60) + "''");
        txt_alt.setText(df.format(loc.getAltitude()) + " m");
    }

    private final Handler gpsCheckHandler = new Handler(Looper.getMainLooper());
}