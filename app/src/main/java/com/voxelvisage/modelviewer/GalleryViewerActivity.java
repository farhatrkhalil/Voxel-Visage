package com.voxelvisage.modelviewer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class GalleryViewerActivity extends AppCompatActivity {

    private GridView gridView;
    private GalleryAdapter galleryAdapter;
    private ArrayList<Uri> selectedImages;
    private boolean[] selectedStates;
    private boolean noImagesPopupShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_viewer);

        gridView = findViewById(R.id.gridView);
        gridView.setSelector(android.R.color.transparent);
        selectedImages = new ArrayList<>();

        loadImages();

        showPopupMessage();

        FrameLayout galleryContainer = findViewById(R.id.gallery_container);
        galleryContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int actionBarHeight = getSupportActionBar().getHeight();

                int reducedPadding = actionBarHeight - dpToPx(30);

                galleryContainer.setPadding(0, reducedPadding, 0, 0);

                galleryContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

        gridView.setOnItemClickListener((parent, view, position, id) -> toggleSelection(position));

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (selectedImages.isEmpty() && !noImagesPopupShown) {
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.processButton) {
            if (selectedImages.size() == 3) {
                Intent intent = new Intent(GalleryViewerActivity.this, MainActivity.class);
                intent.putParcelableArrayListExtra("selectedImages", selectedImages);
                intent.putExtra("source", "GalleryViewerActivity");
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Please select 3 images", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void showPopupMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Image Selection Requirement");
        builder.setMessage("Please select 3 images and click on the right arrow to proceed.");
        builder.setPositiveButton("OK", (dialog, which) -> {
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void loadImages() {
        String[] projection = {MediaStore.Images.Media._ID};
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                null
        );

        ArrayList<Uri> allImages = new ArrayList<>();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                long imageId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageId);
                allImages.add(imageUri);
            }
            cursor.close();
        }

        if (allImages.isEmpty()) {
            showNoImagesPopup();
            return;
        }

        Collections.reverse(allImages);

        selectedStates = new boolean[allImages.size()];

        galleryAdapter = new GalleryAdapter(this, allImages, selectedStates);
        gridView.setAdapter(galleryAdapter);
    }

    private void showNoImagesPopup() {
        noImagesPopupShown = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Images Found");
        builder.setMessage("Either you have denied permission to access images or there are no images in your gallery.");
        builder.setPositiveButton("OK", (dialog, which) -> finish());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void toggleSelection(int position) {
        if (selectedImages.size() >= 3 && !selectedStates[position]) {
            Toast.makeText(this, "You can only select up to 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        selectedStates[position] = !selectedStates[position];

        if (selectedStates[position]) {
            selectedImages.add(galleryAdapter.getItem(position));
        } else {
            selectedImages.remove(galleryAdapter.getItem(position));
        }

        View view = gridView.getChildAt(position - gridView.getFirstVisiblePosition());
        if (view != null) {
            galleryAdapter.updateView(position, view);
        }
    }
}
