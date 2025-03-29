package com.receiptscanner.core;

import android.content.Context;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.receiptscanner.models.ReceiptData;
import com.receiptscanner.models.ReceiptItem;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReceiptScanner {
    private final Context context;
    private final TextRecognizer recognizer;

    public ReceiptScanner(Context context) {
        this.context = context;
        this.recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    }

    public void processImage(InputImage image, OnReceiptProcessedListener listener) {
        recognizer.process(image)
                .addOnSuccessListener(text -> {
                    List<ReceiptItem> items = parseReceipt(text);
                    ReceiptData data = new ReceiptData(items); // Create with items list
                    listener.onSuccess(data);
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    private List<ReceiptItem> parseReceipt(Text text) {
        List<ReceiptItem> items = new ArrayList<>();
        
        // Example parsing logic - you'll need to customize this based on your receipt format
        for (Text.TextBlock block : text.getTextBlocks()) {
            ReceiptItem item = new ReceiptItem();
            
            // Simple example - treat each text block as a product
            item.setProductName(block.getText());
            item.setQuantity(1);
            item.setUnitPrice(0.0); // You'll need to extract the actual price
            item.setTotalPrice(0.0);
            
            items.add(item);
        }
        
        return items;
    }

    private boolean isProductLine(String line) {
        // Patterns for both Greek and English receipts
        String pricePattern = "\\d+[.,]\\d{2}\\s*€?"; // Matches prices like 12.50€ or 12,50
        String quantityPattern = "\\d+(?:[.,]\\d{1,3})?\\s*(?:x|X|×)"; // Matches quantities like 2x or 2.5x
        
        // Common Greek product indicators
        String greekIndicators = "ΤΜΧ|ΤΕΜ|ΤΕΜΑΧΙΑ|ΚΙΛΑ|ΚΙΛ|ΛΙΤΡΑ|ΛΤΡ";
        // Common English product indicators
        String englishIndicators = "PCS|PIECES|KG|L|LITRE|PACK";
        
        String combinedIndicators = "(?:" + greekIndicators + "|" + englishIndicators + ")";
        
        // Check if line contains both price and quantity/unit indicators
        return line.matches(".*" + pricePattern + ".*") && 
               (line.matches(".*" + quantityPattern + ".*") || 
                line.matches(".*" + combinedIndicators + ".*"));
    }

    private ReceiptItem parseProductLine(String line) {
        ReceiptItem item = new ReceiptItem();
        
        // Extract price
        Pattern pricePattern = Pattern.compile("(\\d+[.,]\\d{2})\\s*€?");
        Matcher priceMatcher = pricePattern.matcher(line);
        
        // Extract quantity
        Pattern quantityPattern = Pattern.compile("(\\d+(?:[.,]\\d{1,3})?)\\s*(?:x|X|×)");
        Matcher quantityMatcher = quantityPattern.matcher(line);
        
        // Set price
        if (priceMatcher.find()) {
            String price = priceMatcher.group(1).replace(",", ".");
            item.setUnitPrice(Double.parseDouble(price));
        }
        
        // Set quantity
        if (quantityMatcher.find()) {
            String quantity = quantityMatcher.group(1).replace(",", ".");
            item.setQuantity(Double.parseDouble(quantity));
        } else {
            item.setQuantity(1.0); // Default quantity if not specified
        }
        
        // Extract product name (everything before the price/quantity)
        String productName = line.replaceAll("\\d+[.,]\\d{2}\\s*€?", "")
                               .replaceAll("\\d+(?:[.,]\\d{1,3})?\\s*(?:x|X|×)", "")
                               .trim();
        item.setProductName(productName);
        
        // Calculate total price
        item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
        
        // Set purchase date to current date
        item.setPurchaseDate(new Date());
        
        return item;
    }

    // Add method to detect language
    private boolean isGreekReceipt(String text) {
        // Check for Greek characters
        return text.matches(".*[\\p{InGreek}].*");
    }

    // Add method to preprocess the text
    private String preprocessText(String text) {
        return text.replaceAll("\\s+", " ") // Normalize whitespace
                  .replaceAll("[\\u00A0\\u2007\\u202F]", " ") // Replace special spaces
                  .trim();
    }
} 