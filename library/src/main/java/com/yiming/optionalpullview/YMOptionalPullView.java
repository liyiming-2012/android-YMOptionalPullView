package com.yiming.optionalpullview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

/*
# android-YMOptionalPullView Android下拉刷新库
        特点：
        1. 仅需1个超级View类文件(YMOptionalPullView.java)即可
        2. 接入代码最少、逻辑最简单、兼容性最好、自定义最容易...

        支持说明：
        1. 支持上拉、下拉、上下拉
        2. 支持自定义样式，通过YMOptionalPullView.TransformViewBuilder接口轻松实现, setTransformViewBuilder()设置
        3. 支持RecyclerView、ListView、GridView、WebView、ScrollViw
        4. 支持不需要滚动的View, 如LinearLayout、RelativeLayout、FrameLayout、TextView、ImageView...等等统统支持
        5. 其他复杂的View可以通过自定义YMOptionalPullView.ContentViewLocator类 setContentViewLocator()方法实现

        注意：
        因为支持了RecyclerView,所以需要在build.gradle文件的dependencies中引入：
        compile 'com.android.support:recyclerview-v7:22.2.1'
*/

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
            layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        } else {
            layoutParams.width = LayoutParams.MATCH_PARENT;
            layoutParams.height = LayoutParams.MATCH_PARENT;
        }
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
        resetHeight();
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
        addContentToTopView(mTopView);
        addContentToBottomView(mBottomView);
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

    /**
     * 设置是否关闭下拉功能
     * @param disabled
     */
    public void setDownPullDisabled(boolean disabled) {
        this.downPullDisabled = disabled;
    }

    public boolean getDownPullDisabled() {
        return this.downPullDisabled;
    }

    /**
     * 设置是否关闭上拉功能
     * @param disabled
     */
    public void setUpPullDisabled(boolean disabled) {
        this.upPullDisabled = disabled;
    }

    public boolean getUpPullDisabled() {
        return this.upPullDisabled;
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

/********************以下是自带的加载样式*************************************************************/

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

        private TextView topTv;
        private TextView topIndicator;
        private TextView bottomTv;
        private TextView bottomIndicator;
        private RotateAnimation topLoadingAnim;
        private RotateAnimation bottomLoadingAnim;

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
            float density = getResources().getDisplayMetrics().density;
            LinearLayout content = new LinearLayout(getContext());
            content.setGravity(Gravity.CENTER);

            topIndicator = new TextView(getContext());
            topIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            topIndicator.setTextColor(Color.DKGRAY);
            topIndicator.setGravity(Gravity.CENTER);
            topIndicator.setText("↓");
//            content.addView(topIndicator);
            content.addView(topIndicator, (int)(density * 40), (int)(density * 40));

            topLoadingAnim = new RotateAnimation(0f,360f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            topLoadingAnim.setDuration(500);
            topLoadingAnim.setRepeatCount(ValueAnimator.INFINITE);//无限循环
            topLoadingAnim.setRepeatMode(ValueAnimator.INFINITE);

            topTv = new TextView(getContext());
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
            float density = getResources().getDisplayMetrics().density;
            LinearLayout content = new LinearLayout(getContext());
            content.setGravity(Gravity.CENTER);

            bottomIndicator = new TextView(getContext());
            bottomIndicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            bottomIndicator.setTextColor(Color.DKGRAY);
            bottomIndicator.setGravity(Gravity.CENTER);
            bottomIndicator.setText("↑");
//            content.addView(bottomIndicator);
            content.addView(bottomIndicator, (int)(density * 40), (int)(density * 40));

            bottomLoadingAnim = new RotateAnimation(0f,360f, Animation.RELATIVE_TO_SELF,0.5f,Animation.RELATIVE_TO_SELF,0.5f);
            bottomLoadingAnim.setDuration(500);
            bottomLoadingAnim.setRepeatCount(ValueAnimator.INFINITE);//无限循环
            bottomLoadingAnim.setRepeatMode(ValueAnimator.INFINITE);

            bottomTv = new TextView(getContext());
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
                topIndicator.setText("⊙");
                topIndicator.clearAnimation();
            } else {
                bottomTv.setText(bottomLoadedText);
                bottomIndicator.setText("⊙");
                bottomIndicator.clearAnimation();
            }
        }

        @Override
        public void appearLoadingState(boolean isDownPull) {
            if(isDownPull) {
                topTv.setText(topLoadingText);
                topIndicator.setText("※");
                topIndicator.startAnimation(topLoadingAnim);
            } else {
                bottomTv.setText(bottomLoadingText);
                bottomIndicator.setText("※");
                bottomIndicator.startAnimation(bottomLoadingAnim);
            }
        }

        @Override
        public void appearDraggingState(float height, boolean isDownPull) {
            if(isDownPull) {
                if(height < getCriticalHeight()) {
                    topTv.setText(topDraggingText1);
                    topIndicator.setText("↓");
                } else {
                    topTv.setText(topDraggingText2);
                    topIndicator.setText("↑");
                }
            } else {
                if(height < getCriticalHeight()) {
                    bottomTv.setText(bottomDraggingText1);
                    bottomIndicator.setText("↑");
                } else {
                    bottomTv.setText(bottomDraggingText2);
                    bottomIndicator.setText("↓");
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
    private boolean isMostTop;
    private boolean isMostBottom;
    /**控制下拉功能是否失效*/
    private boolean downPullDisabled;
    /**控制上拉功能是否失效*/
    private boolean upPullDisabled;
    private int mTouchSlop;
    private float overflowY;
    private float overflowYIncrement;
    private float gap;

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
        resetHeight();
    }

    private void resetHeight() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
//                Log.d(debug_tag, "高度=" + mContentView.getMeasuredHeight());
                getViewTreeObserver().removeOnPreDrawListener(this);
                contentLayoutParams= (LayoutParams) mContentView.getLayoutParams();
                contentLayoutParams.height = mContentView.getMeasuredHeight();
                mContentView.setLayoutParams(contentLayoutParams);
                return true;
            }
        });
    }

    private void setupContentView() {
        if(getChildCount() >= 2) {
            mContentView = getChildAt(1);
            contentLayoutParams= (LayoutParams) mContentView.getLayoutParams();
            contentLayoutParams.width = LayoutParams.MATCH_PARENT;
            contentLayoutParams.height = LayoutParams.MATCH_PARENT;
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
        topView.removeAllViews();
        mViewBuilder.addCustomViewToTopView(topView);
    }


    public void addContentToBottomView(LinearLayout bottomView) {
        bottomView.removeAllViews();
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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        super.dispatchTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        Log.d(debug_tag, "onIntercept(" + ev.getAction() + ") y=" + ev.getY());
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
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
                isMostBottom = false;
                isMostTop = false;
                overflowYIncrement = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                isInToOnTouch = false;
                if(isIntercept(ev)) {
                    return true;
                }
                break;
        }

        return super.onInterceptTouchEvent(ev);
    }

    private boolean isIntercept(MotionEvent ev) {
        gap = ev.getY() - lastDownActionY;
        if(gap > mTouchSlop && isMostTop && (mMode == Mode.BOTH_PULL || mMode == Mode.DOWN_PULL) && !downPullDisabled) {
            overflowY = Math.abs(gap);
            Log.d(debug_tag, "onIntercept(Move) 拦截了，开始下拉");
            transformViewIsDownPull = true;
            setGravity(Gravity.TOP);
            return true;//已经到达最上或最下，拦截事件传递,转入onTouchEvent()
        } else if(gap < -mTouchSlop && isMostBottom && (mMode == Mode.BOTH_PULL || mMode == Mode.UP_PULL) && !upPullDisabled) {
            overflowY = Math.abs(gap);
            Log.d(debug_tag, "onIntercept(Move) 拦截了,开始上拉");
            transformViewIsDownPull = false;
            setGravity(Gravity.BOTTOM);
            return true;//已经到达最上或最下，拦截事件传递,转入onTouchEvent()
        }
        return false;
    }

    /**为了兼容没有消费事件的子View，记录是否进入onTouchEvent()的标记*/
    private boolean isInToOnTouch = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
