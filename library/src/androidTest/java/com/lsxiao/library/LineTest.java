package com.lsxiao.library;

import android.test.AndroidTestCase;

/**
 * author lsxiao
 * date 2015-12-24 01:52
 */
public class LineTest extends AndroidTestCase {
    DotView.Line mLineA;
    DotView.Line mLineB;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mLineA = new DotView.Line(0f, 1f, 1f, 1f);
        mLineB = new DotView.Line(0f, 0f, 0f, 1f);
    }

    public void testIntersection() throws Exception {
        assertEquals(mLineB.intersectionPoint(mLineA).y, 1f);
    }
}
