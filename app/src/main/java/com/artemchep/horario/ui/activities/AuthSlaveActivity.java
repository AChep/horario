/*
 * Copyright (C) 2017 XJSHQ@github.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.artemchep.horario.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;

import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class AuthSlaveActivity extends ActivityBase {

    public static final String AUTH_TYPE = "auth_type";
    public static final String AUTH_EMAIL = "auth_email";
    public static final String AUTH_TOKEN = "auth_token";
    public static final String AUTH_TOKEN_SECRET = "auth_token_secret";
    public static final String AUTH_PASSWORD = "auth_password";
    public static final int AUTH_SIGN_IN = 1;
    public static final int AUTH_SIGN_IN_GOOGLE = 2;
    public static final int AUTH_SIGN_IN_FACEBOOK = 3;
    public static final int AUTH_SIGN_IN_TWITTER = 4;
    public static final int AUTH_SIGN_UP = 100;

    @NonNull
    private static String authToString(int auth) {
        switch (auth) {
            case AUTH_SIGN_IN:
                return "sign_in";
            case AUTH_SIGN_IN_GOOGLE:
                return "sign_in_google";
            case AUTH_SIGN_IN_FACEBOOK:
                return "sign_in_facebook";
            case AUTH_SIGN_IN_TWITTER:
                return "sign_in_twitter";
            case AUTH_SIGN_UP:
                return "sign_up";
            default:
                return "unknown";
        }
    }


    private FirebaseAnalytics mAnalytics;
    private int mAuthType;

    @NonNull
    private final OnCompleteListener<AuthResult> mListener = new OnCompleteListener<AuthResult>() {
        @Override
        public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
                setResult(Activity.RESULT_OK);
            } else {
                setResult(Activity.RESULT_CANCELED);
                showErrorToast(task);
            }

            Bundle bundle = new Bundle();
            bundle.putString("type", authToString(mAuthType));
            bundle.putBoolean("is_successful", task.isSuccessful());
            mAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle);

            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_slave);
        setFinishOnTouchOutside(false);

        mAnalytics = FirebaseAnalytics.getInstance(this);

        Intent intent = getIntent();
        mAuthType = intent.getIntExtra(AUTH_TYPE, 0);
        switch (mAuthType) {
            case AUTH_SIGN_IN:
            case AUTH_SIGN_UP: {
                String emailText = intent.getStringExtra(AUTH_EMAIL);
                String passwordText = intent.getStringExtra(AUTH_PASSWORD);
                signWithEmail(mAuthType, emailText, passwordText);
                break;
            }
            case AUTH_SIGN_IN_GOOGLE: {
                String tokenText = intent.getStringExtra(AUTH_TOKEN);
                signWithGoogle(mAuthType, tokenText);
                break;
            }
            case AUTH_SIGN_IN_FACEBOOK: {
                String tokenText = intent.getStringExtra(AUTH_TOKEN);
                signWithFacebook(mAuthType, tokenText);
                break;
            }
            case AUTH_SIGN_IN_TWITTER: {
                String tokenText = intent.getStringExtra(AUTH_TOKEN);
                String secretText = intent.getStringExtra(AUTH_TOKEN_SECRET);
                signWithTwitter(mAuthType, tokenText, secretText);
                break;
            }
        }
    }

    private void signWithEmail(int type, String emailText, String passwordText) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        TextView statusTextView = findViewById(R.id.status);
        switch (type) {
            case AUTH_SIGN_IN:
                statusTextView.setText(R.string.auth_signing_in);
                auth
                        .signInWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(mListener);
                break;
            case AUTH_SIGN_UP:
                statusTextView.setText(R.string.auth_signing_up);
                auth
                        .createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(mListener);
                break;
        }
    }

    private void signWithGoogle(int type, String token) {
        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(R.string.auth_signing_in);

        AuthCredential credential = GoogleAuthProvider.getCredential(token, null);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth
                .signInWithCredential(credential)
                .addOnCompleteListener(mListener);
    }

    private void signWithFacebook(int type, String token) {
        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(R.string.auth_signing_in);

        AuthCredential credential = FacebookAuthProvider.getCredential(token);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth
                .signInWithCredential(credential)
                .addOnCompleteListener(mListener);
    }

    private void signWithTwitter(int type, String token, String secret) {
        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(R.string.auth_signing_in);

        AuthCredential credential = TwitterAuthProvider.getCredential(token, secret);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth
                .signInWithCredential(credential)
                .addOnCompleteListener(mListener);
    }

    /**
     * Shows a toast with the message from task's exception if exists,
     * otherwise shows simple "Auth failed" message.
     */
    private void showErrorToast(@NonNull Task<AuthResult> task) {
        //noinspection ThrowableResultOfMethodCallIgnored
        Exception e = task.getException();
        String message = e != null ? e.getMessage() : "Auth failed.";
        Toasty.error(AuthSlaveActivity.this, message).show();
    }

    @Override
    public void onBackPressed() {
        // Do not exit activity by pressing back key.

        // This is really bad for user experience, but
        // there's no way to interrupt sign in/up process.
    }

}
