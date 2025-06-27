package com.nhlstenden.foodorder.persistence;

import java.time.LocalDateTime;

public class OrderRecord {
    private int id;
    private LocalDateTime dateTime;
    private String itemsCsv;
    private double rawTotal;
    private double finalTotal;
    private String paymentMethod;
    private String username;
    private String deletedBy;

    // Constructor with ID (used when loading from DB)
    public OrderRecord(int id, LocalDateTime dateTime, String itemsCsv, double rawTotal, double finalTotal, String paymentMethod, String username) {
        this.id = id;
        this.dateTime = dateTime;
        this.itemsCsv = itemsCsv;
        this.rawTotal = rawTotal;
        this.finalTotal = finalTotal;
        this.paymentMethod = paymentMethod;
        this.username = username;
    }

    // Constructor without ID (used when saving new)
    public OrderRecord(LocalDateTime dateTime, String itemsCsv, double rawTotal, double finalTotal, String paymentMethod, String username) {
        this(-1, dateTime, itemsCsv, rawTotal, finalTotal, paymentMethod, username);
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getItemsCsv() {
        return itemsCsv;
    }

    public double getRawTotal() {
        return rawTotal;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getUsername() {
        return username;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }
}
