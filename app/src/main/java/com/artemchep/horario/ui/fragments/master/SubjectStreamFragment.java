package com.artemchep.horario.ui.fragments.master;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.artemchep.horario._new.fragments.UserFragment;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.artemchep.horario.ui.activities.SubjectActivity;
import com.artemchep.horario.ui.adapters.SubjectsTasksAdapter;
import com.artemchep.horario.ui.fragments.details.SubjectTaskDetailsFragment;
import com.artemchep.horario.ui.fragments.details.SubjectTaskEditFragment;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectStreamFragment extends ModelFragment<SubjectTask> {

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        // disable editing because model fragment needs
        // an activity for that.
        mEditable = false;
    }

    @NonNull
    @Override
    protected Comparator<SubjectTask> onCreateComparator() {
        return new Comparator<SubjectTask>() {
            @Override
            public int compare(SubjectTask o1, SubjectTask o2) {
                return o1.key.compareTo(o2.key);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_subject_stream, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @Override
    public void onItemClick(@NonNull View view, @NonNull SubjectTask item) {
        super.onItemClick(view, item);
        Bundle args = new Bundle();
        args.putString(UserFragment.EXTRA_USER_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");

        Intent intent = new Intent(getContext(), ChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, UserFragment.class.getName());
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
        startActivity(intent);
/*
        Bundle args = new Bundle();
        args.putString(SubjectTaskDetailsFragment.EXTRA_SUBJECT_ID, "-KlnzthUb_8St7Ax0UT0");
        args.putString(SubjectTaskDetailsFragment.EXTRA_GROUP_ID, "keykeykey");
        args.putString(SubjectTaskDetailsFragment.EXTRA_TASK_ID, item.key);
        args.putString(SubjectTaskDetailsFragment.EXTRA_USER_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");

        Intent intent = new Intent(getContext(), ChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, SubjectTaskDetailsFragment.class.getName());
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
        startActivity(intent);
        */
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<SubjectTask> list) {
        return "sdf";
    }

    @NonNull
    @Override
    protected BaseAdapter<SubjectTask> onCreateAdapter() {
        return new SubjectsTasksAdapter(this, mAggregator.getModels());
    }

    @NonNull
    @Override
    protected Class<SubjectTask> getType() {
        return SubjectTask.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return "_subjects_/-KlnzthUb_8St7Ax0UT0/groups/keykeykey/tasks";
    }

    // --------------------------
    // -- DONT MODIFY ACTIVITY --
    // --------------------------

    @Override
    protected void setupFab() {
    }

    @Override
    protected void setupToolbar() {
    }

    @Override
    protected void setupContainers() {
    }

}