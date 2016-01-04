package com.lsxiao.library;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
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
    DotView mTouchedDotView;
    Circle mTouchCircle;
    Circle mFollowCircle;
    Circle mOriginCircle;
    //intersection on Follow Circle
    PointF mPointA;
    //intersection on Follow Circle
    PointF mPointB;
    //intersection on Touch Circle
    PointF mPointC;
    //intersection on Touch Circle
    PointF mPointD;
    //mid point
    PointF mPointMid;
    Paint mPaint;
    Path mPath;

    float mStartPosX;
    float mStartPosY;

    float mLastPosX;
    float mLastPosY;

    private boolean mCanIntercept = false;

    private int mState = IDLE;

    ValueAnimator mAnimator;
    PointFEvaluator mPointFEvaluator;

    public static final int IDLE = 0;//none move event,and the dotView is visible.
    public static final int STRETCHING = 1;//the touchCircle is faring from the OriginCircle but not beyond the limit length.
    public static final int FOLLOW_MOVING_TO_TOUCH = 2;//followCircle move to touchCircle's position by animate.
    public static final int FOLLOW_MOVING_TO_ORIGIN = 3;//followCircle move to originCircle's position by animate.
    public static final int TOUCH_MOVING_TO_ORIGIN = 4;//touchCircle move to originCircle's position by animate.
    public static final int DRAGGING = 5;//the touchCircle is be dragging.
    public static final int DISMISSING = 6;//the touchCircle is dismissing by animate.

    public DraggableLayout(Context context) {
        this(context, null);
    }

    public DraggableLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DraggableLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * initialize some local variables.
     */
    private void init() {
        //the paint instance which used to draw bezierCurve
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        //the path instance which used to draw bezierCurve
        mPath = new Path();
    }

    /**
     * attachToActivity draggableLayout to view tree and make draggableLayout be a child of the decorView,
     * then let the content view become a child of the draggableLayout.
     * <p/>
     * Draggable dot and the animation will draw on draggable layer which will be drew above on it's children,
     * the DotView is just a placeHolder to listen the down event and notify DraggableLayout to handle the draggable events.
     * so,the DotView can be used in any layout,it's won't have any influence on your own layout.
     *
     * @param activity Activity
     */
    public static void attachToActivity(Activity activity) {
        ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();

        /*if is exist,don't attachToActivity again*/
        for (int i = 0; i < decorView.getChildCount(); i++) {
            View view = decorView.getChildAt(i);
            if (view instanceof DraggableLayout) {
                return;
            }
        }

        /*not exist, need to attachToActivity draggableLayout to view tree*/
        DraggableLayout draggableLayout = new DraggableLayout(activity);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        draggableLayout.setLayoutParams(params);

        for (int i = 0; i < decorView.getChildCount(); i++) {
            View v = decorView.getChildAt(i);
            decorView.removeView(v);
            draggableLayout.addView(v);
        }
        decorView.addView(draggableLayout, 0);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        //after super.dispatchDraw(canvas) make sure draw drag effect above children view.
        if (mTouchedDotView == null) {
            return;
        }

        if (mFollowCircle == null || mTouchCircle == null) {
            initCircle();
        }
        if (mPaint.getColor() != mTouchedDotView.getCircleColor()) {
            mPaint.setColor(mTouchedDotView.getCircleColor());
        }

        if (getState() == STRETCHING
                || getState() == FOLLOW_MOVING_TO_TOUCH
                || getState() == FOLLOW_MOVING_TO_ORIGIN
                || getState() == TOUCH_MOVING_TO_ORIGIN) {
            updatePoint();
            drawBezierCurve(canvas, mPaint);
            mFollowCircle.draw(canvas, mPaint);
        }

        if (getState() == STRETCHING
                || getState() == FOLLOW_MOVING_TO_TOUCH
                || getState() == FOLLOW_MOVING_TO_ORIGIN
                || getState() == TOUCH_MOVING_TO_ORIGIN
                || getState() == DRAGGING
                || getState() == DISMISSING) {
            if (getState() == DISMISSING) {
                mTouchCircle.draw(canvas, mPaint);
            } else {
                canvas.drawBitmap(mTouchedDotView.getDrawingCache(), mTouchCircle.mCenter.x - mTouchCircle.mRadius, mTouchCircle.mCenter.y - mTouchCircle.mRadius, mPaint);
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
     * <p/>
     * if the circle's property has changed,you need to recalculate the position of these points.
     */
    public void updatePoint() {
        mPointA = mFollowCircle.getCutPoint(mTouchCircle.mCenter, true);
        mPointC = mTouchCircle.getCutPoint(mFollowCircle.mCenter, false);

        mPointB = mFollowCircle.getCutPoint(mTouchCircle.mCenter, false);
        mPointD = mTouchCircle.getCutPoint(mFollowCircle.mCenter, true);

        float midX = (mFollowCircle.mCenter.x + mTouchCircle.mCenter.x) / 2;
        float midY = (mFollowCircle.mCenter.y + mTouchCircle.mCenter.y) / 2;
        mPointMid = new PointF(midX, midY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return handleIntercept(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!mCanIntercept) {
            return super.onInterceptTouchEvent(ev);
        }
        return processTouchEvent(ev);
    }

    public void setCanIntercept(boolean canIntercept) {
        mCanIntercept = canIntercept;
    }

    /**
     * handle whether intercept the event.only intercept event when dot is visible and touched.
     *
     * @param event MotionEvent
     * @return true, if need to intercept the event.
     */
    private boolean handleIntercept(MotionEvent event) {
        if (!mCanIntercept) {
            return super.onInterceptTouchEvent(event);
        }
        if (mTouchedDotView == null || mTouchedDotView.getVisibility() != VISIBLE) {
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
        final float length = mOriginCircle.distanceToOtherCircle(mTouchCircle);
        return length > mTouchedDotView.getLimitStretchLength() - 50;
    }

    /**
     * 获取到圆心之间的长度
     *
     * @return float
     */
    private float getLengthBetweenCenter() {
        return mFollowCircle.distanceToOtherCircle(mTouchCircle);
    }


    private void reset() {
        setState(IDLE);
        showDotView();
        initCircle();
        postInvalidate();
        mTouchedDotView = null;
    }

    private void dismissed() {
        setState(IDLE);
        initCircle();
        postInvalidate();
        mTouchedDotView = null;
    }

    /**
     * 状态
     *
     * @param state State
     */
    private void setState(int state) {
        mState = state;
    }

    private int getState() {
        return mState;
    }

    private void showDotView() {
        if (mTouchedDotView == null) {
            return;
        }
        if (mTouchedDotView.getVisibility() == VISIBLE) {
            return;
        }
        mTouchedDotView.setVisibility(VISIBLE);
    }

    private void hideDotView() {
        if (mTouchedDotView == null) {
            return;
        }
        if (mTouchedDotView.getVisibility() == INVISIBLE) {
            return;
        }
        mTouchedDotView.setVisibility(INVISIBLE);
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
                processActionUp(ev);
                break;
            }
        }
        invalidate();
        return processed;
    }


    /**
     * 处理ActionDown事件
     *
     * @param ev MotionEvent
     */
    private boolean processActionDown(MotionEvent ev) {
        mLastPosX = ev.getRawX();
        mLastPosY = ev.getRawY();
        return true;
    }

    public void preDrawDrag(DotView dotView, MotionEvent ev) {
        mTouchedDotView = dotView;
        processActionDown(ev);
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
        mTouchCircle.mCenter.x += dx;
        mTouchCircle.mCenter.y += dy;
        updateFixedCircleRadius();
        switch (mState) {
            case IDLE: {
                /**do prepare for ready to stretch*/
                hideDotView();
                if (getState() != STRETCHING && mTouchedDotView.getOnDotStateChangedListener() != null) {
                    mTouchedDotView.getOnDotStateChangedListener().onStretch(mTouchedDotView);
                }
                setState(STRETCHING);
                break;
            }
            //拉伸状态
            case STRETCHING: {
                //超过了可以拉动的区间
                if (isOverMaxDistance()) {
                    setState(FOLLOW_MOVING_TO_TOUCH);
                    animate(FOLLOW_MOVING_TO_TOUCH);
                }
                break;
            }
            case DRAGGING: {
                //在拖动状态触发
                if (!isOverMaxDistance()) {
                    setState(FOLLOW_MOVING_TO_ORIGIN);
                    animate(FOLLOW_MOVING_TO_ORIGIN);
                }
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
    private void processActionUp(MotionEvent ev) {
        switch (mState) {
            case IDLE: {
                mTouchedDotView = null;
                break;
            }
            case STRETCHING: {
                setState(TOUCH_MOVING_TO_ORIGIN);
                animate(TOUCH_MOVING_TO_ORIGIN, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        reset();
                    }
                });
                break;
            }
            case DRAGGING: {
                setState(DISMISSING);
                animate(DISMISSING);
                break;
            }
        }
        mCanIntercept = false;
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
        mTouchedDotView.getLocationInWindow(dotLocation);
        getLocationInWindow(layoutLocation);

        /*calculate the dx and dy*/
        int dx = -layoutLocation[0] + dotLocation[0];
        int dy = -layoutLocation[1] + dotLocation[1];

        mFollowCircle = new Circle(dx - getLeft() + mTouchedDotView.getWidth() / 2, dy - getTop() + mTouchedDotView.getWidth() / 2, mTouchedDotView.getWidth() / 2);
        mTouchCircle = Circle.copy(mFollowCircle);
        mOriginCircle = Circle.copy(mFollowCircle);
    }

    private void animate(int state) {
        animate(state, null);
    }

    /**
     * the dot will be back the origin position by animate.
     */
    private void animate(int state, Animator.AnimatorListener animatorListener) {
        if (mAnimator != null && mAnimator.isRunning()) {
            return;
        }

        if (mPointFEvaluator == null) {
            mPointFEvaluator = new PointFEvaluator();
        }

        switch (state) {
            case FOLLOW_MOVING_TO_TOUCH: {
                //fixed move to drag
                mAnimator = ValueAnimator.ofObject(mPointFEvaluator, mFollowCircle.mCenter, mTouchCircle.mCenter);
                mAnimator.setEvaluator(mPointFEvaluator);
                mAnimator.setDuration(200);
                mAnimator.setInterpolator(new FastOutSlowInInterpolator());
                mAnimator.addUpdateListener(this);
                if (animatorListener == null) {
                    mAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimator = null;
                            if (mCanIntercept) {
                                if (getState() != DRAGGING && mTouchedDotView.getOnDotStateChangedListener() != null) {
                                    mTouchedDotView.getOnDotStateChangedListener().onDrag(mTouchedDotView);
                                }
                                setState(DRAGGING);
                            } else {
                                setState(DISMISSING);
                                animate(DISMISSING);
                            }
                            postInvalidate();
                        }
                    });
                } else {
                    mAnimator.addListener(animatorListener);
                }

                break;
            }
            case FOLLOW_MOVING_TO_ORIGIN: {
                mFollowCircle = Circle.copy(mTouchCircle);
                mAnimator = ValueAnimator.ofObject(mPointFEvaluator, mFollowCircle.mCenter, mOriginCircle.mCenter);
                mAnimator.setEvaluator(mPointFEvaluator);
                mAnimator.setDuration(300);
                mAnimator.setInterpolator(new FastOutSlowInInterpolator());
                mAnimator.addUpdateListener(this);
                if (animatorListener == null) {
                    mAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimator = null;
                            if (mCanIntercept) {
                                if (getState() != STRETCHING && mTouchedDotView.getOnDotStateChangedListener() != null) {
                                    mTouchedDotView.getOnDotStateChangedListener().onStretch(mTouchedDotView);
                                }
                                setState(STRETCHING);
                            } else {
                                setState(TOUCH_MOVING_TO_ORIGIN);
                                animate(TOUCH_MOVING_TO_ORIGIN);
                            }
                            postInvalidate();
                        }
                    });
                } else {
                    mAnimator.addListener(animatorListener);
                }
                break;
            }
            case TOUCH_MOVING_TO_ORIGIN: {
                mAnimator = ValueAnimator.ofObject(mPointFEvaluator, mTouchCircle.mCenter, mOriginCircle.mCenter);
                mAnimator.setEvaluator(mPointFEvaluator);
                mAnimator.setDuration(300);
                mAnimator.setInterpolator(new FastOutSlowInInterpolator());
                mAnimator.addUpdateListener(this);
                if (animatorListener == null) {
                    mAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimator = null;
                            setState(IDLE);
                            reset();
                        }
                    });
                } else {
                    mAnimator.addListener(animatorListener);
                }
                break;
            }
            case DISMISSING: {
                mAnimator = ValueAnimator.ofFloat(mTouchCircle.mRadius, 0);
                mAnimator.setDuration(300);
                mAnimator.setInterpolator(new AnticipateOvershootInterpolator());
                mAnimator.addUpdateListener(this);
                if (animatorListener == null) {
                    mAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mAnimator = null;
                            if (mTouchedDotView.getOnDotStateChangedListener() != null) {
                                mTouchedDotView.getOnDotStateChangedListener().onDismissed(mTouchedDotView);
                            }
                            dismissed();
                        }
                    });
                } else {
                    mAnimator.addListener(animatorListener);
                }
                //开始dismiss动画
                break;
            }
        }

        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    @Override
    public void onAnimationUpdate(ValueAnimator valueAnimator) {
        switch (mState) {
            case FOLLOW_MOVING_TO_TOUCH: {
                mFollowCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
                updateFixedCircleRadius();
                break;
            }
            case FOLLOW_MOVING_TO_ORIGIN: {
                mFollowCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
                updateFixedCircleRadius();
                break;
            }
            case TOUCH_MOVING_TO_ORIGIN: {
                mTouchCircle.mCenter = (PointF) valueAnimator.getAnimatedValue();
                updateFixedCircleRadius();
                break;
            }
            case DISMISSING: {
                mTouchCircle.mRadius = (float) valueAnimator.getAnimatedValue();
                break;
            }
        }
        postInvalidate();
    }

    /**
     * the fixed circle's radius is depend on the distance from fixedCircle's center to DragCircle's center.
     * you need invoke this method to update the fixedCircle's radius when the distance is changed.
     */
    private void updateFixedCircleRadius() {
        if (mTouchedDotView == null) {
            return;
        }
        final float deltaLength = Math.max(mTouchedDotView.getLimitStretchLength() - getLengthBetweenCenter(), 0);
        final float fraction = deltaLength / mTouchedDotView.getLimitStretchLength();
        mFollowCircle.mRadius = fraction * mTouchCircle.mRadius;
    }
}
