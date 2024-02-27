package com.example.voxelvisage.ui.camera;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ShutterHandler {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static int imagesTaken = 0;
    private static final int MAX_IMAGES = 5;

    public static void takePicture(Context context, LifecycleOwner lifecycleOwner) {
        if (imagesTaken < MAX_IMAGES) {
            File photoFile = createImageFile(context);

            if (photoFile != null) {
                ProcessCameraProvider.getInstance(context).addListener(() -> {
                    try {
                        ProcessCameraProvider cameraProvider = ProcessCameraProvider.getInstance(context).get();
                        bindImageCapture(cameraProvider, context, photoFile, lifecycleOwner);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }, ContextCompat.getMainExecutor(context));
            }
        } else {
            Toast.makeText(context, "Maximum number of images reached", Toast.LENGTH_SHORT).show();
        }
    }

    private static File createImageFile(Context context) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = context.getExternalFilesDir(null);

        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void bindImageCapture(ProcessCameraProvider cameraProvider, Context context, File photoFile, LifecycleOwner lifecycleOwner) {
        ImageCapture imageCapture = new ImageCapture.Builder().build();

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        Camera camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture);

        imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(ImageCapture.OutputFileResults outputFileResults) {
                imagesTaken++;
                Toast.makeText(context, "Image " + imagesTaken + " saved successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(context, "Error capturing image", Toast.LENGTH_SHORT).show();
                exception.printStackTrace();
            }
        });
    }
}
