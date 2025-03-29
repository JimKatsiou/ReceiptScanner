package com.receiptscanner.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.receiptscanner.data.DateConverter;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "receipt_items")
@TypeConverters(DateConverter.class)
public class ReceiptItem implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @PrimaryKey(autoGenerate = true)
    private int id;
    
    private String productName;
    private double unitPrice;
    private double quantity;
    private double totalPrice;
    private Date purchaseDate;
    private String storeName;

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    
    public double getTotalPrice() { return totalPrice; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    
    public Date getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Date purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
} 