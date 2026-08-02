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
            UIUtils.showWarningToast(requireContext(), "No hay resultados para guardar");
            return;
        }

        // 1. Guardar en Base de Datos (Mismo esquema que el Automático)
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NOMBRE, "MANUAL_" + new SimpleDateFormat("HHmm", Locale.getDefault()).format(new Date()));
        
        // Determinar coordenadas según el modo
        if (proyeccion.getSelectedItemPosition() == 0) {
            values.put(DatabaseHelper.COLUMN_ESTE, et_utm_este.getText().toString());
            values.put(DatabaseHelper.COLUMN_NORTE, et_utm_norte.getText().toString());
            values.put(DatabaseHelper.COLUMN_ZONA, spzona.getSelectedItem().toString());
            values.put(DatabaseHelper.COLUMN_HEMISFERIO, sphemisferio.getSelectedItem().toString());
            // En modo UTM no tenemos Lat/Lon directos en campos, pero el motor los calcula internamente.
            // Para el registro manual guardaremos lo que tenemos.
            values.put(DatabaseHelper.COLUMN_LATITUD, "N/A (UTM)");
            values.put(DatabaseHelper.COLUMN_LONGITUD, "N/A (UTM)");
            values.put(DatabaseHelper.COLUMN_ALTURA, et_utm_alt.getText().toString());
        } else {
            values.put(DatabaseHelper.COLUMN_LATITUD, et_lat_gra.getText().toString() + "º " + et_lat_min.getText().toString() + "' " + et_lat_seg.getText().toString() + "''");
            values.put(DatabaseHelper.COLUMN_LONGITUD, et_lon_gra.getText().toString() + "º " + et_lon_min.getText().toString() + "' " + et_lon_seg.getText().toString() + "''");
            values.put(DatabaseHelper.COLUMN_ALTURA, et_alt.getText().toString());
            values.put(DatabaseHelper.COLUMN_ESTE, "N/A (GEO)");
            values.put(DatabaseHelper.COLUMN_NORTE, "N/A (GEO)");
            values.put(DatabaseHelper.COLUMN_ZONA, "-");
            values.put(DatabaseHelper.COLUMN_HEMISFERIO, "-");
        }

        values.put(DatabaseHelper.COLUMN_ALTURA_ORTO, txt_man_alt_orto.getText().toString());
        values.put(DatabaseHelper.COLUMN_PRESION, txt_man_presion.getText().toString() + " mmHg");
        values.put(DatabaseHelper.COLUMN_FACTOR_ESCALA, txt_man_fe.getText().toString());
        values.put(DatabaseHelper.COLUMN_FACTOR_ALTURA, txt_man_fa.getText().toString());
        values.put(DatabaseHelper.COLUMN_FACTOR_COMBINADO, txt_man_fc.getText().toString());
        
        dbHelper.insertarPunto(values);

        // 2. Exportar a TXT
        StringBuilder sb = new StringBuilder();
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        
        sb.append("--- REPORTE DE FACTORES DE CORRECCIÓN (MANUAL) ---\n");
        sb.append("Fecha: ").append(timeStamp).append("\n\n");
        
        if (proyeccion.getSelectedItemPosition() == 0) {
            sb.append("DATOS DE ENTRADA (UTM):\n");
            sb.append("Este: ").append(et_utm_este.getText().toString()).append(" m\n");
            sb.append("Norte: ").append(et_utm_norte.getText().toString()).append(" m\n");
            sb.append("Altura Elipsoidal: ").append(et_utm_alt.getText().toString()).append(" m\n");
            sb.append("Zona: ").append(spzona.getSelectedItem().toString()).append("\n");
            sb.append("Hemisferio: ").append(sphemisferio.getSelectedItem().toString()).append("\n\n");
        } else {
            sb.append("DATOS DE ENTRADA (GEODÉSICAS):\n");
            sb.append("Latitud: ").append(et_lat_gra.getText().toString()).append("º ")
              .append(et_lat_min.getText().toString()).append("' ")
              .append(et_lat_seg.getText().toString()).append("''\n");
            sb.append("Longitud: ").append(et_lon_gra.getText().toString()).append("º ")
              .append(et_lon_min.getText().toString()).append("' ")
              .append(et_lon_seg.getText().toString()).append("''\n");
            sb.append("Altura Elipsoidal: ").append(et_alt.getText().toString()).append(" m\n\n");
        }

        sb.append("DATOS CALCULADOS:\n");
        sb.append("Altura Ortométrica: ").append(txt_man_alt_orto.getText().toString()).append(" m\n");
        sb.append("Presión Estimada: ").append(txt_man_presion.getText().toString()).append(" mmHg\n\n");

        sb.append("FACTORES DE CORRECCIÓN GEOMÉTRICA:\n");
        sb.append("Factor de Escala: ").append(txt_man_fe.getText().toString())
          .append(" (").append(txt_man_fe_ppm.getText().toString()).append(")\n");
        sb.append("Factor de Altura: ").append(txt_man_fa.getText().toString())
          .append(" (").append(txt_man_fa_ppm.getText().toString()).append(")\n");
        sb.append("FACTOR COMBINADO: ").append(txt_man_fc.getText().toString())
          .append(" (").append(txt_man_fc_ppm.getText().toString()).append(")\n");
        sb.append("\nDesarrollado por FactorEscalaTop\n");
        sb.append("------------------------------------------\n");

        String fileName = "Calculo_Manual_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".txt";
        FileUtils.savePublicTxtFile(requireContext(), fileName, sb.toString());
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
    }

    private void calculateWithUtm() {
        try {
            Double K = 0.9996d;
            Double A = 6378137.0d;
            Double B = 6356752.31424518d;
            
            String esteStr = this.et_utm_este.getText().toString();
            String norteStr = this.et_utm_norte.getText().toString();
            String altStr = this.et_utm_alt.getText().toString();

            if (esteStr.isEmpty() || norteStr.isEmpty() || altStr.isEmpty()) {
                UIUtils.showWarningToast(requireContext(), "Por favor, complete todos los campos UTM");
                return;
            }

            Double X = Double.parseDouble(esteStr);
            Double Y = Double.parseDouble(norteStr);
            Double Alt = Double.parseDouble(altStr);
            
            int Z = this.spzona.getSelectedItemPosition() + 1;
            String hemisferio = this.sphemisferio.getSelectedItemPosition() == 0 ? "S" : "N";
            if (hemisferio.equals("S")) {
                Y = Y - 1.0E7d;
            }
            Double M = Y / K;
            Double E = (Math.pow(A, 2.0d) - Math.pow(B, 2.0d)) / Math.pow(A, 2.0d);
            Double D = (Math.pow(A, 2.0d) - Math.pow(B, 2.0d)) / Math.pow(B, 2.0d);
            Double G = (1.0d - Math.sqrt(1.0d - E)) / (1.0d + Math.sqrt(1.0d - E));
            Double Tit = M / (A * (((1.0d - (E / 4.0d)) - ((3.0d * Math.pow(E, 2.0d)) / 64.0d)) - ((5.0d * Math.pow(E, 3.0d)) / 256.0d)));
            Double P = Tit + ((((3.0d * G) / 2.0d) - ((27.0d * Math.pow(G, 3.0d)) / 32.0d)) * Math.sin(2.0d * Tit)) + ((((21.0d * Math.pow(G, 2.0d)) / 16.0d) - ((55.0d * Math.pow(G, 4.0d)) / 32.0d)) * Math.sin(4.0d * Tit)) + (((151.0d * Math.pow(G, 3.0d)) / 96.0d) * Math.sin(6.0d * Tit));
            Double Q = D * Math.pow(Math.cos(P), 2.0d);
            Double T = Math.pow(Math.tan(P), 2.0d);
            Double N = A / Math.sqrt(1.0d - (E * Math.pow(Math.sin(P), 2.0d)));
            Double M2 = (A * (1.0d - E)) / Math.sqrt(Math.pow(1.0d - (E * Math.pow(Math.sin(P), 2.0d)), 3.0d));
            Double R = (X - 500000.0d) / (N * K);
            Double F = P - (((N * Math.tan(P)) / M2) * (((Math.pow(R, 2.0d) / 2.0d) - ((((((5.0d + (3.0d * T)) + (10.0d * Q)) - (4.0d * Math.pow(Q, 2.0d))) - (9.0d * D)) * Math.pow(R, 4.0d)) / 24.0d)) + (((((((61.0d + (90.0d * T)) + (298.0d * Q)) + (45.0d * Math.pow(T, 2.0d))) - (252.0d * D)) - (3.0d * Math.pow(Q, 2.0d))) * Math.pow(R, 6.0d)) / 720.0d)));
            Double F2 = (180.0d * F) / 3.141592653589793d;
            int W = (Z * 6) - 183;
            Double L = ((((double) W) * 3.141592653589793d) / 180.0d) + (((R - ((((1.0d + (2.0d * T)) + Q) * Math.pow(R, 3.0d)) / 6.0d)) + (((((((5.0d - (2.0d * Q)) + (28.0d * T)) - (3.0d * Math.pow(Q, 2.0d))) + (8.0d * D)) + (24.0d * Math.pow(T, 2.0d))) * Math.pow(R, 5.0d)) / 120.0d)) / Math.cos(P));
            calcularManual((180.0d * L) / 3.141592653589793d, F2, Alt);
        } catch (NumberFormatException e) {
            UIUtils.showErrorToast(requireContext(), "Error: Ingrese valores numéricos válidos");
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
                UIUtils.showWarningToast(requireContext(), "Por favor, complete todos los campos Geodésicos");
                return;
            }

            Double L = Math.abs(Double.parseDouble(lonGra)) + (Double.parseDouble(lonMin) / 60.0d) + ((Double.parseDouble(lonSeg) / 60.0d) / 60.0d);
            Double F = Math.abs(Double.parseDouble(latGra)) + (Double.parseDouble(latMin) / 60.0d) + ((Double.parseDouble(latSeg) / 60.0d) / 60.0d);
            Double Alt = Double.parseDouble(altStr);
            calcularManual(L, F, Alt);
        } catch (NumberFormatException e) {
            UIUtils.showErrorToast(requireContext(), "Error: Ingrese valores numéricos válidos");
        }
    }

    public void calcularManual(Double L, Double F, Double Alt) {
        int W;
        Double K = 0.9996d;
        Double A = 6378137.0d;
        Double B = 6356752.31424518d;
        if (L < 0.0d) { L = L * (-1.0d); }
        if (F < 0.0d) { F = F * (-1.0d); }
        if (L > 0.0d) {
            W = (((int) (L / 6.0d)) * 6) + 3;
        } else {
            W = (((int) (L / 6.0d)) * 6) - 3;
        }
        Double E = (Math.pow(A, 2.0d) - Math.pow(B, 2.0d)) / Math.pow(A, 2.0d);
        Double D = (Math.pow(A, 2.0d) - Math.pow(B, 2.0d)) / Math.pow(B, 2.0d);
        Double N = A / Math.sqrt(1.0d - (E * Math.pow(Math.sin((F * 3.141592653589793d) / 180.0d), 2.0d)));
        Double T = Math.pow(Math.tan((F * 3.141592653589793d) / 180.0d), 2.0d);
        Double C = D * Math.pow(Math.cos((F * 3.141592653589793d) / 180.0d), 2.0d);
        Double G = (L - ((double) W)) * ((Math.cos((F * 3.141592653589793d) / 180.0d) * 3.141592653589793d) / 180.0d);
        Double M = A * (((((((1.0d - (E / 4.0d)) - ((3.0d * Math.pow(E, 2.0d)) / 64.0d)) - ((5.0d * Math.pow(E, 3.0d)) / 256.0d)) * ((F * 3.141592653589793d) / 180.0d)) - (((((3.0d * E) / 8.0d) + ((3.0d * Math.pow(E, 2.0d)) / 32.0d)) + ((45.0d * Math.pow(E, 3.0d)) / 1024.0d)) * Math.sin(((2.0d * F) * 3.141592653589793d) / 180.0d))) + ((((15.0d * Math.pow(E, 2.0d)) / 256.0d) + ((45.0d * Math.pow(E, 3.0d)) / 1024.0d)) * Math.sin(((4.0d * F) * 3.141592653589793d) / 180.0d))) - (((35.0d * Math.pow(E, 3.0d)) / 3072.0d) * Math.sin(((6.0d * F) * 3.141592653589793d) / 180.0d)));
        Double Q = K * (1.0d + (((1.0d + C) * Math.pow(G, 2.0d)) / 2.0d) + ((((((5.0d - (4.0d * T)) + (42.0d * C)) + (13.0d * Math.pow(C, 2.0d))) - (28.0d * D)) * Math.pow(G, 4.0d)) / 24.0d) + ((((61.0d - (148.0d * T)) + (16.0d * Math.pow(T, 2.0d))) * Math.pow(G, 6.0d)) / 720.0d));
        Double PPMQ = (-1.0d) * (1.0d - Q) * 1000000.0d;
        Double MM = (A * (1.0d - E)) / Math.sqrt(Math.pow(1.0d - (E * Math.pow(Math.sin((F * 3.141592653589793d) / 180.0d), 2.0d)), 3.0d));
        Double RR = Math.sqrt(MM * N);
        Double KH = (RR + Alt) / (RR + (2.0d * Alt));
        Double PPMH = (-1.0d) * (1.0d - KH) * 1000000.0d;
        Double FC = KH * Q;
        Double PPMFC = PPMQ + PPMH;
        DecimalFormat formatterEsc = new DecimalFormat("#0.00000000");
        DecimalFormat formatter = new DecimalFormat("#0.00");

        double N_geoid = GeoidManager.getGeoidUndulation(F, L);
        double altOrto = Alt - N_geoid;
        double presionMmHg = GeoidManager.calculatePressureMmHg(altOrto);

        this.txt_man_fe.setText(formatterEsc.format(Q));
        this.txt_man_fe_ppm.setText(Math.round(PPMQ) + "ppm");
        this.txt_man_fa.setText(formatterEsc.format(KH));
        this.txt_man_fa_ppm.setText(Math.round(PPMH) + "ppm");
        this.txt_man_fc.setText(formatterEsc.format(FC));
        this.txt_man_fc_ppm.setText(Math.round(PPMFC) + "ppm");
        this.txt_man_alt_orto.setText(formatter.format(altOrto));
        this.txt_man_presion.setText(String.format(Locale.getDefault(), "%.1f", presionMmHg));
    }
}