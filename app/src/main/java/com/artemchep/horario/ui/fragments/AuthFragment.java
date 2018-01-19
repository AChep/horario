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
package com.artemchep.horario.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.Binfo;
import com.artemchep.horario.R;
import com.artemchep.horario._new.activities.MainActivity;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.AuthSlaveActivity;
import com.artemchep.horario.ui.widgets.EmailInputLayout;
import com.artemchep.horario.ui.widgets.SignInLayout;
import com.artemchep.horario.ui.widgets.SignUpLayout;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * @author Artem Chepurnoy
 */
public class AuthFragment extends Fragment implements
        FirebaseAuth.AuthStateListener,
        View.OnClickListener {

    private static final int REQUEST_CODE_SIGN_IN_GOOGLE = 1000;
    private static final int REQUEST_CODE_SIGN_IN_EMAIL = 999;
    private static final int REQUEST_CODE_SIGN_UP_EMAIL = 998;

    private SignInLayout mSignInView;
    private FirebaseAuth mAuth;

    private GoogleApiClient mGoogleApi;
    private boolean mGoogleSignInEnabled;

    private CallbackManager mFacebookCallbackManager;
    private LoginButton mFacebookLoginButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity = getActivity();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(Binfo.GOOGLE_API_TOKEN)
                .requestEmail()
                .build();
        mGoogleApi = new GoogleApiClient.Builder(activity)
                .enableAutoManage(activity, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        setGoogleSignInEnabled(false);
                    }
                })
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        setGoogleSignInEnabled(true);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) switchToMainActivity();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View content = view.findViewById(R.id.content);
        View methods = content.findViewById(R.id.methods);
        mSignInView = content.findViewById(R.id.sign_in_layout);

        content.findViewById(R.id.sign_in).setOnClickListener(this);
        content.findViewById(R.id.sign_up).setOnClickListener(this);
        methods.findViewById(R.id.google_auth).setOnClickListener(this);
        methods.findViewById(R.id.facebook_auth).setOnClickListener(this);
        methods.findViewById(R.id.github_auth).setOnClickListener(this);
        content.findViewById(R.id.restore).setOnClickListener(this);
        content.findViewById(R.id.privacy_policy).setOnClickListener(this);

        initFacebookLoginButton(view);
        setGoogleSignInEnabled(mGoogleSignInEnabled);
    }

    private void initFacebookLoginButton(View view) {
        mFacebookCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginButton = view.findViewById(R.id.facebook_auth_fake);
        mFacebookLoginButton.setFragment(this);
        mFacebookLoginButton.setReadPermissions("email", "public_profile");
        mFacebookLoginButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                String token = loginResult.getAccessToken().getToken();
                Intent intent = new Intent(getActivity(), AuthSlaveActivity.class);
                intent.putExtra(AuthSlaveActivity.AUTH_TYPE, AuthSlaveActivity.AUTH_SIGN_IN_FACEBOOK);
                intent.putExtra(AuthSlaveActivity.AUTH_TOKEN, token);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in:
                if (mSignInView.validate()) {
                    signWithEmail(
                            mSignInView.getEmailText(),
                            mSignInView.getPasswordText());
                }
                break;
            case R.id.sign_up:
                showRegisterDialog();
                break;
            case R.id.google_auth:
                signWithGoogle();
                break;
            case R.id.github_auth:
                // TODO: Add GitHub authentication
                break;
            case R.id.facebook_auth:
                mFacebookLoginButton.performClick();
                break;
            case R.id.twitter_auth:
                // TODO: Add Twitter authentication
                break;
            case R.id.restore:
                showRestorePasswordDialog();
                break;
            case R.id.privacy_policy:
                showPrivacyPolicyDialog();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CODE_SIGN_UP_EMAIL:
                break;
            case REQUEST_CODE_SIGN_IN_EMAIL:
                if (resultCode == Activity.RESULT_CANCELED) {
                    mSignInView.clearPassword();
                }
                break;
            case REQUEST_CODE_SIGN_IN_GOOGLE: {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                if (result.isSuccess() && result.getSignInAccount() != null) {
                    String token = result.getSignInAccount().getIdToken();
                    Intent intent = new Intent(getActivity(), AuthSlaveActivity.class);
                    intent.putExtra(AuthSlaveActivity.AUTH_TYPE, AuthSlaveActivity.AUTH_SIGN_IN_GOOGLE);
                    intent.putExtra(AuthSlaveActivity.AUTH_TOKEN, token);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                break;
            }
            default:
                mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) switchToMainActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(this);
    }

    private void setGoogleSignInEnabled(boolean enabled) {
        mGoogleSignInEnabled = enabled;
        if (getView() != null) {
            getView().findViewById(R.id.google_auth).setEnabled(enabled);
        }
    }

    private void switchToMainActivity() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // --------------------------
    // -- DIALOGS ---------------
    // --------------------------

    /**
     * Shows dialog with privacy policy rules.
     */
    private void showPrivacyPolicyDialog() {
        ActivityBase activity = (ActivityBase) getActivity();
        DialogHelper.showPrivacyDialog(activity);
    }

    /**
     * Shows dialog that provides email input and then calls
     * {@link #sendPasswordResetEmail(String)} to reset password
     * of the user.
     */
    private void showRestorePasswordDialog() {
        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_lock_reset});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog.SingleButtonCallback positiveCallback =
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        assert view != null;
                        EmailInputLayout emailLayout = view
                                .findViewById(R.id.input_layout_email);

                        if (emailLayout.validateEmail()) {
                            sendPasswordResetEmail(emailLayout.getEmailText());
                            dialog.dismiss();
                        } else emailLayout.showErrorLabel();
                    }
                };
        MaterialDialog.SingleButtonCallback negativeCallback =
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                };
        new MaterialDialog.Builder(getContext())
                .customView(R.layout.dialog_auth_reset_password, true)
                .title(R.string.auth_reset_password)
                .positiveText(R.string.auth_reset_password)
                .negativeText(android.R.string.cancel)
                .iconRes(iconDrawableRes)
                .autoDismiss(false)
                .onPositive(positiveCallback)
                .onNegative(negativeCallback)
                .show();
    }

    private void showRegisterDialog() {
        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_account_plus});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog.SingleButtonCallback positiveCallback =
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        assert view != null;
                        SignUpLayout sul = view
                                .findViewById(R.id.sign_up_layout);

                        if (sul.validate()) {
                            signUpWithEmail(sul.getEmailText(), sul.getPasswordText());
                            dialog.dismiss();
                        }
                    }
                };
        MaterialDialog.SingleButtonCallback negativeCallback =
                new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                };
        MaterialDialog md = new MaterialDialog.Builder(getContext())
                .customView(R.layout.dialog_auth_register, true)
                .title(R.string.auth_register)
                .positiveText(R.string.auth_register)
                .negativeText(android.R.string.cancel)
                .iconRes(iconDrawableRes)
                .autoDismiss(false)
                .onPositive(positiveCallback)
                .onNegative(negativeCallback)
                .build();

        View v = md.getCustomView();
        assert v != null;

        // Pull email from current activity
        String email = mSignInView.getEmailText();
        //noinspection StatementWithEmptyBody
        if (TextUtils.isEmpty(email)) {
        } else {
            EditText editText = v.findViewById(R.id.input_email);
            editText.setText(email);
        }

        // Pull password from current activity
        String password = mSignInView.getPasswordText();
        //noinspection StatementWithEmptyBody
        if (TextUtils.isEmpty(password)) {
        } else {
            EditText editText = v.findViewById(R.id.input_password);
            editText.setText(password);
        }

        md.show();
    }

    // --------------------------
    // -- AUTH ------------------
    // --------------------------

    private void sendPasswordResetEmail(final @NonNull String email) {
        mAuth.sendPasswordResetEmail(email);
    }

    private void signUpWithEmail(String emailText, String passwordText) {
        Intent intent = new Intent(getActivity(), AuthSlaveActivity.class);
        intent.putExtra(AuthSlaveActivity.AUTH_TYPE, AuthSlaveActivity.AUTH_SIGN_UP);
        intent.putExtra(AuthSlaveActivity.AUTH_EMAIL, emailText);
        intent.putExtra(AuthSlaveActivity.AUTH_PASSWORD, passwordText);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_SIGN_UP_EMAIL);
    }

    private void signWithEmail(String emailText, String passwordText) {
        Intent intent = new Intent(getActivity(), AuthSlaveActivity.class);
        intent.putExtra(AuthSlaveActivity.AUTH_TYPE, AuthSlaveActivity.AUTH_SIGN_IN);
        intent.putExtra(AuthSlaveActivity.AUTH_EMAIL, emailText);
        intent.putExtra(AuthSlaveActivity.AUTH_PASSWORD, passwordText);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN_EMAIL);
    }

    private void signWithGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApi);
        startActivityForResult(signInIntent, REQUEST_CODE_SIGN_IN_GOOGLE);
    }

}
