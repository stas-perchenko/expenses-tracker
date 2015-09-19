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
    private String imageCached;
    private String userUrl;

    public GoogleSimplifiedUser() {}

    public GoogleSimplifiedUser(String json) throws JSONException {
        JSONObject jObj = new JSONObject(json);
        this.id = jObj.optString("id", null);
        this.displayName = jObj.optString("displayName", null);
        this.nickname = jObj.optString("nickname", null);
        this.gender = jObj.optString("gender", null);
        if (jObj.has("image")) {
            this.imageUrl = jObj.getJSONObject("image").optString("url", null);
        }
        this.imageCached = jObj.optString("imageCached", null);
        this.userUrl = jObj.optString("url", null);
        if (jObj.has("name")) {
            this.name = new Name(jObj.getJSONObject("name"));
        }
    }

    public String toJson() throws JSONException {
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
        jObj.put("imageCached", this.imageCached);
        jObj.put("url", this.userUrl);
        if (this.name != null) {
            jObj.put("name", this.name.toJson());
        }
        return jObj.toString();
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

    public String getImageCached() {
        return imageCached;
    }

    public void setImageCached(String imageCached) {
        this.imageCached = imageCached;
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



        public JSONObject toJson() throws JSONException {
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
