package com.yiming.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ToggleButton;

import com.yiming.optionalpullview.YMOptionalPullView;

/**
 * author:yiming
 * date  :2016/4/14
 */
public class ScrollViewDemo extends AppCompatActivity implements YMOptionalPullView.OnPullListener, CompoundButton.OnCheckedChangeListener {

    YMOptionalPullView opv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_scrollview);
        initYMOptionalPullView();
        ((ToggleButton)findViewById(R.id.tb_down)).setOnCheckedChangeListener(this);
        ((ToggleButton)findViewById(R.id.tb_up)).setOnCheckedChangeListener(this);
    }

    private void initYMOptionalPullView() {
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
        }, 1500);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.tb_down:
                opv.setDownPullDisabled(!isChecked);
                break;
            case R.id.tb_up:
                opv.setUpPullDisabled(!isChecked);
                break;
        }
    }
}
