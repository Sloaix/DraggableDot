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
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * author:lsxiao
 * date:2015/12/25 17:02
 */
public class DraggableLayout extends FrameLayout implements ValueAnimator.AnimatorUpdateListener {
    DotView mTouchedDot;
    Circle mDragCircle;
    Circle mFixedCircle;
    Circle mDotCircle;
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
    private List<DotView> mDotViewList;

    float mStartPosX;
    float mStartPosY;

    float mLastPosX;
    float mLastPosY;

    float mLastLengthBetweenCenter;


    private State mState = State.STATE_IDLE;

    /**
     * control the circle center point animation.
     */
    ValueAnimator mPointAnimator;

    PointFEvaluator mPointFEvaluator;

    public static boolean mDraggable = false;

    public static final int FIXED_MOVE_TO_DRAG = 0;

    public static final int DRAG_MOVE_TO_FIXED = 1;

    private int mCurAnimateCircle = -1;

    private enum State {
        STATE_IDLE(0), STATE_STRETCH(1), STATE_DRAG(2);
        private final int nativeInt;

        State(int nativeInt) {
            this.nativeInt = nativeInt;
        }

        public int val() {
            return nativeInt;
        }
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

    public static void register(Activity activity) {
        DraggableLayout draggableLayout = new DraggableLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        draggableLayout.setLayoutParams(params);

        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
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

        mDotViewList = new ArrayList<>();

        findAllDotView(this);
    }

    /**
     * find the dot.
     * the dot is a nested child.
     *
     * @param viewGroup dot's parent
     * @return DotView
     */
    private void findAllDotView(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view instanceof DotView) {
                mDotViewList.add((DotView) view);
            } else if (view instanceof ViewGroup) {
                findAllDotView((ViewGroup) view);
            }
        }

