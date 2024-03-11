package com.example.voxelvisage.ui.settings;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

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

public class SettingsFragment extends PreferenceFragmentCompat {

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        CheckBoxPreference darkModePreference = findPreference("darkMode");
        assert darkModePreference != null;

        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isSystemDarkMode = currentNightMode == Configuration.UI_MODE_NIGHT_YES;

        setCheckBoxPreferenceIcon("darkMode", R.drawable.darkmode_dark, R.drawable.darkmode_light, isSystemDarkMode);
        setPreferenceIcon("developers_info", R.drawable.developers_dark, R.drawable.developers_light, isSystemDarkMode);
        setPreferenceIcon("privacy_policy", R.drawable.privacypolicy_dark, R.drawable.privacypolicy_light, isSystemDarkMode);
        setPreferenceIcon("license", R.drawable.license_dark, R.drawable.license_light, isSystemDarkMode);
        setPreferenceIcon("version", R.drawable.version_dark, R.drawable.version_light, isSystemDarkMode);


        darkModePreference.setChecked(isSystemDarkMode);

        darkModePreference.setOnPreferenceClickListener(preference -> {
            boolean isDarkModeEnabled = darkModePreference.isChecked();
            updateTheme(isDarkModeEnabled);
            return true;
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

    private void setCheckBoxPreferenceIcon(String key, int lightModeIconResId, int darkModeIconResId, boolean isSystemDarkMode) {
        CheckBoxPreference checkBoxPreference = findPreference(key);
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

    private Drawable getDrawable(int resourceId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getResources().getDrawable(resourceId, requireContext().getTheme());
        } else {
            // For versions below Lollipop
            return getResources().getDrawable(resourceId);
        }
    }


    private void updateTheme(boolean isDarkModeEnabled) {
        int nightMode = isDarkModeEnabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        AppCompatDelegate.setDefaultNightMode(nightMode);

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(requireContext()).edit();
        editor.putBoolean("darkMode", isDarkModeEnabled);
        editor.apply();

        navigateToHomeFragment();
    }

    private void navigateToHomeFragment() {
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.navigation_home);
    }


}
