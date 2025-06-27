package com.nhlstenden.foodorder.util;

import com.nhlstenden.foodorder.model.FoodItem;
import com.nhlstenden.foodorder.model.ToppingDecorator;
import com.nhlstenden.foodorder.observer.Cart;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AppUtils
{
    /**
     * Builds a single comma‐separated string of item names
     * from the cart, so we can save it in the DB/JSON.
     */
    public static String itemsAsCsv(Cart cart) {
        return itemsAsCsv(cart.getItems());
    }

    /**
     * Builds a single comma‐separated string of item names
     * from *any* collection of FoodItems.
     */
    public static String itemsAsCsv(Collection<FoodItem> items) {
        return items.stream()
                .map(FoodItem::getName)
                .collect(Collectors.joining(", "));
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * If you pass in a FoodItem that may be decorated (e.g. a PizzaSauce or Cheese),
     * this returns a map of topping-name → topping-price.
     */
    public static Map<String,Double> collectToppings(FoodItem item) {
        Map<String,Double> toppings = new LinkedHashMap<>();
        FoodItem current = item;

        // walk the decorator chain
        while (current instanceof ToppingDecorator) {
            ToppingDecorator dec = (ToppingDecorator) current;
            double delta = dec.getPrice() - dec.getWrapped().getPrice();
            toppings.put(dec.getClass().getSimpleName(), delta);
            current = dec.getWrapped();
        }

        return toppings;
    }
}
