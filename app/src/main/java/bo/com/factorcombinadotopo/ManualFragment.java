package bo.com.factorcombinadotopo;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.content.ContentValues;
import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ManualFragment extends Fragment {
    private Button btn_calcular;
    private Button btn_limpiar;
    private Button btn_guardar;
    private EditText et_alt;
    private EditText et_lat_dec;
    private EditText et_lon_dec;
    private EditText et_utm_este;
    private EditText et_utm_norte;
    private EditText et_utm_alt;
    private LinearLayout layGeo;
    private LinearLayout layUtm;
    private Spinner proyeccion;
    private Spinner sphemisferio;
    private Spinner spzona;
    private TextView txt_man_fa;
    private TextView txt_man_fa_ppm;
    private TextView txt_man_fc;
    private TextView txt_man_fc_ppm;
    private TextView txt_man_fe;
    private TextView txt_man_fe_ppm;
    private TextView txt_man_alt_orto;
    private TextView txt_man_geoid_model;
    private TextView txt_man_geoid_undulation;
    private TextView txt_man_presion;
    private TextView txt_man_presion_hpa;
    private TextView txt_man_temp;
    private TopoCalculoManager.TopoResult lastTopoResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.layGeo = view.findViewById(R.id.layGeo);
        this.layUtm = view.findViewById(R.id.layUtm);

        this.proyeccion = view.findViewById(R.id.spGeoUtm);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.proyeccion.setAdapter(adapter);

        this.spzona = view.findViewById(R.id.spZona);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(requireContext(), R.array.zonas_bolivia, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spzona.setAdapter(adapter1);

        this.sphemisferio = view.findViewById(R.id.spHemisferio);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(requireContext(), R.array.hemisferios, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sphemisferio.setAdapter(adapter2);

        this.et_lat_dec = view.findViewById(R.id.et_lat_dec);
        this.et_lon_dec = view.findViewById(R.id.et_lon_dec);
        this.et_alt = view.findViewById(R.id.et_alt);
        this.et_utm_este = view.findViewById(R.id.et_utm_este);
        this.et_utm_norte = view.findViewById(R.id.et_utm_norte);
        this.et_utm_alt = view.findViewById(R.id.et_utm_alt);
        
        this.txt_man_fe = view.findViewById(R.id.txt_man_fe);
        this.txt_man_fe_ppm = view.findViewById(R.id.txt_man_fe_ppm);
        this.txt_man_fa = view.findViewById(R.id.txt_man_fa);
        this.txt_man_fa_ppm = view.findViewById(R.id.txt_man_fa_ppm);
        this.txt_man_fc = view.findViewById(R.id.txt_man_fc);
        this.txt_man_fc_ppm = view.findViewById(R.id.txt_man_fc_ppm);
        this.txt_man_alt_orto = view.findViewById(R.id.txt_man_alt_orto);
        this.txt_man_geoid_model = view.findViewById(R.id.txt_man_geoid_model);
        this.txt_man_geoid_undulation = view.findViewById(R.id.txt_man_geoid_undulation);
        this.txt_man_presion = view.findViewById(R.id.txt_man_presion);
        this.txt_man_presion_hpa = view.findViewById(R.id.txt_man_presion_hpa);
        this.txt_man_temp = view.findViewById(R.id.txt_man_temp);

        double initialTemp = TopographyRepository.getInstance(requireContext()).getCurrentAmbientTemp();
        this.txt_man_temp.setText(String.format(Locale.getDefault(), "%.1f °C", initialTemp));

        this.proyeccion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    layUtm.setVisibility(View.VISIBLE);
                    layGeo.setVisibility(View.GONE);
                } else {
                    layGeo.setVisibility(View.VISIBLE);
                    layUtm.setVisibility(View.GONE);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.btn_calcular = view.findViewById(R.id.btn_calcular);
        this.btn_calcular.setOnClickListener(v -> {
            if (proyeccion.getSelectedItemPosition() == 0) calculateWithUtm();
            else calculateWithGeo();
        });

        this.btn_limpiar = view.findViewById(R.id.btn_limpiar);
        this.btn_limpiar.setOnClickListener(v -> limpiar());

        this.btn_guardar = view.findViewById(R.id.btn_guardar);
        this.btn_guardar.setOnClickListener(v -> guardarResultados());

        view.findViewById(R.id.btn_manual_info).setOnClickListener(v -> {
            UIUtils.showProInfoDialog(requireContext(), 
                    "INGRESO MANUAL", 
                    android.text.Html.fromHtml(getString(R.string.guide_manual_body), android.text.Html.FROM_HTML_MODE_LEGACY), 
                    R.drawable.ic_info_round_blue);
        });
    }

    private void guardarResultados() {
        if (lastTopoResult == null) {
            boolean canCalculate = false;
            if (proyeccion.getSelectedItemPosition() == 0) {
                canCalculate = !et_utm_este.getText().toString().isEmpty() && !et_utm_norte.getText().toString().isEmpty();
            } else {
                canCalculate = !et_lat_dec.getText().toString().isEmpty() && !et_lon_dec.getText().toString().isEmpty();
            }

            if (canCalculate) {
                if (NetworkUtils.shouldShowOfflineWarning(requireContext())) {
                    UIUtils.showInfoToast(requireContext(), getString(R.string.msg_offline_warning));
                }
                
                if (NetworkUtils.isNetworkAvailable(requireContext())) {
                    UIUtils.showInfoToast(requireContext(), "Calculando datos faltantes...");
                }
                
                if (proyeccion.getSelectedItemPosition() == 0) calculateWithUtm(() -> showSaveDialogInternal());
                else calculateWithGeo(() -> showSaveDialogInternal());
                return;
            }
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_no_registered_points)); 
            return;
        }
        showSaveDialogInternal();
    }

    private void showSaveDialogInternal() {
        if (!isAdded()) return;
        final Context safeContext = getContext();
        if (safeContext == null) return;

        LayoutInflater inflater = LayoutInflater.from(safeContext);
        View dv = inflater.inflate(R.layout.dialog_save_point, null);
        AlertDialog.Builder b = new AlertDialog.Builder(safeContext);
        AlertDialog dialog = b.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setView(dv);

        EditText etPointName = dv.findViewById(R.id.et_point_name);
        EditText etPointNotes = dv.findViewById(R.id.et_point_notes);

        dv.findViewById(R.id.btn_dialog_save).setOnClickListener(v -> {
            String name = etPointName.getText().toString().trim();
            if (name.isEmpty()) { etPointName.setError(getString(R.string.hint_point_name)); return; }
            ejecutarGuardadoManual(name, etPointNotes.getText().toString());
            UIUtils.safeDismissDialog(dialog);
        });

        dv.findViewById(R.id.btn_dialog_cancel).setOnClickListener(v -> UIUtils.safeDismissDialog(dialog));
        UIUtils.safeShowDialog(dialog);
    }

    private void ejecutarGuardadoManual(String nombrePunto, String notas) {
        if (!isAdded()) return;
        final Context safeContext = getContext();
        if (safeContext == null) return;

        DatabaseHelper dbHelper = DatabaseHelper.getInstance(safeContext);
        ContentValues values = new ContentValues();
        String timeStampLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        values.put(DatabaseHelper.COLUMN_FECHA, timeStampLocal);
        values.put(DatabaseHelper.COLUMN_NOMBRE, nombrePunto);
        
        if (lastTopoResult != null) {
            values.put(DatabaseHelper.COLUMN_LATITUD, GeoUtils.formatLatLon(lastTopoResult.lat));
            values.put(DatabaseHelper.COLUMN_LONGITUD, GeoUtils.formatLatLon(lastTopoResult.lon));
            values.put(DatabaseHelper.COLUMN_ESTE, GeoUtils.formatCoord(lastTopoResult.este));
            values.put(DatabaseHelper.COLUMN_NORTE, GeoUtils.formatCoord(lastTopoResult.norte));
            values.put(DatabaseHelper.COLUMN_ZONA, String.valueOf(lastTopoResult.zona));
            values.put(DatabaseHelper.COLUMN_HEMISFERIO, String.valueOf(lastTopoResult.hemisferio));
            values.put(DatabaseHelper.COLUMN_ALTURA, GeoUtils.formatCoord(lastTopoResult.altEllipsoidal));
            values.put(DatabaseHelper.COLUMN_ALTURA_ORTO, GeoUtils.formatCoord(lastTopoResult.altOrto));
            values.put(DatabaseHelper.COLUMN_PRESION, GeoUtils.formatCoord(lastTopoResult.pressureMmHg));
            values.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, GeoUtils.formatFactor(lastTopoResult.scaleFactor));
            values.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, GeoUtils.formatFactor(lastTopoResult.elevationFactor));
            values.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, GeoUtils.formatFactor(lastTopoResult.combinedFactor));
            values.put(DatabaseHelper.COLUMN_MODELO_GEOIDAL, txt_man_geoid_model.getText().toString());
            values.put(DatabaseHelper.COLUMN_TIPO_REGISTRO, getString(R.string.label_reg_manual));
            
            // Unificación con Automático: Datos que no existen en manual
            values.put(DatabaseHelper.COLUMN_PRECISION, "Ninguno");
            values.put(DatabaseHelper.COLUMN_SATELITES, "Ninguno");
            
            String temp = txt_man_temp.getText().toString();
            values.put(DatabaseHelper.COLUMN_TEMPERATURA, temp.contains("--") ? "Ninguno" : temp);
        }
        
        String finalNotes = (notas == null || notas.trim().isEmpty()) ? getString(R.string.label_no_observations) : notas.trim();
        values.put(DatabaseHelper.COLUMN_NOTAS, finalNotes);
        dbHelper.insertarPunto(values);
        UIUtils.showSuccessToast(safeContext, getString(R.string.msg_point_saved_format, nombrePunto));
    }

    public void limpiar() {
        this.et_lat_dec.setText("0");
        this.et_lon_dec.setText("0");
        this.et_alt.setText("0");
        this.et_utm_este.setText("0");
        this.et_utm_norte.setText("0");
        this.et_utm_alt.setText("0");
        this.txt_man_fe.setText("0");
        this.txt_man_fe_ppm.setText("0");
        this.txt_man_fa.setText("0");
        this.txt_man_fa_ppm.setText("0");
        this.txt_man_fc.setText("0");
        this.txt_man_fc_ppm.setText("0");
        this.txt_man_alt_orto.setText("0");
        this.txt_man_geoid_undulation.setText("0");
        this.txt_man_presion.setText("0");
        this.txt_man_presion_hpa.setText("-- hPa");
        this.txt_man_temp.setText("-- °C");
    }

    private void calculateWithUtm() { calculateWithUtm(null); }
    private void calculateWithUtm(Runnable onDone) {
        try {
            double X = Double.parseDouble(et_utm_este.getText().toString());
            double Y = Double.parseDouble(et_utm_norte.getText().toString());
            double Alt = Double.parseDouble(et_utm_alt.getText().toString());
            
            // Bolivia utiliza Zonas 19, 20 y 21
            int Z = 19 + spzona.getSelectedItemPosition();
            char hem = sphemisferio.getSelectedItemPosition() == 0 ? 'S' : 'N';

            IGMCoordinate.GeoPoint gp = IGMUtmConverter.inverse(X, Y, Z, hem, IGMConstants.Ellipsoid.WGS84);
            Location loc = new Location("manual");
            loc.setLatitude(gp.lat); loc.setLongitude(gp.lon); loc.setAltitude(Alt);

            TopographyRepository.getInstance(requireContext()).calculateCompleteAsync(loc, new TopographyRepository.CalculationCallback() {
                @Override
                public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isAdded()) return;
                        lastTopoResult = res; renderManualResults(res);
                        if (onDone != null) onDone.run();
                    });
                }
                @Override public void onError(Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded()) UIUtils.showErrorToast(requireContext(), "Error: " + e.getMessage());
                    });
                }
            });
        } catch (Exception e) { UIUtils.showErrorToast(requireContext(), getString(R.string.err_invalid_values)); }
    }

    private void calculateWithGeo() { calculateWithGeo(null); }
    private void calculateWithGeo(Runnable onDone) {
        try {
            double lat = Double.parseDouble(et_lat_dec.getText().toString());
            double lon = Double.parseDouble(et_lon_dec.getText().toString());
            double Alt = Double.parseDouble(et_alt.getText().toString());
            
            Location loc = new Location("manual");
            loc.setLatitude(lat); loc.setLongitude(lon); loc.setAltitude(Alt);

            TopographyRepository.getInstance(requireContext()).calculateCompleteAsync(loc, new TopographyRepository.CalculationCallback() {
                @Override
                public void onResult(TopoCalculoManager.TopoResult res, boolean isMgb) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isAdded()) return;
                        lastTopoResult = res; renderManualResults(res);
                        if (onDone != null) onDone.run();
                    });
                }
                @Override public void onError(Exception e) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (isAdded()) UIUtils.showErrorToast(requireContext(), "Error: " + e.getMessage());
                    });
                }
            });
        } catch (Exception e) { UIUtils.showErrorToast(requireContext(), getString(R.string.err_invalid_values)); }
    }

    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        int model = prefs.getInt(MainActivity.KEY_GEOID_MODEL, 1);
        if (txt_man_geoid_model != null) txt_man_geoid_model.setText(model == 1 ? getString(R.string.opt_mgb) : getString(R.string.opt_egm96));
    }

    private void renderManualResults(TopoCalculoManager.TopoResult res) {
        txt_man_fe.setText(GeoUtils.formatFactor(res.scaleFactor));
        txt_man_fe_ppm.setText(Math.round((res.scaleFactor - 1.0) * 1000000.0) + " PPM");
        txt_man_fa.setText(GeoUtils.formatFactor(res.elevationFactor));
        txt_man_fa_ppm.setText(Math.round((res.elevationFactor - 1.0) * 1000000.0) + " PPM");
        txt_man_fc.setText(GeoUtils.formatFactor(res.combinedFactor));
        txt_man_fc_ppm.setText(Math.round((res.combinedFactor - 1.0) * 1000000.0) + " PPM");
        txt_man_alt_orto.setText(GeoUtils.formatCoord(res.altOrto));
        txt_man_geoid_undulation.setText(GeoUtils.formatCoord(res.geoidN));
        txt_man_presion.setText(GeoUtils.formatCoord(res.pressureMmHg));
        txt_man_presion_hpa.setText(String.format(Locale.US, "%.3f %s", res.pressureHpa, getString(R.string.unit_hpa)));
        txt_man_temp.setText(String.format(Locale.getDefault(), "%.1f °C", TopographyRepository.getInstance(requireContext()).getCurrentAmbientTemp()));
    }
}
