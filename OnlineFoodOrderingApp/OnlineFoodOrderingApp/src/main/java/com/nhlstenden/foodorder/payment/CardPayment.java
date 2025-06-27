package com.nhlstenden.foodorder.payment;

public class CardPayment implements PaymentStrategy {
    private final String cardNumber;
    public CardPayment(String cardNumber) { this.cardNumber = cardNumber; }

    @Override public void pay(double amount) {
        System.out.println("Charged $" + amount + " to card " + cardNumber);
    }
}
