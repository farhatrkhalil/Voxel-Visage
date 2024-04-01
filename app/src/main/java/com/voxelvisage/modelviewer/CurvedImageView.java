package com.voxelvisage.modelviewer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

public class CurvedImageView extends AppCompatImageView {
    private Path path;
    private Paint paint;

    public CurvedImageView(Context context) {
        super(context);
        init();
    }

    public CurvedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CurvedImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        path = new Path();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5);
        paint.setColor(getResources().getColor(R.color.Black));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float radius = 20;
        path.addRoundRect(0, 0, getWidth(), getHeight(), radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }
}
