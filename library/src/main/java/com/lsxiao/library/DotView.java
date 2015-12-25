package com.lsxiao.library;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * author:lsxiao
 * date:2015/12/23 19:01
 */
public class DotView extends View {
    //固定住的圆
    Circle mFixedCircle;
    //画笔
    Paint mPaint;
    Activity mActivity;

    public DotView(Context context) {
        this(context, null);
    }

    public DotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void init(Activity activity) {
        mActivity = activity;
        DraggableLayout draggableLayout = new DraggableLayout(mActivity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        draggableLayout.setLayoutParams(params);

        ViewGroup contentView = (ViewGroup) activity.findViewById(android.R.id.content);
        View child = contentView.getChildAt(0);
        contentView.removeView(child);
        draggableLayout.addView(child);
        contentView.addView(draggableLayout);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mActivity == null) {
            throw new IllegalArgumentException("the activity is null,you must init first");
        }
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        mFixedCircle = new Circle(200, 200, 20);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
}
