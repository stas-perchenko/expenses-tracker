package com.alperez.expensestracker.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by stanislav.perchenko on 12-Sep-15.
 */
public final class AppUtils {

    /**
     * Checks whether the device currently has a network connection.
     * @return true if the device has a network connection, false otherwise.
     */
    public static boolean isDeviceOnline(Context c) {
        NetworkInfo networkInfo = ((ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
