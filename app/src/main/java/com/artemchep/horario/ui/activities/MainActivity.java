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
package com.artemchep.horario.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.R;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.fragments.dialogs.ChangelogDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class MainActivity extends ActivityBase implements
        FirebaseAuth.AuthStateListener,
        Drawer.OnDrawerItemClickListener {

    private FirebaseAuth mAuth;
    private Drawer mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppThemeLight_NoActionBar);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        List<IDrawerItem> drawerItems = new ArrayList<>();
        drawerItems.add(new SecondaryDrawerItem()
                .withIdentifier(R.id.nav_settings)
                .withIcon(R.drawable.ic_settings_white_24dp)
                .withName(R.string.nav_settings)
                .withSelectable(false));
        drawerItems.add(new SecondaryDrawerItem()
                .withIdentifier(R.id.nav_feedback)
                .withIcon(R.drawable.ic_email_white_24dp)
                .withName(R.string.nav_feedback)
                .withSelectable(false));
        drawerItems.add(new SecondaryDrawerItem()
                .withIdentifier(R.id.nav_privacy_policy)
                .withIcon(R.drawable.ic_shield_white_24dp)
                .withName(R.string.nav_privacy_policy)
                .withSelectable(false));
        drawerItems.add(new SecondaryDrawerItem()
                .withIdentifier(R.id.nav_about)
                .withIcon(R.drawable.ic_information_outline_white_24dp)
                .withName(R.string.nav_about)
                .withSelectable(false));
        DrawerBuilder builder = new DrawerBuilder()
                .withActivity(this)
                .withDrawerItems(drawerItems)
                .withSavedInstance(savedInstanceState)
                .withOnDrawerItemClickListener(this)
                .withTranslucentStatusBar(true)
                .withCloseOnClick(true)
                .withToolbar(toolbar);

        initPhoneUi(builder, savedInstanceState);

        // Show updated changelog each time application version
        // code is increased.
        if (!ChangelogDialog.isRead(this)) {
            DialogHelper.showChangelogDialog(this);
        }
    }

    private void initPhoneUi(@NonNull DrawerBuilder builder, Bundle savedInstanceState) {
        mDrawer = builder
                .withActionBarDrawerToggleAnimated(true)
                .build();
    }

    private void switchToAuthActivity() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) switchToAuthActivity();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState = mDrawer.saveInstanceState(outState);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAuth.removeAuthStateListener(this);
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        int id = (int) drawerItem.getIdentifier();
        if (id == R.id.nav_feedback) {
            DialogHelper.showFeedbackDialog(this);
        } else if (id == R.id.nav_privacy_policy) {
            DialogHelper.showPrivacyDialog(this);
        } else if (id == R.id.nav_about) {
            DialogHelper.showAboutDialog(this);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return false;
    }

}
