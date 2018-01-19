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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public class SignUpLayout extends SignInLayout {

    private TextInputEditText mPasswordConfirmEditText;
    private TextInputLayout mPasswordConfirmInputLayout;

    public SignUpLayout(Context context) {
        super(context);
    }

    public SignUpLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mPasswordConfirmEditText = findViewById(R.id.input_password_confirm);
        mPasswordConfirmInputLayout = findViewById(R.id.input_layout_password_confirm);

        mPasswordConfirmEditText.addTextChangedListener(new Watcher(mPasswordConfirmEditText));
    }

    /**
     * Checks if passwords do match and shows errors if they don't.
     *
     * @return {@code true} if passwords match, {@code false} otherwise.
     * @see #validatePassword()
     * @see #validateEmail()
     * @see #validate()
     */
    protected boolean validatePasswordConfirm() {
        String password = mPasswordConfirmEditText.getText().toString().trim();
        if (!password.equals(getPasswordText())) {
            String msg = getResources().getString(R.string.auth_error_passwd_not_match);
            mPasswordConfirmEditText.requestFocus();
            mPasswordConfirmInputLayout.setError(msg);
            return false;
        }
        mPasswordConfirmInputLayout.setErrorEnabled(false);
        return true;
    }

    /**
     * @see #validateEmail()
     * @see #validatePassword()
     * @see #validatePasswordConfirm()
     */
    public boolean validate() {
        return super.validate() & validatePasswordConfirm();
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
                case R.id.input_password_confirm:
                    validatePasswordConfirm();
                    break;
            }
        }
    }
}
