package com.pulltorefresh.rainbow.pull_to_refresh;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Nirui on 16/5/23.
 */
public class DefaultHeaderView extends RelativeLayout implements PullToRefreshLayout.HeaderUICallback {
    private ImageView mImageView;
    private TextView mTextView;

    public DefaultHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inflate(context, R.layout.default_header_layout, this);
        mImageView = (ImageView) findViewById(R.id.image);
        mTextView = (TextView) findViewById(R.id.text);
    }

    @Override
    public void onStatePullToRefresh() {
        mTextView.setText(R.string.pull_to_refresh);
        mImageView.setImageResource(R.drawable.arrow_down);
    }

    @Override
    public void onStateReleaseToRefresh() {
        mTextView.setText(R.string.release_to_refresh);
        mImageView.setImageResource(R.drawable.arrow_up);
    }

    @Override
    public void onStateRefreshing() {
        mTextView.setText(R.string.refreshing);
        mImageView.setImageResource(R.drawable.progress);
    }

    @Override
    public void onStateComplete() {
        mTextView.setText(R.string.refresh_complete);
        mImageView.setImageResource(R.drawable.complete);
    }
}
