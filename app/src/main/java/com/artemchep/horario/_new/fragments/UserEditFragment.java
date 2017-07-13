package com.artemchep.horario._new.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.artemchep.horario.R;
import com.artemchep.horario.database.Persy;
import com.artemchep.horario.database.models.User;
import com.artemchep.horario.ui.UiHelper;
import com.artemchep.horario.ui.activities.ActivityHorario;
import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.stfalcon.frescoimageviewer.ImageViewer;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Artem Chepurnoy
 */
public class UserEditFragment extends FragmentBase implements View.OnClickListener, Toolbar.OnMenuItemClickListener {

    public static final String EXTRA_USER = "extra::user";
    public static final String EXTRA_USER_ID = "extra::user::id";

    private static final int PICK_IMAGE_REQUEST = 228;

    private static String[] ANIMALS = new String[]{
            "Alligator", "Anteater", "Armadillo", "Auroch",
            "Axolotl", "Badger", "Bat", "Beaver",
            "Buffalo", "Camel", "Chameleon", "Cheetah",
            "Chipmunk", "Chinchilla", "Chupacabra", "Cormorant",
            "Coyote", "Crow", "Dingo", "Dinosaur",
            "Dolphin", "Duck", "Elephant", "Ferret",
            "Fox", "Frog", "Giraffe", "Gopher",
            "Grizzly", "Hedgehog", "Hippo", "Hyena",
            "Jackal", "Ibex", "Ifrit", "Iguana",
            "Koala", "Kraken", "Lemur", "Leopard",
            "Liger", "Llama", "Manatee", "Mink",
            "Monkey", "Narwhal", "Nyan Cat", "Orangutan",
            "Otter", "Panda", "Penguin", "Platypus",
            "Python", "Pumpkin", "Guagga", "Rabbit",
            "Raccoon", "Rhino", "Sheep", "Shrew",
            "Skunk", "Slow Loris", "Squirrel", "Turtle",
            "Walrus", "Wolf", "Wolverine", "Wombat"
    };

    private User mUser;
    private String mUserId;

    private Toolbar mToolbar;
    private ImageView mUserAvatarImageView;
    private TextView mUserNameTextView;
    private EditText mInfoEmailEditView;
    private EditText mInfoUserNameEditView;
    private EditText mInfoUserDetailsEditView;

    private Persy.Watcher<User> mWatcher;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        mUser = args.getParcelable(EXTRA_USER);
        mUser = mUser.clone();
        mUserId = args.getString(EXTRA_USER_ID, "");

        ActivityHorario activity = (ActivityHorario) getActivity();
        mWatcher = activity.getPersy().watchFor(User.class, "users/" + mUserId);
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.___fragment_user_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mToolbar = view.findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.user_edit);
        mToolbar.setOnMenuItemClickListener(this);
        mUserAvatarImageView = (CircleImageView) view.findViewById(R.id.user_avatar);
        mUserAvatarImageView.setOnClickListener(this);

        mInfoEmailEditView = view.findViewById(R.id.info_email);
        mInfoUserNameEditView = view.findViewById(R.id.info_user_name);
        mInfoUserDetailsEditView = view.findViewById(R.id.info_user_details);

        bind(mUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PICK_IMAGE_REQUEST: {
                break;
            }
        }
    }

    private void bind(@Nullable User user) {
        if (user == null) {
            mUserAvatarImageView.setImageDrawable(null);
            return;
        }

        mInfoEmailEditView.setText(user.email);
        mInfoUserNameEditView.setText(user.name);
        mInfoUserDetailsEditView.setText(user.info);

        // Load avatar
        Glide.with(this)
                .load(user.avatarUrl)
                .fallback(R.drawable.ic_pencil_grey600_24dp)
                .into(mUserAvatarImageView);
    }

    /**
     * Generate Google Docs like name (Anonymous Nyan Cat, etc.)
     * for given key.
     */
    @NonNull
    private String generateNameFor(@NonNull String key) {
        return String.format("Anonymous %s", ANIMALS[key.hashCode() % ANIMALS.length]);
    }

    private void selectAvatar() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab: {
                break;
            }
            case R.id.user_avatar: {
                // TODO: https://github.com/chrisbanes/PhotoView
                //noinspection unchecked
                new ImageViewer.Builder(getContext(), new String[]{
                        "https://habrastorage.org/getpro/tmtm/pictures/6cd/cc9/763/6cdcc97634f82f7c12590bbd2e56ff14.jpg"
                }).show();
                break;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                updateCurrentUser();
                mWatcher.getDatabase().setValue(mUser);
                finish();
                break;
            default:
                return false;
        }
        return true;
    }

    private void updateCurrentUser() {
        mUser.email = mInfoEmailEditView.getText().toString();
        mUser.name = mInfoUserNameEditView.getText().toString();
        mUser.info = mInfoUserDetailsEditView.getText().toString();
    }

}
