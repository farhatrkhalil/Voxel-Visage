package com.example.voxelvisage.ui.settings;

import android.content.SharedPreferences;
import android.content.res.Configuration;
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
