package com.lsxiao.library;

import android.app.Application;
import android.graphics.PointF;
import android.test.ApplicationTestCase;
import android.util.Log;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    PointF p;
    Circle c;

    public void setUp() {
        c = new Circle(1, 1, 3);
        p = new PointF(0, 0);
    }

    public void testPointIsOutOfCircle() throws Exception {
        p.x = p.y = 10;
        Log.d("xls",c.isOutside(p)+"");
    }

    public void testPointIsInCircle() throws Exception {
        p.x = p.y = 2;
        Log.d("xls", c.isOutside(p) + "");
    }
}