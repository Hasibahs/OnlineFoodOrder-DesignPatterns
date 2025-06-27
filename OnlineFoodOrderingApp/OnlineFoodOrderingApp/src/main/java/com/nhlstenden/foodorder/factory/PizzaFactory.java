package com.nhlstenden.foodorder.factory;

import com.nhlstenden.foodorder.model.FoodItem;
import com.nhlstenden.foodorder.model.Pizza;

public class PizzaFactory extends FoodFactory {
    @Override
    public FoodItem createFoodItem(String key, String name, double price) {
        return new Pizza(name, price);
    }
}
