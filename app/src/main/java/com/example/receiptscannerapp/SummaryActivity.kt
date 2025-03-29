package com.example.receiptscannerapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SummaryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)

        // Load receipts from JSON using FileUtils
        val receipts = FileUtils.loadReceiptsFromJson(this)

        // Group by month and calculate the monthly total
        val groupedByMonth = receipts.groupBy { receipt ->
            receipt.date.substring(0, 7) // Extract "YYYY-MM" from the date
        }

        val monthlySummary = groupedByMonth.map { (month: String, items: List<ReceiptItem>) ->
            MonthSummary(month, items.sumOf { it.finalPrice })
        }

        // Set up the RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.summaryRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SummaryAdapter(monthlySummary)
    }
}

// Data class for monthly summary
data class MonthSummary(val month: String, val total: Double)
