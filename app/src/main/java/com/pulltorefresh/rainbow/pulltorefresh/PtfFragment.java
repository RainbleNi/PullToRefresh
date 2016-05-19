package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Nirui on 16/5/17.
 */
public class PtfFragment extends Fragment {
    public static final String LAYOUT_ID = "layout_id";
    private int mLayoutId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mLayoutId = bundle.getInt(LAYOUT_ID);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(mLayoutId, container, false);
        return view;
    }
}
