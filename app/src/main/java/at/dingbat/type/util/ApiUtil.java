package at.dingbat.type.util;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

/**
 * Created by Max on 11/21/2015.
 */
public class ApiUtil {

    public static void silentLogin(GoogleApiClient client, final LoginCallback callback) {
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(client);
        if(opr.isDone()) {
            callback.onLoggedIn(opr.get());
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    callback.onLoggedIn(googleSignInResult);
                }
            });
        }
    }

    public static interface LoginCallback {
        void onLoggedIn(GoogleSignInResult result);
    }

}
