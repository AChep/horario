package com.artemchep.horario._new.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario._new.activities.SubjectActivity;
import com.artemchep.horario.database.models.SubjectInfo;

import org.sufficientlysecure.htmltextview.HtmlTextView;

/**
 * @author Artem Chepurnoy
 */
public class SubjectAboutFragment extends FragmentBase implements SubjectActivity.Page {

    @Nullable
    private SubjectInfo mSubject;

    private View mInfoContainer;
    private HtmlTextView mInfoTextView;

    /**
     * Displays current state of the fragment: it can be
     * `no additional information` or nothing.
     */
    private TextView mStatusTextView;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_subject_about, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mInfoContainer = view.findViewById(R.id.info_container);
        mInfoTextView = view.findViewById(R.id.info);
        mStatusTextView = view.findViewById(R.id.status);

        bind(mSubject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubject(@Nullable SubjectInfo subject) {
        mSubject = subject;
        bind(subject);
    }

    private void bind(@Nullable SubjectInfo subject) {
        if (getView() == null) {
            // Wait till view is created and set subject
            // once again
            return;
        }

        if (subject == null) {
            // Don't show anything when subject is empty
            mInfoContainer.setVisibility(View.GONE);
            mStatusTextView.setVisibility(View.GONE);
            return;
        }

        if (TextUtils.isEmpty(subject.info)) {
            mStatusTextView.setVisibility(View.VISIBLE);
            mInfoContainer.setVisibility(View.GONE);
        } else {
            mStatusTextView.setVisibility(View.GONE);
            mInfoContainer.setVisibility(View.VISIBLE);
            mInfoTextView.setHtml(subject.info);
        }
    }

    @Override
    public void onFabClick(@NonNull View view) {
    }

    @Override
    public boolean hasFab() {
        return false;
    }

}
