package com.yaned.final_2025merino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class InventoriesListActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventories_list);

        ListView listView = findViewById(R.id.list_inventories);
        DatabaseHelper db = new DatabaseHelper(this);
        List<Inventory> inventories = db.getInventories();
        ArrayAdapter<Inventory> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, inventories);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Inventory selected = inventories.get(position);
            Intent intent = new Intent(InventoriesListActivity.this, InventoryDetailActivity.class);
            intent.putExtra("inventory_id", selected.id);
            intent.putExtra("inventory_name", selected.name);
            startActivity(intent);
        });
    }
}
