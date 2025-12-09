package com.yaned.final_2025merino;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class InventoryDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_detail);

        long inventoryId = getIntent().getLongExtra("inventory_id", -1);
        String inventoryName = getIntent().getStringExtra("inventory_name");

        TextView title = findViewById(R.id.detail_title);
        if (inventoryName != null) {
            title.setText(inventoryName);
        }

        ListView listView = findViewById(R.id.detail_list);
        Toast.makeText(this, "Detalle remoto de inventario: pendiente de endpoint API", Toast.LENGTH_LONG).show();
    }
}
