package com.alperez.expensestracker.googlelogin.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.alperez.expensestracker.googlelogin.model.AuthorizationState;
import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;

/**
 * Created by stanislav.perchenko on 15-Sep-15.
 */
public class AuthorizationWebViewClient extends WebViewClient {

    public interface OnAuthorizationWebViewDelegate {
        AuthorizationState getAuthState();
        void onComplete(GoogleAccountCredentials accountCredentials, String code, String error);
    }

    private GoogleAccountCredentials accountCredentials;
    private String mRedirect;
    private OnAuthorizationWebViewDelegate stateProvider;

    public AuthorizationWebViewClient(String redirectUrl, OnAuthorizationWebViewDelegate delegate) {
        mRedirect = redirectUrl;
        if (delegate == null) throw new IllegalArgumentException("Provider for authorization state must not be null");
        this.stateProvider = delegate;
    }

    public void setRedirectUrl(String redirectUrl) {
        mRedirect = redirectUrl;
    }

    public void setAccountCredentials(GoogleAccountCredentials accountCredentials) {
        this.accountCredentials = accountCredentials;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if (stateProvider.getAuthState() == AuthorizationState.AUTHORIZING) {
            if (TextUtils.isEmpty(this.mRedirect)) throw new IllegalStateException("Redirect URL is not provided for the WebView client");
            if (url.startsWith(mRedirect)) {
                Uri uri = Uri.parse(url);
                this.stateProvider.onComplete(this.accountCredentials, uri.getQueryParameter("code"), uri.getQueryParameter("error"));
            }
        }
    }
}
