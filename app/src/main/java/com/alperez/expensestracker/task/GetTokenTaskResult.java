package com.alperez.expensestracker.task;

/**
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class GetTokenTaskResult {
    private String token;
    private Exception e;

    public GetTokenTaskResult(String token, Exception e) {
        this.token = token;
        this.e = e;
    }

    public Exception getError() {
        return e;
    }

    public String getToken() {
        return token;
    }
}
