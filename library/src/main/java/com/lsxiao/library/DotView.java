package com.lsxiao.library;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
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

        mFixedCircle = new Circle(200, 200, 40);
        mDragCircle = new Circle(400, 300, 20);

        mFixedPointA = calCirclePoint(mFixedCircle, mDragCircle, true, true);
        mDragPointA = calCirclePoint(mFixedCircle, mDragCircle, false, true);
        mOperationPointA = new PointF((mFixedPointA.x + mDragPointA.x) / 2, (mFixedPointA.y + mDragPointA.y) / 2);

        mFixedPointB = calCirclePoint(mFixedCircle, mDragCircle, true, false);
        mDragPointB = calCirclePoint(mFixedCircle, mDragCircle, false, false);
        mOperationPointB = new PointF((mFixedPointB.x + mDragPointB.x) / 2, (mFixedPointB.y + mDragPointB.y) / 2);
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
        canvas.drawPath(path, mPaint);

        path = new Path();
        path.moveTo(mFixedPointB.x, mFixedPointB.y);
        path.quadTo(mOperationPointB.x, mOperationPointB.y, mDragPointB.x, mDragPointB.y);
        canvas.drawPath(path, mPaint);
        //计算fixed圆上的点A
        //计算fixed圆上的点B
        //计算drag圆上的点A
        //计算drag圆上的点B
        System.out.println(mFixedPointA.x + "  " + mFixedPointA.y);
        System.out.println(mDragPointA.x + "  " + mDragPointA.y);
        System.out.println(mOperationPointA.x + "  " + mOperationPointA.y);
    }

    /**
     * @param fixed     固定的圆
     * @param drag      拖动的圆
     * @param isOnFixed 点是否在固定的圆上
     * @return PointF
     */
    public static PointF calCirclePoint(Circle fixed, Circle drag, boolean isOnFixed, boolean isCutAngle) {
        //三角形斜边
        final float hypotenuse = fixed.lengthBetweenCenter(drag);
        //三角形直角边
        final float leg = isOnFixed ? fixed.mRadius : drag.mRadius;
        //计算sin
        float sin = leg / hypotenuse;
        //计算弧度
        double radians = Math.asin(sin);
        if (!isCutAngle) {
            radians = Math.toRadians(180) - radians;
        }
        //返回圆上一点
        return isOnFixed ? fixed.getPoint(radians) : drag.getPoint(-radians);
    }

//    public static PointF calOperationPoint(Circle fixed, Circle drag) {
//        //中心点之间的距离
//        final float distance = fixed.lengthBetweenCenter(drag);
//        //
//        float ratio = fixed.mRadius / distance;
//        float x = ratio * distance + fixed.mCenter.x;
//        float y = ratio * fixed.mRadius + fixed.mCenter.y;
//        return null;
//    }

    /**
     * 计算直角边的长度
     *
     * @param hypotenuse 斜边
     * @param otherLeg   另外一个直角边
     * @return float
     */
    public static float getLegLength(float hypotenuse, float otherLeg) {
        return (float) Math.sqrt(Math.pow(hypotenuse, 2) - Math.pow(otherLeg, 2));
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
         * 获取圆上一点x坐标值
         *
         * @param radians cos角度
         * @return x of the point
         */
        public float getPx(double radians) {
            return mRadius * (float) Math.sin(radians) + mCenter.x;
        }

        /**
         * 获取圆上一点y坐标值
         *
         * @param radians sin角度
         * @return y of the point
         */
        public float getPy(double radians) {
            return mRadius * (float) Math.cos(radians) + mCenter.y;
        }

        /**
         * 获取圆上一点
         *
         * @param radians 弧度值
         * @return PointF
         */
        public PointF getPoint(double radians) {
            return new PointF(getPx(radians), getPy(radians));
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

    public static class Line {
        PointF mPointA;
        PointF mPointB;

        public Line(PointF pointA, PointF pointB) {
            mPointA = pointA;
            mPointB = pointB;
        }

        public Line(float pax, float pay, float pbx, float pby) {
            this(new PointF(pax, pay), new PointF(pbx, pby));
        }

        /**
         * 计算并返回两直线的交点
         * 经过A(a,b)B(c,d)直线的一般式(d-b)x-(c-a)y-a(d-b)+b(c-a)=0
         * 经过A(a,b)B(c,d)直线的一般式(d-b)x-(c-a)y=a(d-b)-b(c-a)
         *
         * @param line Line
         * @return PointF
         */
        public PointF intersectionPoint(Line line) {
            //利用行列式求解
            float a1 = line.mPointB.y - line.mPointA.y;
            float a2 = mPointA.y - mPointB.y;
            float b1 = line.mPointA.x - line.mPointB.x;
            float b2 = mPointB.x - mPointA.x;
            float c1 = line.mPointA.x * a1 - line.mPointA.y * b1;
            float c2 = mPointA.x * a2 - mPointA.y * b2;

            float x = (c1 * b2 - c2 * b1) / (a1 * b2 - a2 * b1);
            float y = (a1 * c2 - a2 * c1) / (a1 * b2 - a2 * b1);

            Log.d("xls", "x = " + -x);
            Log.d("xls", "y = " + -y);
            return new PointF(-x, -y);
        }
    }

}
