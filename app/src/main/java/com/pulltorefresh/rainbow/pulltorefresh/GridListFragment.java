package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Nirui on 16/5/18.
 */
public class GridListFragment extends Fragment implements PullToRefreshLayout.RefreshCallback, AdapterView.OnItemClickListener{
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
    PullToRefreshLayout mPtfLayout;
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.gridview_layout, container, false);
        mPtfLayout = (PullToRefreshLayout) root.findViewById(R.id.ptf_layout);
        mPtfLayout.setRefreshCallback(this);
        GridView gridView = (GridView) root.findViewById(R.id.gridview);
        gridView.setOnItemClickListener(this);
        mAdapter = new PictureAdapter(getActivity());
        gridView.setAdapter(mAdapter);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mPtfLayout.autoRefresh();
            }
        }, 500);
        return root;
    }

    @Override
    public void onRefresh() {
        if (!isAdded()) {
            return;
        }
        final Resources resources = getResources();
        new AsyncTask<Void, Void, Bitmap[]>() {

            @Override
            protected Bitmap[] doInBackground(Void ... params) {
                Bitmap[] bp = new Bitmap[PIC_RES.length];
                long time = System.currentTimeMillis();
                int index = 0;
                for (int id : PIC_RES) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeResource(resources, id, options);
                    int inSampleSize = options.outWidth / mAdapter.mGridWidth;
                    options.inSampleSize = inSampleSize;
                    options.inJustDecodeBounds = false;
                    bp[index++] = BitmapFactory.decodeResource(resources, id, options);
                }
                long timePass = (System.currentTimeMillis() - time);
                if (timePass < 1000) {
                    try {
                        Thread.sleep(1000 - timePass);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return bp;
            }

            @Override
            protected void onPostExecute(Bitmap[] result) {
                if (isDetached()) {
                    return;
                }
                mAdapter.setData(result);
                mPtfLayout.refreshComplete();
            }
        }.executeOnExecutor(mExecutor);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Bundle bundle = new Bundle();
        bundle.putInt(SinglePicFragment.IMAGE_RESID, PIC_RES[position]);
        getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(getActivity(),
                SinglePicFragment.class.getName(), bundle)).addToBackStack(null).commit();

    }


    class PictureAdapter extends BaseAdapter {
        private Bitmap[] mPicRes;
        final LayoutInflater mInflater;
        final Context mContext;
        final int mGridWidth;


        PictureAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mContext = context;
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            mGridWidth = (metrics.widthPixels - getResources().getDimensionPixelSize(R.dimen.space_left)) / 2;
        }

        public void setData(Bitmap[] pics) {
            mPicRes = pics;
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
            ((ImageView) convertView).setImageBitmap(mPicRes[position]);
            return convertView;
        }
    }
}
