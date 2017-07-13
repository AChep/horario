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

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class StreamAdapter extends AdapterModel<SubjectTask, StreamAdapter.ViewHolder> {

    public StreamAdapter(@NonNull FragmentModel<SubjectTask> fragment, @NonNull List<SubjectTask> list) {
        super(fragment, list);
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class ViewHolder extends AdapterModel.ViewHolder<SubjectTask> {

        @NonNull
        final UserView userView;

        final TextView statusTextView;
        final TextView titleTextView;
        final TextView decrTextView;

        final View dueView;
        final TextView dueTextView;

        public ViewHolder(View itemView, FragmentModel<SubjectTask> fragment) {
            super(itemView, fragment);
            userView = itemView.findViewById(R.id.uss);
            statusTextView = itemView.findViewById(R.id.status);
            titleTextView = itemView.findViewById(R.id.title);
            decrTextView = itemView.findViewById(R.id.description);
            dueView = itemView.findViewById(R.id.due);
            dueTextView = itemView.findViewById(R.id.due_text);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.___item_post, parent, false);
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

        h.userView.setUser(subject.author);
        h.statusTextView.setText("April 20, 18:50");
    }

}
