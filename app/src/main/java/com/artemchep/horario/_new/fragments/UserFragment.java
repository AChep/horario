package com.artemchep.horario._new.fragments;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.artemchep.basic.Device;
import com.artemchep.horario.R;
import com.artemchep.horario._new.Username;
import com.artemchep.horario._new.activities.GalleryActivity;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.artemchep.horario.ui.activities.ChildActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Artem Chepurnoy
 */
public class UserFragment extends FragmentBase implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_USER = "extra::user";
    public static final String EXTRA_USER_ID = "extra::user::id";

    private User mUser;
    private String mUserId;

    private AppBarLayout mAppBar;
    private Toolbar mToolbar;
    private ImageView mUserAvatarImageView;
    private TextView mUserNameTextView;
    private TextView mUserDetailsTextView;
    private TextView mInfoEmailTextView;
    private MenuItem mEditMenuItem;

    private Persy.Watcher<User> mWatcher;

    @NonNull
    private final ValueEventListener mValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if (dataSnapshot.exists()) {
                mUser = dataSnapshot.getValue(User.class);
                mUser.key = dataSnapshot.getKey();
            } else mUser = null;
            bind(mUser);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        mUser = args.getParcelable(EXTRA_USER);
        mUserId = args.getString(EXTRA_USER_ID, "");

        ActivityHorario activity = (ActivityHorario) getActivity();
        mWatcher = activity.getPersy().watchFor(User.class, "users/" + mUserId);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_user, container, false);
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
        mToolbar.inflateMenu(R.menu.user);
        mToolbar.setOnMenuItemClickListener(this);
        mToolbar.setNavigationIcon(isDarkTheme
                ? R.drawable.ic_arrow_left_white_24dp
                : R.drawable.ic_arrow_left_grey600_24dp);
        mEditMenuItem = mToolbar.getMenu().findItem(R.id.action_edit);
        mUserAvatarImageView = (CircleImageView) view.findViewById(R.id.user_avatar);
        mUserNameTextView = view.findViewById(R.id.user_name);
        mUserDetailsTextView = view.findViewById(R.id.user_details);
        view.findViewById(R.id.fab).setOnClickListener(this);

        mInfoEmailTextView = view.findViewById(R.id.info_email);
        mInfoEmailTextView.setOnClickListener(this);

        if (Device.hasLollipopApi()) {
            setupLollipop();
        }

        bind(mUser);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupLollipop() {
        NestedScrollView scrollView = getView().findViewById(R.id.scroll_view);
        scrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {

            private float elevation = getResources().getDimensionPixelSize(R.dimen.elevation_toolbar);
            private boolean scrolled;

            @Override
            public void onScrollChange(
                    NestedScrollView v,
                    int scrollX, int scrollY,
                    int oldScrollX, int oldScrollY) {
                boolean s = v.canScrollVertically(-1);
                if (scrolled == s) {
                    return;
                } else scrolled = s;

                mAppBar.animate().z(s ? elevation : 0);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        User user = mWatcher.getMap().get(mUserId);
        if (user != null) {
            mUser = user;
            bind(user);
        }

        mWatcher.addListener(mValueEventListener);
    }

    @Override
    public void onStop() {
        mWatcher.removeListener(mValueEventListener);
        super.onStop();
    }

    private void bind(@Nullable User user) {
        updateEditMenuItemVisibility();

        if (user == null) {
            mUserAvatarImageView.setImageDrawable(null);
            mUserAvatarImageView.setOnClickListener(null);
            mUserNameTextView.setText(UiHelper.TEXT_PLACEHOLDER);
            return;
        }

        mUserNameTextView.setText(Username.forUser(user));
        mUserDetailsTextView.setText(user.info);

        if (user.avatarUrl != null) {
            mUserAvatarImageView.setOnClickListener(this);
        } else mUserAvatarImageView.setOnClickListener(null);

        // Load avatar
        Glide.with(this)
                .load(user.avatarUrl)
                .fallback(R.drawable.ic_pencil_grey600_24dp)
                .into(mUserAvatarImageView);

        if (TextUtils.isEmpty(user.email)) {
            mInfoEmailTextView.setVisibility(View.GONE);
        } else {
            mInfoEmailTextView.setVisibility(View.VISIBLE);
            mInfoEmailTextView.setText(user.email);
        }
    }

    private void updateEditMenuItemVisibility() {
        // Hide edit menu item for not your
        // profiles
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !user.getUid().equals(mUserId) || mUser == null) {
            mEditMenuItem.setVisible(false);
        } else mEditMenuItem.setVisible(true);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.info_email: {
                break;
            }
            case R.id.fab: {
                startChatActivity();
                break;
            }
            case R.id.user_avatar: {
                Bundle args = new Bundle();
                ArrayList<String> urls = new ArrayList<>();
                urls.add(mUser.avatarUrl);
                urls.add(mUser.avatarUrl);
                urls.add(mUser.avatarUrl);
                args.putStringArrayList(GalleryFragment.EXTRA_URLS, urls);

                Intent intent = new Intent(getContext(), GalleryActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, GalleryFragment.class.getName());
                intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
                startActivity(intent);
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit:
                startEditActivity();
                break;
            default:
                return false;
        }
        return true;
    }

    private void startEditActivity() {
        Bundle args = new Bundle();
        args.putString(UserFragment.EXTRA_USER_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");
        args.putParcelable(UserFragment.EXTRA_USER, mUser);

        Intent intent = new Intent(getContext(), ChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, UserEditFragment.class.getName());
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
        startActivity(intent);
    }

    private void startChatActivity() {
        Bundle args = new Bundle();
        args.putString(UserFragment.EXTRA_USER_ID, "YhTvBZ5eMTPeuhTKZAh9SeCiVGt1");

        Intent intent = new Intent(getContext(), ChildActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_NAME, ChatFragment.class.getName());
        intent.putExtra(ChildActivity.EXTRA_FRAGMENT_ARGS, args);
        startActivity(intent);
    }

}
