package com.example.voxelvisage.ui.camera;

import androidx.camera.core.CameraSelector;

public class CameraSwitchHandler {

    private CameraSelector currentCameraSelector;
    private int lensFacing;

    public CameraSwitchHandler() {
        lensFacing = CameraSelector.LENS_FACING_BACK;
        currentCameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();
    }

    public CameraSelector getCurrentCameraSelector() {
        return currentCameraSelector;
    }

    public void switchCamera() {
        lensFacing = (lensFacing == CameraSelector.LENS_FACING_BACK)
                ? CameraSelector.LENS_FACING_FRONT
                : CameraSelector.LENS_FACING_BACK;
        currentCameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
    }
}
