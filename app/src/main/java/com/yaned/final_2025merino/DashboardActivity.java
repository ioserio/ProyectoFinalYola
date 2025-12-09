package com.yaned.final_2025merino;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        String username = getIntent().getStringExtra("username");
        TextView welcome = findViewById(R.id.txt_welcome);
        if (username != null && !username.isEmpty()) {
            welcome.setText("Bienvenido, " + username);
        }

        ImageButton btnNewInventory = findViewById(R.id.btn_new_inventory);
        ImageButton btnViewInventories = findViewById(R.id.btn_view_inventories);
        ImageButton btnViewProducts = findViewById(R.id.btn_view_products);
        ImageButton btnViewCategories = findViewById(R.id.btn_view_categories);
        ImageButton btnViewIngresos = findViewById(R.id.btn_view_ingresos);
        ImageButton btnViewWarehouses = findViewById(R.id.btn_view_warehouses);

        btnNewInventory.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, NewInventoryActivity.class);
            startActivity(i);
        });
        btnViewInventories.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, InventoriesListActivity.class);
            startActivity(i);
        });
        btnViewProducts.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, ProductsListActivity.class);
            startActivity(i);
        });
        btnViewCategories.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, CategoriesActivity.class);
            startActivity(i);
        });
        btnViewIngresos.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, IngresosActivity.class);
            startActivity(i);
        });
        btnViewWarehouses.setOnClickListener(v -> {
            Intent i = new Intent(DashboardActivity.this, WarehousesActivity.class);
            startActivity(i);
        });
    }
}
