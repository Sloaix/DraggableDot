package com.lsxiao.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.FrameLayout;

/**
 * author:lsxiao
 * date:2015/12/25 17:02
 */
public class DraggableLayout extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    DotView mTouchedDot;
    Circle mDragCircle;
    Circle mFixedCircle;
    Circle mOriginCircle;
    //intersection on Fixed Circle
    PointF mPointA;
    //intersection on Fixed Circle
    PointF mPointB;
    //intersection on Drag Circle
    PointF mPointC;
    //intersection on Drag Circle
    PointF mPointD;
    //mid point
    PointF mPointMid;
    Paint mPaint;
    Path mPath;

    float mStartPosX;
    float mStartPosY;

    float mLastPosX;
    float mLastPosY;

    private State mState = State.IDLE;
    private boolean mCanIntercept = false;

    /**
     * control the circle center point animation.
     */
    ValueAnimator mPointAnimator;

    PointFEvaluator mPointFEvaluator;

    private enum State {
        IDLE,//停止状态
        STRETCH,//拉伸状态
        DRAG,//拖动状态
        DISMISSING,//消失中状态
        DISMISSED//消失状态
    }

    public DraggableLayout(Context context) {
        this(context, null);
    }

    public DraggableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCanIntercept(boolean canIntercept) {
        Log.d("xls", "mCanIntercept = " + canIntercept);
        this.mCanIntercept = canIntercept;
    }

    public static void bind(Activity activity) {
        DraggableLayout draggableLayout = new DraggableLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        draggableLayout.setLayoutParams(params);

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        for (int i = 0; i < decorView.getChildCount(); i++) {
            View view = decorView.getChildAt(i);
            if (view instanceof DraggableLayout) {
                return;
            }
        }

        for (int i = 0; i < decorView.getChildCount(); i++) {
            View v = decorView.getChildAt(i);
            decorView.removeView(v);
            draggableLayout.addView(v);
        }

        decorView.addView(draggableLayout, 0);
    }

    public interface onStateChangedListener {
        void onDragStart(DotView dotView);

        void onDragEnd(DotView dotView);

        void onRoboundStart(DotView dotView);

        void onRoboundEnd(DotView dotView);
    }

    public abstract class SimpleStateChangedListener implements onStateChangedListener {

    }

    private void init() {
        //the paint which used to draw bezierCurve
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        //the path which used to draw bezierCurve
        mPath = new Path();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        init();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        //after super.dispatchDraw(canvas) make sure draw drag effect above children view.
        if (mTouchedDot == null) {
            return;
        }
        if (mFixedCircle == null || mDragCircle == null) {
            initCircle();
        }

        if (getState() == State.STRETCH) {
            calculatePoint();
            drawBezierCurve(canvas, mPaint);
            mFixedCircle.draw(canvas, mPaint);
        }

        if (getState() == State.STRETCH
                || getState() == State.DRAG
                || getState() == State.DISMISSING) {
            if (getState() == State.DISMISSING) {
                mDragCircle.draw(canvas, mPaint);
            } else {
                canvas.drawBitmap(mTouchedDot.getDrawingCache(), mDragCircle.mCenter.x - mDragCircle.mRadius, mDragCircle.mCenter.y - mDragCircle.mRadius, mPaint);
            }
        }
    }

    /**
     * draw Bezier curve
     *
     * @param canvas Canvas
     * @param paint  Paint
     */
    public void drawBezierCurve(Canvas canvas, Paint paint) {
        mPath.reset();
        mPath.moveTo(mPointA.x, mPointA.y);
        mPath.quadTo(mPointMid.x, mPointMid.y, mPointC.x, mPointC.y);
        mPath.lineTo(mPointD.x, mPointD.y);
        mPath.quadTo(mPointMid.x, mPointMid.y, mPointB.x, mPointB.y);
        mPath.lineTo(mPointA.x, mPointA.y);
        mPath.close();
        canvas.drawPath(mPath, paint);
    }

    /**
     * calculate 5 points that we used to draw Bezier curve.
     * <p>
     * if the circle's property has changed,you need to recalculate the position of these points.
     */
    public void calculatePoint() {
        mPointA = mFixedCircle.getCutPoint(mDragCircle.mCenter, true);
        mPointC = mDragCircle.getCutPoint(mFixedCircle.mCenter, false);

        mPointB = mFixedCircle.getCutPoint(mDragCircle.mCenter, false);
        mPointD = mDragCircle.getCutPoint(mFixedCircle.mCenter, true);

        float midX = (mFixedCircle.mCenter.x + mDragCircle.mCenter.x) / 2;
        float midY = (mFixedCircle.mCenter.y + mDragCircle.mCenter.y) / 2;
        mPointMid = new PointF(midX, midY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return handleIntercept(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return processTouchEvent(ev);
    }

    /**
     * handle whether intercept the event.only intercept event when dot is visible and touched.
     *
     * @param event MotionEvent
     * @return true, if need to intercept the event.
     */
    private boolean handleIntercept(MotionEvent event) {
        if (!mCanIntercept) {
            return false;
        }
        if (mTouchedDot == null || mTouchedDot.getVisibility() != VISIBLE) {
            return super.onInterceptTouchEvent(event);
        }
        initCircle();
        return true;
    }

    /**
     * when over max stretch length,needing to start fixedCircle dismissed animation.
     *
     * @return true, if over the max stretch length
     */
    private boolean isOverMaxDistance() {
        final float length = mOriginCircle.distanceToOtherCircle(mDragCircle);
        return length > mTouchedDot.getMaxStretchLength() - 50;
    }

    /**
     * 获取到圆心之间的长度
     *
     * @return float
     */
    private float distanceBetweenCenter() {
        return mFixedCircle.distanceToOtherCircle(mDragCircle);
    }

    /**
     * 状态
     *
     * @param state State
     */
    private void setState(State state) {
        mState = state;
    }

    private State getState() {
        return mState;
    }

    private void showDotView() {
        if (mTouchedDot.getVisibility() == VISIBLE) {
            return;
        }
        mTouchedDot.setVisibility(VISIBLE);
    }

    private void hideDotView() {
        if (mTouchedDot.getVisibility() == INVISIBLE) {
            return;
        }
        mTouchedDot.setVisibility(INVISIBLE);
    }

    private boolean isOutLayout(MotionEvent ev) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        displayMetrics = getResources().getDisplayMetrics();
        final float x = ev.getRawX();
        final float y = ev.getRawY();
        final float width = displayMetrics.widthPixels;
        final float height = displayMetrics.heightPixels;
        return x < 10 || x > width - 10 || y < 10 || y > height - 10;
    }

    /**
     * process the drag event.
     *
     * @param ev MotionEvent
     * @return true, if consume the event,and start to drag.
     */
    private boolean processTouchEvent(MotionEvent ev) {
        boolean processed = true;
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_MOVE: {
                processed = processMove(ev);
                break;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                Log.d("xls", "ACTION_CANCEL or ACTION_UP");
                processed = processStop(ev);
                break;
            }
        }
        invalidate();
        return processed;
    }


    private boolean isAnimating() {
        return mPointAnimator != null && mPointAnimator.isRunning();
    }


    public void preDrawDrag(DotView dotView, float rawX, float rawY) {
        mTouchedDot = dotView;
        mLastPosX = rawX;
        mLastPosY = rawY;
    }

    /**
     * 处理move事件
     *
     * @param ev MotionEvent
     */
    private boolean processMove(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        final float dx = (x - mLastPosX);
        final float dy = (y - mLastPosY);
        mDragCircle.mCenter.x += dx;
        mDragCircle.mCenter.y += dy;
        updateFixedCircleRadius();
        switch (mState) {
            case IDLE: {
                /**do prepare for ready to stretch*/
                hideDotView();
                setState(State.STRETCH);
                break;
            }
            //拉伸状态
            case STRETCH: {
                //超过了可以拉动的区间
                if (isOverMaxDistance()) {
                    setState(State.DRAG);
                }
                break;
            }
            case DRAG: {
                //在拖动状态触发
                if (!isOverMaxDistance()) {
                    setState(State.STRETCH);
                }
                break;
            }
            case DISMISSED: {
                //已经dismiss完成
                break;
            }
        }
        saveLastMotion(ev);
        return true;
    }


    /**
     * 处理ActionUp事件
     *
     * @param ev MotionEvent
     */
    private boolean processStop(MotionEvent ev) {
        switch (mState) {
            case IDLE: {
                reset();
                break;
            }
            case STRETCH: {
                setState(State.IDLE);
                reset();
                break;
            }
            case DRAG: {
                setState(State.DISMISSING);
                animate(State.DISMISSING, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        setState(State.IDLE);
                        mTouchedDot = null;
                    }
                });
                break;
            }
            case DISMISSED: {
                break;
            }
            default: {
                setState(State.IDLE);
                reset();
                break;
            }
        }
        Log.d("xls", "processStop");
        setCanIntercept(false);
        return true;
    }

    /**
     * reset will make layout to fit IDLE state.
     */
    private void reset() {
        //重置circle位置
        initCircle();
        //显示dotView
        showDotView();
        //没有触摸了,置为空
        mTouchedDot = null;
    }

    /**
     * save the last motion
     *
     * @param ev MotionEvent
     */
    private void saveLastMotion(MotionEvent ev) {
        mLastPosX = ev.getX();
        mLastPosY = ev.getY();
    }

    /**
     * save the start motion
     *
     * @param ev MotionEvent
     */
    private void saveStartMotion(MotionEvent ev) {
        mStartPosX = ev.getX();
        mStartPosY = ev.getY();
    }

    private void initCircle() {
        //the dot location in window
        final int[] dotLocation = new int[2];
        //the layout location in window
        final int[] layoutLocation = new int[2];

        /*get the location instance*/
        mTouchedDot.getLocationInWindow(dotLocation);
        getLocationInWindow(layoutLocation);

        /*calculate the dx and dy*/
        int dx = -layoutLocation[0] + dotLocation[0];
        int dy = -layoutLocation[1] + dotLocation[1];

        mFixedCircle = new Circle(dx - getLeft() + mTouchedDot.getWidth() / 2, dy - getTop() + mTouchedDot.getWidth() / 2, mTouchedDot.getWidth() / 2);
        mDragCircle = Circle.copy(mFixedCircle);
        mOriginCircle = Circle.copy(mFixedCircle);
    }

    private void animate(State state) {
        animate(state, null);
    }

    /**
     * the dot will be back the origin position by animate.
     */
    private void animate(State state, Animator.AnimatorListener animatorListener) {
        if (mPointAnimator != null && mPointAnimator.isRunning()) {
            return;
        }

        if (mPointFEvaluator == null) {
            mPointFEvaluator = new PointFEvaluator();
        }

        switch (state) {
//            case FIXED_MOVE_TO_DRAG: {
//                //fixed move to drag
//                mPointAnimator = ValueAnimator.ofObject(mPointFEvaluator, mFixedCircle.mCenter, mDragCircle.mCenter);
//                mPointAnimator.setEvaluator(mPointFEvaluator);
//                mPointAnimator.setDuration(200);
//                mPointAnimator.setInterpolator(new FastOutSlowInInterpolator());
//                mPointAnimator.addUpdateListener(this);
//                if (animatorListener == null) {
//                    mPointAnimator.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            setState(State.DRAG);
//                            invalidate();
//                        }
//                    });
//                } else {
//                    mPointAnimator.addListener(animatorListener);
//                }
//
//                break;
//            }
//            case FIXED_MOVE_TO_ORIGIN: {
//                mFixedCircle = Circle.copy(mDragCircle);
//                mPointAnimator = ValueAnimator.ofObject(mPointFEvaluator, mFixedCircle.mCenter, mOriginCircle.mCenter);
//                mPointAnimator.setEvaluator(mPointFEvaluator);
//                mPointAnimator.setDuration(200);
//                mPointAnimator.setInterpolator(new FastOutSlowInInterpolator());
//                mPointAnimator.addUpdateListener(this);
//                if (animatorListener == null) {
//                    mPointAnimator.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            setState(State.STRETCH);
//                            invalidate();
//                        }
//                    });
//                } else {
//                    mPointAnimator.addListener(animatorListener);
//                }
//                break;
//            }
//            case DRAG_MOVE_TO_ORIGIN: {
//                mPointAnimator = ValueAnimator.ofObject(mPointFEvaluator, mDragCircle.mCenter, mOriginCircle.mCenter);
//                mPointAnimator.setEvaluator(mPointFEvaluator);
//                mPointAnimator.setDuration(200);
//                mPointAnimator.setInterpolator(new FastOutSlowInInterpolator());
//                mPointAnimator.addUpdateListener(this);
//                if (animatorListener == null) {
//                    mPointAnimator.addListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationStart(Animator animation) {
//
//                        }
//
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            setState(State.IDLE);
//                            invalidate();
//                            showDotView();
//                        }
//                    });
//                } else {
//                    mPointAnimator.addListener(animatorListener);
//                }
//                break;
//            }
            case DISMISSING: {
                mPointAnimator = ValueAnimator.ofFloat(mDragCircle.mRadius, 0);
                mPointAnimator.setDuration(200);
                mPointAnimator.setInterpolator(new AnticipateOvershootInterpolator());
                mPointAnimator.addUpdateListener(this);
                if (animatorListener == null) {
                    mPointAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            setState(State.DISMISSED);
                            postInvalidate();
                        }
                    });
                } else {
                    mPointAnimator.addListener(animatorListener);
                }
                //开始dismiss动画
                break;
            }
        }

        mPointAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
