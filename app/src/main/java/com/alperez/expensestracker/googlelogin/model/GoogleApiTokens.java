package com.alperez.expensestracker.googlelogin.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class which holds access- and refresh- tokens for Google play services APIs and the time they have been obtained
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class GoogleApiTokens {
    private String refreshToken;
    private String accessToken;
    private String tokenType;
    private int expiresInSeconds;
    private long timeAccessTokenObtainedAt;
    private long timeRefreshTokenObtainedAt; // Actually defines account login time


    /**
     * Creates an instance of this model from the cmplete JSON representation. Such as
     * {
     *      "access_token":"1/fFBGRNJru1FQd44AzqT3Zg",
     *      "expires_in":3920,
     *      "token_type":"Bearer"
     *      "refresh_token":"1/xEoDL4iW3cxlI7yDbSRFYNG01kVKM2C-259HOF2aQbI"
     *      "time_obtained_at": <long value, milliseconds>
     * }
     * @param jsonCompletePresentation
     */
    public GoogleApiTokens(@NonNull String jsonCompletePresentation) throws JSONException {
        if (jsonCompletePresentation == null) throw new IllegalArgumentException("JSON argument must not be null");
        JSONObject jObj = new JSONObject(jsonCompletePresentation);
        this.refreshToken = jObj.getString("refresh_token");
        this.accessToken = jObj.getString("access_token");
        this.tokenType = jObj.getString("token_type");
        this.expiresInSeconds = jObj.getInt("expires_in");
        this.timeRefreshTokenObtainedAt= jObj.getLong("time_refresh_token_obtained_at");
        this.timeAccessTokenObtainedAt = jObj.getLong("time_access_token_obtained_at");
        validateToken();
    }


    /**
     * Creates an instance of this model with the get token response JSON. Such as:
     * {
     *      "access_token": "1/fFBGRNJru1FQd44AzqT3Zg",
     *      "expires_in": 3920,
     *      "token_type": "Bearer"
     *      "refresh_token": "1/xEoDL4iW3cxlI7yDbSRFYNG01kVKM2C-259HOF2aQbI"
     * }
     * @param jsonInitialToken
     * @param timeObtainedAt time this response have been got
     */
    public GoogleApiTokens(@NonNull String jsonInitialToken, long timeObtainedAt) throws JSONException {
        if (jsonInitialToken == null) throw new IllegalArgumentException("JSON argument must not be null");
        JSONObject jObj = new JSONObject(jsonInitialToken);
        this.refreshToken = jObj.getString("refresh_token");
        this.accessToken = jObj.getString("access_token");
        this.tokenType = jObj.getString("token_type");
        this.expiresInSeconds = jObj.getInt("expires_in");
        this.timeRefreshTokenObtainedAt= timeObtainedAt;
        this.timeAccessTokenObtainedAt = timeObtainedAt;
        validateToken();
    }

    /**
     * Creates an instance of this model with valid JSON object
     * @param jTokens
     * @param timeObtainedAt
     * @throws JSONException
     */
    public GoogleApiTokens(@NonNull JSONObject jTokens, long timeObtainedAt) throws JSONException {
        if (jTokens == null) throw new IllegalArgumentException("JSON argument must not be null");
        this.refreshToken = jTokens.getString("refresh_token");
        this.accessToken = jTokens.getString("access_token");
        this.tokenType = jTokens.getString("token_type");
        this.expiresInSeconds = jTokens.getInt("expires_in");
        this.timeRefreshTokenObtainedAt= timeObtainedAt;
        this.timeAccessTokenObtainedAt = timeObtainedAt;
        validateToken();
    }


    /**
     * Apply response of refreshing access token for this model. JSON format must be
     * {
     *      "access_token": "1/fFBGRNJru1FQd44AzqT3Zg",
     *      "expires_in": 3920,
     *      "token_type": "Bearer"
     * }
     * @param jsonResponse
     * @param timeUpdatedAt time this response was updated
     */
    public void updateAccessToken(@NonNull String jsonResponse, long timeUpdatedAt) throws JSONException {
        if (jsonResponse == null) throw new IllegalArgumentException("JSON argument must not be null");
        JSONObject jObj = new JSONObject(jsonResponse);
        this.accessToken = jObj.getString("access_token");
        this.tokenType = jObj.getString("token_type");
        this.expiresInSeconds = jObj.getInt("expires_in");
        this.timeAccessTokenObtainedAt = timeUpdatedAt;
        validateToken();
    }

    /**
     * Creates full JSON representation of this instance. Such as
     * {
     *      "access_token":"1/fFBGRNJru1FQd44AzqT3Zg",
     *      "expires_in":3920,
     *      "token_type":"Bearer"
     *      "refresh_token":"1/xEoDL4iW3cxlI7yDbSRFYNG01kVKM2C-259HOF2aQbI"
     *      "time_obtained_at": <long value, milliseconds>
     * }
     * @return
     */
    public String toJson() {
        validateToken();
        try {
            JSONObject jObj = new JSONObject();
            jObj.put("refresh_token", this.refreshToken);
            jObj.put("access_token", this.accessToken);
            jObj.put("token_type", this.tokenType);
            jObj.put("expires_in", this.expiresInSeconds);
            jObj.put("time_refresh_token_obtained_at", this.timeRefreshTokenObtainedAt);
            jObj.put("time_access_token_obtained_at", this.timeAccessTokenObtainedAt);
            return jObj.toString();
        } catch(JSONException e) {
            return null;
        }
    }


    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresInSeconds() {
        return expiresInSeconds;
    }

    public long getTimeRefreshTokenObtainedAt() {
        return timeRefreshTokenObtainedAt;
    }

    public long getTimeAccessTokenObtainedAt() {
        return timeAccessTokenObtainedAt;
    }

    public boolean isExpired() {
        validateToken();
        return System.currentTimeMillis() >= (timeAccessTokenObtainedAt + expiresInSeconds*1000);
    }

    private void validateToken() {
        if (TextUtils.isEmpty(refreshToken)) throw new IllegalStateException("Refresh token for this instance is not set");
        if (TextUtils.isEmpty(accessToken)) throw new IllegalStateException("Access token for this instance is not set");
        if (TextUtils.isEmpty(tokenType)) throw new IllegalStateException("Token type for this instance is not set");
        if (expiresInSeconds <= 0) throw new IllegalStateException("Expiration parameter for access token of this instance is not set");
        if (timeRefreshTokenObtainedAt <= 0) throw new IllegalStateException("Obtaining time for refresh token is not set");
        if (timeAccessTokenObtainedAt <= 0) throw new IllegalStateException("Obtaining/refreshing time for access token is not set");
    }
}
