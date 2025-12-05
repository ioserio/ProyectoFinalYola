package com.yaned.final_2025merino;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class NewInventoryActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private List<Product> products;
    private final List<EditText> countInputs = new ArrayList<>();
    private final List<TextView> diffViews = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_inventory);

        db = new DatabaseHelper(this);
        products = db.getAllProducts();

        LinearLayout container = findViewById(R.id.container_product_rows);
        Button saveBtn = findViewById(R.id.btn_save_inventory);

        for (int idx = 0; idx < products.size(); idx++) {
            Product p = products.get(idx);

            // Fila en una sola línea: [label (peso 2)] [input conteo (peso 1)] [diferencia (peso 1)]
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(0, 12, 0, 12);

            TextView label = new TextView(this);
            label.setSingleLine(true);
            label.setEllipsize(TextUtils.TruncateAt.END);
            label.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2));
            // Mostrar solo la cantidad entre paréntesis, sin el texto 'stock actual'
            label.setText(p.code + " - " + p.name + " (" + p.quantity + ")");
            row.addView(label);

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

            // Actualizar automáticamente diferencias y color al escribir
            final int position = idx;
            input.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    updateDifferenceFor(position);
                }
            });

            container.addView(row);
        }

        saveBtn.setOnClickListener(v -> saveInventory());
    }

    private void updateDifferenceFor(int i) {
        Product p = products.get(i);
        EditText input = countInputs.get(i);
        TextView diffView = diffViews.get(i);
        String txt = input.getText().toString().trim();
        int counted = 0;
        if (!txt.isEmpty()) {
            try {
                counted = Integer.parseInt(txt);
            } catch (NumberFormatException e) {
                diffView.setText("cantidad inválida");
                diffView.setTextColor(0xFFB00020); // rojo error
                return;
            }
        }
        int difference = counted - p.quantity;
        if (difference > 0) {
            diffView.setText("Sobra " + difference);
            diffView.setTextColor(0xFF2E7D32); // verde
        } else if (difference < 0) {
            diffView.setText("Falta " + Math.abs(difference));
            diffView.setTextColor(0xFFB00020); // rojo
        } else {
            diffView.setText("Exacto");
            diffView.setTextColor(0xFF00796B); // teal OK
        }
    }

    private void saveInventory() {
        List<InventoryItem> items = new ArrayList<>();
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            EditText input = countInputs.get(i);
            String txt = input.getText().toString().trim();
            int counted = 0;
            if (!txt.isEmpty()) {
                try {
                    counted = Integer.parseInt(txt);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Cantidad inválida para " + p.name, Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            items.add(new InventoryItem(p.id, counted));
        }

        String invName = "Inventario " + System.currentTimeMillis();
        long invId = db.createInventory(invName, items);
        if (invId > 0) {
            Toast.makeText(this, "Inventario guardado (ID: " + invId + ")", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error al guardar inventario", Toast.LENGTH_SHORT).show();
        }
    }
}
