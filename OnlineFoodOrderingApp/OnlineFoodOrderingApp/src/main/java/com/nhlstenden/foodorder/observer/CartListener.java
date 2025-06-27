package com.nhlstenden.foodorder.observer;

import com.nhlstenden.foodorder.model.FoodItem;

public interface CartListener {
    void onItemAdded(FoodItem item);
    void onItemRemoved(FoodItem item);
}
