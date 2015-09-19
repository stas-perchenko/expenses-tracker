package com.alperez.expensestracker.googlelogin.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alperez.expensestracker.R;
import com.alperez.expensestracker.googlelogin.model.AuthorizationCode;
import com.alperez.expensestracker.googlelogin.model.AuthorizationState;
import com.alperez.expensestracker.googlelogin.model.GoogleAccountCredentials;
import com.alperez.expensestracker.googlelogin.task.GetGoogleAccountUserTask;
import com.alperez.expensestracker.googlelogin.task.GetTokenByAuthorizationCodeTask;
import com.alperez.expensestracker.googlelogin.utils.AuthorizationWebViewClient;
import com.alperez.expensestracker.googlelogin.utils.GoogleOAuth2AuthorizationHelper;
import com.alperez.expensestracker.googlelogin.utils.PreferencesUtils;
import com.alperez.expensestracker.googlelogin.utils.ViewUtils;
import com.alperez.expensestracker.network.NetworkErrorDescriptor;
import com.alperez.expensestracker.widget.SlidingViewFlipper;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.Scopes;

import java.util.Map;

/**
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class ConnectActivity extends Activity {
    private static final int REQUEST_CODE_PICK_ACCOUNT = 101;

    private static final String[] OAUTH2_SCOPES = new String[]{
            Scopes.DRIVE_FILE,
            "https://www.googleapis.com/auth/plus.login",
            "https://www.googleapis.com/auth/plus.me"
    };

    private static final String TAG = "ConnectActivity";


    private SlidingViewFlipper vFlipper;

    private EditText vEdtLogin;
    private Button vBtnSignIn;

    private WebView vWeb;

    private AuthorizationState mState = AuthorizationState.PICKING_ACCOUNT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        vFlipper = (SlidingViewFlipper) findViewById(R.id.anim_container);
        vEdtLogin = (EditText) findViewById(R.id.login);
        vBtnSignIn = (Button) findViewById(R.id.sign_in);
        vWeb = (WebView) findViewById(R.id.web_view);

        vWeb.getSettings().setJavaScriptEnabled(true);
        vWeb.setWebViewClient(mWebClient);

        vWeb.clearFormData();

        //---- To remove cacheg login info ----
        CookieManager cm = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT < 21) {
            CookieSyncManager.createInstance(this);
            cm.removeAllCookie();
            CookieSyncManager.getInstance().sync();
        } else {
            cm.removeAllCookies(null);
            cm.flush();
        }



        findViewById(R.id.pick_account).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, true, null, null, null, null);
                startActivityForResult(i, REQUEST_CODE_PICK_ACCOUNT);
            }
        });

        vBtnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState == AuthorizationState.PICKING_ACCOUNT) {
                    String email = vEdtLogin.getText().toString();
                    if (!TextUtils.isEmpty(email)) {
                        vFlipper.showNext();

                        GoogleAccountCredentials credentials = new GoogleAccountCredentials(email,
                                                                                            getResources().getString(R.string.google_oauth2_request_params),
                                                                                            OAUTH2_SCOPES);
                        PreferencesUtils.saveGoogleAccountCredentials(ConnectActivity.this, credentials);
                        Uri authUri = GoogleOAuth2AuthorizationHelper.buildAuthorizationUrl(credentials);
                        mWebClient.setAccountCredentials(credentials);
                        mWebClient.setRedirectUrl(authUri.getQueryParameter(GoogleOAuth2AuthorizationHelper.AUTH_URL_PARAM_REDIRECT_URI));
                        vWeb.loadUrl(authUri.toString());
                        mState = AuthorizationState.AUTHORIZING;
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {

        switch (mState) {
            case PICKING_ACCOUNT:
                super.onBackPressed();
                break;
            case AUTHORIZING:
                goBackFromAuthorizing();
                break;
            case GETTING_TOKENS:
            case GETTING_USER:
                goBackFromPage2();
                break;
            case AUTHORIZED:
            case AUTHORIZATION_FAILED:
                goToTheFirstPage();
                break;
        }
    }

    private AuthorizationWebViewClient mWebClient = new AuthorizationWebViewClient(null, new AuthorizationWebViewClient.OnAuthorizationWebViewDelegate() {
        @Override
        public AuthorizationState getAuthState() {
            return mState;
        }

        @Override
        public void onComplete(GoogleAccountCredentials accountCredentials, AuthorizationCode authCode, String error) {
            if (authCode == null) {
                String message = null;
                if (TextUtils.isEmpty(error)) {
                    message = getResources().getString(R.string.authorization_error_unknown);
                } else {
                    if ("access_denied".equals(error)) {
                        message = getResources().getString(R.string.authorization_access_denied);
                    } else {
                        message = getResources().getString(R.string.authorization_access_described, error);
                    }
                }
                Toast.makeText(ConnectActivity.this, message, Toast.LENGTH_LONG).show();
                goBackFromAuthorizing();
            } else {
                accountCredentials.setAuthorizationCode(authCode);
                ConnectActivity.this.proceedWithAuthorizationCode(accountCredentials);
            }
        }
    });


    private GetTokenByAuthorizationCodeTask mGetTokenTask;

    /**
     * At this stage an instance for GoogleAccountCredentials must contain valid Authorization Code.
     * This instance of GoogleAccountCredentials is saved into preferences here.
     * @param credentials
     */
    private void proceedWithAuthorizationCode(GoogleAccountCredentials credentials) {
        PreferencesUtils.saveGoogleAccountCredentials(this, credentials);
        mGetTokenTask = new GetTokenByAuthorizationCodeTask() {
            @Override
            protected void onPostExecute(final Map<String, Object> result) {
                if (result.containsKey(GetTokenByAuthorizationCodeTask.RESULT_KEY_ERROR_DESCRIPTOR)) {
                    //----  Error occured  ----
                    setStateAuthorizationFailed((GoogleAccountCredentials) result.get(GetTokenByAuthorizationCodeTask.RESULT_KEY_CREDENTIALS), (NetworkErrorDescriptor) result.get(GetTokenByAuthorizationCodeTask.RESULT_KEY_ERROR_DESCRIPTOR));
                } else {
                    //----  Got access tokens - proceed firther  ----
                    ConnectActivity.this.getWindow().getDecorView().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            proceedWithGettingUserInfo((GoogleAccountCredentials) result.get(GetTokenByAuthorizationCodeTask.RESULT_KEY_CREDENTIALS));
                        }
                    }, 50);
                }
                mGetTokenTask = null;
            }
        };
        mState = AuthorizationState.GETTING_TOKENS;
        updateStatusPresentationPage2();
        vFlipper.showNext();
        mGetTokenTask.safeExecute(credentials);
    }

    private GetGoogleAccountUserTask mGetUserTask;

    private void proceedWithGettingUserInfo(GoogleAccountCredentials credentials) {
        PreferencesUtils.saveGoogleAccountCredentials(ConnectActivity.this, credentials);
        mGetUserTask = new GetGoogleAccountUserTask() {
            @Override
            protected void onPostExecute(Map<String, Object> result) {
                if (result.containsKey(GetGoogleAccountUserTask.RESULT_KEY_ERROR_DESCRIPTOR)) {
                    //----  Error occured  ----
                    setStateAuthorizationFailed((GoogleAccountCredentials) result.get(GetGoogleAccountUserTask.RESULT_KEY_CREDENTIALS), (NetworkErrorDescriptor) result.get(GetGoogleAccountUserTask.RESULT_KEY_ERROR_DESCRIPTOR));
                } else {
                    //----  Got User account data  ----
                    setStateAuthorized((GoogleAccountCredentials) result.get(GetGoogleAccountUserTask.RESULT_KEY_CREDENTIALS));
                }
                mGetUserTask = null;
            }
        };
        mState = AuthorizationState.GETTING_USER;
        updateStatusPresentationPage2();
        mGetUserTask.safeExecute(credentials);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                vEdtLogin.setText(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                Toast.makeText(this, String.format("Account type - %s", data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)), Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Go to page 3, show user info and propose a user to accept this account.
     * Show "Decline" button
     */
    private void setStateAuthorized(GoogleAccountCredentials credentials) {
        PreferencesUtils.saveGoogleAccountCredentials(ConnectActivity.this, credentials);
        mState = AuthorizationState.AUTHORIZED;
        vFlipper.showNext(3);
        //TODO Populate obtained user information on this page
    }

    /**
     * Go to page 4 which tells user about the failure and propose Accept it or try again
     * @param error
     */
    private void setStateAuthorizationFailed(GoogleAccountCredentials credentials, NetworkErrorDescriptor error) {
        mState = AuthorizationState.AUTHORIZATION_FAILED;
        vFlipper.showNext(4);
        PreferencesUtils.removeGoogleAccountCredentials(this, credentials.getAccountName());
        //TODO Populate error info
    }


    /**
     * Shows appropriate status message on page 2 based on the status value,
     * which can be either GETTING_TOKENS or GETTING_USER
     */
    private void updateStatusPresentationPage2() {
        switch (mState) {
            case GETTING_TOKENS:
                //TODO
                break;
            case GETTING_USER:
                //TODO
                break;
        }
    }


    private void goBackFromAuthorizing() {
        ViewUtils.safelyResetWebView(vWeb);
        goToTheFirstPage();
    }

    private void goBackFromPage2() {
        if (mGetTokenTask != null) {
            mGetTokenTask.cancel(true);
            mGetTokenTask = null;
        }
        if (mGetUserTask != null) {
            mGetUserTask.cancel(true);
            mGetUserTask = null;
        }
        goToTheFirstPage();
    }

    private void goToTheFirstPage() {
        mState = AuthorizationState.PICKING_ACCOUNT;
        vFlipper.showFirst();
        vEdtLogin.setText(null);
        vEdtLogin.requestFocus();
    }

}
