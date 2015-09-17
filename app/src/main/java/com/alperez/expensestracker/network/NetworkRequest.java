package com.alperez.expensestracker.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by stanislav.perchenko on 17-Sep-15.
 */
public class NetworkRequest {

    public enum Method {
        POST, GET, PUT, DELETE, RESET
    }

    private HashMap<String, String> headers;
    private HashMap<String, String> params;
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

    public NetworkRequest(String url, HashMap<String, String> params, Method method, HashMap<String, String> headers) {
        this.url = url;
        this.params = params;
        this.method = method;
        this.headers = new HashMap<String, String>();
        this.headers.put("Content-Type", "application/json");

        if (headers != null) {
            for (String key : headers.keySet()) {
                this.headers.put(key, headers.get(key));
            }
        }
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

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public HashMap<String, String> getParams() {
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
