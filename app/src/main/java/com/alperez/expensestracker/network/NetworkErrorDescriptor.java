package com.alperez.expensestracker.network;

/**
 * Created by stanislav.perchenko on 17-Sep-15.
 */
public class NetworkErrorDescriptor {
    public String error;
    public String errorDescription;
    public int responseCode;
    public String rawResponse;
    private NetworkRequest networkRequest;

    public NetworkErrorDescriptor(NetworkRequest request) {
        this.networkRequest = request;
    }

    public NetworkRequest getNetworkRequest() {
        return networkRequest;
    }
}
