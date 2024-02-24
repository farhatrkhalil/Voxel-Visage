package com.example.voxelvisage.ui.camera;

import android.widget.ImageButton;

import com.example.voxelvisage.R;

public class FlashHandler {

    private static boolean isFlashOn = false;

    public static void enableFlash(ImageButton flashButton) {
        isFlashOn = !isFlashOn;

        updateFlashButtonIcon(flashButton);

    }

    private static void updateFlashButtonIcon(ImageButton flashButton) {
        int originalWidth = flashButton.getWidth();
        int originalHeight = flashButton.getHeight();

        if (isFlashOn) {
            flashButton.setImageResource(R.drawable.flashon);
        } else {
            flashButton.setImageResource(R.drawable.flashoff);
        }

        flashButton.getLayoutParams().width = originalWidth;
        flashButton.getLayoutParams().height = originalHeight;

        flashButton.setScaleType(ImageButton.ScaleType.FIT_CENTER);

        flashButton.requestLayout();
    }

}
