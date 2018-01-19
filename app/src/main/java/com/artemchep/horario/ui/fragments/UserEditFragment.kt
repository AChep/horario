package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.artemchep.horario.R
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.EXTRA_USER
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import timber.log.Timber


/**
 * @author Artem Chepurnoy
 */
class UserEditFragment : FragmentBase(), View.OnClickListener {

    companion object {
        const val TAG = "UserEditFragment"
        const val STATE_USER = "state::user"
    }

    private lateinit var userId: String
    private lateinit var origin: User
    private lateinit var user: User

    private lateinit var fab: FloatingActionButton
    private lateinit var toolbar: Toolbar
    private lateinit var nameEditView: EditText
    private lateinit var biographyEditView: EditText
    private lateinit var emailEditView: EditText

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        origin = arguments!!.getParcelable<IUser>(EXTRA_USER)?.let {
            User(
                    id = it.id,
                    name = it.name,
                    email = it.email,
                    biography = it.biography,
                    avatarUrl = it.avatarUrl
            )
        } ?: User()

        user = if (savedInstanceState != null) {
            savedInstanceState.getParcelable(STATE_USER)
        } else origin.copy()
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_user_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener(this@UserEditFragment)
        }
        toolbar = view.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { _ ->
                val host = activity as FragmentHost
                host.fragmentFinish(this@UserEditFragment)
            }
        }
        biographyEditView = view.findViewById(R.id.user_biography)
        emailEditView = view.findViewById(R.id.email)
        nameEditView = view.findViewById<EditText>(R.id.user_name).apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

                override fun afterTextChanged(editable: Editable) {
                    if (nameEditView.text.isNullOrEmpty()) {
                        fab.hide()
                    } else fab.show()
                }
            })
        }

        nameEditView.setText(user.name)
        emailEditView.setText(user.email)
        biographyEditView.setText(user.biography)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        updateCurrentUser()
        outState.putParcelable(STATE_USER, user)
    }

    private fun updateCurrentUser() {
        user.run {
            name = nameEditView.text.toString()
            email = emailEditView.text.toString()
            biography = biographyEditView.text.toString()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab -> {
                updateCurrentUser()

                val success = dbSave()
                if (success) {
                    val host = activity as FragmentHost
                    host.fragmentFinish(this)
                }
            }
        }
    }

    private fun dbSave(): Boolean {
        val process: String?.() -> String? = {
            this?.trim()
                    ?.replace("\n", "")
                    ?.replace("\r", "")
        }

        user.name = user.name?.process()
        user.email = user.email?.process()
        user.biography = user.biography?.trim()

        nameEditView.setText(user.name)

        // Client-side data rules
        if (user.name.isNullOrBlank()) {
            return false
        }

        val firestore = FirebaseFirestore.getInstance()
        val doc = firestore.document("users/$userId")

        val curr: MutableMap<String, Any?> = user.deflateUser()
        val prev: MutableMap<String, Any?> = origin.deflateUser()

        // Update only changed entries
        val map = curr.filter { prev[it.key] != it.value }
        map.takeUnless { map.isEmpty() }?.also {
            Timber.tag(TAG).i("User MERGE: [$map]")
            doc.set(it, SetOptions.merge())
        }
        return true
    }

}
