package com.example.voxelvisage.ui.settings;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public SettingsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is settings fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }

    // Method to handle opening Popup 1
    public void openPopup1() {
        // Implement logic to show Popup 1
        // For example, use a DialogFragment
        // You can use a library like Material Components for Android for creating dialogs
    }

    // Method to handle opening Popup 2
    public void openPopup2() {
        // Implement logic to show Popup 2
        // For example, use a DialogFragment
        // You can use a library like Material Components for Android for creating dialogs
    }
}
