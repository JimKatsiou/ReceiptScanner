package com.receiptscanner.utils;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.receiptscanner.models.ReceiptItem;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExportService {
    public void exportToExcel(List<ReceiptItem> items, String filePath) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Receipt Data");

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Product Name");
        headerRow.createCell(1).setCellValue("Quantity");
        headerRow.createCell(2).setCellValue("Unit Price");
        headerRow.createCell(3).setCellValue("Discount");
        headerRow.createCell(4).setCellValue("Total Price");
        headerRow.createCell(5).setCellValue("Purchase Date");

        // Add data rows
        int rowNum = 1;
        for (ReceiptItem item : items) {
            Row row = sheet.createRow(rowNum++);
            writeReceiptToRow(row, item);
        }

        // Save workbook
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            workbook.write(fos);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeReceiptToRow(Row row, ReceiptItem item) {
        row.createCell(0).setCellValue(item.getProductName());
        row.createCell(1).setCellValue(item.getQuantity());
        row.createCell(2).setCellValue(item.getUnitPrice());
        row.createCell(3).setCellValue(0.0);  // No discount in current model
        row.createCell(4).setCellValue(item.getTotalPrice());
        row.createCell(5).setCellValue(item.getPurchaseDate().toString());
    }
} 