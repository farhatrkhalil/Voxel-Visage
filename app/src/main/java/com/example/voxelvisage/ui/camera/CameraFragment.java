package com.example.voxelvisage.ui.camera;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import androidx.lifecycle.Observer;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cameraViewModel = new ViewModelProvider(this).get(CameraViewModel.class);

        previewView = root.findViewById(R.id.previewView);

        cameraViewModel.getCameraSelector().observe(getViewLifecycleOwner(), new Observer<CameraSelector>() {
            @Override
            public void onChanged(CameraSelector cameraSelector) {
                bindCamera(cameraSelector);
            }
        });

        cameraViewModel.getIsCameraSwitched().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isSwitched) {
                if (isSwitched) {
                    cameraViewModel.setIsCameraSwitched(false);
                    // Optional: Perform actions after camera switch if needed
                }
            }
        });

        if (allPermissionsGranted()) {
            cameraViewModel.startCamera(requireContext());
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }

        ImageButton switchCameraButton = root.findViewById(R.id.switchCameraButton);
        switchCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraViewModel.switchCamera();
            }
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
                .setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(requireActivity(),
                                new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                })
                .setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requireActivity().finish();
                    }
                })
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
