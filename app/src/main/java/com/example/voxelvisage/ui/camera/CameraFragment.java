package com.example.voxelvisage.ui.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.voxelvisage.R;
import com.example.voxelvisage.databinding.FragmentCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;

public class CameraFragment extends Fragment {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private FragmentCameraBinding binding;
    private CameraViewModel cameraViewModel;
    private PreviewView previewView;
    private DoubleTapHandler doubleTapHandler;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        previewView = root.findViewById(R.id.previewView);
        doubleTapHandler = new DoubleTapHandler(() -> cameraViewModel.switchCamera());

        cameraViewModel.getCameraSelector().observe(getViewLifecycleOwner(), this::bindCamera);

        cameraViewModel.getIsCameraSwitched().observe(getViewLifecycleOwner(), isSwitched -> {
            if (isSwitched) {
                cameraViewModel.setIsCameraSwitched(false);
            }
        });

        if (allPermissionsGranted()) {
            cameraViewModel.startCamera(requireContext());
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        ImageButton switchCameraButton = root.findViewById(R.id.switchCameraButton);
        switchCameraButton.setOnClickListener(v -> cameraViewModel.switchCamera());

        ImageButton flashButton = root.findViewById(R.id.flashButton);
        flashButton.setOnClickListener(v -> FlashHandler.enableFlash(flashButton));

        ImageButton shutterButton = root.findViewById(R.id.shutterButton);
        shutterButton.setOnClickListener(v -> {
            // Add an animation here if you want
            new Handler().postDelayed(() -> ShutterHandler.takePicture(requireContext()), 1000);
        });

        previewView.setOnTouchListener((v, event) -> {
            doubleTapHandler.onTouchEvent(event);
            // Remove focus handling
            return event.getAction() == MotionEvent.ACTION_DOWN;
        });

        return root;
    }

    private void bindCamera(CameraSelector cameraSelector) {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                requireActivity().runOnUiThread(() -> {
                    cameraProvider.unbindAll();
                    bindPreview(cameraProvider, cameraSelector);
                });

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider, CameraSelector cameraSelector) {
        Preview previewUseCase = new Preview.Builder().build();

        previewUseCase.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                previewUseCase
        );
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Permission Required")
                .setMessage("Camera permission is required to use this feature. Please grant the permission.")
                .setPositiveButton("Grant Permission", (dialog, which) -> ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION))
                .setNegativeButton("Exit App", (dialog, which) -> requireActivity().finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (allPermissionsGranted()) {
                cameraViewModel.restartApp(requireActivity());
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding = null;
    }
}
