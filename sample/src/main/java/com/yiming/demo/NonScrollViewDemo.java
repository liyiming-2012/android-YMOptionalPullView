package com.yiming.demo;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.yiming.optionalpullview.YMOptionalPullView;

public class NonScrollViewDemo extends AppCompatActivity implements YMOptionalPullView.OnPullListener {

    YMOptionalPullView opv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_non_scrollview);
        opv = (YMOptionalPullView) findViewById(R.id.opv);
        opv.setOnPullListener(this);
    }

    @Override
    public YMOptionalPullView.Mode getMode() {
        return YMOptionalPullView.Mode.BOTH_PULL;
    }

    @Override
    public void onLoad(final YMOptionalPullView opv, boolean isDownPull) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                opv.notifyLoadComplete(true);
            }
        }, 2000);
    }
}
