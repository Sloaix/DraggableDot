package com.lsxiao.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * author:lsxiao
 * date:2015/12/23 19:01
 */
public class DotView extends TextView {
    static DraggableLayout sDraggableLayout;
    Circle mCircle;
    Paint mPaint;
    float mMaxStretchLength;
    private int mRadius;
    int mCircleColor;

    public DotView(Context context) {
        this(context, null);
    }

    public DotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.DotView,
                0, 0);

        try {
            mRadius = a.getDimensionPixelOffset(R.styleable.DotView_radius, 40);
            mCircleColor = a.getColor(R.styleable.DotView_circle_color, Color.RED);
            mMaxStretchLength = a.getDimensionPixelOffset(R.styleable.DotView_max_stretch_length, 400);
        } finally {
            a.recycle();
        }
        init();
    }

    private void init() {
        setGravity(Gravity.CENTER);
        setDrawingCacheEnabled(true);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mCircle = new Circle(mRadius, mRadius, mRadius);
    }

    public DraggableLayout findDraggableLayout() {
        Activity activity = (Activity) getContext();
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
        for (int i = 0; i < decorView.getChildCount(); i++) {
            View view = decorView.getChildAt(i);
            if (view == null) {
                break;
            }
            if (view instanceof DraggableLayout) {
                return (DraggableLayout) view;
            }
        }
        return null;
    }

    public Circle getCopyCicle() {
        return Circle.copy(mCircle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!(getContext() instanceof Activity)) {
            throw new IllegalArgumentException("you must provide a activity as context");
        }

        if (sDraggableLayout == null) {
            sDraggableLayout = findDraggableLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureSpec = MeasureSpec.makeMeasureSpec(mRadius * 2, MeasureSpec.EXACTLY);
        setMeasuredDimension(measureSpec, measureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mCircle.draw(canvas, mPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            sDraggableLayout.preDrawDrag(this, ev);
        }
        return super.onTouchEvent(ev);
    }

    public float getMaxStretchLength() {
        return mMaxStretchLength;
    }

    public void setMaxStretchLength(float maxStretchLength) {
        mMaxStretchLength = maxStretchLength;
    }
}
