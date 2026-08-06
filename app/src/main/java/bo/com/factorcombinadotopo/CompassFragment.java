package bo.com.factorcombinadotopo;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class CompassFragment extends Fragment implements SensorEventListener {

    private ImageView imgDial, imgBubble, imgNeedle;
    private TextView txtAzimut, txtAzimutDms, txtEste, txtNorte, txtAlt, txtRef, txtLocation;
    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    
    private float[] gravity;
    private float[] geomagnetic;
    private float currentAzimut = 0f;
    private float currentPitch = 0f;
    private float currentRoll = 0f;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private long lastLocationRequestTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compass, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        imgDial = view.findViewById(R.id.img_compass_dial);
        imgBubble = view.findViewById(R.id.img_level_bubble);
        imgNeedle = view.findViewById(R.id.img_compass_needle);
        txtAzimut = view.findViewById(R.id.txt_compass_pro_azimut);
        txtAzimutDms = view.findViewById(R.id.txt_comp_azimut_dms);
        txtLocation = view.findViewById(R.id.txt_comp_location);
        
        txtEste = view.findViewById(R.id.txt_comp_este);
        txtNorte = view.findViewById(R.id.txt_comp_norte);
        txtAlt = view.findViewById(R.id.txt_comp_alt);
        txtRef = view.findViewById(R.id.txt_comp_ref);

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        setupLocationUpdates();
    }

    private void setupLocationUpdates() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (!isAdded() || getView() == null) return;
                for (Location location : locationResult.getLocations()) {
                    if (location != null) updateGpsUI(location);
                }
            }
        };
    }

    private void updateGpsUI(Location loc) {
        if (!isAdded() || getView() == null) return;
        
        double geoidN = GeoidManager.getGeoidUndulation(loc.getLatitude(), loc.getLongitude());
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        float offset = prefs.getFloat("PressureOffset", 0f);
        
        TopoCalculoManager.TopoResult res = TopoCalculoManager.calculateAll(
                loc.getLatitude(), loc.getLongitude(), loc.getAltitude(), geoidN, offset);
        
        txtAlt.setText(GeoUtils.formatCoord(res.altOrto) + " m");
        txtEste.setText(GeoUtils.formatCoord(res.este) + " m");
        txtNorte.setText(GeoUtils.formatCoord(res.norte) + " m");
        
        txtRef.setText(String.format(Locale.US, "%d%c (WGS84)", res.zona, res.hemisferio));

        updateLocationName(loc.getLatitude(), loc.getLongitude());
    }

    private void updateLocationName(double lat, double lon) {
        if (System.currentTimeMillis() - lastLocationRequestTime < 60000) return;
        lastLocationRequestTime = System.currentTimeMillis();

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String city = addresses.get(0).getLocality();
                    String country = addresses.get(0).getCountryName();
                    String locationName = (city != null ? city + ", " : "") + country;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded() && txtLocation != null) txtLocation.setText(locationName);
                    });
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded() && txtLocation != null) txtLocation.setText(R.string.label_unknown_location);
                    });
                }
            } catch (Exception ignored) {}
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
        if (magnetometer != null) sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        try {
            LocationRequest request = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000).build();
            fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException ignored) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravity = event.values.clone();
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagnetic = event.values.clone();
        }

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                
                float azimutDeg = (float) Math.toDegrees(orientation[0]);
                if (azimutDeg < 0) azimutDeg += 360;

                float pitchDeg = (float) Math.toDegrees(orientation[1]);
                float rollDeg = (float) Math.toDegrees(orientation[2]);

                updateCompassAndLevel(azimutDeg, pitchDeg, rollDeg);
            }
        }
    }

    private void updateCompassAndLevel(float azimut, float pitch, float roll) {
        // Suavizado
        currentAzimut = currentAzimut + 0.2f * (azimut - currentAzimut);
        currentPitch = currentPitch + 0.2f * (pitch - currentPitch);
        currentRoll = currentRoll + 0.2f * (roll - currentRoll);

        // Rotación del Dial y la Aguja (Inversa al giro del móvil)
        if (imgDial != null) imgDial.setRotation(-currentAzimut);
        if (imgNeedle != null) imgNeedle.setRotation(-currentAzimut);

        // Movimiento de la Burbuja (Ajustado para dial mediano)
        float maxOffset = 40f; 
        float xOffset = Math.max(-maxOffset, Math.min(maxOffset, currentRoll * 1.2f));
        float yOffset = Math.max(-maxOffset, Math.min(maxOffset, currentPitch * 1.2f));

        if (imgBubble != null) {
            imgBubble.setTranslationX(xOffset);
            imgBubble.setTranslationY(-yOffset);
        }

        if (txtAzimut != null) {
            txtAzimut.setText(String.format(Locale.getDefault(), "%.0f° %s", currentAzimut, getDirection(currentAzimut)));
        }

        if (txtAzimutDms != null) {
            txtAzimutDms.setText(formatToDMS(currentAzimut));
        }
    }

    private String formatToDMS(double val) {
        int d = (int) val;
        double mDouble = (val - d) * 60.0;
        int m = (int) mDouble;
        int s = (int) Math.round((mDouble - m) * 60.0);
        
        if (s == 60) { s = 0; m++; }
        if (m == 60) { m = 0; d++; }
        d %= 360;
        
        return getString(R.string.label_azimut_dms, d, m, s);
    }

    private String getDirection(float azimut) {
        if (azimut >= 337.5 || azimut < 22.5) return getString(R.string.dir_n);
        if (azimut >= 22.5 && azimut < 67.5) return getString(R.string.dir_ne);
        if (azimut >= 67.5 && azimut < 112.5) return getString(R.string.dir_e);
        if (azimut >= 112.5 && azimut < 157.5) return getString(R.string.dir_se);
        if (azimut >= 157.5 && azimut < 202.5) return getString(R.string.dir_s);
        if (azimut >= 202.5 && azimut < 247.5) return getString(R.string.dir_so);
        if (azimut >= 247.5 && azimut < 292.5) return getString(R.string.dir_o);
        if (azimut >= 292.5 && azimut < 337.5) return getString(R.string.dir_no);
        return "";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // En el nuevo diseño minimalista, ocultamos o usamos un indicador discreto si fuera necesario
    }
}
