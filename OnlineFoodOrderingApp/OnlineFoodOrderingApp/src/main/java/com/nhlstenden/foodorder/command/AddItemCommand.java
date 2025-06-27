package com.nhlstenden.foodorder.command;

import com.nhlstenden.foodorder.model.FoodItem;
import com.nhlstenden.foodorder.observer.Cart;

public class AddItemCommand implements Command {
    private final Cart cart;
    private final FoodItem item;

    public AddItemCommand(Cart cart, FoodItem item) {
        this.cart = cart;
        this.item = item;
    }

    @Override
    public void execute() {
        cart.addItem(item);
    }

    @Override
    public void undo() {
        cart.removeItem(item);
    }
}
