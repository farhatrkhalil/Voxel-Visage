package com.example.voxelvisage;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class GalleryViewerActivity extends AppCompatActivity {

    private GridView gridView;
    private GalleryAdapter galleryAdapter;
    private ArrayList<Uri> selectedImages;
    private boolean[] selectedStates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery_viewer);

        gridView = findViewById(R.id.gridView);
        selectedImages = new ArrayList<>();

        loadImages();

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            toggleSelection(position);
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_process) {
            if (selectedImages.size() == 5) {
                finish();
            } else {
                Toast.makeText(this, "Please select 5 images", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }


    private void loadImages() {
        String[] projection = {MediaStore.Images.Media.DATA};
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
                String imagePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                Uri imageUri = Uri.parse("file://" + imagePath);
                allImages.add(imageUri);
            }
            cursor.close();
        }

        selectedStates = new boolean[allImages.size()];

        galleryAdapter = new GalleryAdapter(this, allImages, selectedStates);
        gridView.setAdapter(galleryAdapter);
    }

    private void toggleSelection(int position) {
        selectedStates[position] = !selectedStates[position];

        if (selectedStates[position]) {
            selectedImages.add((Uri) galleryAdapter.getItem(position));
        } else {
            selectedImages.remove(galleryAdapter.getItem(position));
        }

        galleryAdapter.notifyDataSetChanged();
    }
}
