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
    public PointF center;
    public float radius;

    public Circle(PointF center, float radius) {
        this.center = center;
        this.radius = radius;
    }

    public Circle(float cx, float cy, float radius) {
        this(new PointF(cx, cy), radius);
    }

    public void draw(Canvas canvas, Paint paint) {
        canvas.drawCircle(center.x, center.y, radius, paint);
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

        cutPoint.x = outPoint.x - center.x;
        cutPoint.y = outPoint.y - center.y;

        float ratio = radius / (float) Math.sqrt(cutPoint.x * cutPoint.x + cutPoint.y * cutPoint.y);
        cutPoint.x *= ratio;
        cutPoint.y *= ratio;

        temp = new PointF(cutPoint.x, cutPoint.y);
        float radians = first ? (float) Math.acos(ratio) : (float) -Math.acos(ratio);

        cutPoint.x = temp.x * (float) Math.cos(radians) - temp.y * (float) Math.sin(radians) + center.x;
        cutPoint.y = temp.x * (float) Math.sin(radians) + temp.y * (float) Math.cos(radians) + center.y;

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
        System.out.println(center);
//        System.out.println(p.x+" "+p.y);
//        System.out.println(distanceOfPoints(p, center));
        return radius < distanceOfPoints(p, center);
    }


    public static double distanceOfPoints(PointF a, PointF b) {
        return distanceOfPoints(a.x, a.y, b.x, b.y);
    }

    public static double distanceOfPoints(double x1, double y1, double x2, double y2) {
        double d = (x2 - x1) * (x2 - x1) - (y2 - y1) * (y2 - y1);
        return Math.sqrt(d);
    }

    /**
     * check the point if in circle.
     *
     * @param p PointF
     * @return true, if is inside.
     */
    public boolean isContainPoint(PointF p) {
        return !isOutside(p);
    }

    /**
     * 计算两圆之间圆心距离
     *
     * @param circle Circle
     * @return float
     */
    public float distanceToOtherCircle(Circle circle) {
        return (float) Math.abs(Math.sqrt(Math.pow(circle.center.x - center.x, 2) + Math.pow(circle.center.y - center.y, 2)));
    }

    @Override
    public String toString() {
        return "Circle{" +
                "center= [" + center.x + "," + center.y + "]" +
                ", radius=" + radius +
                '}';
    }

    public static Circle copy(Circle circle) {
        return new Circle(circle.center.x, circle.center.y, circle.radius);
    }
}
