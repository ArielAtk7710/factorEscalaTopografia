package bo.com.solucionesit.factorcombinado;

import android.app.AlertDialog;
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
import android.widget.TextView;
import java.text.DecimalFormat;

public class AutomaticFragment extends Fragment {
    private Typeface fontAwesome;
    private Typeface materialIco;
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

        this.fontAwesome = Typeface.createFromAsset(requireContext().getAssets(), "fonts/fontawesome-webfont.ttf");
        this.materialIco = Typeface.createFromAsset(requireContext().getAssets(), "fonts/MaterialIcons-Regular.ttf");

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
            txt_presicion.setTypeface(fontAwesome);
            txt_presicion.setText("\uf140 ±" + Math.round(loc.getAccuracy()) + " m");

            Bundle extras = loc.getExtras();
            txt_sat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.satellite, 0, 0, 0);
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
            new AlertDialog.Builder(requireContext())
                    .setMessage("El GPS se encuentra deshabilitado")
                    .setTitle("AVISO TERRATEC")
                    .show();
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
            new AlertDialog.Builder(requireContext())
                    .setMessage("GENIAL!!!. GPS habilitado")
                    .setTitle("AVISO TERRATEC")
                    .show();
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