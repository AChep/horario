package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.artemchep.horario.R
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.interfaces.Unique
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.UsersAdapter
import com.artemchep.horario.ui.utils.EXTRA_PERSON
import com.artemchep.horario.ui.utils.EXTRA_PERSON_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*

/**
 * @author Artem Chepurnoy
 */
abstract class FragmentStoreUsers<T : IUser> : FragmentStore<T>(), AdapterBase.OnItemClickListener {

    companion object {
        const val TAG = "FragmentStoreUsers"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    override val filter: (T) -> Boolean = { !it.name.isNullOrEmpty() }
    override val comparator: Comparator<T> = compareBy(
            { it.name },
            { it.id }
    )

    /**
     * Map of the registered pairs of reference-listeners; we use it
     * to fetch user data by key.
     */
    private val map = HashMap<String, ListenerRegistration>()

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<T>): AdapterBase<T, *> {
        return UsersAdapter(list).apply {
            yourUserId = userId
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onStop() {
        map.values.forEach { it.remove() }
        map.clear()

        super.onStop()
    }

    override fun onItemClick(view: View, pos: Int) {
        val data = adapter.getItem(pos)
        val args = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_PERSON_ID, data.id!!)
            putParcelable(EXTRA_PERSON, data)
        }

        // Start user fragment
        val host = activity as FragmentHost
        host.fragmentShow(UserFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    // --------------------------
    // --  STORE ----------------
    // --------------------------

    abstract fun getModel(snapshot: DocumentSnapshot, user: T): T?

    // --------------------------
    // --  DATABASE OVERRIDE ----
    // --------------------------

    override fun performPutModel(model: T): T? {
        val key = model.getKey()

        // Re-use previous subject; this should be the case after restoring models
        // from instance state.
        val i = Unique.Utils.indexOf(aggregator.modelsAll, key)
        return super.performPutModel(if (i >= 0) aggregator.modelsAll[i] else model)
    }

    override fun onEvent(snapshot: QuerySnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            return
        }

        snapshot?.documentChanges?.forEach {
            when (it.type) {
                DocumentChange.Type.REMOVED -> {
                    val userKey = it.document.id
                    map.remove(userKey)!!.remove()
                }
                DocumentChange.Type.ADDED -> {
                    val userKey = it.document.id

                    // Register value event listener for
                    // the content of a post
                    val ref = firestore.document("users/$userKey")
                    val listener = EventListener<DocumentSnapshot> { snapshot, e ->
                        if (e != null) {
                            return@EventListener
                        }

                        val i = Unique.Utils.indexOf(aggregator.modelsAll, userKey)
                        if (i >= 0) {
                            val user = getModel(snapshot, aggregator.modelsAll[i])
                            aggregator.put(user ?: getModel(snapshot)) // update existing subject
                        }
                    }

                    val registration = ref.addSnapshotListener(listener)
                    map.put(userKey, registration)
                }
                else -> {
                }
            }
        }
    }

}