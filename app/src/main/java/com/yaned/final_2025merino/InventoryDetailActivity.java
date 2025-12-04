package com.yaned.final_2025merino;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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
        DatabaseHelper db = new DatabaseHelper(this);
        List<DatabaseHelper.InventoryItemWithProduct> items = db.getInventoryItems(inventoryId);
        ArrayAdapter<DatabaseHelper.InventoryItemWithProduct> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);
    }
}

