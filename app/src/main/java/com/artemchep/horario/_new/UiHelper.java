package com.artemchep.horario._new;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;

import com.artemchep.horario.R;
import com.thebluealliance.spectrum.internal.ColorUtil;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Artem Chepurnoy
 */
public class UiHelper {

    /**
     * A description that should be displayed before actual label is loaded. This should be displayed
     * instead of the key of model.
     */
    @NonNull
    public static final String TEXT_PLACEHOLDER = "• • •";

    public static void updateTextInputLayout(@NonNull TextInputLayout layout, @ColorInt int color) {
        boolean isColorDark = ColorUtil.isColorDark(color);

        int editTextColor;
        int editTextBgColor;
        int hintTextAppearance;
        if (isColorDark) {
            editTextColor = Color.WHITE;
            editTextBgColor = 0x88FFFFFF;
            hintTextAppearance = R.style.TextInputLayoutAppearance_White;
        } else {
            editTextColor = Color.BLACK;
            editTextBgColor = 0x88000000;
            hintTextAppearance = R.style.TextInputLayoutAppearance_Black;
        }

        EditText editText = layout.getEditText();
        editText.setTextColor(editTextColor);
        ViewCompat.setBackgroundTintList(editText, ColorStateList.valueOf(editTextBgColor));
        layout.setHintTextAppearance(hintTextAppearance);

        try {
            Field field = TextInputLayout.class.getDeclaredField("mDefaultTextColor");
            field.setAccessible(true);
            field.set(layout, ColorStateList.valueOf(editTextBgColor));

            Method method = TextInputLayout.class.getDeclaredMethod("updateLabelState", boolean.class, boolean.class);
            method.setAccessible(true);
            method.invoke(layout, false, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void updateToolbarTitle(@NonNull Toolbar toolbar, @ColorInt int color) {
        boolean isColorDark = ColorUtil.isColorDark(color);
        toolbar.setTitleTextColor(isColorDark ? Color.WHITE : Color.BLACK);
    }

    public static void updateCollapsingToolbarTitle(@NonNull CollapsingToolbarLayout toolbar, @ColorInt int color) {
        boolean isColorDark = ColorUtil.isColorDark(color);
        if (isColorDark) {
            toolbar.setExpandedTitleColor(Color.WHITE);
            toolbar.setCollapsedTitleTextColor(Color.WHITE);
        } else {
            toolbar.setExpandedTitleColor(Color.BLACK);
            toolbar.setCollapsedTitleTextColor(Color.BLACK);
        }
    }

    public static void updateToolbarBackIcon(@NonNull Toolbar toolbar, @ColorInt int color) {
        boolean isColorDark = ColorUtil.isColorDark(color);
        toolbar.setNavigationIcon(isColorDark
                ? R.drawable.ic_arrow_left_white_24dp
                : R.drawable.ic_arrow_left_black_24dp);
    }

    public static void updateToolbarCloseIcon(@NonNull Toolbar toolbar, @ColorInt int color) {
        boolean isColorDark = ColorUtil.isColorDark(color);
        toolbar.setNavigationIcon(isColorDark
                ? R.drawable.ic_close_white_24dp
                : R.drawable.ic_close_black_24dp);
    }

    public static void updateToolbarMenuIcons(@NonNull Toolbar toolbar, @ColorInt int color, int... items) {
        boolean isColorDark = ColorUtil.isColorDark(color);
        for (int id : items) {
            MenuItem menuItem = toolbar.getMenu().findItem(id);
            switch (id) {
                case R.id.action_due:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_clock_white_24dp
                            : R.drawable.ic_clock_black_24dp);
                    break;
                case R.id.action_edit:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_pencil_white_24dp
                            : R.drawable.ic_pencil_black_24dp);
                    break;
                case R.id.action_share:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_share_variant_white_24dp
                            : R.drawable.ic_share_variant_black_24dp);
                    break;
                case R.id.action_palette:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_palette_white_24dp
                            : R.drawable.ic_palette_black_24dp);
                    break;
                case R.id.action_add_editor:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_account_plus_white_24dp
                            : R.drawable.ic_account_plus_black_24dp);
                    break;
                case R.id.action_archive:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_archive_black_24dp
                            : R.drawable.ic_archive_black_24dp);
                    break;
                case R.id.action_restore:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_unarchive_black_24dp
                            : R.drawable.ic_unarchive_black_24dp);
                    break;
                case R.id.action_remind:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_clock_white_24dp
                            : R.drawable.ic_clock_black_24dp);
                    break;
                case R.id.action_delete:
                    menuItem.setIcon(isColorDark
                            ? R.drawable.ic_delete_white_24dp
                            : R.drawable.ic_delete_black_24dp);
                    break;
            }
        }
        toolbar.setOverflowIcon(ContextCompat.getDrawable(toolbar.getContext(), isColorDark
                ? R.drawable.ic_dots_vertical_white_24dp
                : R.drawable.ic_dots_vertical_black_24dp));
    }

}
