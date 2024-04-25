package com.voxelvisage.modelviewer.camera;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.voxelvisage.modelviewer.R;

import java.util.Objects;

public class CameraActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        showInstructionsPopup();
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
        } else if (item.getItemId() == R.id.action_button_remove) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                .setPositiveButton(android.R.string.ok, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }
}