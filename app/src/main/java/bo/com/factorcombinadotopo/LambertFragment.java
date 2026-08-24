package bo.com.factorcombinadotopo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.Locale;

public class LambertFragment extends Fragment {

    private MaterialButtonToggleGroup toggleGroup;
    private EditText etLat, etLon, etX, etY;
    private LinearLayout layoutGeoInputs, layoutLambertInputs, layoutResults;
    private View resLambert, resGeo;
    private TextView txtResX, txtResY, txtResLat, txtResLon;
    private com.google.android.material.button.MaterialButton btnCopy, btnShare;

    private boolean isForward = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lambert, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toggleGroup = view.findViewById(R.id.toggleGroup);
        etLat = view.findViewById(R.id.etLat);
        etLon = view.findViewById(R.id.etLon);
        etX = view.findViewById(R.id.etX);
        etY = view.findViewById(R.id.etY);
        layoutGeoInputs = view.findViewById(R.id.layoutGeoInputs);
        layoutLambertInputs = view.findViewById(R.id.layoutLambertInputs);
        layoutResults = view.findViewById(R.id.layoutResults);
        resLambert = view.findViewById(R.id.resLambert);
        resGeo = view.findViewById(R.id.resGeo);
        txtResX = view.findViewById(R.id.txtResX);
        txtResY = view.findViewById(R.id.txtResY);
        txtResLat = view.findViewById(R.id.txtResLat);
        txtResLon = view.findViewById(R.id.txtResLon);
        btnCopy = view.findViewById(R.id.btnCopyResults);
        btnShare = view.findViewById(R.id.btnShareResults);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                isForward = (checkedId == R.id.btnForward);
                updateUIForMode();
            }
        });

        view.findViewById(R.id.btnCalculate).setOnClickListener(v -> calculate());

        view.findViewById(R.id.btn_lambert_info).setOnClickListener(v -> {
            UIUtils.showProInfoDialog(requireContext(), 
                    "PROYECCIÓN LAMBERT BOLIVIA", 
                    android.text.Html.fromHtml(getString(R.string.guide_lambert_body), android.text.Html.FROM_HTML_MODE_LEGACY), 
                    R.drawable.ic_info_round_blue);
        });

        btnCopy.setOnClickListener(v -> copyResults());
        btnShare.setOnClickListener(v -> shareResults());
    }

    private void updateUIForMode() {
        if (isForward) {
            layoutGeoInputs.setVisibility(View.VISIBLE);
            layoutLambertInputs.setVisibility(View.GONE);
            resLambert.setVisibility(View.VISIBLE);
            resGeo.setVisibility(View.GONE);
        } else {
            layoutGeoInputs.setVisibility(View.GONE);
            layoutLambertInputs.setVisibility(View.VISIBLE);
            resLambert.setVisibility(View.GONE);
            resGeo.setVisibility(View.VISIBLE);
        }
        layoutResults.setVisibility(View.GONE);
    }

    private void calculate() {
        try {
            IGMLambertConverter converter = new IGMLambertConverter(IGMConstants.Ellipsoid.WGS84);
            
            if (isForward) {
                double lat = Double.parseDouble(etLat.getText().toString());
                double lon = Double.parseDouble(etLon.getText().toString());
                IGMCoordinate.LambertPoint result = converter.forward(lat, lon);
                
                txtResX.setText(String.format(Locale.US, "%.3f", result.x));
                txtResY.setText(String.format(Locale.US, "%.3f", result.y));
            } else {
                double x = Double.parseDouble(etX.getText().toString());
                double y = Double.parseDouble(etY.getText().toString());
                IGMCoordinate.GeoPoint result = converter.inverse(x, y);
                
                txtResLat.setText(String.format(Locale.US, "%.8f", result.lat));
                txtResLon.setText(String.format(Locale.US, "%.8f", result.lon));
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
        ClipData clip = ClipData.newPlainText("Proyección Lambert", report);
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
        startActivity(Intent.createChooser(intent, "Compartir Proyección Lambert"));
    }

    private String formatResultsForExport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("      REPORTE TÉCNICO: LAMBERT BOLIVIA\n");
        sb.append("=========================================\n");
        sb.append("MODO: ").append(isForward ? "GEO -> LAMBERT" : "LAMBERT -> GEO").append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("DATOS DE ENTRADA:\n");
        if (isForward) {
            sb.append("- Latitud: ").append(etLat.getText().toString()).append("\n");
            sb.append("- Longitud: ").append(etLon.getText().toString()).append("\n");
        } else {
            sb.append("- Este (X): ").append(etX.getText().toString()).append("\n");
            sb.append("- Norte (Y): ").append(etY.getText().toString()).append("\n");
        }
        
        sb.append("-----------------------------------------\n");
        sb.append("RESULTADOS:\n");
        if (isForward) {
            sb.append("- Este (X): ").append(txtResX.getText().toString()).append("\n");
            sb.append("- Norte (Y): ").append(txtResY.getText().toString()).append("\n");
        } else {
            sb.append("- Latitud: ").append(txtResLat.getText().toString()).append("\n");
            sb.append("- Longitud: ").append(txtResLon.getText().toString()).append("\n");
        }
        sb.append("=========================================\n");
        sb.append("Generado por FactorEscalaTop");
        return sb.toString();
    }
}
