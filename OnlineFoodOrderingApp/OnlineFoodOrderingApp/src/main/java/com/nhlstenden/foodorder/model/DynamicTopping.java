package com.nhlstenden.foodorder.model;

public class DynamicTopping extends ToppingDecorator {
    private final String toppingName;
    private final double toppingPrice;

    public DynamicTopping(FoodItem wrapped, String toppingName, double toppingPrice) {
        super(wrapped);
        this.toppingName = toppingName;
        this.toppingPrice = toppingPrice;
    }

    @Override
    public String getName() {
        return wrapped.getName() + " + " + toppingName;
    }

    @Override
    public double getPrice() {
        return wrapped.getPrice() + toppingPrice;
    }

    public String getToppingName() {
        return toppingName;
    }

    public double getToppingPrice() {
        return toppingPrice;
    }
}
