package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.R
import com.artemchep.horario.database.data.Member
import com.artemchep.horario.database.data.Role
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IMember
import com.artemchep.horario.database.models.IRole
import com.artemchep.horario.extensions.withItems
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.UsersAdapter
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.*
import es.dmoral.toasty.Toasty
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectModerateMembersFragment : FragmentStoreUsers<IMember>() {

    companion object {
        private val TAG = "SubjectModerateMembersFragment"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String
    private var role: Role? = null

    private lateinit var memberDoc: FireDocument<IRole>

    private val actionPromoteTo = { role: Int ->
        val doc = firestore.document("subjects/$subjectId/members/$userId")
        val map = HashMap<String, Any?>()

        // Commit changes
        if (role != Role.NONE) {
            doc.set(map.apply {
                put("role", when (role) {
                    Role.OWNER -> "100"
                    Role.ADMIN -> "10"
                    Role.MEMBER -> "1"
                    else -> throw IllegalArgumentException()
                })
            })
        } else doc.delete()
    }

    private val actions = listOf(
            Triple(Role.ADMIN, Role.NONE, "Remove"),
            Triple(Role.ADMIN, Role.MEMBER, "Make member"),
            Triple(Role.OWNER, Role.ADMIN, "Make admin")
    )

    private lateinit var membersRef: CollectionReference

    private val subjectListener = EventListener<DocumentSnapshot> { snapshot, e ->
        role = if (e == null) Role.from(snapshot) else null
    }

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
        }

        Timber.tag(TAG).i("Attach: subject_id=$subjectId")

        firestore = FirebaseFirestore.getInstance()
        membersRef = firestore.collection("subjects/$subjectId/members")
        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        super.onLinkDatabase(manager)

        memberDoc = FireDocument<IRole>("subjects/$subjectId/members/$userId", "member", {
            Role().apply { inflateRole(it) }
        }).also(manager::link)
    }

    override fun onCreateAdapter(list: MutableList<IMember>): AdapterBase<IMember, *> {
        return Adapter(list, {
            memberDoc.value?.let { your ->
                your.role > role || userId == id
            } ?: false
        })
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_moderate_members, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        memberDoc.observer.follow {
            adapter.notifyDataSetChanged()
        }
    }

    override fun onItemClick(view: View, pos: Int) {
        when (view.id) {
            R.id.more -> memberDoc.value?.let {
                val data = adapter.getItem(pos)
                if (it.role < data.role || it.role == data.role && userId != data.id) {
                    Toasty.info(context!!, "No permissions").show()
                    return@let
                }

                val options: MutableList<Pair<String, () -> Any?>> = ArrayList()
                options.add(Pair("Remove from class", {
                    val userId = data.id!!
                    membersRef.document(userId).delete()
                }))

                MaterialDialog.Builder(context!!)
                        .title(data.name ?: "User options")
                        .withItems(options)
                        .show()
            }
            else -> super.onItemClick(view, pos)
        }
    }

    override fun getQuery(): Query = membersRef

    override fun getModel(snapshot: DocumentSnapshot): IMember {
        return Member(id = snapshot.id).apply {
            Role.from(snapshot)?.let { role = it.role }
        }
    }

    override fun getModel(snapshot: DocumentSnapshot, member: IMember): IMember? {
        val user = User.from(snapshot)
        return if (user != null) {
            Member(id = member.id, role = member.role).apply { inflateUser(snapshot) }
        } else null
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Adapter(list: List<IMember>, val hasOptions: IMember.() -> Boolean) : UsersAdapter<IMember>(list) {

        inner class ViewHolder(v: View) : UsersAdapter<IMember>.ViewHolder(v) {
            internal val moreView = v.findViewById<View>(R.id.more)

            init {
                moreView.setOnClickListener(this)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_user_moderated, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(h: UsersAdapter<IMember>.ViewHolder, position: Int) {
            super.onBindViewHolder(h, position)
            val holder = h as ViewHolder
            val member = getItem(position)

            // Owner of the subject should be immutable
            holder.moreView.visibility = if (member.hasOptions()) {
                View.VISIBLE
            } else View.GONE
        }

    }

}