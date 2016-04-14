package com.yiming.optionalpullview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/**
 * author:yiming
 * date  :2016/3/27
 * 拉拉拉，刷刷刷，上拉下拉随便拉
 */
public class YMOptionalPullView extends LinearLayout {

    private static final String debug_tag = YMOptionalPullView.class.getSimpleName();

/********************以下是公共接口部分*************************************************************/

    /**
     * 设置需要添加刷新功能的View
     * @param contentView
     */
    public void setContentView(View contentView) {
        setContentView(contentView, null);
    }

    /**
     * 设置需要添加刷新功能的View
     * @param contentView
     */
    public void setContentView(View contentView, LayoutParams layoutParams) {
        if(mContentView != null) {
            throw new UnsupportedOperationException("ContentView exist already!");
        }
        if(layoutParams == null) {
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        } else {
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = 0;
        }
        layoutParams.weight = 1;
        mContentView = contentView;
        contentLayoutParams = layoutParams;
        addView(mContentView, 1, contentLayoutParams);
        if(!selectLocator(mContentView)) {
            Log.e(debug_tag,
                    "未找到相应的ContentViewLocator!!! 请通过setContentViewLocator()方法自行实现");
        }
        if(mBottomView == null) {
            adddBottomView();
        }
    }

    public void setOnPullListener(OnPullListener onPullListener) {
        this.mOnPullListener = onPullListener;
        if(this.mOnPullListener != null) {
            Mode mode = mOnPullListener.getMode();
            mMode = mode == null ? Mode.NEITHER_PULL : mode;
        }
    }

    public void setTransformViewBuilder(TransformViewBuilder tvb) {
        this.mViewBuilder = tvb;
        maxheight = tvb.getDragMaxHeight();
        criticalValue = tvb.getCriticalHeight();
    }

    /**
     * 加载完成时调用
     */
    public void notifyLoadComplete(boolean succeed) {
        appearLoadComplete(succeed);
        completeLoad();
    }

    public void setContentViewLocator(ContentViewLocator locator) {
        this.mContentViewLocator = locator;
    }

/********************以下是定制需要实现的接口*************************************************************/

    public static interface OnPullListener {
    /**
     * 通过此方法设置加载类型，上拉还是下拉还是上下都能拉
     * @return
     */
    Mode getMode();

    /**
     * 开始加载时的回调
     * @param opv
     * @param isDownPull
     */
        void onLoad(YMOptionalPullView opv, boolean isDownPull);
    }

    public static interface TransformViewBuilder {
        /**
         * 返回可以拖拽的最大高度
         * @return 最大高度
         */
        float getDragMaxHeight();

        /**
         * 返回临界点高度值
         * @return 临界值
         */
        float getCriticalHeight();

        /**
         * 添加头部下拉展示的布局
         * @param topView 头部View，需要把布局添加进去
         */
        void addCustomViewToTopView(LinearLayout topView);

        /**
         * 添加尾部上拉展示的布局
         * @param bottomView 尾部View，需要把布局添加进去
         */
        void addCustomViewToBottomView(LinearLayout bottomView);

        /**
         * 加载完成回调
         * @param succeed 加载成功或失败
         * @param isDownPull true是下拉， false是上拉
         */
        void appearLoadComplete(boolean succeed, boolean isDownPull);
        /**
         * 开始加载时回调
         * @param isDownPull true是下拉， false是上拉
         */
        void appearLoadingState(boolean isDownPull);

        /**
         *  正在拖拽时回调
         * @param height 当前已经拖拽的位移高度
         * @param isDownPull  true是下拉， false是上拉
         */
        void appearDraggingState(float height, boolean isDownPull);
    }


    /**
     * 判定View是不是滑到了最顶部或者最底部
     */
    public static interface ContentViewLocator {
        boolean isMostTop(View contentView);
        boolean isMostBottom(View contentView);
    }

/********************以下是再带的加载样式*************************************************************/

