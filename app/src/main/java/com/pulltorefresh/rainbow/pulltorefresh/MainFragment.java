package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Created by Nirui on 16/5/17.
 */
public class MainFragment extends Fragment implements AdapterView.OnItemClickListener{

    private int[] sTitles = new int[] {
            R.string.case1,
            R.string.case2,
    };



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_layout, container, false);
        GridView gridView = (GridView) view.findViewById(R.id.gridview);
        gridView.setAdapter(new GridAdapter(getActivity(), sTitles));
        gridView.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                replaceFragment(GridListFragment.class);
                break;
            case 1:
                replaceFragment(ListViewFragment.class);
                break;
            case 2:
                replacePftFragment(R.layout.ptf_layout_wrap_content);
                break;
            default:
                break;
        }
    }

    private void replaceFragment(Class<? extends Fragment> c) {
        getFragmentManager().beginTransaction().replace(R.id.container, Fragment.instantiate(getActivity(), c.getName(),
                null)).addToBackStack(null).commit();
    }

    public void replacePftFragment(int layoutId) {
        Bundle bundle = new Bundle();
        bundle.putInt(PtfFragment.LAYOUT_ID, layoutId);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.container, Fragment.instantiate(getActivity(), PtfFragment.class.getName(), bundle));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public class GridAdapter extends BaseAdapter {
        private final int[] mTitles;
        private final LayoutInflater mLayoutInflater;
        public GridAdapter(Context context, int[] titles) {
            mTitles = titles;
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }

        @Override
        public Object getItem(int position) {
            return mTitles[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View root;
            if (convertView == null) {
                root = mLayoutInflater.inflate(R.layout.textview_layout, parent, false);
            } else {
                root = convertView;
            }
            ((TextView)root.findViewById(R.id.text)).setText(mTitles[position]);
            return root;
        }
    }
}
