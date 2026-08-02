package bo.com.factorcombinadotopo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeMap;

public class RegisterFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtNoData;
    private PuntosAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Punto> puntosList = new ArrayList<>();
    
    // Header views
    private ImageView btnExportAll, btnExportSelected, btnDelete;
    private ImageView btnCancelSelection;
    private TextView btnConfirmDelete;
    
    private boolean isSelectionMode = false;
    private Set<Integer> selectedIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = new DatabaseHelper(requireContext());
        recyclerView = view.findViewById(R.id.rv_puntos);
        txtNoData = view.findViewById(R.id.txt_no_data);
        
        btnExportAll = view.findViewById(R.id.btn_header_export_all);
        btnExportSelected = view.findViewById(R.id.btn_header_export_selected);
        btnDelete = view.findViewById(R.id.btn_header_delete);
        btnConfirmDelete = view.findViewById(R.id.btn_confirm_delete);
        btnCancelSelection = view.findViewById(R.id.btn_cancel_selection);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PuntosAdapter(puntosList);
        recyclerView.setAdapter(adapter);

        // Exportar Todo (Visible siempre que no estemos seleccionando)
        btnExportAll.setOnClickListener(v -> exportarHistorialTxt(puntosList, "Completo"));

        // Exportar Selección (Activa modo selección si no está activo)
        btnExportSelected.setOnClickListener(v -> {
            if (!isSelectionMode) {
                toggleSelectionMode();
                UIUtils.showInfoToast(requireContext(), "Seleccione puntos para exportar");
            } else {
                if (selectedIds.isEmpty()) {
                    UIUtils.showWarningToast(requireContext(), "No hay puntos seleccionados");
                    return;
                }
                exportarSeleccionados();
            }
        });

        // Eliminar (Activa modo selección si no está activo)
        btnDelete.setOnClickListener(v -> {
            if (!isSelectionMode) {
                toggleSelectionMode();
                UIUtils.showInfoToast(requireContext(), "Seleccione puntos para borrar");
            } else {
                toggleSelectionMode(); // Simplemente sale del modo si ya estaba en él (como un botón de toggle)
            }
        });

        btnConfirmDelete.setOnClickListener(v -> {
            if (selectedIds.isEmpty()) {
                toggleSelectionMode();
                return;
            }
            eliminarSeleccionados();
        });

        btnCancelSelection.setOnClickListener(v -> toggleSelectionMode());

        cargarPuntos();
    }

    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        selectedIds.clear();
        
        // El de "Exportar Todo" se queda visible o no según prefieras, lo ocultaremos para dar foco
        btnExportAll.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
        
        // Estilo del de Exportar Selección
        btnExportSelected.setColorFilter(requireContext().getColor(isSelectionMode ? R.color.accent_orange : R.color.text_secondary));
        
        // Estilo de la Papelera
        btnDelete.setColorFilter(requireContext().getColor(isSelectionMode ? R.color.state_error : R.color.text_secondary));
        
        // Botones de acción final
        btnConfirmDelete.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        btnCancelSelection.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        
        adapter.notifyDataSetChanged();
    }

    private void eliminarSeleccionados() {
        for (int id : selectedIds) {
            dbHelper.eliminarPunto(id);
        }
        UIUtils.showSuccessToast(requireContext(), "Registros eliminados: " + selectedIds.size());
        toggleSelectionMode();
        cargarPuntos();
    }

    private void exportarSeleccionados() {
        List<Punto> seleccionados = new ArrayList<>();
        for (Punto p : puntosList) {
            if (selectedIds.contains(p.id)) {
                seleccionados.add(p);
            }
        }
        exportarHistorialTxt(seleccionados, "Seleccion");
        toggleSelectionMode();
    }

    private void exportarHistorialTxt(List<Punto> lista, String sufijo) {
        if (lista.isEmpty()) {
            UIUtils.showWarningToast(requireContext(), "No hay datos para exportar");
            return;
        }

        // Agrupar por fecha
        TreeMap<String, List<Punto>> agrupados = new TreeMap<>();
        for (Punto p : lista) {
            String fechaKey = p.fecha.split(" ")[0];
            if (!agrupados.containsKey(fechaKey)) {
                agrupados.put(fechaKey, new ArrayList<>());
            }
            List<Punto> subLista = agrupados.get(fechaKey);
            if (subLista != null) subLista.add(p);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("   REPORTE ").append(sufijo.toUpperCase()).append(" - FACTORESCALATOP   \n");
        sb.append("==========================================\n\n");

        for (String fecha : agrupados.keySet()) {
            sb.append("--- FECHA: ").append(fecha).append(" ---\n");
            List<Punto> subLista = agrupados.get(fecha);
            if (subLista != null) {
                for (Punto p : subLista) {
                    sb.append("PUNTO: ").append(p.nombre).append("\n");
                    sb.append("  UTM: E=").append(p.este).append(" | N=").append(p.norte).append("\n");
                    sb.append("  LAT/LON: ").append(p.latitud).append(" / ").append(p.longitud).append("\n");
                    sb.append("  ALT: Elipsoidal=").append(p.altura).append(" | Ortométrica=").append(p.altOrto).append("\n");
                    sb.append("  PRESIÓN: ").append(p.presion).append("\n");
                    sb.append("  K COMBINADO: ").append(p.fc).append("\n");
                    sb.append("------------------------------------------\n");
                }
            }
            sb.append("\n");
        }
        
        sb.append("Generado por FactorEscalaTop el ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())).append("\n");

        String fileName = "Historial_" + sufijo + "_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".txt";
        FileUtils.savePublicTxtFile(requireContext(), fileName, sb.toString());
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarPuntos();
    }

    private void cargarPuntos() {
        puntosList.clear();
        Cursor cursor = dbHelper.obtenerPuntos();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Punto p = new Punto();
                p.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                p.nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE));
                p.latitud = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LATITUD));
                p.longitud = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LONGITUD));
                p.altura = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALTURA));
                p.altOrto = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALTURA_ORTO));
                p.presion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRESION));
                p.este = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ESTE));
                p.norte = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NORTE));
                p.zona = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ZONA));
                p.hemisferio = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_HEMISFERIO));
                p.fe = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_ESCALA));
                p.fa = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_ALTURA));
                p.fc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_COMBINADO));
                p.fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA));
                puntosList.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }

        if (puntosList.isEmpty()) {
            txtNoData.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtNoData.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private static class Punto {
        int id;
        String nombre, latitud, longitud, altura, altOrto, presion, este, norte, zona, hemisferio, fe, fa, fc, fecha;
        boolean isExpanded = false;
    }

    private class PuntosAdapter extends RecyclerView.Adapter<PuntosAdapter.ViewHolder> {
        private List<Punto> list;

        PuntosAdapter(List<Punto> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_punto, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Punto punto = list.get(position);
            
            holder.txtNombre.setText(punto.nombre);
            holder.txtResumenUtm.setText("E: " + punto.este + " | N: " + punto.norte);
            
            holder.txtDetLat.setText(punto.latitud);
            holder.txtDetLon.setText(punto.longitud);
            holder.txtDetAlt.setText(punto.altura);
            holder.txtDetAltOrto.setText(punto.altOrto);
            holder.txtDetPresion.setText(punto.presion);
            holder.txtDetSis.setText("WGS-84 " + punto.zona + " " + punto.hemisferio);
            holder.txtDetFe.setText(punto.fe);
            holder.txtDetFa.setText(punto.fa);
            holder.txtDetFc.setText(punto.fc);
            holder.txtFechaFull.setText("Registrado: " + punto.fecha);

            // Gestión de Selección
            holder.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            holder.cbSelect.setChecked(selectedIds.contains(punto.id));
            holder.cbSelect.setOnClickListener(v -> {
                if (holder.cbSelect.isChecked()) {
                    selectedIds.add(punto.id);
                } else {
                    selectedIds.remove(punto.id);
                }
            });

            // Lógica de expansión
            holder.layoutExpand.setVisibility(punto.isExpanded ? View.VISIBLE : View.GONE);
            holder.imgArrow.setRotation(punto.isExpanded ? 180 : 0);
            holder.txtExpandLabel.setText(punto.isExpanded ? "Ocultar Detalles" : "Ver Detalles");

            holder.btnExpand.setOnClickListener(v -> {
                punto.isExpanded = !punto.isExpanded;
                notifyItemChanged(position);
            });

            holder.btnCopy.setOnClickListener(v -> copiarAlPortapapeles(punto));
            holder.btnShare.setOnClickListener(v -> compartirPunto(punto));

            // Botón Eliminar individual se oculta en modo selección para evitar confusiones
            holder.btnDelete.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                dbHelper.eliminarPunto(punto.id);
                cargarPuntos();
                UIUtils.showSuccessToast(requireContext(), "Registro eliminado");
            });
        }

        private void copiarAlPortapapeles(Punto p) {
            String reporte = generarReporte(p);
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Punto Topográfico", reporte);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                UIUtils.showSuccessToast(requireContext(), "Copiado al portapapeles");
            }
        }

        private void compartirPunto(Punto p) {
            String reporte = generarReporte(p);
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, reporte);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, "Compartir Punto");
            startActivity(shareIntent);
        }

        private String generarReporte(Punto p) {
            return "--- REPORTE PUNTO: " + p.nombre + " ---\n" +
                   "Este: " + p.este + "\nNorte: " + p.norte + "\n" +
                   "Zona/Hem: " + p.zona + " " + p.hemisferio + "\n" +
                   "Lat: " + p.latitud + "\nLon: " + p.longitud + "\n" +
                   "Alt Elipsoidal: " + p.altura + "\n" +
                   "Alt Ortométrica: " + p.altOrto + "\n" +
                   "Presión: " + p.presion + "\n" +
                   "FACTOR COMBINADO: " + p.fc + "\n" +
                   "Fecha: " + p.fecha + "\n" +
                   "---------------------------";
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtResumenUtm, txtDetLat, txtDetLon, txtDetAlt, txtDetAltOrto, txtDetPresion, txtDetSis, txtDetFe, txtDetFa, txtDetFc, txtFechaFull, txtExpandLabel;
            ImageView btnDelete, btnCopy, btnShare, imgArrow;
            CheckBox cbSelect;
            LinearLayout layoutExpand, btnExpand;

            ViewHolder(View itemView) {
                super(itemView);
                txtNombre = itemView.findViewById(R.id.txt_item_nombre);
                txtResumenUtm = itemView.findViewById(R.id.txt_item_resumen_utm);
                btnDelete = itemView.findViewById(R.id.btn_item_delete);
                btnCopy = itemView.findViewById(R.id.btn_item_copy);
                btnShare = itemView.findViewById(R.id.btn_item_share);
                imgArrow = itemView.findViewById(R.id.img_expand_arrow);
                txtExpandLabel = itemView.findViewById(R.id.txt_expand_label);
                btnExpand = itemView.findViewById(R.id.btn_expand_details);
                layoutExpand = itemView.findViewById(R.id.layout_details_expand);
                cbSelect = itemView.findViewById(R.id.cb_item_select);
                
                txtDetLat = itemView.findViewById(R.id.txt_det_lat);
                txtDetLon = itemView.findViewById(R.id.txt_det_lon);
                txtDetAlt = itemView.findViewById(R.id.txt_det_alt);
                txtDetAltOrto = itemView.findViewById(R.id.txt_det_alt_orto);
                txtDetPresion = itemView.findViewById(R.id.txt_det_presion);
                txtDetSis = itemView.findViewById(R.id.txt_det_sis);
                txtDetFe = itemView.findViewById(R.id.txt_det_fe);
                txtDetFa = itemView.findViewById(R.id.txt_det_fa);
                txtDetFc = itemView.findViewById(R.id.txt_det_fc);
                txtFechaFull = itemView.findViewById(R.id.txt_item_fecha_full);
            }
        }
    }
}
