// CameraViewModel.java
package com.example.voxelvisage.ui.camera;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraViewModel extends ViewModel {

    private final MutableLiveData<ImageProxy> cameraImage = new MutableLiveData<>();
    private ExecutorService cameraExecutor;

    public CameraViewModel() {
        // No need to initialize the camera here
    }

    void startCamera(Context context) {
        cameraExecutor = Executors.newSingleThreadExecutor();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(context, cameraProvider);

            } catch (Exception e) {
                // Handle any errors
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindPreview(Context context, ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy image) {
                // Analyze the image here if needed
                // For simplicity, we just update the LiveData with the latest image
                cameraImage.postValue(image);
            }
        });

        // Use getViewLifecycleOwner() instead of casting context to LifecycleOwner
        cameraProvider.bindToLifecycle(
                (LifecycleOwner) context,
                cameraSelector,
                preview,
                imageAnalysis
        ); // Correct placement of the closing parenthesis
    }

    public LiveData<ImageProxy> getCameraImage() {
        return cameraImage;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Shutdown the camera executor when the ViewModel is cleared
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }
}
