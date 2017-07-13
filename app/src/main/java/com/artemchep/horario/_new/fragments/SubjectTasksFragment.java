package com.artemchep.horario._new.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.artemchep.horario._new.activities.GalleryActivity;
import com.artemchep.horario._new.activities.SubjectActivity;
import com.artemchep.horario._new.adapters.AdapterModel;
import com.artemchep.horario._new.adapters.StreamAdapter;
import com.artemchep.horario._new.adapters.TasksAdapter;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.database.models.SubjectInfo;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectTasksFragment extends FragmentModel<SubjectTask> implements SubjectActivity.Page {

    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            SubjectTask task = dataSnapshot.getValue(SubjectTask.class);
            task.key = dataSnapshot.getKey();
            getAggregator().put(task);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            SubjectTask task = dataSnapshot.getValue(SubjectTask.class);
            task.key = dataSnapshot.getKey();
            getAggregator().put(task);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            getAggregator().remove(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };
    private SubjectInfo mSubject;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_subject_lessons, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        getAggregator().reset();
        FirebaseDatabase.getInstance().getReference()
                .child("subjects").child("testing_is_fun").child("tasks")
                .addChildEventListener(mChildEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference()
                .child("subjects").child("testing_is_fun").child("tasks")
                .removeEventListener(mChildEventListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubject(@Nullable SubjectInfo subject) {
        mSubject = subject;
    }

    @Override
    public Filter<SubjectTask> onCreateFilter() {
        return new Filter<>();
    }

    @Override
    public Comparator<SubjectTask> onCreateComparator() {
        return new Comparator<SubjectTask>() {
            @Override
            public int compare(SubjectTask a, SubjectTask b) {
                return a.key.compareToIgnoreCase(b.key);
            }
        };
    }

    @Override
    public AdapterModel<SubjectTask, ? extends AdapterModel.ViewHolder<SubjectTask>> onCreateAdapter(List<SubjectTask> list) {
        return new TasksAdapter(this, list);
    }

    @Override
    public void onItemClick(View v, SubjectTask data) {
        super.onItemClick(v, data);

        Bundle args = new Bundle();
        args.putString(SubjectTaskFragment.EXTRA_SUBJECT_ID, "-KlnzthUb_8St7Ax0UT0");
        args.putString(SubjectTaskFragment.EXTRA_TASK_ID, "keykeykey");
        args.putString(SubjectTaskFragment.EXTRA_GROUP_ID, "keykeykey");
        args.putInt(SubjectTaskFragment.EXTRA_COLOR, mSubject.color);

        Intent intent = new Intent(getContext(), ChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, SubjectTaskFragment.class.getName());
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
        startActivity(intent);
    }

    @Override
    public void onFabClick(@NonNull View view) {
    }

    @Override
    public boolean hasFab() {
        return true;
    }

}
