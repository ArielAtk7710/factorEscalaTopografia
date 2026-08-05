package bo.com.factorcombinadotopo;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import android.os.Environment;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.text.DecimalFormat;

public class ManualFragment extends Fragment {
    private Button btn_calcular;
    private Button btn_limpiar;
    private Button btn_guardar;
    private EditText et_alt;
    private EditText et_lat_gra;
    private EditText et_lat_min;
    private EditText et_lat_seg;
    private EditText et_lon_gra;
    private EditText et_lon_min;
    private EditText et_lon_seg;
    private EditText et_utm_alt;
    private EditText et_utm_este;
    private EditText et_utm_norte;
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
    private TextView txt_man_presion;
    private TextView txt_man_presion_hpa;
    private TopoCalculoManager.TopoResult lastTopoResult;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_manual, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize layouts first
        this.layGeo = view.findViewById(R.id.layGeo);
        this.layUtm = view.findViewById(R.id.layUtm);

        // Initialize Spinners and Adapters
        this.proyeccion = view.findViewById(R.id.spGeoUtm);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.proyeccion.setAdapter(adapter);

        this.spzona = view.findViewById(R.id.spZona);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(requireContext(), R.array.zonas, android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spzona.setAdapter(adapter1);

        this.sphemisferio = view.findViewById(R.id.spHemisferio);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(requireContext(), R.array.hemisferios, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.sphemisferio.setAdapter(adapter2);

        // Initialize other views
        this.et_lat_gra = view.findViewById(R.id.et_lat_gra);
        this.et_lat_min = view.findViewById(R.id.et_lat_min);
        this.et_lat_seg = view.findViewById(R.id.et_lat_seg);
        this.et_lon_gra = view.findViewById(R.id.et_lon_gra);
        this.et_lon_min = view.findViewById(R.id.et_lon_min);
        this.et_lon_seg = view.findViewById(R.id.et_lon_seg);
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
        this.txt_man_presion = view.findViewById(R.id.txt_man_presion);
        this.txt_man_presion_hpa = view.findViewById(R.id.txt_man_presion_hpa);

        // Set listeners
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

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        this.btn_calcular = view.findViewById(R.id.btn_calcular);
        this.btn_calcular.setOnClickListener(v -> {
            switch (proyeccion.getSelectedItemPosition()) {
                case 0:
                    calculateWithUtm();
                    break;
                case 1:
                    calculateWithGeo();
                    break;
            }
        });

        this.btn_limpiar = view.findViewById(R.id.btn_limpiar);
        this.btn_limpiar.setOnClickListener(this::limpiar);

        this.btn_guardar = view.findViewById(R.id.btn_guardar);
        this.btn_guardar.setOnClickListener(v -> {
            guardarResultados();
        });
    }

    private void guardarResultados() {
        if (txt_man_fe.getText().toString().equals("0") && txt_man_fc.getText().toString().equals("0")) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_no_registered_points)); 
            return;
        }

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_save_point, null);
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setView(dialogView);

        EditText etPointName = dialogView.findViewById(R.id.et_point_name);
        EditText etPointNotes = dialogView.findViewById(R.id.et_point_notes);
        Button btnSave = dialogView.findViewById(R.id.btn_dialog_save);
        Button btnCancel = dialogView.findViewById(R.id.btn_dialog_cancel);

        btnSave.setOnClickListener(v -> {
            String pointName = etPointName.getText().toString().trim();
            String pointNotes = etPointNotes.getText().toString().trim();
            if (pointName.isEmpty()) {
                etPointName.setError(getString(R.string.hint_point_name));
                return;
            }
            ejecutarGuardadoManual(pointName, pointNotes);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void ejecutarGuardadoManual(String nombrePunto, String notas) {
        if (!isAdded()) return;
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(requireContext());
        ContentValues values = new ContentValues();
        
        // 1. Obtener Hora Local Exacta
        String timeStampLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        DecimalFormat df9 = new DecimalFormat("#0.000000000");
        DecimalFormat df3 = new DecimalFormat("#0.000");

        // 2. Mapear datos técnicos completos (Consistencia con Automático)
        values.put(DatabaseHelper.COLUMN_FECHA, timeStampLocal);
        values.put(DatabaseHelper.COLUMN_NOMBRE, nombrePunto);
        
        if (lastTopoResult != null) {
            double lat = Math.abs(lastTopoResult.lat);
            double lon = Math.abs(lastTopoResult.lon);
            String latStr = (lastTopoResult.lat < 0 ? "-" : "") + (int)lat + "º " + (int)((lat-(int)lat)*60) + "' " + df3.format(((lat-(int)lat)*60 - (int)((lat-(int)lat)*60))*60) + "''";
            String lonStr = (lastTopoResult.lon < 0 ? "-" : "") + (int)lon + "º " + (int)((lon-(int)lon)*60) + "' " + df3.format(((lon-(int)lon)*60 - (int)((lon-(int)lon)*60))*60) + "''";

            values.put(DatabaseHelper.COLUMN_LATITUD, latStr);
            values.put(DatabaseHelper.COLUMN_LONGITUD, lonStr);
            values.put(DatabaseHelper.COLUMN_ESTE, df3.format(lastTopoResult.este));
            values.put(DatabaseHelper.COLUMN_NORTE, df3.format(lastTopoResult.norte));
            values.put(DatabaseHelper.COLUMN_ZONA, String.valueOf(lastTopoResult.zona));
            values.put(DatabaseHelper.COLUMN_HEMISFERIO, String.valueOf(lastTopoResult.hemisferio));
            values.put(DatabaseHelper.COLUMN_ALTURA, df3.format(lastTopoResult.altOrto + lastTopoResult.geoidN));
            values.put(DatabaseHelper.COLUMN_ALTURA_ORTO, df3.format(lastTopoResult.altOrto));
            
            // Guardar formato dual de presión para el reporte
            String dualPressure = String.format(Locale.US, "%.3f mmHg | %.3f hPa", 
                lastTopoResult.pressureMmHg, lastTopoResult.pressureHpa);
            values.put(DatabaseHelper.COLUMN_PRESION, dualPressure);
            
            values.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, df9.format(lastTopoResult.scaleFactor));
            values.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, df9.format(lastTopoResult.elevationFactor));
            
            // Guardar Factor Combinado con su PPM para el reporte
            long ppm = Math.round((lastTopoResult.combinedFactor - 1.0) * 1000000.0);
            String fcWithPpm = df9.format(lastTopoResult.combinedFactor) + " (" + ppm + " PPM)";
            values.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, fcWithPpm);
        } else {
            values.put(DatabaseHelper.COLUMN_LATITUD, "");
            values.put(DatabaseHelper.COLUMN_LONGITUD, "");
            values.put(DatabaseHelper.COLUMN_ESTE, "");
            values.put(DatabaseHelper.COLUMN_NORTE, "");
            values.put(DatabaseHelper.COLUMN_ALTURA, "");
            values.put(DatabaseHelper.COLUMN_ALTURA_ORTO, "");
            values.put(DatabaseHelper.COLUMN_PRESION, "");
            values.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, "");
            values.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, "");
            values.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, "");
        }
        
        // 3. Manejo de Notas Vacías
        String finalNotes = (notas == null || notas.trim().isEmpty()) ? getString(R.string.label_no_observations) : notas.trim();
        values.put(DatabaseHelper.COLUMN_NOTAS, finalNotes);
        
        // 4. Guardar
        dbHelper.insertarPunto(values);
        UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_saved_format, nombrePunto));
    }

    public void limpiar(View v) {
        switch (this.proyeccion.getSelectedItemPosition()) {
            case 0:
                this.et_utm_este.setText("0");
                this.et_utm_norte.setText("0");
                this.et_utm_alt.setText("0");
                break;
            case 1:
                this.et_lat_gra.setText("0");
                this.et_lat_min.setText("0");
                this.et_lat_seg.setText("0");
                this.et_lon_gra.setText("0");
                this.et_lon_min.setText("0");
                this.et_lon_seg.setText("0");
                this.et_alt.setText("0");
                break;
        }
        this.txt_man_fe.setText("0");
        this.txt_man_fe_ppm.setText("0");
        this.txt_man_fa.setText("0");
        this.txt_man_fa_ppm.setText("0");
        this.txt_man_fc.setText("0");
        this.txt_man_fc_ppm.setText("0");
        this.txt_man_alt_orto.setText("0");
        this.txt_man_presion.setText("0");
        this.txt_man_presion_hpa.setText("-- hPa");
    }

    private void calculateWithUtm() {
        try {
            String esteStr = this.et_utm_este.getText().toString();
            String norteStr = this.et_utm_norte.getText().toString();
            String altStr = this.et_utm_alt.getText().toString();

            if (esteStr.isEmpty() || norteStr.isEmpty() || altStr.isEmpty()) {
                UIUtils.showWarningToast(requireContext(), getString(R.string.msg_manual_entry_hint));
                return;
            }

            Double X = Double.parseDouble(esteStr);
            Double Y = Double.parseDouble(norteStr);
            Double Alt = Double.parseDouble(altStr);
            
            int Z = this.spzona.getSelectedItemPosition() + 1;
            String hemisferio = this.sphemisferio.getSelectedItemPosition() == 0 ? "S" : "N";

            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            float offset = prefs.getFloat("PressureOffset", 0f);

            lastTopoResult = TopoCalculoManager.calculateFromUtm(X, Y, Z, hemisferio, Alt, offset);
            renderManualResults(lastTopoResult);
            
        } catch (NumberFormatException e) {
            UIUtils.showErrorToast(requireContext(), getString(R.string.err_invalid_values));
        }
    }

    private void calculateWithGeo() {
        try {
            String latGra = this.et_lat_gra.getText().toString();
            String latMin = this.et_lat_min.getText().toString();
            String latSeg = this.et_lat_seg.getText().toString();
            String lonGra = this.et_lon_gra.getText().toString();
            String lonMin = this.et_lon_min.getText().toString();
            String lonSeg = this.et_lon_seg.getText().toString();
            String altStr = this.et_alt.getText().toString();

            if (latGra.isEmpty() || latMin.isEmpty() || latSeg.isEmpty() || 
                lonGra.isEmpty() || lonMin.isEmpty() || lonSeg.isEmpty() || altStr.isEmpty()) {
                UIUtils.showWarningToast(requireContext(), getString(R.string.msg_manual_entry_hint));
                return;
            }

            double lonDec = Math.abs(Double.parseDouble(lonGra)) + (Double.parseDouble(lonMin) / 60.0d) + ((Double.parseDouble(lonSeg) / 3600.0d));
            double latDec = Math.abs(Double.parseDouble(latGra)) + (Double.parseDouble(latMin) / 60.0d) + ((Double.parseDouble(latSeg) / 3600.0d));
            
            if (Double.parseDouble(lonGra) < 0) lonDec *= -1;
            if (Double.parseDouble(latGra) < 0) latDec *= -1;
            
            Double Alt = Double.parseDouble(altStr);
            
            double geoidN = GeoidManager.getGeoidUndulation(latDec, lonDec);
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            float offset = prefs.getFloat("PressureOffset", 0f);
            
            lastTopoResult = TopoCalculoManager.calculateAll(latDec, lonDec, Alt, geoidN, offset);
            renderManualResults(lastTopoResult);

        } catch (NumberFormatException e) {
            UIUtils.showErrorToast(requireContext(), getString(R.string.err_invalid_values));
        }
    }

    private void renderManualResults(TopoCalculoManager.TopoResult res) {
        DecimalFormat formatterEsc = new DecimalFormat("#0.000000000");
        DecimalFormat formatter = new DecimalFormat("#0.000");

        this.txt_man_fe.setText(formatterEsc.format(res.scaleFactor));
        this.txt_man_fe_ppm.setText(Math.round((res.scaleFactor - 1.0) * 1000000.0) + " PPM");
        this.txt_man_fa.setText(formatterEsc.format(res.elevationFactor));
        this.txt_man_fa_ppm.setText(Math.round((res.elevationFactor - 1.0) * 1000000.0) + " PPM");
        this.txt_man_fc.setText(formatterEsc.format(res.combinedFactor));
        this.txt_man_fc_ppm.setText(Math.round((res.combinedFactor - 1.0) * 1000000.0) + " PPM");
        this.txt_man_alt_orto.setText(formatter.format(res.altOrto));
        
        this.txt_man_presion.setText(String.format(Locale.getDefault(), "%.3f", res.pressureMmHg));
        this.txt_man_presion_hpa.setText(String.format(Locale.getDefault(), "%.3f %s", 
            res.pressureHpa, getString(R.string.unit_hpa)));
    }
}
