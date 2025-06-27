package com.nhlstenden.foodorder.observer;

import com.nhlstenden.foodorder.model.FoodItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {
    private final List<FoodItem> items     = new ArrayList<>();
    private final List<CartListener> listeners = new ArrayList<>();



    public void addListener(CartListener l) {
        listeners.add(l);
    }

    public void addItem(FoodItem i) {
        items.add(i);
        listeners.forEach(l -> l.onItemAdded(i));
    }

    public void removeItem(FoodItem i) {
        if (items.remove(i)) {
            listeners.forEach(l -> l.onItemRemoved(i));
        }
    }

    public List<FoodItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    public double getTotalCost() {
        return items.stream()
                .mapToDouble(FoodItem::getPrice)
                .sum();
    }

    public void clear() {
        List<FoodItem> copy = new ArrayList<>(items);
        for (FoodItem item : copy) {
            removeItem(item);
        }
    }
}
