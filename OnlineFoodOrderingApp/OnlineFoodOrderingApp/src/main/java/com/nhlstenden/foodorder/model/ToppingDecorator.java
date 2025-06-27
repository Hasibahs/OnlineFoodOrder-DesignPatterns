package com.nhlstenden.foodorder.model;

public abstract class ToppingDecorator extends FoodItem
{
    protected final FoodItem wrapped;

    protected ToppingDecorator(FoodItem wrapped)
    {
        super(wrapped.getName(), wrapped.getPrice());
        this.wrapped = wrapped;
    }

    /** Returns the inner food item that this decorator wraps. */
    public FoodItem getWrapped()
    {
        return wrapped;
    }
}
