package com.alperez.expensestracker.googlelogin.utils;

import android.os.Build;
import android.webkit.WebView;

/**
 * Created by stanislav.perchenko on 15-Sep-15.
 */
public class ViewUtils {

    public static void safelyResetWebView(WebView wv) {
        wv.stopLoading();
        if (Build.VERSION.SDK_INT >= 18) {
            wv.loadUrl("about:blank");
        } else {
            wv.clearView();
        }
    }

}
