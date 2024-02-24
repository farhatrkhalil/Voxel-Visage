package com.example.voxelvisage;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
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

    private static final int REQUEST_CODE_PICK_IMAGES = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                showPlusMenu(findViewById(R.id.navigation_home));
                return true;
            }

            return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment_activity_main))
                    || super.onOptionsItemSelected(item);
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
        if (currentDestinationId == R.id.navigation_home && item.getItemId() == R.id.navigation_home) {
            showPlusMenu(findViewById(R.id.navigation_home));
            return true;
        }

        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment_activity_main))
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, (Openable) null)
                || super.onSupportNavigateUp();
    }

    private void showPlusMenu(android.view.View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.plus_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_capture) {
                return true;
            } else if (item.getItemId() == R.id.action_choose_from_gallery) {
                openGallery();
                return true;
            } else if (item.getItemId() == R.id.action_cancel) {
                NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
                navController.navigate(R.id.navigation_home);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(this, GalleryViewerActivity.class);
        startActivity(galleryIntent);
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
                    menuItem.setIcon(R.drawable.plusfilled);
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
            return R.drawable.plus;
        }
        return 0;
    }
}
