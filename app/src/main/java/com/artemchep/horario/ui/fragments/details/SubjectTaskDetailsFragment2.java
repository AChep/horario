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
package com.artemchep.horario.ui.fragments.details;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.ChangeBounds;
import android.support.transition.Fade;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.artemchep.horario.Hierarchy;
import com.artemchep.horario.R;
import com.artemchep.horario.aggregator.Aggregator;
import com.artemchep.horario.aggregator.Filter;
import com.artemchep.horario.billing.SkuUi;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.SubjectTask;
import com.artemchep.horario.database.models.SubjectTaskComment;
import com.artemchep.horario.database.navigation.Dbb;
import com.artemchep.horario.models.Model;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.artemchep.horario._new.fragments.FragmentBase;
import com.artemchep.horario.ui.widgets.PrettyTimeView;
import com.artemchep.horario.ui.widgets.UserView;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.dmoral.toasty.Toasty;

/**
 * @author Artem Chepurnoy
 */
public class SubjectTaskDetailsFragment2 extends FragmentBase implements View.OnClickListener {

    public static final String EXTRA_SUBJECT_ID = "extra::subject_id";
    public static final String EXTRA_GROUP_ID = "extra::group_id";
    public static final String EXTRA_USER_ID = "extra::user_id";
    public static final String EXTRA_TASK_ID = "extra::task_id";
    public static final String EXTRA_TASK = "extra::task_model";

    private static final String STATE_TASK_INFO = "state::task_info";

    private Toolbar mToolbar;

    private String mSubjectId;
    private String mGroupId;
    private String mTaskId;
    private String mUserId;
    private DatabaseReference mTaskRef;
    private SubjectTask mTaskInfo;

    private ImageView mCommentSend;
    private EditText mCommentEditText;
    private TextView mTitleTextView;
    private HtmlTextView mDescrTextView;
    // due
    private View mDueContainer;
    private TextView mDueTextView;
    // footer
    private UserView mUserView;

    private View mFooterHeaderView;
    private TextView mFooterHeaderTitleView;
    private UserView mFooterCommentView;
    private PrettyTimeView mFooterCommentTimeView;
    private TextView mFooterCommentTextView;
    private TransitionSet mFooterTransition;
    private ViewGroup mFooterView;
    private SubjectTaskComment mFooterComment;
    private SubjectTask mTaskInfo2;

    public static class CommentDecorator extends Model implements Hierarchy.Item {

        public final SubjectTaskComment comment;
        public String sorting;
        public String sort;
        public int poo;

        CommentDecorator(SubjectTaskComment comment) {
            this.comment = comment;
            this.key = comment.key;

            long now = comment.timestamp > 0 ? comment.timestamp : Long.MAX_VALUE;
            sort = Long.toString(now, Character.MAX_RADIX);
            int size = sort.length();
            if (size < 14) {
                char[] c = new char[14 - size];
                Arrays.fill(c, '0');
                sort = new String(c) + sort;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        @NonNull
        @Override
        public String getKey() {
            return comment.key;
        }

        @NonNull
        @Override
        public String getSortKey() {
            return sort;
        }

        @Nullable
        @Override
        public String getParentKey() {
            return comment.parent;
        }
    }

    @NonNull
    private final ValueEventListener mInfoEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                bind(dataSnapshot.getValue(SubjectTask.class));
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    private Persy.Watcher<SubjectTaskComment> mWatcher;
    private CommentAdapter mAdapter;


    /**
     * Subscribed to the list of comments and passes them to
     * {@link #mCommentHierarchy hierarchy manager}.
     */
    @NonNull
    private final ChildEventListener mCommentsEventListener = new Persy.ChildEventListenerAdapter() {

        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            SubjectTaskComment model = mWatcher.getModel();
            mCommentHierarchy.put(new CommentDecorator(model));
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            SubjectTaskComment model = mWatcher.getModel();
            mCommentHierarchy.put(new CommentDecorator(model));
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            SubjectTaskComment model = mWatcher.getModel();
            mCommentHierarchy.remove(new CommentDecorator(model));
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }

    };

