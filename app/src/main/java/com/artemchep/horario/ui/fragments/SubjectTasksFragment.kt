package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Task
import com.artemchep.horario.database.models.ITask
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class SubjectTasksFragment : FragmentStore<ITask>(), AdapterBase.OnItemClickListener, SubjectActivity.Page {

    private var color: Int = 0
    private lateinit var userId: String
    private lateinit var subjectId: String

    /** Reference to a list of the tasks of this subject */
    private lateinit var tasksQuery: Query

    override val filter: (ITask) -> Boolean = { true }
    override val comparator: Comparator<ITask> = compareBy(
            { it.timestamp },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            color = getInt(EXTRA_COLOR, Palette.UNKNOWN)
        }

        val firestore = FirebaseFirestore.getInstance()
        tasksQuery = firestore.collection("subjects/$subjectId/tasks")
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<ITask>): Adapter = Adapter(context!!, list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).run {
            // Show two columns of posts in
            // `large` interfaces
            layoutManager = if (isLarge) {
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else LinearLayoutManager(activity)
        }
    }

    override fun onItemClick(view: View, pos: Int) {
        val data = adapter.getItem(pos)
        val args = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putString(EXTRA_TASK_ID, data.getKey())
            putParcelable(EXTRA_TASK, data)
        }

//        val host = activity as FragmentHost
//        host.fragmentShow(SubjectTaskFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun onFabClick(view: View) {
        val args = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectTaskEditFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun hasFab(): Int = R.drawable.ic_plus_white_24dp

    override fun getQuery(): Query = tasksQuery

    override fun getModel(snapshot: DocumentSnapshot): ITask = Task.from(snapshot)!!

    /**
     * @author Artem Chepurnoy
     */
    class Adapter(private val context: Context, list: List<ITask>) : AdapterBase<ITask, Adapter.ViewHolder>(list) {

        /**
         * @author Artem Chepurnoy
         */
        class ViewHolder(adapter: Adapter, v: View) : AdapterBase.ViewHolder<Adapter>(adapter, v) {
            internal val userView: UserView = itemView.findViewById(R.id.user_view)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_task, parent, false)
            return ViewHolder(this, v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = getItem(position)
            val res = context.resources

            holder.userView.modelId = post.author
        }

    }

}
