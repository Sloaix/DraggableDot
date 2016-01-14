package com.lsxiao.library;

import android.graphics.PointF;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class CircleTest {
    PointF p;
    Circle c;

    @Before
    public void setUp() {
        c = new Circle(1, 1, 3);
        p = new PointF(0, 0);
    }

    @Test
    public void pointIsOutOfCircle() throws Exception {
        p.x =p.y= 10;
        assertTrue(c.isOutside(p));
    }

    @Test
    public void pointIsInCircle() throws Exception {
        p.x =p.y= 2;
        assertTrue(c.isOutside(p));
    }
}