package com.nhlstenden.foodorder.receipt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Simple text‐based receipt builder.
 */
public class TextReceiptBuilder implements ReceiptBuilder
{
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StringBuilder sb = new StringBuilder();

    @Override
    public void reset()
    {
        sb.setLength(0);
    }

    @Override
    public void addHeader(String header)
    {
        sb.append(header)
                .append(System.lineSeparator())
                .append("=".repeat(header.length()))
                .append(System.lineSeparator());
    }

    @Override
    public void addDateTime(LocalDateTime dateTime)
    {
        sb.append("Date: ")
                .append(dateTime.format(DATE_FMT))
                .append(System.lineSeparator())
                .append(System.lineSeparator());
    }

    @Override
    public void addItemLine(String name,
                            double basePrice,
                            Map<String, Double> toppings)
    {
        sb.append(String.format("%-20s $%6.2f", name, basePrice))
                .append(System.lineSeparator());

        for (Map.Entry<String, Double> t : toppings.entrySet())
        {
            sb.append(String.format("    + %-16s $%6.2f",
                            t.getKey(),
                            t.getValue()))
                    .append(System.lineSeparator());
        }
    }

    @Override
    public void addSubtotal(double subtotal)
    {
        sb.append(System.lineSeparator())
                .append(String.format("%-20s $%6.2f", "Subtotal:", subtotal))
                .append(System.lineSeparator());
    }

    @Override
    public void addTotal(double total)
    {
        sb.append(System.lineSeparator())
                .append(String.format("%-20s $%6.2f", "Total:", total))
                .append(System.lineSeparator())
                .append(System.lineSeparator());
    }

    @Override
    public void addPaymentMethod(String method)
    {
        sb.append("Paid via: ")
                .append(method)
                .append(System.lineSeparator());
    }

    @Override
    public String build()
    {
        return sb.toString();
    }
}
