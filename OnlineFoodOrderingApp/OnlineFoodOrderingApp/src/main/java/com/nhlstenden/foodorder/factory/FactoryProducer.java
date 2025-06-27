package com.nhlstenden.foodorder.factory;

public class FactoryProducer {
    public static FoodFactory getFactory(String category) {
        if ("pizza".equalsIgnoreCase(category)) {
            return new PizzaFactory();
        }
        return new DrinkFactory();
    }
}
