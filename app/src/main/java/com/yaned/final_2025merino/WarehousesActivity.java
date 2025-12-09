package com.yaned.final_2025merino;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.AlmacenDTO;
import com.yaned.final_2025merino.api.dto.BasicResponse;

import java.util.ArrayList;
import java.util.List;

public class WarehousesActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private final List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warehouses);

        ListView listView = findViewById(R.id.list_warehouses);
        ProgressBar progress = findViewById(R.id.progress_warehouses);
        Button addBtn = findViewById(R.id.btn_add_warehouse);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        addBtn.setOnClickListener(v -> showAddWarehouseDialog(progress));

        loadWarehouses(progress);
    }

    private void loadWarehouses(ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<AlmacenDTO> remotos = api.listarAlmacenes();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    items.clear();
                    if (remotos != null) {
                        for (AlmacenDTO a : remotos) {
                            items.add(a.id + " - " + a.nombre);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No se pudieron cargar almacenes", Toast.LENGTH_SHORT).show();
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

    private void showAddWarehouseDialog(ProgressBar progress) {
        View form = getLayoutInflater().inflate(R.layout.dialog_add_warehouse, null);
        EditText nombreInput = form.findViewById(R.id.input_nombre);
        EditText ubicacionInput = form.findViewById(R.id.input_ubicacion);

        new AlertDialog.Builder(this)
                .setTitle("Nuevo almacén")
                .setView(form)
                .setPositiveButton("Guardar", (d, which) -> {
                    String nombre = nombreInput.getText().toString().trim();
                    String ubicacion = ubicacionInput.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "Nombre requerido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(() -> {
                        try {
                            ApiRepository api = new ApiRepository();
                            BasicResponse r = api.agregarAlmacen(nombre, ubicacion);
                            runOnUiThread(() -> {
                                if (r != null && r.success) {
                                    Toast.makeText(this, "Almacén creado", Toast.LENGTH_SHORT).show();
                                    loadWarehouses(progress);
                                } else {
                                    String reason = (r != null && r.msg != null && !r.msg.isEmpty()) ? r.msg : "No se pudo crear almacén";
                                    Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    }).start();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

