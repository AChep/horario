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
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Patterns;
import android.view.View;

import com.artemchep.horario.R;

/**
 * @author Artem Chepurnoy
 */
public class EmailInputLayout extends TextInputLayout {

    private TextInputEditText mEditText;

    public EmailInputLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmailInputLayout(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mEditText = (TextInputEditText) findViewById(R.id.input_email);
        mEditText.addTextChangedListener(new Watcher(mEditText));
    }

    public boolean validateEmail() {
        String email = getEmailText();
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void showErrorLabel() {
        if (isErrorEnabled()) {
            return;
        }

        String msg = getResources().getString(R.string.auth_error_invalid_email);
        setError(msg);
    }

    public void hideErrorLabel() {
        setErrorEnabled(false);
    }

    @NonNull
    public String getEmailText() {
        return mEditText.getText().toString().trim();
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
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (viewId) {
                case R.id.input_email:
                    if (validateEmail()) {
                        hideErrorLabel();
                    } else showErrorLabel();
                    break;
            }
        }
    }

}
