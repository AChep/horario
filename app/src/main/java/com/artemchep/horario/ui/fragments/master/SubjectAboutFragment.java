package com.artemchep.horario.ui.fragments.master;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.database.models.Subject;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class SubjectAboutFragment extends Fragment implements View.OnClickListener {

    private String mShareableCode;
    private Subject mSubject;

    private TextView mShareableCodeTextView;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_subject_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        View code = view.findViewById(R.id.code);
        code.findViewById(R.id.code_copy).setOnClickListener(this);
        mShareableCodeTextView = (TextView) code.findViewById(R.id.code_label);
    }

    @Override
    public void onResume() {
        super.onResume();
        bind(mSubject);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.code_copy: {
                if (mShareableCode == null) {
                    Timber.wtf("Copying non-existent shareable code.");
                    return; // should never happen
                }

                // Copy email to clipboard
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Shareable code", mShareableCode);
                clipboard.setPrimaryClip(clip);

                // Show toast message
                String msg = getString(R.string.subject_about_code_copied);
                Toasty.info(getContext(), msg).show();
                break;
            }
        }
    }

    public void setSubject(@NonNull Subject subject) {
        mSubject = subject;

        if (isResumed()) {
            bind(mSubject);
        }
    }

    public void bind(@NonNull Subject subject) {
        mShareableCode = "test";
        String label = getString(R.string.subject_about_code_label, mShareableCode);
        mShareableCodeTextView.setText(label);
    }

}