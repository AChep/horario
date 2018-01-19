package com.artemchep.horario.ui.widgets

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.ui.Username
import com.bumptech.glide.Glide
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * @author Artem Chepurnoy
 */
class UserView : ModelView<IUser> {

    var avatarView: ImageView? = null
        private set
    var bioTextView: TextView? = null
        private set
    var nameView: TextView? = null
        private set

    /**
     * Binds given user to this view
     */
    override fun bind(model: IUser?) {
        if (model == null) {
            // User can be null if document of it is not
            // created yet, or not loaded yet: see STATE
            // variable

            avatarView?.setImageDrawable(null)
            bioTextView?.setTextExclusive(null)
            nameView?.text = when (state) {
                SUCCESS -> {
                    // User document can be un-existent and
                    // in this cause we generate username for
                    // UID.
                    modelId?.let {
                        val username = Username.forId(it)
                        username
                    }
                }
                LOADING -> UiHelper.TEXT_PLACEHOLDER
                else -> null
            }
        } else {
            // Set avatar
            if (model.avatarUrl != null) {
                Glide.with(context)
                        .load(model.avatarUrl)
                        .into(avatarView)
            } else avatarView?.setImageDrawable(null)

            // Set bio & name
            bioTextView?.setTextExclusive(model.biography)
            nameView?.text = Username.forUser(model).let {
                it
            }
        }
    }

    override fun getModel(snapshot: DocumentSnapshot): IUser {
        return User(id = snapshot.id).apply { inflateUser(snapshot) }
    }

    override fun getDocument(firestore: FirebaseFirestore, id: String): DocumentReference {
        return firestore.collection("users").document(id)
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        nameView = findViewById(R.id.user_name)
        avatarView = findViewById(R.id.user_avatar)
        bioTextView = findViewById(R.id.user_biography)
    }

}
