package com.artemchep.horario.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.artemchep.horario.ui.FormatHelper;
import com.artemchep.horario.ui.activities.ChildActivity;

import jp.wasabeef.richeditor.RichEditor;

/**
 * @author Artem Chepurnoy
 */
public class RichEditorFragment extends FragmentBase {

    public static final String EXTRA_HTML = "extra::html";
    public static final String EXTRA_TITLE = "extra::name";
    public static final String STATE_HTML = "state::html";

    /**
     * User saved his changes
     */
    public static final int RC_SAVED = 2;

    /**
     * User discarded changes
     */
    public static final int RC_DISCARDED = 1;

    /**
     * User did not make any changes
     */
    public static final int RC_UNMODIFIED = 0;

    private RichEditor mEditor;
    private FormatHelper mFormatHelper;

    private String mHtml = "";
    private String mTitle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ChildActivity activity = (ChildActivity) context;
        mFormatHelper = new FormatHelper(activity, "editor::");

        Bundle args = getArguments();
        if (args != null) {
            mHtml = args.getString(EXTRA_HTML, "");
            mTitle = args.getString(EXTRA_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(mTitle);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().setResult(RC_DISCARDED);
                getActivity().supportFinishAfterTransition();
            }
        });

        mEditor = view.findViewById(R.id.editor);
        mFormatHelper.init(mEditor, view.findViewById(R.id.format));

        view.findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isModified()) {
                    Intent data = new Intent();
                    data.putExtra(EXTRA_HTML, mEditor.getHtml());
                    getActivity().setResult(RC_SAVED, data);
                } else getActivity().setResult(RC_UNMODIFIED);
                getActivity().supportFinishAfterTransition();
            }
        });

        // Load attrs
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{
                R.attr.is_dark_theme,
                android.R.attr.colorBackground,
        });
        boolean isDarkTheme = a.getBoolean(0, false);
        int windowColor = a.getColor(1, Color.WHITE);
        a.recycle();

        int editorPaddingH;
        int editorPaddingV;
        {
            // RichEditor's padding is a little bit retarded.
            float paddingH = getResources().getDimension(R.dimen.activity_horizontal_margin);
            float paddingV = getResources().getDimension(R.dimen.activity_vertical_margin);
            float density = getResources().getDisplayMetrics().density;
            editorPaddingH = Math.round(paddingH / density);
            editorPaddingV = Math.round(paddingV / density);
        }

        mEditor.setPlaceholder(getString(R.string.hint_text));
        mEditor.setEditorFontColor(isDarkTheme ? Color.WHITE : Color.BLACK);
        mEditor.setEditorBackgroundColor(windowColor);
        mEditor.setPadding(editorPaddingH, editorPaddingV, editorPaddingH, editorPaddingV);
        mEditor.setHtml(mHtml);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            String html = savedInstanceState.getString(STATE_HTML, mEditor.getHtml());
            mEditor.setHtml(html);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_HTML, mEditor.getHtml());
    }

    @Override
    public boolean onBackPressed() {
        if (isModified()) {
            Intent data = new Intent();
            data.putExtra(EXTRA_HTML, mEditor.getHtml());
            getActivity().setResult(RC_DISCARDED, data);
        }
        return super.onBackPressed();
    }

    /**
     * @return {@code true} if user modified description,
     * {@code false} otherwise.
     */
    private boolean isModified() {
        String html = mEditor.getHtml();
        return !TextUtils.equals(html, mHtml);
    }

}
