package com.yaned.final_2025merino;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.InventarioDTO;

import java.util.ArrayList;
import java.util.List;

public class InventoriesListActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private final List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventories_list);

        ListView listView = findViewById(R.id.list_inventories);
        ProgressBar progress = findViewById(R.id.progress_inventories);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
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
                        for (InventarioDTO inv : remotos) {
                            String line = inv.sku + " - " + inv.nombre_producto + " | Almacén: " + inv.nombre_almacen + " | Stock: " + inv.stock;
                            items.add(line);
                        }
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

