package com.yaned.final_2025merino;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.InventarioRegistroDTO;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InventariosRealizadosActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private final List<String> refs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventarios_realizados);
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            TextView header = headerContainer.findViewById(R.id.header_title);
            if (header != null) header.setText(getString(R.string.title_inventories));
        }

        ListView listView = findViewById(R.id.list_refs);
        ProgressBar progress = findViewById(R.id.progress_refs);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, refs);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            String ref = refs.get(position);
            Intent intent = new Intent(this, InventarioRealizadoDetalleActivity.class);
            intent.putExtra("inventario_ref", ref);
            startActivity(intent);
        });

        loadRefs(progress);
    }

    private void loadRefs(ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<InventarioRegistroDTO> registros = api.listarInventarioRegistros();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    refs.clear();
                    if (registros != null) {
                        Set<String> set = new LinkedHashSet<>();
                        for (InventarioRegistroDTO r : registros) {
                            String ref = r.inventario_ref != null ? r.inventario_ref : "(sin referencia)";
                            set.add(ref);
                        }
                        refs.addAll(set);
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

