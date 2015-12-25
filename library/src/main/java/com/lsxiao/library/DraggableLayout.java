package com.lsxiao.library;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * author:lsxiao
 * date:2015/12/25 17:02
 */
public class DraggableLayout extends FrameLayout {
    DotView mDotView;
    Circle mDragCircle;
    Circle mFixedCircle;
    PointF mFixedPointA;
    PointF mFixedPointB;
    PointF mDragPointA;
    PointF mDragPointB;
    PointF mOperationPointA;
    PointF mOperationPointB;
    //画笔
    Paint mPaint;
    //路径
    Path mPath;

    float mStartPosX;
    float mStartPosY;

    float mLastPosX;
    float mLastPosY;

    /**
     * 是否可以拖动
     */
    public static boolean mDraggable = false;

    public static ValueAnimator valueAnimator;

    public DraggableLayout(Context context) {
        this(context, null);
    }

    public DraggableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWillNotDraw(false);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init();
    }

    private void init() {
        mDotView = findDotView(this);
        if (mDotView == null) {
            throw new NullPointerException("you must init first");
        }

        //实例化拖动助手
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        mPath = new Path();
    }

    private DotView findDotView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof DotView) {
                return (DotView) view;
            } else if (view instanceof ViewGroup) {
                return findDotView((ViewGroup) view);
            }
        }
        return null;
    }

    /**
     * 是否触摸到DotView
     *
     * @param event MotionEvent
     * @return true if touched on dot view
     */
    private boolean isTouchDot(MotionEvent event) {
        int[] location = new int[2];
        mDotView.getLocationOnScreen(location);
        final int w = mDotView.getMeasuredWidth();
        final int h = mDotView.getMeasuredHeight();
        int posX = (int) event.getRawX();
        int posY = (int) event.getRawY();

        int lowerX = location[0];
        int upperX = w + location[0];

        int lowerY = location[1];
        int upperY = h + location[1];
        log(lowerX, upperX);
        log(lowerY, upperY);
        log(posX, posY);
        return (posX > lowerX && posX < upperX && posY > lowerY && posY < upperY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mFixedCircle == null || mDragCircle == null) {
            initCircle();
        }

        calculatePoint();
        canvas.drawCircle(mFixedCircle.mCenter.x, mFixedCircle.mCenter.y, mFixedCircle.mRadius, mPaint);
        canvas.drawCircle(mDragCircle.mCenter.x, mDragCircle.mCenter.y, mDragCircle.mRadius, mPaint);
        drawDrag(canvas);
    }

    public void drawDrag(Canvas canvas) {
        mPath.reset();
        mPath.moveTo(mFixedPointA.x, mFixedPointA.y);
        mPath.quadTo(mOperationPointA.x, mOperationPointA.y, mDragPointA.x, mDragPointA.y);
        mPath.lineTo(mDragPointB.x, mDragPointB.y);
        mPath.quadTo(mOperationPointB.x, mOperationPointB.y, mFixedPointB.x, mFixedPointB.y);
        mPath.lineTo(mFixedPointA.x, mFixedPointA.y);
        mPath.close();
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 计算贝塞尔曲线的六个点
     */
    public void calculatePoint() {
        mFixedPointA = mFixedCircle.getCutPoint(mDragCircle.mCenter, true);
        mDragPointA = mDragCircle.getCutPoint(mFixedCircle.mCenter, false);

        mFixedPointB = mFixedCircle.getCutPoint(mDragCircle.mCenter, false);
        mDragPointB = mDragCircle.getCutPoint(mFixedCircle.mCenter, true);

        float oxa = (mFixedCircle.mCenter.x + mDragCircle.mCenter.x) / 2;
        float oya = (mFixedCircle.mCenter.y + mDragCircle.mCenter.y) / 2;
        mOperationPointA = new PointF(oxa, oya);
        mOperationPointB = new PointF(oxa, oya);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (isTouchDot(event) && mDotView.getVisibility() == VISIBLE) {
            mDraggable = true;
            return true;
        } else {
            return super.onInterceptTouchEvent(event);
        }
    }

    /**
     * 保存最后一次Motion
     *
     * @param ev MotionEvent
     */
    private void saveLastMotion(MotionEvent ev) {
        mLastPosX = ev.getX();
        mLastPosY = ev.getY();
    }

    /**
     * 保存开始的Motion
     *
     * @param ev MotionEvent
     */
    private void saveStartMotion(MotionEvent ev) {
        mStartPosX = ev.getX();
        mStartPosY = ev.getY();
    }

    private void log(MotionEvent ev) {
        Log.d("xls", ev.getX() + "");
        Log.d("xls", ev.getY() + "");
    }

    private void log(int x, int y) {
        Log.d("xls", x + "  " + y);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mDraggable) {
            return false;
        }
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                saveStartMotion(ev);
                saveLastMotion(ev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = ev.getX();
                final float y = ev.getY();
                final int dx = (int) (x - mLastPosX);
                final int dy = (int) (y - mLastPosY);
                mDragCircle.mCenter.x += dx;
                mDragCircle.mCenter.y += dy;
                saveLastMotion(ev);
                break;
            }

            case MotionEvent.ACTION_UP: {
                mDraggable = false;
                resetCircle();
            }

            case MotionEvent.ACTION_CANCEL: {
                resetCircle();
                break;
            }
        }
        invalidate();
        return true;
    }

    private void resetCircle() {
        initCircle();
    }

    private void initCircle() {
        final int[] dotLocation = new int[2];
        final int[] layoutLocation = new int[2];
        mDotView.getLocationInWindow(dotLocation);
        getLocationInWindow(layoutLocation);
        int dx = -layoutLocation[0] + dotLocation[0];
        int dy = -layoutLocation[1] + dotLocation[1];
        log(layoutLocation[0] - dotLocation[0], layoutLocation[1] - dotLocation[1]);
        mFixedCircle = new Circle(dx - getLeft() + mDotView.getWidth() / 2, dy - getTop() + mDotView.getWidth() / 2, mDotView.getWidth() / 2);
        mDragCircle = new Circle(mFixedCircle.mCenter.x, mFixedCircle.mCenter.y, mFixedCircle.mRadius);
    }

}
