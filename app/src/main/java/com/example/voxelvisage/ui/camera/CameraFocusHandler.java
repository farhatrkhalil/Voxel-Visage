// CameraFocusHandler.java
package com.example.voxelvisage.ui.camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;  // Import Log
import android.view.MotionEvent;
import android.view.View;

public class CameraFocusHandler extends View {

    private static final String TAG = "CameraFocusHandler";  // Add a TAG for logging

    private float focusX;
    private float focusY;

    public CameraFocusHandler(Context context) {
        super(context);
        init();
    }

    public CameraFocusHandler(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setFocusArea(float x, float y) {
        focusX = x;
        focusY = y;
        invalidate();
        setVisibility(View.VISIBLE);

        Log.d(TAG, "setFocusArea: " + focusX + ", " + focusY);  // Log the focus area
    }

    private void init() {
        setWillNotDraw(false);
        setVisibility(View.GONE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw a yellow focus square at the touched location
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);

        float squareSize = 100f;
        float left = focusX - squareSize / 2;
        float top = focusY - squareSize / 2;
        float right = focusX + squareSize / 2;
        float bottom = focusY + squareSize / 2;

        canvas.drawRect(left, top, right, bottom, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                setFocusArea(event.getX(), event.getY());
                return true;
            default:
                return false;
        }
    }

}
