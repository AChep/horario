package com.artemchep.horario._new.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.artemchep.horario._new.activities.SubjectActivity;
import com.artemchep.horario._new.adapters.AdapterModel;
import com.artemchep.horario._new.adapters.StreamAdapter;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.database.models.SubjectInfo;
import com.artemchep.horario.database.models.SubjectTask;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectStreamFragment extends FragmentModel<SubjectTask> implements SubjectActivity.Page {

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

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_subject_stream, container, false);
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
        return new StreamAdapter(this, list);
    }

    @Override
    public void onFabClick(@NonNull View view) {
    }

    @Override
    public boolean hasFab() {
        return true;
    }

}
