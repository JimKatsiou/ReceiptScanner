package com.example.receiptscannerapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.provider.MediaStore
import com.google.cloud.vision.v1.AnnotateImageRequest
import com.google.cloud.vision.v1.AnnotateImageResponse
import com.google.cloud.vision.v1.Feature
import com.google.cloud.vision.v1.Image
import com.google.cloud.vision.v1.ImageAnnotatorClient
import com.google.protobuf.ByteString
import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private val cameraPermissionCode = 100
    private lateinit var cameraLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the camera launcher
        cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val imageBitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    result.data?.extras?.getParcelable("data", Bitmap::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    result.data?.extras?.getParcelable<Bitmap>("data")
                }

                if (imageBitmap != null) {
                    processImageWithCloudVision(imageBitmap)
                } else {
                    Toast.makeText(this, "Failed to capture image.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Camera capture failed", Toast.LENGTH_SHORT).show()
            }
        }

        // Request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), cameraPermissionCode)
        }

        // Set up the scan button
        val scanButton: Button = findViewById(R.id.scanButton)
        scanButton.setOnClickListener {
            openCamera()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(cameraIntent)
    }

    /**
     * Process the captured image using Google Cloud Vision API.
     */
    private fun processImageWithCloudVision(bitmap: Bitmap) {
        Log.e("---1---", "mpla")
        val stream = ByteArrayOutputStream()
        Log.e("---2---stream", stream.toString())
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val imageBytes = ByteString.copyFrom(stream.toByteArray())

        val visionImage = Image.newBuilder().setContent(imageBytes).build()
        val feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build()
        Log.e("---3---", "mpla3")
        val request = AnnotateImageRequest.newBuilder()
            .addFeatures(feature)
            .setImage(visionImage)
            .build()

        val requests = listOf(request)

        try {
            val client = ImageAnnotatorClient.create()
            val response: List<AnnotateImageResponse> = client.batchAnnotateImages(requests).responsesList
            Log.e("---res---", response.toString())
            for (res in response) {
                if (res.hasError()) {
                    Log.e("CloudVisionError", "Error: ${res.error.message}")
                    Toast.makeText(this, "Cloud Vision API Error: ${res.error.message}", Toast.LENGTH_SHORT).show()
                    return
                }

                // Log the text annotations
                val text = res.textAnnotationsList.firstOrNull()?.description ?: ""
                Log.d("CloudVisionText", "Detected text: $text")
                extractDataFromText(text)
            }

            client.shutdownNow()
        } catch (e: Exception) {
            e.printStackTrace() // Log the stack trace for debugging
            Log.d("Failed to process image", " ${e.message}")
            Toast.makeText(this, "Failed to process image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Extracts receipt data from OCR text.
     */
    private fun extractDataFromText(ocrText: String) {
        val items = parseReceiptText(ocrText)
        if (items.isNotEmpty()) {
            saveReceiptsToJson(items)
        } else {
            Toast.makeText(this, "No items found in receipt.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Parses OCR text into a list of ReceiptItem objects.
     */
    private fun parseReceiptText(ocrText: String): List<ReceiptItem> {
        val items = mutableListOf<ReceiptItem>()
        val lines = ocrText.split("\n")

        // Extract date if available; otherwise, use the current date
        val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""") // Matches a date in the format YYYY-MM-DD
        val dateFromText = dateRegex.find(ocrText)?.value
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val receiptDate = dateFromText ?: currentDate // Use extracted date or fallback to today's date

        for (line in lines) {
            val match = Regex("""(.+?)\s+(\d+)\s+([\d.]+)\s+([\d.]+)?""").find(line)
            if (match != null) {
                val (name, quantity, price, discount) = match.destructured
                items.add(
                    ReceiptItem(
                        name = name.trim(),
                        quantity = quantity.toInt(),
                        price = price.toDouble(),
                        discount = discount.toDoubleOrNull() ?: 0.0,
                        date = receiptDate
                    )
                )
            }
        }
        return items
    }

    /**
     * Saves receipt items to a JSON file.
     */
    private fun saveReceiptsToJson(receipts: List<ReceiptItem>) {
        val gson = Gson()
        val jsonString = gson.toJson(receipts)
        val file = File(getExternalFilesDir(null), "receipts.json")
        file.writeText(jsonString)
        Toast.makeText(this, "Receipts saved to ${file.absolutePath}", Toast.LENGTH_SHORT).show()
    }
}
