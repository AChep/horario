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
package com.artemchep.horario;

import android.content.Context;
import android.os.StrictMode;
import android.support.annotation.NonNull;

import com.artemchep.basic.HeartBase;
import com.artemchep.basic.timber.ReleaseTree;
import com.artemchep.horario.content.PreferenceStore;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.solovyev.android.checkout.Billing;

import es.dmoral.toasty.Toasty;
import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class Heart extends HeartBase {

    public static final String CATEGORY_EXAMS = "com.artemchep.horario.EXAMS";
    public static final String CATEGORY_NOTES = "com.artemchep.horario.NOTES";
    public static final String CATEGORY_LESSONS = "com.artemchep.horario.LESSONS";
    public static final String CATEGORY_NOTIFICATIONS = "com.artemchep.horario.NOTIFICATIONS";

    private static final String TAG = "Heart";

    @NonNull
    public static RefWatcher getRefWatcher(@NonNull Context context) {
        Heart application = (Heart) context.getApplicationContext();
        return application.mRefWatcher;
    }

    @NonNull
    private final Billing mBilling = new Billing(this, new Billing.DefaultConfiguration() {

        @NonNull
        @Override
        public String getPublicKey() {
            return Binfo.GOOGLE_PLAY_PUBLIC_KEY;
        }

    });

    private RefWatcher mRefWatcher;

    /**
     * @author Artem Chepurnoy
     */
    private static class HeartOnPreferenceStoreChangeListener implements PreferenceStore.OnPreferenceStoreChangeListener {
        @Override
        public void onPreferenceStoreChange(
                @NonNull Context context, @NonNull PreferenceStore.Preference pref,
                @NonNull Object old) {
            switch (pref.key) {
                case Config.KEY_UI_THEME:
                    String msg = context.getString(R.string.restart_app_to_apply_theme);
                    Toasty.info(context, msg).show();
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //noinspection ConstantConditions
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Timber.tag(TAG).d("Init in analyzer process.");
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        if (Binfo.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        initLeakCanary();
        initFirebase();
        initConfig();

        // Setup log
        Timber.plant(new ReleaseTree());
    }

    private void initConfig() {
        Config config = Config.getInstance();
        config.load(this);
        config.addListener(new HeartOnPreferenceStoreChangeListener(), Config.KEY_UI_THEME);
    }

    private void initLeakCanary() {
        mRefWatcher = LeakCanary.install(this);
    }

    private void initFirebase() {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.setPersistenceEnabled(true);
    }

    @Override
    protected Billing getBilling() {
        return mBilling;
    }

}
