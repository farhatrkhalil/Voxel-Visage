package com.example.voxelvisage;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import okhttp3.logging.HttpLoggingInterceptor;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.voxelvisage.api.ApiClient;
import com.example.voxelvisage.api.ApiService;
import com.example.voxelvisage.api.FaceModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ResultActivity extends AppCompatActivity {

    private LinearLayout imageContainer;
    private String source;
    private ApiService apiService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        apiService = ApiClient.getClient().create(ApiService.class);
        imageContainer = findViewById(R.id.imageContainer);

        Intent intent = getIntent();
        if (intent != null) {
            ArrayList<String> imageFilePaths = intent.getStringArrayListExtra("imageFilePaths");
            ArrayList<Uri> selectedImages = intent.getParcelableArrayListExtra("selectedImages");

            source = intent.getStringExtra("source");

            if (imageFilePaths != null && !imageFilePaths.isEmpty()) {
                sendImagesToApi(imageFilePaths);
            } else if (selectedImages != null && !selectedImages.isEmpty()) {
                displayImagesUris(selectedImages);
            }
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }

    private void sendImagesToApi(List<String> imagePaths) {
        Log.d("API Call", "Sending images to API: " + imagePaths.toString()); // Log the list of image paths being sent

        // Create a MultipartBody.Builder to build the request body
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);

        // Add each image file to the request body
        for (String imagePath : imagePaths) {
            File file = new File(imagePath);
            if (file.exists()) {
                // Add the file part with the field name "files"
                builder.addFormDataPart("files", file.getName(),
                        RequestBody.create(MediaType.parse("image/*"), file));
            }
        }

        // Build the request body
        RequestBody requestBody = builder.build();

        // Create Retrofit instance with logging interceptor
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClient.addInterceptor(loggingInterceptor);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiClient.getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        // Make the API call with the request body
        Call<List<FaceModel>> call = apiService.processImages(requestBody);
        call.enqueue(new Callback<List<FaceModel>>() {
            @Override
            public void onResponse(Call<List<FaceModel>> call, Response<List<FaceModel>> response) {
                if (response.isSuccessful()) {
                    List<FaceModel> faceModels = response.body();
                    displayImagesWithLandmarks(faceModels);
                } else {
                    Log.e("API Call", "Unsuccessful response: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<FaceModel>> call, Throwable t) {
                Log.e("API Call", "Failed to make API call", t);
            }
        });
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

    private void displayImagesWithLandmarks(List<FaceModel> faceModels) {
        for (FaceModel faceModel : faceModels) {
            String imagePath = faceModel.getOriginalImage();
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Log.e("ImagePath", "Image file exists at path: " + imagePath); // Log the existence of the image file
                Bitmap originalImageBitmap = BitmapFactory.decodeFile(imagePath);
                if (originalImageBitmap != null) {
                    List<List<Integer>> landmarkCoordinates = faceModel.getLandmarksCoordinates();
                    drawLandmarks(originalImageBitmap, landmarkCoordinates);
                    ImageView imageView = new ImageView(this);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            0, 1f);
                    layoutParams.setMargins(0, 10, 0, 0);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setImageBitmap(originalImageBitmap);
                    imageContainer.addView(imageView);
                } else {
                    Log.e("BitmapDecode", "Failed to decode bitmap for file: " + imagePath);
                }
            } else {
                Log.e("FileNotFound", "Image file not found at path: " + imagePath);
            }
        }
    }




    private void drawLandmarks(Bitmap image, List<List<Integer>> landmarkCoordinates) {
        Canvas canvas = new Canvas(image);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(5);

        for (List<Integer> coordinates : landmarkCoordinates) {
            int x = coordinates.get(0);
            int y = coordinates.get(1);
            canvas.drawCircle(x, y, 5, paint);
        }
    }

    private void displayImagesUris(ArrayList<Uri> imageUris) {
        // Convert URIs to paths
        List<String> imagePaths = new ArrayList<>();
        for (Uri imageUri : imageUris) {
            imagePaths.add(getRealPathFromUri(imageUri));
        }

        sendImagesToApi(imagePaths);
    }

    private String getRealPathFromUri(Uri uri) {
        String result;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}

