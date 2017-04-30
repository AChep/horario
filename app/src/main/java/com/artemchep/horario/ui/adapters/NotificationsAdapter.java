package com.artemchep.horario.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.artemchep.horario.Palette;
import com.artemchep.horario.R;
import com.artemchep.horario.models.Notification;
import com.artemchep.horario.ui.drawables.CircleDrawable;
import com.artemchep.horario.ui.fragments.master.ModelFragment;
import com.thebluealliance.spectrum.internal.ColorUtil;

import java.util.List;

import static com.artemchep.horario.Palette.PALETTE;

/**
 * @author Artem Chepurnoy
 */
public class NotificationsAdapter extends ModelFragment.BaseAdapter<Notification> {

    /**
     * @author Artem Chepurnoy
     */
    private static class ViewHolder extends ModelFragment.BaseHolder<Notification> {

        final TextView titleView;
        final TextView summaryView;
        final TextView authorView;
        final TextView timestampView;
        final TextView avatarView;
        final CircleDrawable avatarDrawable;

        ViewHolder(@NonNull View v, @NonNull ModelFragment<Notification> fragment) {
            super(v, fragment);
            avatarDrawable = new CircleDrawable();
            avatarView = (TextView) v.findViewById(R.id.color);
            avatarView.setBackground(avatarDrawable);
            titleView = (TextView) v.findViewById(R.id.title);
            summaryView = (TextView) v.findViewById(R.id.summary);
            authorView = (TextView) v.findViewById(R.id.author);
            timestampView = (TextView) v.findViewById(R.id.timestamp);
        }

    }

    public NotificationsAdapter(
            @NonNull ModelFragment<Notification> fragment,
            @NonNull List<Notification> list) {
        super(fragment, list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View v = LayoutInflater.from(context).inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v, getFragment());
    }

    @Override
    public void onBindViewHolder(ModelFragment.BaseHolder<Notification> holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewHolder h = (ViewHolder) holder;
        Notification teacher = getItem(position);

        int color;
        switch (teacher.priority) {
            case Notification.PRIORITY_LOW:
                color = Palette.TEAL; // teal
                break;
            case Notification.PRIORITY_MEDIUM:
                color = Palette.GREEN; // green
                break;
            case Notification.PRIORITY_HIGH:
                color = Palette.RED; // red
                break;
            default:
                color = Palette.GREY; // grey
                break;
        }
        int textColor = ColorUtil.isColorDark(color) ? 0xFFFFFFFF : 0xFF000000;
        h.avatarDrawable.setColor(color);
        h.avatarDrawable.invalidateSelf();
        /*
        if (teacher.name != null && teacher.name.length() > 0) {
            h.avatarView.setText(Character.toString(teacher.name.charAt(0)));
        } else h.avatarView.setText(null);
        */
        h.avatarView.setTextColor(textColor);
        h.titleView.setText(teacher.title);
        //h.timestampView.setText(teacher.timestamp + " TODO");

        if (TextUtils.isEmpty(teacher.author)) {
            h.authorView.setVisibility(View.GONE);
        } else {
            h.authorView.setText(teacher.author);
            h.authorView.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(teacher.summary)) {
            h.summaryView.setVisibility(View.GONE);
        } else {
            h.summaryView.setText(teacher.summary);
            h.summaryView.setVisibility(View.VISIBLE);
        }
    }

}
