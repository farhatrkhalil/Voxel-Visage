package com.example.voxelvisage.ui.camera;

import android.content.Context;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
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

    private final MutableLiveData<CameraSelector> cameraSelector = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isCameraSwitched = new MutableLiveData<>();
    private ExecutorService cameraExecutor;
    private CameraSwitchHandler cameraSwitchHandler;
    private Context context;

    public void setIsCameraSwitched(boolean isSwitched) {
        isCameraSwitched.setValue(isSwitched);
    }

    public CameraViewModel() {
        cameraSwitchHandler = new CameraSwitchHandler();
        cameraSelector.setValue(cameraSwitchHandler.getCurrentCameraSelector());
    }

    LiveData<CameraSelector> getCameraSelector() {
        return cameraSelector;
    }

    LiveData<Boolean> getIsCameraSwitched() {
        return isCameraSwitched;
    }

    void startCamera(Context context) {
        this.context = context;

        cameraExecutor = Executors.newSingleThreadExecutor();

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
        });

        cameraProvider.bindToLifecycle(
                (LifecycleOwner) context,
                cameraSelector.getValue(),
                preview,
                imageAnalysis
        );
    }

    void switchCamera() {
        cameraSwitchHandler.switchCamera();
        cameraSelector.setValue(cameraSwitchHandler.getCurrentCameraSelector());
        isCameraSwitched.postValue(true);
    }

    void restartApp(Context context) {
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }
}
