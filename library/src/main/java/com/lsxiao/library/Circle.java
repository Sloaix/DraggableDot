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
     * intersection
     *
     * @param outPoint a point out of circle
     * @param first    the first intersection
     * @return return intersection, if the outPoint is inside,will return null.
     */
    public PointF getIntersection(PointF outPoint, boolean first) {

        final PointF intersection = new PointF(0, 0);
        final PointF temp;

        intersection.x = outPoint.x - center.x;
        intersection.y = outPoint.y - center.y;

        float ratio = radius / (float) Math.sqrt(intersection.x * intersection.x + intersection.y * intersection.y);
        intersection.x *= ratio;
        intersection.y *= ratio;

        temp = new PointF(intersection.x, intersection.y);
        float radians = first ? (float) Math.acos(ratio) : (float) -Math.acos(ratio);

        intersection.x = temp.x * (float) Math.cos(radians) - temp.y * (float) Math.sin(radians) + center.x;
        intersection.y = temp.x * (float) Math.sin(radians) + temp.y * (float) Math.cos(radians) + center.y;

        return intersection;
    }

    public PointF[] commonTangentIntersections(Circle ci) {
        float x1 = center.x;
        float y1 = center.y;
        float x2 = ci.center.x;
        float y2 = ci.center.y;
        float r1 = radius;
        float r2 = ci.radius;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float d = (float) Math.sqrt(dx * dx + dy * dy);

        float a1 = (float) Math.atan((y2 - y1) / (x1 - x2));
        float a2 = (float) Math.asin((r1 - r2) / d);
        float a3 = (float) Math.PI / 2 - a1 - a2;
        float a4 = (float) Math.PI / 2 - a1 + a2;


        float offX1 = (float) Math.cos(a3) * r1;
        float offY1 = (float) Math.sin(a3) * r1;
        float offX2 = (float) Math.cos(a3) * r2;
        float offY2 = (float) Math.sin(a3) * r2;
        float offX3 = (float) Math.cos(a4) * r1;
        float offY3 = (float) Math.sin(a4) * r1;
        float offX4 = (float) Math.cos(a4) * r2;
        float offY4 = (float) Math.sin(a4) * r2;

        float p1x = x1 - offX1;
        float p1y = y1 - offY1;
        float p2x = x2 - offX2;
        float p2y = y2 - offY2;
        float p3x = x1 + offX3;
        float p3y = y1 + offY3;
        float p4x = x2 + offX4;
        float p4y = y2 + offY4;


        PointF p1 = new PointF(p1x, p1y);
        PointF p2 = new PointF(p2x, p2y);
        PointF p3 = new PointF(p3x, p3y);
        PointF p4 = new PointF(p4x, p4y);

        return new PointF[]{p1, p2, p3, p4};
    }

    public boolean isOutside(PointF p) {
        if (p == null) {
            throw new IllegalArgumentException("point can't be null");
        }
        System.out.println(center);
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
     * distance between circle center
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

    public static Circle clone(Circle circle) {
        return new Circle(circle.center.x, circle.center.y, circle.radius);
    }
}
