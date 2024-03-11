package com.example.voxelvisage.ui.settings;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.navigation.Navigation;
import androidx.preference.CheckBoxPreference;
import androidx.navigation.NavController;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.example.voxelvisage.R;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat {

    private boolean isSystemDarkMode;
    private Handler handler = new Handler(Looper.getMainLooper());

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        CheckBoxPreference notificationsPreference = findPreference("notifications");
        assert notificationsPreference != null;
        CheckBoxPreference darkModePreference = findPreference("darkMode");
        assert darkModePreference != null;

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        setThemeIcons();

        setNotificationsIcon(notificationsPreference, isSystemDarkMode);

        notificationsPreference.setOnPreferenceClickListener(preference -> {
            boolean areNotificationsEnabled = notificationsPreference.isChecked();
            updateNotificationsIcon(notificationsPreference, areNotificationsEnabled, isSystemDarkMode);
            return true;
        });

        darkModePreference.setChecked(isSystemDarkMode);

        darkModePreference.setOnPreferenceClickListener(preference -> {
            boolean isDarkModeEnabled = darkModePreference.isChecked();
            updateTheme(isDarkModeEnabled);
            return true;
        });

        Objects.requireNonNull(getPreferenceManager().getSharedPreferences()).registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
            assert key != null;
            if (key.equals("darkMode")) {
                setThemeIcons();
                handler.postDelayed(() -> setNotificationsIcon(notificationsPreference, isSystemDarkMode), 100);
            }
        });

        Preference developersPreference = findPreference("developers_info");
        assert developersPreference != null;
        developersPreference.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Developers");
            builder.setMessage("Voxel Visage is developed by:\n\n" +
                    "Abed El Fattah Amouneh\n" +
                    "Khalil Farhat\n" +
                    "Sammy Medawar\n" +
                    "Wally El Sayed");

            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            builder.show();

            return true;
        });

        Preference privacyPolicyPreference = findPreference("privacy_policy");
        assert privacyPolicyPreference != null;
        privacyPolicyPreference.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Privacy Policy");
            builder.setMessage("Voxel Visage Privacy Policy:\n\n" +
                    "Your privacy is important to us. This app does not collect personal information.");

            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            builder.show();

            return true;
        });

        Preference licensePreference = findPreference("license");
        assert licensePreference != null;
        licensePreference.setOnPreferenceClickListener(preference -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("App License");
            builder.setMessage("This app is licensed under the MIT License. " +
                    "The MIT License is a type of software license that allows you, the user, a great deal of freedom. " +
                    "It means you can use, modify, and distribute this app according to the terms of the MIT License. " +
                    "You are free to do so as long as you provide appropriate attribution or credit to the original authors.\n" +
                    "Thank you for using our app!");

            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

            builder.show();

            return true;
        });
    }

    private void setCheckBoxPreferenceIcon(int lightModeIconResId, int darkModeIconResId, boolean isSystemDarkMode) {
        CheckBoxPreference checkBoxPreference = findPreference("darkMode");
        if (checkBoxPreference != null) {
            int iconResourceId = isSystemDarkMode ? darkModeIconResId : lightModeIconResId;
            Drawable iconDrawable = getDrawable(iconResourceId);
            checkBoxPreference.setIcon(iconDrawable);
        }
    }

    private void setPreferenceIcon(String key, int lightModeIconResId, int darkModeIconResId, boolean isSystemDarkMode) {
        Preference preference = findPreference(key);
        if (preference != null) {
            int iconResourceId = isSystemDarkMode ? darkModeIconResId : lightModeIconResId;
            Drawable iconDrawable = getDrawable(iconResourceId);
            preference.setIcon(iconDrawable);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getDrawable(int resourceId) {
        return getResources().getDrawable(resourceId, requireContext().getTheme());
    }

    private void setNotificationsIcon(CheckBoxPreference preference, boolean isSystemDarkMode) {
        int iconResourceId = isSystemDarkMode ? R.drawable.notifications_light_off : R.drawable.notifications_dark_off;
        Drawable iconDrawable = getDrawable(iconResourceId);
        preference.setIcon(iconDrawable);
    }

    private void updateNotificationsIcon(CheckBoxPreference preference, boolean areNotificationsEnabled, boolean isSystemDarkMode) {
        int iconResourceId;
        if (areNotificationsEnabled) {
            iconResourceId = isSystemDarkMode ? R.drawable.notifications_light_on : R.drawable.notifications_dark_on;
        } else {
            iconResourceId = isSystemDarkMode ? R.drawable.notifications_light_off : R.drawable.notifications_dark_off;
        }
        Drawable iconDrawable = getDrawable(iconResourceId);
        preference.setIcon(iconDrawable);
    }

    private void updateTheme(boolean isDarkModeEnabled) {
        int nightMode = isDarkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightMode);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
        editor.putBoolean("darkMode", isDarkModeEnabled);
        editor.apply();

        handler.postDelayed(() -> {
            if (isAdded() && getContext() != null) {
                setThemeIcons();
                setNotificationsIcon(findPreference("notifications"), isSystemDarkMode);
            }
        }, 200);

        navigateToHomeFragment();
    }



    private void navigateToHomeFragment() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_home);
    }

    private void setThemeIcons() {
        setCheckBoxPreferenceIcon(R.drawable.darkmode_dark, R.drawable.darkmode_light, isSystemDarkMode);
        setPreferenceIcon("developers_info", R.drawable.developers_dark, R.drawable.developers_light, isSystemDarkMode);
        setPreferenceIcon("privacy_policy", R.drawable.privacypolicy_dark, R.drawable.privacypolicy_light, isSystemDarkMode);
        setPreferenceIcon("license", R.drawable.license_dark, R.drawable.license_light, isSystemDarkMode);
        setPreferenceIcon("version", R.drawable.version_dark, R.drawable.version_light, isSystemDarkMode);
    }
}
