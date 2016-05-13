package com.yiming.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.yiming.optionalpullview.YMOptionalPullView;

/**
 * author:yiming
 * date  :2016/4/14
 */
public class GridViewDemo extends AppCompatActivity implements YMOptionalPullView.OnPullListener {

    GridView gridView;
    YMOptionalPullView opv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gridview);
        initGridView();
        initYMOptionalPullView();
    }

    private void initGridView() {
        gridView = (GridView) findViewById(R.id.gridView);
        String[] arr = new String[100];
        for (int i=0; i<arr.length; i++) arr[i] = "item " + (i+1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        gridView.setAdapter(adapter);
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
    public void onLoad(YMOptionalPullView opv, boolean isDownPull) {

    }
}
