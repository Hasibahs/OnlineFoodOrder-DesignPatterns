package com.nhlstenden.foodorder.receipt;

import java.time.LocalDateTime;
import java.util.Map;

public interface ReceiptBuilder
{
    void reset();

    void addHeader(String header);

    void addDateTime(LocalDateTime dateTime);

    void addItemLine(String name,
                     double basePrice,
                     Map<String, Double> toppings);


    void addSubtotal(double subtotal);

    void addTotal(double total);

    void addPaymentMethod(String method);

    String build();
}
