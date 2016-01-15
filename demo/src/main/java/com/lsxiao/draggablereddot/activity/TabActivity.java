package com.lsxiao.draggablereddot.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.lsxiao.draggablereddot.R;
import com.lsxiao.draggablereddot.base.BaseActivity;

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

    }

    public static void start(Activity activity) {
        final Intent intent = new Intent(activity, TabActivity.class);
        activity.startActivity(intent);
    }
}
