package com.receiptscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.receiptscanner.core.OnReceiptProcessedListener;
import com.receiptscanner.core.ReceiptScanner;
import com.receiptscanner.models.ReceiptData;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int CAMERA_PERMISSION_CODE = 100;
    private PreviewView viewFinder;
    private ImageCapture imageCapture;
    private final Executor cameraExecutor = Executors.newSingleThreadExecutor();
    private ReceiptScanner receiptScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFinder = findViewById(R.id.viewFinder);
        receiptScanner = new ReceiptScanner(this);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }

        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> takePhoto());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = 
            ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this, "Error starting camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(getExternalCacheDir(), "receipt.jpg");
        ImageCapture.OutputFileOptions outputOptions = 
            new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, cameraExecutor,
            new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults output) {
                    try {
                        InputImage image = InputImage.fromFilePath(MainActivity.this, 
                            output.getSavedUri());
                        receiptScanner.processImage(image, receiptListener);
                    } catch (Exception e) {
                        runOnUiThread(() -> 
                            Toast.makeText(MainActivity.this, 
                                "Error processing image", Toast.LENGTH_SHORT).show());
                    }
                }

                @Override
                public void onError(@NonNull ImageCaptureException exception) {
                    runOnUiThread(() -> 
                        Toast.makeText(MainActivity.this, 
                            "Error capturing image", Toast.LENGTH_SHORT).show());
                }
            });
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission is required", 
                    Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private final OnReceiptProcessedListener receiptListener = new OnReceiptProcessedListener() {
        @Override
        public void onSuccess(ReceiptData data) {
            runOnUiThread(() -> {
                Intent intent = new Intent(MainActivity.this, SummaryActivity.class);
                intent.putExtra("receipt_data", data);
                startActivity(intent);
            });
        }

        @Override
        public void onError(String error) {
            runOnUiThread(() -> 
                Toast.makeText(MainActivity.this, 
                    "Error: " + error, Toast.LENGTH_SHORT).show());
        }
    };
} 