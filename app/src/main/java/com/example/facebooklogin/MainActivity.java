package com.example.facebooklogin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    public CallbackManager mCallbackManager = CallbackManager.Factory.create();
    AppCompatButton fbbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        fbbtn = findViewById(R.id.btnFbLogin);
        // Add code to print out the key hash
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.app.roadradar",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch (Exception e) {
            Log.d("Keycatch", "onCreate: " + e);
        }

        fbbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fbLogin();
            }
        });

    }


    public void fbLogin() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        setFacebookData(loginResult);
                        Log.d("Onsucess", "onSuccess: " + "onsuccess");
                    }

                    @Override
                    public void onCancel() {
                        onFbCancel();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        onFbError(exception.getMessage());
                        Log.d("error", "onError: " + "onerror");
                    }
                });
    }

    private void setFacebookData(final LoginResult loginResult) {
        GraphRequest request = GraphRequest.newMeRequest(
                loginResult.getAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {

                        try {
                            String firstName = response.getJSONObject().getString("first_name");
                            String lastName = response.getJSONObject().getString("last_name");
                            String email = response.getJSONObject().getString("email");
                            String id = response.getJSONObject().getString("id");
                            AccessToken accessToken = AccessToken.getCurrentAccessToken();
                            String token = accessToken.getToken();

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);

                            String name = firstName + " " + lastName;
                            if (TextUtils.isEmpty(email)) {
                                Bundle bundle = new Bundle();
                                bundle.putString("name", name);
                                bundle.putString("fb_token", token);
                            } else {
                            }

                        } catch (JSONException e) {

                        }

                    }

                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,email,first_name,last_name,gender");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public void onFbCancel() {
        showSnackbar(getString(R.string.fb_login_cancel));
    }

    public void showSnackbar(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Snackbar.make(findViewById(android.R.id.content),
                    msg,
                    Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    public void onFbError(String error) {
        showSnackbar(getString(R.string.fb_login_error));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

}



