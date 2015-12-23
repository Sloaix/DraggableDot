package com.lsxiao.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

/**
 * author:lsxiao
 * date:2015/12/23 19:01
 */
public class DotView extends View {
    //固定住的圆
    Circle mFixedCircle;
    //拖动的圆
    Circle mDragCircle;
    //画笔
    Paint mPaint;

    PointF mFixedPointA;
    PointF mFixedPointB;
    PointF mDragPointA;
    PointF mDragPointB;
    PointF mOperationPointA;
    PointF mOperationPointB;

    public DotView(Context context) {
        this(context, null);
    }

    public DotView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas) {

        //计算fixed圆上的点A
        //计算fixed圆上的点B
        //计算drag圆上的点A
        //计算drag圆上的点B

    }

    private PointF calFixedPointA() {
        return calCirclePoint(mFixedCircle, mDragCircle);
    }

    private PointF calCirclePoint(Circle first, Circle second) {
        //计算两圆心之间的距离
        //计算FixedCircle切点到DragCircle圆心的长
        final float maxSizeLength = first.lengthBetweenCenter(second);
        final float rightSideLength = getRightAngleSideLength(maxSizeLength, first.mRadius);
        //计算angle
        float cosAngle = rightSideLength / maxSizeLength;
        float sinAngle = first.mRadius / maxSizeLength;
        return first.getPointOnCircle(cosAngle, sinAngle);
    }

    /**
     * 计算直角边的长度
     *
     * @param maxSizeLength             斜边
     * @param otherRightAngleSideLength 另外一个直角边
     * @return
     */
    public float getRightAngleSideLength(float maxSizeLength, float otherRightAngleSideLength) {
        return (float) Math.sqrt(Math.pow(maxSizeLength, 2) + Math.pow(otherRightAngleSideLength, 2));
    }

    /**
     * 圆
     */
    class Circle {
        //圆心
        PointF mCenter;
        //半径
        float mRadius;

        public Circle(PointF center, float radius) {
            mCenter = center;
            mRadius = radius;
        }

        public Circle(float cx, float cy, float radius) {
            this(new PointF(cx, cy), radius);
        }

        public void draw(Canvas canvas, Paint paint) {
            canvas.drawCircle(mCenter.x, mCenter.y, mRadius, paint);
        }

        /**
         * 获取圆上一点x坐标值
         *
         * @param cosAngle cos角度
         * @return x of the point
         */
        public float getPointXOnCircle(float cosAngle) {
            return mRadius * cosAngle + mCenter.x;
        }

        /**
         * 获取圆上一点y坐标值
         *
         * @param sinAngle sin角度
         * @return y of the point
         */
        public float getPointYOnCircle(float sinAngle) {
            return mRadius * sinAngle + mCenter.y;
        }

        /**
         * 获取圆上一点
         *
         * @param cosAngle
         * @param sinAngle
         * @return
         */
        public PointF getPointOnCircle(float cosAngle, float sinAngle) {
            return new PointF(getPointXOnCircle(cosAngle), getPointYOnCircle(sinAngle));
        }

        /**
         * 计算两圆之间圆心距离
         *
         * @param circle Circle
         * @return float
         */
        public float lengthBetweenCenter(Circle circle) {
            return PointF.length(circle.mCenter.x, circle.mCenter.y);
        }
    }

}
