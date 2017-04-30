package com.artemchep.horario.ui;

import android.support.annotation.NonNull;
import android.view.View;

import com.artemchep.horario.R;

import jp.wasabeef.richeditor.RichEditor;

/**
 * @author Artem Chepurnoy
 */
public class FormatHelper implements View.OnClickListener {

    private RichEditor mRichEditor;

    public void init(@NonNull RichEditor editor, View panel) {
        mRichEditor = editor;

        // Setup panel
        panel.findViewById(R.id.format_bold).setOnClickListener(this);
        panel.findViewById(R.id.format_underline).setOnClickListener(this);
        panel.findViewById(R.id.format_link_insert).setOnClickListener(this);
        panel.findViewById(R.id.format_italic).setOnClickListener(this);
        panel.findViewById(R.id.format_strike).setOnClickListener(this);
        panel.findViewById(R.id.format_bullets).setOnClickListener(this);
        panel.findViewById(R.id.format_numbers).setOnClickListener(this);
        panel.findViewById(R.id.format_header_1).setOnClickListener(this);
        panel.findViewById(R.id.format_header_2).setOnClickListener(this);
        panel.findViewById(R.id.format_header_3).setOnClickListener(this);
        panel.findViewById(R.id.format_header_4).setOnClickListener(this);
        panel.findViewById(R.id.format_header_5).setOnClickListener(this);
        panel.findViewById(R.id.format_header_6).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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

}
