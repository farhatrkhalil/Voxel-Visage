package com.example.voxelvisage.ui.camera;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

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
    private ArrayList<String> imageFilePaths = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_camera, container, false);

        cameraView = rootView.findViewById(R.id.CameraView);
        Button captureButton = rootView.findViewById(R.id.Button);
        counterTextView = rootView.findViewById(R.id.CounterTextView);

        updateCounterText();

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (capturedImages < MAX_IMAGES) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                    }
                } else {
                    showMaxImagesPopup();
                }
            }
        });

        if (savedInstanceState == null) {
            showInstructionsPopup();
        }

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            if (extras != null) {

                Bitmap capturedImage = (Bitmap) data.getExtras().get("data");
                String imagePath = saveImageToFile(capturedImage);

                loadFullResolutionImage(imagePath);

                capturedImages++;
                updateCounterText();
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

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
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

    private void updateCounterText() {
        counterTextView.setText(String.format("%d/%d", capturedImages, MAX_IMAGES));
    }

    private void showInstructionsPopup() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Instructions")
                .setMessage("Capture 5 images, and then click on proceed to navigate to the result page.")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        triggerTakePictureButtonClick();
                    }
                })
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
                .setPositiveButton("Proceed", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        navigateToResultPage();
                    }
                })
                .setNegativeButton("Reset", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetCapture();
                    }
                })
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
        updateCounterText();
        cameraView.setImageBitmap(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.capture_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            clearCapturedImage();
                        }
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
        Toast.makeText(requireContext(), "Captured image removed", Toast.LENGTH_SHORT).show();
    }
}
