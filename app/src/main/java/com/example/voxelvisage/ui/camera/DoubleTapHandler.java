package com.example.voxelvisage.ui.camera;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class DoubleTapHandler {

    private GestureDetector gestureDetector;
    private OnDoubleTapListener onDoubleTapListener;

    public DoubleTapHandler(OnDoubleTapListener onDoubleTapListener) {
        this.onDoubleTapListener = onDoubleTapListener;
        initGestureDetector();
    }

    private void initGestureDetector() {
        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (onDoubleTapListener != null) {
                    onDoubleTapListener.onDoubleTap();
                }
                return true;
            }
        });
    }

    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    public interface OnDoubleTapListener {
        void onDoubleTap();
    }
}
