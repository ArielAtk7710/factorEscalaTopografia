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

import java.util.Locale;

public class DistanceReductionFragment extends Fragment {

    private Spinner spEllipsoid;
    private EditText etDistInclinada, etAltA, etAltB, etLatMedia;
    private LinearLayout layoutResults;
    private TextView txtResD1, txtResD2, txtResFactor;
    private com.google.android.material.button.MaterialButton btnCopy, btnShare;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_distance_reduction, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spEllipsoid = view.findViewById(R.id.spEllipsoid);
        etDistInclinada = view.findViewById(R.id.etDistInclinada);
        etAltA = view.findViewById(R.id.etAltA);
        etAltB = view.findViewById(R.id.etAltB);
        etLatMedia = view.findViewById(R.id.etLatMedia);
        layoutResults = view.findViewById(R.id.layoutResults);
        txtResD1 = view.findViewById(R.id.txtResD1);
        txtResD2 = view.findViewById(R.id.txtResD2);
        txtResFactor = view.findViewById(R.id.txtResFactor);
        btnCopy = view.findViewById(R.id.btnCopyResults);
        btnShare = view.findViewById(R.id.btnShareResults);

        ArrayAdapter<IGMConstants.Ellipsoid> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, IGMConstants.Ellipsoid.values());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spEllipsoid.setAdapter(adapter);

        view.findViewById(R.id.btnCalculate).setOnClickListener(v -> calculate());

        view.findViewById(R.id.btn_distance_reduction_info).setOnClickListener(v -> {
            UIUtils.showProInfoDialog(requireContext(), 
                    "REDUCCIÓN DE DISTANCIAS", 
                    android.text.Html.fromHtml(getString(R.string.guide_distance_reduction_body), android.text.Html.FROM_HTML_MODE_LEGACY), 
                    R.drawable.ic_info_round_blue);
        });

        btnCopy.setOnClickListener(v -> copyResults());
        btnShare.setOnClickListener(v -> shareResults());
    }

    private void calculate() {
        try {
            double dab = Double.parseDouble(etDistInclinada.getText().toString());
            double ha = Double.parseDouble(etAltA.getText().toString());
            double hb = Double.parseDouble(etAltB.getText().toString());
            double lat = Double.parseDouble(etLatMedia.getText().toString());
            IGMConstants.Ellipsoid ellipsoid = (IGMConstants.Ellipsoid) spEllipsoid.getSelectedItem();

            IGMDistanceReducer.Result result = IGMDistanceReducer.reduce(dab, ha, hb, Math.toRadians(lat), ellipsoid);

            txtResD1.setText(String.format(Locale.US, "%.3f m", result.d1));
            txtResD2.setText(String.format(Locale.US, "%.3f m", result.d2));
            txtResFactor.setText(String.format(Locale.US, "%.8f", result.factor));

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
        ClipData clip = ClipData.newPlainText("Reducción de Distancia", report);
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
        startActivity(Intent.createChooser(intent, "Compartir Reducción de Distancia"));
    }

    private String formatResultsForExport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("      REPORTE TÉCNICO: REDUCCIÓN\n");
        sb.append("=========================================\n");
        sb.append("ELIPSOIDE: ").append(spEllipsoid.getSelectedItem().toString()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("DATOS DE ENTRADA:\n");
        sb.append("- Dist. Inclinada: ").append(etDistInclinada.getText().toString()).append(" m\n");
        sb.append("- Altura Punto A: ").append(etAltA.getText().toString()).append(" m\n");
        sb.append("- Altura Punto B: ").append(etAltB.getText().toString()).append(" m\n");
        sb.append("- Latitud Media: ").append(etLatMedia.getText().toString()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append("RESULTADOS:\n");
        sb.append("- Dist. Horizontal: ").append(txtResD1.getText().toString()).append("\n");
        sb.append("- Dist. Elipsoidal: ").append(txtResD2.getText().toString()).append("\n");
        sb.append("- Factor Reduc.: ").append(txtResFactor.getText().toString()).append("\n");
        sb.append("=========================================\n");
        sb.append("Generado por FactorEscalaTop");
        return sb.toString();
    }
}
