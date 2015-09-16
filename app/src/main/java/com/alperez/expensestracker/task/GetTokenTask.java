package com.alperez.expensestracker.task;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.auth.GoogleAuthUtil;

/**
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class GetTokenTask extends AsyncTask<String, Void, GetTokenTaskResult> {
    private Context mContext;

    public GetTokenTask(Context context) {
        mContext = context;
    }

    @Override
    protected GetTokenTaskResult doInBackground(String... params) {
        try {
            String token = GoogleAuthUtil.getToken(mContext, params[0], params[1]);
            return new GetTokenTaskResult(token, null);
        } catch(Exception e) {
            return new GetTokenTaskResult(null, e);
        }
    }
}

