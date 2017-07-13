package com.artemchep.horario._new.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.basic.ui.MultiSelector;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.Message;
import com.artemchep.horario.database.models.SubjectTaskComment;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.database.navigation.Dbb;
import com.artemchep.horario.models.Absence;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.artemchep.horario.ui.activities.MainActivity;
import com.artemchep.horario.ui.fragments.details.SubjectTaskDetailsFragment2;
import com.artemchep.horario.ui.widgets.ChatPanelView;
import com.artemchep.horario.ui.widgets.ChatPanelView2;
import com.artemchep.horario.ui.widgets.PrettyTimeView;
import com.artemchep.horario.ui.widgets.UserView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.RuntimeExecutionException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class ChatFragment extends FragmentBase implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_USER = "extra::user";
    public static final String EXTRA_USER_OWNER_ID = "extra::user1::id";
    public static final String EXTRA_USER_OOHO_ID = "extra::user2::id";

    private User mUser;
    private String mUserId;
    private String mUserYouId;
    private String mUserFriendId;

    private AppBarLayout mAppBar;
    private RecyclerView mRecyclerView;
    private MessageAdapter mAdapter;
    private Toolbar mToolbar;

    private DatabaseReference mDatabaseRef;


    private ChildEventListener mChildEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);
            message.key = dataSnapshot.getKey();
            mAggregator.put(message);
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Message message = dataSnapshot.getValue(Message.class);
            message.key = dataSnapshot.getKey();
            mAggregator.put(message);
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Message message = dataSnapshot.getValue(Message.class);
            message.key = dataSnapshot.getKey();
            mAggregator.remove(message);
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private Aggregator<Message> mAggregator = new Aggregator<>(new Filter<Message>(), new Comparator<Message>() {
        @Override
        public int compare(Message message, Message t1) {
            return 0;
        }
    }, new Aggregator.Observer<Message>() {
        @Override
        public void add(@NonNull Message model, int i) {
            mAdapter.notifyItemInserted(i);
        }

        @Override
        public void set(@NonNull Message model, int i) {
            mAdapter.notifyItemChanged(i);
        }

        @Override
        public void remove(@NonNull Message model, int i) {
            mAdapter.notifyItemRemoved(i);
        }

        @Override
        public void move(@NonNull Message model, int from, int to) {
        }

        @Override
        public void avalanche() {
            mAdapter.notifyDataSetChanged();
        }
    });

    private ChatPanelView2 mChat;

    private MultiSelector<String> mSelector;
    private MaterialCab mCab;
    private MaterialCab.Callback mCabCallback = new MaterialCab.Callback() {
        @Override
        public boolean onCabCreated(MaterialCab cab, Menu menu) {
            mAppBar.setExpanded(true, true);

            // Load theme attrs
            TypedArray a = getActivity().getTheme().obtainStyledAttributes(
                    new int[]{R.attr.icon_close_grey, android.R.attr.textColorPrimary});
            int iconDrawableRes = a.getResourceId(0, 0);
            int titleTextColor = a.getColor(1, Color.BLACK);
            a.recycle();

            cab.setContentInsetStartRes(R.dimen.mcab_default_content_inset);
            cab.setCloseDrawableRes(iconDrawableRes);
            cab.getToolbar().setTitleTextColor(titleTextColor);
            cab.getToolbar().setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bg_cab));

            cab.setMenu(R.menu.___chat);
            return true;
        }

        @Override
        public boolean onCabItemClicked(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_reply: {
                    // Build items list.
                    List<Message> list = new ArrayList<>();
                    for (String key : mSelector.getSelections()) {
                        for (int i = 0; i < mAdapter.getItemCount(); i++) {
                            final Message j = mAdapter.mList.get(i);
                            if (j.key.equals(key)) {
                                list.add(j);
                            }
                        }
                    }

                    MessageStack stack = new MessageStack();
                    stack.messages = new Message[list.size()];
                    int i = 0;
                    for (Message msg : list) {
                        stack.messages[i++] = msg;
                    }
                    mChat.setMode(ChatPanelView.Mode.REPLY, stack);
                    break;
                }
                case R.id.action_forward:
                    break;
                default:
                    return false;
            }
            mCab.finish();
            return true;
        }

        @Override
        public boolean onCabFinished(MaterialCab cab) {
            mSelector.clear();
            cab.getMenu().clear();
            return true;
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ChildActivity activity = (ChildActivity) context;
        mCab = new MaterialCab(activity, R.id.cab_stub);
        mSelector = new MultiSelector<>();
        mSelector.registerListener(new MultiSelector.Callback<String>() {

            @Override
            public void onSelectorStatusChanged(@NonNull MultiSelector<String> selector, boolean isEmpty) {
                if (isEmpty) {
                    mCab.finish();
                } else mCab.start(mCabCallback);
            }

            @Override
            public void onSelectorSelectionsChanged(@NonNull MultiSelector<String> selector) {
                int count = selector.getSelections().size();
                mCab.setTitle(getString(R.string.chat_toolbar_ctx_title, count));
            }

        });

        Bundle args = getArguments();
        mUser = args.getParcelable(EXTRA_USER);
        mUserYouId = args.getString(EXTRA_USER_OWNER_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");
        mUserFriendId = args.getString(EXTRA_USER_OOHO_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");

        String a, b;
        if (mUserYouId.compareTo(mUserFriendId) > 0) {
            a = mUserYouId;
            b = mUserFriendId;
        } else {
            a = mUserFriendId;
            b = mUserYouId;
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("chat_dialogue").child(a).child(b);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load attrs
        TypedArray a = getActivity().getTheme().obtainStyledAttributes(new int[]{
                R.attr.is_dark_theme,
        });
        boolean isDarkTheme = a.getBoolean(0, false);
        a.recycle();

        mAppBar = view.findViewById(R.id.appbar);
        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationIcon(isDarkTheme
                ? R.drawable.ic_arrow_left_white_24dp
                : R.drawable.ic_arrow_left_grey600_24dp);

        mChat = view.findViewById(R.id.chat);
        mChat.setCallback(new ChatPanelView.Callback<MessageStack>() {
            @Override
            public boolean onAction(ChatPanelView view, ChatPanelView.Mode mode, MessageStack object) {
                Message message = new Message();
                message.text = view.getMessage();
                message.author = "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1";
                mDatabaseRef.push().setValue(message);
                return true;
            }
        });

        mAdapter = new MessageAdapter(mAggregator.getModels(), this, mSelector);

        mRecyclerView = view.findViewById(R.id.recycler);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true));
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDatabaseRef.addChildEventListener(mChildEventListener);
    }

    @Override
    public void onStop() {
        mDatabaseRef.removeEventListener(mChildEventListener);
        super.onStop();
    }

    @Override
    public boolean onBackPressed() {
        // Close contextual action bar on first
        // back button press.
        if (mCab.isActive()) {
            mCab.finish();
            return false;
        }
        return super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_email: {
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                break;
            default:
                return false;
        }
        return true;
    }

    private void onItemClick(View v, final Message item) {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.subject_stream_comment_reply));
        options.add(getString(R.string.subject_stream_comment_copy));

        new MaterialDialog.Builder(getContext())
                .title("Message options")
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        switch (position) {
                            case 0: { // reply
                                MessageStack stack = new MessageStack();
                                stack.messages = new Message[]{item};
                                mChat.setMode(ChatPanelView.Mode.REPLY, stack);
                                break;
                            }
                            case 1: { // copy text
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Message text", item.text);
                                clipboard.setPrimaryClip(clip);

                                // Show toast message
                                String msg = getString(R.string.clipboard_copied_text);
                                Toasty.info(getContext(), msg).show();
                                break;
                            }
                        }
                    }
                })
                .show();
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class MessageStack implements Parcelable {

        public Message[] messages;

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    public static class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        private static final int TYPE_INCOMING = 1;
        private static final int TYPE_OUTCOMING = 2;

        @NonNull
        private final ChatFragment mFragment;
        @NonNull
        private final List<Message> mList;
        @NonNull
        private final MultiSelector<String> mSelector;

        /**
         * @author Artem Chepurnoy
         */
        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

            final UserView userView;
            final ImageView avatarView;
            final TextView textTextView;
            final TextView hierarchyTextView;
            final TextView timeTextView;
            final View spaceTextView;
            final View crownTextView;

            private final ChatFragment mFragment;
            private final MultiSelector<String> mSelector;

            ViewHolder(View itemView, ChatFragment fragment, MultiSelector<String> selector) {
                super(itemView);
                itemView.setOnClickListener(this);
                itemView.setLongClickable(true);
                itemView.setOnLongClickListener(this);

                userView = (UserView) itemView.findViewById(R.id.user);
                avatarView = userView.getAvatarView();
                avatarView.setOnClickListener(this);
                textTextView = (TextView) itemView.findViewById(R.id.text);
                hierarchyTextView = (TextView) itemView.findViewById(R.id.hierarchy);
                timeTextView = (TextView) itemView.findViewById(R.id.time);
                spaceTextView = itemView.findViewById(R.id.space);
                crownTextView = itemView.findViewById(R.id.user_crown);

                mFragment = fragment;
                mSelector = selector;
            }

            @Override
            public void onClick(View v) {
                final Message data = getItem();
                if (data == null) return;

                if (mSelector.isEmpty()) {
                    mFragment.onItemClick(v, data);
                } else mSelector.toggle(data.key);
            }

            @Override
            public boolean onLongClick(View v) {
                final Message data = getItem();
                if (data == null || mSelector.contains(data.key)) return false;

                mSelector.add(data.key);
                return true;
            }

            /**
             * @return the item associated with this holder,
             * {@code null} if no one.
             */
            @Nullable
            protected Message getItem() {
                int i = getAdapterPosition();
                return i != RecyclerView.NO_POSITION
                        ? mFragment.mAdapter.mList.get(i)
                        : null;
            }

        }

        public MessageAdapter(
                @NonNull List<Message> list,
                @NonNull ChatFragment fragment,
                @NonNull MultiSelector<String> selector) {
            mList = list;
            mFragment = fragment;
            mSelector = selector;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v;
            switch (viewType) {
                case TYPE_INCOMING:
                    v = inflater.inflate(R.layout.___item_message_in, parent, false);
                    break;
                case TYPE_OUTCOMING:
                    v = inflater.inflate(R.layout.___item_message_out, parent, false);
                    break;
                default:
                    throw new RuntimeException();
            }
            return new ViewHolder(v, mFragment, mSelector);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Message message = mList.get(position);
            String key = message.key;
            mSelector.connect(holder.itemView, key);
            mSelector.bind(holder.itemView, key);

            holder.userView.setUser(message.author);
            holder.textTextView.setText(message.text);
        }

        @Override
        public int getItemViewType(int position) {
            Message message = mList.get(position);
            if ("YhTvBZ5eDTPeuhTKZAh9SeCiVGt1".equals(message.author)) {
                return TYPE_OUTCOMING;
            } else return TYPE_INCOMING;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @NonNull
        public List<Message> getList() {
            return mList;
        }

    }

}
