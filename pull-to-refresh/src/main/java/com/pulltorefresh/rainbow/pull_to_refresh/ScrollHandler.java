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


    private final Scroller mScroller;

    interface ScrollHandlerCallback {
        void onOffsetChange(int offset);
        void startRefresh();
        boolean canContentScrollUp();
        void onScrollStateChanged(int newState);
    }

    private int mScrollState = STATE_ABOVE_REFRESH_LINE;
    public static final int STATE_ABOVE_REFRESH_LINE = 0;
    public static final int STATE_BELOW_REFRESH_LINE = 1;
    public static final int STATE_REFRESHING = 2;
    public static final int STATE_COMPLETE = 3;

    public ScrollHandler(Context context, ScrollHandlerCallback scrollCallback) {
        mScroller = new Scroller(context);
        mScrollHandlerCallback = scrollCallback;
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
        switch (mScrollState) {
            case STATE_REFRESHING:
                if (mCurrentOffsetY > mRefreshCriticalPosition) {
                    scrollToRefreshPosition();
                }
                break;
            case STATE_COMPLETE:
                scrollToInitPosition();
                break;
            case STATE_ABOVE_REFRESH_LINE:
                scrollToInitPosition();
                break;
            case STATE_BELOW_REFRESH_LINE:
                setState(STATE_REFRESHING);
                mScrollHandlerCallback.startRefresh();
                scrollToRefreshPosition();
                break;
        }
    }

    /**
     *
     * @param y
     * @return if has consumed the move
     */
    boolean moveToY(int y) {
        if (mStartAction) {
            int ydiff = y - mLastPointY;
            mLastPointY = y;
            if (ydiff > 0) {
                if (mScrollHandlerCallback.canContentScrollUp()) {
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
            switch (mScrollState) {
                case STATE_COMPLETE:
                    if (mCurrentOffsetY == 0) {
                        if (newOffsetY >= mRefreshCriticalPosition) {
                            setState(STATE_BELOW_REFRESH_LINE);
                        } else {
                            setState(STATE_ABOVE_REFRESH_LINE);
                        }
                    }
                    break;
                case STATE_BELOW_REFRESH_LINE:
                    if (newOffsetY < mRefreshCriticalPosition) {
                        setState(STATE_ABOVE_REFRESH_LINE);
                    }
                    break;
                case STATE_ABOVE_REFRESH_LINE:
                    if (newOffsetY >= mRefreshCriticalPosition) {
                        setState(STATE_BELOW_REFRESH_LINE);
                    }
                    break;
                default:
                    break;
            }
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
        if (mCurrentOffsetY == mInitPosition) {
            return;
        }
        mScroller.startScroll(0, mCurrentOffsetY, 0, mInitPosition - mCurrentOffsetY, 2000);
        mHandler.removeCallbacks(this);
        mHandler.post(this);
    }

    private void scrollToRefreshPosition() {
        mScroller.startScroll(0, mCurrentOffsetY, 0, mRefreshingPosition - mCurrentOffsetY, 2000);
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
        mStartAction = false;
        setState(STATE_COMPLETE);
        scrollToInitPosition();
    }

    public int getCurrentOffset() {
        return mCurrentOffsetY;
    }

    private void setState(int state) {
        if (mScrollState != state) {
            mScrollState = state;
            mScrollHandlerCallback.onScrollStateChanged(mScrollState);
        } else {
            throw new IllegalStateException("old state:" + mScrollState + ", newState:" + state);
        }
    }
}
