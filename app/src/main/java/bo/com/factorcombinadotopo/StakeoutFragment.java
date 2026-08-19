package bo.com.factorcombinadotopo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.res.ColorStateList;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import bo.com.factorcombinadotopo.gnss.GnssMeasurement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fragmento de Replanteo Topográfico.
 */
public class StakeoutFragment extends Fragment implements SensorEventListener {

    private Spinner spPoints;
    private TextView txtDistance, txtDirection, txtTargetE, txtTargetN, txtRelAzimuth;
    private TextView txtPrecision, txtSat;
    private ImageView imgArrow;
    private FloatingActionButton fabStatic;

    private SensorManager sensorManager;
    private Sensor accelerometer, magnetometer;
    private float[] gravity, geomagnetic;
    private float currentDeviceHeading = 0f;

    private SurveyViewModel viewModel;
    private List<StakeoutPoint> pointList = new ArrayList<>();
    private StakeoutPoint selectedPoint;
    private GnssMeasurement lastFilteredLocation;
    private boolean isStaticModeActive = false;

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

    private final ActivityResultLauncher<Intent> importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) importPoints(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_stakeout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spPoints = view.findViewById(R.id.sp_stakeout_points);
        txtDistance = view.findViewById(R.id.txt_stakeout_distance);
        txtDirection = view.findViewById(R.id.txt_stakeout_direction);
        txtTargetE = view.findViewById(R.id.txt_target_este);
        txtTargetN = view.findViewById(R.id.txt_target_norte);
        txtRelAzimuth = view.findViewById(R.id.txt_stakeout_azimuth_rel);
        txtPrecision = view.findViewById(R.id.txt_stakeout_precision);
        txtSat = view.findViewById(R.id.txt_stakeout_sat);
        imgArrow = view.findViewById(R.id.img_stakeout_arrow);
        fabStatic = view.findViewById(R.id.fab_stakeout_static);

        view.findViewById(R.id.btn_stakeout_info).setOnClickListener(v -> showFormatInfoDialog());

        view.findViewById(R.id.fab_import_points).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("text/plain");
            importLauncher.launch(intent);
        });

        if (fabStatic != null) {
            fabStatic.setOnClickListener(v -> {
                isStaticModeActive = !isStaticModeActive;
                viewModel.startStaticMeasurement();
                
                if (isStaticModeActive) {
                    fabStatic.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.state_success)));
                    UIUtils.showSuccessToast(requireContext(), "Modo Estático: Promediando posición...");
                } else {
                    fabStatic.setBackgroundTintList(ColorStateList.valueOf(requireContext().getColor(R.color.state_error)));
                    UIUtils.showInfoToast(requireContext(), "Modo Estático desactivado.");
                }
            });
        }

        spPoints.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPoint = pointList.get(position);
                txtTargetE.setText("E: " + GeoUtils.formatCoord(selectedPoint.getEasting()));
                txtTargetN.setText("N: " + GeoUtils.formatCoord(selectedPoint.getNorthing()));
                updateStakeoutUI();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        }

        viewModel = new ViewModelProvider(requireActivity()).get(SurveyViewModel.class);
        
        // Observar Ubicación Filtrada para máxima estabilidad en Replanteo
        viewModel.getFilteredLocation().observe(getViewLifecycleOwner(), gnss -> {
            lastFilteredLocation = gnss;
            if (gnss != null && txtPrecision != null) {
                txtPrecision.setText(String.format(Locale.US, "± %.2f m", gnss.accuracy));
            }
            updateStakeoutUI();
        });
    }

    private void importPoints(Uri uri) {
        try {
            List<StakeoutPoint> imported = StakeoutParser.parseUri(requireContext(), uri);
            if (!imported.isEmpty()) {
                pointList.clear();
                pointList.addAll(imported);
                ArrayAdapter<StakeoutPoint> adapter = new ArrayAdapter<>(requireContext(),
                        android.R.layout.simple_spinner_item, pointList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spPoints.setAdapter(adapter);
                UIUtils.showSuccessToast(requireContext(), "Puntos importados: " + imported.size());
            } else {
                UIUtils.showWarningToast(requireContext(), "El archivo no contiene puntos con el formato correcto.");
            }
        } catch (Exception e) {
            UIUtils.showErrorToast(requireContext(), "Error al importar: " + e.getMessage());
        }
    }

    private void showFormatInfoDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.title_stakeout_guide))
                .setMessage(android.text.Html.fromHtml(getString(R.string.stakeout_guide_body), android.text.Html.FROM_HTML_MODE_LEGACY))
                .setPositiveButton(getString(R.string.btn_understood), null)
                .show();
    }

    private void updateStakeoutUI() {
        if (lastFilteredLocation == null || selectedPoint == null) return;

        // 1. Convertir ubicación actual FILTRADA a UTM usando el motor IGM
        IGMCoordinate.UtmPoint currentUtm = IGMUtmConverter.forward(
                lastFilteredLocation.latitude, lastFilteredLocation.longitude, IGMConstants.Ellipsoid.WGS84);

        // 2. Cálculo de Distancia Euclidiana (En el plano UTM sobre base estable)
        double dx = selectedPoint.getEasting() - currentUtm.easting;
        double dy = selectedPoint.getNorthing() - currentUtm.northing;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // 3. Cálculo de Acimut de Destino (Ángulo desde el Norte)
        double targetAzimuth = Math.toDegrees(Math.atan2(dx, dy));
        if (targetAzimuth < 0) targetAzimuth += 360;

        // 4. Actualizar Textos
        txtDistance.setText(GeoUtils.formatDistance(distance));
        
        // 5. Ángulo Relativo para la flecha
        // La flecha debe girar (AzimutDestino - RumboDispositivo)
        float relativeAngle = (float) targetAzimuth - currentDeviceHeading;
        imgArrow.setRotation(relativeAngle);
        txtRelAzimuth.setText(String.format(Locale.getDefault(), "%.0f°", relativeAngle));

        // Feedback visual de dirección
        txtDirection.setText(getBearingDescription(relativeAngle));
    }

    private String getBearingDescription(float relAngle) {
        float angle = (relAngle + 360) % 360;
        if (angle < 20 || angle > 340) return "ADELANTE";
        if (angle >= 20 && angle < 70) return "DERECHA ADELANTE";
        if (angle >= 70 && angle < 110) return "DERECHA";
        if (angle >= 110 && angle < 160) return "DERECHA ATRÁS";
        if (angle >= 160 && angle < 200) return "ATRÁS";
        if (angle >= 200 && angle < 250) return "IZQUIERDA ATRÁS";
        if (angle >= 250 && angle < 290) return "IZQUIERDA";
        return "IZQUIERDA ADELANTE";
    }

    @Override
    public void onResume() {
        super.onResume();
        if (accelerometer != null) sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        if (magnetometer != null) sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        
        // Registrar callback de Satélites
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        try {
            lm.registerGnssStatusCallback(gnssCallback, new Handler(Looper.getMainLooper()));
        } catch (SecurityException ignored) {}
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) sensorManager.unregisterListener(this);
        
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        lm.unregisterGnssStatusCallback(gnssCallback);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) gravity = event.values.clone();
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) geomagnetic = event.values.clone();

        if (gravity != null && geomagnetic != null) {
            float[] R = new float[9];
            float[] I = new float[9];
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                float[] orientation = new float[3];
                SensorManager.getOrientation(R, orientation);
                float azimut = (float) Math.toDegrees(orientation[0]);
                if (azimut < 0) azimut += 360;
                
                // Filtro de suavizado (Alpha 0.15) - Armonizado con posición filtrada
                currentDeviceHeading = currentDeviceHeading + 0.15f * (azimut - currentDeviceHeading);
                updateStakeoutUI();
            }
        }
    }

    @Override public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
