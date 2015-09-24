package com.alperez.expensestracker.utils;

import java.io.IOException;

/**
 * Created by stanislav.perchenko on 24-Sep-15.
 */
public class ExternalMediaException extends IOException {

    private static final long serialVersionUID = 1L;
    private String message;

    ExternalMediaException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return new String(ExternalMediaException.class.getName() + ((message != null && message.length() > 0) ? ": " + message : ""));
    }
}
