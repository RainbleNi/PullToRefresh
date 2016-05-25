package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout;

/**
 * Created by Nirui on 16/5/18.
 */
public class ListViewFragment extends Fragment implements PullToRefreshLayout.RefreshCallback{
    private PictureAdapter mAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    PullToRefreshLayout ptfLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.listview_layout, container, false);
        ptfLayout = (PullToRefreshLayout) root.findViewById(R.id.ptf_layout);
        ptfLayout.setRefreshCallback(this);
        ptfLayout.setScrollAnimationDuration(2000);
        final TextView headView = (TextView) root.findViewById(R.id.header);
        ptfLayout.setHeaderUICallback(new PullToRefreshLayout.HeaderUICallback() {
            @Override
            public void onStatePullToRefresh() {
                headView.setText("Pull to refresh");
            }

            @Override
            public void onStateReleaseToRefresh() {
                headView.setText("Release to refresh");
            }

            @Override
            public void onStateRefreshing() {
                headView.setText("In refreshing");
            }

            @Override
            public void onStateComplete() {
                headView.setText("Refresh completed");
            }
        });
        root.findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ptfLayout.autoRefresh();
            }
        });

        ListView listView = (ListView) root.findViewById(R.id.listview);
        mAdapter = new PictureAdapter(getActivity());
        mAdapter.setData(20);
        listView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isDetached()) {
                    return;
                }
                mAdapter.setData(20);
                ptfLayout.refreshComplete();
            }
        }, 3000);
    }


    class PictureAdapter extends BaseAdapter {
        final LayoutInflater mInflater;
        final Context mContext;
        final int mListHeight;
        private int mCount;


        PictureAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mContext = context;
            mListHeight = getResources().getDimensionPixelSize(R.dimen.listview_height);
        }

        public void setData(int count) {
            mCount = count;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = new TextView(mContext);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mListHeight);
                view.setGravity(Gravity.CENTER);
                view.setLayoutParams(params);
            } else {
                view = (TextView) convertView;
            }
            view.setText(String.format("No %d Page", position));
            return view;
        }
    }
}
