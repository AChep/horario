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
package com.artemchep.horario.ui.fragments.details;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.DialogHelper;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectDetailsFragment extends MooDetailsFragment<Subject> {

    private static final String TAG = "SubjectDetailsFragment";

    private View mNoteContainer;
    private TextView mNoteTextView;

    @NonNull
    @Override
    protected Class<Subject> getType() {
        return Subject.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mArgsTimetablePath + "/" + Db.SUBJECTS;
    }

    @Override
    protected ViewGroup onCreateContentView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @NonNull List<ContentItem<Subject>> contentItems,
            @Nullable Bundle savedInstanceState) {
        getToolbar().inflateMenu(R.menu.details_subject);

        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_details_subject, container, false);
        initWithFab(R.id.action_edit, R.drawable.ic_pencil_white_24dp);

        mNoteContainer = vg.findViewById(R.id.info_container);
        mNoteTextView = (TextView) mNoteContainer.findViewById(R.id.info);
        contentItems.add(new TextContentItem<Subject>(mNoteContainer, mNoteTextView) {
            @Override
            public String getText(Subject model) {
                return model != null ? model.info : null;
            }
        });

        return vg;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                DialogHelper.showSubjectDialog(getMainActivity(), mArgsTimetablePath, mModel);
                break;
            case R.id.action_add_lesson:
                DialogHelper.showLessonDialog(getMainActivity(), mArgsTimetablePath, null, mModel, null);
                break;
            case R.id.action_add_exam:
                DialogHelper.showExamDialog(getMainActivity(), mArgsTimetablePath, null, mModel, null);
                break;
            case R.id.action_delete:
                mDatabaseRef.setValue(null);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void updateAppBar(@NonNull Subject model) {
        super.updateAppBar(model);
        getHelper().setTitle(model.name);
        getHelper().setAppBarBackgroundColor(model.color);
    }

    @Override
    protected boolean hasAdditionalInfo(@NonNull Subject model) {
        return !TextUtils.isEmpty(model.info);
    }

}
