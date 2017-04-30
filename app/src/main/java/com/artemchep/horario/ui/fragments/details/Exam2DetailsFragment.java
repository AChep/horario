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

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.artemchep.basic.utils.PatternUtils;
import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Teacher;
import com.artemchep.horario.models.Timetable;
import com.artemchep.horario.ui.DialogHelper;
import com.google.firebase.crash.FirebaseCrash;
import com.thebluealliance.spectrum.internal.ColorUtil;

import java.sql.Time;
import java.util.List;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;


/**
 * @author Artem Chepurnoy
 */
public abstract class Exam2DetailsFragment extends MooDetailsFragment<Timetable> {
/*
    private static final String TAG = "TimetableDetailsFragment";

    private String mUserId;

    private View mShareContainer;
    private View mShareLinkContainer;
    private TextView mShareLinkTextView;
    private MenuItem mMenuShareItem;

    private boolean mMenuVisible;

    private String mLink;

    @NonNull
    @Override
    protected Class<Timetable> getType() {
        return Timetable.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mArgsTimetablePath + "/" + Db.TIMETABLES;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        // Load theme color
    }

    @Override
    protected ViewGroup onCreateContentView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @NonNull List<ContentItem<Timetable>> contentItems,
            @Nullable Bundle savedInstanceState) {
        getToolbar().inflateMenu(R.menu.details_timetable);
        mMenuShareItem = getToolbar().getMenu().findItem(R.id.action_share);

        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_details_exam, container, false);

        mShareContainer = vg.findViewById(R.id.share_container);
        mShareLinkContainer = vg.findViewById(R.id.share_link_container);
        mShareLinkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLink == null) {
                    return; // should never happen
                }

                // Copy email to clipboard
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Shareable link", mLink);
                clipboard.setPrimaryClip(clip);

                // Show toast message
                String msg = getString(R.string.details_link_clipboard, mLink);
                Toasty.info(getContext(), msg).show();
            }
        });
        mShareLinkTextView = (TextView) mShareLinkContainer.findViewById(R.id.share_link);
        mShareContainer.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLink == null) {
                    return; // should never happen
                }

                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    i.putExtra(Intent.EXTRA_TEXT, "Horario shared timetable: " + mLink);
                    startActivity(Intent.createChooser(i, getString(R.string.share_link_via)));
                } catch (Exception e) {
                    FirebaseCrash.report(new Exception("Failed to share timetable.", e));
                }
            }
        });

        return vg;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                DialogHelper.showTeacherDialog(getMainActivity(), mArgsTimetablePath, mModel);
                break;
            case R.id.action_add_lesson:
                DialogHelper.showLessonDialog(getMainActivity(), mArgsTimetablePath, null, null, mModel);
                break;
            case R.id.action_add_exam:
                DialogHelper.showExamDialog(getMainActivity(), mArgsTimetablePath, null, null, mModel);
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
    protected void updateAll() {
        super.updateAll();

        // Display share link
        if (mLink == null) {
            mShareContainer.setVisibility(View.GONE);
            mShareLinkContainer.setVisibility(View.GONE);
        } else {
            mShareContainer.setVisibility(View.VISIBLE);
            mShareLinkContainer.setVisibility(View.VISIBLE);
            mShareLinkTextView.setText(mLink);
        }
    }

    @Override
    protected void updateAppBar(@NonNull Timetable model) {
        super.updateAppBar(model);
        getHelper().setTitle(model.name);
        getHelper().setAppBarBackgroundColor(Palette.GREY);
    }

    @Override
    protected boolean hasAdditionalInfo(@NonNull Timetable model) {
        return !TextUtils.isEmpty(model.info);
    }*/

}
