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
import com.yaned.final_2025merino.api.dto.BasicResponse;
import com.yaned.final_2025merino.api.dto.CategoriaDTO;

import java.util.ArrayList;
import java.util.List;

public class CategoriesActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private final List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        ListView listView = findViewById(R.id.list_categories);
        ProgressBar progress = findViewById(R.id.progress_categories);
        Button addBtn = findViewById(R.id.btn_add_category);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        addBtn.setOnClickListener(v -> showAddCategoryDialog(progress));

        loadCategories(progress);
    }

    private void loadCategories(ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<CategoriaDTO> remotos = api.listarCategorias();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    items.clear();
                    if (remotos != null) {
                        for (CategoriaDTO c : remotos) {
                            items.add(c.id + " - " + c.nombre);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No se pudieron cargar categorías", Toast.LENGTH_SHORT).show();
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

    private void showAddCategoryDialog(ProgressBar progress) {
        View form = getLayoutInflater().inflate(R.layout.dialog_add_category, null);
        EditText nombreInput = form.findViewById(R.id.input_nombre);
        EditText descripcionInput = form.findViewById(R.id.input_descripcion);

        new AlertDialog.Builder(this)
                .setTitle("Nueva categoría")
                .setView(form)
                .setPositiveButton("Guardar", (d, which) -> {
                    String nombre = nombreInput.getText().toString().trim();
                    String descripcion = descripcionInput.getText().toString().trim();
                    if (nombre.isEmpty()) {
                        Toast.makeText(this, "Nombre requerido", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    new Thread(() -> {
                        try {
                            ApiRepository api = new ApiRepository();
                            BasicResponse r = api.agregarCategoria(nombre, descripcion);
                            runOnUiThread(() -> {
                                if (r != null && r.success) {
                                    Toast.makeText(this, "Categoría creada", Toast.LENGTH_SHORT).show();
                                    loadCategories(progress);
                                } else {
                                    String reason = (r != null && r.msg != null && !r.msg.isEmpty()) ? r.msg : "No se pudo crear categoría";
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

