package com.artemchep.horario.ui;

import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.ChildActivity;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.richeditor.RichEditor;

/**
 * @author Artem Chepurnoy
 */
public class FormatHelper implements View.OnClickListener {

    private static final String ACTION_TEXT_COLOR = "text_color";

    private static final int[] IDS = {
            R.id.format_undo,
            R.id.format_redo,
            R.id.format_bold,
            R.id.format_underline,
            R.id.format_link_insert,
            R.id.format_italic,
            R.id.format_strike,
            R.id.format_bullets,
            R.id.format_numbers,
            R.id.format_clear,
            R.id.format_color_text,
            R.id.format_header_1,
            R.id.format_header_2,
            R.id.format_header_3,
            R.id.format_header_4,
            R.id.format_header_5,
            R.id.format_header_6,
    };

    private final String mTag;
    private final Activity mActivity;
    private final List<View> mViews;

    private RichEditor mRichEditor;

    public FormatHelper(@NonNull Activity activity, @NonNull String tag) {
        mTag = tag;
        mActivity = activity;
        mViews = new ArrayList<>();
    }

    public void start() {
        ChildActivity activity = (ChildActivity) mActivity;
        activity.addFormatHelper(this);
    }

    public void stop() {
        ChildActivity activity = (ChildActivity) mActivity;
        activity.removeFormatHelper(this);
    }

    public void init(@NonNull RichEditor editor, View panel) {
        mRichEditor = editor;

        for (int id : IDS) {
            View view = panel.findViewById(id);
            view.setOnClickListener(this);
            mViews.add(view);
        }
    }

    public void setEnabled(boolean enabled) {
        float alpha = enabled ? 1f : 0.4f;
        for (View view : mViews) {
            view.setEnabled(enabled);
            view.setAlpha(alpha);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.format_undo:
                mRichEditor.undo();
                break;
            case R.id.format_redo:
                mRichEditor.redo();
                break;
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
            case R.id.format_clear:
                mRichEditor.removeFormat();
                break;
            case R.id.format_color_text: {
                ChildActivity activity = (ChildActivity) v.getContext();
                new ColorChooserDialog.Builder(activity, R.string.dialog_changelog_title)
                        .allowUserColorInput(true)
                        .backButton(android.R.string.cancel)
                        .doneButton(android.R.string.ok)
                        .tag(getTag() + ACTION_TEXT_COLOR)
                        .show(activity);
                break;
            }
        }
    }

    public void onColorSelection(@NonNull String tag, @ColorInt int selectedColor) {
        switch (tag) {
            case ACTION_TEXT_COLOR:
                mRichEditor.setTextColor(selectedColor);
                break;
        }
    }

    @NonNull
    public String getTag() {
        return mTag;
    }

}
