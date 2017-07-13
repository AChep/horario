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

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.artemchep.basic.tests.Check;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.horario.R;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.database.navigation.Dbb;
import com.artemchep.horario.ui.FormatHelper;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.artemchep.horario._new.fragments.FragmentBase;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

import es.dmoral.toasty.Toasty;
import jp.wasabeef.richeditor.RichEditor;

/**
 * @author Artem Chepurnoy
 */
public class SubjectTaskEditFragment extends FragmentBase implements
        Toolbar.OnMenuItemClickListener,
        View.OnClickListener {

    public static final String EXTRA_SUBJECT_ID = "extra::subject_id";
    public static final String EXTRA_GROUP_ID = "extra::group_id";
    public static final String EXTRA_USER_ID = "extra::user_id";
    public static final String EXTRA_TASK_ID = "extra::task_id";
    public static final String EXTRA_TASK = "extra::task_model";

    private static final String STATE_TASK_INFO = "state::task_info";

    private FormatHelper mFormatHelper;

    private Spinner mTypeSpinner;
    private EditText mEditorTitle;
    private RichEditor mEditor;
    private View mDueContainer;
    private TextView mDueTextView;

    private SubjectTask mSubjectTask;

    private String mSubjectId;
    private String mGroupId;
    private String mTaskId;
    private String mUserId;

    /**
     * Time when back button was pressed, used
     * to ensure user wants to stop editing the task.
     *
     * @see #onBackPressed()
     */
    private long mLastBackPressTime;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ChildActivity activity = (ChildActivity) context;
        mFormatHelper = new FormatHelper(activity, "task_editor::");

        Bundle args = getArguments();
        mSubjectId = args.getString(EXTRA_SUBJECT_ID);
        mGroupId = args.getString(EXTRA_GROUP_ID);
        mTaskId = args.getString(EXTRA_TASK_ID);
        mUserId = args.getString(EXTRA_USER_ID);

        if (mUserId == null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                mUserId = user.getUid();
            } else finishToLogin();
        }

        Check.getInstance().isNonNull(mSubjectId);
        Check.getInstance().isNonNull(mGroupId);

        SubjectTask task = args.getParcelable(EXTRA_TASK);
        if (task != null) {
            mSubjectTask = task.clone();
        } else mSubjectTask = new SubjectTask();
        mSubjectTask.key = mTaskId;
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_subject_task_details_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDueContainer = view.findViewById(R.id.due);
        mDueContainer.setOnClickListener(this);
        mDueTextView = (TextView) mDueContainer.findViewById(R.id.due_text);
        mTypeSpinner = (Spinner) view.findViewById(R.id.spinner);
        mEditorTitle = (EditText) view.findViewById(R.id.title);
        mEditor = (RichEditor) view.findViewById(R.id.editor);
        mFormatHelper.init(mEditor, view.findViewById(R.id.format));

        view.findViewById(R.id.fab).setOnClickListener(this);
        setupToolbar(view);

        // Load attrs
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{
                R.attr.is_dark_theme,
                android.R.attr.colorBackground,
        });
        boolean isDarkTheme = a.getBoolean(0, false);
        int windowColor = a.getColor(1, Color.WHITE);
        a.recycle();

        int editorPadding;
        {
            // RichEditor's padding is a little bit retarded.
            float padding = getResources().getDimension(R.dimen.activity_horizontal_margin);
            float density = getResources().getDisplayMetrics().density;
            editorPadding = Math.round(padding / density);
        }

        mFormatHelper.setEnabled(mEditor.hasFocus());
        mEditor.setPlaceholder(getString(R.string.hint_content));
        mEditor.setEditorFontColor(isDarkTheme ? 0xFFbbbbbb : 0xFF666666);
        mEditor.setEditorBackgroundColor(windowColor);
        mEditor.setPadding(editorPadding, 0, editorPadding, 0);
        mEditor.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // Disable formatting options when editor
                // is not focused.
                mFormatHelper.setEnabled(hasFocus);
            }
        });

        if (savedInstanceState != null) {
            mSubjectTask = savedInstanceState.getParcelable(STATE_TASK_INFO);
        }

        assert mSubjectTask != null;
        bind(mSubjectTask);
    }

    private void setupToolbar(@NonNull View view) {
        // Load attrs
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{
                R.attr.icon_arrow_left_grey,
        });
        int iconCloseGrey = a.getResourceId(0, R.drawable.ic_close_grey600_24dp);
        a.recycle();

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(iconCloseGrey);
        toolbar.inflateMenu(R.menu.subject_task_edit);
        toolbar.setOnMenuItemClickListener(this);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishToParent();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mFormatHelper.start();
    }

    @Override
    public void onStop() {
        mFormatHelper.stop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentTask();
        outState.putParcelable(STATE_TASK_INFO, mSubjectTask);
    }

    @Override
    public boolean onBackPressed() {
        long now = SystemClock.uptimeMillis();
        if (now - mLastBackPressTime > 2000) {
            mLastBackPressTime = now;

            String text = getString(R.string.press_again_to_exit);
            Toasty.custom(getContext(), text, null, Color.WHITE, Toast.LENGTH_SHORT, false).show();
            return true; // eat back press event
        }
        return super.onBackPressed();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_time:
                selectDueTime();
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.due:
                // Clear existing due time
                selectDueTime(0, 0, 0);
                break;
            case R.id.fab:
                publish();
                finish();
                break;
        }
    }

    private void publish() {
        updateCurrentTask();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child(Dbb.SUBJECTS).child(mSubjectId)
                .child(Dbb.SUBJECT_GROUPS).child(mGroupId)
                .child(Dbb.SUBJECT_GROUP_TASK);
        if (mSubjectTask.key == null) {
            ref = ref.push();
            mSubjectTask.key = ref.getKey();
            mSubjectTask.author = mUserId;
        } else ref = ref.child(mSubjectTask.key);

        ref.setValue(mSubjectTask);
    }

    /**
     * Shows {@link DatePickerDialog} and redirects the result of
     * its date selection to {@link #selectDueTime(int, int, int)}.
     */
    private void selectDueTime() {
        int y, m, d;
        if (mSubjectTask.due > 0) {

            // Select previously selected day
            y = DateUtilz.getYear(mSubjectTask.due);
            m = DateUtilz.getMonth(mSubjectTask.due);
            d = DateUtilz.getDay(mSubjectTask.due);
        } else {

            // Select current day
            Calendar calendar = Calendar.getInstance();
            y = calendar.get(Calendar.YEAR);
            m = calendar.get(Calendar.MONTH);
            d = calendar.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectDueTime(year, month, dayOfMonth);
            }
        }, y, m, d);
        datePickerDialog.show();
    }

    private void selectDueTime(int year, int month, int dayOfMonth) {
        int date = DateUtilz.mergeDate(year, month, dayOfMonth);
        mSubjectTask.due = date;

        if (date > 0) {
            String[] months = getResources().getStringArray(R.array.months);

            Calendar calendar = Calendar.getInstance();
            int y = calendar.get(Calendar.YEAR);
            if (y != year) {
                mDueTextView.setText(getString(
                        R.string.subject_stream_task_due_year,
                        months[month], dayOfMonth, year));
            } else mDueTextView.setText(getString(
                    R.string.subject_stream_task_due,
                    months[month], dayOfMonth));

            mDueContainer.setVisibility(View.VISIBLE);
        } else mDueContainer.setVisibility(View.GONE);
    }

    private void updateCurrentTask() {
        final int type;
        switch (mTypeSpinner.getSelectedItemPosition()) {
            case 0:
                type = SubjectTask.TYPE_QUESTION;
                break;
            case 1:
                type = SubjectTask.TYPE_ASSIGNMENT;
                break;
            case 2:
            default:
                type = SubjectTask.TYPE_ANNOUNCEMENT;
                break;
        }

        mSubjectTask.type = type;
        mSubjectTask.title = mEditorTitle.getText().toString().trim();

        String html = mEditor.getHtml();
        mSubjectTask.descriptionHtml = html;
        mSubjectTask.description = HtmlUtils.fromLegacyHtml(html).toString();
    }

    private void bind(@NonNull SubjectTask task) {
        switch (task.type) {
            case SubjectTask.TYPE_QUESTION:
                mTypeSpinner.setSelection(0, false);
                break;
            case SubjectTask.TYPE_ASSIGNMENT:
                mTypeSpinner.setSelection(1, false);
                break;
            case SubjectTask.TYPE_ANNOUNCEMENT:
            default:
                mTypeSpinner.setSelection(2, false);
                break;
        }

        int y, m, d;
        y = DateUtilz.getYear(task.due);
        m = DateUtilz.getMonth(task.due);
        d = DateUtilz.getDay(task.due);
        selectDueTime(y, m, d);

        mEditorTitle.setText(task.title);
        mEditor.setHtml(task.descriptionHtml);
    }

}