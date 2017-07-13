package com.artemchep.horario._new.adapters;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artemchep.horario.R;
import com.artemchep.horario._new.fragments.FragmentModel;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.ui.widgets.UserView;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class UsersAdapter extends AdapterModel<User, UsersAdapter.ViewHolder> {

    public UsersAdapter(@NonNull FragmentModel<User> fragment, @NonNull List<User> list) {
        super(fragment, list);
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class ViewHolder extends AdapterModel.ViewHolder<User> {

        @NonNull
        final UserView userView;

        public ViewHolder(View itemView, FragmentModel<User> fragment) {
            super(itemView, fragment);
            userView = itemView.findViewById(R.id.user);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.___item_user, parent, false);
        return new ViewHolder(v, mFragment);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        User user = getItem(position);
        holder.userView.bind(user);
    }

}
