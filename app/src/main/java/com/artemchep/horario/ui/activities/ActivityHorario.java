package com.artemchep.horario.ui.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.database.Persy;

/**
 * @author Artem Chepurnoy
 */
public class ActivityHorario extends ActivityBase {

    private Persy mPersy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mPersy = Persy.getInstance();
        super.onCreate(savedInstanceState);
        mPersy.start();
    }

    @Override
    protected void onDestroy() {
        mPersy.stop();
        super.onDestroy();
        mPersy = null;
    }

    @NonNull
    public final Persy getPersy() {
        return mPersy;
    }

}
