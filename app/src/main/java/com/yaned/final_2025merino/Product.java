package com.yaned.final_2025merino;

public class Product {
    public final int id;
    public final String code;
    public final String name;
    public final int quantity;

    public Product(int id, String code, String name, int quantity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return code + " - " + name + " (stock: " + quantity + ")";
    }
}
