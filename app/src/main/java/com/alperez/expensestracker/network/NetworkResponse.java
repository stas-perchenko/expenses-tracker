package com.alperez.expensestracker.network;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;

/**
 * Created by stanislav.perchenko on 17-Sep-15.
 */
public class NetworkResponse {

    public HttpURLConnection connection;
    public boolean disconnected;

    NetworkResponse(@NonNull HttpURLConnection connection) {
        if (connection == null) throw new IllegalArgumentException("HttpURLConnection instance must be provided");
        this.connection = connection;
    }




    public String readResponse() throws IOException {
        checkDisconnected();
        try {
            int respCode = connection.getResponseCode();
            BufferedReader reader = new BufferedReader(new InputStreamReader((respCode >= 200 && respCode < 300) ? connection.getInputStream() : connection.getErrorStream()));
            try {
                StringBuilder responseBuilder = new StringBuilder();
                String s = null;
                while ((s = reader.readLine()) != null) {
                    responseBuilder.append(s);
                }
                return responseBuilder.toString();
            } finally {
                reader.close();
            }
        } catch (SocketTimeoutException ex) {
            return "";
        }
    }


    /**********************************************************************************************/
    /***********************  Reading response data when a request was successful  ****************/
    /**********************************************************************************************/
    public void readDataStream(OutputStream outStream) throws  IOException{
        checkDisconnected();
        if (isRequestOk()) {
            InputStream is = connection.getInputStream();
            try {
                readConnectionStream(outStream, is);
            } finally {
                is.close();
            }
        } else {
            throw new IllegalStateException("Connection returned error. Data stream can not be read.");
        }
    }

    public byte[] readDataBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            readDataStream(buffer);
            return buffer.toByteArray();
        } finally {
            buffer.close();
        }
    }

    public String readResponseData() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            readDataStream(buffer);
            return buffer.toString("utf-8");
        } finally {
            buffer.close();
        }
    }



    /**********************************************************************************************/
    /***********************  Reading response data when a request returned error  ****************/
    /**********************************************************************************************/
    public void readErrorStream(OutputStream outStream) throws  IOException{
        checkDisconnected();
        if (!isRequestOk()) {
            InputStream is = connection.getErrorStream();
            try {
                readConnectionStream(outStream, is);
            } finally {
                is.close();
            }
        } else {
            throw new IllegalStateException("Request was successful. You must read data stream");
        }
    }

    public byte[] readErrorBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            readErrorStream(buffer);
            return buffer.toByteArray();
        } finally {
            buffer.close();
        }
    }

    public String readResponseError() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            readErrorStream(buffer);
            return buffer.toString("utf-8");
        } finally {
            buffer.close();
        }
    }


    /**********************************************************************************************/
    /***************************  Check and manipulate connection state  **************************/
    /**********************************************************************************************/

    /**
     * Returns true if the response code is some kind of "2xx"
     * @return
     */
    public boolean isRequestOk() throws IOException{
        checkDisconnected();
        return (connection.getResponseCode() >= 200) && connection.getResponseCode() < 300;
    }


    public void release() {
        if (!disconnected) {
            this.connection.disconnect();
            disconnected = true;
        }
    }

    public boolean isReleased() {
        return disconnected;
    }



    private void checkDisconnected() {
        if (disconnected) throw new IllegalStateException("HttpURLConnection instance have already been released");
    }

    private void readConnectionStream(OutputStream oStream, InputStream iStream) throws IOException {
        byte[] data = new byte[2048];
        int nRead;
        while ((nRead = iStream.read(data, 0, data.length)) != -1) {
            oStream.write(data, 0, nRead);
        }
        oStream.flush();
    }

}
