package com.yaned.final_2025merino;

public class Inventory {
    public final long id;
    public final String name;
    public final long createdAt;

    public Inventory(long id, String name, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return name;
    }
}

