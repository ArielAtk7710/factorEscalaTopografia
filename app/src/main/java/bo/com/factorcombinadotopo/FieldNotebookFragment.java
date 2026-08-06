package bo.com.factorcombinadotopo;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FieldNotebookFragment extends Fragment {

    private EditText etEstacion, etAltIns, etPuntoRef, etAltPrisma, etPuntoAux, etEste, etNorte, etCota, etObs;
    private Spinner spTipoReg;
    private Button btnGuardar;
    private TextView btnCancelar;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_field_notebook, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());

        // Bind Views
        etEstacion = view.findViewById(R.id.et_lib_estacion);
        etAltIns = view.findViewById(R.id.et_lib_alt_ins);
        etPuntoRef = view.findViewById(R.id.et_lib_punto_ref);
        etAltPrisma = view.findViewById(R.id.et_lib_alt_prisma);
        etPuntoAux = view.findViewById(R.id.et_lib_punto_aux);
        etEste = view.findViewById(R.id.et_lib_este);
        etNorte = view.findViewById(R.id.et_lib_norte);
        etCota = view.findViewById(R.id.et_lib_cota);
        etObs = view.findViewById(R.id.et_lib_obs);
        spTipoReg = view.findViewById(R.id.sp_lib_tipo_reg);
        btnGuardar = view.findViewById(R.id.btn_lib_guardar);
        btnCancelar = view.findViewById(R.id.btn_lib_cancelar);

        // Setup Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(), 
                R.array.tipos_registro, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTipoReg.setAdapter(adapter);

        btnGuardar.setOnClickListener(v -> guardarEnLibreta());
        btnCancelar.setOnClickListener(v -> limpiarFormulario());
    }

    private void guardarEnLibreta() {
        String estacion = etEstacion.getText().toString().trim();
        String puntoAux = etPuntoAux.getText().toString().trim();

        if (estacion.isEmpty() || puntoAux.isEmpty()) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.warn_fill_required));
            return;
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_LIB_ESTACION, estacion);
        values.put(DatabaseHelper.COL_LIB_ALT_INS, etAltIns.getText().toString());
        values.put(DatabaseHelper.COL_LIB_PUNTO_REF, etPuntoRef.getText().toString());
        values.put(DatabaseHelper.COL_LIB_ALT_PRI, etAltPrisma.getText().toString());
        values.put(DatabaseHelper.COL_LIB_PUNTO_AUX, puntoAux);
        values.put(DatabaseHelper.COL_LIB_TIPO_REG, spTipoReg.getSelectedItem().toString());
        values.put(DatabaseHelper.COL_LIB_ESTE, etEste.getText().toString());
        values.put(DatabaseHelper.COL_LIB_NORTE, etNorte.getText().toString());
        values.put(DatabaseHelper.COL_LIB_COTA, etCota.getText().toString());
        values.put(DatabaseHelper.COL_LIB_OBS, etObs.getText().toString());
        
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        values.put(DatabaseHelper.COL_LIB_FECHA, timeStamp);

        long id = dbHelper.insertarLibreta(values);
        if (id != -1) {
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_saved));
            limpiarFormulario();
        } else {
            UIUtils.showErrorToast(requireContext(), getString(R.string.err_db_save));
        }
    }

    private void limpiarFormulario() {
        etEstacion.setText("");
        etAltIns.setText("");
        etPuntoRef.setText("");
        etAltPrisma.setText("");
        etPuntoAux.setText("");
        etEste.setText("");
        etNorte.setText("");
        etCota.setText("");
        etObs.setText("");
        spTipoReg.setSelection(0);
    }
}
