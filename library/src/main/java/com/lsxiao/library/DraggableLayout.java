package com.lsxiao.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * author:lsxiao
 * date:2015/12/25 17:02
 */
public class DraggableLayout extends FrameLayout {
    ViewDragHelper mViewDragHelper;
    DotView mDotView;
    private int mLeft;
    private int mTop;
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

    Path mPath;

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

        //DotView的父布局
        ViewGroup dotViewParent = (ViewGroup) mDotView.getParent();

        //实例化拖动助手
        mViewDragHelper = ViewDragHelper.create(dotViewParent, 1.0f, new DragCallback());
        mPaint = new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

//        if (mFixedCircle == null) {
//            mFixedCircle = new Circle((mDotView.getLeft() + mDotView.getMeasuredWidth()) / 2, (mDotView.getTop() + mDotView.getMeasuredHeight()) / 2, mDotView.getMeasuredWidth() / 2);
//        }
//
//        if (mDragCircle == null) {
//            mDragCircle = mFixedCircle;
//        }
        mFixedCircle = new Circle(500, 500, 20);
        mDragCircle = new Circle(300, 500, 30);
        update();
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
    private boolean isTouchOnDot(MotionEvent event) {
        if (mDotView.getVisibility() != VISIBLE) {
            return false;
        }
        int[] location = new int[2];
        mDotView.getLocationOnScreen(location);
        int upperLimit = location[1] + mDotView.getMeasuredHeight();
        int lowerLimit = location[1];
        int y = (int) event.getRawY();
        return (y > lowerLimit && y < upperLimit);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        update();
        mFixedCircle.draw(canvas, mPaint);
        mDragCircle.draw(canvas, mPaint);
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

//    public void update() {
//        mFixedPointA = mFixedCircle.getCutPoint(mDragCircle.mCenter, true);
//        mDragPointA = mDragCircle.getCutPoint(mFixedCircle.mCenter, false);
//
//        float oxa = (mFixedPointA.x + mDragPointA.x) / 2;
//        float oya = (mFixedPointA.y + mDragPointA.y) / 2;
//        mOperationPointA = new PointF(oxa, oya);
//
//        mFixedPointB = mFixedCircle.getCutPoint(mDragCircle.mCenter, false);
//        mDragPointB = mDragCircle.getCutPoint(mFixedCircle.mCenter, true);
//
//
//        float oxb = (mFixedPointB.x + mDragPointB.x) / 2;
//        float oyb = (mFixedPointB.y + mDragPointB.y) / 2;
//        mOperationPointB = new PointF(oxb, oyb);
//    }

    public void update() {
//        float length = mDragCircle.lengthBetweenCenter(mFixedCircle);
//        float fac = 0.1f * length;
//        mFixedCircle.mRadius += mDragCircle.mRadius * fac;

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
        boolean isIntercept = mViewDragHelper.shouldInterceptTouchEvent(event);
        return isTouchOnDot(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mViewDragHelper.processTouchEvent(event);
        return true;
    }

    class DragCallback extends ViewDragHelper.Callback {
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            return child instanceof DotView;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            if (child.getTop() != top) {
                mTop = top;
                mDragCircle.mCenter.y += dy;
                invalidate();
            }
            return child.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            if (child.getLeft() != left) {
                mLeft = left;
                mDragCircle.mCenter.x += dx;
                invalidate();
            }
            return child.getLeft();
        }
    }
}
