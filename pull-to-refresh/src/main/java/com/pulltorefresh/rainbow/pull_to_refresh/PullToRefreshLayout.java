package com.pulltorefresh.rainbow.pull_to_refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * usage
 *
 * xml define
 *
 <com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout
     android:id="@+id/ptf_layout"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     // you can define header in .xml use header_layout
     // ptf:header_layout="@layout/header_layout"

     // you can define content in .xml use content_layout
     // ptf:content_layout="@layout/content_layout"

     // if there is scrollview(such as listview, gridview, scrollview) in content, so define it to perform currently.
     // ptf:scroll_id="@+id/scrollview"

     //you can also difine header view yourself, if no define, will use DefaultHeaderView
     <View />


     <GridView
        android:id="@+id/gridview"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:numColumns="2"/>

 </com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout>
 *
 * in java
 *
 * PullToRefreshLayout ptfLayout = (PullToRefreshLayout) root.findViewById(R.id.ptf_layout);
 *
 * //register refresh callback
 * ptfLayout.setRefreshCallback();
 *
 * //register header ui callback
 * ptflayout.setHeaderUICallback();
 *
 * //call when refresh is completed
 * ptflayout.refreshComplete()
 *
 * //start auto refresh without MotionEvent
 * ptflayout.autoRefresh()
 *
 *
 * other extend function:
 *
 * //set Animation duration
 * {@link #setScrollAnimationDuration(int)}
 * //set refresh critical position
 * {@link #setRefreshingLine(float)}
 * //set the coefficient of friction of pull to refresh action
 * {@link #setCoefficientOfFriction(float)}
 *
 * more usage see GridListFragment, ListViewFragment
 *
 * https://github.com/RainbleNi/PullToRefresh
 */
public class PullToRefreshLayout extends ViewGroup implements ScrollHandler.ScrollHandlerCallback {
    private int mHeaderLayout;
    private int mContentLayout;
    private View mHeaderView;
    private View mContentView;
    private final ScrollHandler mScrollHandler;
    private RefreshCallback mRefreshCallback;
    private HeaderUICallback mHeaderUICallback;
    private int mScrollId;
    private View mScrollView;

    /**
     * refresh callback, use to receive refresh event
     */
    public interface RefreshCallback {
        void onRefresh();
    }

    /**
     * contain 4 UI states of header, usage see DefaultHeaderView.
     */
    public interface HeaderUICallback {
        void onStatePullToRefresh();
        void onStateReleaseToRefresh();
        void onStateRefreshing();
        void onStateComplete();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        mHeaderLayout = a.getResourceId(R.styleable.PullToRefreshLayout_header_layout, 0);
        mContentLayout = a.getResourceId(R.styleable.PullToRefreshLayout_content_layout, 0);
        mScrollId = a.getResourceId(R.styleable.PullToRefreshLayout_scroll_id, 0);
        LayoutInflater inflater = LayoutInflater.from(context);
        if (mHeaderLayout != 0) {
            mHeaderView = inflater.inflate(mHeaderLayout, this, false);
        }
        if (mContentLayout != 0) {
            mContentView = inflater.inflate(mContentLayout, this, false);
        }
        mScrollHandler = new ScrollHandler(context, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mHeaderView == null) {
            if (mContentView == null) {
                if (getChildCount() == 1) {
                    mContentView = getChildAt(0);
                } else if (getChildCount() == 2) {
                    mHeaderView = getChildAt(0);
                    mContentView = getChildAt(1);
                } else if (getChildCount() == 0) {
                    throw new IllegalStateException("you have not declear content in R.styleable.PullToRefreshLayout_header_layout or xml");
                } else {
                    throw new IllegalStateException("you have declear too much contents in xml");
                }
            } else {
                if (getChildCount() == 1) {
                    mHeaderView = getChildAt(0);
                    addView(mContentView);
                } else if (getChildCount() > 1) {
                    throw new IllegalStateException("you have declear contents in R.styleable.PullToRefreshLayout_header_layout," +
                            "should not declear content in xml");
                }
            }
        } else {
            if (mContentView == null) {
                if (getChildCount() == 1) {
                    mContentView = getChildAt(0);
                    addView(mHeaderView, 0);
                } else if (getChildCount() == 0) {
                    throw new IllegalStateException("you have not declear content in R.styleable.PullToRefreshLayout_header_layout or xml");
                } else {
                    throw new IllegalStateException("you have declear too much contents in xml");
                }
            } else {
                if (getChildCount() == 0) {
                    addView(mHeaderView);
                    addView(mContentView);
                } else {
                    throw new IllegalStateException("you have declear contents in R.styleable.PullToRefreshLayout_header_layout," +
                            "should not declear content in xml");
                }
            }
        }
        if (mHeaderView == null) {
            inflate(getContext(), R.layout.default_header_view, this);
            mHeaderView = findViewById(R.id.header);
            setHeaderUICallback((HeaderUICallback) mHeaderView);
        }

        if (mScrollId != 0) {
            View view = findViewById(mScrollId);
            if (view == null) {
                throw new IllegalStateException("can not find PullToRefreshLayout_scroll_id :" + mScrollId);
            } else {
                mScrollView = view;
            }
        }
    }

    /**
     * HeaderUICallback is used to adjust ui of header
     * @param callback
     */
    public void setHeaderUICallback(HeaderUICallback callback) {
        mHeaderUICallback = callback;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int currentOffset = mScrollHandler.getCurrentOffset();
        MarginLayoutParams clp = (MarginLayoutParams) mContentView.getLayoutParams();
        int left = getPaddingLeft() + clp.leftMargin;
        int top = getPaddingTop() + clp.topMargin;
        mContentView.layout(left, top + currentOffset, left + mContentView.getMeasuredWidth(), top + mContentView.getMeasuredHeight() + currentOffset);

        MarginLayoutParams hlp = (MarginLayoutParams) mHeaderView.getLayoutParams();
        int hLeft = getPaddingLeft() + hlp.leftMargin;
        int hBottom = 0 - hlp.bottomMargin;
        mHeaderView.layout(hLeft, hBottom - mHeaderView.getMeasuredHeight() + currentOffset, hLeft + mHeaderView
                .getMeasuredWidth(), hBottom + currentOffset);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        MarginLayoutParams clp = (MarginLayoutParams) mContentView.getLayoutParams();

        measureChildWithMargins(mContentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        int width = mContentView.getMeasuredWidth() + clp.leftMargin + clp.rightMargin + getPaddingLeft() + getPaddingRight();
        int height = mContentView.getMeasuredHeight() + clp.topMargin + clp.bottomMargin + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));

        measureChildWithMargins(mHeaderView, MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY)
                , 0,
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY), 0);
        MarginLayoutParams hlp = (MarginLayoutParams) mHeaderView.getLayoutParams();
        mScrollHandler.setRefreshPosition(mHeaderView.getMeasuredHeight() + hlp.topMargin + hlp.bottomMargin);
        PTFLog.t("onMeasure");
    }

    private boolean mLastDoSuper = true;
    private MotionEvent mLastEvent;
    private boolean mDispatchToScrollView = false;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        boolean doSuper = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollHandler.downAtY((int) ev.getY());
                Rect rect = new Rect();
                if (mScrollView != null && mScrollView.getVisibility() == VISIBLE && mScrollView.getGlobalVisibleRect
                        (rect) && rect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                    mDispatchToScrollView = true;
                } else {
                    mDispatchToScrollView = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                doSuper = !mScrollHandler.moveToY((int) ev.getY());
                if (mLastDoSuper) {
                    if (!doSuper) {
                        sendCancelEvent();
                    }
                } else {
                    if (doSuper) {
                        sendDownEvent();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mScrollHandler.upOrCancel();
                doSuper = mLastDoSuper;
                break;
        }
        mLastEvent = ev;
        mLastDoSuper = doSuper;

        if (doSuper) {
            super.dispatchTouchEvent(ev);
        }
        return true;
    }

    private void sendCancelEvent() {
        MotionEvent e = MotionEvent.obtain(mLastEvent.getDownTime(), mLastEvent.getEventTime(), MotionEvent
                .ACTION_CANCEL, mLastEvent.getX(), mLastEvent.getY(), mLastEvent.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        MotionEvent e = MotionEvent.obtain(mLastEvent.getDownTime(), mLastEvent.getEventTime(), MotionEvent
                .ACTION_DOWN, mLastEvent.getX(), mLastEvent.getY(), mLastEvent.getMetaState());
        super.dispatchTouchEvent(e);
    }

    @Override
    public void onOffsetChange(int offset) {
        mHeaderView.offsetTopAndBottom(offset);
        mContentView.offsetTopAndBottom(offset);
    }

    /**
     * @param callback
     */
    public void setRefreshCallback(RefreshCallback callback) {
        mRefreshCallback = callback;
    }

    @Override
    public void startRefresh() {
        if (mRefreshCallback != null) {
            mRefreshCallback.onRefresh();
        }
    }

    @Override
    public boolean canContentScrollUp() {
        if (mDispatchToScrollView) {
            return mScrollView.canScrollVertically(-1);
        } else {
            return mContentView.canScrollVertically(-1);
        }
    }

    @Override
    public void onScrollStateChanged(int newState) {
        if (mHeaderUICallback == null) {
            return;
        }
        switch (newState) {
            case ScrollHandler.STATE_ABOVE_REFRESH_LINE:
                mHeaderUICallback.onStatePullToRefresh();
                break;
            case ScrollHandler.STATE_BELOW_REFRESH_LINE:
                mHeaderUICallback.onStateReleaseToRefresh();
                break;
            case ScrollHandler.STATE_REFRESHING:
                mHeaderUICallback.onStateRefreshing();
                break;
            case ScrollHandler.STATE_COMPLETE:
                mHeaderUICallback.onStateComplete();
                break;
        }

    }

    /**
     * should call this method when refresh is completed.
     */
    public void refreshComplete() {
        mScrollHandler.setRefreshComplete();
    }

    /**
     * show refresh animation auto, without MotionEvent
     */
    public void autoRefresh() {
        mScrollHandler.autoRefresh();
    }

    /**
     * set Animation duration
     * @param timeInMillion
     */
    public void setScrollAnimationDuration(int timeInMillion) {
        mScrollHandler.setScrollAnimationDuration(timeInMillion);
    }

    /**
     * the refreshing cretical point, the ratio to the header height, default is 1, the same height as header
     * @param ratio
     */
    public void setRefreshingLine(float ratio) {
        mScrollHandler.setRefreshLineRatio(ratio);
    }


    /**
     * the coefficient of friction of pull to refresh action,
     * default is 0.5. You pull 200px, the view scroll 100px .
     * @param coefficientOfFriction
     */
    public void setCoefficientOfFriction(float coefficientOfFriction) {
        mScrollHandler.setFrictionRatio(coefficientOfFriction);
    }
}
