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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LineCalculatorFragment extends Fragment {

    private Spinner spZone, spHemisphere;
    private EditText etEast1, etNorth1, etEast2, etNorth2;
    private LinearLayout layoutResults;
    private TextView txtResDist, txtResAzimuth, txtResConv1, txtResConv2;
    private com.google.android.material.button.MaterialButton btnCopy, btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_line_calculator, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spZone = view.findViewById(R.id.spZone);
        spHemisphere = view.findViewById(R.id.spHemisphere);
        etEast1 = view.findViewById(R.id.etEast1);
        etNorth1 = view.findViewById(R.id.etNorth1);
        etEast2 = view.findViewById(R.id.etEast2);
        etNorth2 = view.findViewById(R.id.etNorth2);
        layoutResults = view.findViewById(R.id.layoutResults);
        txtResDist = view.findViewById(R.id.txtResDist);
        txtResAzimuth = view.findViewById(R.id.txtResAzimuth);
        txtResConv1 = view.findViewById(R.id.txtResConv1);
        txtResConv2 = view.findViewById(R.id.txtResConv2);
        btnCopy = view.findViewById(R.id.btnCopyResults);
        btnShare = view.findViewById(R.id.btnShareResults);

        List<Integer> zones = new ArrayList<>();
        for (int i = 1; i <= 60; i++) zones.add(i);
        ArrayAdapter<Integer> zoneAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, zones);
        zoneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spZone.setAdapter(zoneAdapter);
        spZone.setSelection(18); // Zona 19 por defecto (Bolivia)

        String[] hemispheres = {"Norte", "Sur"};
        ArrayAdapter<String> hemiAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, hemispheres);
        hemiAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spHemisphere.setAdapter(hemiAdapter);
        spHemisphere.setSelection(1); // Sur por defecto

        view.findViewById(R.id.btnCalculate).setOnClickListener(v -> calculate());

        view.findViewById(R.id.btn_line_calculator_info).setOnClickListener(v -> {
            UIUtils.showProInfoDialog(requireContext(), 
                    "LÍNEA ENTRE PUNTOS UTM", 
                    android.text.Html.fromHtml(getString(R.string.guide_line_calculator_body), android.text.Html.FROM_HTML_MODE_LEGACY), 
                    R.drawable.ic_info_round_blue);
        });

        btnCopy.setOnClickListener(v -> copyResults());
        btnShare.setOnClickListener(v -> shareResults());
    }

    private void calculate() {
        try {
            double e1 = Double.parseDouble(etEast1.getText().toString());
            double n1 = Double.parseDouble(etNorth1.getText().toString());
            double e2 = Double.parseDouble(etEast2.getText().toString());
            double n2 = Double.parseDouble(etNorth2.getText().toString());
            int zone = (int) spZone.getSelectedItem();
            String hemi = spHemisphere.getSelectedItem().toString().equals("Norte") ? "N" : "S";

            IGMLineCalculator.LineResult result = IGMLineCalculator.calculate(e1, n1, e2, n2, zone, hemi);

            txtResDist.setText(String.format(Locale.US, "%.3f m", result.distance));
            txtResAzimuth.setText(String.format(Locale.US, "%.4f°", result.azimuth));
            txtResConv1.setText(String.format(Locale.US, "%.4f°", result.convergence1));
            txtResConv2.setText(String.format(Locale.US, "%.4f°", result.convergence2));

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
        ClipData clip = ClipData.newPlainText("Línea UTM", report);
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
        startActivity(Intent.createChooser(intent, "Compartir Línea UTM"));
    }

    private String formatResultsForExport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("      REPORTE TÉCNICO: LÍNEA UTM\n");
        sb.append("=========================================\n");
        sb.append("ZONA: ").append(spZone.getSelectedItem().toString()).append(" | HEMISFERIO: ").append(spHemisphere.getSelectedItem().toString()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("DATOS DE ENTRADA:\n");
        sb.append("PUNTO 1:\n");
        sb.append("- Este: ").append(etEast1.getText().toString()).append("\n");
        sb.append("- Norte: ").append(etNorth1.getText().toString()).append("\n");
        sb.append("PUNTO 2:\n");
        sb.append("- Este: ").append(etEast2.getText().toString()).append("\n");
        sb.append("- Norte: ").append(etNorth2.getText().toString()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("RESULTADOS:\n");
        sb.append("- Distancia Plana: ").append(txtResDist.getText().toString()).append("\n");
        sb.append("- Acimut Cuadríc.: ").append(txtResAzimuth.getText().toString()).append("\n");
        sb.append("- Convergencia P1: ").append(txtResConv1.getText().toString()).append("\n");
        sb.append("- Convergencia P2: ").append(txtResConv2.getText().toString()).append("\n");
        sb.append("=========================================\n");
        sb.append("Generado por FactorEscalaTop");
        return sb.toString();
    }
}
