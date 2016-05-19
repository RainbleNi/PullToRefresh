package com.pulltorefresh.rainbow.pull_to_refresh;

import android.util.Log;

/**
 * Created by Nirui on 16/5/18.
 */
public class PTFLog {
    private final static boolean DEBUG = true;

    public static void d(String message) {
        if (DEBUG) {
            Log.d("pulltorefresh_log", message);
        }
    }
}
