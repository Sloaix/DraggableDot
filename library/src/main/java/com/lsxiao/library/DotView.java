package com.lsxiao.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * author:lsxiao
 * date:2015/12/23 19:01
 */
public class DotView extends View {
    //固定住的圆
    Circle mFixedCircle;
    //画笔
    Paint mPaint;
    float mMaxStretchLength;

    private int mSize;
    int mHintColor;

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
            mSize = a.getDimensionPixelOffset(R.styleable.DotView_size, 20);
            mHintColor = a.getColor(R.styleable.DotView_hit_color, Color.RED);
            mMaxStretchLength = a.getDimensionPixelOffset(R.styleable.DotView_max_stretch_length, 400);
        } finally {
            a.recycle();
        }
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        mFixedCircle = new Circle(mSize / 2, mSize / 2, mSize / 2);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureSpec = MeasureSpec.makeMeasureSpec(mSize, MeasureSpec.EXACTLY);
        setMeasuredDimension(measureSpec, measureSpec);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                return true;
            case MotionEvent.ACTION_UP:
                return false;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mFixedCircle.draw(canvas, mPaint);
    }

    public float getMaxStretchLength() {
        return mMaxStretchLength;
    }

    public void setMaxStretchLength(float maxStretchLength) {
        mMaxStretchLength = maxStretchLength;
    }
}
