package com.alperez.expensestracker.googlelogin.utils;

import java.util.Date;

/**
 * Created by stanislav.perchenko on 23-Sep-15.
 */
public class AccessTokenExpiresException extends Exception {
    private long expirationTime;
    private long checkTime;
    private String message;

    public AccessTokenExpiresException(long expirationTime, long checkTime) {
        super();
        this.expirationTime = expirationTime;
        this.checkTime = checkTime;

    }

    @Override
    public String getMessage() {
        if (message == null) {
            message = String.format(MSG_TEMPLATE, new Date(expirationTime), new Date(checkTime));
        }
        return message;
    }

    private static final String MSG_TEMPLATE = "Access token expired. expiration time - %1$tb %1$te %1$tT.%1$tL;  time of check - %2$tb %2$te %2$tT.%2$tL";
}
