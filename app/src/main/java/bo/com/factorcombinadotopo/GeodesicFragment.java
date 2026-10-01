package bo.com.factorcombinadotopo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Locale;

public class GeodesicFragment extends Fragment {

    private MaterialButtonToggleGroup toggleGroup;
    private Spinner spEllipsoid;
    private EditText etLat1, etLon1, etLat2, etLon2, etAzimuth, etDistance;
    private LinearLayout layoutPoint2, layoutDirectParams, layoutResults;
    private View resLatLon, resDistAz;
    private TextView txtResLat, txtResLon, txtResDist, txtResAzDirect, txtResAzInv;
    private com.google.android.material.button.MaterialButton btnCopy, btnShare;

    private boolean isDirectMode = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_geodesic, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleGroup = view.findViewById(R.id.toggleGroup);
        spEllipsoid = view.findViewById(R.id.spEllipsoid);
        etLat1 = view.findViewById(R.id.etLat1);
        etLon1 = view.findViewById(R.id.etLon1);
        etLat2 = view.findViewById(R.id.etLat2);
        etLon2 = view.findViewById(R.id.etLon2);
        etAzimuth = view.findViewById(R.id.etAzimuth);
        etDistance = view.findViewById(R.id.etDistance);
        layoutPoint2 = view.findViewById(R.id.layoutPoint2);
        layoutDirectParams = view.findViewById(R.id.layoutDirectParams);
        layoutResults = view.findViewById(R.id.layoutResults);
        resLatLon = view.findViewById(R.id.resLatLon);
        resDistAz = view.findViewById(R.id.resDistAz);
        txtResLat = view.findViewById(R.id.txtResLat);
        txtResLon = view.findViewById(R.id.txtResLon);
        txtResDist = view.findViewById(R.id.txtResDist);
        txtResAzDirect = view.findViewById(R.id.txtResAzDirect);
        txtResAzInv = view.findViewById(R.id.txtResAzInv);
        btnCopy = view.findViewById(R.id.btnCopyResults);
        btnShare = view.findViewById(R.id.btnShareResults);

        ArrayAdapter<IGMConstants.Ellipsoid> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, IGMConstants.Ellipsoid.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEllipsoid.setAdapter(adapter);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isDirectMode = (checkedId == R.id.btnDirect);
                updateUIForMode();
            }
        });

        view.findViewById(R.id.btnCalculate).setOnClickListener(v -> calculate());
        view.findViewById(R.id.btnClear).setOnClickListener(v -> {
            etLat1.setText("");
            etLon1.setText("");
            etLat2.setText("");
            etLon2.setText("");
            etAzimuth.setText("");
            etDistance.setText("");
            layoutResults.setVisibility(View.GONE);
        });

        view.findViewById(R.id.btn_geodesic_info).setOnClickListener(v -> {
            UIUtils.showProInfoDialog(requireContext(), 
                    "CÁLCULOS GEODÉSICOS", 
                    android.text.Html.fromHtml(getString(R.string.guide_geodesic_body), android.text.Html.FROM_HTML_MODE_LEGACY), 
                    R.drawable.ic_info_round_blue);
        });

        btnCopy.setOnClickListener(v -> copyResults());
        btnShare.setOnClickListener(v -> shareResults());
    }

    private void updateUIForMode() {
        if (isDirectMode) {
            layoutPoint2.setVisibility(View.GONE);
            layoutDirectParams.setVisibility(View.VISIBLE);
            resLatLon.setVisibility(View.VISIBLE);
            resDistAz.setVisibility(View.GONE);
        } else {
            layoutPoint2.setVisibility(View.VISIBLE);
            layoutDirectParams.setVisibility(View.GONE);
            resLatLon.setVisibility(View.GONE);
            resDistAz.setVisibility(View.VISIBLE);
        }
        layoutResults.setVisibility(View.GONE);
    }

    private void calculate() {
        try {
            double lat1 = Double.parseDouble(etLat1.getText().toString());
            double lon1 = Double.parseDouble(etLon1.getText().toString());
            IGMConstants.Ellipsoid ellipsoid = (IGMConstants.Ellipsoid) spEllipsoid.getSelectedItem();

            IGMGeodesicCalculator.GeoResult result;

            if (isDirectMode) {
                double azimuth = Double.parseDouble(etAzimuth.getText().toString());
                double distance = Double.parseDouble(etDistance.getText().toString());
                result = IGMGeodesicCalculator.vincentyDirect(lat1, lon1, azimuth, distance, ellipsoid);
                
                txtResLat.setText(String.format(Locale.US, "%.8f", result.lat));
                txtResLon.setText(String.format(Locale.US, "%.8f", result.lon));
                txtResAzInv.setText(String.format(Locale.US, "%.4f°", result.azimuthInverse));
            } else {
                double lat2 = Double.parseDouble(etLat2.getText().toString());
                double lon2 = Double.parseDouble(etLon2.getText().toString());
                result = IGMGeodesicCalculator.vincentyInverse(lat1, lon1, lat2, lon2, ellipsoid);

                txtResDist.setText(String.format(Locale.US, "%.3f m", result.distance));
                txtResAzDirect.setText(String.format(Locale.US, "%.4f°", result.azimuthDirect));
                txtResAzInv.setText(String.format(Locale.US, "%.4f°", result.azimuthInverse));
            }

            layoutResults.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Error en los datos de entrada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void copyResults() {
        if (!isAdded()) return;
        String report = formatResultsForExport();
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Cálculo Geodésico", report);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_copied_clipboard));
        }
    }

    private void shareResults() {
        if (!isAdded()) return;
        String report = formatResultsForExport();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, report);
        startActivity(Intent.createChooser(intent, "Compartir Cálculo Geodésico"));
    }

    private String formatResultsForExport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("      REPORTE TÉCNICO: GEODÉSICA\n");
        sb.append("=========================================\n");
        sb.append("MODO: ").append(isDirectMode ? "DIRECTO" : "INVERSO").append("\n");
        sb.append("ELIPSOIDE: ").append(spEllipsoid.getSelectedItem().toString()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("DATOS DE ENTRADA:\n");
        sb.append("- Latitud 1: ").append(etLat1.getText().toString()).append("\n");
        sb.append("- Longitud 1: ").append(etLon1.getText().toString()).append("\n");
        
        if (isDirectMode) {
            sb.append("- Azimut: ").append(etAzimuth.getText().toString()).append("°\n");
            sb.append("- Distancia: ").append(etDistance.getText().toString()).append(" m\n");
        } else {
            sb.append("- Latitud 2: ").append(etLat2.getText().toString()).append("\n");
            sb.append("- Longitud 2: ").append(etLon2.getText().toString()).append("\n");
        }
        
        sb.append("-----------------------------------------\n");
        sb.append("RESULTADOS:\n");
        if (isDirectMode) {
            sb.append("- Latitud 2: ").append(txtResLat.getText().toString()).append("\n");
            sb.append("- Longitud 2: ").append(txtResLon.getText().toString()).append("\n");
            sb.append("- Azimut Inv: ").append(txtResAzInv.getText().toString()).append("\n");
        } else {
            sb.append("- Distancia: ").append(txtResDist.getText().toString()).append("\n");
            sb.append("- Azimut Dir: ").append(txtResAzDirect.getText().toString()).append("\n");
            sb.append("- Azimut Inv: ").append(txtResAzInv.getText().toString()).append("\n");
        }
        sb.append("=========================================\n");
        sb.append("Generado por FactorEscalaTop");
        return sb.toString();
    }
}