    /**
     *
     */
    @NonNull
    private final Hierarchy mCommentHierarchy = new Hierarchy(new Hierarchy.Observer() {

        @Override
        public void put(@NonNull Hierarchy.ItemDecorator model) {
            CommentDecorator d = (CommentDecorator) model.mItem;
            d.sorting = model.sorting;
            d.poo = model.poo;
            mAggregator.put(d);
        }

        @Override
        public void remove(@NonNull Hierarchy.ItemDecorator model) {
            CommentDecorator d = (CommentDecorator) model.mItem;
            mAggregator.remove(d);
        }

    });


    private Aggregator<CommentDecorator> mAggregator = new Aggregator<>(new Filter<CommentDecorator>(),
            new Comparator<CommentDecorator>() {
                @Override
                public int compare(CommentDecorator a, CommentDecorator b) {
                    return a.sorting.compareTo(b.sorting);
                }
            }, new Aggregator.Observer<CommentDecorator>() {

        @Override
        public void add(@NonNull CommentDecorator model, int i) {
            mAdapter.notifyItemInserted(i);
        }

        @Override
        public void set(@NonNull CommentDecorator model, int i) {
            mAdapter.notifyItemChanged(i);
        }

        @Override
        public void remove(@NonNull CommentDecorator model, int i) {
            mAdapter.notifyItemRemoved(i);
            mAggregator.refilter();
        }

        @Override
        public void move(@NonNull CommentDecorator model, int from, int to) {
            mAdapter.notifyItemRemoved(from);
            mAdapter.notifyItemInserted(to);
        }

        @Override
        public void avalanche() {
            mAdapter.notifyDataSetChanged();
        }
    });

    /**
     * @author Artem Chepurnoy
     */
    private enum FooterMode {
        REPLY,
        EDIT,
        COMMENT
    }

    private FooterMode mFooterMode = FooterMode.COMMENT;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Bundle args = getArguments();
        mSubjectId = "sdf";//args.getString(EXTRA_SUBJECT_ID);
        mGroupId = "sdf";//args.getString(EXTRA_GROUP_ID);
        mTaskId = "sdf";//args.getString(EXTRA_TASK_ID);
        mUserId = "sdf";//args.getString(EXTRA_USER_ID);

        ActivityHorario activity = (ActivityHorario) getActivity();
        mTaskRef = FirebaseDatabase.getInstance().getReference()
                .child(Dbb.SUBJECTS).child(mSubjectId)
                .child(Dbb.SUBJECT_GROUPS).child(mGroupId)
                .child(Dbb.SUBJECT_GROUP_TASK).child(mTaskId);
        mWatcher = activity.getPersy().watchFor(SubjectTaskComment.class,
                Dbb.SUBJECTS + "/" + mSubjectId + "/comments/" + mTaskId + "/"
                        + Dbb.SUBJECT_GROUP_TASK_COMMENTS
        );

        mFooterTransition = new TransitionSet()
                .setOrdering(TransitionSet.ORDERING_TOGETHER)
                .addTransition(new ChangeBounds())
                .addTransition(new Fade());
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subject_task_details2, container, false);

        mCommentSend = (ImageView) view.findViewById(R.id.footer_action);
        mCommentSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mCommentEditText.getText().toString();
                boolean sent = comment(text);

