package com.example.receiptscannerapp

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object FileUtils {
    fun loadReceiptsFromJson(context: Context): List<ReceiptItem> {
        val file = File(context.getExternalFilesDir(null), "receipts.json")
        return if (file.exists()) {
            val gson = Gson()
            val type = object : TypeToken<List<ReceiptItem>>() {}.type
            gson.fromJson(file.readText(), type)
        } else {
            emptyList()
        }
    }
}
