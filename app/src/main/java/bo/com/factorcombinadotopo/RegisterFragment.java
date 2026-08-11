package bo.com.factorcombinadotopo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.tabs.TabLayout;
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
    private PuntosAdapter puntosAdapter;
    private LibretaAdapter libretaAdapter;
    private DatabaseHelper dbHelper;
    private final List<Punto> puntosList = new ArrayList<>();
    private final List<LibretaEntry> libretaList = new ArrayList<>();
    
    private TabLayout tabLayout;
    private int activeTab = 0; // 0: Puntos, 1: Libreta
    
    private ImageView btnExportAll, btnExportSelected, btnDelete;
    private ImageView btnCancelSelection;
    private TextView btnConfirmDelete, btnConfirmExport;
    
    private boolean isSelectionMode = false;
    private boolean isExportMode = false;
    private final Set<Integer> selectedIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dbHelper = DatabaseHelper.getInstance(requireContext());
        recyclerView = view.findViewById(R.id.rv_puntos);
        txtNoData = view.findViewById(R.id.txt_no_data);
        
        btnExportAll = view.findViewById(R.id.btn_header_export_all);
        btnExportSelected = view.findViewById(R.id.btn_header_export_selected);
        btnDelete = view.findViewById(R.id.btn_header_delete);
        btnConfirmDelete = view.findViewById(R.id.btn_confirm_delete);
        btnConfirmExport = view.findViewById(R.id.btn_confirm_export);
        btnCancelSelection = view.findViewById(R.id.btn_cancel_selection);
        
        tabLayout = view.findViewById(R.id.tabs_register_sub);
        setupTabs();

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        puntosAdapter = new PuntosAdapter(puntosList);
        libretaAdapter = new LibretaAdapter(libretaList);
        recyclerView.setAdapter(puntosAdapter);

        btnExportAll.setOnClickListener(v -> {
            if (activeTab == 0) exportarHistorialTxt(puntosList, "Completo");
            else exportarLibretaTxt(libretaList, "Completo_Libreta");
        });

        btnExportSelected.setOnClickListener(v -> {
            if (!isSelectionMode) {
                isExportMode = true;
                toggleSelectionMode();
                UIUtils.showInfoToast(requireContext(), getString(R.string.msg_select_points_export));
            } else {
                toggleSelectionMode();
            }
        });

        btnDelete.setOnClickListener(v -> {
            if (!isSelectionMode) {
                isExportMode = false;
                toggleSelectionMode();
                UIUtils.showInfoToast(requireContext(), getString(R.string.msg_select_points_delete));
            } else {
                toggleSelectionMode();
            }
        });

        btnConfirmDelete.setOnClickListener(v -> {
            if (selectedIds.isEmpty()) { toggleSelectionMode(); return; }
            eliminarSeleccionados();
        });

        btnConfirmExport.setOnClickListener(v -> {
            if (selectedIds.isEmpty()) { toggleSelectionMode(); return; }
            exportarSeleccionados();
        });

        btnCancelSelection.setOnClickListener(v -> toggleSelectionMode());

        cargarDatos();
    }

    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                activeTab = tab.getPosition();
                if (isSelectionMode) toggleSelectionMode();
                cargarDatos();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void cargarDatos() {
        if (activeTab == 0) {
            recyclerView.setAdapter(puntosAdapter);
            cargarPuntos();
        } else {
            recyclerView.setAdapter(libretaAdapter);
            cargarLibreta();
        }
    }

    private void toggleSelectionMode() {
        isSelectionMode = !isSelectionMode;
        selectedIds.clear();
        btnExportAll.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
        btnExportSelected.setColorFilter(requireContext().getColor(isSelectionMode && isExportMode ? R.color.accent_orange : R.color.text_secondary));
        btnDelete.setColorFilter(requireContext().getColor(isSelectionMode && !isExportMode ? R.color.state_error : R.color.text_secondary));
        btnConfirmExport.setVisibility(isSelectionMode && isExportMode ? View.VISIBLE : View.GONE);
        btnConfirmDelete.setVisibility(isSelectionMode && !isExportMode ? View.VISIBLE : View.GONE);
        btnCancelSelection.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        if (activeTab == 0) puntosAdapter.notifyDataSetChanged();
        else libretaAdapter.notifyDataSetChanged();
    }

    private void eliminarSeleccionados() {
        if (selectedIds.isEmpty()) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_no_points_selected));
            toggleSelectionMode();
            return;
        }

        UIUtils.showConfirmDialog(requireContext(), R.string.dialog_delete_title, R.string.dialog_delete_msg, () -> {
            for (int id : selectedIds) {
                if (activeTab == 0) dbHelper.eliminarPunto(id);
                else dbHelper.eliminarLibreta(id);
            }
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_deleted));
            toggleSelectionMode();
            cargarDatos();
        });
    }

    private void exportarSeleccionados() {
        if (selectedIds.isEmpty()) {
            UIUtils.showWarningToast(requireContext(), getString(R.string.msg_no_points_selected));
            toggleSelectionMode();
            return;
        }
        if (activeTab == 0) {
            List<Punto> seleccionados = new ArrayList<>();
            for (Punto p : puntosList) if (selectedIds.contains(p.id)) seleccionados.add(p);
            exportarHistorialTxt(seleccionados, "Seleccion");
        } else {
            List<LibretaEntry> seleccionados = new ArrayList<>();
            for (LibretaEntry e : libretaList) if (selectedIds.contains(e.id)) seleccionados.add(e);
            exportarLibretaTxt(seleccionados, "Seleccion_Libreta");
        }
        toggleSelectionMode();
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
                p.geoidModel = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_MODELO_GEOIDAL));
                p.tipoRegistro = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIPO_REGISTRO));
                p.precision = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PRECISION));
                p.satelites = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SATELITES));
                p.temperatura = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TEMPERATURA));
                p.fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA));
                p.notas = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTAS));
                puntosList.add(p);
            } while (cursor.moveToNext());
            cursor.close();
        }
        actualizarVistaVacia(puntosList.isEmpty());
    }

    private void cargarLibreta() {
        libretaList.clear();
        Cursor cursor = dbHelper.obtenerLibreta();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                LibretaEntry e = new LibretaEntry();
                e.id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_ID));
                e.estacion = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_ESTACION));
                e.altIns = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_ALT_INS));
                e.puntoRef = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_PUNTO_REF));
                e.altPri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_ALT_PRI));
                e.puntoAux = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_PUNTO_AUX));
                e.tipoReg = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_TIPO_REG));
                e.este = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_ESTE));
                e.norte = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_NORTE));
                e.cota = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_COTA));
                e.obs = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_OBS));
                e.fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LIB_FECHA));
                libretaList.add(e);
            } while (cursor.moveToNext());
            cursor.close();
        }
        actualizarVistaVacia(libretaList.isEmpty());
    }

    private void actualizarVistaVacia(boolean isEmpty) {
        txtNoData.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        if (activeTab == 0) puntosAdapter.notifyDataSetChanged();
        else libretaAdapter.notifyDataSetChanged();
    }

    private void exportarHistorialTxt(List<Punto> lista, String sufijo) {
        if (lista.isEmpty()) { UIUtils.showWarningToast(requireContext(), getString(R.string.warn_no_export_data)); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("    REPORTE TOPOGRÁFICO - FACTORESCALATOP           \n");
        sb.append("    Tipo: ").append(sufijo.toUpperCase()).append("\n");
        sb.append("====================================================\n\n");
        
        for (Punto p : lista) {
            sb.append(buildPuntoInfoString(p));
            sb.append("----------------------------------------------------\n\n");
        }
        
        String timeTag = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String fileName = "Reporte_Topografico_" + sufijo + "_" + timeTag + ".txt";
        
        String successMsg = sufijo.equals("Completo") ? 
            getString(R.string.msg_export_all_success) : 
            getString(R.string.msg_export_selected_success);
            
        FileUtils.savePublicTxtFile(requireContext(), fileName, sb.toString(), successMsg);
    }

    private void exportarLibretaTxt(List<LibretaEntry> lista, String sufijo) {
        if (lista.isEmpty()) { UIUtils.showWarningToast(requireContext(), getString(R.string.warn_no_export_data)); return; }
        StringBuilder sb = new StringBuilder();
        sb.append("==========================================\n");
        sb.append("   LIBRETA DE CAMPO - FACTORESCALATOP     \n");
        sb.append("==========================================\n\n");
        
        for (LibretaEntry e : lista) {
            sb.append(buildLibretaInfoString(e));
            sb.append("\n------------------------------------------\n\n");
        }
        
        String timeTag = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(new Date());
        String fileName = "Libreta_" + sufijo + "_" + timeTag + ".txt";

        String successMsg = sufijo.equals("Completo_Libreta") ? 
            getString(R.string.msg_export_all_success) : 
            getString(R.string.msg_export_selected_success);
            
        FileUtils.savePublicTxtFile(requireContext(), fileName, sb.toString(), successMsg);
    }

    private String buildPuntoInfoString(Punto p) {
        if (!isAdded()) return "";
        StringBuilder sb = new StringBuilder();
        
        // Re-formatear datos numéricos para el reporte (usando Locale US para punto decimal)
        double fcVal = Double.parseDouble(p.fc.replace(",", "."));
        String formattedFc = GeoUtils.formatFactorWithPpm(fcVal);
        
        sb.append("IDENTIFICACIÓN DEL PUNTO:\n");
        sb.append("  Nombre: ").append(p.nombre).append("\n");
        sb.append("  Origen: ").append(p.tipoRegistro).append("\n");
        sb.append("  Fecha:  ").append(p.fecha).append("\n");
        sb.append("  Notas:  ").append(p.notas).append("\n\n");

        sb.append("COORDENADAS GEODÉSICAS (WGS84):\n");
        sb.append("  Latitud:   ").append(p.latitud).append("\n");
        sb.append("  Longitud:  ").append(p.longitud).append("\n");
        sb.append("  Alt. Elipsoidal: ").append(p.altura).append(" m\n");
        sb.append("  Alt. Ortométrica: ").append(p.altOrto).append(" m\n\n");

        sb.append("PROYECCIÓN CARTOGRÁFICA (UTM):\n");
        sb.append("  Este (X):  ").append(p.este).append(" m\n");
        sb.append("  Norte (Y): ").append(p.norte).append(" m\n");
        sb.append("  Zona/Hem:  ").append(p.zona).append(p.hemisferio).append("\n\n");

        sb.append("FACTORES Y DATOS TÉCNICOS:\n");
        sb.append("  Modelo Geoidal:    ").append(p.geoidModel).append("\n");
        sb.append("  Factor de Escala:  ").append(p.fe).append("\n");
        sb.append("  Factor de Altura:  ").append(p.fa).append("\n");
        sb.append("  Factor Combinado:  ").append(formattedFc).append("\n");
        sb.append("  Presión Atmo.:     ").append(p.presion).append("\n\n");

        sb.append("CONDICIONES DE CAPTURA:\n");
        sb.append("  Precisión GPS: ").append(p.precision).append("\n");
        sb.append("  Satélites:     ").append(p.satelites).append("\n");
        sb.append("  Temperatura:   ").append(p.temperatura).append("\n");
        
        return sb.toString();
    }

    private String buildLibretaInfoString(LibretaEntry e) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.label_libreta_title)).append(":\n");
        sb.append("  ").append(getString(R.string.label_reg_date)).append(": ").append(e.fecha).append("\n");
        sb.append("  ").append(getString(R.string.label_station)).append(": ").append(e.estacion).append(" (").append(getString(R.string.label_instrument)).append(": ").append(e.altIns).append(getString(R.string.unit_meter)).append(") -> AUX: ").append(e.puntoAux).append("\n");
        sb.append("  ").append(getString(R.string.label_ref_format, e.puntoRef)).append(" (").append(getString(R.string.label_prisma)).append(": ").append(e.altPri).append(getString(R.string.unit_meter)).append(") | ").append(e.tipoReg).append("\n");
        
        // Coordenadas con formato profesional US
        try {
            double este = Double.parseDouble(e.este.replace(",", "."));
            double norte = Double.parseDouble(e.norte.replace(",", "."));
            double cota = Double.parseDouble(e.cota.replace(",", "."));
            sb.append("  COORD: E=").append(GeoUtils.formatCoord(este)).append(" | N=").append(GeoUtils.formatCoord(norte)).append(" | Z=").append(GeoUtils.formatCoord(cota)).append("\n");
        } catch (Exception ex) {
            sb.append("  COORD: E=").append(e.este).append(" | N=").append(e.norte).append(" | Z=").append(e.cota).append("\n");
        }

        sb.append("  ").append(getString(R.string.label_observations)).append(": ").append((e.obs == null || e.obs.isEmpty()) ? getString(R.string.label_no_observations) : e.obs);
        return sb.toString();
    }

    @Override public void onResume() { super.onResume(); cargarDatos(); }

    private static class Punto {
        int id; String nombre, latitud, longitud, altura, altOrto, presion, este, norte, zona, hemisferio, fe, fa, fc, geoidModel, tipoRegistro, precision, satelites, temperatura, fecha, notas;
        boolean isExpanded = false;
    }

    private static class LibretaEntry {
        int id; String estacion, altIns, puntoRef, altPri, puntoAux, tipoReg, este, norte, cota, obs, fecha;
        boolean isExpanded = false;
    }

    private class PuntosAdapter extends RecyclerView.Adapter<PuntosAdapter.ViewHolder> {
        private final List<Punto> list;
        PuntosAdapter(List<Punto> list) { this.list = list; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_punto, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            Punto p = list.get(pos);
            h.txtNombre.setText(p.nombre); h.txtResumenUtm.setText("E: " + p.este + " | N: " + p.norte);
            h.txtDetLat.setText(p.latitud); h.txtDetLon.setText(p.longitud); 
            h.txtDetAlt.setText(p.altura + " m");
            h.txtDetAltOrto.setText(p.altOrto + " m");
            
            // Re-formatear para UI (Datos limpios en DB -> Etiquetas en UI)
            try {
                double fcVal = Double.parseDouble(p.fc.replace(",", "."));
                double presVal = Double.parseDouble(p.presion.replace(",", "."));
                h.txtDetFc.setText(GeoUtils.formatFactorWithPpm(fcVal));
                h.txtDetPresion.setText(GeoUtils.formatPressureDual(presVal));
            } catch (Exception e) {
                h.txtDetFc.setText(p.fc);
                h.txtDetPresion.setText(p.presion);
            }

            h.txtDetSis.setText("WGS-84 " + p.zona + " " + p.hemisferio);
            h.txtDetGeoid.setText(p.geoidModel);
            h.txtTipoReg.setText(p.tipoRegistro);
            h.txtDetPrecision.setText(p.precision);
            h.txtDetSat.setText(p.satelites);
            h.txtDetTemp.setText(p.temperatura);
            h.txtDetFe.setText(p.fe); h.txtDetFa.setText(p.fa);
            h.txtFechaFull.setText(getString(R.string.label_registered_format, p.fecha));
            h.txtDetNotas.setText((p.notas != null && !p.notas.isEmpty()) ? p.notas : getString(R.string.label_no_obs_list));
            h.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            h.cbSelect.setChecked(selectedIds.contains(p.id));
            h.cbSelect.setOnClickListener(v -> { if (h.cbSelect.isChecked()) selectedIds.add(p.id); else selectedIds.remove(p.id); });
            h.layoutExpand.setVisibility(p.isExpanded ? View.VISIBLE : View.GONE);
            h.imgArrow.setRotation(p.isExpanded ? 180 : 0);
            h.txtExpandLabel.setText(p.isExpanded ? getString(R.string.label_hide_details) : getString(R.string.label_show_details));
            h.btnExpand.setOnClickListener(v -> { p.isExpanded = !p.isExpanded; notifyItemChanged(pos); });
            h.btnCopy.setOnClickListener(v -> copiarPunto(p));
            h.btnShare.setOnClickListener(v -> compartirPunto(p));
            h.btnDelete.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            h.btnDelete.setOnClickListener(v -> {
                UIUtils.showConfirmDialog(requireContext(), R.string.dialog_delete_title, R.string.dialog_delete_msg_single, () -> {
                    dbHelper.eliminarPunto(p.id);
                    cargarPuntos();
                    UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_deleted));
                });
            });
        }
        @Override public int getItemCount() { return list.size(); }
        private void copiarPunto(Punto p) {
            String r = buildPuntoInfoString(p);
            ((ClipboardManager)requireContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Punto Topografico", r));
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_copied_clipboard));
        }
        private void compartirPunto(Punto p) {
            String r = buildPuntoInfoString(p);
            Intent si = new Intent(Intent.ACTION_SEND); si.setType("text/plain"); si.putExtra(Intent.EXTRA_TEXT, r);
            startActivity(Intent.createChooser(si, "Compartir Punto"));
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtResumenUtm, txtDetLat, txtDetLon, txtDetAlt, txtDetAltOrto, txtDetPresion, txtDetSis, txtDetGeoid, txtDetFe, txtDetFa, txtDetFc, txtFechaFull, txtExpandLabel, txtDetNotas, txtTipoReg, txtDetPrecision, txtDetSat, txtDetTemp;
            ImageView btnDelete, btnCopy, btnShare, imgArrow; CheckBox cbSelect; LinearLayout layoutExpand, btnExpand;
            ViewHolder(View v) {
                super(v);
                txtNombre = v.findViewById(R.id.txt_item_nombre); txtResumenUtm = v.findViewById(R.id.txt_item_resumen_utm);
                btnDelete = v.findViewById(R.id.btn_item_delete); btnCopy = v.findViewById(R.id.btn_item_copy);
                btnShare = v.findViewById(R.id.btn_item_share); imgArrow = v.findViewById(R.id.img_expand_arrow);
                txtExpandLabel = v.findViewById(R.id.txt_expand_label); btnExpand = v.findViewById(R.id.btn_expand_details);
                layoutExpand = v.findViewById(R.id.layout_details_expand); cbSelect = v.findViewById(R.id.cb_item_select);
                txtDetLat = v.findViewById(R.id.txt_det_lat); txtDetLon = v.findViewById(R.id.txt_det_lon);
                txtDetAlt = v.findViewById(R.id.txt_det_alt); txtDetAltOrto = v.findViewById(R.id.txt_det_alt_orto);
                txtDetPresion = v.findViewById(R.id.txt_det_presion); txtDetSis = v.findViewById(R.id.txt_det_sis);
                txtDetGeoid = v.findViewById(R.id.txt_det_geoid_model);
                txtTipoReg = v.findViewById(R.id.txt_item_tipo_registro);
                txtDetPrecision = v.findViewById(R.id.txt_det_precision);
                txtDetSat = v.findViewById(R.id.txt_det_sat);
                txtDetTemp = v.findViewById(R.id.txt_det_temp);
                txtDetFe = v.findViewById(R.id.txt_det_fe); txtDetFa = v.findViewById(R.id.txt_det_fa);
                txtDetFc = v.findViewById(R.id.txt_det_fc); txtFechaFull = v.findViewById(R.id.txt_item_fecha_full);
                txtDetNotas = v.findViewById(R.id.txt_det_notas);
            }
        }
    }

    private class LibretaAdapter extends RecyclerView.Adapter<LibretaAdapter.ViewHolder> {
        private final List<LibretaEntry> list;
        LibretaAdapter(List<LibretaEntry> list) { this.list = list; }
        @NonNull @Override public ViewHolder onCreateViewHolder(@NonNull ViewGroup p, int vt) {
            return new ViewHolder(LayoutInflater.from(p.getContext()).inflate(R.layout.item_libreta, p, false));
        }
        @Override public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
            LibretaEntry e = list.get(pos);
            h.txtNombre.setText(e.estacion + " -> " + e.puntoAux); h.txtResumen.setText(getString(R.string.label_ref_format, e.puntoRef) + " | " + getString(R.string.label_prisma) + ": " + e.altPri + " " + getString(R.string.unit_meter));
            h.txtDetTipo.setText(e.tipoReg); h.txtDetAltIns.setText(e.altIns + " " + getString(R.string.unit_meter)); h.txtDetEste.setText(e.este + " " + getString(R.string.unit_meter));
            h.txtDetNorte.setText(e.norte + " " + getString(R.string.unit_meter)); h.txtDetCota.setText(e.cota + " " + getString(R.string.unit_meter)); h.txtDetFecha.setText(e.fecha);
            h.txtDetObs.setText((e.obs == null || e.obs.isEmpty()) ? getString(R.string.label_no_obs_list) : e.obs);
            h.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
            h.cbSelect.setChecked(selectedIds.contains(e.id));
            h.cbSelect.setOnClickListener(v -> { if (h.cbSelect.isChecked()) selectedIds.add(e.id); else selectedIds.remove(e.id); });
            h.layoutExpand.setVisibility(e.isExpanded ? View.VISIBLE : View.GONE);
            h.imgArrow.setRotation(e.isExpanded ? 180 : 0);
            h.txtExpandLabel.setText(e.isExpanded ? getString(R.string.label_hide_details) : getString(R.string.label_show_details));
            h.btnExpand.setOnClickListener(v -> { e.isExpanded = !e.isExpanded; notifyItemChanged(pos); });
            h.btnCopy.setOnClickListener(v -> copiarLibreta(e));
            h.btnShare.setOnClickListener(v -> compartirLibreta(e));
            h.btnDelete.setVisibility(isSelectionMode ? View.GONE : View.VISIBLE);
            h.btnDelete.setOnClickListener(v -> {
                UIUtils.showConfirmDialog(requireContext(), R.string.dialog_delete_title, R.string.dialog_delete_msg_single, () -> {
                    dbHelper.eliminarLibreta(e.id);
                    cargarLibreta();
                    UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_point_deleted));
                });
            });
        }
        @Override public int getItemCount() { return list.size(); }
        private void copiarLibreta(LibretaEntry e) {
            String r = buildLibretaInfoString(e);
            ((ClipboardManager)requireContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Libreta de Campo", r));
            UIUtils.showSuccessToast(requireContext(), getString(R.string.msg_copied_clipboard));
        }
        private void compartirLibreta(LibretaEntry e) {
            String r = buildLibretaInfoString(e);
            Intent si = new Intent(Intent.ACTION_SEND); si.setType("text/plain"); si.putExtra(Intent.EXTRA_TEXT, r);
            startActivity(Intent.createChooser(si, "Compartir Registro"));
        }
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtResumen, txtDetTipo, txtDetAltIns, txtDetEste, txtDetNorte, txtDetCota, txtDetFecha, txtDetObs, txtExpandLabel;
            ImageView btnDelete, btnCopy, btnShare, imgArrow; CheckBox cbSelect; LinearLayout layoutExpand, btnExpand;
            ViewHolder(View v) {
                super(v);
                txtNombre = v.findViewById(R.id.txt_item_lib_nombre); txtResumen = v.findViewById(R.id.txt_item_lib_resumen);
                btnDelete = v.findViewById(R.id.btn_item_lib_delete); btnCopy = v.findViewById(R.id.btn_item_lib_copy);
                btnShare = v.findViewById(R.id.btn_item_lib_share); imgArrow = v.findViewById(R.id.img_lib_expand_arrow);
                txtExpandLabel = v.findViewById(R.id.txt_lib_expand_label); btnExpand = v.findViewById(R.id.btn_expand_lib_details);
                layoutExpand = v.findViewById(R.id.layout_lib_details_expand); cbSelect = v.findViewById(R.id.cb_item_select_lib);
                txtDetTipo = v.findViewById(R.id.txt_lib_det_tipo); txtDetAltIns = v.findViewById(R.id.txt_lib_det_alt_ins);
                txtDetEste = v.findViewById(R.id.txt_lib_det_este); txtDetNorte = v.findViewById(R.id.txt_lib_det_norte);
                txtDetCota = v.findViewById(R.id.txt_lib_det_cota); txtDetFecha = v.findViewById(R.id.txt_lib_det_fecha);
                txtDetObs = v.findViewById(R.id.txt_lib_det_obs);
            }
        }
    }
}
