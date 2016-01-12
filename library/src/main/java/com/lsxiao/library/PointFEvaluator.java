package com.lsxiao.library;

import android.animation.TypeEvaluator;
import android.graphics.PointF;

/**
 * author lsxiao
 * date 2015-12-26 16:49
 */
public class PointFEvaluator implements TypeEvaluator<PointF> {

    public PointFEvaluator() {
    }

    @Override
    public PointF evaluate(float fraction, PointF startValue, PointF endValue) {
        float x = startValue.x + (fraction * (endValue.x - startValue.x));
        float y = startValue.y + (fraction * (endValue.y - startValue.y));

        return new PointF(x, y);
    }
}
