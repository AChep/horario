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
import com.artemchep.horario.ui.DialogHelper;
import com.thebluealliance.spectrum.internal.ColorUtil;

import java.util.List;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;


/**
 * @author Artem Chepurnoy
 */
public class TeacherDetailsFragment extends MooDetailsFragment<Teacher> {

    private static final String TAG = "TeacherDetailsFragment";

    private View mEmailContainer;
    private View mPhoneContainer;
    private View mNoteContainer;
    private TextView mEmailTextView;
    private TextView mPhoneTextView;
    private TextView mNoteTextView;
    private Button mPhoneButton;
    private Button mEmailButton;

    private boolean mIsDarkTheme;

    @NonNull
    @Override
    protected Class<Teacher> getType() {
        return Teacher.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mArgsTimetablePath + "/" + Db.TEACHERS;
    }

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        // Load theme color
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.is_dark_theme});
        mIsDarkTheme = a.getBoolean(0, false);
        a.recycle();
    }

    @Override
    protected ViewGroup onCreateContentView(
            @NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @NonNull List<ContentItem<Teacher>> contentItems,
            @Nullable Bundle savedInstanceState) {
        getToolbar().inflateMenu(R.menu.details_teacher);

        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_details_teacher, container, false);
        initWithFab(R.id.action_edit, R.drawable.ic_pencil_white_24dp);

        mNoteContainer = vg.findViewById(R.id.info_container);
        mPhoneContainer = vg.findViewById(R.id.phone_container);
        mPhoneContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mModel.phone)) {
                    Timber.tag(TAG).w("Tried to copy an empty phone!");
                    return;
                }
                // Copy email to clipboard
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mModel.phone, mModel.phone); // TODO: More informative description of the clip
                clipboard.setPrimaryClip(clip);

                // Show toast message
                String msg = getString(
                        R.string.details_phone_clipboard,
                        mModel.phone);
                Toasty.info(getContext(), msg).show();
            }
        });
        mEmailContainer = vg.findViewById(R.id.email_container);
        mEmailContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mModel.email)) {
                    Timber.tag(TAG).w("Tried to copy an empty email!");
                    return;
                }
                // Copy email to clipboard
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(mModel.email, mModel.email); // TODO: More informative description of the clip
                clipboard.setPrimaryClip(clip);

                // Show toast message
                String msg = getString(
                        R.string.details_email_clipboard,
                        mModel.email);
                Toasty.info(getContext(), msg).show();
            }
        });
        mPhoneButton = (Button) mPhoneContainer.findViewById(R.id.phone_send);
        mPhoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mModel.phone)) {
                    Timber.tag(TAG).w("Tried to call an empty phone!");
                    return;
                }

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mModel.phone));

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ignored) {
                }
            }
        });
        mEmailButton = (Button) mEmailContainer.findViewById(R.id.email_send);
        mEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(mModel.email)) {
                    Timber.tag(TAG).w("Tried to send to an empty email!");
                    return;
                }

                String[] recipients = {mModel.email};
                Intent intent = new Intent()
                        .putExtra(Intent.EXTRA_EMAIL, recipients);
                intent.setAction(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); // only email apps should handle it

                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // TODO:!!!!!
                    String msg = "No email app"; //getString(R.string.feedback_error_no_app);
                    Toasty.info(getContext(), msg).show();
                }
            }
        });

        mNoteTextView = (TextView) mNoteContainer.findViewById(R.id.info);
        mEmailTextView = (TextView) mEmailContainer.findViewById(R.id.email);
        mPhoneTextView = (TextView) mPhoneContainer.findViewById(R.id.phone);

        // Note
        contentItems.add(new ContentItem<Teacher>() {
            @Override
            public void onSet(@Nullable Teacher model) {
                if (model == null || TextUtils.isEmpty(model.info)) {
                    mNoteContainer.setVisibility(View.GONE);
                } else {
                    mNoteContainer.setVisibility(View.VISIBLE);
                    mNoteTextView.setText(model.info);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Teacher old, @Nullable Teacher model) {
                String a = old != null ? old.info : null;
                String b = model != null ? model.info : null;
                return !TextUtils.equals(a, b);
            }
        });
        // Email
        contentItems.add(new ContentItem<Teacher>() {
            @Override
            public void onSet(@Nullable Teacher model) {
                if (model == null || TextUtils.isEmpty(model.email)) {
                    mEmailContainer.setVisibility(View.GONE);
                } else {
                    mEmailContainer.setVisibility(View.VISIBLE);
                    mEmailTextView.setText(model.info);

                    if (PatternUtils.isEmail(model.email)) {
                        mEmailButton.setVisibility(View.VISIBLE);
                    } else mEmailButton.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Teacher old, @Nullable Teacher model) {
                String a = old != null ? old.email : null;
                String b = model != null ? model.email : null;
                return !TextUtils.equals(a, b);
            }
        });
        // Phone
        contentItems.add(new ContentItem<Teacher>() {
            @Override
            public void onSet(@Nullable Teacher model) {
                if (model == null || TextUtils.isEmpty(model.phone)) {
                    mPhoneContainer.setVisibility(View.GONE);
                } else {
                    mPhoneContainer.setVisibility(View.VISIBLE);
                    mPhoneTextView.setText(model.phone);

                    if (PatternUtils.isPhone(model.phone)) {
                        mPhoneButton.setVisibility(View.VISIBLE);
                    } else mPhoneButton.setVisibility(View.GONE);
                }
            }

            @Override
            public boolean hasChanged(@Nullable Teacher old, @Nullable Teacher model) {
                String a = old != null ? old.phone : null;
                String b = model != null ? model.phone : null;
                return !TextUtils.equals(a, b);
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
        Teacher model = getModel();

        // Make color a little bit lighter on dark backgrounds
        // and darker on light ones.
        int textAccentColor = model != null ? model.color : Palette.GREY;
        if (ColorUtil.isColorDark(textAccentColor) == mIsDarkTheme) {
            int filter = mIsDarkTheme ? Color.WHITE : Color.BLACK;
            textAccentColor = ColorUtils.blendARGB(textAccentColor, filter, 0.5f);
        }

        mEmailButton.setTextColor(textAccentColor);
        mPhoneButton.setTextColor(textAccentColor);
    }

    @Override
    protected void updateAppBar(@NonNull Teacher model) {
        super.updateAppBar(model);
        getHelper().setTitle(model.name);
        getHelper().setAppBarBackgroundColor(model.color);
    }

    @Override
    protected boolean hasAdditionalInfo(@NonNull Teacher model) {
        return !TextUtils.isEmpty(model.info);
    }

}
