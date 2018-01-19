package com.artemchep.horario._new.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.activities.ChildActivity;

/**
 * @author Artem Chepurnoy
 */
public class DialogActivity extends ChildActivity {

    @NonNull
    public static Intent makeFor(
            @NonNull Context context, @NonNull Class<? extends Fragment> fragment,
            @Nullable Bundle args) {
        Intent intent = new Intent(context, DialogActivity.class);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, fragment.getName());
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
        return intent;
    }

    @Override
    protected void setupTheme() {
        int theme = Config.INSTANCE.get(Config.KEY_UI_THEME);
        switch (theme) {
            case Config.THEME_LIGHT:
                setTheme(R.style.AppThemeLight_Dialog);
                break;
            case Config.THEME_DARK:
                setTheme(R.style.AppTheme_Dialog);
                break;
            case Config.THEME_BLACK:
                setTheme(R.style.AppThemeBlack_Dialog);
                break;
        }

        WindowManager.LayoutParams windowManager = getWindow().getAttributes();
        windowManager.dimAmount = 0.62f;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
    }

    @Override
    protected void setupContent() {
        setContentView(R.layout.___activity_dialog);
    }

}
