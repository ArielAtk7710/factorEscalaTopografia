package bo.com.factorcombinadotopo;

import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.text.DecimalFormat;
import android.view.Gravity;
import android.widget.Toast;
import android.content.ContentValues;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AutomaticFragment extends Fragment {
    private TextView txt_alt;
    private TextView txt_est;
    private TextView txt_fa;
    private TextView txt_fa_ppm;
    private TextView txt_fc;
    private TextView txt_fc_ppm;
    private TextView txt_fe;
    private TextView txt_fe_ppm;
    private TextView txt_hemis;
    private TextView txt_lat;
    private TextView txt_lon;
    private TextView txt_nort;
    private TextView txt_presicion;
    private TextView txt_sat;
    private TextView txt_zona;
    private SwitchMaterial switchMapa;
    private CardView cardMapa;
    private Button btn_guardar_punto;

    private boolean isGpsCurrentlyEnabled = true;

    private final Runnable manualInfoRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && !isGpsCurrentlyEnabled) {
                showProToast("info", "Ingrese de Forma Manual los datos");
            }
        }
    };

    private final Runnable periodicAlertRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded() && !isGpsCurrentlyEnabled) {
                // Paso 1: Mostrar Advertencia (Amarillo)
                showProToast("warning", "Active el GPS de su Dispositivo");
                resetUIData();

                // Paso 2: Programar Info (Azul) a los 5 segundos
                gpsCheckHandler.removeCallbacks(manualInfoRunnable);
                gpsCheckHandler.postDelayed(manualInfoRunnable, 5000);

                // Paso 3: Reiniciar este ciclo en 20 segundos totales
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

        LocationManager mlocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (mlocManager == null) return;

        boolean isEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!isEnabled && (isGpsCurrentlyEnabled || forceNotification)) {
            // Recién desactivado (o forzado al entrar)
            isGpsCurrentlyEnabled = false;
            
            // Detener cualquier ciclo previo y empezar uno nuevo
            stopAlertCycles();
            gpsCheckHandler.post(periodicAlertRunnable);
            
        } else if (isEnabled && (!isGpsCurrentlyEnabled || forceNotification)) {
            // Recién activado (o forzado al entrar)
            isGpsCurrentlyEnabled = true;
            stopAlertCycles();
            showProToast("success", "GPS ACTIVADO");
        }
    }

    private void stopAlertCycles() {
        gpsCheckHandler.removeCallbacks(periodicAlertRunnable);
        gpsCheckHandler.removeCallbacks(manualInfoRunnable);
    }

    private void showSavePointDialog() {
        if (!isAdded() || getActivity() == null) return;

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_point, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.setView(dialogView);

        EditText etPointName = dialogView.findViewById(R.id.et_point_name);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        btnSave.setOnClickListener(v -> {
            String pointName = etPointName.getText().toString().trim();
            if (pointName.isEmpty()) {
                etPointName.setError("Ingrese un nombre");
                return;
            }
            
            // Lógica de guardado dual (DB y TXT)
            guardarPuntoEnDbYTXT(pointName);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void guardarPuntoEnDbYTXT(String nombrePunto) {
        // 1. Guardar en Base de Datos
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOMBRE, nombrePunto);
        values.put(DatabaseHelper.COLUMN_LATITUD, txt_lat.getText().toString());
        values.put(DatabaseHelper.COLUMN_LONGITUD, txt_lon.getText().toString());
        values.put(DatabaseHelper.COLUMN_ALTURA, txt_alt.getText().toString());
        values.put(DatabaseHelper.COLUMN_ESTE, txt_est.getText().toString());
        values.put(DatabaseHelper.COLUMN_NORTE, txt_nort.getText().toString());
        values.put(DatabaseHelper.COLUMN_ZONA, txt_zona.getText().toString());
        values.put(DatabaseHelper.COLUMN_HEMISFERIO, txt_hemis.getText().toString());
        values.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, txt_fe.getText().toString());
        values.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, txt_fa.getText().toString());
        values.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, txt_fc.getText().toString());
        
        long id = dbHelper.insertarPunto(values);

        // 2. Exportar a TXT (Lógica similar a ManualFragment)
        StringBuilder sb = new StringBuilder();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        sb.append("--- REPORTE DE PUNTO REGISTRADO (AUTOMÁTICO) ---\n");
        sb.append("Punto: ").append(nombrePunto).append("\n");
        sb.append("Fecha: ").append(timeStamp).append("\n\n");
        
        sb.append("COORDENADAS GEODÉSICAS:\n");
        sb.append("Latitud: ").append(txt_lat.getText().toString()).append("\n");
        sb.append("Longitud: ").append(txt_lon.getText().toString()).append("\n");
        sb.append("Altura: ").append(txt_alt.getText().toString()).append("\n\n");
        
        sb.append("COORDENADAS UTM:\n");
        sb.append("Este: ").append(txt_est.getText().toString()).append("\n");
        sb.append("Norte: ").append(txt_nort.getText().toString()).append("\n");
        sb.append("Zona/Hem: ").append(txt_zona.getText().toString()).append(" ").append(txt_hemis.getText().toString()).append("\n\n");

        sb.append("FACTORES DE CORRECCIÓN:\n");
        sb.append("Factor de Escala: ").append(txt_fe.getText().toString()).append(" (").append(txt_fe_ppm.getText().toString()).append(")\n");
        sb.append("Factor de Altura: ").append(txt_fa.getText().toString()).append(" (").append(txt_fa_ppm.getText().toString()).append(")\n");
        sb.append("FACTOR COMBINADO: ").append(txt_fc.getText().toString()).append(" (").append(txt_fc_ppm.getText().toString()).append(")\n");
        sb.append("\nDesarrollado por Attack7710\n");
        sb.append("------------------------------------------------\n");

        String fileName = "Punto_" + nombrePunto.replaceAll("[^a-zA-Z0-9]", "_") + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
        
        try {
            File path = requireContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (path != null && !path.exists()) path.mkdirs();
            File file = new File(path, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(sb.toString().getBytes());
            fos.close();
            
            if (id != -1) {
                showProToast("success", "Punto '" + nombrePunto + "' guardado. Reporte en: Android/data/.../files/Documents");
            }
        } catch (IOException e) {
            if (id != -1) {
                showProToast("success", "Punto guardado en Registro (Error al exportar TXT)");
            }
        }
    }

    private Handler gpsCheckHandler = new Handler(Looper.getMainLooper());
    private final Runnable checkGpsRunnable = new Runnable() {
        @Override
        public void run() {
            if (!isAdded() || getActivity() == null) return;
            
            LocationManager mlocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            if (mlocManager != null) {
                boolean isEnabled = mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                if (isEnabled != isGpsCurrentlyEnabled) {
                    checkGpsState(false);
                }
            }
            gpsCheckHandler.postDelayed(this, 5000);
        }
    };

    private void showProToast(String type, String message) {
        if (!isAdded() || getActivity() == null) return;

        View layout = getLayoutInflater().inflate(R.layout.layout_custom_toast_pro, getActivity().findViewById(android.R.id.content), false);
        View root = layout.findViewById(R.id.toast_root);
        android.widget.ImageView icon = layout.findViewById(R.id.toast_icon);
        TextView label = layout.findViewById(R.id.toast_label);
        TextView msg = layout.findViewById(R.id.toast_message);

        switch (type.toLowerCase()) {
            case "success":
                root.setBackgroundResource(R.drawable.bg_toast_success);
                icon.setImageResource(R.drawable.ic_toast_success);
                label.setText("Éxito:");
                break;
            case "warning":
                root.setBackgroundResource(R.drawable.bg_toast_warning);
                icon.setImageResource(R.drawable.ic_toast_warning);
                label.setText("GPS Desactivado");
                break;
            case "error":
                root.setBackgroundResource(R.drawable.bg_toast_error);
                icon.setImageResource(R.drawable.ic_toast_error);
                label.setText("Error:");
                break;
            default: // info
                root.setBackgroundResource(R.drawable.bg_toast_info);
                icon.setImageResource(R.drawable.ic_toast_info);
                label.setText("Información:");
                break;
        }

        msg.setText(message);

        Toast toast = new Toast(requireContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    private void resetUIData() {
        if (txt_lat == null) return;
        txt_lat.setText("0");
        txt_lon.setText("0");
        txt_alt.setText("0");
        txt_est.setText("0");
        txt_nort.setText("0");
        txt_zona.setText("0");
        txt_hemis.setText("-");
        txt_fe.setText("0");
        txt_fe_ppm.setText("0ppm");
        txt_fa.setText("0");
        txt_fa_ppm.setText("0ppm");
        txt_fc.setText("0");
        txt_fc_ppm.setText("0ppm");
        txt_presicion.setText("±0 m");
        txt_sat.setText("0");
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(gpsReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        
        checkGpsState(true); // Forzar chequeo inicial al entrar
        
        gpsCheckHandler.post(checkGpsRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(gpsReceiver);
        gpsCheckHandler.removeCallbacks(checkGpsRunnable);
        stopAlertCycles();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_automatic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.txt_est = view.findViewById(R.id.txt_est);
        this.txt_nort = view.findViewById(R.id.txt_nort);
        this.txt_zona = view.findViewById(R.id.txt_zona);
        this.txt_hemis = view.findViewById(R.id.txt_hemis);
        this.txt_lat = view.findViewById(R.id.txt_lat);
        this.txt_alt = view.findViewById(R.id.txt_alt);
        this.txt_lon = view.findViewById(R.id.txt_lon);
        this.txt_fe = view.findViewById(R.id.txt_fe);
        this.txt_fe_ppm = view.findViewById(R.id.txt_fe_ppm);
        this.txt_fa = view.findViewById(R.id.txt_fa);
        this.txt_fa_ppm = view.findViewById(R.id.txt_fa_ppm);
        this.txt_fc = view.findViewById(R.id.txt_fc);
        this.txt_fc_ppm = view.findViewById(R.id.txt_fc_ppm);
        this.txt_presicion = view.findViewById(R.id.txt_presicion);
        this.txt_sat = view.findViewById(R.id.txt_sat);
        this.switchMapa = view.findViewById(R.id.switch_mapa);
        this.cardMapa = view.findViewById(R.id.card_mapa);
        this.btn_guardar_punto = view.findViewById(R.id.btn_guardar_punto_auto);

        this.btn_guardar_punto.setOnClickListener(v -> {
            showSavePointDialog();
        });

        this.switchMapa.setOnCheckedChangeListener((buttonView, isChecked) -> {
            this.cardMapa.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        if (ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_FINE_LOCATION") != 0 &&
            ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1000);
        } else {
            locationStart();
        }
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) requireActivity().getSystemService("location");
        if (mlocManager == null) return;

        Localizacion local = new Localizacion();
        boolean gpsEnabled = mlocManager.isProviderEnabled("gps");
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
            startActivity(settingsIntent);
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_FINE_LOCATION") == 0 ||
            ActivityCompat.checkSelfPermission(requireContext(), "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            mlocManager.requestLocationUpdates("network", 0L, 0.0f, local);
            mlocManager.requestLocationUpdates("gps", 0L, 0.0f, local);
        }
    }

    public class Localizacion implements LocationListener {
        @Override
        @RequiresApi(api = 17)
        public void onLocationChanged(Location loc) {
            Integer Z;
            Integer W;
            showGPSData(loc);
            Double K = 0.9996d;
            Double L = loc.getLongitude();
            Double F = loc.getLatitude();
            Double Alt = loc.getAltitude();

            String hemisferio = F < 0.0d ? "S" : "N";
            if (L > 0.0d) {
                Z = ((int) Math.abs(L / 6.0d)) + 31;
                W = (((int) (L / 6.0d)) * 6) + 3;
            } else {
                Z = 30 - ((int) Math.abs(L / 6.0d));
                W = (((int) (L / 6.0d)) * 6) - 3;
            }
            Double A = 6378137.0d;
            Double B = 6356752.31424518d;
            Double E = (Math.pow(A, 2.0d) - Math.pow(B, 2.0d)) / Math.pow(A, 2.0d);
            Double D = (Math.pow(A, 2.0d) - Math.pow(B, 2.0d)) / Math.pow(B, 2.0d);
            Double N = A / Math.sqrt(1.0d - (E * Math.pow(Math.sin((F * 3.141592653589793d) / 180.0d), 2.0d)));
            Double T = Math.pow(Math.tan((F * 3.141592653589793d) / 180.0d), 2.0d);
            Double C = D * Math.pow(Math.cos((F * 3.141592653589793d) / 180.0d), 2.0d);
            Double G = (L - ((double) W)) * ((Math.cos((F * 3.141592653589793d) / 180.0d) * 3.141592653589793d) / 180.0d);
            Double M = A * (((((((1.0d - (E / 4.0d)) - ((3.0d * Math.pow(E, 2.0d)) / 64.0d)) - ((5.0d * Math.pow(E, 3.0d)) / 256.0d)) * ((F * 3.141592653589793d) / 180.0d)) - (((((3.0d * E) / 8.0d) + ((3.0d * Math.pow(E, 2.0d)) / 32.0d)) + ((45.0d * Math.pow(E, 3.0d)) / 1024.0d)) * Math.sin(((2.0d * F) * 3.141592653589793d) / 180.0d))) + ((((15.0d * Math.pow(E, 2.0d)) / 256.0d) + ((45.0d * Math.pow(E, 3.0d)) / 1024.0d)) * Math.sin(((4.0d * F) * 3.141592653589793d) / 180.0d))) - (((35.0d * Math.pow(E, 3.0d)) / 3072.0d) * Math.sin(((6.0d * F) * 3.141592653589793d) / 180.0d)));
            Double Rt = K * N * (G + ((((1.0d - T) + C) * Math.pow(G, 3.0d)) / 6.0d) + ((((((5.0d - (18.0d * T)) + Math.pow(T, 2.0d)) + (72.0d * C)) - (58.0d * D)) * Math.pow(G, 5.0d)) / 120.0d));
            Double X = Rt + 500000.0d;
            Double H = K * (M + (N * Math.tan((F * 3.141592653589793d) / 180.0d) * ((Math.pow(G, 2.0d) / 2.0d) + (((((5.0d - T) + (9.0d * C)) + (4.0d * Math.pow(C, 2.0d))) * Math.pow(G, 4.0d)) / 24.0d) + ((((((61.0d - (58.0d * T)) + Math.pow(T, 2.0d)) + (600.0d * C)) - (330.0d * D)) * Math.pow(G, 6.0d)) / 720.0d))));
            Double Y = H;
            if (H < 0.0d) {
                Y = H + 1.0E7d;
            }
            Double Q = K * (1.0d + (((1.0d + C) * Math.pow(G, 2.0d)) / 2.0d) + ((((((5.0d - (4.0d * T)) + (42.0d * C)) + (13.0d * Math.pow(C, 2.0d))) - (28.0d * D)) * Math.pow(G, 4.0d)) / 24.0d) + ((((61.0d - (148.0d * T)) + (16.0d * Math.pow(T, 2.0d))) * Math.pow(G, 6.0d)) / 720.0d));
            Double PPMQ = (-1.0d) * (1.0d - Q) * 1000000.0d;
            Double MM = (A * (1.0d - E)) / Math.sqrt(Math.pow(1.0d - (E * Math.pow(Math.sin((F * 3.141592653589793d) / 180.0d), 2.0d)), 3.0d));
            Double RR = Math.sqrt(MM * N);
            Double KH = (RR + Alt) / (RR + (2.0d * Alt));
            Double PPMH = (-1.0d) * (1.0d - KH) * 1000000.0d;
            Double FC = KH * Q;
            Double PPMFC = PPMQ + PPMH;

            DecimalFormat formatter = new DecimalFormat("#0.00");
            DecimalFormat formatterEsc = new DecimalFormat("#0.00000000");

            txt_est.setText(formatter.format(X) + " m");
            txt_nort.setText(formatter.format(Y) + " m");
            txt_zona.setText(Integer.toString(Z));
            txt_hemis.setText(hemisferio);
            txt_presicion.setText("±" + Math.round(loc.getAccuracy()) + " m");

            Bundle extras = loc.getExtras();
            if (extras != null) {
                txt_sat.setText(Integer.toString(extras.getInt("satellites")));
            }

            txt_fe.setText(formatterEsc.format(Q));
            txt_fe_ppm.setText(Math.round(PPMQ) + "ppm");
            txt_fa.setText(formatterEsc.format(KH));
            txt_fa_ppm.setText(Math.round(PPMH) + "ppm");
            txt_fc.setText(formatterEsc.format(FC));
            txt_fc_ppm.setText(Math.round(PPMFC) + "ppm");
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
            // La lógica de repetición controlada se maneja en checkGpsRunnable
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            // Se elimina la alerta intrusiva anterior para una mejor experiencia de usuario
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case 0: Log.d("STATE", "LocationProvider.OUT_OF_SERVICE"); break;
                case 1: Log.d("STATE", "LocationProvider.TEMPORARILY_UNAVAILABLE"); break;
                case 2: Log.d("STATE", "LocationProvider.AVAILABLE"); break;
            }
        }
    }

    public void showGPSData(Location loc) {
        DecimalFormat formatter = new DecimalFormat("#0.000");
        Double lat = Math.abs(loc.getLatitude());
        String latGra = Integer.toString(lat.intValue());
        Double tmpLat = (lat - lat.intValue()) * 60.0d;
        String latMin = Integer.toString(tmpLat.intValue());
        Double tmpLat2 = (tmpLat - tmpLat.intValue()) * 60.0d;
        String hemisferio = loc.getLatitude() < 0.0d ? "-" : "";
        this.txt_lat.setText(hemisferio + latGra + "º " + latMin + "' " + formatter.format(tmpLat2) + "'' ");

        Double lon = Math.abs(loc.getLongitude());
        String lonGra = Integer.toString(lon.intValue());
        Double tmpLon = (lon - lon.intValue()) * 60.0d;
        String lonMin = Integer.toString(tmpLon.intValue());
        Double tmpLon2 = (tmpLon - tmpLon.intValue()) * 60.0d;
        String eOrW = loc.getLongitude() < 0.0d ? "-" : "";
        this.txt_lon.setText(eOrW + lonGra + "º " + lonMin + "' " + formatter.format(tmpLon2) + "'' ");
        this.txt_alt.setText(formatter.format(loc.getAltitude()) + " m");
    }
}