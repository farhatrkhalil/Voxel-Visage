package com.example.voxelvisage.ui.camera;

import android.content.Context;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
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
    private final MutableLiveData<CameraControl> cameraControl = new MutableLiveData<>();
    private final MutableLiveData<CameraInfo> cameraInfo = new MutableLiveData<>();
    private ExecutorService cameraExecutor;
    private CameraSwitchHandler cameraSwitchHandler;
    private Context context;

    public void setIsCameraSwitched(boolean isSwitched) {
        isCameraSwitched.setValue(isSwitched);
    }

    public LiveData<CameraSelector> getCameraSelector() {
        return cameraSelector;
    }

    public LiveData<Boolean> getIsCameraSwitched() {
        return isCameraSwitched;
    }

    public LiveData<CameraControl> getCameraControl() {
        return cameraControl;
    }

    public LiveData<CameraInfo> getCameraInfo() {
        return cameraInfo;
    }

    public CameraViewModel() {
        cameraSwitchHandler = new CameraSwitchHandler();
        cameraSelector.setValue(cameraSwitchHandler.getCurrentCameraSelector());
    }

    public void startCamera(Context context) {
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

        Camera camera = cameraProvider.bindToLifecycle(
                (LifecycleOwner) context,
                cameraSelector.getValue(),
                preview,
                imageAnalysis
        );

        CameraControl cameraControl = camera.getCameraControl();
        CameraInfo cameraInfo = camera.getCameraInfo();

        this.cameraControl.postValue(cameraControl);
        this.cameraInfo.postValue(cameraInfo);
    }

    public void switchCamera() {
        cameraSwitchHandler.switchCamera();
        cameraSelector.setValue(cameraSwitchHandler.getCurrentCameraSelector());
        isCameraSwitched.postValue(true);
    }

    public void restartApp(Context context) {

    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (cameraExecutor != null && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown();
        }
    }
}
