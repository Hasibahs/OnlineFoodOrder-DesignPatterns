package com.nhlstenden.foodorder.factory;

import com.nhlstenden.foodorder.model.Drink;
import com.nhlstenden.foodorder.model.FoodItem;

public class DrinkFactory extends FoodFactory {
    @Override
    public FoodItem createFoodItem(String key, String name, double price) {
        return new Drink(name, price);
    }
}
