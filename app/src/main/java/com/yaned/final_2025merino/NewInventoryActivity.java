package com.yaned.final_2025merino;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
        Button calcBtn = findViewById(R.id.btn_calculate_diffs);
        Button saveBtn = findViewById(R.id.btn_save_inventory);

        for (int idx = 0; idx < products.size(); idx++) {
            Product p = products.get(idx);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.VERTICAL);
            row.setPadding(0, 12, 0, 12);

            TextView label = new TextView(this);
            label.setText(p.code + " - " + p.name + " (stock actual: " + p.quantity + ")");
            row.addView(label);

            LinearLayout line2 = new LinearLayout(this);
            line2.setOrientation(LinearLayout.HORIZONTAL);

            TextView conteoLabel = new TextView(this);
            conteoLabel.setText("Conteo: ");
            line2.addView(conteoLabel);

            EditText input = new EditText(this);
            input.setHint("0");
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            line2.addView(input);
            countInputs.add(input);

            row.addView(line2);

            TextView diff = new TextView(this);
            diff.setText("Diferencia: -");
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

        calcBtn.setOnClickListener(v -> calculateDifferences());
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
                diffView.setText("Diferencia: cantidad inválida");
                diffView.setTextColor(0xFFB00020); // rojo error
                return;
            }
        }
        int difference = counted - p.quantity;
        if (difference > 0) {
            diffView.setText("Diferencia: Sobra " + difference);
            diffView.setTextColor(0xFF2E7D32); // verde
        } else if (difference < 0) {
            diffView.setText("Diferencia: Falta " + Math.abs(difference));
            diffView.setTextColor(0xFFB00020); // rojo
        } else {
            diffView.setText("Diferencia: Exacto");
            diffView.setTextColor(0xFF00796B); // teal OK
        }
    }

    private void calculateDifferences() {
        for (int i = 0; i < products.size(); i++) {
            updateDifferenceFor(i);
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
