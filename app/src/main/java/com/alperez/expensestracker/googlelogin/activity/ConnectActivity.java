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
import com.alperez.expensestracker.googlelogin.utils.AuthorizationWebViewClient;
import com.alperez.expensestracker.googlelogin.utils.GoogleOAuth2AuthorizationHelper;
import com.alperez.expensestracker.googlelogin.utils.ViewUtils;
import com.alperez.expensestracker.utils.PreferencesUtils;
import com.alperez.expensestracker.widget.SlidingViewFlipper;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.Scopes;

/**
 * Created by stanislav.perchenko on 14-Sep-15.
 */
public class ConnectActivity extends Activity {
    private static final int REQUEST_CODE_PICK_ACCOUNT = 101;
    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 102;

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
                //getToken();
                if (mState == AuthorizationState.PICKING_ACCOUNT) {
                    String email = vEdtLogin.getText().toString();
                    if (!TextUtils.isEmpty(email)) {
                        vFlipper.showNext();

                        GoogleAccountCredentials credentials = new GoogleAccountCredentials(email, getResources().getString(R.string.google_oauth2_request_params), new String[]{Scopes.DRIVE_FILE});
                        PreferencesUtils.saveGoogleAccountCredentials(ConnectActivity.this, credentials);
                        Uri authUri = GoogleOAuth2AuthorizationHelper.buildAuthorizationUrl(credentials);
                        mWebClient.setAccountCredentials(credentials);
                        mWebClient.setRedirectUrl(authUri.getQueryParameter(GoogleOAuth2AuthorizationHelper.AUTH_URL_PARAM_REDIRECT_URI));
                        vWeb.loadUrl(authUri.toString());

                        vWeb.stopLoading();
                        //vWeb.loadUrl("http://www.google.com.ua");
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
                //TODO
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

    /**
     * At this stage an instance for GoogleAccountCredentials must contain valid Authorization Code.
     * This instance of GoogleAccountCredentials is saved into preferences here.
     * @param accountCredentials
     */
    private void proceedWithAuthorizationCode(GoogleAccountCredentials accountCredentials) {
        PreferencesUtils.saveGoogleAccountCredentials(this, accountCredentials);
        //TODO proceed to get TOKEn
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                vEdtLogin.setText(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
                Toast.makeText(this, String.format("Account type - %s", data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE)), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR && resultCode == RESULT_OK) {
            //getToken();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    private static final String SCOPES = "oauth2:https://www.googleapis.com/auth/drive.readonly";



    /*private ProgressDialog mPd;

    private void getToken() {
        String email = vEdtLogin.getText().toString();
        if (TextUtils.isEmpty(email)) return;


        mPd = new ProgressDialog(this);
        mPd.setIndeterminate(true);
        mPd.setMessage("Wait!");
        mPd.setCancelable(false);
        mPd.show();

        new GetTokenTask(this){
            @Override
            protected void onPostExecute(GetTokenTaskResult result) {
                mPd.dismiss();
                if (result.getToken() != null) {
                    Toast.makeText(ConnectActivity.this, result.getToken(), Toast.LENGTH_SHORT).show();
                } else if (result.getError() instanceof GooglePlayServicesAvailabilityException) {
                    int statusCode = ((GooglePlayServicesAvailabilityException) result.getError()).getConnectionStatusCode();
                    GooglePlayServicesUtil.getErrorDialog(statusCode, ConnectActivity.this, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR).show();
                } else if (result.getError() instanceof UserRecoverableAuthException) {
                    Intent intent = ((UserRecoverableAuthException) result.getError()).getIntent();
                    startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                } else {
                    Toast.makeText(ConnectActivity.this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            protected void onCancelled(GetTokenTaskResult getTokenTaskResult) {
                mPd.dismiss();
            }
        }.execute(email, SCOPES);
    }*/




    private void goBackFromAuthorizing() {
        ViewUtils.safelyResetWebView(vWeb);
        mState = AuthorizationState.PICKING_ACCOUNT;
        vFlipper.showPrevious();
        vEdtLogin.setText(null);
        vEdtLogin.requestFocus();
    }


}
