package com.yiming.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * author:yiming
 * date  :2016/5/17
 */
public class NonScrollView extends RelativeLayout {

    public NonScrollView(Context context) {
        super(context);
    }

    public NonScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d("NonScrollView", "NonScrollView->dispatchTouchEvent()--" + ev.getAction());
        boolean b = super.dispatchTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("NonScrollView", "NonScrollView->dispatchTouchEvent(Down) return " + b);
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d("NonScrollView", "NonScrollView->dispatchTouchEvent(Up) return " + b);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("NonScrollView", "NonScrollView->dispatchTouchEvent(Move) return " + b);
                break;
        }
        return b;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d("NonScrollView", "NonScrollView->onInterceptTouchEvent()--" + ev.getAction());
        boolean b = super.onInterceptTouchEvent(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("NonScrollView", "NonScrollView->onInterceptTouchEvent(Down) return " + b);
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d("NonScrollView", "NonScrollView->onInterceptTouchEvent(Up) return " + b);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("NonScrollView", "NonScrollView->onInterceptTouchEvent(Move) return " + b);
                break;
        }
        return b;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("NonScrollView", "NonScrollView->onTouchEvent()--" + event.getAction());
        boolean b = super.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d("NonScrollView", "NonScrollView->onTouchEvent(Down) return " + b);
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d("NonScrollView", "NonScrollView->onTouchEvent(Up) return " + b);
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d("NonScrollView", "NonScrollView->onTouchEvent(Move) return " + b);
                break;
        }
        return true;
    }
}
