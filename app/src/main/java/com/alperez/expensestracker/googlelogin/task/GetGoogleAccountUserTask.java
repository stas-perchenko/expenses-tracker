package com.alperez.expensestracker.googlelogin.task;

import android.os.AsyncTask;
import android.os.Build;

import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;
import com.alperez.expensestracker.googlelogin.network.OAuthApis;
import com.alperez.expensestracker.network.NetworkErrorDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by stanislav.perchenko on 19-Sep-15.
 */
public class GetGoogleAccountUserTask extends AsyncTask<GoogleAccountCredentials, Void, Map<String, Object>> {
    public static final String RESULT_KEY_CREDENTIALS = "credentials";
    public static final String RESULT_KEY_ERROR_DESCRIPTOR = "error";

    @Override
    protected Map<String, Object> doInBackground(GoogleAccountCredentials... params) {
        NetworkErrorDescriptor err = OAuthApis.getGoogleAccountUser(params[0]);
        Map<String, Object> ret = new HashMap<>();
        if (err != null) {
            ret.put(RESULT_KEY_ERROR_DESCRIPTOR, err);
        }
        ret.put(RESULT_KEY_CREDENTIALS, params[0]);
        return ret;
    }

    public void safeExecute(GoogleAccountCredentials... params) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            super.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            super.execute(params);
        }
    }
}
