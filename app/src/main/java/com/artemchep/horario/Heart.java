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

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.artemchep.horario.timber.ReleaseTree;
import com.artemchep.horario.content.PreferenceStore;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import net.danlew.android.joda.JodaTimeAndroid;

import org.jetbrains.annotations.NotNull;
import org.solovyev.android.checkout.Billing;

import es.dmoral.toasty.Toasty;
import pl.tajchert.nammu.Nammu;
import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class Heart extends Application {

    private static final String TAG = "Heart";
    @NotNull
    public static final String CHANNEL_SERVICE = "chan:service";

    public static final String INTENT_DB_EVENTS_UPDATED = "intent::events::db_updated";

    @NonNull
    public static RefWatcher getRefWatcher(@NonNull Context context) {
        Heart application = (Heart) context.getApplicationContext();
        return application.mRefWatcher;
    }

    @NonNull
    public static Billing getBilling(@NonNull Context context) {
        Heart application = (Heart) context.getApplicationContext();
        return application.getBilling();
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
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Setup log
        Timber.plant(new ReleaseTree());

        //noinspection ConstantConditions
        if (LeakCanary.isInAnalyzerProcess(this)) {
            Timber.tag(TAG).d("Init in analyzer process.");
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }

        if (Binfo.DEBUG && false) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());
        }

        initLeakCanary();
        initFirebase();
        initConfig();

        JodaTimeAndroid.init(this);
        Fresco.initialize(this);
        Nammu.init(this);
    }

    private void initConfig() {
        Config.INSTANCE.load(this);
        Config.INSTANCE.addListener(new HeartOnPreferenceStoreChangeListener());
    }

    private void initLeakCanary() {
        mRefWatcher = LeakCanary.install(this);
    }

    private void initFirebase() {
        FirebaseFirestore.setLoggingEnabled(true);
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        db.setPersistenceEnabled(true);
    }

    public Billing getBilling() {
        return mBilling;
    }

}