//                Log.d(debug_tag, "!!!!!!!!!onTouch(Down) y=" + event.getY());
                isInToOnTouch = true;
                break;
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
//                Log.d(debug_tag, "onTouch(UP) y=" + event.getY());
                startRecover();
                isMostBottom = false;
                isMostTop = false;
                overflowYIncrement = 0;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(debug_tag, "onTouch(MOVE) y=" + event.getY());
                if(isInToOnTouch) {
                    if(isIntercept(event)) {
                        isInToOnTouch = false;
                        prePullTransition(event);
                    }
                } else {
                    prePullTransition(event);
                }
                break;
        }
        return super.onTouchEvent(event);
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

    private void prePullTransition(MotionEvent event) {
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
    }

    private void pullTransition() {
        if(transformViewIsDownPull) {//下拉
            float height =  mTopView.getHeight() + overflowYIncrement * (1-mTopView.getHeight()/maxheight);
            changeViewHeight(height, true);
        } else {//上拉
            float height =  mBottomView.getHeight() + overflowYIncrement * (1-mBottomView.getHeight()/maxheight);
            changeViewHeight(height, true);
        }
    }

    private void changeViewHeight(float height, boolean refreshContent) {
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
                changeViewHeight(value, false);
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
                changeViewHeight(value, false);
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
            mContentViewLocator = new GridViewLocator();
            return true;
        } else if(contentView instanceof ScrollView) {
            mContentViewLocator = new ScrollViewLocator();
            return true;
        } else if(contentView instanceof WebView) {
            mContentViewLocator = new WebViewLocator();
            return true;
        } else {
            mContentViewLocator = new NonScrollViewLocator();
            return true;
        }

