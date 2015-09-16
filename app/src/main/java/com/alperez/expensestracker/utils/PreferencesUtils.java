package com.alperez.expensestracker.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.alperez.expensestracker.model.Account;
import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;

import org.json.JSONException;

import java.text.ParseException;

/**
 * Created by stanislav.perchenko on 12-Sep-15.
 */
public final class PreferencesUtils {
    private static final String KEY_CONNECTED_ACCOUNT = "connected_account";
    private static final String GOOGLE_ACCOUNT_CREDS_STORAGE_SUFFIX = "_googleAccountsCreds";

    public static Account loadConnectedAccount(Context context) {
        try {
            return new Account(context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE).getString(KEY_CONNECTED_ACCOUNT, null));
        } catch(ParseException e) {
            e.printStackTrace();
        } catch(JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     *
     * @param context
     * @param acc instance of account to be updated with or to be initially saved.
     * @throws IllegalAccountInstanceException when there is an account already in the preferences and it's being tried
     * to be replaced with somem otfher account
     */
    public static void updateConnectedAccount(Context context, Account acc) {
        SharedPreferences sPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            Account savedAcc = new Account(sPrefs.getString(KEY_CONNECTED_ACCOUNT, null));
            if (savedAcc.getAccountId() != acc.getAccountId()) {
                throw new IllegalAccountInstanceException(acc, savedAcc.getAccountName(), "Tying to update already saved account with another one");
            }
        } catch(Exception e){}
        sPrefs.edit().putString(KEY_CONNECTED_ACCOUNT, acc.toJson()).commit();
    }


    /**
     *
     * @param context
     * @param acc An instance of the same account which is intended to be deleted from preferences
     */
    public static void removeConnectedAccount(Context context, Account acc) {
        SharedPreferences sPrefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        try {
            Account savedAcc = new Account(sPrefs.getString(KEY_CONNECTED_ACCOUNT, null));
            if (savedAcc.getAccountId() != acc.getAccountId()) {
                throw new IllegalAccountInstanceException(acc, savedAcc.getAccountName(), "You must provide an instance of the same account you are going to delete");
            }
        } catch(Exception e){}
        sPrefs.edit().remove(KEY_CONNECTED_ACCOUNT).commit();
    }

    public static void saveGoogleAccountCredentials(Context context, GoogleAccountCredentials credentials) {
        SharedPreferences sPrefs = context.getSharedPreferences(context.getPackageName()+GOOGLE_ACCOUNT_CREDS_STORAGE_SUFFIX, Context.MODE_PRIVATE);
        sPrefs.edit().putString(credentials.getAccountName(), credentials.toJson()).commit();
    }



    public static final class IllegalAccountInstanceException extends IllegalArgumentException {
        private Account rejectedAccount;
        private String expectedAccountName;
        public IllegalAccountInstanceException(Account rejectedAccount, String expectedAccountName, String message) {
            super(message);
            this.rejectedAccount = rejectedAccount;
            this.expectedAccountName = expectedAccountName;
        }

        public Account getRejectedAccount() {
            return rejectedAccount;
        }

        public String getExpectedAccountName() {
            return expectedAccountName;
        }

        private String cachedMessage;

        @Override
        public String getMessage() {
            if (cachedMessage == null) {
                StringBuilder sb = new StringBuilder(super.getMessage());
                sb.append("{ rejected account - ");
                sb.append((rejectedAccount != null) ? rejectedAccount.getAccountName() : "null");
                sb.append("; expected account - ");
                sb.append(expectedAccountName);
                sb.append(" }");
                cachedMessage = sb.toString();
            }
            return cachedMessage;
        }
    }
}