    private TextView topTv;
    private ImageView topIv;
    private TextView bottomTv;
    private ImageView bottomIv;
    /**
     * 自带的样式
     */
    private TransformViewBuilder mViewBuilder = new TransformViewBuilder() {

        public String topDraggingText1 = "下拉刷新";
        public String topDraggingText2 = "松开刷新";
        public String topLoadingText = "正在努力刷新";
        public String topLoadedText = "刷新成功";

        public String bottomDraggingText1 = "上拉加载更多";
        public String bottomDraggingText2 = "松开加载更多";
        public String bottomLoadingText = "正在努力加载";
        public String bottomLoadedText = "加载成功";

        @Override
        public float getDragMaxHeight() {
            return getResources().getDisplayMetrics().density * 200;
        }

        @Override
        public float getCriticalHeight() {
            return getResources().getDisplayMetrics().density * 60;
        }

        @Override
        public void addCustomViewToTopView(LinearLayout topView) {
            LinearLayout content = new LinearLayout(getContext());
            content.setGravity(Gravity.CENTER);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 100);
            ImageView iv = new ImageView(getContext());
            iv.setImageResource(R.drawable.loading_anim1);
            content.addView(iv);
            topIv = iv;
            TextView tv = new TextView(getContext());
            tv.setText(topLoadingText);
            tv.setEms(6);
            tv.setTextColor(Color.DKGRAY);
            tv.setTextSize(14);
            tv.setPadding((int) (getResources().getDisplayMetrics().density * 5), 0, 0, 0);
            tv.setGravity(Gravity.CENTER);
            content.addView(tv);
            topTv = tv;
            topView.addView(content, layoutParams);
//            topView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            topView.setGravity(Gravity.CENTER);
        }

        @Override
        public void addCustomViewToBottomView(LinearLayout bottomView) {
            LinearLayout content = new LinearLayout(getContext());
            content.setGravity(Gravity.CENTER);
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 100);
            ImageView iv = new ImageView(getContext());
            iv.setImageResource(R.drawable.loading_anim1);
            content.addView(iv);
            bottomIv = iv;
            TextView tv = new TextView(getContext());
            tv.setText(bottomLoadingText);
            tv.setEms(6);
            tv.setTextColor(Color.DKGRAY);
            tv.setTextSize(14);
            tv.setPadding((int) (getResources().getDisplayMetrics().density * 5), 0, 0, 0);
            tv.setGravity(Gravity.CENTER);
            content.addView(tv);
            bottomTv = tv;
            bottomView.addView(content, layoutParams);
//            bottomView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
            bottomView.setGravity(Gravity.CENTER);
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
                frameAnim.addFrame(getResources().getDrawable(R.drawable.loading_anim1), 100);
                frameAnim.addFrame(getResources().getDrawable(R.drawable.loading_anim2), 100);
                frameAnim.setOneShot(false);
                topIv.setImageDrawable(frameAnim);
                frameAnim.start();
            } else {
                bottomTv.setText(bottomLoadingText);
                AnimationDrawable frameAnim = new AnimationDrawable();
                frameAnim.addFrame(getResources().getDrawable(R.drawable.loading_anim1), 100);
                frameAnim.addFrame(getResources().getDrawable(R.drawable.loading_anim2), 100);
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
    };


