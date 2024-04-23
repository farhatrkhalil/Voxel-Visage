package com.voxelvisage.modelviewer.camera;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.voxelvisage.modelviewer.R;
import com.voxelvisage.modelviewer.main.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final String IMAGE_FILE_PATHS_KEY = "imageFilePaths";
    private static final int MAX_IMAGES = 3;
    private int capturedImages = 0;
    private ImageView cameraView;
    private TextView counterTextView;
    private final ArrayList<String> imageFilePaths = new ArrayList<>();
    private ImageButton closeButton;
    private int currentImageIndex = 0;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        cameraView = findViewById(R.id.CameraView);
        Button captureButton = findViewById(R.id.Button);
        Button proceedButton = findViewById(R.id.Proceed);
        ImageButton leftArrow = findViewById(R.id.LeftArrow);
        ImageButton rightArrow = findViewById(R.id.RightArrow);
        counterTextView = findViewById(R.id.CounterTextView);
        closeButton = findViewById(R.id.CloseButton);
        closeButton.setOnClickListener(v -> handleRemoveImageClick());
        updateCloseButtonVisibility();

        updateCounterText();
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        captureButton.setOnClickListener(v -> {
            if (capturedImages < MAX_IMAGES) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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

        cameraView.setOnTouchListener(new View.OnTouchListener() {
            private float startX;

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
        updateArrowButtonsState();

        new Handler().postDelayed(() -> rightArrow.performClick(), 100);
    }


    private void handleRemoveImageClick() {
        if (!imageFilePaths.isEmpty() && currentImageIndex < imageFilePaths.size()) {
            showRemoveImageConfirmationDialog();
        }
    }

    private void showRemoveImageConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Remove Image")
                .setMessage("Are you sure you want to remove this image?")
                .setPositiveButton("Yes", (dialog, which) -> removeCurrentImage())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeCurrentImage() {
        if (!imageFilePaths.isEmpty() && currentImageIndex < imageFilePaths.size()) {
            imageFilePaths.remove(currentImageIndex);
            capturedImages--;
            updateCounterText();
            updateButtonStateAfterCapture();
            closeButton.setVisibility(imageFilePaths.isEmpty() ? View.GONE : View.VISIBLE);
            updateCloseButtonVisibility();
            updateProceedButtonState();
            updateArrowButtonsState();
            Toast.makeText(this, "Image removed", Toast.LENGTH_SHORT).show();
            if (!imageFilePaths.isEmpty()) {
                if (currentImageIndex >= imageFilePaths.size()) {
                    currentImageIndex = imageFilePaths.size() - 1;
                }
                loadImageAtIndex(currentImageIndex);
            } else {
                cameraView.setImageBitmap(null);
                closeButton.setVisibility(View.GONE);
                ImageButton leftArrow = findViewById(R.id.LeftArrow);
                ImageButton rightArrow = findViewById(R.id.RightArrow);
                leftArrow.setVisibility(View.INVISIBLE);
                rightArrow.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void updateCloseButtonVisibility() {
        closeButton.setVisibility(imageFilePaths.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateProceedButtonState() {
        Button proceedButton = findViewById(R.id.Proceed);
        proceedButton.setEnabled(capturedImages == MAX_IMAGES && !imageFilePaths.isEmpty());
    }

    private void updateButtonStateAfterCapture() {
        updateCounterText();
        Button proceedButton = findViewById(R.id.Proceed);
        proceedButton.setEnabled(capturedImages > 0 && capturedImages == MAX_IMAGES && !imageFilePaths.isEmpty());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                updateCloseButtonVisibility();
                showImageSavedToast();
                if (capturedImages == MAX_IMAGES) {
                    showCompletionPopup();
                }
                updateArrowButtonsState();
            }
        }
    }

    private String saveImageToFile(Bitmap imageBitmap) {
        File directory = getDir("images", Context.MODE_PRIVATE);
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = String.format("IMG_%s.jpg", timeStamp);
        File file = new File(directory, fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(this)
                .setTitle("Instructions")
                .setMessage("To create a 3D model of your face, 3 pictures are required:\n\n" +
                        "- A centered image of yourself only containing your face\n\n" +
                        "- An offset image of the front of your face to the left\n\n" +
                        "- An offset image of the front of your face to the right\n\n" +
                        "The more accurately you follow these instructions, the more accurate the results.\n\n" +
                        "Please ensure the images are selected in the following order:\n\n" +
                        "1. Centered face image\n\n" +
                        "2. Left face image\n\n" +
                        "3. Right face image.\n\n" +
                        "This order is mandatory.")
                .setPositiveButton(android.R.string.ok, (dialog, which) -> triggerTakePictureButtonClick())
                .setCancelable(false)
                .show();
    }

    private void triggerTakePictureButtonClick() {
        Button captureButton = findViewById(R.id.Button);
        captureButton.performClick();
    }

    private void showImageSavedToast() {
        String message = String.format("Image Number %s Saved", capturedImages);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showCompletionPopup() {
        new AlertDialog.Builder(this)
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
        Intent resultIntent = new Intent(this, MainActivity.class);
        resultIntent.putExtra(IMAGE_FILE_PATHS_KEY, imageFilePaths);
        resultIntent.putExtra("source", "CameraFragment");
        startActivity(resultIntent);
    }

    private void showMaxImagesPopup() {
        new AlertDialog.Builder(this)
                .setTitle("Maximum Images Captured")
                .setMessage("You have captured the maximum number of images.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void resetCapture() {
        capturedImages = 0;
        imageFilePaths.clear();
        updateCounterText();
        ImageButton leftArrow = findViewById(R.id.LeftArrow);
        ImageButton rightArrow = findViewById(R.id.RightArrow);
        leftArrow.setVisibility(View.INVISIBLE);
        rightArrow.setVisibility(View.INVISIBLE);
        cameraView.setImageBitmap(null);
        updateProceedButtonState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.capture_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else if (item.getItemId() == R.id.action_button_remove) {
            showClearImagePopup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showClearImagePopup() {
        if (capturedImages > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Captured Image")
                    .setMessage("Are you sure you want to remove the captured image/s?")
                    .setPositiveButton("OK", (dialog, which) -> clearCapturedImage())
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            showNoImagesPopup();
        }
    }

    private void showNoImagesPopup() {
        new AlertDialog.Builder(this)
                .setTitle("No Images Captured")
                .setMessage("There are no captured images to remove.")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void clearCapturedImage() {
        capturedImages = 0;
        updateCounterText();
        cameraView.setImageBitmap(null);
        closeButton.setVisibility(View.GONE);
        imageFilePaths.clear();
        updateArrowButtonsState();
        updateProceedButtonState();
        Toast.makeText(this, "Captured image/s removed", Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float startX = 0; // Initialize startX here
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
        return super.onTouchEvent(event);
    }


    private void showPreviousImage() {
        if (currentImageIndex > 0 && currentImageIndex < imageFilePaths.size()) {
            currentImageIndex--;
            loadImageAtIndex(currentImageIndex);
        }
        updateArrowButtonsState();
    }

    private void showNextImage() {
        if (currentImageIndex >= 0 && currentImageIndex < imageFilePaths.size() - 1) {
            currentImageIndex++;
            loadImageAtIndex(currentImageIndex);
        }
        updateArrowButtonsState();
    }

    private void loadImageAtIndex(int index) {
        if (!imageFilePaths.isEmpty() && index >= 0 && index < imageFilePaths.size()) {
            String imagePath = imageFilePaths.get(index);
            loadFullResolutionImage(imagePath);
            updateCounterText();
        }
    }

    private void updateArrowButtonsState() {
        ImageButton leftArrow = findViewById(R.id.LeftArrow);
        ImageButton rightArrow = findViewById(R.id.RightArrow);

        leftArrow.setEnabled(currentImageIndex > 0);
        rightArrow.setEnabled(currentImageIndex < imageFilePaths.size() - 1);

        if (imageFilePaths.size() >= 2) {
            leftArrow.setVisibility(View.VISIBLE);
            rightArrow.setVisibility(View.VISIBLE);
        } else {
            leftArrow.setVisibility(View.INVISIBLE);
            rightArrow.setVisibility(View.INVISIBLE);
        }
    }
}
