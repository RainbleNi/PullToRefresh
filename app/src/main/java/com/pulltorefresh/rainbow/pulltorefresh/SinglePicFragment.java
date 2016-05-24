package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pulltorefresh.rainbow.pull_to_refresh.PullToRefreshLayout;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Nirui on 16/5/23.
 */
public class SinglePicFragment extends Fragment implements PullToRefreshLayout.RefreshCallback{
    private ImageView mImageView;
    private PullToRefreshLayout mPTFLayout;
    public static final String IMAGE_RESID = "image_resid";
    private Executor mExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.single_pic_layout, container, false);
        mImageView = (ImageView) view.findViewById(R.id.image);
        mPTFLayout = (PullToRefreshLayout) view.findViewById(R.id.ptf_layout);
        mPTFLayout.setRefreshCallback(this);
        mPTFLayout.startRefresh();
        return view;
    }


    @Override
    public void onRefresh() {
        final int id = getArguments().getInt(IMAGE_RESID);
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                return BitmapFactory.decodeResource(getResources(), id);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                mImageView.setImageBitmap(bitmap);
                mPTFLayout.refreshComplete();
            }
        }.executeOnExecutor(mExecutor);
    }
}
