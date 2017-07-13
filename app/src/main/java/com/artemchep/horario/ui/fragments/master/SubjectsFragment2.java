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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;
import com.artemchep.basic.ui.activities.ActivityBase;
import com.artemchep.horario.R;
import com.artemchep.horario._new.activities.SubjectActivity;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.Key;
import com.artemchep.horario.database.models.KeySubjectInfo;
import com.artemchep.horario.database.models.SubjectInfo;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.artemchep.horario.ui.adapters.SubjectsAdapter;
import com.artemchep.horario.ui.adapters.SubjectsAdapter2;
import com.artemchep.horario.ui.fragments.details.SubjectTaskDetailsFragment;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class SubjectsFragment2 extends ModelFragment<KeySubjectInfo> {

    private static final String TAG = "SubjectsFragment";

    @NonNull
    public static Comparator<KeySubjectInfo> createComparator() {
        // Items without SubjectInfo set should be below
        // ones that have it, otherwise sort by name.
        return new Comparator<KeySubjectInfo>() {
            @Override
            public int compare(KeySubjectInfo o1, KeySubjectInfo o2) {
                if (o1.getModel() != null) {
                    if (o2.getModel() != null) {
                        int i = o1.getModel().name.compareToIgnoreCase(o2.getModel().name);
                        return i != 0 ? i : o1.key.compareTo(o2.key);
                    } else return 1; // o1 > o2
                } else if (o2.getModel() == null) {
                    return o1.key.compareTo(o2.key);
                } else return -1; // o1 < o2
            }
        };
    }

    @NonNull
    private final ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getRef().getParent().getKey();
            SubjectInfo info;
            if (dataSnapshot.exists()) {
                info = dataSnapshot.getValue(SubjectInfo.class);
                info.key = key;
            } else info = null;

            for (KeySubjectInfo model : mAggregator.getModels()) {
                if (model.key.equals(key)) {
                    KeySubjectInfo clone = model.clone();
                    clone.setModel(info);
                    mAggregator.put(clone); // refresh it
                    break;
                }
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @NonNull
    private final List<Persy.Watcher<SubjectInfo>> mWatchers = new ArrayList<>();

    /**
     * Listens the list of references to subjects and setup
     * loading actual subject.
     */
    @NonNull
    private final Aggregator.Observer<KeySubjectInfo> mObserver = new Aggregator.Observer<KeySubjectInfo>() {

        @Override
        public void add(@NonNull KeySubjectInfo model, int i) {
            String path = "_subjects_/" + model.key + "/info";

            Persy.Watcher<SubjectInfo> watcher;
            watcher = getMainActivity().getPersy().watchFor(SubjectInfo.class, path);
            watcher.addListener(mValueEventListener);
            mWatchers.add(watcher);
        }

        @Override
        public void set(@NonNull KeySubjectInfo model, int i) {
            // this can not change the key of the model
        }

        @Override
        public void remove(@NonNull KeySubjectInfo model, int i) {
            String path = "_subjects_/" + model.key + "/info";

            Persy.Watcher<SubjectInfo> watcher;
            watcher = getMainActivity().getPersy().checkFor(SubjectInfo.class, path);
            // Watcher should never be null here
            //noinspection ConstantConditions
            watcher.removeListener(mValueEventListener);
            mWatchers.remove(watcher);
        }

        @Override
        public void move(@NonNull KeySubjectInfo model, int from, int to) {
            // this can not change the key of the model
        }

        @Override
        public void avalanche() {
            for (Persy.Watcher<SubjectInfo> watcher : mWatchers) {
                watcher.removeListener(mValueEventListener);
            }

            mWatchers.clear();

            for (KeySubjectInfo model : mAggregator.getModels()) {
                String path = "_subjects_/" + model.key + "/info";

                Persy.Watcher<SubjectInfo> watcher;
                watcher = getMainActivity().getPersy().watchFor(SubjectInfo.class, path);
                watcher.addListener(mValueEventListener);
                mWatchers.add(watcher);
            }
        }

    };

    @NonNull
    @Override
    protected Comparator<KeySubjectInfo> onCreateComparator() {
        return createComparator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mAggregator.registerListener(mObserver);
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_subjects));
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
                DialogHelper.showSubjectDialog(activity, mTimetablePath, null);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_subjects, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onStart() {
        super.onStart();
        mObserver.avalanche();
    }

    @Override
    public void onStop() {
        super.onStop();
        for (Persy.Watcher<SubjectInfo> watcher : mWatchers) {
            watcher.removeListener(mValueEventListener);
        }

        mWatchers.clear();
    }

    @Override
    public void onItemClick(@NonNull View view, @NonNull KeySubjectInfo item) {
        super.onItemClick(view, item);
        /*
        Intent intent = new Intent(getContext(), ChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, SubjectTaskDetailsFragment.class.getName());
        startActivity(intent);
        */
        /*
        Bundle args = new Bundle();
        args.putBoolean(ModelFragment.EXTRA_EDITABLE, mEditable);
        args.putString(ModelFragment.EXTRA_PATH, mTimetablePath);
        args.putParcelable(MooDetailsFragment.EXTRA_MODEL, item);
        Fragment fragment = new SubjectFragment();
        fragment.setArguments(args);

        MainActivity activity = (MainActivity) getActivity();
        activity.navigateDetailsFrame(fragment);
        */
        Intent intent = new Intent(getContext(), SubjectActivity.class);
//        intent.putExtra(SubjectActivity.EXTRA_PATH, getPath());
        intent.putExtra(SubjectActivity.EXTRA_SUBJECT, item.getModel());
        startActivity(intent);
    }

    @NonNull
    @Override
    protected BaseAdapter<KeySubjectInfo> onCreateAdapter() {
        return new SubjectsAdapter2(this, mAggregator.getModels());
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<KeySubjectInfo> list) {
        if (list.size() == 1) {
            KeySubjectInfo subject = list.get(0);
            return "asdasd";// getString(R.string.snackbar_subject_removed_named, subject.name);
        } else return getString(R.string.snackbar_subject_removed_plural);
    }

    @NonNull
    @Override
    protected Class<KeySubjectInfo> getType() {
        return KeySubjectInfo.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mTimetablePath + "/" + Db.SUBJECTS;
    }

    // --------------------------
    // -- ACTION MODE -----------
    // --------------------------

    @Override
    public boolean onCabCreated(MaterialCab cab, Menu menu) {
        super.onCabCreated(cab, menu);
        getActivity().getMenuInflater().inflate(R.menu.master_context_subjects, menu);
        return true;
    }

    @Override
    public boolean onCabItemClicked(MenuItem menuItem) {
        if (super.onCabItemClicked(menuItem)) {
            return true;
        }

        /*
        switch (menuItem.getItemId()) {
            case R.id.action_palette:
                // Build items list
                final List<Subject> list = getSelections();
                if (list.isEmpty()) {
                    Timber.tag(TAG).d("Palette action clicked with no real selections.");
                } else {
                    // get color
                    Integer c = list.get(0).color;
                    int color = list.get(0).color;
                    for (Subject subject : list) {
                        if (color != subject.color) {
                            c = null;
                            break;
                        }
                    }

                    // get ids
                    String[] ids = new String[list.size()];
                    for (int i = 0; i < ids.length; i++) {
                        ids[i] = list.get(i).key;
                    }

                    ActivityBase activity = (ActivityBase) getActivity();
                    DialogHelper.showPaletteDialog(activity, mTimetablePath, ids, c);
                }

                break;
            default:
                return false;
        }
        */

        getMainActivity().finishContextualActionBar();
        return true;
    }

}
