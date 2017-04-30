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
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.Config;
import com.artemchep.horario.R;
import com.artemchep.horario.content.PreferenceStore;
import com.artemchep.horario.database.Db;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.models.Note;
import com.artemchep.horario.models.Subject;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.activities.NoteActivity;
import com.artemchep.horario.ui.adapters.NotesAdapter;
import com.artemchep.horario.ui.widgets.ContainersLayout;
import com.artemchep.horario.ui.widgets.CustomAppBar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;

/**
 * @author Artem Chepurnoy
 */
public class NotesFragment extends ModelFragment<Note> implements PreferenceStore.OnPreferenceStoreChangeListener, Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_TIMETABLE_PATH_PUBLIC = "extra::timetable_path::public";

    @NonNull
    public static Comparator<Note> createComparator() {
        // sort by name and key
        return new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                int i;
                i = o2.priority.compareTo(o1.priority);
                return i != 0 ? i : o1.key.compareTo(o2.key);
            }
        };
    }

    /**
     * @author Artem Chepurnoy
     */
    private enum ViewMode {
        STREAM,
        DASHBOARD,
    }

    private String mTimetablePathPublic;

    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;

    private MenuItem mStreamMenuItem;
    private MenuItem mDashboardMenuItem;

    private Persy.Watcher<Subject> mWatcherSubjects;

    @NonNull
    private final ChildEventListener mWatcherSubjectsListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            notifyAdapterItemsBy(subject.key);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Subject subject = mWatcherSubjects.getModel();
            notifyAdapterItemsBy(subject.key);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            notifyAdapterItemsBy(dataSnapshot.getKey());
        }

        private void notifyAdapterItemsBy(String key) {
            List<Note> list = getAdapter().getItems();
            for (int i = 0; i < list.size(); i++) {
                Note note = list.get(i);
                if (note.subjects != null && note.subjects.contains(key)) {
                    getAdapter().notifyItemChanged(i);
                }
            }
        }

    };

    @NonNull
    @Override
    protected Comparator<Note> onCreateComparator() {
        return createComparator();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Get timetable data
        Bundle args = getArguments();
        MainActivity activity = getMainActivity();
        mTimetablePathPublic = args.getString(EXTRA_TIMETABLE_PATH_PUBLIC);

        Persy persy = activity.getPersy();
        mWatcherSubjects = persy.watchFor(Subject.class, mTimetablePathPublic + "/" + Db.SUBJECTS);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ContainersLayout containers = getMainActivity().mContainers;
        containers.setCardDecorationEnabled(false);
    }

    @Override
    protected void setupToolbar() {
        super.setupToolbar();
        CustomAppBar appBar = getMainActivity().mAppBar;
        appBar.setTitle(getString(R.string.nav_notes));

        Toolbar toolbar = appBar.getToolbarSpecific();
        toolbar.inflateMenu(R.menu.master_notes);
        toolbar.setOnMenuItemClickListener(this);

        Menu menu = toolbar.getMenu();
        mStreamMenuItem = menu.findItem(R.id.action_view_stream);
        mDashboardMenuItem = menu.findItem(R.id.action_view_dashboard);
    }

    @Override
    protected void setupFab() {
        super.setupFab();
        FloatingActionButton fab = getMainActivity().mFab;
        fab.show();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityOptionsCompat options = ActivityOptionsCompat
                        .makeClipRevealAnimation(v, 0, 0, v.getWidth(), v.getHeight());
                startActivity(createNoteIntent(null), options.toBundle());
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main_notes, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT,
                ItemTouchHelper.START | ItemTouchHelper.END) {

            @Override
            public boolean onMove(
                    RecyclerView recyclerView,
                    RecyclerView.ViewHolder viewHolder,
                    RecyclerView.ViewHolder target) {
                int a = mAggregator.getModels().size();
                int b = mAggregator.getModelsAll().size();
                if (a != b) {
                    return false; // do not allow sorting with applied filters
                }

                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();
                if (fromPos == toPos) {
                    return false;
                }

                getAdapter().notifyItemMoved(fromPos, toPos);
                Note note = getAdapter().getItem(toPos);
                if (fromPos < toPos) {
                    // priority should be less

                } else {
                    // priority should be greater

                }
                return true;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
                View view = getView();
                if (view == null) {
                    // This should not happen normally.
                    return;
                }

                int i = mRecyclerView.getChildAdapterPosition(viewHolder.itemView);
                final Note note = getAdapter().getItem(i);
                final List<Note> list = new ArrayList<>();
                list.add(note);
                removeModels(list);
            }

            @Override
            public boolean isLongPressDragEnabled() {
                int a = mAggregator.getModels().size();
                int b = mAggregator.getModelsAll().size();
                return a == b && false; // do not allow sorting with applied filters
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return true;
            }
        };

        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        if (getMainActivity().mContainers.hasTwoColumns()) {
            // workaround for the bug where layout manager
            // calculates wrong width.
            // TODO: Investigate and fix gap strategy
            mLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
        }
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(mLayoutManager);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onItemClick(@NonNull View view, @NonNull Note item) {
        super.onItemClick(view, item);
//        Pair<View, String> p1 = Pair.create(view.findViewById(R.id.title), "title");
//        Pair<View, String> p2 = Pair.create(view.findViewById(R.id.content), "content");
        Pair<View, String> p3 = Pair.create(view, "card");
        //noinspection unchecked
        ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(getActivity(), p3);
        startActivity(createNoteIntent(item), options.toBundle());
    }

    /**
     * @param note a note to be edited, {@code null} to create one
     * @return an intent to start the note editor/creator.
     */
    @NonNull
    private Intent createNoteIntent(@Nullable Note note) {
        Intent intent = new Intent(getActivity(), NoteActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(NoteActivity.EXTRA_TIMETABLE_PATH, mTimetablePath);
        intent.putExtra(NoteActivity.EXTRA_TIMETABLE_PATH_PUBLIC, mTimetablePathPublic);
        intent.putExtra(NoteActivity.EXTRA_NOTE, note);
        return intent;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Config config = Config.getInstance();
        switch (item.getItemId()) {
            // View mode
            case R.id.action_view_stream:
                config.edit(getContext())
                        .put(Config.KEY_UI_NOTES_DASHBOARD_VIEW, false)
                        .commit(this);
                setViewMode(ViewMode.STREAM);
                break;
            case R.id.action_view_dashboard:
                config.edit(getContext())
                        .put(Config.KEY_UI_NOTES_DASHBOARD_VIEW, true)
                        .commit(this);
                setViewMode(ViewMode.DASHBOARD);
                break;
            // Other
            default:
                return false;
        }
        return true;
    }

    @Override
    public void onPreferenceStoreChange(
            @NonNull Context context, @NonNull PreferenceStore.Preference pref,
            @NonNull Object old) {
        switch (pref.key) {
            case Config.KEY_UI_NOTES_DASHBOARD_VIEW:
                updateViewDashboardPreference();
                break;
        }
    }

    @NonNull
    @Override
    protected ModelFragment.BaseAdapter<Note> onCreateAdapter() {
        return new NotesAdapter(this, mAggregator.getModels(), mWatcherSubjects.getMap());
    }

    @Override
    public void onStart() {
        super.onStart();
        Config.getInstance().addListener(this, Config.KEY_UI_NOTES_DASHBOARD_VIEW);
        mWatcherSubjects.addListener(mWatcherSubjectsListener);

//        getAdapter().notifyDataSetChanged();
        updateViewDashboardPreference();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("width=" + mRecyclerView.getMeasuredWidth());
        Timber.d("height=" + mRecyclerView.getMeasuredHeight());

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mRecyclerView.getParent().requestLayout();
            }
        });

