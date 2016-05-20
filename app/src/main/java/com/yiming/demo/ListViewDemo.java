package com.yiming.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.yiming.optionalpullview.YMOptionalPullView;

/**
 * author:yiming
 * date  :2016/4/14
 */
public class ListViewDemo extends AppCompatActivity implements YMOptionalPullView.OnPullListener {

    ListView listView;
    YMOptionalPullView opv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContent1();//是否XML布局
//        setContent2();//使用代码中设置布局
        initYMOptionalPullView();
        initListView();
    }

    private void setContent1() {
        setContentView(R.layout.layout_listview);
        opv = (YMOptionalPullView) findViewById(R.id.opv);
        listView = (ListView) findViewById(R.id.listview);
    }

    private void setContent2() {
        opv = new YMOptionalPullView(this);
        listView = new ListView(this);
        opv.setContentView(listView);
        setContentView(opv);
    }

    private void initListView() {
        String[] arr = new String[20];
        for (int i=0; i<arr.length; i++) arr[i] = "item " + (i+1);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arr);
        listView.setAdapter(adapter);
    }

    private void initYMOptionalPullView() {
        opv.setOnPullListener(this);
    }

    @Override
    public YMOptionalPullView.Mode getMode() {//设置支持上下拉
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
