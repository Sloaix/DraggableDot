package com.lsxiao.library;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * author lsxiao
 * date 2015-12-26 16:49
 */
public class PointFEvaluator implements TypeEvaluator<PointF> {
    private PointF mPoint;

    public PointFEvaluator() {
    }

    public PointFEvaluator(PointF reuse) {
        mPoint = reuse;
    }

    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        float x = startValue.x + (fraction * (endValue.x - startValue.x));
        float y = startValue.y + (fraction * (endValue.y - startValue.y));

        if (mPoint != null) {
            mPoint.set(x, y);
            return mPoint;
        } else {
            return new PointF(x, y);
        }
    }
}
