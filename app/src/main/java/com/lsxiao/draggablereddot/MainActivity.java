package com.lsxiao.draggablereddot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lsxiao.library.DotView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DotView dotView = (DotView) findViewById(R.id.dot);
        dotView.init(this);
    }
}
