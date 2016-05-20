package com.yiming.demo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.yiming.optionalpullview.YMOptionalPullView;

/**
 * author:yiming
 * date  :2016/5/20
 */
public class CustomViewBuilder implements YMOptionalPullView.TransformViewBuilder {

    private String topDraggingText1 = "下拉刷新";
    private String topDraggingText2 = "松开刷新";
    private String topLoadingText = "正在努力刷新";
    private String topLoadedText = "刷新成功";

    private String bottomDraggingText1 = "上拉加载更多";
    private String bottomDraggingText2 = "松开加载更多";
    private String bottomLoadingText = "正在努力加载";
    private String bottomLoadedText = "加载成功";

    private TextView topTv;
    private ImageView topIv;
    private TextView bottomTv;
    private ImageView bottomIv;

    private Context mContext;
    private Resources res;
    private float density;

    public CustomViewBuilder(Context context) {
        this.mContext = context;
        this.res = context.getResources();
        this.density = res.getDisplayMetrics().density;
    }

    @Override
    public float getDragMaxHeight() {
        return density * 200;
    }

    @Override
    public float getCriticalHeight() {
        return density * 60;
    }

    @Override
    public void addCustomViewToTopView(LinearLayout topView) {
        LinearLayout content = new LinearLayout(mContext);
        content.setGravity(Gravity.CENTER);

        topIv = new ImageView(mContext);
        topIv.setImageResource(R.drawable.loading_anim1);
        content.addView(topIv, (int)(density * 40), (int)(density * 40));

        topTv = new TextView(mContext);
        topTv.setText(topLoadingText);
        topTv.setEms(6);
        topTv.setTextColor(Color.DKGRAY);
        topTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        topTv.setPadding((int) (density * 5), 0, 0, 0);
        topTv.setGravity(Gravity.CENTER);
        content.addView(topTv);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        topView.addView(content, layoutParams);
//            topView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        topView.setGravity(Gravity.CENTER);
        topView.setBackgroundColor(0xcccccccc);
    }

    @Override
    public void addCustomViewToBottomView(LinearLayout bottomView) {
        LinearLayout content = new LinearLayout(mContext);
        content.setGravity(Gravity.CENTER);

        bottomIv = new ImageView(mContext);
        bottomIv.setImageResource(R.drawable.loading_anim1);
        content.addView(bottomIv, (int)(density * 40), (int)(density * 40));

        bottomTv = new TextView(mContext);
        bottomTv.setText(bottomLoadingText);
        bottomTv.setEms(6);
        bottomTv.setTextColor(Color.DKGRAY);
        bottomTv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        bottomTv.setPadding((int) (density * 5), 0, 0, 0);
        bottomTv.setGravity(Gravity.CENTER);
        content.addView(bottomTv);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        bottomView.addView(content, layoutParams);
//            bottomView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        bottomView.setGravity(Gravity.CENTER);
        bottomView.setBackgroundColor(0xcccccccc);
    }

    @Override
    public void appearLoadComplete(boolean succeed, boolean isDownPull) {
        if(isDownPull) {
            topTv.setText(topLoadedText);
            topIv.setImageResource(R.drawable.load_succeed);
        } else {
            bottomTv.setText(bottomLoadedText);
            bottomIv.setImageResource(R.drawable.load_succeed);
        }
    }

    @Override
    public void appearLoadingState(boolean isDownPull) {
        if(isDownPull) {
            topTv.setText(topLoadingText);
            AnimationDrawable frameAnim = new AnimationDrawable();
            frameAnim.addFrame(res.getDrawable(R.drawable.loading_anim1), 100);
            frameAnim.addFrame(res.getDrawable(R.drawable.loading_anim2), 100);
            frameAnim.setOneShot(false);
            topIv.setImageDrawable(frameAnim);
            frameAnim.start();
        } else {
            bottomTv.setText(bottomLoadingText);
            AnimationDrawable frameAnim = new AnimationDrawable();
            frameAnim.addFrame(res.getDrawable(R.drawable.loading_anim1), 100);
            frameAnim.addFrame(res.getDrawable(R.drawable.loading_anim2), 100);
            frameAnim.setOneShot(false);
            bottomIv.setImageDrawable(frameAnim);
            frameAnim.start();
        }
    }

    @Override
    public void appearDraggingState(float height, boolean isDownPull) {
        if(isDownPull) {
            if(height < getCriticalHeight()) {
                topTv.setText(topDraggingText1);
                topIv.setImageResource(R.drawable.load_succeed);
            } else {
                topTv.setText(topDraggingText2);
                topIv.setImageResource(R.drawable.loading_anim1);
            }
        } else {
            if(height < getCriticalHeight()) {
                bottomTv.setText(bottomDraggingText1);
                bottomIv.setImageResource(R.drawable.load_succeed);
            } else {
                bottomTv.setText(bottomDraggingText2);
                bottomIv.setImageResource(R.drawable.loading_anim1);
            }
        }
    }
}
