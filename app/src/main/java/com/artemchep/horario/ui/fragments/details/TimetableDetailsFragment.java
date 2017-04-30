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
package com.artemchep.horario.ui.fragments.details;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.ShareHelper;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.models.Timetable;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.ReceiverActivity;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class TimetableDetailsFragment extends ModelDetailsFragment<Timetable> implements Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_USER_ID = "extra::uid";
    public static final String EXTRA_TIMETABLE = "timetable";

    private static final String TAG = "TimetableDetailsFragment";

    private DatabaseReference mDatabaseRef;
    private String mUserId;
    private Timetable mTimetable;

    private Toolbar mToolbar;
    private View mEmptyView;
    private View mDeletedView;
    private View mShareContainer;
    private View mShareLinkContainer;
    private TextView mTitleTextView;
    private TextView mAbbrTextView;
    private TextView mShareLinkTextView;
    private MenuItem mMenuShareItem;

    private boolean mMenuVisible;

    private String mLink;

    @NonNull
    private final ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String publicAddressOld = mTimetable.publicAddress;
            if (dataSnapshot.exists()) {
                mTimetable = dataSnapshot.getValue(Timetable.class);
                mTimetable.key = dataSnapshot.getKey();
            } else mTimetable = new Timetable();

            String publicAddress = mTimetable.publicAddress;
            if (TextUtils.equals(publicAddress, publicAddressOld)) {
                // Do not re-register is-shared leaf listener, because
                // it didn't change.
                bind(mTimetable);
                return;
            }

            mLink = null; // reset link
            bind(mTimetable);

            // unregister old listener if exists
            Pair<String, String> old = splitPublicAddress(publicAddressOld);
            if (old != null) {
                Db.user(old.first)
                        .timetablePublic(old.second).ref()
                        .child(Db.LEAF_IS_SHARED)
                        .removeEventListener(mIsSharedValueEventListener);
            }

            // register old listener if possible
            Pair<String, String> cur = splitPublicAddress(publicAddressOld);
            if (cur != null) {
                Db.user(cur.first)
                        .timetablePublic(cur.second).ref()
                        .child(Db.LEAF_IS_SHARED)
                        .addValueEventListener(mIsSharedValueEventListener);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @NonNull
    private final ValueEventListener mIsSharedValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            boolean isShared;
            if (dataSnapshot.exists()) {
                isShared = dataSnapshot.getValue(boolean.class);
            } else isShared = false;

            setShared(isShared);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @NonNull
    private final ShareHelper mShareHelper = new ShareHelper();

    @Override
    public void onCreate(@Nullable Bundle savedState) {
        super.onCreate(savedState);

        Bundle args = getArguments();
        mUserId = args.getString(EXTRA_USER_ID);

        // Load extras
        if (savedState != null) {
            // Timetable must not be null here!
            //noinspection ConstantConditions
            mTimetable = savedState.getParcelable(EXTRA_TIMETABLE);
        } else {
            mTimetable = args.getParcelable(EXTRA_TIMETABLE);
        }

        if (mTimetable == null) {
            mTimetable = new Timetable();
        }

        if (mTimetable.key != null) {
            mLink = null;

            // register old listener if possible
            Pair<String, String> cur = splitPublicAddress(mTimetable.publicAddress);
            if (cur != null) {
                Db.user(cur.first)
                        .timetablePublic(cur.second).ref()
                        .child(Db.LEAF_IS_SHARED)
                        .addValueEventListener(mIsSharedValueEventListener);
            }

            mDatabaseRef = Db.user(mUserId).timetable(mTimetable.key).ref();
        }
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_timetable, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        AppBarLayout appBar = (AppBarLayout) view.findViewById(R.id.appbar);
        mToolbar = (Toolbar) appBar.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.details_timetable);
        mToolbar.setOnMenuItemClickListener(this);
        mMenuShareItem = mToolbar.getMenu().findItem(R.id.action_share);

        mEmptyView = view.findViewById(R.id.empty);
        mTitleTextView = (TextView) appBar.findViewById(R.id.title);
        mAbbrTextView = (TextView) appBar.findViewById(R.id.abbr);
        mDeletedView = view.findViewById(R.id.deleted);
        mShareContainer = view.findViewById(R.id.share_container);
        mShareLinkContainer = view.findViewById(R.id.share_link_container);
        mShareLinkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLink == null) {
                    return; // should never happen
                }

                // Copy email to clipboard
                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Shareable link", mLink);
                clipboard.setPrimaryClip(clip);

                // Show toast message
                String msg = getString(R.string.details_link_clipboard, mLink);
                Toasty.info(getContext(), msg).show();
            }
        });
        mShareLinkTextView = (TextView) mShareLinkContainer.findViewById(R.id.share_link);
        mShareContainer.findViewById(R.id.share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mLink == null) {
                    return; // should never happen
                }

                try {
                    Intent i = new Intent(Intent.ACTION_SEND);
                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
                    i.putExtra(Intent.EXTRA_TEXT, "Horario shared timetable: " + mLink);
                    startActivity(Intent.createChooser(i, getString(R.string.share_link_via)));
                } catch (Exception e) {
                    FirebaseCrash.report(new Exception("Failed to share timetable.", e));
                }
            }
        });

        bind(mTimetable);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        if (mTimetable.key == null) {
            return false;
        }

        switch (item.getItemId()) {
            // Show edit dialog
            case R.id.action_edit: {
                DialogHelper.showTimetableDialog(getMainActivity(), mUserId, mTimetable);
                break;
            }
            // Try to mark `isShared` as true/false in
            // database
            case R.id.action_share: {
                boolean isShared = !item.isChecked();
                performShareTimetable(isShared);
                break;
            }
            // Remove current timetable
            case R.id.action_delete: {
                Pair<String, String> pair = splitPublicAddress(mTimetable.publicAddress);
                boolean isCopy = pair != null && !mUserId.equals(pair.first);

                // If current user is not an author of the original public part
                // then don't show additional dialog.
                if (isCopy) {
                    performRemoveTimetable(false);
                } else new MaterialDialog.Builder(getContext())
                        .customView(R.layout.dialog_timetable_remove, true)
                        .positiveText(R.string.dialog_remove)
                        .negativeText(android.R.string.cancel)
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                switch (which) {
                                    case POSITIVE: {
                                        CheckBox checkBox = (CheckBox) dialog.getCustomView().findViewById(R.id.checkbox);
                                        performRemoveTimetable(checkBox.isChecked());
                                        break;
                                    }
                                }
                            }
                        })
                        .show();
                break;
            }
            default:
                return false;
        }
        return true;
    }

    private void performShareTimetable(boolean share) {
        Pair<String, String> cur = splitPublicAddress(mTimetable.publicAddress);
        assert cur != null;
        String userId = cur.first;
        String publicKey = cur.second;

        DatabaseReference db = Db.user(userId)
                .timetablePublic(publicKey)
                .ref().child(Db.LEAF_IS_SHARED);
        db.setValue(share);
    }

    private void performRemoveTimetable(boolean includingSharedCopies) {
        Map<String, Object> childUpdates = new HashMap<>();

        // Clean public part of the timetable if needed
        if (includingSharedCopies) {
            Pair<String, String> pair = splitPublicAddress(mTimetable.publicAddress);
            if (pair != null) {
                String path = Db.user(pair.first).timetablePublic(pair.second).path();
                childUpdates.put(path, null);
            }
        }

        // Clean private part of the timetable
        if (mTimetable.privateKey != null) {
            String path = Db.user(mUserId).timetablePrivate(mTimetable.privateKey).path();
            childUpdates.put(path, null);
        }

        // Clean header
        String path = Db.user(mUserId).timetable(mTimetable.key).path();
        childUpdates.put(path, null);
        FirebaseDatabase.getInstance().getReference().updateChildren(childUpdates);
    }

    /**
     * @return a pair of `public_user` and `public_timetable_key`, or
     * {@code null} if given public address is incorrect.
     */
    @Nullable
    private Pair<String, String> splitPublicAddress(String publicAddress) {
        if (publicAddress == null) {
            return null;
        }

        String[] parts = publicAddress.split("/");
        if (parts.length != 2) {
            return null;
        } else return new Pair<>(parts[0], parts[1]);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mDatabaseRef != null) {
            mDatabaseRef.addValueEventListener(mValueEventListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDatabaseRef != null) {
            mDatabaseRef.removeEventListener(mValueEventListener);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_TIMETABLE, mTimetable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // unregister old listener if needed
        Pair<String, String> cur = splitPublicAddress(mTimetable.publicAddress);
        if (cur != null) {
            Db.user(cur.first)
                    .timetablePublic(cur.second).ref()
                    .child(Db.LEAF_IS_SHARED)
                    .removeEventListener(mIsSharedValueEventListener);
        }
    }

    @Override
    protected void bind(@NonNull Timetable timetable) {
        mTimetable = timetable;
        mTitleTextView.setText(timetable.name);
        getHelper().setAppBarBackgroundColor(Palette.GREY);
        // Check if current timetable is shared from
        // other user.
        Pair<String, String> cur = splitPublicAddress(mTimetable.publicAddress);
        mAbbrTextView.setText(cur != null && !mUserId.equals(cur.first) ? "shared" : null);
        mMenuShareItem.setVisible(cur != null && mUserId.equals(cur.first));

        boolean isDeleted = timetable.key == null;
        updateMenuVisibility(!isDeleted);
        if (isDeleted) {
            mDeletedView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);
        } else {
            mDeletedView.setVisibility(View.GONE);
            if (mLink == null) {
                mEmptyView.setVisibility(View.VISIBLE);
            } else mEmptyView.setVisibility(View.GONE);
        }

        // Display share link
        if (mLink == null) {
            mShareContainer.setVisibility(View.GONE);
            mShareLinkContainer.setVisibility(View.GONE);
        } else {
            mShareContainer.setVisibility(View.VISIBLE);
            mShareLinkContainer.setVisibility(View.VISIBLE);
            mShareLinkTextView.setText(mLink);
        }
    }

    private void updateMenuVisibility(boolean visible) {
        if (mMenuVisible == visible) {
            return;
        } else mMenuVisible = visible;

        Menu menu = mToolbar.getMenu();
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            menu.getItem(i).setVisible(visible);
        }
    }

    private void setShared(boolean isShared) {
        mLink = null;

        if (isShared) {
            Pair<String, String> cur = splitPublicAddress(mTimetable.publicAddress);
            if (cur != null) {
                String userId = cur.first;
                String publicKey = cur.second;
                mShareHelper.decode(userId, publicKey);
                mLink = ReceiverActivity.createShareLink(mShareHelper.toKey());
            }
        }

        mMenuShareItem.setChecked(isShared);
        bind(mTimetable);
    }

}
