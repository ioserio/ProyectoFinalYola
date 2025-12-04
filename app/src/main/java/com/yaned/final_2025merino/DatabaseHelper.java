package com.yaned.final_2025merino;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "inventory_db.sqlite";
    private static final int DB_VERSION = 2; // bump version to apply new schema

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT, code TEXT NOT NULL, name TEXT NOT NULL, quantity INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE inventories (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, created_at INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE inventory_items (id INTEGER PRIMARY KEY AUTOINCREMENT, inventory_id INTEGER NOT NULL, product_id INTEGER NOT NULL, counted_qty INTEGER NOT NULL, FOREIGN KEY(inventory_id) REFERENCES inventories(id), FOREIGN KEY(product_id) REFERENCES products(id))");

        seedProducts(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple strategy: recreate tables when upgrading schema
        db.execSQL("DROP TABLE IF EXISTS inventory_items");
        db.execSQL("DROP TABLE IF EXISTS inventories");
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

    private void seedProducts(SQLiteDatabase db) {
        // Seed 15 products with codes P001..P015 and random quantities
        String[] names = new String[]{
                "Producto A", "Producto B", "Producto C", "Producto D", "Producto E",
                "Producto F", "Producto G", "Producto H", "Producto I", "Producto J",
                "Producto K", "Producto L", "Producto M", "Producto N", "Producto O"
        };
        Random random = new Random();
        for (int i = 0; i < names.length; i++) {
            String code = String.format("P%03d", i + 1);
            String name = names[i];
            ContentValues cv = new ContentValues();
            cv.put("code", code);
            cv.put("name", name);
            cv.put("quantity", 1 + random.nextInt(100));
            db.insert("products", null, cv);
        }
    }

    // Obtener lista de productos
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, code, name, quantity FROM products ORDER BY name", null);
        try {
            while (c.moveToNext()) {
                Product p = new Product(
                        c.getInt(0),
                        c.getString(1),
                        c.getString(2),
                        c.getInt(3)
                );
                list.add(p);
            }
        } finally {
            c.close();
        }
        return list;
    }

    // Crear inventario y sus ítems
    public long createInventory(String name, List<InventoryItem> items) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues inv = new ContentValues();
            inv.put("name", name);
            inv.put("created_at", System.currentTimeMillis());
            long invId = db.insert("inventories", null, inv);
            if (invId == -1) {
                throw new RuntimeException("No se pudo crear inventario");
            }
            for (InventoryItem item : items) {
                ContentValues it = new ContentValues();
                it.put("inventory_id", invId);
                it.put("product_id", item.productId);
                it.put("counted_qty", item.countedQty);
                db.insert("inventory_items", null, it);
            }
            db.setTransactionSuccessful();
            return invId;
        } finally {
            db.endTransaction();
        }
    }

    // Obtener inventarios (solo cabecera)
    public List<Inventory> getInventories() {
        List<Inventory> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id, name, created_at FROM inventories ORDER BY created_at DESC", null);
        try {
            while (c.moveToNext()) {
                Inventory inv = new Inventory(
                        c.getLong(0),
                        c.getString(1),
                        c.getLong(2)
                );
                list.add(inv);
            }
        } finally {
            c.close();
        }
        return list;
    }

    public static class InventoryItemWithProduct {
        public final String productName;
        public final int stock;
        public final int counted;
        public InventoryItemWithProduct(String productName, int stock, int counted) {
            this.productName = productName;
            this.stock = stock;
            this.counted = counted;
        }
        @Override
        public String toString() {
            int diff = counted - stock;
            String status = diff > 0 ? ("Sobra " + diff) : diff < 0 ? ("Falta " + Math.abs(diff)) : "Exacto";
            return productName + ": stock=" + stock + ", conteo=" + counted + " (" + status + ")";
        }
    }

    public List<InventoryItemWithProduct> getInventoryItems(long inventoryId) {
        List<InventoryItemWithProduct> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT p.name, p.quantity, ii.counted_qty " +
                        "FROM inventory_items ii " +
                        "JOIN products p ON p.id = ii.product_id " +
                        "WHERE ii.inventory_id = ? ORDER BY p.name",
                new String[]{String.valueOf(inventoryId)}
        );
        try {
            while (c.moveToNext()) {
                list.add(new InventoryItemWithProduct(
                        c.getString(0),
                        c.getInt(1),
                        c.getInt(2)
                ));
            }
        } finally {
            c.close();
        }
        return list;
    }
}
