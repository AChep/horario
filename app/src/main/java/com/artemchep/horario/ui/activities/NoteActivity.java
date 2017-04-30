package com.artemchep.horario.ui.activities;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.Device;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.basic.utils.HtmlUtils;
import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Address;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Note;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.drawables.ChevronIconDrawable;
import com.artemchep.horario.ui.widgets.SwipeBackLayout;
import com.artemchep.horario.utils.DateUtilz;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thebluealliance.spectrum.internal.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.wasabeef.richeditor.RichEditor;

/**
 * @author Artem Chepurnoy
 */
public class NoteActivity extends ActivityBase implements
        SwipeBackLayout.SwipeBackListener,
        SwipeBackLayout.FinishListener,
        View.OnClickListener, Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_TIMETABLE_PATH_PUBLIC = "extra::timetable_path::public";
    public static final String EXTRA_NOTE = "note";

    private static final String SAVED_FORMAT_PANEL_SHOWN = "saved::format_panel::shown";

    private Subject mUnknownSubject = new Subject();
    private String mTimetablePath;
    private Note mNote;

    {
        mUnknownSubject.name = "unknown";
        mUnknownSubject.color = Color.GRAY;
    }

    private Toolbar mToolbar;
    private SwipeBackLayout mSwipeBackLayout;
    private ColorDrawable mColorDrawable;
    private TextView mTimeTextView;
    private RichEditor mRichEditor;
    private AppCompatEditText mEditTextTitle;
    private View mFormatPanel;
    private ImageButton mChevronButton;
    private ViewGroup mSubjectViewGroup;

    private boolean mIsFormatPanelShown;
    private ChevronIconDrawable mChevronDrawable;

    private String[] mMonths;

    /**
     * Key is the key of subject,
     * value is the subject view
     */
    private HashMap<String, TextView> mHashMapView = new HashMap<>();

    private Persy mPersy = new Persy();
    private Persy.Watcher<Subject> mWatcherSubjects;

    @NonNull
    private final ChildEventListener mWatcherSubjectsListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            TextView tv = mHashMapView.get(subject.key);

            if (tv != null) {
                bindSubject(tv, subject);
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            TextView tv = mHashMapView.get(subject.key);

            if (tv != null) {
                bindSubject(tv, subject);
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            TextView tv = mHashMapView.get(key);

            if (tv != null) {
                bindSubject(tv, null);
            }
        }
    };

    @NonNull
    private final View.OnClickListener mSubjectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            removing_key:
            {
                TextView tv = (TextView) v;
                for (Map.Entry<String, TextView> entry : mHashMapView.entrySet()) {
                    if (tv == entry.getValue()) {
                        mHashMapView.remove(entry.getKey());
                        break removing_key;
                    }
                }

                // no corresponding key found
                return;
            }

            // Animatedly remove subject view
            if (ViewCompat.isLaidOut(mSubjectViewGroup)) {
                TransitionManager.beginDelayedTransition(mSubjectViewGroup);
            }
            mSubjectViewGroup.removeView(v);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Apply current theme
        switch (Config.getInstance().getInt(Config.KEY_UI_THEME)) {
            case Config.THEME_LIGHT:
                setTheme(R.style.AppThemeLight_NoActionBar_Translucent);
                break;
            case Config.THEME_DARK:
                setTheme(R.style.AppTheme_NoActionBar_Translucent);
                break;
            case Config.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_NoActionBar_Translucent);
                break;
        }

        mPersy.start();

        // Get timetable data
        Bundle args = getIntent().getExtras();
        if (args == null) {
            args = new Bundle();
        }
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);
        String timetablePathPublic = args.getString(EXTRA_TIMETABLE_PATH_PUBLIC);

        // Load extras
        if (savedInstanceState != null) {
            // Note must not be null here!
            //noinspection ConstantConditions
            mNote = savedInstanceState.getParcelable(EXTRA_NOTE);
        } else {
            Note note = args.getParcelable(EXTRA_NOTE);
            if (note != null) {
                mNote = note;
            } else mNote = new Note();
        }

        if (mTimetablePath == null) {
            Config config = Config.getInstance();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            Address address = Address.fromString(config.getString(Config.KEY_ADDRESS));

            if (user == null || address == null) {
                mTimetablePath = "god";
            } else {
                timetablePathPublic = Db.user(address.publicUserKey)
                        .timetablePublic(address.publicKey)
                        .path();
                mTimetablePath = Db.user(user.getUid())
                        .timetablePrivate(address.privateKey)
                        .path();
            }
        }

        mWatcherSubjects = mPersy.watchFor(Subject.class, timetablePathPublic + "/" + Db.SUBJECTS);

        setContentView(R.layout.activity_note);

        // Load attrs
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{
                R.attr.is_dark_theme,
                R.attr.bg_card_color,
        });
        boolean isDarkTheme = a.getBoolean(0, false);
        int cardColor = a.getColor(1, Color.WHITE);
        a.recycle();

        mColorDrawable = new ColorDrawable(Color.BLACK);
        View view = findViewById(R.id.container);
        view.setBackground(mColorDrawable);
        initSwipeBackLayout();
        initToolbar();

        mMonths = getResources().getStringArray(R.array.months);
        mTimeTextView = (TextView) findViewById(R.id.time);
        mTimeTextView.setOnClickListener(this);
        mRichEditor = (RichEditor) findViewById(R.id.editor);
        mRichEditor.setPlaceholder(getString(R.string.hint_content));
        mRichEditor.setEditorFontColor(isDarkTheme ? 0xFFeeeeee : 0xFF333333);
        mRichEditor.setEditorBackgroundColor(cardColor);
        mEditTextTitle = (AppCompatEditText) findViewById(R.id.title);
        mSubjectViewGroup = (ViewGroup) findViewById(R.id.subject_group);
        findViewById(R.id.subject_add).setOnClickListener(this);

        // Setup format layout
        mChevronDrawable = new ChevronIconDrawable();
        mChevronDrawable.setColor(isDarkTheme ? Color.WHITE : 0xFF757575);
        mChevronButton = (ImageButton) findViewById(R.id.chevron);
        mChevronButton.setOnClickListener(this);
        mChevronButton.setImageDrawable(mChevronDrawable);
        mFormatPanel = findViewById(R.id.format);
        mFormatPanel.findViewById(R.id.format_bold).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_underline).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_link_insert).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_italic).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_strike).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_bullets).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_numbers).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_header_1).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_header_2).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_header_3).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_header_4).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_header_5).setOnClickListener(this);
        mFormatPanel.findViewById(R.id.format_header_6).setOnClickListener(this);

        // Load format panel state
        if (savedInstanceState != null) {
            boolean shown = savedInstanceState.getBoolean(SAVED_FORMAT_PANEL_SHOWN);
            setFormatPanelShown(shown);
        } else {
            setFormatPanelShown(false);
        }

        // Load title and content text
        setTime(mNote.due);
        mEditTextTitle.setText(mNote.title);
        mRichEditor.setHtml(mNote.contentHtml == null ? mNote.content : mNote.contentHtml);
        // Load subjects
        LayoutInflater inflater = getLayoutInflater();
        if (mNote.subjects != null) for (String key : mNote.subjects) {
            TextView v = (TextView) inflater.inflate(R.layout.item_note_subject, mSubjectViewGroup, false);
            v.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_item_note_subject).mutate());
            v.setOnClickListener(mSubjectOnClickListener);
            mHashMapView.put(key, v);
            mSubjectViewGroup.addView(v, mSubjectViewGroup.getChildCount() - 1);
        }

        if (Device.hasLollipopApi()) {
            //noinspection NewApi
            fixTransitionGlitch();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void fixTransitionGlitch() {
        Transition transition = getWindow().getSharedElementEnterTransition();
        if (transition == null) {
            return;
        }

        transition.addListener(new Transition.TransitionListener() {

            @Override
            public void onTransitionStart(Transition transition) {
            }

            @Override
            public void onTransitionEnd(Transition transition) {
                requestFixLayout();
            }

            @Override
            public void onTransitionCancel(Transition transition) {
                requestFixLayout();
            }

            @Override
            public void onTransitionPause(Transition transition) {
            }

            @Override
            public void onTransitionResume(Transition transition) {
            }

            private void requestFixLayout() {
                mEditTextTitle.requestLayout();
            }
        });
    }

    private void setTime(int time) {
        if (time > 0) {
            int day = DateUtilz.getDay(time);
            int month = DateUtilz.getMonth(time);
            mTimeTextView.setText(mMonths[month] + " " + day);
            mTimeTextView.setVisibility(View.VISIBLE);
        } else mTimeTextView.setVisibility(View.GONE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onViewPositionChanged(0, 0);
    }

    private void initSwipeBackLayout() {
        mSwipeBackLayout = (SwipeBackLayout) findViewById(R.id.swipe_back_layout);
        mSwipeBackLayout.setOnSwipeBackListener(this);
        mSwipeBackLayout.setOnFinishListener(this);
        mSwipeBackLayout.setDragEdge(SwipeBackLayout.DragEdge.TOP);
        mSwipeBackLayout.setSwipeBackEnabled(!isTaskRoot());
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.note);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWatcherSubjects.addListener(mWatcherSubjectsListener);

        for (Map.Entry<String, TextView> entry : mHashMapView.entrySet()) {
            Subject subject = mWatcherSubjects.getMap().get(entry.getKey());
            bindSubject(entry.getValue(), subject);
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save: {
                updateCurrentNote();

                // Push changes
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference(mTimetablePath)
                        .child(Db.NOTES);
                if (mNote.key == null) {
                    // Create priority text
                    long now = System.currentTimeMillis();
                    String priority = Long.toString(now, Character.MAX_RADIX);
                    int size = priority.length();
                    if (size < 14) {
                        char[] c = new char[14 - size];
                        Arrays.fill(c, '0');
                        priority = new String(c) + priority;
                    }

                    // Create new note
                    mNote.priority = priority;
                    ref.push().setValue(mNote);
                } else ref.child(mNote.key).setValue(mNote);

                finish();
                break;
            }
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.time:
                break;
            case R.id.subject_add: {
                final List<Subject> subjects = new ArrayList<>(mWatcherSubjects.getMap().values());

                // Filter already added subjects
                // to avoid duplicates.
                for (int i = subjects.size() - 1; i >= 0; i--) {
                    if (mHashMapView.containsKey(subjects.get(i).key)) {
                        subjects.remove(i);
                    }
                }

                if (subjects.isEmpty()) {
                    Toast.makeText(NoteActivity.this, "No subjects to add", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Form popup menu and show it
                PopupMenu popup = new PopupMenu(NoteActivity.this, v);
                int i = 0;
                for (Subject subject : subjects) {
                    MenuItem item = popup.getMenu().add(0, i++, 0, subject.name);
                    item.setActionView(R.layout.item_note_subject_popup);
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        TextView tv = (TextView) getLayoutInflater().inflate(
                                R.layout.item_note_subject, mSubjectViewGroup, false);
                        tv.setBackground(ContextCompat
                                .getDrawable(NoteActivity.this, R.drawable.bg_item_note_subject)
                                .mutate());
                        tv.setOnClickListener(mSubjectOnClickListener);
                        Subject subject = subjects.get(item.getItemId());
                        bindSubject(tv, subject);
                        mHashMapView.put(subject.key, tv);

                        // Animatedly add new subject view
                        if (ViewCompat.isLaidOut(mSubjectViewGroup)) {
                            TransitionManager.beginDelayedTransition(mSubjectViewGroup);
                        }
                        mSubjectViewGroup.addView(tv, mSubjectViewGroup.getChildCount() - 1);
                        return true;
                    }
                });
                popup.show();
                break;
            }
            case R.id.chevron:
                setFormatPanelShown(!mIsFormatPanelShown);
                break;
            case R.id.format_link_insert: {
                MaterialDialog md = new MaterialDialog.Builder(this)
                        .customView(R.layout.dialog_link_insert, true)
                        .title("Insert link")
                        .positiveText(R.string.dialog_insert)
                        .negativeText(android.R.string.cancel)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(
                                    @NonNull MaterialDialog dialog,
                                    @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE: {
                                        View view = dialog.getCustomView();
                                        assert view != null;
                                        TextInputEditText titleTiet = (TextInputEditText) view.findViewById(R.id.input_name);
                                        TextInputLayout linkTil = (TextInputLayout) view.findViewById(R.id.input_layout_link);
                                        TextInputEditText linkTiet = (TextInputEditText) linkTil.findViewById(R.id.input_link);

                                        String link = linkTiet.getText().toString();
                                        if (TextUtils.isEmpty(link)) {
                                            return;
                                        }

                                        String title = titleTiet.getText().toString();
                                        if (TextUtils.isEmpty(title)) title = link;
                                        mRichEditor.insertLink(link, title);
                                        break;
                                    }
                                    case NEGATIVE:
                                        break;
                                }

                                dialog.dismiss();
                            }
                        })
                        .autoDismiss(false)
                        .build();

                md.show();
                break;
            }
            case R.id.format_bold:
                mRichEditor.setBold();
                break;
            case R.id.format_italic:
                mRichEditor.setItalic();
                break;
            case R.id.format_underline:
                mRichEditor.setUnderline();
                break;
            case R.id.format_strike:
                mRichEditor.setStrikeThrough();
                break;
            case R.id.format_header_1:
                mRichEditor.setHeading(1);
                break;
            case R.id.format_header_2:
                mRichEditor.setHeading(2);
                break;
            case R.id.format_header_3:
                mRichEditor.setHeading(3);
                break;
            case R.id.format_header_4:
                mRichEditor.setHeading(4);
                break;
            case R.id.format_header_5:
                mRichEditor.setHeading(5);
                break;
            case R.id.format_header_6:
                mRichEditor.setHeading(6);
                break;
            case R.id.format_bullets:
                mRichEditor.setBullets();
                break;
            case R.id.format_numbers:
                mRichEditor.setNumbers();
                break;
        }
    }

    private void setFormatPanelShown(boolean shown) {
        View view = getWindow().getDecorView();
        boolean rtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
        boolean chevronPointingToRight = shown != rtl;

        if (!isPowerSaveMode()) {
            if (chevronPointingToRight) {
                mChevronDrawable.transformToRight();
            } else mChevronDrawable.transformToLeft();
        } else if (chevronPointingToRight) {
            mChevronDrawable.transformToShape(ChevronIconDrawable.POINTING_RIGHT, false);
        } else mChevronDrawable.transformToShape(ChevronIconDrawable.POINTING_LEFT, false);

        if (shown) {
            mFormatPanel.setVisibility(View.VISIBLE);
        } else mFormatPanel.setVisibility(View.GONE);

        mIsFormatPanelShown = shown;
    }

    @Override
    public void onViewPositionChanged(float fractionAnchor, float fractionScreen) {
        float alpha = (1 - fractionScreen) * 0.4f;
        mColorDrawable.setAlpha(Math.round(alpha * 255));
    }

    @Override
    public void onFinish() {
        // This improves shared transition on
        // tablets, because of toolbar location.
        mToolbar.setVisibility(View.INVISIBLE);
        supportFinishAfterTransition();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWatcherSubjects.removeListener(mWatcherSubjectsListener);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateCurrentNote();
        outState.putBoolean(SAVED_FORMAT_PANEL_SHOWN, mIsFormatPanelShown);
        outState.putParcelable(EXTRA_NOTE, mNote);
    }

    private void updateCurrentNote() {
        mNote.title = getTitleText();
        mNote.contentHtml = getContentText();
        mNote.content = HtmlUtils.fromLegacyHtml(mNote.contentHtml).toString();
        Collection<String> collection = mHashMapView.keySet();
        if (collection.isEmpty()) {
            if (mNote.subjects == null) {
                mNote.subjects = new ArrayList<>();
            } else mNote.subjects.clear();
        } else {
            if (mNote.subjects == null) {
                mNote.subjects = new ArrayList<>();
            } else mNote.subjects.clear();
            mNote.subjects.addAll(collection);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPersy.stop();
    }

    @NonNull
    private String getTitleText() {
        return mEditTextTitle.getText().toString();
    }

    @NonNull
    private String getContentText() {
        return mRichEditor.getHtml();
    }

    private void bindSubject(@NonNull TextView tv, @Nullable Subject subject) {
        if (subject == null) {
            subject = mUnknownSubject;
        }

        int colorBg = subject.color;
        int colorText = ColorUtil.isColorDark(colorBg) ? Color.WHITE : Color.BLACK;

        tv.setText(subject.name);
        tv.setTextColor(colorText);
        GradientDrawable drawable = (GradientDrawable) tv.getBackground().getCurrent();
        drawable.setColor(colorBg);
    }

}
