package com.yaned.final_2025merino;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.InventarioDTO;

import java.util.ArrayList;
import java.util.List;

public class InventoriesListActivity extends AppCompatActivity {
    private ArrayAdapter<InventarioDTO> adapter;
    private final List<InventarioDTO> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventories_list);
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            TextView header = headerContainer.findViewById(R.id.header_title);
            if (header != null) header.setText(getString(R.string.header_stock));
        }

        ListView listView = findViewById(R.id.list_inventories);
        ProgressBar progress = findViewById(R.id.progress_inventories);

        adapter = new ArrayAdapter<InventarioDTO>(this, R.layout.item_inventory, items) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View row = convertView;
                if (row == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    row = inflater.inflate(R.layout.item_inventory, parent, false);
                }

                InventarioDTO inv = getItem(position);
                TextView tvSku = row.findViewById(R.id.tv_sku);
                TextView tvProducto = row.findViewById(R.id.tv_producto);
                TextView tvAlmacen = row.findViewById(R.id.tv_almacen);
                TextView tvStock = row.findViewById(R.id.tv_stock);

                if (inv != null) {
                    tvSku.setText(inv.sku != null ? inv.sku : "");
                    tvProducto.setText(inv.nombre_producto != null ? inv.nombre_producto : "");
                    tvAlmacen.setText(inv.nombre_almacen != null ? inv.nombre_almacen : "");
                    tvStock.setText(String.valueOf(inv.stock));
                }

                return row;
            }
        };
        listView.setAdapter(adapter);

        loadInventories(progress);
    }

    private void loadInventories(ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<InventarioDTO> remotos = api.listarInventarios();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    items.clear();
                    if (remotos != null) {
                        items.addAll(remotos);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No se pudieron cargar inventarios", Toast.LENGTH_SHORT).show();
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
}
