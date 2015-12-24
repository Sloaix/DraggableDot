package com.lsxiao.library;

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
        mPaint.setStrokeWidth(2);
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Paint.Style.FILL);

        mFixedCircle = new Circle(200, 200, 20);
        mDragCircle = new Circle(400, 300, 40);

        mFixedPointA = mFixedCircle.getCutPoint(mDragCircle.mCenter, true);
        mDragPointA = mDragCircle.getCutPoint(mFixedCircle.mCenter, false);
        mOperationPointA = new PointF((mFixedPointA.x + mDragPointA.x) / 2 + (mFixedCircle.mRadius + mDragCircle.mRadius) / 1.5f, (mFixedPointA.y + mDragPointA.y) / 2);

        mFixedPointB = mFixedCircle.getCutPoint(mDragCircle.mCenter, false);
        mDragPointB = mDragCircle.getCutPoint(mFixedCircle.mCenter, true);
        mOperationPointB = new PointF((mFixedPointB.x + mDragPointB.x) / 2 - (mFixedCircle.mRadius + mDragCircle.mRadius) / 1.5f, (mFixedPointB.y + mDragPointB.y) / 2);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("xls", "hehe");
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
        drawCircle(canvas);
    }

    private void drawCircle(Canvas canvas) {
        mFixedCircle.draw(canvas, mPaint);
        mDragCircle.draw(canvas, mPaint);

        Path path = new Path();
        path.moveTo(mFixedPointA.x, mFixedPointA.y);
        path.quadTo(mOperationPointA.x, mOperationPointA.y, mDragPointA.x, mDragPointA.y);
        path.lineTo(mDragPointB.x, mDragPointB.y);
        path.quadTo(mOperationPointB.x, mOperationPointB.y, mFixedPointB.x, mFixedPointB.y);
        path.lineTo(mFixedPointA.x, mFixedPointA.y);
        path.close();
        canvas.drawPath(path, mPaint);
    }

    /**
     * 圆
     */
    public static class Circle {
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
         * 获取圆上切点
         *
         * @param outPoint 圆外的点
         * @param first    第一个点
         * @return Point
         */
        public PointF getCutPoint(PointF outPoint, boolean first) {
            final PointF cutPoint = new PointF(0, 0);
            final PointF temp;

            cutPoint.x = outPoint.x - mCenter.x;
            cutPoint.y = outPoint.y - mCenter.y;

            float ratio = mRadius / (float) Math.sqrt(cutPoint.x * cutPoint.x + cutPoint.y * cutPoint.y);
            cutPoint.x *= ratio;
            cutPoint.y *= ratio;

            temp = new PointF(cutPoint.x, cutPoint.y);
            float radians = first ? (float) Math.acos(ratio) : (float) -Math.acos(ratio);

            cutPoint.x = temp.x * (float) Math.cos(radians) - temp.y * (float) Math.sin(radians) + mCenter.x;
            cutPoint.y = temp.x * (float) Math.sin(radians) + temp.y * (float) Math.cos(radians) + mCenter.y;

            return cutPoint;
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
