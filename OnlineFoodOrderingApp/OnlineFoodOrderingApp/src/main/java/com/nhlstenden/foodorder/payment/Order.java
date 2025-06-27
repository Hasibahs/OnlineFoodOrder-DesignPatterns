package com.nhlstenden.foodorder.payment;

import com.nhlstenden.foodorder.observer.Cart;
import javafx.scene.control.Alert;

/**
 * Encapsulates order placement (without discount support).
 */
public class Order {
    private final Cart cart;
    private PaymentStrategy paymentStrategy;

    public Order(Cart cart) {
        this.cart = cart;
    }

    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.paymentStrategy = strategy;
    }

    public void placeOrder() {
        if (cart.getItems().isEmpty()) {
            showAlert("Cart is empty!", Alert.AlertType.WARNING);
            return;
        }
        if (paymentStrategy == null) {
            showAlert("No payment method selected", Alert.AlertType.WARNING);
            return;
        }

        double total = cart.getTotalCost();

        // pay
        paymentStrategy.pay(total);

        // audit
        System.out.printf("ORDER TOTAL: %.2f\n", total);
        cart.getItems().forEach(i -> System.out.println("  * " + i.getName()));

        // clear cart
        cart.getItems().forEach(cart::removeItem);
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
