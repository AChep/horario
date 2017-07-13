package com.artemchep.horario._new.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.basic.ui.MultiSelector;
import com.artemchep.horario.R;
import com.artemchep.horario._new.Username;
import com.artemchep.horario._new.activities.SubjectActivity;
import com.artemchep.horario._new.adapters.AdapterModel;
import com.artemchep.horario._new.adapters.UsersAdapter;
import com.artemchep.horario._new.content.AdapterObserver;
import com.artemchep.horario._new.content.FirebaseRelational;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.database.models.SubjectInfo;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.models.Model;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Artem Chepurnoy
 */
public class SubjectStudentsFragment extends FragmentModel<User> implements SubjectActivity.Page {

    private RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    private View mEmptyView;

    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            mRational.put(dataSnapshot.getKey());
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            mRational.put(dataSnapshot.getKey());
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            mRational.remove(dataSnapshot.getKey());
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private FirebaseRelational<User> mRational = new FirebaseRelational<>(
            new FirebaseRelational.Finder<User>() {
                @NonNull
                @Override
                public String getPath(@NonNull String key) {
                    return "users/" + key;
                }

                @NonNull
                @Override
                public Class<User> getType() {
                    return User.class;
                }
            },
            new FirebaseRelational.Observer<User>() {
                @Override
                public void put(@NonNull User model) {
                    getAggregator().put(model);
                }

                @Override
                public void remove(@NonNull String key) {
                    getAggregator().remove(key);
                }

                @Override
                public void clear() {
                    getAggregator().reset();
                }
            });

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_subject_students, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmptyView = view.findViewById(R.id.empty);
    }

    @Override
    public Filter<User> onCreateFilter() {
        return new Filter<>();
    }

    @Override
    public Comparator<User> onCreateComparator() {
        return new Comparator<User>() {
            @Override
            public int compare(User a, User b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        };
    }

    @Override
    public AdapterModel<User, ? extends AdapterModel.ViewHolder<User>> onCreateAdapter(List<User> list) {
        return new UsersAdapter(this, list);
    }

    @Override
    public void onStart() {
        super.onStart();
        mRational.start();
        FirebaseDatabase.getInstance().getReference()
                .child("subjects")
                .child("testing_is_fun")
                .child("students")
                .addChildEventListener(mChildEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        FirebaseDatabase.getInstance().getReference()
                .child("subjects")
                .child("testing_is_fun")
                .child("students")
                .removeEventListener(mChildEventListener);
        mRational.stop();
        mRational.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSubject(@Nullable SubjectInfo subject) {
    }

    @Override
    public void onFabClick(@NonNull View view) {

    }

    @Override
    public boolean hasFab() {
        return true;
    }

}
