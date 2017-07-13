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

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.R;
import com.artemchep.horario.database.models.SubjectGroup;
import com.artemchep.horario.ui.DialogHelper;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario.ui.activities.TimetableActivity;
import com.artemchep.horario.ui.adapters.SubjectsGroupsAdapter;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Comparator;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class SubjectGroupsFragment extends ModelFragment<SubjectGroup> implements
        SubjectsGroupsAdapter.OnActionButtonClickListener {

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        // disable editing because model fragment needs
        // an activity for that.
        mEditable = false;
    }

    @NonNull
    @Override
    protected Comparator<SubjectGroup> onCreateComparator() {
        return new Comparator<SubjectGroup>() {
            @Override
            public int compare(SubjectGroup o1, SubjectGroup o2) {
                int i = o1.name.compareToIgnoreCase(o2.name);
                return i != 0 ? i : o1.key.compareTo(o2.key);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details_subject_groups, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @NonNull
    @Override
    protected String getSnackBarRemoveMessage(List<SubjectGroup> list) {
        return "sdf";
    }

    @NonNull
    @Override
    protected BaseAdapter<SubjectGroup> onCreateAdapter() {
        List<SubjectGroup> source = mAggregator.getModels();
        SubjectsGroupsAdapter adapter = new SubjectsGroupsAdapter(this, source);
        adapter.setOnActionButtonClickListener(this);
        return adapter;
    }

    @Override
    public void onClick(
            @NonNull View view, @NonNull SubjectGroup group,
            @NonNull SubjectsGroupsAdapter.ActionButton button) {
        switch (button) {
            case DELETE: {
                final String key = group.key;
                new MaterialDialog.Builder(getContext())
                        .title(group.name)
                        .content(getString(R.string.subject_groups_remove_question))
                        .negativeText(android.R.string.cancel)
                        .positiveText(R.string.dialog_remove)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(
                                    @NonNull MaterialDialog dialog,
                                    @NonNull DialogAction which) {
                                String path = getPath();
                                FirebaseDatabase.getInstance()
                                        .getReference(path)
                                        .child(key)
                                        .setValue(null);
                            }
                        })
                        .show();
                break;
            }
            case EDIT: {
                ActivityHorario activity = (ActivityHorario) getActivity();
                DialogHelper.showSubjectGroupDialog(activity,
                        "user/YhTvBZ5eMTPeuhTKZAh9SeCiVGt1/timetable_public/-KguZ6-bx-bkMGjutdkJ/subjects/-KbyZXLDGApiHnpxiHKW",
                        group);
                break;
            }
            case TIMETABLE: {
                Intent intent = new Intent(getContext(), TimetableActivity.class);
                startActivity(intent);
                break;
            }
        }
    }

    @NonNull
    @Override
    protected Class<SubjectGroup> getType() {
        return SubjectGroup.class;
    }

    @NonNull
    @Override
    protected String getPath() {
        return "user/YhTvBZ5eMTPeuhTKZAh9SeCiVGt1/timetable_public/-KguZ6-bx-bkMGjutdkJ/subjects/-KbyZXLDGApiHnpxiHKW/groups";
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