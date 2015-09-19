package com.alperez.expensestracker.googlelogin.network;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;
import com.alperez.expensestracker.googlelogin.model.GoogleApiTokens;
import com.alperez.expensestracker.googlelogin.model.GoogleSimplifiedUser;
import com.alperez.expensestracker.network.Network;
import com.alperez.expensestracker.network.NetworkErrorDescriptor;
import com.alperez.expensestracker.network.NetworkRequest;
import com.alperez.expensestracker.network.NetworkResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stanislav.perchenko on 19-Sep-15.
 */
public class OAuthApis {
    private static final String TAG = "OAuthApis";












    /**********************************************************************************************/
    /**************************************  Static APIs methods  *********************************/
    /**********************************************************************************************/

    private static final Object lockerGetAllNewTokensForGoogleAccount = new Object();
    public static NetworkErrorDescriptor getAllNewTokensForGoogleAccount(@NonNull GoogleAccountCredentials accountCreds) {
        if (accountCreds == null) {
            throw new IllegalArgumentException("valid GoogleAccountCredentials instance must be provided");
        } else if (!accountCreds.isAuthorized()) {
            throw new IllegalArgumentException("an instance of GoogleAccountCredentials must be authorized with google account first");
        }


        synchronized (lockerGetAllNewTokensForGoogleAccount) {
            Map<String, String> headers = new HashMap<>();
            headers.put("content-type", "application/x-www-form-urlencoded");
            headers.put("user-agent", "expensestracker-1066");

            Map<String, String> params = new HashMap<>();
            params.put("code", accountCreds.getAuthorizationCode().getCode());
            params.put("client_id", accountCreds.getOauth2RequestParams().getClientId());
            params.put("client_secret", accountCreds.getOauth2RequestParams().getClientSecret());
            params.put("redirect_uri", accountCreds.getOauth2RequestParams().getLocalRedirectUri());
            params.put("grant_type", "authorization_code");

            NetworkRequest nr = new NetworkRequest(accountCreds.getOauth2RequestParams().getTokenUri(), params, NetworkRequest.Method.POST, headers);
            try {
                //----  Mating the request  ----
                NetworkResponse nResp = Network.doRequest(nr);

                //----  Getting response data  ----
                int respCode = nResp.getResponseCode();
                boolean isOk = nResp.isRequestOk();
                String respData = null;
                JSONObject jResp = null;
                try {
                    respData = (isOk) ? nResp.readResponseData() : nResp.readResponseError();
                    jResp = new JSONObject(respData);
                } catch (JSONException e) {
                    Log.e(TAG, "Error performing network request. Bad JSON format of response - " + respData);
                    e.printStackTrace();
                    NetworkErrorDescriptor err = new NetworkErrorDescriptor(nr);
                    err.responseCode = respCode;
                    err.rawResponse = respData;
                    err.error = "JSONException: " +e.getMessage();
                    err.errorDescription = e.toString();
                    return err;
                } finally {
                    nResp.release();
                }


                /* Error response example (code 400)
                * {
                *   "error_description": "Code was already redeemed.",
                *   "error": "invalid_grant"
                * }*/

                /* Goog response example
                * {
                *   "access_token":"1/fFAGRNJru1FTz70BzhT3Zg",
                *   "expires_in":3920,
                *   "token_type":"Bearer",
                *   "refresh_token":"1/xEoDL4iW3cxlI7yDbSRFYNG01kVKM2C-259HOF2aQbI"
                * }*/


                if (isOk) {
                    Log.i(TAG, "Server returned OK response. Code="+respCode+",  data: "+respData);
                    try {
                        GoogleApiTokens tokens = new GoogleApiTokens(jResp, System.currentTimeMillis());
                        accountCreds.setApiTokens(tokens); //Set the result!!!
                        return null;
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON format is wrong: "+e.getMessage());
                        NetworkErrorDescriptor err = new NetworkErrorDescriptor(nr);
                        err.responseCode = respCode;
                        err.rawResponse = respData;
                        err.error = "JSONExeption: "+e.getMessage();
                        err.errorDescription = e.toString();
                        return err;
                    }

                } else {
                    Log.e(TAG, "Server returned error. Resp code="+respCode+",  data: "+respData);
                    NetworkErrorDescriptor err = new NetworkErrorDescriptor(nr);
                    err.responseCode = respCode;
                    err.rawResponse = respData;
                    err.error = jResp.optString("error");
                    err.errorDescription = jResp.optString("error_description");
                    return err;
                }
            } catch(IOException e) {
                Log.e(TAG, "Error performing network request. Method="+nr.getMethod().name()+", URL="+nr.getUriForRequest());
                e.printStackTrace();

                NetworkErrorDescriptor err = new NetworkErrorDescriptor(nr);
                err.error = "IOException: " +e.getMessage();
                err.errorDescription = e.toString();
                return err;
            }
        }
    }