                // Reset edit text if sent
                if (sent) {
                    mCommentEditText.setText("");
                }
            }
        });
        mCommentEditText = (EditText) view.findViewById(R.id.footer_field);
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mCommentSend.setEnabled(s.length() > 0);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedState) {
        super.onViewCreated(view, savedState);
        List<SkuUi> list = new ArrayList<>();

        mAdapter = new CommentAdapter(new HashSet<String>(), mAggregator.getModels(), this);
        RecyclerView rv = (RecyclerView) view.findViewById(R.id.recycler);
        rv.setLayoutManager(new LinearLayoutManager(getActivity()));
        rv.setNestedScrollingEnabled(false);
        rv.setAdapter(mAdapter);

//        RecyclerViewHeader header = (RecyclerViewHeader) view.findViewById(R.id.header);
//        header.attachTo(rv);
        mTitleTextView = (TextView) view.findViewById(R.id.title);
        mDescrTextView = (HtmlTextView) view.findViewById(R.id.description);
        mDueContainer = view.findViewById(R.id.due);
        mDueContainer.setOnClickListener(this);
        mDueTextView = (TextView) mDueContainer.findViewById(R.id.due_text);
        mUserView = (UserView) view.findViewById(R.id.user);
        mUserView.findViewById(R.id.user_name).setOnClickListener(this);

        mFooterView = (ViewGroup) view.findViewById(R.id.footer);
        mFooterHeaderView = mFooterView.findViewById(R.id.footer_header);
        mFooterHeaderView.findViewById(R.id.footer_header_close).setOnClickListener(this);
        mFooterHeaderTitleView = (TextView) mFooterHeaderView.findViewById(R.id.footer_header_title);
        mFooterCommentView = (UserView) mFooterView.findViewById(R.id.footer_comment);
        mFooterCommentTimeView = (PrettyTimeView) mFooterCommentView.findViewById(R.id.footer_comment_time);
        mFooterCommentTextView = (TextView) mFooterCommentView.findViewById(R.id.footer_comment_text);

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.subject_task_details);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_left_grey600_24dp);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_edit:
                        Bundle args = new Bundle();
                        args.putString(SubjectTaskEditFragment.EXTRA_SUBJECT_ID, "-KlnzthUb_8St7Ax0UT0");
                        args.putString(SubjectTaskEditFragment.EXTRA_GROUP_ID, "keykeykey");
                        args.putString(SubjectTaskEditFragment.EXTRA_TASK_ID, "keykeykey");
                        args.putString(SubjectTaskEditFragment.EXTRA_USER_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");
                        args.putParcelable(SubjectTaskEditFragment.EXTRA_TASK, mTaskInfo2);

                        Intent intent = new Intent(getContext(), ChildActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, SubjectTaskEditFragment.class.getName());
                        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
                        startActivity(intent);
                        break;
                }
                return true;
            }
        });

        SubjectTask task = new SubjectTask();
        task.title = "Computer Network";
        task.description = "Computer Network";
        task.due = 30;
        bind(task);
    }

    @Override
    public void onStart() {
        super.onStart();

        mWatcher.addListener(mCommentsEventListener);
        mTaskRef.addValueEventListener(mInfoEventListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        mWatcher.removeListener(mCommentsEventListener);
        mTaskRef.removeEventListener(mCommentsEventListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_name:
                break;
            case R.id.due: {
                Context context = getContext();
                Toasty.info(context, "3 days left").show();
                break;
            }
            case R.id.footer_header_close: {
                switchFooterMode(FooterMode.COMMENT, null);
                break;
            }
        }
    }

    private void bind(@NonNull SubjectTask info) {
        mTaskInfo2 = info;

        mTitleTextView.setText(info.title);
        if (info.descriptionHtml != null) {
            mDescrTextView.setHtml(info.descriptionHtml);
        } else mDescrTextView.setText(null);

        // Display author
        mUserView.setUser("YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");

        // Display due time
        if (info.due > 0 || true) {
            // TODO: Due time: String resources
            String time = "14:35";
            mDueContainer.setVisibility(View.VISIBLE);
            mDueTextView.setText("due " + time);
        } else mDueContainer.setVisibility(View.GONE); // this task has no due time

        // Set the type of the task as a title
        // of this fragment.
        String title;
        switch (info.type) {
            case SubjectTask.TYPE_QUESTION:
                title = "Question";
                break;
            case SubjectTask.TYPE_ASSIGNMENT:
                title = "Assignment";
                break;
            case SubjectTask.TYPE_ANNOUNCEMENT:
            default:
                title = "Announcement";
                break;
        }
        mToolbar.setTitle(title);
    }

    private boolean comment(@NonNull String text) {
        if (TextUtils.isEmpty(text)) {
            return false; // do not comment this
        }

        SubjectTaskComment comment = new SubjectTaskComment();
        switch (mFooterMode) {
            case EDIT:
                mTaskRef.child(Dbb.SUBJECT_GROUP_TASK_COMMENTS)
                        .child(mFooterComment.key).child("text")
                        .setValue(text);
                break;
            case REPLY:
                comment.parent = mFooterComment.key;
            case COMMENT:
                comment.text = text;
                comment.author = mUserId;
                comment.timestamp = 0; // will be set by database trigger, you can not set it manually
                mTaskRef.child(Dbb.SUBJECT_GROUP_TASK_COMMENTS).push().setValue(comment);
                break;
        }

        switchFooterMode(FooterMode.COMMENT, null);
        return true;
    }

    public void onItemClick(@NonNull View view, final @NonNull SubjectTaskComment item) {
        List<String> options = new ArrayList<>();
        options.add(getString(R.string.subject_stream_comment_reply));
        options.add(getString(R.string.subject_stream_comment_copy));

        if (mUserId.equals(item.author)) {
            options.add(getString(R.string.subject_stream_comment_edit));
            options.add(getString(R.string.subject_stream_comment_delete));
        }

        new MaterialDialog.Builder(getContext())
                .title(R.string.subject_stream_comment_options)
                .items(options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View itemView, int position, CharSequence text) {
                        switch (position) {
                            case 0: // reply
                                switchFooterMode(FooterMode.REPLY, item);
                                break;
                            case 1: { // copy text
                                ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("Comment text", item.text);
                                clipboard.setPrimaryClip(clip);

                                // Show toast message
                                String msg = getString(R.string.clipboard_copied_text);
                                Toasty.info(getContext(), msg).show();
                                break;
                            }
                            case 2: // edit
                                switchFooterMode(FooterMode.EDIT, item);
                                break;
                            case 3: // delete
                                mTaskRef.child(Dbb.SUBJECT_GROUP_TASK_COMMENTS)
                                        .child(item.key).setValue(null);
                                break;
                        }
                    }
                })
                .show();
    }

    private void switchFooterMode(@NonNull FooterMode mode, @Nullable SubjectTaskComment comment) {
        TransitionManager.beginDelayedTransition(mFooterView, mFooterTransition);
        mFooterMode = mode;
        mFooterComment = comment;
        mCommentEditText.setText(null);

        if (mode == FooterMode.COMMENT) {
            mCommentSend.setImageResource(R.drawable.ic_send_grey600_24dp);
            mFooterHeaderView.setVisibility(View.GONE);
            mFooterCommentView.setVisibility(View.GONE);
            mFooterCommentView.setUser(null);
            mFooterCommentTimeView.setTime(0);

            mCommentEditText.clearFocus();
            return;
        }

        assert comment != null;

        String title;
        if (mode == FooterMode.EDIT) {
            title = getString(R.string.subject_stream_context_edit);
            mCommentSend.setImageResource(R.drawable.ic_check_grey600_24dp);
            mCommentEditText.setText(comment.text);
            mCommentEditText.post(new Runnable() {
                @Override
                public void run() {
                    mCommentEditText.setSelection(mCommentEditText.length());
                }
            });
        } else if (mode == FooterMode.REPLY) {
            title = getString(R.string.subject_stream_context_reply);
            mCommentSend.setImageResource(R.drawable.ic_reply_grey600_24dp);
        } else title = "Unknown";

        mFooterHeaderView.setVisibility(View.VISIBLE);
        mFooterHeaderTitleView.setText(title);
        mFooterCommentView.setVisibility(View.VISIBLE);
        mFooterCommentView.setUser(comment.author);
        mFooterCommentTimeView.setTime(comment.timestamp);
        mFooterCommentTextView.setText(comment.text);

        mCommentEditText.requestFocus();
        mCommentEditText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(mCommentEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    /**
     * @author Artem Chepurnoy
     */
    public static class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

        @NonNull
        private final SubjectTaskDetailsFragment2 mFragment;
        @NonNull
        private final List<CommentDecorator> mList;
        @NonNull
        private final Set<String> mAuthors;

        /**
         * @author Artem Chepurnoy
         */
        static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            final UserView userView;
            final ImageView avatarView;
            final TextView textTextView;
            final TextView hierarchyTextView;
            final PrettyTimeView timeTextView;
            final View spaceTextView;
            final View crownTextView;

            private final SubjectTaskDetailsFragment2 mFragment;

            ViewHolder(View itemView, SubjectTaskDetailsFragment2 fragment) {
                super(itemView);
                itemView.setOnClickListener(this);
                userView = (UserView) itemView.findViewById(R.id.user);
                avatarView = userView.getAvatarView();
                avatarView.setOnClickListener(this);
                textTextView = (TextView) itemView.findViewById(R.id.text);
                hierarchyTextView = (TextView) itemView.findViewById(R.id.hierarchy);
                timeTextView = (PrettyTimeView) itemView.findViewById(R.id.time);
                spaceTextView = itemView.findViewById(R.id.space);
                crownTextView = itemView.findViewById(R.id.user_crown);

                mFragment = fragment;
            }

            @Override
            public void onClick(View v) {
                if (v == itemView) {
                    SubjectTaskComment comment = getItem();
                    if (comment != null) mFragment.onItemClick(v, comment);
                } else if (v == avatarView) {

                }
            }

            /**
             * @return the item associated with this holder,
             * {@code null} if no one.
             */
            @Nullable
            protected SubjectTaskComment getItem() {
                int i = getAdapterPosition();
                return i != RecyclerView.NO_POSITION
                        ? mFragment.mAdapter.getItem(i)
                        : null;
            }

        }

        public CommentAdapter(
                @NonNull Set<String> authors,
                @NonNull List<CommentDecorator> list,
                @NonNull SubjectTaskDetailsFragment2 fragment) {
            mList = list;
            mAuthors = authors;
            mFragment = fragment;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View v = inflater.inflate(R.layout.item_comment, parent, false);
            return new ViewHolder(v, mFragment);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SubjectTaskComment comment = getItem(position);

            // Display a crown for subject's editors, so
            // people know trusted ones
            if (mAuthors.contains(comment.author)) {
                holder.crownTextView.setVisibility(View.VISIBLE);
            } else holder.crownTextView.setVisibility(View.GONE);

            // Display how old this comment is in relative time
            if (comment.timestamp > 0) {
                holder.timeTextView.setTime(comment.timestamp);
                holder.timeTextView.setVisibility(View.VISIBLE);
            } else {
                holder.timeTextView.setTime(0);
                holder.timeTextView.setVisibility(View.GONE);
            }

            holder.userView.setUser(comment.author);
            holder.textTextView.setText(comment.text);


            if (comment.parent != null) {
                holder.hierarchyTextView.setText(Integer.toString(mList.get(position).poo + 1));
                holder.spaceTextView.setVisibility(View.VISIBLE);
                holder.spaceTextView.setLayoutParams(new RelativeLayout.LayoutParams(64 * mList.get(position).poo, 1));
            } else holder.spaceTextView.setVisibility(View.GONE);

        }

        @Override
        public int getItemCount() {
            return mList.size();
        }

        @NonNull
        public List<CommentDecorator> getList() {
            return mList;
        }

        @NonNull
        public SubjectTaskComment getItem(int position) {
            return mList.get(position).comment;
        }

    }

}