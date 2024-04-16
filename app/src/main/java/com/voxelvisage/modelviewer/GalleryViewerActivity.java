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

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;
import com.voxelvisage.modelviewer.RetrofitClient;

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

        checkApiConnectivity();

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

    private void checkApiConnectivity() {
        new Thread(() -> {
            try {
                URL url = new URL("http://172.25.112.1:3003/check_api");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    String apiURL = connection.getURL().toString();
                    runOnUiThread(() -> {
                        Toast.makeText(GalleryViewerActivity.this, String.format("API Connected at: %s", apiURL), Toast.LENGTH_LONG).show();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(GalleryViewerActivity.this, "API Connection Failed", Toast.LENGTH_SHORT).show();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(GalleryViewerActivity.this, "API Connection Failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void uploadImages() {

        if (selectedImages.size() != 3) {
            Toast.makeText(this, "Please select 3 images", Toast.LENGTH_SHORT).show();
            return;
        }

        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri imageUri : selectedImages) {
            String imagePath = getImagePath(imageUri);
            if (imagePath == null) {
                // Handle error if path retrieval fails
                continue;
            }

            File imageFile = new File(imagePath);
            RequestBody requestBody = RequestBody.create(imageFile, MediaType.parse("image/*"));
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("files", imageFile.getName(), requestBody);
            imageParts.add(imagePart);
        }

        // Get an instance of ApiService (implementation detail)
        ApiService apiService = RetrofitClient.getInstance().create(ApiService.class);

        Call<YourResponseModel> call = apiService.uploadImages(imageParts);

        // Handle the asynchronous call
        call.enqueue(new Callback<YourResponseModel>() {
            @Override
            public void onResponse(Call<YourResponseModel> call, Response<YourResponseModel> response) {
                if (response.isSuccessful()) {
                    // Handle successful upload response (show success message etc.)
                    YourResponseModel responseModel = response.body();
                    // Access response data from responseModel
                } else {
                    // Handle upload failure (show error message etc.)
                    int statusCode = response.code();
                    // Handle error based on status code
                }
            }

            @Override
            public void onFailure(Call<YourResponseModel> call, Throwable t) {
                // Handle network error or other failures
                Toast.makeText(GalleryViewerActivity.this, "Upload Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_viewer_menu, menu);
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
            } else if (!selectedImages.isEmpty()) {
                selectedImages.clear();
                finish();
            }
            return true;
        } else if (item.getItemId() == R.id.action_process) {
            uploadImages();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return false;
    }


    private void showPopupMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Image Selection Requirement");
        builder.setMessage("Please select 3 facial images and click on the right arrow to proceed.\n\nThe order must be as follows:\n\n" +
                "1• Centered face image\n\n" +
                "2• Left face image\n\n" +
                "3• Right face image.\n\n" +
                "This order is mandatory.");
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

    private String getImagePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(columnIndex);
            cursor.close();
            return path;
        }
        return null;
    }
}
