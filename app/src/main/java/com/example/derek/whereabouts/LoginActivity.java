package com.example.derek.whereabouts;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * Created by Derek on 2/20/16.
 */
public class LoginActivity extends AppCompatActivity {

    LoginButton loginButton;
    CallbackManager callbackManager;
    public final static String USERNAME = "username";
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        setContentView(R.layout.activity_login);

        sharedPref = getSharedPreferences(USERNAME, Context.MODE_PRIVATE);

        editor = sharedPref.edit();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        callbackManager = CallbackManager.Factory.create();

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("Facebook", "onSuccess: ");
                // App code
                editor.putString(USERNAME, loginResult.getAccessToken().getUserId());
                editor.apply();

                ProfileTracker profileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                        this.stopTracking();
                        Profile.setCurrentProfile(currentProfile);
                        Log.d("facebook", Profile.getCurrentProfile().getName());
                    }
                };
                profileTracker.startTracking();

                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onCancel() {
                Log.d("Facebook", "onCancel: ");
                // App code

            }

            @Override
            public void onError(FacebookException exception) {
                Log.d("Facebook", "onError: ");
                // App code

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