//        mLayoutManager.invalidateSpanAssignments();
//        mRecyclerView.requestLayout();
    }

    @Override
    public void onStop() {
        mWatcherSubjects.removeListener(mWatcherSubjectsListener);
        Config.getInstance().removeListener(this, Config.KEY_UI_NOTES_DASHBOARD_VIEW);
        super.onStop();
    }

    private void updateViewDashboardPreference() {
        boolean dashboard = Config.getInstance().getBoolean(Config.KEY_UI_NOTES_DASHBOARD_VIEW);
        setViewMode(dashboard ? ViewMode.DASHBOARD : ViewMode.STREAM);
    }

    /**
     * Sets {@link ViewMode view mode} that defines number of
     * columns in list of fragment_notes.
     */
    private void setViewMode(@NonNull ViewMode mode) {
        final int spanCount;
        switch (mode) {
            case STREAM:
                spanCount = 1;
                break;
            case DASHBOARD:
                spanCount = getResources().getInteger(R.integer.note_column_count);
                break;
            default:
                throw new IllegalArgumentException();
        }

        if (mStreamMenuItem != null) {
            // Update menu if it exists.
            mStreamMenuItem.setVisible(spanCount != 1);
            mDashboardMenuItem.setVisible(spanCount == 1);
        }

        mLayoutManager.setSpanCount(spanCount);
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<Note> list) {
        if (list.size() == 1) {
            Note note = list.get(0);

            if (TextUtils.isEmpty(note.title)) {
                return getString(R.string.snackbar_note_removed);
            } else return getString(R.string.snackbar_note_removed_named, note.title);
        } else return getString(R.string.snackbar_note_removed_plural);
    }

    @NonNull
    @Override
    protected Class<Note> getType() {
        return Note.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return mTimetablePath + "/" + Db.NOTES;
    }

}
