package com.yaned.final_2025merino;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.InventarioRegistroDTO;

import java.util.ArrayList;
import java.util.List;

public class InventarioRealizadoDetalleActivity extends AppCompatActivity {
    private final List<InventarioRegistroDTO> items = new ArrayList<>();
    private RecyclerView rv;
    private ProgressBar progress;
    private DetalleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventario_realizado_detalle);
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            TextView header = headerContainer.findViewById(R.id.header_title);
            if (header != null) header.setText("Detalle inventario");
        }

        String inventarioRef = getIntent().getStringExtra("inventario_ref");
        TextView tvRef = findViewById(R.id.tv_ref);
        tvRef.setText(inventarioRef);

        rv = findViewById(R.id.rv_detalle);
        progress = findViewById(R.id.progress_detalle);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DetalleAdapter(items);
        rv.setAdapter(adapter);

        loadDetalle(inventarioRef);
    }

    private void loadDetalle(String inventarioRef) {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<InventarioRegistroDTO> registros = api.listarInventarioRegistros();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    items.clear();
                    if (registros != null) {
                        for (InventarioRegistroDTO r : registros) {
                            if (inventarioRef != null && inventarioRef.equals(r.inventario_ref)) {
                                items.add(r);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No se pudo cargar el detalle", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    static class DetalleAdapter extends RecyclerView.Adapter<DetalleAdapter.VH> {
        private final List<InventarioRegistroDTO> data;
        DetalleAdapter(List<InventarioRegistroDTO> data) { this.data = data; }

        static class VH extends RecyclerView.ViewHolder {
            TextView sku, descripcion, stock, conteo, diferencia;
            VH(@NonNull View itemView) {
                super(itemView);
                sku = itemView.findViewById(R.id.tv_sku);
                descripcion = itemView.findViewById(R.id.tv_descripcion);
                stock = itemView.findViewById(R.id.tv_stock_real);
                conteo = itemView.findViewById(R.id.tv_conteo);
                diferencia = itemView.findViewById(R.id.tv_diferencia);
            }
        }

        @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventario_detalle_compacto, parent, false);
            return new VH(v);
        }

        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            InventarioRegistroDTO inv = data.get(pos);
            h.sku.setText(inv.sku != null ? inv.sku : "");
            h.descripcion.setText(inv.descripcion != null ? inv.descripcion : "");
            h.stock.setText(String.valueOf(inv.stock_real));
            h.conteo.setText(String.valueOf(inv.conteo));
            h.diferencia.setText(String.valueOf(inv.diferencia));
            h.diferencia.setTextColor(inv.diferencia >= 0 ? 0xFF2E7D32 : 0xFFB00020);
        }

        @Override public int getItemCount() { return data.size(); }
    }
}
