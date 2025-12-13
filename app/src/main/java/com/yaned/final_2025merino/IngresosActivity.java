package com.yaned.final_2025merino;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.yaned.final_2025merino.api.ApiRepository;
import com.yaned.final_2025merino.api.dto.AlmacenDTO;
import com.yaned.final_2025merino.api.dto.BasicResponse;
import com.yaned.final_2025merino.api.dto.ProductoDTO;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IngresosActivity extends AppCompatActivity {
    private Spinner productoSpinner;
    private EditText cantidadInput;
    private Button addItemBtn;
    private LinearLayout itemsContainer;
    private Button enviarBtn;
    private ProgressBar progress;
    private Spinner almacenSpinner;

    private final List<ProductoDTO> productos = new ArrayList<>();
    private final List<AlmacenDTO> almacenes = new ArrayList<>();

    private static final int ALMACEN_ID_POR_DEFECTO = 1; // ajusta según tu BD

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingresos);
        View headerContainer = findViewById(R.id.header_container);
        if (headerContainer != null) {
            TextView header = headerContainer.findViewById(R.id.header_title);
            if (header != null) header.setText(getString(R.string.title_ingresos));
        }

        productoSpinner = findViewById(R.id.spinner_producto);
        cantidadInput = findViewById(R.id.input_cantidad);
        addItemBtn = findViewById(R.id.btn_add_item);
        itemsContainer = findViewById(R.id.container_items);
        enviarBtn = findViewById(R.id.btn_enviar_ingreso);
        progress = findViewById(R.id.progress_ingresos);
        almacenSpinner = findViewById(R.id.spinner_almacen);

        cargarAlmacenes();
        cargarProductos();

        addItemBtn.setOnClickListener(v -> agregarItem());
        enviarBtn.setOnClickListener(v -> enviarIngreso());
    }

    private void cargarAlmacenes() {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<AlmacenDTO> remotos = api.listarAlmacenes();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    almacenes.clear();
                    if (remotos != null) almacenes.addAll(remotos);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresAlmacenes());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    almacenSpinner.setAdapter(adapter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error al cargar almacenes: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void cargarProductos() {
        progress.setVisibility(View.VISIBLE);
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                List<ProductoDTO> remotos = api.listarProductos();
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    productos.clear();
                    if (remotos != null) productos.addAll(remotos);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresProductos());
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    productoSpinner.setAdapter(adapter);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private List<String> nombresAlmacenes() {
        List<String> list = new ArrayList<>();
        for (AlmacenDTO a : almacenes) list.add(a.nombre);
        return list;
    }

    private List<String> nombresProductos() {
        List<String> list = new ArrayList<>();
        for (ProductoDTO p : productos) list.add(p.sku + " - " + p.nombre);
        return list;
    }

    private void agregarItem() {
        int sel = productoSpinner.getSelectedItemPosition();
        if (sel < 0 || sel >= productos.size()) {
            Toast.makeText(this, "Selecciona un producto", Toast.LENGTH_SHORT).show();
            return;
        }
        int cantidad = 0;
        try { cantidad = Integer.parseInt(cantidadInput.getText().toString().trim()); } catch (Exception ignored) {}
        if (cantidad <= 0) { Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show(); return; }

        ProductoDTO p = productos.get(sel);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 12, 0, 12);

        TextView label = new TextView(this);
        label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
        label.setText(p.sku + " - " + p.nombre);
        row.addView(label);

        TextView qty = new TextView(this);
        qty.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        qty.setText("x" + cantidad);
        row.addView(qty);

        // Guardar id del producto y cantidad en tags
        row.setTag(new int[]{p.id, cantidad});

        itemsContainer.addView(row);
        cantidadInput.setText("");
    }

    private void enviarIngreso() {
        int childCount = itemsContainer.getChildCount();
        if (childCount == 0) { Toast.makeText(this, "Agrega al menos un item", Toast.LENGTH_SHORT).show(); return; }

        // almacen seleccionado
        int selAlm = almacenSpinner.getSelectedItemPosition();
        if (selAlm < 0 || selAlm >= almacenes.size()) { Toast.makeText(this, "Selecciona un almacén", Toast.LENGTH_SHORT).show(); return; }
        int almacenId = almacenes.get(selAlm).id;

        // Construir JSON de items
        JSONArray items = new JSONArray();
        for (int i = 0; i < childCount; i++) {
            View row = itemsContainer.getChildAt(i);
            Object tag = row.getTag();
            if (!(tag instanceof int[])) continue;
            int[] data = (int[]) tag;
            int productoId = data[0];
            int cantidad = data[1];
            try {
                JSONObject it = new JSONObject();
                it.put("producto_id", productoId);
                it.put("cantidad", cantidad);
                items.put(it);
            } catch (Exception ignored) {}
        }
        if (items.length() == 0) { Toast.makeText(this, "Items inválidos", Toast.LENGTH_SHORT).show(); return; }

        progress.setVisibility(View.VISIBLE);
        final String itemsPayload = items.toString();
        new Thread(() -> {
            try {
                ApiRepository api = new ApiRepository();
                BasicResponse r = api.registrarIngreso(almacenId, "Ingreso app", "Ingreso desde Android", itemsPayload);
                runOnUiThread(() -> {
                    progress.setVisibility(View.GONE);
                    if (r != null && r.success) {
                        Toast.makeText(this, "Ingreso registrado", Toast.LENGTH_SHORT).show();
                        itemsContainer.removeAllViews();
                    } else {
                        String reason = (r != null && r.msg != null && !r.msg.isEmpty()) ? r.msg : "No se pudo registrar ingreso";
                        Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
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
