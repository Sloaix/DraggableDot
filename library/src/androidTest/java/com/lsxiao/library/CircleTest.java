package com.lsxiao.library;

import android.graphics.PointF;
import android.test.AndroidTestCase;

/**
 * author lsxiao
 * date 2015-12-24 00:22
 */
public class CircleTest extends AndroidTestCase {
    DotView.Circle mFixedCir;
    DotView.Circle mDragCir;
    PointF p;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        mFixedCir = new DotView.Circle(0f, 0f, 3f);
        mDragCir = new DotView.Circle(4f, 3f, 3f);
//        p = DotView.calCirclePoint(mFixedCir, mDragCir);
//        Log.d("xls",Math.toDegrees(Math.asin(0.5))+"");
    }

    public void testLengthBetweenCenter() throws Exception {
        assertEquals(mFixedCir.lengthBetweenCenter(mDragCir), 5f);
    }

    public void testCutPointPosition() throws Exception {
        assertEquals(p.x, 0f);
        assertEquals(p.x, mFixedCir.mCenter.x);
        assertEquals(p.y, mFixedCir.mRadius);
        assertEquals(p.y, mDragCir.mRadius);
    }

    public void testRightAngleSizeLength() throws Exception {
        //勾三股四弦五
        assertEquals(DotView.getLegLength(5, 3), 4f);
    }
}
