package com.example.voxelvisage;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.voxelvisage.R;

import java.util.ArrayList;
import java.util.Objects;

public class ResultActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        imageView = findViewById(R.id.imageView);

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<Uri> selectedImages = intent.getParcelableArrayListExtra("selectedImages");
            if (selectedImages != null && !selectedImages.isEmpty()) {
                displayImage(selectedImages.get(0));
            }
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            navigateToGalleryActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToGalleryActivity() {
        Intent intent = new Intent(this, GalleryViewerActivity.class);
        startActivity(intent);
        finish();
    }

    private void displayImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .into(imageView);
    }
}
