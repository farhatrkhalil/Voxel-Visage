package com.example.voxelvisage;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voxelvisage.ui.camera.CameraFragment;

import java.util.ArrayList;
import java.util.Objects;

public class ResultActivity extends AppCompatActivity {

    private LinearLayout imageContainer;
    private String source;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageContainer = findViewById(R.id.imageContainer);

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<String> imageFilePaths = intent.getStringArrayListExtra("imageFilePaths");
            ArrayList<Uri> selectedImages = intent.getParcelableArrayListExtra("selectedImages");

            source = intent.getStringExtra("source");

            if (imageFilePaths != null && !imageFilePaths.isEmpty()) {
                displayImages(imageFilePaths);
            } else if (selectedImages != null && !selectedImages.isEmpty()) {
                displayImagesUris(selectedImages);
            }
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateBack();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateBack() {
        if ("GalleryViewerActivity".equals(source)) {
            Intent intent = new Intent(this, GalleryViewerActivity.class);
            startActivity(intent);
            finish();
        } else if ("CameraFragment".equals(source)) {
            getSupportFragmentManager().popBackStack();
            finish();
        } else {
            finish();
        }
    }

    private void displayImages(ArrayList<String> imageFilePaths) {
        for (String imagePath : imageFilePaths) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1f);
            layoutParams.setMargins(0, 10, 0, 0);
            imageView.setLayoutParams(layoutParams);

            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
            imageView.setImageBitmap(imageBitmap);

            imageContainer.addView(imageView);
        }
    }


    private void displayImagesUris(ArrayList<Uri> imageUris) {
        for (Uri imageUri : imageUris) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0, 1f);
            layoutParams.setMargins(0, 10, 0, 0);
            imageView.setLayoutParams(layoutParams);

            imageView.setImageURI(imageUri);

            imageContainer.addView(imageView);
        }
    }
}