//        Log.d("xls", mState.name());
        switch (mState) {
//            case FIXED_MOVE_TO_DRAG: {
//                mFixedCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
//                updateFixedCircleRadius();
//                break;
//            }
//            case FIXED_MOVE_TO_ORIGIN: {
//                mFixedCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
//                updateFixedCircleRadius();
//                break;
//            }
//            case DRAG_MOVE_TO_ORIGIN: {
//                mDragCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
//                updateFixedCircleRadius();
//                break;
//            }
            case DISMISSING: {
                mDragCircle.mRadius = (float) valueAnimator.getAnimatedValue();
                //开始dismiss动画
                break;
            }
        }
        invalidate();
    }

    /**
     * the fixed circle's radius is depend on the distance from fixedCircle's center to DragCircle's center.
     * you need invoke this method to update the fixedCircle's radius when the distance is changed.
     */
    private void updateFixedCircleRadius() {
        final float dLength = Math.max(mTouchedDot.getMaxStretchLength() - distanceBetweenCenter(), 0);
        final float fraction = dLength / mTouchedDot.getMaxStretchLength();
        mFixedCircle.mRadius = fraction * mDragCircle.mRadius;
    }

    /**
     * reset fixed circle's radius and center point to default.
     */
    private void resetFixedCircle() {
        //the dot location in window
        final int[] dotLocation = new int[2];
        //the layout location in window
        final int[] layoutLocation = new int[2];

        /*get the location instance*/
        mTouchedDot.getLocationInWindow(dotLocation);
        getLocationInWindow(layoutLocation);

        /*calculate the dx and dy*/
        int dx = -layoutLocation[0] + dotLocation[0];
        int dy = -layoutLocation[1] + dotLocation[1];

        mFixedCircle = new Circle(dx - getLeft() + mTouchedDot.getWidth() / 2, dy - getTop() + mTouchedDot.getWidth() / 2, mTouchedDot.getWidth() / 2);
    }

    private Circle getOriginCircle() {
        //the dot location in window
        final int[] dotLocation = new int[2];
        //the layout location in window
        final int[] layoutLocation = new int[2];

        /*get the location instance*/
        mTouchedDot.getLocationInWindow(dotLocation);
        getLocationInWindow(layoutLocation);

        /*calculate the dx and dy*/
        int dx = -layoutLocation[0] + dotLocation[0];
        int dy = -layoutLocation[1] + dotLocation[1];

        return new Circle(dx - getLeft() + mTouchedDot.getWidth() / 2, dy - getTop() + mTouchedDot.getWidth() / 2, mTouchedDot.getWidth() / 2);
    }
}
