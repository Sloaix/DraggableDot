package com.lsxiao.library;

import android.graphics.PointF;
import android.test.AndroidTestCase;
import android.util.Log;

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

    public void testQiedian() throws Exception {
        Log.d("xls", cal(new PointF(0, 0), new PointF(18.72f, -1.72f), 10,true) + "");
    }

    PointF cal(PointF pCenter, PointF pOutside, float radius, boolean firstPoint) {
        PointF E = new PointF(0, 0);
        PointF F = new PointF(0, 0);
        PointF G = new PointF(0, 0);
        PointF H = new PointF(0, 0);

        //1. 坐标平移到圆心ptCenter处,求园外点的新坐标E
        E.x = pOutside.x - pCenter.x;
        E.y = pOutside.y - pCenter.y; //平移变换到E

        //2. 求园与OE的交点坐标F, 相当于E的缩放变换
        float t = radius / (float) Math.sqrt(E.x * E.x + E.y * E.y);  //得到缩放比例
        F.x = E.x * t;
        F.y = E.y * t;   //缩放变换到F

        //3. 将E旋转变换角度a到切点G，其中cos(a)=r/OF=t, 所以a=arccos(t);
        float a = firstPoint ? (float) Math.acos(t) : (float) -Math.acos(t);  //得到旋转角度
        G.x = F.x * (float) Math.cos(a) - F.y * (float) Math.sin(a);
        G.y = F.x * (float) Math.sin(a) + F.y * (float) Math.cos(a);    //旋转变换到G

        //4. 将G平移到原来的坐标下得到新坐标H
        H.x = G.x + pCenter.x;
        H.y = G.y + pCenter.y;             //平移变换到H

        //5. 返回H
        return new PointF(H.x, H.y);
        //6. 实际应用过程中，只要一个中间变量E,其他F,G,H可以不用。
    }
}
