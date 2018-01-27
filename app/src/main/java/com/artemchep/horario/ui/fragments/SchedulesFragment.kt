package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario.database.data.Schedule
import com.artemchep.horario.database.models.ISchedule
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.interfaces.Unique
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.SchedulesAdapter
import com.artemchep.horario.ui.utils.*
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class SchedulesFragment : FragmentStore<ISchedule>(), AdapterBase.OnItemClickListener {

    companion object {
        const val TAG = "SchedulesFragment"
    }

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var userId: String

    private lateinit var schedulesQuery: Query

    override val filter: (ISchedule) -> Boolean = { it.name != null }
    override val comparator: Comparator<ISchedule> = compareBy(
            { it.name },
            { it.id }
    )

    /**
     * Map of the registered pairs of reference-listeners; we use it
     * to fetch schedule data by key.
     */
    private val map = HashMap<String, ListenerRegistration>()

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }

        schedulesQuery = firestore.collection("users/$userId/schedules")
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<ISchedule>): AdapterBase<ISchedule, *> = SchedulesAdapter(list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_schedules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onItemClick(view: View, model: ISchedule) {
        super.onItemClick(view, model)
        val args = Bundle().apply {
            putInt(EXTRA_COLOR, Palette.GREY)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, model.subject!!)
            putString(EXTRA_SCHEDULE_ID, model.id!!)
            putParcelable(EXTRA_SCHEDULE, model)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectScheduleFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun onStop() {
        map.values.forEach { it.remove() }
        map.clear()

        super.onStop()
    }

    // --------------------------
    // --  STORE ----------------
    // --------------------------

    override fun getQuery(): Query = schedulesQuery

    override fun getModel(snapshot: DocumentSnapshot): ISchedule {
        // Document has `subject` and `schedule` keys
        // at [users/$userId/schedules]
        return Schedule(id = snapshot.id).apply {
            subject = snapshot.getString("subject")
        }
    }

    fun getModel(snapshot: DocumentSnapshot, schedule: ISchedule): ISchedule? {
        return Schedule(id = snapshot.id).apply {
            inflateSchedule(snapshot)
            // Copy extra params
            subject = schedule.subject
        }
    }

    // --------------------------
    // --  DATABASE OVERRIDE ----
    // --------------------------

    override fun performPutModel(model: ISchedule): ISchedule? {
        val key = model.getKey()

        // Re-use previous schedule; this should be the case after restoring models
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
                    val subjectKey = it.document.getString("subject")
                    val scheduleKey = it.document.getString("schedule")

                    // Register value event listener for
                    // the content of a schedule
                    val ref = firestore.document("subjects/$subjectKey/schedules/$scheduleKey")
                    val listener = EventListener<DocumentSnapshot> { snapshot, e ->
                        if (e != null) {
                            return@EventListener
                        }

                        val i = Unique.Utils.indexOf(aggregator.modelsAll, userKey)
                        if (i >= 0) {
                            val model = getModel(snapshot, aggregator.modelsAll[i])
                            aggregator.put(model ?: getModel(snapshot)) // update existing schedule
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