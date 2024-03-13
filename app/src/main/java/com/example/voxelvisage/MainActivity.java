package com.example.voxelvisage;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.customview.widget.Openable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.example.voxelvisage.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private int currentDestinationId;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
                return true;
            } else {
                return NavigationUI.onNavDestinationSelected(item, navController)
                        || super.onOptionsItemSelected(item);
            }
        });

        navController.navigate(R.id.navigation_home);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Voxel Visage");

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateIcon(destination.getId());
            currentDestinationId = destination.getId();
            invalidateOptionsMenu();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentDestinationId == R.id.navigation_home) {
            getMenuInflater().inflate(R.menu.camera_menu, menu);
        } else if (currentDestinationId == R.id.navigation_settings) {
            menu.clear();
        } else {
            getMenuInflater().inflate(R.menu.bottom_nav_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (currentDestinationId == R.id.navigation_settings && item.getItemId() == R.id.navigation_settings) {
            Log.d("Navigation", "Settings clicked");
            navController.navigate(R.id.navigation_settings);
            return true;
        } else if (item.getItemId() == R.id.action_button_share) {
            shareOptionsDialog();
            return true;
        } else {
            return NavigationUI.onNavDestinationSelected(item, navController)
                    || super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, (Openable) null)
                || super.onSupportNavigateUp();
    }

    private void updateIcon(int itemId) {
        BottomNavigationView bottomNavigationView = findViewById(R.id.nav_view);
        Menu menu = bottomNavigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.getItemId() == itemId) {
                if (itemId == R.id.navigation_settings) {
                    menuItem.setIcon(R.drawable.settingsfilled);
                } else if (itemId == R.id.navigation_home) {
                    menuItem.setIcon(R.drawable.homefilled);
                }
            } else {
                menuItem.setIcon(getDefaultIcon(menuItem.getItemId()));
            }
        }
    }

    private int getDefaultIcon(int itemId) {
        if (itemId == R.id.navigation_settings) {
            return R.drawable.settings;
        } else if (itemId == R.id.navigation_home) {
            return R.drawable.home;
        }
        return 0;
    }

    private void shareOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thanks for sharing our app!")
                .setItems(new CharSequence[]{"Share Url", "Copy Url", "Cancel"},
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                handleShareOptionSelection(which);
                            }
                        });

        builder.create().show();
    }

    private void handleShareOptionSelection(int selectedOption) {
        String url = "https://github.com/farhatrkhalil/Voxel-Visage";
        switch (selectedOption) {
            case 0:
                shareUrl(url);
                break;
            case 1:
                copyToClipboard(url);
                Toast.makeText(MainActivity.this, "URL copied to clipboard", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                break;
        }
    }

    private void copyToClipboard(String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    private void shareUrl(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent, "Share URL"));
    }
}
