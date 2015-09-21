package com.alperez.expensestracker.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Performs general network requests using HttpURLConnection
 *
 * Created by stanislav.perchenko on 17-Sep-15.
 */
public class Network {

    public static NetworkResponse doRequest(NetworkRequest request) throws IOException {
        URL preparedURL = new URL(buildUri(request.getUrl(), request.getParams()).toString());
        HttpURLConnection connection = (HttpURLConnection) preparedURL.openConnection();
        connection.setConnectTimeout(request.getTimeOut());
        connection.setDoInput(true);
        if (request.getHeaders() != null) {
            for (String key : request.getHeaders().keySet()) {
                connection.addRequestProperty(key, request.getHeaders().get(key));
            }
        }
        NetworkRequest.Method method = request.getMethod();
        if (isMethodPermitted(method.name())) {
            connection.setRequestMethod(method.name());
        } else {
            try {
                Field methodField = HttpURLConnection.class.getDeclaredField("method");
                methodField.setAccessible(true);
                methodField.set(connection, method.name());
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if ((method == NetworkRequest.Method.POST || method == NetworkRequest.Method.PUT)) {
            if (request.getData() != null && request.getData().length != 0) {
                connection.addRequestProperty("Content-length", Integer.toString(request.getData().length));
                connection.setDoOutput(true);
                BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
                outputStream.write(request.getData());
                outputStream.flush();
                outputStream.close();
            } else {
                connection.addRequestProperty("Content-length", "0");   //Required by android 2
            }
        }
        return new NetworkResponse(connection);
    }

    public static boolean isNetworkAvailable(Context context, boolean includeConnectingStage) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (info != null) && (includeConnectingStage ? info.isConnectedOrConnecting() : info.isConnected());
    }



    private static Uri buildUri(String host, Map<String, String> params) {
        Uri.Builder builder = Uri.parse(host).buildUpon();
        for (String key : params.keySet()) {
            builder.appendQueryParameter(key, params.get(key));
        }
        return builder.build();
    }

    public static String getParamsAsEncodedData(Map<String, String> params) {
        if (params == null || params.size() == 0) return "";

        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            if (sb.length() > 0) sb.append('&');
            sb.append(encodeString(key, "utf-8"));
            sb.append('=');
            sb.append(encodeString(params.get(key), "utf-8"));
        }
        return sb.toString();
    }

    public static String appendParamsToUrl(String baseUrl, Map<String, String> params) {
        if (params == null || params.size() == 0) return baseUrl;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(baseUrl);

        int counter = 0;
        for (String key : params.keySet()) {
            urlBuilder.append(counter++ == 0 ? "?" : "&").append(key).append("=").append(encodeString(params.get(key), "utf-8"));
        }

        return urlBuilder.toString();
    }


    public static String encodeString(String s, String charSet) {
        if (TextUtils.isEmpty(s)) return "";
        try {
            return URLEncoder.encode(s, charSet);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return URLEncoder.encode(s);
    }





    private static final String[] PERMITTED_USER_METHODS = {
            "OPTIONS",
            "GET",
            "HEAD",
            "POST",
            "PUT",
            "DELETE",
            "TRACE"
    };

    private static boolean isMethodPermitted(String method) {
        for (String permittedUserMethod : PERMITTED_USER_METHODS) {
            if (permittedUserMethod.equals(method)) {
                return true;
            }
        }
        return false;
    }

}
