package com.lsxiao.draggablereddot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.lsxiao.draggablereddot.R;
import com.lsxiao.draggablereddot.base.BaseActivity;
import com.lsxiao.library.DotView;

/**
 * author:lsxiao
 * date:2016/01/04 18:54
 */
public class TabActivity extends BaseActivity {
    @Override
    protected int getLayoutId() {
        return R.layout.activity_tab;
    }

    @Override
    protected void afterCreate(Bundle savedInstanceState) {
        DotView dotView = (DotView) findViewById(R.id.dot);
        dotView.setOnDotStateChangedListener(new DotView.onDotStateChangedListener() {
            @Override
            public void onStretch(DotView dotView) {
                Log.d("xls", "onStretch");
            }

            @Override
            public void onDrag(DotView dotView) {
                Log.d("xls", "onDrag");
            }

            @Override
            public void onDismissed(DotView dotView) {
                Log.d("xls", "onDismissed");
            }
        });
    }

    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, TabActivity.class);
        activity.startActivity(intent);
    }
}
