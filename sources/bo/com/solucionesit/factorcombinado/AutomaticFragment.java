package bo.com.solucionesit.factorcombinado;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.DecimalFormat;

/* JADX INFO: loaded from: classes.dex */
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

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (ActivityCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_FINE_LOCATION") != 0 && ActivityCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1000);
        } else {
            locationStart();
        }
        this.fontAwesome = Typeface.createFromAsset(getActivity().getAssets(), "fonts/fontawesome-webfont.ttf");
        this.materialIco = Typeface.createFromAsset(getActivity().getAssets(), "fonts/MaterialIcons-Regular.ttf");
        return inflater.inflate(R.layout.fragment_automatic, container, false);
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.txt_est = (TextView) getActivity().findViewById(R.id.txt_est);
        this.txt_nort = (TextView) getActivity().findViewById(R.id.txt_nort);
        this.txt_zona = (TextView) getActivity().findViewById(R.id.txt_zona);
        this.txt_hemis = (TextView) getActivity().findViewById(R.id.txt_hemis);
        this.txt_lat = (TextView) getActivity().findViewById(R.id.txt_lat);
        this.txt_alt = (TextView) getActivity().findViewById(R.id.txt_alt);
        this.txt_lon = (TextView) getActivity().findViewById(R.id.txt_lon);
        this.txt_fe = (TextView) getActivity().findViewById(R.id.txt_fe);
        this.txt_fe_ppm = (TextView) getActivity().findViewById(R.id.txt_fe_ppm);
        this.txt_fa = (TextView) getActivity().findViewById(R.id.txt_fa);
        this.txt_fa_ppm = (TextView) getActivity().findViewById(R.id.txt_fa_ppm);
        this.txt_fc = (TextView) getActivity().findViewById(R.id.txt_fc);
        this.txt_fc_ppm = (TextView) getActivity().findViewById(R.id.txt_fc_ppm);
        this.txt_presicion = (TextView) getActivity().findViewById(R.id.txt_presicion);
        this.txt_sat = (TextView) getActivity().findViewById(R.id.txt_sat);
    }

    private void locationStart() {
        LocationManager mlocManager = (LocationManager) getActivity().getSystemService("location");
        Localizacion Local = new Localizacion();
        boolean gpsEnabled = mlocManager.isProviderEnabled("gps");
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent("android.settings.LOCATION_SOURCE_SETTINGS");
            startActivity(settingsIntent);
        }
        if (ActivityCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_FINE_LOCATION") != 0 && ActivityCompat.checkSelfPermission(getContext(), "android.permission.ACCESS_COARSE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1000);
        } else {
            mlocManager.requestLocationUpdates("network", 0L, 0.0f, Local);
            mlocManager.requestLocationUpdates("gps", 0L, 0.0f, Local);
        }
    }

    public class Localizacion implements LocationListener {
        public Localizacion() {
        }

        @Override // android.location.LocationListener
        @RequiresApi(api = 17)
        public void onLocationChanged(Location loc) {
            Integer Z;
            Integer W;
            AutomaticFragment.this.showGPSData(loc);
            Double K = Double.valueOf(0.9996d);
            Double L = Double.valueOf(loc.getLongitude());
            Double F = Double.valueOf(loc.getLatitude());
            Double Alt = Double.valueOf(loc.getAltitude());
            Integer.valueOf(0);
            Integer.valueOf(0);
            String hemisferio = F.doubleValue() < 0.0d ? "S" : "N";
            if (L.doubleValue() > 0.0d) {
                Z = Integer.valueOf(((int) Math.abs(L.doubleValue() / 6.0d)) + 31);
                W = Integer.valueOf((((int) (L.doubleValue() / 6.0d)) * 6) + 3);
            } else {
                Z = Integer.valueOf(30 - ((int) Math.abs(L.doubleValue() / 6.0d)));
                W = Integer.valueOf((((int) (L.doubleValue() / 6.0d)) * 6) - 3);
            }
            Double A = Double.valueOf(6378137.0d);
            Double B = Double.valueOf(6356752.31424518d);
            Double.valueOf(A.doubleValue() / (A.doubleValue() - B.doubleValue()));
            Double E = Double.valueOf((Math.pow(A.doubleValue(), 2.0d) - Math.pow(B.doubleValue(), 2.0d)) / Math.pow(A.doubleValue(), 2.0d));
            Double D = Double.valueOf((Math.pow(A.doubleValue(), 2.0d) - Math.pow(B.doubleValue(), 2.0d)) / Math.pow(B.doubleValue(), 2.0d));
            Double N = Double.valueOf(A.doubleValue() / Math.sqrt(1.0d - (E.doubleValue() * Math.pow(Math.sin((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d))));
            Double T = Double.valueOf(Math.pow(Math.tan((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d));
            Double C = Double.valueOf(D.doubleValue() * Math.pow(Math.cos((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d));
            Double G = Double.valueOf((L.doubleValue() - ((double) W.intValue())) * ((Math.cos((F.doubleValue() * 3.141592653589793d) / 180.0d) * 3.141592653589793d) / 180.0d));
            Double M = Double.valueOf(A.doubleValue() * (((((((1.0d - (E.doubleValue() / 4.0d)) - ((3.0d * Math.pow(E.doubleValue(), 2.0d)) / 64.0d)) - ((5.0d * Math.pow(E.doubleValue(), 3.0d)) / 256.0d)) * ((F.doubleValue() * 3.141592653589793d) / 180.0d)) - (((((3.0d * E.doubleValue()) / 8.0d) + ((3.0d * Math.pow(E.doubleValue(), 2.0d)) / 32.0d)) + ((45.0d * Math.pow(E.doubleValue(), 3.0d)) / 1024.0d)) * Math.sin(((2.0d * F.doubleValue()) * 3.141592653589793d) / 180.0d))) + ((((15.0d * Math.pow(E.doubleValue(), 2.0d)) / 256.0d) + ((45.0d * Math.pow(E.doubleValue(), 3.0d)) / 1024.0d)) * Math.sin(((4.0d * F.doubleValue()) * 3.141592653589793d) / 180.0d))) - (((35.0d * Math.pow(E.doubleValue(), 3.0d)) / 3072.0d) * Math.sin(((6.0d * F.doubleValue()) * 3.141592653589793d) / 180.0d))));
            Double Rt = Double.valueOf(K.doubleValue() * N.doubleValue() * (G.doubleValue() + ((((1.0d - T.doubleValue()) + C.doubleValue()) * Math.pow(G.doubleValue(), 3.0d)) / 6.0d) + ((((((5.0d - (18.0d * T.doubleValue())) + Math.pow(T.doubleValue(), 2.0d)) + (72.0d * C.doubleValue())) - (58.0d * D.doubleValue())) * Math.pow(G.doubleValue(), 5.0d)) / 120.0d)));
            Double X = Double.valueOf(Rt.doubleValue() + 500000.0d);
            Double H = Double.valueOf(K.doubleValue() * (M.doubleValue() + (N.doubleValue() * Math.tan((F.doubleValue() * 3.141592653589793d) / 180.0d) * ((Math.pow(G.doubleValue(), 2.0d) / 2.0d) + (((((5.0d - T.doubleValue()) + (9.0d * C.doubleValue())) + (4.0d * Math.pow(C.doubleValue(), 2.0d))) * Math.pow(G.doubleValue(), 4.0d)) / 24.0d) + ((((((61.0d - (58.0d * T.doubleValue())) + Math.pow(T.doubleValue(), 2.0d)) + (600.0d * C.doubleValue())) - (330.0d * D.doubleValue())) * Math.pow(G.doubleValue(), 6.0d)) / 720.0d)))));
            Double Y = H;
            if (H.doubleValue() < 0.0d) {
                Y = Double.valueOf(H.doubleValue() + 1.0E7d);
            }
            Double Q = Double.valueOf(K.doubleValue() * (1.0d + (((1.0d + C.doubleValue()) * Math.pow(G.doubleValue(), 2.0d)) / 2.0d) + ((((((5.0d - (4.0d * T.doubleValue())) + (42.0d * C.doubleValue())) + (13.0d * Math.pow(C.doubleValue(), 2.0d))) - (28.0d * D.doubleValue())) * Math.pow(G.doubleValue(), 4.0d)) / 24.0d) + ((((61.0d - (148.0d * T.doubleValue())) + (16.0d * Math.pow(T.doubleValue(), 2.0d))) * Math.pow(G.doubleValue(), 6.0d)) / 720.0d)));
            Double PPMQ = Double.valueOf((-1.0d) * (1.0d - Q.doubleValue()) * 1000000.0d);
            Double MM = Double.valueOf((A.doubleValue() * (1.0d - E.doubleValue())) / Math.sqrt(Math.pow(1.0d - (E.doubleValue() * Math.pow(Math.sin((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d)), 3.0d)));
            Double RR = Double.valueOf(Math.sqrt(MM.doubleValue() * N.doubleValue()));
            Double KH = Double.valueOf((RR.doubleValue() + Alt.doubleValue()) / (RR.doubleValue() + (2.0d * Alt.doubleValue())));
            Double PPMH = Double.valueOf((-1.0d) * (1.0d - KH.doubleValue()) * 1000000.0d);
            Double FC = Double.valueOf(KH.doubleValue() * Q.doubleValue());
            Double PPMFC = Double.valueOf(PPMQ.doubleValue() + PPMH.doubleValue());
            DecimalFormat formatter = new DecimalFormat("#0.00");
            DecimalFormat formatterEsc = new DecimalFormat("#0.00000000");
            AutomaticFragment.this.txt_est.setText(formatter.format(X) + " m");
            AutomaticFragment.this.txt_nort.setText(formatter.format(Y) + " m");
            AutomaticFragment.this.txt_zona.setText(Integer.toString(Z.intValue()));
            AutomaticFragment.this.txt_hemis.setText(hemisferio);
            AutomaticFragment.this.txt_presicion.setTypeface(AutomaticFragment.this.fontAwesome);
            AutomaticFragment.this.txt_presicion.setText("\uf140 ±" + Integer.toString(Math.round(loc.getAccuracy())) + " m");
            Bundle extras = loc.getExtras();
            AutomaticFragment.this.txt_sat.setCompoundDrawablesWithIntrinsicBounds(R.drawable.satellite, 0, 0, 0);
            AutomaticFragment.this.txt_sat.setText(Integer.toString(extras.getInt("satellites")));
            AutomaticFragment.this.txt_fe.setText(formatterEsc.format(Q));
            AutomaticFragment.this.txt_fe_ppm.setText(Integer.toString((int) Math.round(PPMQ.doubleValue())) + "ppm");
            AutomaticFragment.this.txt_fa.setText(formatterEsc.format(KH));
            AutomaticFragment.this.txt_fa_ppm.setText(Integer.toString((int) Math.round(PPMH.doubleValue())) + "ppm");
            AutomaticFragment.this.txt_fc.setText(formatterEsc.format(FC));
            AutomaticFragment.this.txt_fc_ppm.setText(Integer.toString((int) Math.round(PPMFC.doubleValue())) + "ppm");
        }

        @Override // android.location.LocationListener
        public void onProviderDisabled(String provider) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AutomaticFragment.this.getActivity());
            builder.setMessage("El GPS se encuentra deshabilitado").setTitle("AVISO TERRATEC");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override // android.location.LocationListener
        public void onProviderEnabled(String provider) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AutomaticFragment.this.getActivity());
            builder.setMessage("GENIAL!!!. GPS habilitado").setTitle("AVISO TERRATEC");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

        @Override // android.location.LocationListener
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case 0:
                    Log.d("STATE", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case 1:
                    Log.d("STATE", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
                case 2:
                    Log.d("STATE", "LocationProvider.AVAILABLE");
                    break;
            }
        }
    }

    public void showGPSData(Location loc) {
        DecimalFormat formatter = new DecimalFormat("#0.000");
        Double lat = Double.valueOf(Math.abs(loc.getLatitude()));
        String latGra = Integer.toString((int) lat.longValue());
        Double tmpLat = Double.valueOf((lat.doubleValue() - lat.longValue()) * 60.0d);
        String latMin = Integer.toString((int) tmpLat.longValue());
        Double tmpLat2 = Double.valueOf((tmpLat.doubleValue() - tmpLat.longValue()) * 60.0d);
        String hemisferio = loc.getLatitude() < 0.0d ? "-" : "";
        this.txt_lat.setText(hemisferio + latGra + "º " + latMin + "' " + formatter.format(tmpLat2) + "'' ");
        Double lon = Double.valueOf(Math.abs(loc.getLongitude()));
        String lonGra = Integer.toString((int) lon.longValue());
        Double tmpLon = Double.valueOf((lon.doubleValue() - lon.longValue()) * 60.0d);
        String lonMin = Integer.toString((int) tmpLon.longValue());
        Double tmpLon2 = Double.valueOf((tmpLon.doubleValue() - tmpLon.longValue()) * 60.0d);
        String eOrW = loc.getLongitude() < 0.0d ? "-" : "";
        this.txt_lon.setText(eOrW + lonGra + "º " + lonMin + "' " + formatter.format(tmpLon2) + "'' ");
        this.txt_alt.setText(formatter.format(loc.getAltitude()) + " m");
    }
}
