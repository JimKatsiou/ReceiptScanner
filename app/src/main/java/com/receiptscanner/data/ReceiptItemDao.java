package com.receiptscanner.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;
import java.util.Date;
import com.receiptscanner.models.ReceiptItem;

@Dao
public interface ReceiptItemDao {
    @Insert
    void insert(ReceiptItem item);

    @Query("SELECT * FROM receipt_items")
    List<ReceiptItem> getAllItems();

    @Query("SELECT * FROM receipt_items WHERE productName LIKE '%' || :name || '%'")
    List<ReceiptItem> findByProductName(String name);

    @Query("SELECT * FROM receipt_items WHERE purchaseDate >= :startDate AND purchaseDate <= :endDate")
    List<ReceiptItem> getItemsByDateRange(long startDate, long endDate);

    @Query("SELECT AVG(unitPrice) FROM receipt_items WHERE productName = :productName")
    double getAveragePrice(String productName);
} 