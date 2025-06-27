package com.nhlstenden.foodorder.factory;

import com.nhlstenden.foodorder.model.FoodItem;

public abstract class FoodFactory {
    public abstract FoodItem createFoodItem(String key, String name, double price);
}
