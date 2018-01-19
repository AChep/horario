package com.artemchep.horario.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.R;
import com.artemchep.horario._new.widgets.ContextualToolbar;

/**
 * @author Artem Chepurnoy
 */
public class ActivityHorario extends ActivityBase {

    private ContextualToolbar mCtb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCtb = new ContextualToolbar();
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar_contextual);
        if (toolbar != null) {
            mCtb.init(toolbar);
        }
    }

    @NonNull
    public final ContextualToolbar getCtb() {
        return mCtb;
    }

}
