package com.example.voxelvisage;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupWithNavController(binding.navView, navController);

        navView.setOnNavigationItemSelectedListener(item -> {
            navController.navigate(item.getItemId());
            return true;
        });

        navController.navigate(R.id.navigation_camera);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Voxel Visage");

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            updateIcon(destination.getId());
            currentDestinationId = destination.getId();
            invalidateOptionsMenu();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (currentDestinationId == R.id.navigation_camera) {
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
        return NavigationUI.onNavDestinationSelected(item, Navigation.findNavController(this, R.id.nav_host_fragment_activity_main))
                || super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
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
                } else if (itemId == R.id.navigation_camera) {
                    menuItem.setIcon(R.drawable.camerafilled);
                }
            } else {
                menuItem.setIcon(getDefaultIcon(menuItem.getItemId()));
            }
        }
    }

    private int getDefaultIcon(int itemId) {
        if (itemId == R.id.navigation_settings) {
            return R.drawable.settings;
        } else if (itemId == R.id.navigation_camera) {
            return R.drawable.camera;
        }
        return 0;
    }
}
