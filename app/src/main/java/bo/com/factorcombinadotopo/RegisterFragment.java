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
    private ImageView btnExport, btnDeleteMode;
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
        
        btnExport = view.findViewById(R.id.btn_header_export);
        btnDeleteMode = view.findViewById(R.id.btn_header_delete_mode);
        btnConfirmDelete = view.findViewById(R.id.btn_confirm_delete);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PuntosAdapter(puntosList);
        recyclerView.setAdapter(adapter);

        btnDeleteMode.setOnClickListener(v -> toggleSelectionMode());
        
        btnConfirmDelete.setOnClickListener(v -> {
            if (selectedIds.isEmpty()) {
                toggleSelectionMode();
                return;
            }
            eliminarSeleccionados();
        });

        btnExport.setOnClickListener(v -> exportarHistorialTxt());

        cargarPuntos();
    }

    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        selectedIds.clear();
        
        btnConfirmDelete.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        btnDeleteMode.setColorFilter(requireContext().getColor(isSelectionMode ? R.color.state_error : R.color.text_secondary));
        btnExport.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
        
        adapter.notifyDataSetChanged();
    }

    private void eliminarSeleccionados() {
        for (int id : selectedIds) {
            dbHelper.eliminarPunto(id);
        }
        Toast.makeText(requireContext(), "Registros eliminados: " + selectedIds.size(), Toast.LENGTH_SHORT).show();
        toggleSelectionMode();
        cargarPuntos();
    }

    private void exportarHistorialTxt() {
        if (puntosList.isEmpty()) {
            Toast.makeText(requireContext(), "No hay datos para exportar", Toast.LENGTH_SHORT).show();
            return;
        }

        // Agrupar por fecha (solo parte YYYY-MM-DD)
        TreeMap<String, List<Punto>> agrupados = new TreeMap<>();
        for (Punto p : puntosList) {
            String fechaKey = p.fecha.split(" ")[0];
            if (!agrupados.containsKey(fechaKey)) {
                agrupados.put(fechaKey, new ArrayList<>());
            }
            agrupados.get(fechaKey).add(p);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("   HISTORIAL COMPLETO - FACTORESCALATOP   \n");
        sb.append("==========================================\n\n");

        for (String fecha : agrupados.keySet()) {
            sb.append("--- FECHA: ").append(fecha).append(" ---\n");
            for (Punto p : agrupados.get(fecha)) {
                sb.append("PUNTO: ").append(p.nombre).append("\n");
                sb.append("  UTM: E=").append(p.este).append(" | N=").append(p.norte).append("\n");
                sb.append("  LAT/LON: ").append(p.latitud).append(" / ").append(p.longitud).append("\n");
                sb.append("  K COMBINADO: ").append(p.fc).append("\n");
                sb.append("------------------------------------------\n");
            }
            sb.append("\n");
        }
        
        sb.append("Generado por FactorEscalaTop el ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date())).append("\n");

        String fileName = "Historial_Registros_" + new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date()) + ".txt";
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
        String nombre, latitud, longitud, altura, este, norte, zona, hemisferio, fe, fa, fc, fecha;
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

            // El botón eliminar individual se oculta en modo selección para evitar confusiones
            holder.btnDelete.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> {
                dbHelper.eliminarPunto(punto.id);
                cargarPuntos();
                Toast.makeText(requireContext(), "Registro eliminado", Toast.LENGTH_SHORT).show();
            });
        }

        private void copiarAlPortapapeles(Punto p) {
            String reporte = generarReporte(p);
            ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Punto Topográfico", reporte);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(requireContext(), "Copiado al portapapeles", Toast.LENGTH_SHORT).show();
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
                   "Alt: " + p.altura + "\n" +
                   "FACTOR COMBINADO: " + p.fc + "\n" +
                   "Fecha: " + p.fecha + "\n" +
                   "---------------------------";
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtResumenUtm, txtDetLat, txtDetLon, txtDetAlt, txtDetSis, txtDetFe, txtDetFa, txtDetFc, txtFechaFull, txtExpandLabel;
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
                txtDetSis = itemView.findViewById(R.id.txt_det_sis);
                txtDetFe = itemView.findViewById(R.id.txt_det_fe);
                txtDetFa = itemView.findViewById(R.id.txt_det_fa);
                txtDetFc = itemView.findViewById(R.id.txt_det_fc);
                txtFechaFull = itemView.findViewById(R.id.txt_item_fecha_full);
            }
        }
    }
}
