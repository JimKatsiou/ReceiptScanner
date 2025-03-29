package com.receiptscanner;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.receiptscanner.adapters.ReceiptItemAdapter;
import com.receiptscanner.data.ReceiptDatabase;
import com.receiptscanner.models.ReceiptData;
import com.receiptscanner.models.ReceiptItem;
import com.receiptscanner.utils.ExportService;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SummaryActivity extends AppCompatActivity {
    private ReceiptItemAdapter adapter;
    private ReceiptDatabase database;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        database = ReceiptDatabase.getDatabase(this);
        executorService = Executors.newSingleThreadExecutor();

        // Get receipt data from intent
        ReceiptData receiptData = (ReceiptData) getIntent().getSerializableExtra("receipt_data");
        if (receiptData == null || receiptData.getItems() == null) {
            Toast.makeText(this, "No receipt data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Setup RecyclerView
        RecyclerView recyclerView = findViewById(R.id.receiptItemsList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReceiptItemAdapter(receiptData.getItems());
        recyclerView.setAdapter(adapter);

        // Setup buttons
        findViewById(R.id.saveButton).setOnClickListener(v -> saveToDatabase(receiptData));
        findViewById(R.id.exportButton).setOnClickListener(v -> exportToExcel(receiptData));
    }

    private void saveToDatabase(ReceiptData receiptData) {
        executorService.execute(() -> {
            try {
                for (ReceiptItem item : receiptData.getItems()) {
                    database.receiptItemDao().insert(item);
                }
                runOnUiThread(() -> Toast.makeText(this, 
                    "Saved to database", Toast.LENGTH_SHORT).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, 
                    "Error saving to database", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void exportToExcel(ReceiptData receiptData) {
        File exportFile = new File(getExternalFilesDir(null), "receipts.xlsx");
        ExportService exportService = new ExportService();
        
        try {
            exportService.exportToExcel(receiptData.getItems(), exportFile.getAbsolutePath());
            Toast.makeText(this, 
                "Exported to " + exportFile.getAbsolutePath(), 
                Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, 
                "Error exporting to Excel", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 