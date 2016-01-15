package com.lsxiao.draggablereddot.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lsxiao.draggablereddot.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View vAppLayout1 = findViewById(R.id.tv_tab_layout);
        View vListLayout = findViewById(R.id.tv_list_layout);

        vAppLayout1.setOnClickListener(this);
        vListLayout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_tab_layout: {
                TabActivity.start(this);
                break;
            }
            case R.id.tv_list_layout: {
                ListActivity.start(this);
                break;
            }
            default: {

            }
        }
    }
}
