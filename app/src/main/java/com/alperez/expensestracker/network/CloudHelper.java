package com.alperez.expensestracker.network;

import com.alperez.expensestracker.model.Account;

/**
 * Created by stanislav.perchenko on 12-Sep-15.
 */
public class CloudHelper {

    public static interface OnDbSynchronizationListener {
        void onComplete(Account account, boolean updated, boolean initialized);
        void onError(Account account, Throwable error);
    }

    public static void synchronizeDbWithAccount(Account account, boolean createIfNotExist, final OnDbSynchronizationListener l) {
        //TODO Implement this
    }

}
