package com.lsxiao.library;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
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
    private DraggableLayout mDraggableLayout;
    private Circle mBgCircle;
    private Paint mPaint;
    private float mLimitStretchLength;
    private int mRadius;
    private int mCircleColor;
    private onDotStateChangedListener mOnDotStateChangedListener;

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
            mRadius = a.getDimensionPixelOffset(R.styleable.DotView_xls_radius, dp2px(10));
            mCircleColor = a.getColor(R.styleable.DotView_xls_circle_color, Color.RED);
            mLimitStretchLength = a.getDimensionPixelOffset(R.styleable.DotView_xls_max_stretch_length, dp2px(100));
        } finally {
            a.recycle();
        }
        init();
    }

    private int dp2px(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    /**
     * initialize some local variables.
     */
    private void init() {
        setGravity(Gravity.CENTER);
        setDrawingCacheEnabled(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mCircleColor);
        mPaint.setStyle(Paint.Style.FILL);

        mBgCircle = new Circle(mRadius, mRadius, mRadius);
    }

    /**
     * to find the draggableLayout instance layout in the view tree.
     *
     * @return null, if don't find,else return DraggableLayout instance.
     */
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

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        //if is preview mode,then don't need draggableLayout instance.
        if (isInEditMode()) {
            return;
        }

        //retrieve the draggable instance.
        if (mDraggableLayout == null) {
            mDraggableLayout = findDraggableLayout();
            if (mDraggableLayout == null) {
                throw new IllegalArgumentException("the draggableLayout isn't be attached to view tree,you must invoke the attachToActivity method of DraggableLayout");
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measureSpec = MeasureSpec.makeMeasureSpec(mRadius * 2, MeasureSpec.EXACTLY);
        setMeasuredDimension(measureSpec, measureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mBgCircle.draw(canvas, mPaint);
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN && isEnabled() && mDraggableLayout != null) {
            mDraggableLayout.preDrawDrag(this, ev);
            mDraggableLayout.setCanIntercept(true);
            return true;
        }
        return false;
    }

    public float getLimitStretchLength() {
        return mLimitStretchLength;
    }

    public int getCircleColor() {
        return mCircleColor;
    }

    public onDotStateChangedListener getOnDotStateChangedListener() {
        return mOnDotStateChangedListener;
    }

    public void setOnDotStateChangedListener(onDotStateChangedListener onDotStateChangedListener) {
        mOnDotStateChangedListener = onDotStateChangedListener;
    }

    /**
     * the callback interface.
     */
    public interface onDotStateChangedListener {
        void onStretch(DotView dotView);

        void onDrag(DotView dotView);

        void onDismissed(DotView dotView);
    }

    /**
     * the simple callback implement.
     */
    public class SimpleDotStateChangedListener implements onDotStateChangedListener {
        @Override
        public void onStretch(DotView dotView) {
            //do nothing.
        }

        @Override
        public void onDrag(DotView dotView) {
            //do nothing.
        }

        @Override
        public void onDismissed(DotView dotView) {
            //do nothing.
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDraggableLayout = null;
        mOnDotStateChangedListener = null;
    }
}
