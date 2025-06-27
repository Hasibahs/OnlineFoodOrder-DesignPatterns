package com.nhlstenden.foodorder.command;

import com.nhlstenden.foodorder.model.FoodItem;
import com.nhlstenden.foodorder.observer.Cart;

public class RemoveItemCommand implements Command {
    private final Cart cart;
    private final FoodItem item;

    public RemoveItemCommand(Cart cart, FoodItem item) {
        this.cart = cart;
        this.item = item;
    }

    @Override
    public void execute() {
        cart.removeItem(item);
    }

    @Override
    public void undo() {
        cart.addItem(item);
    }
}
