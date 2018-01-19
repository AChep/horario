package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.R
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.UsersAdapter
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import es.dmoral.toasty.Toasty
import timber.log.Timber


/**
 * @author Artem Chepurnoy
 */
class SubjectModerateRequestsFragment : FragmentStoreUsers<IUser>() {

    companion object {
        private val TAG = "SubjectModerateRequestsFragment"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var subjectId: String

    private lateinit var requestsQuery: Query

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            subjectId = getString(EXTRA_SUBJECT_ID)
        }

        Timber.tag(TAG).i("Attach: subject_id=$subjectId")

        firestore = FirebaseFirestore.getInstance()
        requestsQuery = firestore.collection("subjects/$subjectId/__requests")
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<IUser>): AdapterBase<IUser, *> = Adapter(list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_moderate_requests, container, false)
    }

    override fun onItemClick(view: View, pos: Int) {
        when (view.id) {
            R.id.btn -> {
                val data = adapter.getItem(pos)
                val userId = data.id
                val userName = data.name

                firestore.batch().apply {
                    // Add as a member
                    run {
                        val map: MutableMap<String, Any?> = hashMapOf(Pair("role", "1"))
                        val ref = firestore.document("subjects/$subjectId/members/$userId")
                        set(ref, map)
                    }

                    // Remove from requests
                    run {
                        val ref = firestore.document("subjects/$subjectId/__requests/$userId")
                        delete(ref)
                    }
                }.commit().addOnFailureListener(activity!!, { task ->
                    Timber.tag(TAG).w("Failed to add a member: " + task.message)

                    val msg = if (userName.isNullOrEmpty()) {
                        "Failed to add a member"
                    } else "Failed to add $userName"
                    Toasty.error(activity!!, msg).show()
                })
            }
            else -> super.onItemClick(view, pos)
        }
    }

    override fun getQuery(): Query = requestsQuery

    override fun getModel(snapshot: DocumentSnapshot): IUser = User(id = snapshot.id)

    override fun getModel(snapshot: DocumentSnapshot, user: IUser): IUser? = User.from(snapshot)

    /**
     * @author Artem Chepurnoy
     */
    private class Adapter(list: List<IUser>) : UsersAdapter<IUser>(list) {

        inner class ViewHolder(v: View) : UsersAdapter<IUser>.ViewHolder(v) {
            init {
                v.findViewById<View>(R.id.btn).setOnClickListener(this)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_user_request, parent, false)
            return ViewHolder(v)
        }
    }

}
