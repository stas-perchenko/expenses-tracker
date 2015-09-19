package com.alperez.expensestracker.googlelogin.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;

/**
 * Created by stanislav.perchenko on 19-Sep-15.
 */
public class PreferencesUtils {
    private static final String GOOGLE_ACCOUNT_CREDS_STORAGE_SUFFIX = "_googleAccountsCreds";

    @SuppressLint("CommitPrefEdits")
    public static void saveGoogleAccountCredentials(Context context, GoogleAccountCredentials credentials) {
        SharedPreferences sPrefs = context.getSharedPreferences(context.getPackageName()+GOOGLE_ACCOUNT_CREDS_STORAGE_SUFFIX, Context.MODE_PRIVATE);
        sPrefs.edit().putString(credentials.getAccountName(), credentials.toJson()).commit();
    }

    /**
     *
     * @param context
     * @param googleAccountName user email
     */
    public static void removeGoogleAccountCredentials(Context context, String googleAccountName) {
        SharedPreferences sPrefs = context.getSharedPreferences(context.getPackageName()+GOOGLE_ACCOUNT_CREDS_STORAGE_SUFFIX, Context.MODE_PRIVATE);
        sPrefs.edit().remove(googleAccountName).commit();
    }
}
