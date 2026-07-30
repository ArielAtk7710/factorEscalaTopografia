package bo.com.solucionesit.factorcombinado;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import java.text.DecimalFormat;

/* JADX INFO: loaded from: classes.dex */
public class ManualFragment extends Fragment {
    private Button btn_calcular;
    private Button btn_limpiar;
    private EditText et_alt;
    private EditText et_lat_gra;
    private EditText et_lat_min;
    private EditText et_lat_seg;
    private EditText et_lon_gra;
    private EditText et_lon_min;
    private EditText et_lon_seg;
    private EditText et_utm_alt;
    private EditText et_utm_este;
    private EditText et_utm_hemis;
    private EditText et_utm_norte;
    private EditText et_utm_zona;
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
    private View view;

    @Override // android.support.v4.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_manual, container, false);
        this.proyeccion = (Spinner) this.view.findViewById(R.id.spGeoUtm);
        this.proyeccion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() { // from class: bo.com.solucionesit.factorcombinado.ManualFragment.1
            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    ManualFragment.this.layUtm.setVisibility(0);
                    ManualFragment.this.layGeo.setVisibility(8);
                } else {
                    ManualFragment.this.layGeo.setVisibility(0);
                    ManualFragment.this.layUtm.setVisibility(8);
                }
            }

            @Override // android.widget.AdapterView.OnItemSelectedListener
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.btn_calcular = (Button) this.view.findViewById(R.id.btn_calcular);
        this.btn_calcular.setOnClickListener(new View.OnClickListener() { // from class: bo.com.solucionesit.factorcombinado.ManualFragment.2
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                switch (ManualFragment.this.proyeccion.getSelectedItemPosition()) {
                    case 0:
                        ManualFragment.this.calculateWithUtm(v);
                        break;
                    case 1:
                        ManualFragment.this.calculateWithGeo(v);
                        break;
                }
            }
        });
        this.btn_limpiar = (Button) this.view.findViewById(R.id.btn_limpiar);
        this.btn_limpiar.setOnClickListener(new View.OnClickListener() { // from class: bo.com.solucionesit.factorcombinado.ManualFragment.3
            @Override // android.view.View.OnClickListener
            public void onClick(View v) {
                ManualFragment.this.limpiar(v);
            }
        });
        return this.view;
    }

    public void limpiar(View view) {
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
    }

    @Override // android.support.v4.app.Fragment
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.proyeccion = (Spinner) getActivity().findViewById(R.id.spGeoUtm);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.opciones, android.R.layout.simple_spinner_item);
        this.proyeccion.setAdapter((SpinnerAdapter) adapter);
        this.spzona = (Spinner) getActivity().findViewById(R.id.spZona);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.zonas, android.R.layout.simple_spinner_item);
        this.spzona.setAdapter((SpinnerAdapter) adapter1);
        this.sphemisferio = (Spinner) getActivity().findViewById(R.id.spHemisferio);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getActivity().getBaseContext(), R.array.hemisferios, android.R.layout.simple_spinner_item);
        this.sphemisferio.setAdapter((SpinnerAdapter) adapter2);
        this.layGeo = (LinearLayout) getActivity().findViewById(R.id.layGeo);
        this.layUtm = (LinearLayout) getActivity().findViewById(R.id.layUtm);
        this.et_lat_gra = (EditText) getActivity().findViewById(R.id.et_lat_gra);
        this.et_lat_min = (EditText) getActivity().findViewById(R.id.et_lat_min);
        this.et_lat_seg = (EditText) getActivity().findViewById(R.id.et_lat_seg);
        this.et_lon_gra = (EditText) getActivity().findViewById(R.id.et_lon_gra);
        this.et_lon_min = (EditText) getActivity().findViewById(R.id.et_lon_min);
        this.et_lon_seg = (EditText) getActivity().findViewById(R.id.et_lon_seg);
        this.et_alt = (EditText) getActivity().findViewById(R.id.et_alt);
        this.et_utm_este = (EditText) getActivity().findViewById(R.id.et_utm_este);
        this.et_utm_norte = (EditText) getActivity().findViewById(R.id.et_utm_norte);
        this.et_utm_alt = (EditText) getActivity().findViewById(R.id.et_utm_alt);
        this.txt_man_fe = (TextView) getActivity().findViewById(R.id.txt_man_fe);
        this.txt_man_fe_ppm = (TextView) getActivity().findViewById(R.id.txt_man_fe_ppm);
        this.txt_man_fa = (TextView) getActivity().findViewById(R.id.txt_man_fa);
        this.txt_man_fa_ppm = (TextView) getActivity().findViewById(R.id.txt_man_fa_ppm);
        this.txt_man_fc = (TextView) getActivity().findViewById(R.id.txt_man_fc);
        this.txt_man_fc_ppm = (TextView) getActivity().findViewById(R.id.txt_man_fc_ppm);
    }

    public void calculateWithUtm(View view) {
        Double K = Double.valueOf(0.9996d);
        Double A = Double.valueOf(6378137.0d);
        Double B = Double.valueOf(6356752.31424518d);
        Double X = Double.valueOf(Double.parseDouble(this.et_utm_este.getText().toString()));
        Double Y = Double.valueOf(Double.parseDouble(this.et_utm_norte.getText().toString()));
        Double Alt = Double.valueOf(Double.parseDouble(this.et_utm_alt.getText().toString()));
        Integer Z = Integer.valueOf(this.spzona.getSelectedItemPosition() + 1);
        String hemisferio = this.sphemisferio.getSelectedItemPosition() == 0 ? "S" : "N";
        if (hemisferio.equals("S")) {
            Y = Double.valueOf(Y.doubleValue() - 1.0E7d);
        }
        Double M = Double.valueOf(Y.doubleValue() / K.doubleValue());
        Double.valueOf(A.doubleValue() / (A.doubleValue() - B.doubleValue()));
        Double E = Double.valueOf((Math.pow(A.doubleValue(), 2.0d) - Math.pow(B.doubleValue(), 2.0d)) / Math.pow(A.doubleValue(), 2.0d));
        Double D = Double.valueOf((Math.pow(A.doubleValue(), 2.0d) - Math.pow(B.doubleValue(), 2.0d)) / Math.pow(B.doubleValue(), 2.0d));
        Double G = Double.valueOf((1.0d - Math.sqrt(1.0d - E.doubleValue())) / (1.0d + Math.sqrt(1.0d - E.doubleValue())));
        Double Tit = Double.valueOf(M.doubleValue() / (A.doubleValue() * (((1.0d - (E.doubleValue() / 4.0d)) - ((3.0d * Math.pow(E.doubleValue(), 2.0d)) / 64.0d)) - ((5.0d * Math.pow(E.doubleValue(), 3.0d)) / 256.0d))));
        Double P = Double.valueOf(Tit.doubleValue() + ((((3.0d * G.doubleValue()) / 2.0d) - ((27.0d * Math.pow(G.doubleValue(), 3.0d)) / 32.0d)) * Math.sin(2.0d * Tit.doubleValue())) + ((((21.0d * Math.pow(G.doubleValue(), 2.0d)) / 16.0d) - ((55.0d * Math.pow(G.doubleValue(), 4.0d)) / 32.0d)) * Math.sin(4.0d * Tit.doubleValue())) + (((151.0d * Math.pow(G.doubleValue(), 3.0d)) / 96.0d) * Math.sin(6.0d * Tit.doubleValue())));
        Double Q = Double.valueOf(D.doubleValue() * Math.pow(Math.cos(P.doubleValue()), 2.0d));
        Double T = Double.valueOf(Math.pow(Math.tan(P.doubleValue()), 2.0d));
        Double N = Double.valueOf(A.doubleValue() / Math.sqrt(1.0d - (E.doubleValue() * Math.pow(Math.sin(P.doubleValue()), 2.0d))));
        Double M2 = Double.valueOf((A.doubleValue() * (1.0d - E.doubleValue())) / Math.sqrt(Math.pow(1.0d - (E.doubleValue() * Math.pow(Math.sin(P.doubleValue()), 2.0d)), 3.0d)));
        Double R = Double.valueOf((X.doubleValue() - 500000.0d) / (N.doubleValue() * K.doubleValue()));
        Double F = Double.valueOf(P.doubleValue() - (((N.doubleValue() * Math.tan(P.doubleValue())) / M2.doubleValue()) * (((Math.pow(R.doubleValue(), 2.0d) / 2.0d) - ((((((5.0d + (3.0d * T.doubleValue())) + (10.0d * Q.doubleValue())) - (4.0d * Math.pow(Q.doubleValue(), 2.0d))) - (9.0d * D.doubleValue())) * Math.pow(R.doubleValue(), 4.0d)) / 24.0d)) + (((((((61.0d + (90.0d * T.doubleValue())) + (298.0d * Q.doubleValue())) + (45.0d * Math.pow(T.doubleValue(), 2.0d))) - (252.0d * D.doubleValue())) - (3.0d * Math.pow(Q.doubleValue(), 2.0d))) * Math.pow(R.doubleValue(), 6.0d)) / 720.0d))));
        Double F2 = Double.valueOf((180.0d * F.doubleValue()) / 3.141592653589793d);
        Integer W = Integer.valueOf((Z.intValue() * 6) - 183);
        Double L = Double.valueOf(((((double) W.intValue()) * 3.141592653589793d) / 180.0d) + (((R.doubleValue() - ((((1.0d + (2.0d * T.doubleValue())) + Q.doubleValue()) * Math.pow(R.doubleValue(), 3.0d)) / 6.0d)) + (((((((5.0d - (2.0d * Q.doubleValue())) + (28.0d * T.doubleValue())) - (3.0d * Math.pow(Q.doubleValue(), 2.0d))) + (8.0d * D.doubleValue())) + (24.0d * Math.pow(T.doubleValue(), 2.0d))) * Math.pow(R.doubleValue(), 5.0d)) / 120.0d)) / Math.cos(P.doubleValue())));
        calcularManual(Double.valueOf((180.0d * L.doubleValue()) / 3.141592653589793d), F2, Alt);
    }

    public void calculateWithGeo(View view) {
        Double.valueOf(0.9996d);
        Double.valueOf(6378137.0d);
        Double.valueOf(6356752.31424518d);
        Double L = Double.valueOf(Math.abs(Double.parseDouble(this.et_lon_gra.getText().toString())) + (Double.parseDouble(this.et_lon_min.getText().toString()) / 60.0d) + ((Double.parseDouble(this.et_lon_seg.getText().toString()) / 60.0d) / 60.0d));
        Double F = Double.valueOf(Math.abs(Double.parseDouble(this.et_lat_gra.getText().toString())) + (Double.parseDouble(this.et_lat_min.getText().toString()) / 60.0d) + ((Double.parseDouble(this.et_lat_seg.getText().toString()) / 60.0d) / 60.0d));
        Double Alt = Double.valueOf(Double.parseDouble(this.et_alt.getText().toString()));
        calcularManual(L, F, Alt);
    }

    public void calcularManual(Double L, Double F, Double Alt) {
        Integer W;
        Double K = Double.valueOf(0.9996d);
        Double A = Double.valueOf(6378137.0d);
        Double B = Double.valueOf(6356752.31424518d);
        if (L.doubleValue() < 0.0d) {
            L = Double.valueOf(L.doubleValue() * (-1.0d));
        }
        if (F.doubleValue() < 0.0d) {
            F = Double.valueOf(F.doubleValue() * (-1.0d));
        }
        Integer.valueOf(0);
        Integer.valueOf(0);
        if (F.doubleValue() < 0.0d) {
        }
        if (L.doubleValue() > 0.0d) {
            Integer.valueOf(((int) Math.abs(L.doubleValue() / 6.0d)) + 31);
            W = Integer.valueOf((((int) (L.doubleValue() / 6.0d)) * 6) + 3);
        } else {
            Integer.valueOf(30 - ((int) Math.abs(L.doubleValue() / 6.0d)));
            W = Integer.valueOf((((int) (L.doubleValue() / 6.0d)) * 6) - 3);
        }
        Double.valueOf(A.doubleValue() / (A.doubleValue() - B.doubleValue()));
        Double E = Double.valueOf((Math.pow(A.doubleValue(), 2.0d) - Math.pow(B.doubleValue(), 2.0d)) / Math.pow(A.doubleValue(), 2.0d));
        Double D = Double.valueOf((Math.pow(A.doubleValue(), 2.0d) - Math.pow(B.doubleValue(), 2.0d)) / Math.pow(B.doubleValue(), 2.0d));
        Double N = Double.valueOf(A.doubleValue() / Math.sqrt(1.0d - (E.doubleValue() * Math.pow(Math.sin((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d))));
        Double T = Double.valueOf(Math.pow(Math.tan((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d));
        Double C = Double.valueOf(D.doubleValue() * Math.pow(Math.cos((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d));
        Double G = Double.valueOf((L.doubleValue() - ((double) W.intValue())) * ((Math.cos((F.doubleValue() * 3.141592653589793d) / 180.0d) * 3.141592653589793d) / 180.0d));
        Double M = Double.valueOf(A.doubleValue() * (((((((1.0d - (E.doubleValue() / 4.0d)) - ((3.0d * Math.pow(E.doubleValue(), 2.0d)) / 64.0d)) - ((5.0d * Math.pow(E.doubleValue(), 3.0d)) / 256.0d)) * ((F.doubleValue() * 3.141592653589793d) / 180.0d)) - (((((3.0d * E.doubleValue()) / 8.0d) + ((3.0d * Math.pow(E.doubleValue(), 2.0d)) / 32.0d)) + ((45.0d * Math.pow(E.doubleValue(), 3.0d)) / 1024.0d)) * Math.sin(((2.0d * F.doubleValue()) * 3.141592653589793d) / 180.0d))) + ((((15.0d * Math.pow(E.doubleValue(), 2.0d)) / 256.0d) + ((45.0d * Math.pow(E.doubleValue(), 3.0d)) / 1024.0d)) * Math.sin(((4.0d * F.doubleValue()) * 3.141592653589793d) / 180.0d))) - (((35.0d * Math.pow(E.doubleValue(), 3.0d)) / 3072.0d) * Math.sin(((6.0d * F.doubleValue()) * 3.141592653589793d) / 180.0d))));
        Double R = Double.valueOf(K.doubleValue() * N.doubleValue() * (G.doubleValue() + ((((1.0d - T.doubleValue()) + C.doubleValue()) * Math.pow(G.doubleValue(), 3.0d)) / 6.0d) + ((((((5.0d - (18.0d * T.doubleValue())) + Math.pow(T.doubleValue(), 2.0d)) + (72.0d * C.doubleValue())) - (58.0d * D.doubleValue())) * Math.pow(G.doubleValue(), 5.0d)) / 120.0d)));
        Double.valueOf(R.doubleValue() + 500000.0d);
        Double H = Double.valueOf(K.doubleValue() * (M.doubleValue() + (N.doubleValue() * Math.tan((F.doubleValue() * 3.141592653589793d) / 180.0d) * ((Math.pow(G.doubleValue(), 2.0d) / 2.0d) + (((((5.0d - T.doubleValue()) + (9.0d * C.doubleValue())) + (4.0d * Math.pow(C.doubleValue(), 2.0d))) * Math.pow(G.doubleValue(), 4.0d)) / 24.0d) + ((((((61.0d - (58.0d * T.doubleValue())) + Math.pow(T.doubleValue(), 2.0d)) + (600.0d * C.doubleValue())) - (330.0d * D.doubleValue())) * Math.pow(G.doubleValue(), 6.0d)) / 720.0d)))));
        if (H.doubleValue() < 0.0d) {
            Double.valueOf(H.doubleValue() + 1.0E7d);
        }
        Double Q = Double.valueOf(K.doubleValue() * (1.0d + (((1.0d + C.doubleValue()) * Math.pow(G.doubleValue(), 2.0d)) / 2.0d) + ((((((5.0d - (4.0d * T.doubleValue())) + (42.0d * C.doubleValue())) + (13.0d * Math.pow(C.doubleValue(), 2.0d))) - (28.0d * D.doubleValue())) * Math.pow(G.doubleValue(), 4.0d)) / 24.0d) + ((((61.0d - (148.0d * T.doubleValue())) + (16.0d * Math.pow(T.doubleValue(), 2.0d))) * Math.pow(G.doubleValue(), 6.0d)) / 720.0d)));
        Double PPMQ = Double.valueOf((-1.0d) * (1.0d - Q.doubleValue()) * 1000000.0d);
        Double MM = Double.valueOf((A.doubleValue() * (1.0d - E.doubleValue())) / Math.sqrt(Math.pow(1.0d - (E.doubleValue() * Math.pow(Math.sin((F.doubleValue() * 3.141592653589793d) / 180.0d), 2.0d)), 3.0d)));
        Double RR = Double.valueOf(Math.sqrt(MM.doubleValue() * N.doubleValue()));
        Double KH = Double.valueOf((RR.doubleValue() + Alt.doubleValue()) / (RR.doubleValue() + (2.0d * Alt.doubleValue())));
        Double PPMH = Double.valueOf((-1.0d) * (1.0d - KH.doubleValue()) * 1000000.0d);
        Double FC = Double.valueOf(KH.doubleValue() * Q.doubleValue());
        Double PPMFC = Double.valueOf(PPMQ.doubleValue() + PPMH.doubleValue());
        new DecimalFormat("#0.000");
        DecimalFormat formatterEsc = new DecimalFormat("#0.00000000");
        this.txt_man_fe.setText(formatterEsc.format(Q));
        this.txt_man_fe_ppm.setText(Integer.toString((int) Math.round(PPMQ.doubleValue())) + "ppm");
        this.txt_man_fa.setText(formatterEsc.format(KH));
        this.txt_man_fa_ppm.setText(Integer.toString((int) Math.round(PPMH.doubleValue())) + "ppm");
        this.txt_man_fc.setText(formatterEsc.format(FC));
        this.txt_man_fc_ppm.setText(Integer.toString((int) Math.round(PPMFC.doubleValue())) + "ppm");
    }
}
