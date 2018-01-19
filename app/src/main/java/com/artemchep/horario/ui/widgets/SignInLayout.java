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
package com.artemchep.horario.ui.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public class SignInLayout extends LinearLayout {

    private static final String BUNDLE_EMAIL = "email";
    private static final String BUNDLE_PASSWORD = "password";

    private EmailInputLayout mEmailLayout;
    private TextInputLayout mPasswordInputLayout;
    private TextInputEditText mPasswordEditText;

    public SignInLayout(Context context) {
        super(context);
    }

    public SignInLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEmailLayout = findViewById(R.id.input_layout_email);
        mPasswordEditText = findViewById(R.id.input_password);
        mPasswordInputLayout = findViewById(R.id.input_layout_password);

        mPasswordEditText.addTextChangedListener(new Watcher(mPasswordEditText));
    }

    /**
     * Validates email and shows errors if any.
     *
     * @return {@code true} if email is valid, {@code false} otherwise.
     * @see #validatePassword()
     * @see #validate()
     */
    protected boolean validateEmail() {
        return mEmailLayout.validateEmail();
    }

    /**
     * Validates password and shows errors if any.
     *
     * @return {@code true} if password is valid, {@code false} otherwise.
     * @see #validateEmail()
     * @see #validate()
     */
    protected boolean validatePassword() {
        Resources res = getResources();
        String password = getPasswordText();
        StringBuilder sb = new StringBuilder();
        // Validate password
        if (password.length() < 8)
            sb.append(res.getString(R.string.auth_error_passwd_length, 8));
        // Ui
        String errorMessage = sb.toString();
        if (errorMessage.length() > 0) {
            mPasswordEditText.requestFocus();
            mPasswordInputLayout.setError(errorMessage);
            return false;
        }
        mPasswordInputLayout.setErrorEnabled(false);
        return true;
    }

    /**
     * @see #validateEmail()
     * @see #validatePassword()
     */
    public boolean validate() {
        return validatePassword() & validateEmail();
    }

    public void clearPassword() {
        mPasswordEditText.setText("");
    }

    @NonNull
    public String getEmailText() {
        return mEmailLayout.getEmailText();
    }

    @NonNull
    public String getPasswordText() {
        return mPasswordEditText.getText().toString().trim();
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Watcher implements TextWatcher {

        private final int viewId;

        private Watcher(View view) {
            this.viewId = view.getId();
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // unused
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            // unused
        }

        public void afterTextChanged(Editable editable) {
            switch (viewId) {
                case R.id.input_password:
                    validatePassword();
                    break;
            }
        }
    }
}
