package com.lsxiao.library;

import android.graphics.Point;

/**
 * author:lsxiao
 * date:2015/12/25 19:39
 */
public class Line {
    public final double m, b;

    public Line(double m, double b) {
        this.m = m;
        this.b = b;
    }

    public Point intersect(Line line) {
        double x = (line.b - this.b) / (this.m - line.m);
        double y = this.m * x + this.b;
        return new Point((int) x, (int) y);
    }

    public int get(int x) {
        return (int) (this.m * x + this.b);
    }

    public double get(double x) {
        return this.m * x + this.b;
    }
}
