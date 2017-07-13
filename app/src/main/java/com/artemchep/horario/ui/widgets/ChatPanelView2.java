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
package com.artemchep.horario.ui.widgets;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario._new.fragments.ChatFragment;
import com.artemchep.horario.database.models.Message;

import java.util.List;

/**
 * @author Artem Chepurnoy
 */
public class ChatPanelView2 extends ChatPanelView<ChatFragment.MessageStack> {

    public ChatPanelView2(Context context) {
        super(context);
    }

    public ChatPanelView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateReplyView(@NonNull ChatFragment.MessageStack object) {
        LinearLayout container = new LinearLayout(getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (Message msg : object.messages) {
            View view = inflater.inflate(R.layout.___item_message_in, container, false);

            UserView uv = view.findViewById(R.id.user);
            uv.setUser(msg.author);
            TextView tv = view.findViewById(R.id.text);
            tv.setText(msg.text);

            container.addView(view);
        }

        return container;
    }

    @Override
    protected View onCreateEditView(@NonNull ChatFragment.MessageStack object) {
        return new View(getContext());
    }

}