    /**
     *
     * @param accountCreds    the progided credentials instance must have valid Access Token
     * @return
     */
    public static NetworkErrorDescriptor getGoogleAccountUser(@NonNull GoogleAccountCredentials accountCreds) {
        NetworkErrorDescriptor err = null;
        if (accountCreds.getApiTokens() == null) {
            err = new NetworkErrorDescriptor(null);
            err.error = "not authorized";
            err.errorDescription = "You must get access token before calling PEOPLE API";
            return err;
        } else if ((accountCreds.getApiTokens().getTimeAccessTokenObtainedAt() + accountCreds.getApiTokens().getExpiresInSeconds()*1000 - 250) > System.currentTimeMillis()) {
            err = new NetworkErrorDescriptor(null);
            err.error = "expired";
            err.errorDescription = "Your access token has been expired";
        }
        if (err != null) return err;

        synchronized (lockerGetGoogleAccountUser) {
            NetworkRequest nr = new NetworkRequest(accountCreds.getOauth2RequestParams().getPeopleUri(), new HashMap<String, String>(), NetworkRequest.Method.GET, new HashMap<String, String>());
            try {
                //----  Mating the request  ----
                NetworkResponse nResp = Network.doRequest(nr);

                //----  Getting response data  ----
                int respCode = nResp.getResponseCode();
                boolean isOk = nResp.isRequestOk();
                String respData = null;
                JSONObject jResp = null;
                try {
                    respData = (isOk) ? nResp.readResponseData() : nResp.readResponseError();
                    jResp = new JSONObject(respData);
                } catch (JSONException e) {
                    Log.e(TAG, "Error performing network request. Bad JSON format of response - " + respData);
                    e.printStackTrace();
                    err = new NetworkErrorDescriptor(nr);
                    err.responseCode = respCode;
                    err.rawResponse = respData;
                    err.error = "JSONException: " +e.getMessage();
                    err.errorDescription = e.toString();
                    return err;
                } finally {
                    nResp.release();
                }

                if (isOk) {
                    Log.i(TAG, "Server returned OK response. Code="+respCode+",  data: "+respData);
                    try {
                        GoogleSimplifiedUser user = new GoogleSimplifiedUser(jResp);
                        accountCreds.setGoogleAccountUser(user); //Set the result!!!
                        return null;
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON format is wrong: "+e.getMessage());
                        err = new NetworkErrorDescriptor(nr);
                        err.responseCode = respCode;
                        err.rawResponse = respData;
                        err.error = "JSONExeption: "+e.getMessage();
                        err.errorDescription = e.toString();
                        return err;
                    }

                } else {
                    Log.e(TAG, "Server returned error. Resp code="+respCode+",  data: "+respData);
                    err = new NetworkErrorDescriptor(nr);
                    err.responseCode = respCode;
                    err.rawResponse = respData;
                    err.error = jResp.optString("error");
                    err.errorDescription = jResp.optString("error_description");
                    return err;
                }

            } catch(IOException e) {
                Log.e(TAG, "Error performing network request. Method="+nr.getMethod().name()+", URL="+nr.getUriForRequest());
                e.printStackTrace();

                err = new NetworkErrorDescriptor(nr);
                err.error = "IOException: " +e.getMessage();
                err.errorDescription = e.toString();
                return err;
            }
        }
    }
    private static final Object lockerGetGoogleAccountUser = new Object();
}
