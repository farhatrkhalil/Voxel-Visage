package com.example.voxelvisage;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        WebView webView = findViewById(R.id.modelWebView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.loadUrl("file:///android_res/raw/model_viewer.html");

        setTitle("3D Model");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.result_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = getIntent();
        String source = intent.getStringExtra("source");

        if ("GalleryViewerActivity".equals(source)) {
            Intent galleryIntent = new Intent(this, GalleryViewerActivity.class);
            startActivity(galleryIntent);
            finish();
        }
    }
}
