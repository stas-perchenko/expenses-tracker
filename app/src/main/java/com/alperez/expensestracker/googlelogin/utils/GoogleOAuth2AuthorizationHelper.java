package com.alperez.expensestracker.googlelogin.utils;

import android.net.Uri;

import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;
import com.alperez.expensestracker.googlelogin.model.GoogleApiTokens;
import com.alperez.expensestracker.network.NetworkRequest;

/**
 * Created by stanislav.perchenko on 15-Sep-15.
 */
public class GoogleOAuth2AuthorizationHelper {
    public static final String AUTH_URL_PARAM_CLIENT_ID = "client_id";
    public static final String AUTH_URL_PARAM_REDIRECT_URI = "redirect_uri";
    public static final String AUTH_URL_PARAM_SCOPE = "scope";
    public static final String AUTH_URL_PARAM_EMAIL = "login_hint";
    public static final String AUTH_URL_PARAM_APPROVAL_PROMPT = "approval_prompt";
    public static final String AUTH_URL_PARAM_ACCESS_TYPE = "access_type";


    public static Uri buildAuthorizationUrl(GoogleAccountCredentials account) {

        Uri.Builder builder = Uri.parse(account.getOauth2RequestParams().getAuthUri()).buildUpon();
        builder.appendQueryParameter("response_type", "code");
        builder.appendQueryParameter(AUTH_URL_PARAM_CLIENT_ID, account.getOauth2RequestParams().getClientId());

        String redir = null;
        for (String url : account.getOauth2RequestParams().getRedirectUris()) {
            if (url.contains("localhost")) {
                redir = url; break;
            }
        }
        if (redir == null) throw new IllegalStateException("No valid Redirect URL provided for authorizartion");
        builder.appendQueryParameter(AUTH_URL_PARAM_REDIRECT_URI, redir);

        StringBuilder scope = new StringBuilder();
        for (String scp : account.getScopes()) {
            if (scope.length() > 0) scope.append(' ');
            scope.append(scp);
        }
        builder.appendQueryParameter(AUTH_URL_PARAM_SCOPE, scope.toString());

        builder.appendQueryParameter(AUTH_URL_PARAM_EMAIL, account.getAccountName());

        builder.appendQueryParameter(AUTH_URL_PARAM_APPROVAL_PROMPT, "force");

        builder.appendQueryParameter(AUTH_URL_PARAM_ACCESS_TYPE, "offline");

        return builder.build();
    }


    public static NetworkRequest addAccessTokenHeaderToRequest(NetworkRequest request, GoogleApiTokens tokens) throws AccessTokenExpiresException {
        if (request != null || tokens != null) {
            long expTime = tokens.getTimeAccessTokenObtainedAt() + tokens.getExpiresInSeconds()*1000;
            long curTime = System.currentTimeMillis();
            if ((expTime - 250) < curTime) {
                throw new AccessTokenExpiresException(expTime, curTime);
            }
            request.addHeader("Authorization", String.format("Bearer %s", tokens.getAccessToken()));
        }
        return request;
    }

}
