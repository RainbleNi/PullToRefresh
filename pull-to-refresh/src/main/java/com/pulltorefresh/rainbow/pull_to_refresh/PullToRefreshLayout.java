package com.pulltorefresh.rainbow.pull_to_refresh;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nirui on 16/5/17.
 */
public class PullToRefreshLayout extends ViewGroup implements ScrollHandler.ScrollHandlerCallback {
    private int mHeaderLayout;
    private int mContentLayout;
    private View mHeaderView;
    private View mContentView;
    private final ScrollHandler mScrollHandler;
    private final int mDefaultHeaderHeight;
    private RefreshCallback mRefreshCallback;
    private HeaderUICallback mHeaderUICallback;

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PullToRefreshLayout);
        mHeaderLayout = a.getResourceId(R.styleable.PullToRefreshLayout_header_layout, 0);
        mContentLayout = a.getResourceId(R.styleable.PullToRefreshLayout_content_layout, 0);
        LayoutInflater inflater = LayoutInflater.from(context);
        if (mHeaderLayout != 0) {
            mHeaderView = inflater.inflate(mHeaderLayout, this, false);
        }
        if (mContentLayout != 0) {
            mContentView = inflater.inflate(mContentLayout, this, false);
        }
        mScrollHandler = new ScrollHandler(context, this);
        mDefaultHeaderHeight = getResources().getDimensionPixelSize(R.dimen.default_header_height);
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
    }

    public void setHeaderView(View view) {
        if (view == mHeaderView) {
            return;
        }
        if (mHeaderView != null) {
            removeView(mHeaderView);
        }
        if (view != null) {
            LayoutParams lp = view.getLayoutParams();
            if (lp == null) {
                lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                view.setLayoutParams(lp);
            }
            addView(view);
        }
        mHeaderView = view;
    }

    public void setHeaderUICallback(HeaderUICallback callback) {
        mHeaderUICallback = callback;
    }

    public interface HeaderUICallback {
        public void onStatePullToRefresh();
        public void onStateReleaseToRefresh();
        public void onStateRefreshing();
        public void onStateComplete();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        PTFLog.d("onLayout");
        int currentOffset = mScrollHandler.getCurrentOffset();
        MarginLayoutParams clp = (MarginLayoutParams) mContentView.getLayoutParams();
        int left = getPaddingLeft() + clp.leftMargin;
        int top = getPaddingTop() + clp.topMargin;
        mContentView.layout(left, top + currentOffset, left + mContentView.getMeasuredWidth(), top + mContentView.getMeasuredHeight() + currentOffset);
        if (mHeaderView != null) {
            MarginLayoutParams hlp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            int hLeft = getPaddingLeft() + hlp.leftMargin;
            int hBottom = 0 - hlp.bottomMargin;
            mHeaderView.layout(hLeft, hBottom - mHeaderView.getMeasuredHeight() + currentOffset, hLeft + mHeaderView.getMeasuredWidth(), hBottom + currentOffset);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        MarginLayoutParams clp = (MarginLayoutParams) mContentView.getLayoutParams();
        /*int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int hightMode = MeasureSpec.getMode(heightMeasureSpec);
        int hightSize = MeasureSpec.getSize(heightMeasureSpec);*/


        measureChildWithMargins(mContentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
        int width = mContentView.getMeasuredWidth() + clp.leftMargin + clp.rightMargin + getPaddingLeft() + getPaddingRight();
        int height = mContentView.getMeasuredHeight() + clp.topMargin + clp.bottomMargin + getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(resolveSize(width, widthMeasureSpec), resolveSize(height, heightMeasureSpec));

        if (mHeaderView != null) {
                measureChildWithMargins(mHeaderView, MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY)
                    , 0,
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY), 0);
            MarginLayoutParams hlp = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mScrollHandler.setRefreshPosition(mHeaderView.getMeasuredHeight() + hlp.topMargin + hlp.bottomMargin);
        } else {
            mScrollHandler.setRefreshPosition(mDefaultHeaderHeight);
        }
    }

    private boolean mLastDoSuper = true;
    private MotionEvent mLastEvent;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        PTFLog.t("action:" + ev.getAction() + ", y:" + ev.getY());
        int action = ev.getAction();
        boolean doSuper = true;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mScrollHandler.downAtY((int) ev.getY());
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
            return super.dispatchTouchEvent(ev);
        } else {
            return true;
        }
    }

    private void sendCancelEvent() {
        PTFLog.d("sendCancelEvent");
        MotionEvent e = MotionEvent.obtain(mLastEvent.getDownTime(), mLastEvent.getEventTime(), MotionEvent
                .ACTION_CANCEL, mLastEvent.getX(), mLastEvent.getY(), mLastEvent.getMetaState());
        super.dispatchTouchEvent(e);
    }

    private void sendDownEvent() {
        PTFLog.d("sendDownEvent");
        MotionEvent e = MotionEvent.obtain(mLastEvent.getDownTime(), mLastEvent.getEventTime(), MotionEvent
                .ACTION_DOWN, mLastEvent.getX(), mLastEvent.getY(), mLastEvent.getMetaState());
        super.dispatchTouchEvent(e);
    }

    @Override
    public void onOffsetChange(int offset) {
        if (mHeaderView != null) {
            mHeaderView.offsetTopAndBottom(offset);
        }
        mContentView.offsetTopAndBottom(offset);
        PTFLog.d("content top:" + mContentView.getTop());
    }

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
        return mContentView.canScrollVertically(-1);
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

    public interface RefreshCallback {
        void onRefresh();
    }

    public void refreshComplete() {
        mScrollHandler.setRefreshComplete();
    }

    public void autoRefresh() {
        mScrollHandler.autoRefresh();
    }
}
