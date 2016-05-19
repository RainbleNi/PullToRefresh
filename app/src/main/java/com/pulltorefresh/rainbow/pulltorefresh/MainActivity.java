package com.pulltorefresh.rainbow.pulltorefresh;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.container, Fragment.instantiate(this, MainFragment.class.getName()));
        transaction.commit();
    }
}
