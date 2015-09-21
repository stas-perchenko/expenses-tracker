package com.alperez.expensestracker.network;

import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stanislav.perchenko on 17-Sep-15.
 */
public class NetworkRequest {

    public enum Method {
        POST, GET, PUT, DELETE, RESET
    }

    private Map<String, String> headers;
    private Map<String, String> params;
    private String url;
    private Method method;
    private int timeOut = 10000;

    private byte[] data;

    public NetworkRequest(String url) {
        this(url, null);
    }

    public NetworkRequest(String url, HashMap<String, String> params) {
        this(url, params, Method.GET);
    }

    public NetworkRequest(String url, HashMap<String, String> params, Method method) {
        this(url, params, method, null);
    }

    public NetworkRequest(String url, Map<String, String> params, Method method, Map<String, String> headers) {
        this.url = url;
        this.params = params;
        this.method = method;


        this.headers = new HashMap<String, String>();
        if (headers != null) this.headers.putAll(headers);
        if (!this.headers.containsKey("Content-Type")) {
            this.headers.put("Content-Type", "application/json");
        }
    }

    private Uri mUri;
    public Uri getUriForRequest() {
        if (mUri == null) {
            Uri.Builder builder = Uri.parse(this.url).buildUpon();
            if (params != null) {
                for (String key : params.keySet()) {
                    builder.appendQueryParameter(key, params.get(key));
                }
            }
            mUri = builder.build();
        }
        return mUri;
    }

    public int addHeader(String name, String value) {
        this.headers.put(name, value);
        return this.headers.size();
    }

    public void setData(String data) {
        try {
            setData(data.getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            setData(data.getBytes());
        }
    }

    public void setData(Object data) throws IOException {
        setData(getBytesFromObject(data));
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Method getMethod() {
        return method;
    }

    public byte[] getData() {
        return data;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    private byte[] getBytesFromObject(Object obj) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(obj);
            return bos.toByteArray();
        } finally {
            if (out != null) out.close();
            bos.close();
        }
    }
}
