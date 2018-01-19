package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectMembersFragment : FragmentStoreUsers<IUser>(), SubjectActivity.Page {

    companion object {
        private val TAG = "SubjectMembersFragment"
    }

    private lateinit var subjectId: String
    private lateinit var membersQuery: Query

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            subjectId = getString(EXTRA_SUBJECT_ID)
        }

        Timber.tag(TAG).i("Attach: subject_id=$subjectId")

        val firestore = FirebaseFirestore.getInstance()
        membersQuery = firestore.collection("subjects/$subjectId/members")
        super.onAttach(context)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_students, container, false)
    }

    override fun getQuery(): Query = membersQuery

    override fun getModel(snapshot: DocumentSnapshot): IUser = User(id = snapshot.id)

    override fun getModel(snapshot: DocumentSnapshot, user: IUser): IUser? = User.from(snapshot)

    override fun onFabClick(view: View) = Unit

    override fun hasFab(): Int = 0
}