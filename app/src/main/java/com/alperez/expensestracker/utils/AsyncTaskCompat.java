package com.alperez.expensestracker.utils;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public abstract class AsyncTaskCompat<T, G, F> extends AsyncTask<T, G, F> {
    @SuppressLint("NewApi")
    public void safeExecute(T... params) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            super.execute(params);
        }
    }
}