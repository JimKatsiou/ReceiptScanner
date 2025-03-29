package com.receiptscanner.models;

import java.io.Serializable;
import java.util.List;

public class ReceiptData implements Serializable {
    private static final long serialVersionUID = 1L;
    private List<ReceiptItem> items;
    private String storeName;
    private double totalAmount;

    public ReceiptData(List<ReceiptItem> items) {
        this.items = items;
    }

    public List<ReceiptItem> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItem> items) {
        this.items = items;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
} 