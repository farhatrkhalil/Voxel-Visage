package com.example.voxelvisage.ui.camera;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class PinchToZoomHandler {

    private final ScaleGestureDetector scaleGestureDetector;
    private final OnPinchListener pinchListener;

    public interface OnPinchListener {
        void onPinch(float scaleFactor);
    }

    public PinchToZoomHandler(Context context, OnPinchListener pinchListener) {
        this.pinchListener = pinchListener;
        this.scaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener());
    }

    public boolean onTouchEvent(MotionEvent event) {
        return scaleGestureDetector.onTouchEvent(event);
    }

    private class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            pinchListener.onPinch(scaleFactor);
            return true;
        }
    }
}