//        return false;
    }

/*******************************以下是默认支持的滑动View定位器实现类*******************************************************/

    public static class ListViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            ListView lv = (ListView)contentView;
            int firstVisiblePosition = lv.getFirstVisiblePosition();
            int firstChildViewTop = lv.getChildAt(0).getTop();
            int lvPaddingTop = lv.getPaddingTop();
            if(firstVisiblePosition == 0 && firstChildViewTop - lvPaddingTop == 0) {
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
            if(lastVisiblePosition == lv.getCount()-1 && lastChildViewBottom + lvPaddingBottom <= lv.getHeight()) {
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
            View childView = manager.findViewByPosition(0);
            if(childView != null) {
                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
                if(childView.getTop() == rv.getPaddingTop() + params.topMargin) {
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
                if(childView.getBottom() <= rv.getHeight() - rv.getPaddingBottom() - params.bottomMargin) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class GridViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            GridView gv = (GridView) contentView;
            int firstVisiblePosition = gv.getFirstVisiblePosition();
            int firstChildViewTop = gv.getChildAt(0).getTop();
            int lvPaddingTop = gv.getPaddingTop();
            if(firstVisiblePosition == 0 && firstChildViewTop - lvPaddingTop == 0) {
                return true;
            }
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            GridView gv = (GridView)contentView;
            int lastVisiblePosition = gv.getLastVisiblePosition();
            int lastChildViewBottom = gv.getChildAt(gv.getChildCount()-1).getBottom();
            int lvPaddingBottom = gv.getPaddingBottom();
            if(lastVisiblePosition == gv.getCount()-1 && lastChildViewBottom + lvPaddingBottom <= gv.getHeight()) {
                return true;
            }
            return false;
        }
    }

    public static class ScrollViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            ScrollView sv = (ScrollView)contentView;
            if(sv.getScrollY() == 0) {
                return true;
            }
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            ScrollView sv = (ScrollView)contentView;
            View childView = sv.getChildAt(0);
            if(childView.getMeasuredHeight() + sv.getPaddingBottom() <= sv.getScrollY() + sv.getMeasuredHeight()) {
                return true;
            }
            return false;
        }
    }

    public static class WebViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            WebView wv = (WebView) contentView;
            if(wv.getScrollY() == 0) {
                return true;
            }
            return false;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            WebView wv = (WebView) contentView;
            if(wv.getContentHeight()*wv.getScale() == (wv.getHeight()+wv.getScrollY())){
                return true;
            }
            return false;
        }
    }

    public static class NonScrollViewLocator implements ContentViewLocator {

        @Override
        public boolean isMostTop(View contentView) {
            return true;
        }

        @Override
        public boolean isMostBottom(View contentView) {
            return true;
        }
    }
}
