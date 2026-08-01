package bo.com.solucionesit.factorcombinado;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView txtNoData;
    private PuntosAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Punto> puntosList = new ArrayList<>();

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

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PuntosAdapter(puntosList);
        recyclerView.setAdapter(adapter);

        cargarPuntos();
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
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                String nombre = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOMBRE));
                String fc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FACTOR_COMBINADO));
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FECHA));
                puntosList.add(new Punto(id, nombre, fc, fecha));
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

    private class Punto {
        int id;
        String nombre;
        String fc;
        String fecha;

        Punto(int id, String nombre, String fc, String fecha) {
            this.id = id;
            this.nombre = nombre;
            this.fc = fc;
            this.fecha = fecha;
        }
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
            holder.txtFecha.setText(punto.fecha);
            holder.txtFC.setText(punto.fc);

            holder.btnDelete.setOnClickListener(v -> {
                dbHelper.eliminarPunto(punto.id);
                cargarPuntos();
                Toast.makeText(requireContext(), "Registro eliminado", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtNombre, txtFecha, txtFC;
            ImageView btnDelete;

            ViewHolder(View itemView) {
                super(itemView);
                txtNombre = itemView.findViewById(R.id.txt_item_nombre);
                txtFecha = itemView.findViewById(R.id.txt_item_fecha);
                txtFC = itemView.findViewById(R.id.txt_item_fc);
                btnDelete = itemView.findViewById(R.id.btn_item_delete);
            }
        }
    }
}
