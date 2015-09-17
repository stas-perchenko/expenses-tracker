package com.alperez.expensestracker.googlelogin.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Contains request params and access paths used to authorize with Google accounts. This model represents
 * JSON obtained from the Google Developer Console for an application. Such as:
 * {
 *      "client_id": "557190350054-5j8h306qm91fj31enjqg3lsmp82no9ea.apps.googleusercontent.com",
 *      "auth_uri": "https://accounts.google.com/o/oauth2/auth",
 *      "token_uri": "https://accounts.google.com/o/oauth2/token",
 *      "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
 *      "redirect_uris": ["urn:ietf:wg:oauth:2.0:oob",
 *      "http://localhost"]
 * }
 *
 *
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class OAuth2RequestParams {
    private String clientId;
    private String clientSecret;
    private String authUri;
    private String tokenUri;
    private String authProviderX509CertUrl;
    private String[] redirectUris;


    public OAuth2RequestParams(String json) throws JSONException {
        if (json == null) throw new IllegalArgumentException("JSON argument can not be null");

        JSONObject jObj = new JSONObject(json);
        this.clientId = jObj.getString("client_id");
        this.clientSecret = jObj.getString("client_secret");
        this.authUri = jObj.getString("auth_uri");
        this.tokenUri = jObj.getString("token_uri");
        this.authProviderX509CertUrl = jObj.getString("auth_provider_x509_cert_url");
        JSONArray jRedir = jObj.getJSONArray("redirect_uris");
        this.redirectUris = new String[jRedir.length()];
        for (int i=0; i<redirectUris.length; i++) {
            this.redirectUris[i] = jRedir.getString(i);
        }
    }

    public String toJson() throws JSONException {
        JSONObject jObj = new JSONObject();
        jObj.put("client_id", this.clientId);
        jObj.put("client_secret", this.clientSecret);
        jObj.put("auth_uri", this.authUri);
        jObj.put("token_uri", this.tokenUri);
        jObj.put("auth_provider_x509_cert_url", this.authProviderX509CertUrl);
        JSONArray jRedir = new JSONArray();
        for (String s : redirectUris) {
            jRedir.put(s);
        }
        jObj.put("redirect_uris", jRedir);
        return jObj.toString();
    }


    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAuthUri() {
        return authUri;
    }

    public String getTokenUri() {
        return tokenUri;
    }

    public String getAuthProviderX509CertUrl() {
        return authProviderX509CertUrl;
    }

    public String[] getRedirectUris() {
        return redirectUris;
    }

    private static final String LOCAL_URI_SIGN = "localhost";
    public String getLocalRedirectUri() {
        for (String s : redirectUris) {
            if (s.contains(LOCAL_URI_SIGN)) return s;
        }
        return "";
    }

}
