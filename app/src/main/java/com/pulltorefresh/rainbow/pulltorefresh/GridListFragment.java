package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout;

/**
 * Created by Nirui on 16/5/18.
 */
public class GridListFragment extends Fragment implements PullToRefreshLayout.RefreshCallback{
    private PictureAdapter mAdapter;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final int[] PIC_RES = new int[] {
            R.mipmap.pic1,
            R.mipmap.pic2,
            R.mipmap.pic3,
            R.mipmap.pic4,
            R.mipmap.pic5,
            R.mipmap.pic6,
            R.mipmap.pic7,
            R.mipmap.pic8,
            R.mipmap.pic9,
            R.mipmap.pic10,
            R.mipmap.pic11,
    };
    PullToRefreshLayout ptfLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.gridview_layout, container, false);
        ptfLayout = (PullToRefreshLayout) root.findViewById(R.id.ptf_layout);
        ptfLayout.setRefreshCallback(this);
        GridView gridView = (GridView) root.findViewById(R.id.gridview);
        mAdapter = new PictureAdapter(getActivity());
        mAdapter.setData(PIC_RES);
        gridView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onRefresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter.setData(PIC_RES);
                ptfLayout.refreshComplete();
            }
        }, 3000);
    }


    class PictureAdapter extends BaseAdapter {
        private int[] mPicRes;
        final LayoutInflater mInflater;
        final Context mContext;
        final int mGridWidth;


        PictureAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mContext = context;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mGridWidth = (metrics.widthPixels - getResources().getDimensionPixelSize(R.dimen.space_left)) / 2;
        }

        public void setData(int[] picRes) {
            mPicRes = picRes;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mPicRes == null ? 0 : mPicRes.length;
        }

        @Override
        public Object getItem(int position) {
            return mPicRes[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new ImageView(mContext);
                AbsListView.LayoutParams params = new AbsListView.LayoutParams(mGridWidth, mGridWidth);
                convertView.setPadding(10, 10, 10, 10);
                convertView.setLayoutParams(params);
            }
            ((ImageView) convertView).setImageResource(mPicRes[position]);
            return convertView;
        }
    }
}