/*********************************以下是核心代码***************************************************/

    private float maxheight = 600;
    private float criticalValue = 170;

    private ContentViewLocator mContentViewLocator;
    private OnPullListener mOnPullListener;
    private View mContentView;
    private LayoutParams contentLayoutParams;
    private LinearLayout mTopView;
    private LinearLayout mBottomView;
    private Mode mMode = Mode.NEITHER_PULL;
    private float lastDownActionY;
    private boolean  isMostTop;
    private boolean isMostBottom;
    private int mTouchSlop;
    private float overflowY;
    private float overflowYIncrement;

    public static enum Mode {
        UP_PULL,
        DOWN_PULL,
        BOTH_PULL,
        NEITHER_PULL
    }

    public YMOptionalPullView(Context context) {
        super(context);
        init();
    }

    public YMOptionalPullView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if(mViewBuilder != null) {
            criticalValue = mViewBuilder.getCriticalHeight();
            maxheight = mViewBuilder.getDragMaxHeight();
        }
        setOrientation(LinearLayout.VERTICAL);
        addTopView();
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setupContentView();
        if(mBottomView == null) {
            adddBottomView();
        }
    }

    private void setupContentView() {
        if(getChildCount() >= 2) {
            mContentView = getChildAt(1);
            contentLayoutParams= (LayoutParams) mContentView.getLayoutParams();
            contentLayoutParams.weight = 1;
            contentLayoutParams.width = LayoutParams.MATCH_PARENT;
            contentLayoutParams.height = 0;
            mContentView.setLayoutParams(contentLayoutParams);
            if(!selectLocator(mContentView)) {
                Log.e(debug_tag,
                        "未找到相应的ContentViewLocator!!! 请通过setContentViewLocator()方法自行实现");
            }
        }
    }

    private void addTopView() {
        mTopView = new LinearLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
//        mTopView.setBackgroundColor(Color.BLUE);
        addContentToTopView(mTopView);
        addView(mTopView, layoutParams);
    }

    private void adddBottomView() {
        mBottomView = new LinearLayout(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
//        mBottomView.setBackgroundColor(Color.RED);
        addContentToBottomView(mBottomView);
        addView(mBottomView, layoutParams);
    }

    public void addContentToTopView(LinearLayout topView) {
        mViewBuilder.addCustomViewToTopView(topView);
    }


    public void addContentToBottomView(LinearLayout bottomView) {
        mViewBuilder.addCustomViewToBottomView(bottomView);
    }

    /**
     * 拖拽时展示样式
     * @param height
     */
    private void appearDraggingState(float height) {
        mViewBuilder.appearDraggingState(height, transformViewIsDownPull);
    }

    /**
     * 正在加载时展示样式
     */
    private void appearLoadingState() {
        mViewBuilder.appearLoadingState(transformViewIsDownPull);
    }

    /**
     * 加载完成时展示样式
     * @param succeed
     */
    private void appearLoadComplete(boolean succeed) {
        mViewBuilder.appearLoadComplete(succeed, transformViewIsDownPull);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(debug_tag, "onInterceptTouchEvent(Down) y=" + ev.getY());
                if(mTopView.getHeight() > 0 || mBottomView.getHeight() > 0) {
                    return super.onInterceptTouchEvent(ev);
                }
                lastDownActionY = ev.getY();
                if(mMode == Mode.BOTH_PULL || mMode == Mode.DOWN_PULL) {
                    isMostTop = isMostTop();
                }
                if(mMode == Mode.BOTH_PULL || mMode == Mode.UP_PULL) {
                    isMostBottom = isMostBottom();
                }
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                Log.d(debug_tag, "onInterceptTouchEvent(Up) y=" + ev.getY());
                isMostBottom = false;
                isMostTop = false;
                overflowYIncrement = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                Log.d(debug_tag, "onInterceptTouchEvent(Move) y=" + ev.getY());
                if(isMostTop && (mMode == Mode.BOTH_PULL || mMode == Mode.DOWN_PULL)) {
                    overflowY = ev.getY() - lastDownActionY;
                    if(overflowY > mTouchSlop) {
                        Log.d(debug_tag, "onInterceptTouchEvent(Move) 拦截了，开始下拉");
                        transformViewIsDownPull = true;
                        return true;//已经到达最上或最下，拦截事件传递,转入onTouchEvent()
                    }
                } else if(isMostBottom && (mMode == Mode.BOTH_PULL || mMode == Mode.UP_PULL)) {
                    overflowY = lastDownActionY - ev.getY();
                    if(overflowY > mTouchSlop) {
                        Log.d(debug_tag, "onInterceptTouchEvent(Move) 拦截了,开始上拉");
                        transformViewIsDownPull = false;
                        return true;//已经到达最上或最下，拦截事件传递,转入onTouchEvent()
                    }
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private boolean isMostTop() {
        if(mContentViewLocator == null) {
            Log.d(debug_tag, "没有找到定位器!");
            return false;
        }
        return mContentViewLocator.isMostTop(mContentView);
    }

    private boolean isMostBottom() {
        if(mContentViewLocator == null) {
            Log.d(debug_tag, "没有找到定位器!");
            return false;
        }
        return mContentViewLocator.isMostBottom(mContentView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                startRecover();
                isMostBottom = false;
                isMostTop = false;
                overflowYIncrement = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                float temp;
                if(transformViewIsDownPull) {
                    temp = event.getY() - lastDownActionY;
                } else {
                    temp = lastDownActionY - event.getY();
                }
                if(temp >= 0) {
                    overflowYIncrement = temp - overflowY;
                    overflowY = temp;
                    pullTransition();
                }
                break;
        }

        return super.onTouchEvent(event);
    }

    private void pullTransition() {
        if(transformViewIsDownPull) {//下拉
            float height =  mTopView.getHeight() + overflowYIncrement * (1-mTopView.getHeight()/maxheight);
            changleViewHeight(height, true);
        } else {//上拉
            float height =  mBottomView.getHeight() + overflowYIncrement * (1-mBottomView.getHeight()/maxheight);
            changleViewHeight(height, true);
        }
    }

    private void changleViewHeight(float height, boolean refreshContent) {
        if(height > maxheight) {
            height = maxheight;
        } else if(height < 0) {
            height = 0;
        }
//        Log.d(debug_tag, "---height=" + height);
        View view = transformViewIsDownPull ? mTopView : mBottomView;
        LayoutParams params = (LayoutParams) view.getLayoutParams();
        params.height = (int) height;
        view.setLayoutParams(params);
        if(view == mBottomView) {
            if(mContentView instanceof RecyclerView) {//兼容
                RecyclerView rv = (RecyclerView) mContentView;
                rv.scrollBy(0, (int) height);
            } else {
                mContentView.scrollTo(0, params.height);
            }
        }
        if(refreshContent) {
            appearDraggingState(height);
        }
    }

    private void startRecover() {
        int curHeight = transformViewIsDownPull ? mTopView.getHeight() : mBottomView.getHeight();
        ValueAnimator animator1 = new ValueAnimator();
        int dragOverflow = (int)(curHeight - criticalValue);
        final boolean overtopCritical = dragOverflow > 0;//是否拉到临界值了
        animator1.setFloatValues(curHeight, overtopCritical ? criticalValue : 0);
        int duration = overtopCritical ? (int)(dragOverflow * 1.5f) : (int)(curHeight * 1.5f);
        animator1.setDuration(duration > 300 ? 300 : duration);
        animator1.setInterpolator(new DecelerateInterpolator(2));
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                changleViewHeight(value, false);
            }
        });
        animator1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                appearLoadingState();
                if (overtopCritical && mOnPullListener != null) {
                    mOnPullListener.onLoad(YMOptionalPullView.this, transformViewIsDownPull);
                }
            }
        });
        animator1.start();
    }

    private boolean transformViewIsDownPull;

    private void completeLoad() {
        ValueAnimator animator1 = new ValueAnimator();
        animator1.setFloatValues(criticalValue, 0);
        animator1.setDuration((int) (criticalValue * 1.5));
        animator1.setStartDelay(100);
        animator1.setInterpolator(new DecelerateInterpolator(3));
        animator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                changleViewHeight(value, false);
            }
        });
        animator1.start();
    }

    /**
     * 根据不同的滑动View选择相应的定位器
     * @param contentView
     * @return
     */
    private boolean selectLocator(View contentView) {
        if(contentView == null) return false;
        if(contentView instanceof ListView) {
            mContentViewLocator = new ListViewLocator();
            return true;
        } else if(contentView instanceof RecyclerView) {
            mContentViewLocator = new RecyclerViewLocator();
            return true;
        } else if(contentView instanceof GridView) {
//            mContentViewLocator = new GridViewLocator();
//            return true;
        } else if(contentView instanceof ScrollView) {
//            mContentViewLocator = new ScrollViewLocator();
//            return true;
        }

        return false;
    }

