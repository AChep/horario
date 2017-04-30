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
package com.artemchep.horario.ui.fragments.master;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.basic.utils.LongUtils;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Notification;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.adapters.NotificationsAdapter;
import com.artemchep.horario.ui.widgets.CustomAppBar;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class NotificationsFragment extends ModelFragment<Notification> {

    @NonNull
    @Override
    protected Comparator<Notification> onCreateComparator() {
        // compare by time and key
        return new Comparator<Notification>() {
            @Override
            public int compare(Notification o1, Notification o2) {
                int i = LongUtils.compare(o1.timestamp, o2.timestamp);
                return i != 0 ? i : o1.key.compareTo(o2.key);
            }
        };
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_notifications));
    }

    @Override
    protected void setupFab() {
        super.setupFab();
        FloatingActionButton fab = getMainActivity().mFab;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityBase activity = getMainActivity();
                DialogHelper.showNotificationDialog(activity, mTimetablePath, null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_notifications, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @NonNull
    @Override
    protected BaseAdapter<Notification> onCreateAdapter() {
        return new NotificationsAdapter(this, mAggregator.getModels());
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<Notification> list) {
        if (list.size() == 1) {
            Notification notification = list.get(0);
            return TextUtils.isEmpty(notification.title)
                    ? getString(R.string.snackbar_notification_removed)
                    : getString(R.string.snackbar_notification_removed_named, notification.title);
        } else return getString(R.string.snackbar_notification_removed_plural);
    }

    @NonNull
    @Override
    protected Class<Notification> getType() {
        return Notification.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mTimetablePath + "/" + Db.NOTIFICATIONS;
    }

}
