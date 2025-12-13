package com.yaned.final_2025merino;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.BasicResponse;
import com.yaned.final_2025merino.api.dto.ProductoDTO;
import com.yaned.final_2025merino.api.dto.StockResponse;
import com.yaned.final_2025merino.api.dto.InventarioDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewInventoryActivity extends AppCompatActivity {

    private final List<ProductoDTO> products = new ArrayList<>();
    private final List<EditText> countInputs = new ArrayList<>();
    private final List<TextView> diffViews = new ArrayList<>();
    private final List<TextView> stockViews = new ArrayList<>();
    private final Map<Integer, Integer> stockPorProducto = new HashMap<>();
    private LinearLayout container;
    private ProgressBar progress;

    private static final int ALMACEN_ID_POR_DEFECTO = 1; // ajusta según tu BD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_inventory);
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            TextView header = headerContainer.findViewById(R.id.header_title);
            if (header != null) header.setText(getString(R.string.title_new_inventory));
        }

        container = findViewById(R.id.container_product_rows);
        Button saveBtn = findViewById(R.id.btn_save_inventory);
        progress = new ProgressBar(this);

        cargarProductosRemotos();

        saveBtn.setOnClickListener(v -> saveInventory());
    }

    private void cargarProductosRemotos() {
        container.removeAllViews();
        countInputs.clear();
        diffViews.clear();
        stockViews.clear();
        stockPorProducto.clear();

        // Indicador simple de carga
        progress.setIndeterminate(true);
        if (progress.getParent() == null) {
            container.addView(progress);
        }

        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<ProductoDTO> remotos = api.listarProductos();
                runOnUiThread(() -> {
                    container.removeView(progress);
                    if (remotos == null || remotos.isEmpty()) {
                        Toast.makeText(this, "No hay productos en el servidor", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    products.clear();
                    products.addAll(remotos);
                    renderRows();
                    cargarStockActual();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    container.removeView(progress);
                    Toast.makeText(this, "Error cargando productos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void renderRows() {
        container.removeAllViews();
        for (int idx = 0; idx < products.size(); idx++) {
            ProductoDTO p = products.get(idx);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 12, 0, 12);

            TextView label = new TextView(this);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            label.setText(p.sku + " - " + p.nombre);
            row.addView(label);

            // Stock actual
            TextView stockView = new TextView(this);
            stockView.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            stockView.setText("Stock: ...");
            row.addView(stockView);
            stockViews.add(stockView);

            EditText input = new EditText(this);
            input.setHint("0");
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            row.addView(input);
            countInputs.add(input);

            TextView diff = new TextView(this);
            diff.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            diff.setText("-");
            row.addView(diff);
            diffViews.add(diff);

            final int position = idx;
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override public void afterTextChanged(Editable s) { updateDifferenceFor(position); }
            });

            container.addView(row);
        }
    }

    private void cargarStockActual() {
        // Carga en lote: obtiene todos los inventarios y mapea stock por producto para el almacén por defecto
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<InventarioDTO> inventarios = api.listarInventarios();
                // reconstruir mapa por producto sólo del almacén seleccionado
                stockPorProducto.clear();
                if (inventarios != null) {
                    for (InventarioDTO inv : inventarios) {
                        if (inv.almacen_id == ALMACEN_ID_POR_DEFECTO) {
                            stockPorProducto.put(inv.producto_id, inv.stock);
                        }
                    }
                }
                runOnUiThread(() -> {
                    // actualizar todas las filas con el stock correspondiente (o 0 si no hay)
                    for (int idx = 0; idx < products.size(); idx++) {
                        ProductoDTO p = products.get(idx);
                        int stock = stockPorProducto.containsKey(p.id) ? stockPorProducto.get(p.id) : 0;
                        TextView sv = stockViews.get(idx);
                        sv.setText("Stock: " + stock);
                        updateDifferenceFor(idx);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    for (int idx = 0; idx < products.size(); idx++) {
                        TextView sv = stockViews.get(idx);
                        sv.setText("Stock: 0");
                        updateDifferenceFor(idx);
                    }
                });
            }
        }).start();
    }

    private void updateDifferenceFor(int i) {
        EditText input = countInputs.get(i);
        TextView diffView = diffViews.get(i);
        ProductoDTO p = products.get(i);
        int stock = stockPorProducto.containsKey(p.id) ? stockPorProducto.get(p.id) : 0;
        String txt = input.getText().toString().trim();
        int counted = 0;
        if (!txt.isEmpty()) {
            try { counted = Integer.parseInt(txt); }
            catch (NumberFormatException e) {
                diffView.setText("cantidad inválida");
                diffView.setTextColor(0xFFB00020);
                return;
            }
        }
        int diff = counted - stock;
        if (counted > 0 || stock > 0) {
            diffView.setText("Dif: " + diff);
            diffView.setTextColor(diff >= 0 ? 0xFF2E7D32 : 0xFFB00020);
        } else {
            diffView.setText("-");
            diffView.setTextColor(0xFF000000);
        }
    }

    private void saveInventory() {
        String referencia = "Inventario " + System.currentTimeMillis();
        new Thread(() -> {
            ApiRepository api = new ApiRepository();
            java.util.List<java.util.Map<String, Object>> itemsPayload = new java.util.ArrayList<>();
            for (int i = 0; i < products.size(); i++) {
                ProductoDTO p = products.get(i);
                EditText input = countInputs.get(i);
                String txt = input.getText().toString().trim();
                int counted = 0;
                if (!txt.isEmpty()) {
                    try { counted = Integer.parseInt(txt); } catch (Exception ignored) {}
                }
                int stock = stockPorProducto.getOrDefault(p.id, 0);

                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("producto_id", p.id);
                item.put("sku", p.sku);
                item.put("descripcion", p.nombre);
                item.put("stock_real", stock);
                item.put("conteo", counted);
                itemsPayload.add(item);
            }

            try {
                BasicResponse resp = api.guardarInventarioRegistros(referencia, ALMACEN_ID_POR_DEFECTO, itemsPayload);
                runOnUiThread(() -> {
                    if (resp != null && resp.success) {
                        Toast.makeText(this, "Inventario guardado: " + (resp.msg != null ? resp.msg : "ok"), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String m = (resp != null && resp.msg != null) ? resp.msg : "Error al guardar";
                        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
