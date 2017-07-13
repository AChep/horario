package com.artemchep.horario._new.adapters;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario._new.fragments.FragmentModel;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.ui.widgets.UserView;
import com.artemchep.horario.utils.DateUtilz;

import java.util.Calendar;
import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class TasksAdapter extends AdapterModel<SubjectTask, TasksAdapter.ViewHolder> {

    private final String[] mMonths;
    private final int mCurrentYear;

    public TasksAdapter(@NonNull FragmentModel<SubjectTask> fragment, @NonNull List<SubjectTask> list) {
        super(fragment, list);
        mMonths = fragment.getResources().getStringArray(R.array.months);
        mCurrentYear = Calendar.getInstance().get(Calendar.YEAR);
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class ViewHolder extends AdapterModel.ViewHolder<SubjectTask> {

        @NonNull
        final TextView titleTextView;
        @NonNull
        final TextView decrTextView;
        @NonNull
        final TextView dueTextView;
        @NonNull
        final View dueView;

        public ViewHolder(View itemView, FragmentModel<SubjectTask> fragment) {
            super(itemView, fragment);
            titleTextView = itemView.findViewById(R.id.title);
            decrTextView = itemView.findViewById(R.id.description);
            dueTextView = itemView.findViewById(R.id.due_text);
            dueView = itemView.findViewById(R.id.due);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.___item_task, parent, false);
        return new ViewHolder(v, mFragment);
    }

    @Override
    public void onBindViewHolder(ViewHolder h, int position) {
        super.onBindViewHolder(h, position);
        SubjectTask subject = getItem(position);
        Resources res = mFragment.getResources();

        // Bind description view
        h.titleTextView.setText(subject.title);
        if (!TextUtils.isEmpty(subject.description)) {
            h.decrTextView.setText(subject.description);
            h.decrTextView.setVisibility(View.VISIBLE);
        } else h.decrTextView.setVisibility(View.GONE);

        // Bind due-time view
        if (subject.due > 0) {
            int y, m, d;
            // Select previously selected day
            y = DateUtilz.getYear(subject.due);
            m = DateUtilz.getMonth(subject.due);
            d = DateUtilz.getDay(subject.due);

            if (y != mCurrentYear) {
                h.dueTextView.setText(res.getString(
                        R.string.subject_stream_task_due_year,
                        mMonths[m], d, y));
            } else h.dueTextView.setText(res.getString(
                    R.string.subject_stream_task_due,
                    mMonths[m], d));

            h.dueView.setVisibility(View.VISIBLE);
        } else h.dueView.setVisibility(View.GONE);
    }

}
