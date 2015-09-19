package com.alperez.expensestracker.googlelogin.model;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class which holds code obtained as a result of authorization process.
 * This code can be exchanged on the the related access token
 * Time when the code was obtained is also hold.
 *
 * Created by stanislav.perchenko on 16-Sep-15.
 */
public class AuthorizationCode {

    private String code;
    private long timeObtainedMillis;

    public AuthorizationCode(String code, long time) {
        if (TextUtils.isEmpty(code)) throw new IllegalArgumentException("Valid Authorization code value must be provided");
        if (time <= 0) throw new IllegalArgumentException("Valid time value must be provided");
        this.code = code;
        this.timeObtainedMillis = time;
    }

    public AuthorizationCode(String json) throws JSONException {
        if (json == null) throw new IllegalArgumentException("Not-null JSON string representation must be provided");
        JSONObject jObj = new JSONObject(json);
        this.code = jObj.getString("code");
        this.timeObtainedMillis = jObj.getLong("time");
    }

    public String toJson() {
        try {
            return toJSONObject().toString();
        } catch(JSONException e) {
            return null;
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jObj = new JSONObject();
        jObj.put("code", code);
        jObj.put("time", timeObtainedMillis);
        return jObj;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getTimeObtainedMillis() {
        return timeObtainedMillis;
    }

    public void setTimeObtainedMillis(long timeObtainedMillis) {
        this.timeObtainedMillis = timeObtainedMillis;
    }
}
