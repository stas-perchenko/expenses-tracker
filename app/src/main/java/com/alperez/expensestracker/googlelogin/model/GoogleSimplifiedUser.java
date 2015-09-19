package com.alperez.expensestracker.googlelogin.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by stanislav.perchenko on 19-Sep-15.
 */
public class GoogleSimplifiedUser {

    private String id;
    private String displayName;
    private String nickname;
    private String gender;
    private Name name;
    private String imageUrl;
    private String userUrl;

    public GoogleSimplifiedUser() {}

    public GoogleSimplifiedUser(String json) throws JSONException {
        this(new JSONObject(json));
    }

    public GoogleSimplifiedUser(JSONObject jUser) throws JSONException {
        this.id = jUser.optString("id", null);
        this.displayName = jUser.optString("displayName", null);
        this.nickname = jUser.optString("nickname", null);
        this.gender = jUser.optString("gender", null);
        if (jUser.has("image")) {
            this.imageUrl = jUser.getJSONObject("image").optString("url", null);
        }
        this.userUrl = jUser.optString("url", null);
        if (jUser.has("name")) {
            this.name = new Name(jUser.getJSONObject("name"));
        }
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
        jObj.put("id", id);
        jObj.put("displayName", this.displayName);
        jObj.put("nickname", this.nickname);
        jObj.put("gender", this.gender);
        if (this.imageUrl != null) {
            JSONObject jImage = new JSONObject();
            jImage.put("url", this.imageUrl);
            jObj.put("image", jImage);
        }
        jObj.put("url", this.userUrl);
        if (this.name != null) {
            jObj.put("name", this.name.toJSONObject());
        }
        return jObj;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Name getName() {
        return name;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUserUrl() {
        return userUrl;
    }

    public void setUserUrl(String userUrl) {
        this.userUrl = userUrl;
    }

    public static class Name {
        public String formatted;
        public String familyName;
        public String givenName;
        public String middleName;
        public String honorificPrefix;
        public String honorificSuffix;

        public Name() {}

        public Name(JSONObject jName) throws JSONException {
            this.formatted = jName.optString("formatted", null);
            this.familyName = jName.optString("familyName", null);
            this.givenName = jName.optString("givenName", null);
            this.middleName = jName.optString("middleName", null);
            this.honorificPrefix = jName.optString("honorificPrefix", null);
            this.honorificSuffix = jName.optString("honorificSuffix", null);
        }



        public JSONObject toJSONObject() throws JSONException {
            JSONObject jName = new JSONObject();
            jName.put("formatted", formatted);
            jName.put("familyName", familyName);
            jName.put("givenName", givenName);
            jName.put("middleName", middleName);
            jName.put("honorificPrefix", honorificPrefix);
            jName.put("honorificSuffix", honorificSuffix);
            return jName;
        }

    }
}
