package com.example.voxelvisage.ui.settings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.example.voxelvisage.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference developersPreference = findPreference("developers_info");
        assert developersPreference != null;
        developersPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Developers");
                builder.setMessage("Voxel Visage is developed by:\n\n" +
                        "Abed El Fattah Amouneh\n" +
                        "Khalil Farhat\n" +
                        "Sammy Medawar\n" +
                        "Wally El Sayed");

                builder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();

                return true;
            }
        });

        Preference privacyPolicyPreference = findPreference("privacy_policy");
        assert privacyPolicyPreference != null;
        privacyPolicyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("Privacy Policy");
                builder.setMessage("Voxel Visage Privacy Policy:\n\n" +
                        "Your privacy is important to us. This app does not collect personal information.");

                builder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();

                return true;
            }
        });

        Preference licensePreference = findPreference("license");
        assert licensePreference != null;
        licensePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle("App License");
                builder.setMessage("This app is licensed under the MIT License. " +
                        "The MIT License is a type of software license that allows you, the user, a great deal of freedom. " +
                        "It means you can use, modify, and distribute this app according to the terms of the MIT License. " +
                        "You are free to do so as long as you provide appropriate attribution or credit to the original authors.\n" +
                        "Thank you for using our app!");


                builder.setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                });

                builder.show();

                return true;
            }
        });
    }
}
