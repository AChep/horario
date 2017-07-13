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
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.artemchep.basic.Atomic;
import com.artemchep.horario.R;
import com.artemchep.horario._new.Username;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario.ui.bitmaps.AvatarFactory;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * @author Artem Chepurnoy
 */
public class UserView extends RelativeLayout {

    private static String ANONYMOUS = "Anonymous %s";
    private static String[] ANIMALS = new String[]{
            "Alligator", "Anteater", "Armadillo", "Auroch",
            "Axolotl", "Badger", "Bat", "Beaver",
            "Buffalo", "Camel", "Chameleon", "Cheetah",
            "Chipmunk", "Chinchilla", "Chupacabra", "Cormorant",
            "Coyote", "Crow", "Dingo", "Dinosaur",
            "Dolphin", "Duck", "Elephant", "Ferret",
            "Fox", "Frog", "Giraffe", "Gopher",
            "Grizzly", "Hedgehog", "Hippo", "Hyena",
            "Jackal", "Ibex", "Ifrit", "Iguana",
            "Koala", "Kraken", "Lemur", "Leopard",
            "Liger", "Llama", "Manatee", "Mink",
            "Monkey", "Narwhal", "Nyan Cat", "Orangutan",
            "Otter", "Panda", "Penguin", "Platypus",
            "Python", "Pumpkin", "Guagga", "Rabbit",
            "Raccoon", "Rhino", "Sheep", "Shrew",
            "Skunk", "Slow Loris", "Squirrel", "Turtle",
            "Walrus", "Wolf", "Wolverine", "Wombat"
    };

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
            mWatcher.addListener(mEventListener);

            if (mWatcher.getMap().size() > 0) {
                User user = mWatcher.getMap().values().iterator().next();
                bind(user);
            } else {
                // Set placeholder text
                mNameTextView.setText(UiHelper.TEXT_PLACEHOLDER);
                mAvatarView.setImageDrawable(null);
            }
        }

        @Override
        public void onStop(Object... objects) {
            mWatcher.removeListener(mEventListener);
        }
    });

    private Persy mPersy;
    private Persy.Watcher<User> mWatcher;

    private boolean mInitialized;
    private boolean mAttachedToWindow;

    public UserView(Context context) {
        super(context);
        init();
    }

    public UserView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if (mInitialized) {
            return;
        } else mInitialized = true;

        ActivityHorario activity = (ActivityHorario) getContext();
        mPersy = activity.getPersy();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAvatarView = (ImageView) findViewById(R.id.user_avatar);
        mNameTextView = (TextView) findViewById(R.id.user_name);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachedToWindow = true;
        if (mWatcher != null) {
            mAtomic.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachedToWindow = false;
        mAtomic.stop();
    }

    public void setUser(@Nullable String uid) {
        mAtomic.stop();

        if (uid == null || uid.matches(Db.Restriction.REGEX_PATH_DISALLOWED)) {
            mUserId = null;
            mWatcher = null;
            return;
        } else mUserId = uid;

        mWatcher = mPersy.watchFor(User.class, "users/" + uid);
        if (mAttachedToWindow) mAtomic.start();
    }

    @Nullable
    public String getUser() {
        return mUserId;
    }

    public void bind(@Nullable User user) {
        if (user != null) {
            String name = Username.forUser(user);
            mNameTextView.setText(name);

            // Update the avatar
            if (mAvatarView != null) {
                if (user.avatarUrl != null) {
                    Context context = getContext();
                    Glide.with(context)
                            .load(user.avatarUrl)
                            .into(mAvatarView);
                } else setAvatarDrawable(null);
            }
        } else if (mUserId != null) {
            String name = generateNameFor(mUserId);
            mNameTextView.setText(name);
            setImageBitmap(AvatarFactory.create(mUserId.hashCode()));
        } else {
            mNameTextView.setText(null);
            setAvatarDrawable(null);
        }
    }

    /**
     * Generate Google Docs like name (Anonymous Nyan Cat, etc.)
     * for given key.
     */
    @NonNull
    private String generateNameFor(@NonNull String key) {
        return String.format(ANONYMOUS, ANIMALS[key.hashCode() % ANIMALS.length]);
    }

    private void setImageBitmap(@Nullable Bitmap bitmap) {
        if (mAvatarView != null) {
            mAvatarView.setImageBitmap(bitmap);
        }
    }

    private void setAvatarDrawable(@Nullable Drawable drawable) {
        if (mAvatarView != null) {
            mAvatarView.setImageDrawable(drawable);
        }
    }

    public ImageView getAvatarView() {
        return mAvatarView;
    }

    public TextView getNameView() {
        return mNameTextView;
    }

}
