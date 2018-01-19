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
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Schedule
import com.artemchep.horario.database.models.ISchedule
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.SchedulesAdapter
import com.artemchep.horario.ui.utils.*
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class SubjectSchedulesFragment : FragmentStore<ISchedule>(), SubjectActivity.Page {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String
    private var color: Int = 0

    /** Query a list of the schedules of this subject */
    private lateinit var schedulesQuery: Query

    override val filter: (ISchedule) -> Boolean = { true }
    override val comparator: Comparator<ISchedule> = compareBy(
            { it.timestamp },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        firestore = FirebaseFirestore.getInstance()
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            color = getInt(EXTRA_COLOR, Palette.UNKNOWN)
        }

        schedulesQuery = firestore.collection("subjects/$subjectId/schedules")
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<ISchedule>): SchedulesAdapter<ISchedule> {
        return SchedulesAdapter(list)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_schedules, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).run {
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onItemClick(view: View, model: ISchedule) {
        super.onItemClick(view, model)
        val args = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putString(EXTRA_SCHEDULE_ID, model.id!!)
            putParcelable(EXTRA_SCHEDULE, model)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectScheduleFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    /**
     * Starts [SubjectScheduleEditFragment] with params
     * to create new schedule
     */
    override fun onFabClick(view: View) {
        val args = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectScheduleEditFragment::class.java, args, 0)
    }

    override fun hasFab(): Int = R.drawable.ic_plus_white_24dp

    override fun getQuery(): Query = schedulesQuery

    override fun getModel(snapshot: DocumentSnapshot): ISchedule = Schedule.from(snapshot)!!

}
