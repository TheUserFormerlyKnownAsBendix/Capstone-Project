package at.dingbat.type.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import at.dingbat.apiutils.ApiUtil;
import at.dingbat.type.R;


public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInOptions gso;
    private GoogleApiClient googleClient;

    private SignInButton signInButton;

    private ApiUtil.LoginCallback loginCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();

        googleClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        loginCallback = new ApiUtil.LoginCallback() {
            @Override
            public void onLoggedIn(GoogleSignInResult result) {
                if(result.isSuccess()) {
                    GoogleSignInAccount account = result.getSignInAccount();

                    Intent i = new Intent(SignInActivity.this, MainActivity.class);
                    startActivity(i);
                } else Log.e(SignInActivity.class.getCanonicalName(), getString(R.string.login_failed));
            }
        };

        signInButton = (SignInButton) findViewById(R.id.activity_sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setScopes(gso.getScopeArray());

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            loginCallback.onLoggedIn(result);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        ApiUtil.silentLogin(googleClient, loginCallback);
    }

}
