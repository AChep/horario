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
package com.artemchep.horario.ui.widgets;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artemchep.basic.Atomic;
import com.artemchep.horario.R;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.ui.bitmaps.AvatarFactory;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * @author Artem Chepurnoy
 */
public class GroupUserView extends LinearLayout {

    private String mUserId;

    private ImageView mAvatarView;
    private TextView mNameTextView;

    @NonNull
    private final ValueEventListener mEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                User user = dataSnapshot.getValue(User.class);
                user.key = dataSnapshot.getKey();
                bind(user);
            } else bind(null);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @NonNull
    private final Atomic mAtomic = new Atomic(new Atomic.Callback() {
        @Override
        public void onStart(Object... objects) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(mUserId)
                    .addValueEventListener(mEventListener);
        }

        @Override
        public void onStop(Object... objects) {
            FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(mUserId)
                    .removeEventListener(mEventListener);
        }
    }) {

        @Override
        public void start(Object... objects) {
            if (mUserId != null) {
                super.start(objects);
            }
        }

        @Override
        public void stop(Object... objects) {
            if (mUserId != null) {
                super.stop(objects);
            }
        }

    };

    public GroupUserView(Context context) {
        super(context);
    }

    public GroupUserView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAvatarView = (ImageView) findViewById(R.id.avatar);
        mNameTextView = (TextView) findViewById(R.id.name);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAtomic.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAtomic.stop();
    }

    public void setUser(@NonNull String uid) {
        mAtomic.stop();

        bind(null);

        if (uid.matches(Db.Restriction.REGEX_PATH_DISALLOWED)) {
            mUserId = null;
        } else {
            mUserId = uid;
            mAtomic.start();
        }
    }

    @Nullable
    public String getUser() {
        return mUserId;
    }

    public void bind(User user) {
        if (user != null) {
            mNameTextView.setText(user.name);
            Glide.with(getContext()).load(user.avatarUrl).into(mAvatarView);
        } else {
            mNameTextView.setText("Lame");
            if (mUserId != null) {
                mAvatarView.setImageBitmap(AvatarFactory.create(mUserId.hashCode()));
            }
        }
    }

}