/*******************************以下是默认支持的滑动View定位器实现类*******************************************************/

    public static class ListViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            ListView lv = (ListView)contentView;
            int firstVisiblePosition = lv.getFirstVisiblePosition();
            int firstChildViewTop = lv.getChildAt(0).getTop();
            int lvPaddingTop = lv.getPaddingTop();
//            Log.d(debug_tag, "firstVisiblePosition=" + firstVisiblePosition
//                    + ", firstChildViewTop=" + firstChildViewTop + ", lvPaddingTop=" + lvPaddingTop);
            if(firstVisiblePosition == 0 && firstChildViewTop - lvPaddingTop == 0) {
                Log.d(debug_tag, "ListView isMostTop!");
                return true;
            }
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            ListView lv = (ListView)contentView;
            int lastVisiblePosition = lv.getLastVisiblePosition();
            int lastChildViewBottom = lv.getChildAt(lv.getChildCount()-1).getBottom();
            int lvPaddingBottom = lv.getPaddingBottom();
//            Log.d(debug_tag, "lastVisiblePosition=" + lastVisiblePosition + ", lv.getCount()=" + lv.getCount()
//                    + ", lastChildViewBottom=" + lastChildViewBottom + ", lvPaddingBottom=" + lvPaddingBottom + ", h=" + lv.getHeight());
            if(lastVisiblePosition == lv.getCount()-1 && lastChildViewBottom + lvPaddingBottom == lv.getHeight()) {
                Log.d(debug_tag, "ListView isMostBottom!");
                return true;
            }
            return false;
        }
    }

    public static class RecyclerViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            RecyclerView rv = (RecyclerView)contentView;
            RecyclerView.LayoutManager manager = rv.getLayoutManager();
//            int childTop = manager.getChildAt(0).getTop() - rv.getPaddingTop();
            View childView = manager.findViewByPosition(0);
            if(childView != null) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                if(childView.getTop() == rv.getPaddingTop() + params.topMargin) {
                    Log.d(debug_tag, "RecyclerView isMostBottom!");
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            RecyclerView rv = (RecyclerView)contentView;
            RecyclerView.LayoutManager manager = rv.getLayoutManager();
            View childView = manager.findViewByPosition(manager.getItemCount()-1);
            if(childView != null) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                if(childView.getBottom() == rv.getHeight() - rv.getPaddingBottom() - params.bottomMargin) {
                    Log.d(debug_tag, "RecyclerView isMostBottom!");
                    return true;
                }
            }
            return false;
        }
    }

    public static class GridViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            return false;
        }
    }

    public static class ScrollViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            return false;
        }
    }
}
