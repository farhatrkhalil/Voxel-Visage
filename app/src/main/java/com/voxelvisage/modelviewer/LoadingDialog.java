package com.voxelvisage.modelviewer;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

public class LoadingDialog extends Dialog {

    public LoadingDialog(Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_loading);

        ImageView loadingAnimationView = findViewById(R.id.loadingAnimationView);

        Animation rotationAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.rotate_animation);

        loadingAnimationView.startAnimation(rotationAnimation);
    }
}