        if (mDotViewList.size() == 0) {
            throw new NullPointerException("can't find DotView instance,the DotView isn't in this '" + viewGroup.toString() + "' ViewGroup");
        }
    }


    private DotView findTouchedDot(MotionEvent event) {
        for (DotView dotView : mDotViewList) {
            int[] location = new int[2];
            dotView.getLocationOnScreen(location);
            final int w = dotView.getMeasuredWidth();
            final int h = dotView.getMeasuredHeight();
            int posX = (int) event.getRawX();
            int posY = (int) event.getRawY();

            int lowerX = location[0];
            int upperX = w + location[0];

            int lowerY = location[1];
            int upperY = h + location[1];

            final boolean isTouched = (posX > lowerX && posX < upperX && posY > lowerY && posY < upperY);
            if (isTouched) {
                return dotView;
            }
        }
        return null;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        init();
    }

    /**
     * detect whether the event is in the dot area.
     *
     * @param event MotionEvent
     * @return true if touched on dot view
     */
    private boolean isTouchDot(MotionEvent event) {
        return findTouchedDot(event) != null;
    }

    private void log(MotionEvent ev) {
        Log.d("xls", ev.getX() + "");
        Log.d("xls", ev.getY() + "");
    }

    private void log(int x, int y) {
        Log.d("xls", x + "  " + y);
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

        calculatePoint();

        drawDragCircle(canvas, mPaint);

        drawBezierCurve(canvas, mPaint);
    }

    /**
     * draw Bezier curve
     *
     * @param canvas Canvas
     * @param paint  Paint
     */
    public void drawBezierCurve(Canvas canvas, Paint paint) {
        if (getState() == State.STATE_DRAG) {
            return;
        }
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
     * draw drag circle
     *
     * @param canvas Canvas
     * @param paint  Paint
     */
    public void drawDragCircle(Canvas canvas, Paint paint) {
        if (getState() == State.STATE_STRETCH) {
            mFixedCircle.draw(canvas, paint);
        }
        if (getState() == State.STATE_DRAG || getState() == State.STATE_STRETCH) {
            mDragCircle.draw(canvas, paint);
        }
    }

    /**
     * calculate 5 points that we used to draw Bezier curve.
     * <p/>
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
        return handleTouch(ev);
    }

    /**
     * handle whether intercept the event.only intercept event when dot is visible and touched.
     *
     * @param event MotionEvent
     * @return true, if need to intercept the event.
     */
    private boolean handleIntercept(MotionEvent event) {
        findAllDotView(this);
        mTouchedDot = findTouchedDot(event);
        Log.d("xls", (mTouchedDot == null) + "");
        if (mTouchedDot == null || mTouchedDot.getVisibility() != VISIBLE) {
            return super.onInterceptTouchEvent(event);
        }
        mDraggable = true;
        initCircle();
        return true;
    }

    /**
     * when over max stretch length,needing to start fixedCircle dismissed animation.
     *
     * @return true, if over the max stretch length
     */
    private boolean isOverMaxDistance() {
        final float length = mDotCircle.distanceToOtherCircle(mDragCircle);
        return length > mTouchedDot.getMaxStretchLength() - 50;
    }

    /**
     * 获取到圆心之间的长度
     *
     * @return float
     */
    private float getLengthBetweenCenter() {
        return mFixedCircle.distanceToOtherCircle(mDragCircle);
    }

    /**
     * 状态
     *
     * @param state State
     */
    private void setState(State state) {
        Log.d("xls", "state changed to " + state.toString());
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
     * handle the drag event.
     *
     * @param ev MotionEvent
     * @return true, if consume the event,and start to drag.
     */
    private boolean handleTouch(MotionEvent ev) {
        if (!mDraggable) {
            return false;
        }
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                if (isOutLayout(ev)) {
                    setState(State.STATE_IDLE);
                    initCircle();
                    showDotView();
                    mDraggable = false;
                    mLastPosX = 0;
                    mLastPosY = 0;
                    invalidate();
                    return false;
                }
                saveStartMotion(ev);
                saveLastMotion(ev);
                mLastLengthBetweenCenter = 0;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                if (isOutLayout(ev)) {
                    setState(State.STATE_IDLE);
                    initCircle();
                    showDotView();
                    mDraggable = false;
                    mLastPosX = 0;
                    mLastPosY = 0;
                    invalidate();
                    return false;
                }
                hideDotView();
                final float x = ev.getX();
                final float y = ev.getY();
                final float dx = (x - mLastPosX);
                final float dy = (y - mLastPosY);
                mDragCircle.mCenter.x += dx;
                mDragCircle.mCenter.y += dy;
                updateFixedCircleRadius();
                if (isOverMaxDistance()) {
                    if (getState() == State.STATE_STRETCH) {
                        animate(FIXED_MOVE_TO_DRAG);
                    }
                } else if (dx != 0 || dy != 0) {
                    if (getState() == State.STATE_DRAG) {
                        resetFixedCircle();
                    }
                    setState(State.STATE_STRETCH);
                }
                saveLastMotion(ev);
                break;
            }

            case MotionEvent.ACTION_UP: {
                if (getState() == State.STATE_DRAG) {
                    setState(State.STATE_IDLE);
                    initCircle();
                    showDotView();
                } else if (getState() == State.STATE_STRETCH) {
                    animate(DRAG_MOVE_TO_FIXED);
                }
                mDraggable = false;
                mLastPosX = 0;
                mLastPosY = 0;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mDraggable = false;
                mLastPosX = 0;
                mLastPosY = 0;
                initCircle();
                break;
            }
        }
        invalidate();
        return true;
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
        mDotCircle = Circle.copy(mFixedCircle);
    }

    /**
     * the dot will be back the origin position by animate.
     */
    private void animate(int type) {
        if (mPointAnimator != null && mPointAnimator.isRunning()) {
            return;
        }

        mCurAnimateCircle = type;

        if (mPointFEvaluator == null) {
            mPointFEvaluator = new PointFEvaluator();
        }

        // drag move to fixed
        if (type == DRAG_MOVE_TO_FIXED) {
            mPointAnimator = ValueAnimator.ofObject(mPointFEvaluator, mDragCircle.mCenter, mFixedCircle.mCenter);
            mPointAnimator.setEvaluator(mPointFEvaluator);
            mPointAnimator.setDuration(200);
            mPointAnimator.setInterpolator(new FastOutSlowInInterpolator());
            mPointAnimator.addUpdateListener(this);
            mPointAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    setState(State.STATE_IDLE);
                    initCircle();
                    showDotView();
                    mCurAnimateCircle = -1;
                    invalidate();
                }
            });
        } else if (type == FIXED_MOVE_TO_DRAG) {
            //fixed move to drag
            mPointAnimator = ValueAnimator.ofObject(mPointFEvaluator, mFixedCircle.mCenter, mDragCircle.mCenter);
            mPointAnimator.setEvaluator(mPointFEvaluator);
            mPointAnimator.setDuration(200);
            mPointAnimator.setInterpolator(new FastOutSlowInInterpolator());
            mPointAnimator.addUpdateListener(this);
            mPointAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mDraggable) {
                        setState(State.STATE_DRAG);
                    } else {
                        setState(State.STATE_IDLE);
                        initCircle();
                        showDotView();
                    }

                    mCurAnimateCircle = -1;
                    invalidate();
                }
            });
        } else {
            return;
        }

        mPointAnimator.start();
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        if (mCurAnimateCircle == DRAG_MOVE_TO_FIXED) {
            mDragCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
            updateFixedCircleRadius();
        } else {
            mFixedCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
            updateFixedCircleRadius();
        }
        invalidate();
    }

    /**
     * the fixed circle's radius is depend on the distance from fixedCircle's center to DragCircle's center.
     * you need invoke this method to update the fixedCircle's radius when the distance is changed.
     */
    private void updateFixedCircleRadius() {
        final float dLength = Math.max(mTouchedDot.getMaxStretchLength() - getLengthBetweenCenter(), 0);
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

    private Circle getDotCircle() {
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
