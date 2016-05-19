package com.yiming.demo;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yiming.optionalpullview.YMOptionalPullView;

/**
 * author:yiming
 * date  :2016/4/14
 */
public class RecyclerViewDemo extends AppCompatActivity implements YMOptionalPullView.OnPullListener {

    private YMOptionalPullView opv;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_recycler);
        initRecyclerView();
        initYMOptionalPullView();
    }

    private void initRecyclerView() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
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

    private RecyclerView.Adapter adapter = new RecyclerView.Adapter() {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            TextView tv = (TextView) LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, null);
            tv.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new MyViewHolder(tv);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView tv = (TextView) holder.itemView;
            tv.setText("text " + (position + 1));
        }

        @Override
        public int getItemCount() {
            return 20;
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }
    };
}
