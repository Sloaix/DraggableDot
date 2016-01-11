package com.lsxiao.library;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Circle Shape
 * author:lsxiao
 * date:2015/12/25 19:04
 */
public class Circle {
    public PointF mCenter;
    public float mRadius;

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
     * @return return cut point, if the outPoint is inside,will return null.
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
     * 点是否在圆外
     *
     * @return boolean
     */
    public boolean isOutside(PointF p) {
        if (p == null) {
            throw new IllegalArgumentException("point can't be null");
        }
        return mRadius < Math.abs(Math.sqrt(Math.pow(p.x - mCenter.x, 2) + Math.pow(p.y - mCenter.y, 2)));
    }

    public boolean isInside(PointF p) {
        return !isOutside(p);
    }

    /**
     * 计算两圆之间圆心距离
     *
     * @param circle Circle
     * @return float
     */
    public float distanceToOtherCircle(Circle circle) {
        return (float) Math.abs(Math.sqrt(Math.pow(circle.mCenter.x - mCenter.x, 2) + Math.pow(circle.mCenter.y - mCenter.y, 2)));
    }

    @Override
    public String toString() {
        return "Circle{" +
                "mCenter= [" + mCenter.x + "," + mCenter.y + "]" +
                ", mRadius=" + mRadius +
                '}';
    }

    public static Circle copy(Circle circle) {
        return new Circle(circle.mCenter.x, circle.mCenter.y, circle.mRadius);
    }
}
