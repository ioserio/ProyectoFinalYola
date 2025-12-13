package com.yaned.final_2025merino;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.BasicResponse;
import com.yaned.final_2025merino.api.dto.CategoriaDTO;
import com.yaned.final_2025merino.api.dto.ProductoDTO;

import java.util.ArrayList;
import java.util.List;

public class ProductsListActivity extends AppCompatActivity {
    private ArrayAdapter<String> adapter;
    private final List<String> items = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_list);
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            TextView header = headerContainer.findViewById(R.id.header_title);
            if (header != null) header.setText(getString(R.string.title_products));
        }

        ListView listView = findViewById(R.id.list_products);
        ProgressBar progress = findViewById(R.id.progress_products);
        Button addBtn = findViewById(R.id.btn_add_product);
        Button addCatBtn = findViewById(R.id.btn_add_category);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

        addBtn.setOnClickListener(v -> showAddProductDialog(progress));
        addCatBtn.setOnClickListener(v -> showAddCategoryDialog());

        loadProducts(progress);
    }

    private void loadProducts(ProgressBar progress) {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<ProductoDTO> remotos = api.listarProductos();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    items.clear();
                    if (remotos != null) {
                        for (ProductoDTO p : remotos) {
                            items.add(p.sku + " - " + p.nombre);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No se pudieron cargar productos", Toast.LENGTH_SHORT).show();
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

    private void showAddProductDialog(ProgressBar progress) {
        final ProgressBar progressRef = progress;
        View form = getLayoutInflater().inflate(R.layout.dialog_add_product, null);
        EditText skuInput = form.findViewById(R.id.input_sku);
        EditText nameInput = form.findViewById(R.id.input_nombre);
        EditText descInput = form.findViewById(R.id.input_descripcion);
        EditText priceInput = form.findViewById(R.id.input_precio);
        Spinner catSpinner = form.findViewById(R.id.spinner_categoria);

        // Cargar categorías
        List<CategoriaDTO> categorias = new ArrayList<>();
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        catSpinner.setAdapter(catAdapter);

        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<CategoriaDTO> rem = api.listarCategorias();
                runOnUiThread(() -> {
                    categorias.clear();
                    if (rem != null) categorias.addAll(rem);
                    catAdapter.clear();
                    catAdapter.add("Sin categoría");
                    for (CategoriaDTO c : categorias) catAdapter.add(c.nombre);
                    catAdapter.notifyDataSetChanged();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    catAdapter.clear();
                    catAdapter.add("Sin categoría");
                    catAdapter.notifyDataSetChanged();
                });
            }
        }).start();

        new AlertDialog.Builder(this)
                .setTitle("Nuevo producto")
                .setView(form)
                .setPositiveButton("Guardar", (d, which) -> {
                    String sku = skuInput.getText().toString().trim();
                    String nombre = nameInput.getText().toString().trim();
                    String descripcion = descInput.getText().toString().trim();
                    String precioTxt = priceInput.getText().toString().trim();
                    double precio = 0;
                    try { precio = Double.parseDouble(precioTxt); } catch (Exception ignored) {}

                    if (sku.isEmpty() || nombre.isEmpty()) {
                        Toast.makeText(this, "SKU y nombre son requeridos", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    final double precioFinal = precio;
                    // Obtener categoria_id seleccionado (0 = Sin categoría)
                    Integer categoriaIdFinalTmp = null;
                    int sel = catSpinner.getSelectedItemPosition();
                    if (sel > 0 && sel - 1 < categorias.size()) {
                        categoriaIdFinalTmp = categorias.get(sel - 1).id;
                    }
                    final Integer categoriaIdFinal = categoriaIdFinalTmp;

                    new Thread(() -> {
                        try {
                            ApiRepository api = new ApiRepository();
                            BasicResponse r = api.agregarProducto(sku, nombre, descripcion, precioFinal, categoriaIdFinal);
                            runOnUiThread(() -> {
                                if (r != null && r.success) {
                                    Toast.makeText(this, "Producto creado", Toast.LENGTH_SHORT).show();
                                    loadProducts(progressRef);
                                } else {
                                    String reason = (r != null && r.msg != null && !r.msg.isEmpty()) ? r.msg : "No se pudo crear producto";
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

    private void showAddCategoryDialog() {
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
