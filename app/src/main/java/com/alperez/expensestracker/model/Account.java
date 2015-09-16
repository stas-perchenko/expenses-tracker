package com.alperez.expensestracker.model;

import android.text.TextUtils;

import com.alperez.expensestracker.utils.DateUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by stanislav.perchenko on 12-Sep-15.
 */
public class Account {
    private String accountName;
    private long accountId;
    private String accessToken;
    private Date timeLoggedIn;
    private Date lastTimeTokenUpdated;
    private String dbName;
    private long driveDbVersion;
    private String displayName;

    public boolean isValid() {
        return !TextUtils.isEmpty(accountName) && accountId > 0 && !TextUtils.isEmpty(displayName);
    }

    public boolean isConnected() {
        return isValid() && !TextUtils.isEmpty(accessToken);
    }

    public Account() {}

    public Account(String json) throws JSONException, ParseException {
        fromJson(json);
    }


    public String toJson() {
        try {
            JSONObject jObj = new JSONObject();
            jObj.put("accountName", accountName);
            jObj.put("accountId", accountId);
            jObj.put("accessToken", accessToken);
            jObj.put("timeLoggedIn", (timeLoggedIn != null) ? DateUtils.formatSqlDate(timeLoggedIn) : null);
            jObj.put("lastTimeTokenUpdated", (lastTimeTokenUpdated != null) ? DateUtils.formatSqlDate(lastTimeTokenUpdated) : null);
            jObj.put("dbName", dbName);
            jObj.put("driveDbVersion", driveDbVersion);
            jObj.put("displayName", displayName);
            return jObj.toString();
        } catch(JSONException e) {
            return null;
        }
    }

    public void fromJson(String json) throws JSONException, ParseException {
        if (json == null) throw new JSONException("JSON string argument is null");
        JSONObject jObj = new JSONObject(json);
        this.accountName = jObj.optString("accountName", null);
        this.accountId = jObj.getLong("accountId");
        this.accessToken = jObj.optString("accessToken", null);
        this.timeLoggedIn = (accessToken != null) ?  DateUtils.parseSqlDate(jObj.optString("timeLoggedIn"))  : null;
        this.lastTimeTokenUpdated = (accessToken != null) ?  DateUtils.parseSqlDate(jObj.optString("lastTimeTokenUpdated"))  : null;
        this.dbName = jObj.optString("dbName", null);
        this.driveDbVersion = jObj.getLong("driveDbVersion");
        this.displayName = jObj.optString("displayName");
    }


    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Date getTimeLoggedIn() {
        return timeLoggedIn;
    }

    public void setTimeLoggedIn(Date timeLoggedIn) {
        this.timeLoggedIn = timeLoggedIn;
    }

    public Date getLastTimeTokenUpdated() {
        return lastTimeTokenUpdated;
    }

    public void setLastTimeTokenUpdated(Date lastTimeTokenUpdated) {
        this.lastTimeTokenUpdated = lastTimeTokenUpdated;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public long getDriveDbVersion() {
        return driveDbVersion;
    }

    public void setDriveDbVersion(long driveDbVersion) {
        this.driveDbVersion = driveDbVersion;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
