package com.example.voxelvisage.ui.settings;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.example.voxelvisage.R;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
