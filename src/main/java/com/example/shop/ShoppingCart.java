package com.example.shop;

import java.util.HashMap;
import java.util.Map;

public class ShoppingCart {
    private Map<Product, Integer> items = new HashMap<>();

    public void addItem(Product product) {
        items.put(product, items.getOrDefault(product, 0) + 1);
    }

    public void removeItem(Product product) {
        if (items.containsKey(product)) {
            if (items.get(product) > 1) {
                items.put(product, items.get(product) - 1);
            } else {
                items.remove(product);
            }
        }
    }

    public Map<Product, Integer> getItemsWithQuantity() {
        return items;
    }

    public double getTotalPrice() {
        return items.entrySet().stream()
                .mapToDouble(entry -> entry.getKey().getPrice() * entry.getValue())
                .sum();
    }

    public void clear() {
        items.clear();
    }
}
