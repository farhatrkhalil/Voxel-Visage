package com.example.voxelvisage.ui.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.voxelvisage.R;
import com.example.voxelvisage.ResultActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class CameraFragment extends Fragment {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final String IMAGE_FILE_PATHS_KEY = "imageFilePaths";
    private static final int MAX_IMAGES = 5;
    private int capturedImages = 0;
    private ImageView cameraView;
    private TextView counterTextView;
    private final ArrayList<String> imageFilePaths = new ArrayList<>();

    @SuppressLint({"QueryPermissionsNeeded", "ClickableViewAccessibility"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

        cameraView = rootView.findViewById(R.id.CameraView);
        Button captureButton = rootView.findViewById(R.id.Button);
        Button proceedButton = rootView.findViewById(R.id.Proceed);
        ImageButton leftArrow = rootView.findViewById(R.id.LeftArrow);
        ImageButton rightArrow = rootView.findViewById(R.id.RightArrow);
        proceedButton.setEnabled(false);
        proceedButton.setOnClickListener(v -> handleProceedButtonClick());
        counterTextView = rootView.findViewById(R.id.CounterTextView);
        leftArrow.setOnClickListener(v -> showPreviousImage());
        rightArrow.setOnClickListener(v -> showNextImage());

        updateArrowIcons();
        updateArrowButtonsState();


        updateCounterText();

        captureButton.setOnClickListener(v -> {
            if (capturedImages < MAX_IMAGES) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            } else {
                showMaxImagesPopup();
                updateProceedButtonState();
            }
            updateProceedButtonState();
        });

        if (savedInstanceState == null) {
            showInstructionsPopup();
        }

        setHasOptionsMenu(true);

        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            private float startX;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float deltaX = endX - startX;

                        if (Math.abs(deltaX) > 50) {
                            if (deltaX > 0) {
                                showPreviousImage();
                            } else {
                                showNextImage();
                            }
                        }
                        return true;
                }
                return false;
            }
        });

        new Handler().postDelayed(() -> rightArrow.performClick(), 100);

        return rootView;
    }


    private void handleProceedButtonClick() {
        if (capturedImages == MAX_IMAGES && !imageFilePaths.isEmpty()) {
            navigateToResultPage();
        } else {
            Toast.makeText(requireContext(), "Capture 5 images before proceeding.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateProceedButtonState() {
        Button proceedButton = requireView().findViewById(R.id.Proceed);

        proceedButton.setEnabled(capturedImages == MAX_IMAGES && !imageFilePaths.isEmpty());
    }

    private void updateButtonStateAfterCapture() {
        updateCounterText();

        Button proceedButton = requireView().findViewById(R.id.Proceed);

        proceedButton.setEnabled(capturedImages > 0 && capturedImages == MAX_IMAGES && !imageFilePaths.isEmpty());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {

                Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
                assert capturedImage != null;
                String imagePath = saveImageToFile(capturedImage);

                loadFullResolutionImage(imagePath);

                capturedImages++;
                updateButtonStateAfterCapture();
                imageFilePaths.add(imagePath);
                showImageSavedToast();

                if (capturedImages == MAX_IMAGES) {
                    showCompletionPopup();
                }
            }
        }
    }

    private String saveImageToFile(Bitmap imageBitmap) {
        File directory = requireContext().getDir("images", Context.MODE_PRIVATE);

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";

        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving image", Toast.LENGTH_SHORT).show();
        }

        return file.getAbsolutePath();
    }

    private void loadFullResolutionImage(String imagePath) {
        if (imagePath != null) {
            Bitmap fullResolutionImage = BitmapFactory.decodeFile(imagePath);
            cameraView.setImageBitmap(fullResolutionImage);
        }
    }


    @SuppressLint("DefaultLocale")
    private void updateCounterText() {
        counterTextView.setText(String.format("%d/%d", capturedImages, MAX_IMAGES));
    }

    private void showInstructionsPopup() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Instructions")
                .setMessage("Capture 5 images, and then click on proceed to navigate to the result page.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> triggerTakePictureButtonClick())
                .setCancelable(false)
                .show();
    }

    private void triggerTakePictureButtonClick() {
        Button captureButton = requireView().findViewById(R.id.Button);

        captureButton.performClick();
    }

    private void showImageSavedToast() {
        Toast.makeText(requireContext(), "Image Number " + capturedImages + " Saved", Toast.LENGTH_SHORT).show();
    }

    private void showCompletionPopup() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Capture Completed")
                .setMessage("Do you want to proceed or reset?")
                .setPositiveButton("Proceed", (dialog, which) -> {
                    updateProceedButtonState();
                    navigateToResultPage();
                })
                .setNegativeButton("Reset", (dialog, which) -> resetCapture())
                .show();
    }

    private void navigateToResultPage() {
        Intent resultIntent = new Intent(requireContext(), ResultActivity.class);
        resultIntent.putExtra(IMAGE_FILE_PATHS_KEY, imageFilePaths);
        resultIntent.putExtra("source", "CameraFragment");
        startActivity(resultIntent);
    }


    private void showMaxImagesPopup() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Maximum Images Captured")
                .setMessage("You have captured the maximum number of images.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void resetCapture() {
        capturedImages = 0;
        imageFilePaths.clear();
        updateCounterText();
        cameraView.setImageBitmap(null);
        updateProceedButtonState();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();

        MenuItem removeItem = menu.add(Menu.NONE, R.id.action_button_remove, Menu.NONE, "Remove");
        removeItem.setIcon(R.drawable.ic_custom_remove);
        removeItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_button_remove) {
            showClearImagePopup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearImagePopup() {
        if (capturedImages > 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Clear Captured Image")
                    .setMessage("Are you sure you want to remove the captured image/s?")
                    .setPositiveButton("OK", (dialog, which) -> {
                        clearCapturedImage();
                        resetCapture();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            showNoImagesPopup();
        }
    }

    private void showNoImagesPopup() {
        new AlertDialog.Builder(requireContext())
                .setTitle("No Images Captured")
                .setMessage("There are no captured images to remove.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void clearCapturedImage() {
        capturedImages = 0;
        updateCounterText();
        cameraView.setImageBitmap(null);
        Toast.makeText(requireContext(), "Captured image/s removed", Toast.LENGTH_SHORT).show();
    }

    private int currentImageIndex = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cameraView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            private float startX;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        float deltaX = endX - startX;

                        if (Math.abs(deltaX) > 50) {
                            if (deltaX > 0) {
                                showPreviousImage();
                            } else {
                                showNextImage();
                            }
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void showPreviousImage() {
        if (currentImageIndex > 0 && currentImageIndex < imageFilePaths.size()) {
            currentImageIndex--;
            loadImageAtIndex(currentImageIndex);
        }
        updateArrowButtonsState();
        updateArrowIcons();
    }

    private void showNextImage() {
        if (currentImageIndex >= 0 && currentImageIndex < imageFilePaths.size() - 1) {
            currentImageIndex++;
            loadImageAtIndex(currentImageIndex);
        }
        updateArrowButtonsState();
        updateArrowIcons();
    }


    private void loadImageAtIndex(int index) {
        String imagePath = imageFilePaths.get(index);
        loadFullResolutionImage(imagePath);
        updateCounterText();
    }

    private void updateArrowButtonsState() {
        if (getView() != null) {
            ImageButton leftArrow = requireView().findViewById(R.id.LeftArrow);
            ImageButton rightArrow = requireView().findViewById(R.id.RightArrow);

            leftArrow.setEnabled(currentImageIndex > 0);
            rightArrow.setEnabled(currentImageIndex < imageFilePaths.size() - 1);

            if (capturedImages == 1) {
                leftArrow.setVisibility(View.INVISIBLE);
                rightArrow.setVisibility(View.INVISIBLE);
            } else {
                leftArrow.setVisibility(View.VISIBLE);
                rightArrow.setVisibility(View.VISIBLE);
            }
        }
    }


    private void updateArrowIcons() {
        if (getView() != null) {
            ImageButton leftArrow = requireView().findViewById(R.id.LeftArrow);
            ImageButton rightArrow = requireView().findViewById(R.id.RightArrow);

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            boolean isDarkMode = sharedPreferences.getBoolean("darkMode", false);

            if (isDarkMode) {
                leftArrow.setImageResource(R.drawable.left_arrow_light);
                rightArrow.setImageResource(R.drawable.right_arrow_light);
            } else {
                leftArrow.setImageResource(R.drawable.left_arrow);
                rightArrow.setImageResource(R.drawable.right_arrow);
            }
        }
    }
}
