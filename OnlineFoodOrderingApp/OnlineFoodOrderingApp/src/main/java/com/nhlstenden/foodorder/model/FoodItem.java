package com.nhlstenden.foodorder.model;

public abstract class FoodItem
{
    private final String name;
    private final double price;

    protected FoodItem(String name, double price)
    {
        this.name  = name;
        this.price = price;
    }

    public String getName()
    {
        return name;
    }

    public double getPrice()
    {
        return price;
    }
}
