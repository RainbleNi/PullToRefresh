package com.pulltorefresh.rainbow.pull_to_refresh;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Scroller;

/**
 * Created by Nirui on 16/5/18.
 */
public class ScrollHandler implements Runnable {

    private int mInitPosition = 0;
    private int mRefreshingPosition;
    private int mRefreshCriticalPosition;
    private int mCurrentOffsetY;
    private int mLastPointY;
    private boolean mStartAction;
    private static final float FRICTION = 0.5f;
    private final ScrollHandlerCallback mScrollHandlerCallback;
    private final Handler mHandler;
    private boolean mIsRefreshing = false;


    private final Scroller mScroller;

    interface ScrollHandlerCallback {
        void onOffsetChange(int offset);
        void startRefresh();
    }

    public ScrollHandler(Context context, ScrollHandlerCallback listener) {
        mScroller = new Scroller(context);
        mScrollHandlerCallback = listener;
        mHandler = new Handler(Looper.getMainLooper());
    }

    void setRefreshPosition(int positon) {
        mRefreshingPosition = positon;
        mRefreshCriticalPosition = positon;
    }

    void downAtY(int y) {
        mLastPointY = y;
        mStartAction = true;
        mHandler.removeCallbacks(this);
    }

    void upOrCancel() {
        mStartAction = false;
        if (mIsRefreshing) {
            if (mCurrentOffsetY > mRefreshCriticalPosition) {
                scrollToRefreshPosition();
            }
        } else {
            if (mCurrentOffsetY < mRefreshCriticalPosition) {
                scrollToInitPosition();
            } else {
                mScrollHandlerCallback.startRefresh();
                mIsRefreshing = true;
                scrollToRefreshPosition();
            }
        }
    }

    /**
     *
     * @param y
     * @param canContentScrollUp judge if content can scroll up
     * @return if has consumed the move
     */
    boolean moveToY(int y, boolean canContentScrollUp) {
        if (mStartAction) {
            int ydiff = y - mLastPointY;
            mLastPointY = y;
            if (ydiff > 0) {
                if (canContentScrollUp) {
                    return false;
                }
            } else {
                if (mCurrentOffsetY == mInitPosition) {
                    PTFLog.d("mCurrentOffsetY == mInitPosition");
                    return false;
                }
            }
            int offsetDiff = (int) (ydiff * FRICTION);
            int newOffsetY = adjustOffset(mCurrentOffsetY + offsetDiff);
            mScrollHandlerCallback.onOffsetChange(newOffsetY - mCurrentOffsetY);
            mCurrentOffsetY = newOffsetY;
            PTFLog.d("mCurrentOffset:" + mCurrentOffsetY);
            return true;
        } else {
            return false;
        }
    }

    private int adjustOffset(int offset) {
        if (offset < mInitPosition) {
            offset = mInitPosition;
        }
        return offset;
    }

    private void scrollToInitPosition() {
        mScroller.startScroll(0, mCurrentOffsetY, 0, mInitPosition - mCurrentOffsetY);
        mHandler.removeCallbacks(this);
        mHandler.post(this);
    }

    private void scrollToRefreshPosition() {
        mScroller.startScroll(0, mCurrentOffsetY, 0, mRefreshingPosition - mCurrentOffsetY);
        mHandler.removeCallbacks(this);
        mHandler.post(this);
    }

    @Override
    public void run() {
        int newOffsetY;
        if (mScroller.isFinished() || !mScroller.computeScrollOffset()) {
            newOffsetY = mScroller.getFinalY();
        } else {
            newOffsetY = mScroller.getCurrY();
            mHandler.postDelayed(this, 30);
        }
        mScrollHandlerCallback.onOffsetChange(newOffsetY - mCurrentOffsetY);
        mCurrentOffsetY = newOffsetY;
        PTFLog.d("mCurrentOffset:" + mCurrentOffsetY);

    }

    public void setRefreshComplete() {
        mIsRefreshing = false;
        mStartAction = false;
        scrollToInitPosition();
    }

    public int getCurrentOffset() {
        return mCurrentOffsetY;
    }
}
