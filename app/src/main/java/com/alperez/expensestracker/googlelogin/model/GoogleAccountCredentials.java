package com.alperez.expensestracker.googlelogin.model;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class GoogleAccountCredentials {

    private String accountName;
    private OAuth2RequestParams oauth2RequestParams;
    private String[] scopes;
    private String authorizationCode;
    private GoogleApiTokens apiTokens;

    public GoogleAccountCredentials(String accountName, String oauth2RequestParamsJson, String[] scopes, String authorizationCode) {
        this.accountName = accountName;
        try {
            this.oauth2RequestParams = new OAuth2RequestParams(oauth2RequestParamsJson);
        } catch(JSONException e){
            Log.e(getClass().getSimpleName(), oauth2RequestParamsJson);
            e.printStackTrace();
        }
        this.scopes = scopes;
        this.authorizationCode = authorizationCode;
        validateInstance();
    }

    /**
     * Creates an instance of this class from complete JSON representation. Such as:
     * {
     *     "account_name": "someemail@gmail.com",
     *     "request_params": <JSON representation of the instance of OAuth2RequestParams class>,
     *     "scopes": ["scope1", "scope2", "scope3"],
     *     "authorizationCode": "4/nDliL8KdCS68z9YJfVwrco539h9bIBZtz7V3v6ugvcM"     - OPTIONAL
     *     "api_tokens": <JSON representation of the instance of GoogleApiTokens class - OPTIONAL>
     * }
     * @param jsonCompleteRepresentation
     */
    public GoogleAccountCredentials(String jsonCompleteRepresentation) {
        JSONObject jObj = null;

        try {
            jObj = new JSONObject(jsonCompleteRepresentation);
            this.accountName = jObj.getString("account_name");

            this.oauth2RequestParams = new OAuth2RequestParams(jObj.getJSONObject("request_params").toString());

            JSONArray jScopes = jObj.getJSONArray("scopes");
            this.scopes = new String[jScopes.length()];
            for (int i=0; i<scopes.length; i++) {
                scopes[i] = jScopes.getString(i);
            }

        } catch(JSONException e){}

        if (jObj != null) {
            JSONObject jTokens = jObj.optJSONObject("api_tokens");
            if (jTokens != null) {
                try {
                    this.apiTokens = new GoogleApiTokens(jTokens.toString());
                } catch(JSONException e) {
                    throw new IllegalArgumentException("GoogleApiTokens json can not be parsed");
                }
            }
            this.authorizationCode = jObj.optString("authorizationCode");
        }

        validateInstance();
    }

    public String toJson() {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put("account_name", this.accountName);
            jObj.put("request_params", this.oauth2RequestParams.toJson());
            JSONArray jScopes = new JSONArray();
            for (String scope : this.scopes) {
                jScopes.put(scope);
            }
            jObj.put("scopes", jScopes);
            if (this.apiTokens != null) {
                jObj.put("api_tokens", this.apiTokens.toJson());
            }
            jObj.put("authorizationCode", this.authorizationCode);
            return jObj.toString();
        } catch(JSONException e) {
            return null;
        }
    }

    public boolean isAuthorized() {
        return !TextUtils.isEmpty(this.authorizationCode);
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public void setApiTokens(GoogleApiTokens apiTokens) {
        this.apiTokens = apiTokens;
    }

    public String getAccountName() {
        return accountName;
    }

    public OAuth2RequestParams getOauth2RequestParams() {
        return oauth2RequestParams;
    }

    public String[] getScopes() {
        return scopes;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public GoogleApiTokens getApiTokens() {
        return apiTokens;
    }

    public void validateInstance() {
        if (TextUtils.isEmpty(this.accountName)) throw new IllegalStateException("Account name is not set");
        if (this.oauth2RequestParams == null) throw new IllegalStateException("OAuth2 request parameters are not set");
        if (scopes == null || scopes.length == 0) throw new IllegalStateException("Scopes are not provided");
    }
}
