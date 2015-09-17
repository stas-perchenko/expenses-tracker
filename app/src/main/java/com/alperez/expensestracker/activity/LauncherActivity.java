package com.alperez.expensestracker.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.alperez.expensestracker.googlelogin.activity.ConnectActivity;
import com.alperez.expensestracker.model.Account;
import com.alperez.expensestracker.network.CloudHelper;
import com.alperez.expensestracker.network.Network;
import com.alperez.expensestracker.utils.PreferencesUtils;

import java.io.File;

/**
 * Created by stanislav.perchenko on 12-Sep-15.
 */
public class LauncherActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Account account = PreferencesUtils.loadConnectedAccount(this);
        boolean needConnectionActfivity = false;
        if (account == null) {
            needConnectionActfivity = true;
        } else if (account != null && account.isValid() && !account.isConnected()) {
            needConnectionActfivity = true;
            PreferencesUtils.removeConnectedAccount(this, account);
        }

        if (needConnectionActfivity) {
            startActivity(new Intent(this, ConnectActivity.class));
            finish();
            return;
        }

        if (!account.isValid()) {
            Toast.makeText(this, "Fault", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        File dbFile = getDatabasePath(account.getDbName());
        if (dbFile.exists()) {
            //TODO Start main activity
            finish();
            return;
        } else {
            //TODO Set content View for this actfivity
            if (!Network.isNetworkAvailable(this, true)) {
                //TODO
                //showNoNetworkDialog();
            } else {
                //TODO Show updating progress
                CloudHelper.synchronizeDbWithAccount(account, true, new CloudHelper.OnDbSynchronizationListener() {
                    @Override
                    public void onComplete(Account account, boolean updated, boolean initialized) {
                        //TODO Start main activity
                    }

                    @Override
                    public void onError(Account account, Throwable error) {

                    }
                });
            }
        }
    }
}
