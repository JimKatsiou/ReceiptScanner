package com.example.receiptscannerapp

import java.text.SimpleDateFormat
import java.util.*

data class ReceiptItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val discount: Double,
    val date: String // Format: "YYYY-MM-DD"
) {
    val finalPrice: Double
        get() = quantity * price - discount
}
