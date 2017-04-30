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
package com.artemchep.horario.ui.fragments.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.HorizontalScrollView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.fragments.dialogs.base.DialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.thebluealliance.spectrum.SpectrumPalette;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

import static com.artemchep.horario.Palette.PALETTE;

/**
 * Dialog for editing color components
 * of multiple subjects/teachers passed with {@link #EXTRA_MODEL_ID_ARRAY}.
 * You should put {@link #EXTRA_TIMETABLE_PATH}.
 *
 * @author Artem Chepurnoy
 * @see SubjectDialog
 * @see TeacherDialog
 */
public class PaletteDialog extends DialogFragment implements SpectrumPalette.OnColorSelectedListener {

    public static final String EXTRA_TIMETABLE_PATH = "extra::timetable_path";
    public static final String EXTRA_MODEL_ID_ARRAY = "model:ids";

    public static final String EXTRA_COLOR = "color";
    public static final String EXTRA_HAS_COLOR = "color:has";

    private String mTimetablePath;
    private String[] mModelIds;

    private int mColor;
    private boolean mColorSelected;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedState) {
        Activity activity = getActivity();
        assert activity != null;

        Bundle args = getArguments();
        mTimetablePath = args.getString(EXTRA_TIMETABLE_PATH);
        mModelIds = args.getStringArray(EXTRA_MODEL_ID_ARRAY);

        // Load extras
        Bundle bundle = savedState != null ? savedState : args;
        mColor = bundle.getInt(EXTRA_COLOR);
        mColorSelected = bundle.getBoolean(EXTRA_HAS_COLOR);

        // Load icon
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                new int[]{R.attr.icon_palette});
        int iconDrawableRes = a.getResourceId(0, 0);
        a.recycle();

        MaterialDialog md = new MaterialDialog.Builder(activity)
                .customView(R.layout.dialog_edit_palette, true)
                .iconRes(iconDrawableRes)
                .title(R.string.dialog_subject_palette)
                .negativeText(android.R.string.cancel)
                .positiveText(R.string.dialog_save)
                .onAny(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(
                            @NonNull MaterialDialog dialog,
                            @NonNull DialogAction which) {
                        switch (which) {
                            case POSITIVE:
                                if (!mColorSelected) {
                                    Toasty.error(getContext(), getString(R.string.dialog_subject_error_select_color)).show();
                                    return; // don't dismiss dialog
                                }

                                Map<String, Object> changes = new HashMap<>();
                                for (String key : mModelIds) changes.put(key + "/color", mColor);

                                DatabaseReference dbRef = FirebaseDatabase
                                        .getInstance()
                                        .getReference(mTimetablePath);
                                dbRef.updateChildren(changes);
                                break;
                        }
                        dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        View view = md.getCustomView();
        assert view != null;

        final SpectrumPalette paletteView = (SpectrumPalette) view.findViewById(R.id.palette);
        paletteView.setOnColorSelectedListener(this);
        paletteView.setFixedColumnCount(PALETTE.length);
        paletteView.setColors(PALETTE);

        if (mColorSelected) {
            mColor = Palette.findColorByHue(PALETTE, mColor);
            paletteView.setSelectedColor(mColor);

            // Automatically scroll palette view
            // to ensure that selected item is shown on start
            paletteView.post(new Runnable() {
                @Override
                public void run() {
                    int[] p = PALETTE;
                    int width = paletteView.getMeasuredWidth() / p.length;
                    int pos = 0;

                    for (int i = 0; i < p.length; i++) {
                        if (mColor == p[i]) {
                            pos = i;
                            break;
                        }
                    }

                    HorizontalScrollView scrollView = (HorizontalScrollView) paletteView.getParent();
                    scrollView.scrollBy(width * pos, 0);
                }
            });
        }
        return md;
    }

    @Override
    public void onColorSelected(@ColorInt int color) {
        mColorSelected = true;
        mColor = color;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_HAS_COLOR, mColorSelected);
        outState.putInt(EXTRA_COLOR, mColor);
    }

}
