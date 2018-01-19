package com.artemchep.horario.ui.fragments

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Event
import com.artemchep.horario.database.data.Role
import com.artemchep.horario.database.data.Schedule
import com.artemchep.horario.database.models.IEvent
import com.artemchep.horario.database.models.IRole
import com.artemchep.horario.database.models.ISchedule
import com.artemchep.horario.extensions.setHtmlExclusive
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.fragments.SubjectEventFragment.Companion.DATE_FORMAT
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.ui.widgets.UserView
import com.artemchep.horario.utils.DateUtilz
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.thebluealliance.spectrum.internal.ColorUtil
import es.dmoral.toasty.Toasty
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout
import org.joda.time.DateTime
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.util.*
import kotlin.collections.HashMap

typealias EditFragment = SubjectScheduleEditFragment

/**
 * @author Artem Chepurnoy
 */
class SubjectScheduleFragment : FragmentStore<IEvent>(),
        Toolbar.OnMenuItemClickListener,
        View.OnClickListener {

    companion object {
        const val STATE_SCHEDULE = "state::schedule"
        const val STATE_SUBSCRIBED = "state::subscribed"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String
    private lateinit var scheduleId: String
    private var subjectName: String? = null
    private var color: Int = 0

    private lateinit var schedule: FireDocument<ISchedule>
    private lateinit var subscriber: FireDocument<IRole>
    private lateinit var eventsQuery: Query

    private lateinit var toolbar: Toolbar
    private lateinit var toolbarCollapsing: CollapsingToolbarLayout
    private lateinit var appBar: AppBarLayout
    private lateinit var fab: FloatingActionButton
    private lateinit var userView: UserView
    private lateinit var subscribeButton: Button
    private lateinit var descriptionHtmlView: HtmlTextView

    private lateinit var editMenuItem: MenuItem
    private lateinit var removeMenuItem: MenuItem

    override val filter: (IEvent) -> Boolean = { true }
    override val comparator: Comparator<IEvent> = compareBy(
            { it.dateStart }, { it.timeStart },
            { it.dateEnd }, { it.timeEnd },
            { it.repeatType }, { it.repeatEvery },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        firestore = FirebaseFirestore.getInstance()
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            scheduleId = getString(EXTRA_SCHEDULE_ID)

            subjectName = getString(EXTRA_SUBJECT_NAME)
            color = retrieveColor()
        }

        val doc = firestore.document("subjects/$subjectId/schedules/$scheduleId")
        eventsQuery = doc.collection("events")

        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        super.onLinkDatabase(manager)
        val scheduleRef = firestore.document("subjects/$subjectId/schedules/$scheduleId")

        // Listen to the schedule
        schedule = FireDocument<ISchedule>(
                scheduleRef, "schedule",
                { snapshot -> Schedule(id = snapshot.id).apply { inflateSchedule(snapshot) } }
        ).also {
            it.value = arguments!!.getParcelable(EXTRA_SCHEDULE)
            manager.link(it)
        }

        // Listen to the membership
        subscriber = FireDocument<IRole>(
                scheduleRef.collection("subscribers").document(userId), "schedule",
                { Role() }
        ).also {
            manager.link(it)
        }
    }

    override fun onCreateAdapter(list: MutableList<IEvent>): Adapter = Adapter(context!!, list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_schedule2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }

        fab = view.findViewById<FloatingActionButton>(R.id.fab).withOnClick(this)
        appBar = view.findViewById(R.id.appbar)
        toolbarCollapsing = view.findViewById(R.id.toolbar_collapsing)
        toolbar = toolbarCollapsing.findViewById<Toolbar>(R.id.toolbar).apply {
            inflateMenu(R.menu.___subject_schedule)
            setOnMenuItemClickListener(this@SubjectScheduleFragment)
            editMenuItem = menu.findItem(R.id.action_edit)!!
            removeMenuItem = menu.findItem(R.id.action_remove)!!
        }
        userView = view.findViewById<UserView>(R.id.user).apply {
            nameView?.setOnClickListener(this@SubjectScheduleFragment)
        }
        subscribeButton = view.findViewById<Button>(R.id.subscribe).withOnClick(this)
        descriptionHtmlView = view.findViewById(R.id.text)

        // Set color
        appBar.setBackgroundColor(color)
        UiHelper.updateToolbarTitle(toolbar, color)
        UiHelper.updateToolbarBackIcon(toolbar, color)
        UiHelper.updateCollapsingToolbarTitle(toolbarCollapsing, color)
        UiHelper.updateToolbarMenuIcons(toolbar, color, R.id.action_edit)

        if (ColorUtil.isColorDark(color)) {
            subscribeButton.setTextColor(Color.WHITE)
            subscribeButton.setBackgroundResource(R.drawable.bg_btn_outline_white)
        } else {
            subscribeButton.setTextColor(Color.BLACK)
            subscribeButton.setBackgroundResource(R.drawable.bg_btn_outline_black)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        schedule.observer.follow { schedule ->
            val isMine = schedule?.author == userId
            removeMenuItem.isVisible = isMine
            if (isMine) {
                fab.show()
            } else fab.hide()

            descriptionHtmlView.setHtmlExclusive(schedule?.description)
            toolbarCollapsing.title = schedule?.name
            userView.modelId = schedule?.author
        }

        subscriber.observer.follow { role ->
            subscribeButton.text = if (role != null) {
                "Subscribed"
            } else "Subscribe"
        }

        manager.state.follow { state ->
            editMenuItem.isVisible = state == FireManager.SUCCESS && schedule.value?.author == userId
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> schedule.value?.let {
                val host = activity as FragmentHost
                val args = EditFragment.args(userId, subjectId, it, color)
                host.fragmentShow(EditFragment::class.java, args, 0)
            }
            R.id.action_remove -> {
                val md = MaterialDialog.Builder(context!!)
                        .content(R.string.schedule_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            val firestore = FirebaseFirestore.getInstance()
                            firestore.document("subjects/$subjectId/schedules/$scheduleId").delete()

                            // Finish the fragment
                            val host = activity as FragmentHost
                            host.fragmentFinish(this)
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
        }
        return false
    }

    override fun onClick(v: View) {
        when (v.id) {
        // Opens user's profile
            R.id.user_name -> userView.modelId?.let {
                val args = UserFragment.args(
                        userId = userId,
                        personId = it,
                        person = userView.model
                )

                // Show user fragment
                val host = activity as FragmentHost
                host.fragmentShow(UserFragment::class.java, args, 0)
            }
        // Subscribes/un-subscribes user from this
        // schedule
            R.id.subscribe -> {
                if (subscriber.observer.value != null) {
                    val content = schedule.observer.value?.name?.let {
                        getString(R.string.__subject_schedule_unsubscribe_from, it)
                    } ?: getString(R.string.__subject_schedule_unsubscribe)

                    // Show confirm dialog
                    MaterialDialog.Builder(context!!)
                            .content(content)
                            .positiveText(android.R.string.ok)
                            .negativeText(android.R.string.cancel)
                            .onPositive { _, _ ->
                                if (subscriber.observer.value != null) {
                                    val batch = firestore.batch()
                                    batch.delete(subscriber.ref)

                                    // Do the job of our trigger functions: remove this schedule from
                                    // user directly.
                                    run {
                                        val key = "$subjectId,$scheduleId"
                                        batch.delete(firestore.document("users/$userId/schedules/$key"))
                                    }

                                    batch.commit().addOnSuccessListener(activity!!, {
                                        Toasty.normal(context!!, "Unsubscribed").show()
                                    })
                                }
                            }
                            .build()
                            .show()
                } else {
                    val map: MutableMap<String, Any?> = hashMapOf(Pair("tmp", true))
                    val batch = firestore.batch()
                    batch.set(subscriber.ref, map)

                    // Do the job of our trigger functions: add this schedule to
                    // user directly.
                    run {
                        val key = "$subjectId,$scheduleId"
                        batch.set(firestore.document("users/$userId/schedules/$key"),
                                HashMap<String, Any?>().apply {
                                    Pair("subject", subjectId)
                                    Pair("schedule", scheduleId)
                                })

                        // TODO: Notify the user
                    }

                    batch.commit().addOnSuccessListener(activity!!, {
                        Toasty.normal(context!!, "Subscribed").show()
                    })
                }
            }
            R.id.fab -> {
                val args = SubjectEventEditFragment.args(
                        userId = userId,
                        subjectId = subjectId,
                        scheduleId = scheduleId,
                        color = color)

                // Show new event fragment
                val host = activity as FragmentHost
                host.fragmentShow(SubjectEventEditFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
        }
    }

    override fun getQuery(): Query = eventsQuery

    override fun getModel(snapshot: DocumentSnapshot): IEvent = Event.from(snapshot)!!

    override fun onItemClick(view: View, model: IEvent) {
        super.onItemClick(view, model)

        val args = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putString(EXTRA_SCHEDULE_ID, scheduleId)
            putString(EXTRA_EVENT_ID, model.id!!)
            putParcelable(EXTRA_EVENT, model)
            putInt(EXTRA_COLOR, color)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectEventFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
    }

    /**
     * @author Artem Chepurnoy
     */
    class Adapter(val context: Context, list: List<IEvent>) : AdapterBase<IEvent, Adapter.ViewHolder>(list) {

        /**
         * @author Artem Chepurnoy
         */
        class ViewHolder(adapter: Adapter, v: View) : AdapterBase.ViewHolder<Adapter>(adapter, v) {
            val titleTextView = v.findViewById<TextView>(R.id.title)
            val dateStartView = v.findViewById<View>(R.id.datetime_start)
            val dateStartDateView = dateStartView.findViewById<TextView>(R.id.datetime_start_date)
            val dateStartTimeView = dateStartView.findViewById<TextView>(R.id.datetime_start_time)
            val dateEndView = v.findViewById<View>(R.id.datetime_end)
            val dateEndDateView = dateEndView.findViewById<TextView>(R.id.datetime_end_date)
            val dateEndTimeView = dateEndView.findViewById<TextView>(R.id.datetime_end_time)
            val dateRepeatView = v.findViewById<TextView>(R.id.datetime_repeat)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_event, parent, false)
            return ViewHolder(this, v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val h = holder
            val res = context.resources
            val event = getItem(position)
            val title = event.title?.takeUnless { it.isBlank() } ?: when (event.type) {
                1 -> res.getString(R.string.lecture)
                2 -> res.getString(R.string.practice)
                3 -> res.getString(R.string.seminar)
                4 -> res.getString(R.string.lab)
                else -> "Event"
            }

            h.titleTextView.setTextExclusive(title)
            h.dateStartDateView.text = DateTime(event.dateStart).toString(DATE_FORMAT)

            val a = event.timeStart
            val b = event.timeEnd
            val timeStart = DateUtilz.format(a / 60, a % 60)
            val timeEnd = DateUtilz.format(b / 60, b % 60)

            if (event.dateStart == event.dateEnd) {
                h.dateStartTimeView.text = timeStart + " - " + timeEnd
                h.dateEndView.visibility = View.GONE
            } else {
                h.dateStartTimeView.text = timeStart
                h.dateEndTimeView.text = timeEnd
                h.dateEndDateView.text = DateTime(event.dateEnd).toString(DATE_FORMAT)
                h.dateEndView.visibility = View.VISIBLE
            }

            when (event.repeatType) {
                Event.REPEAT_OFF -> h.dateRepeatView.visibility = View.GONE
                else -> {
                    h.dateRepeatView.visibility = View.VISIBLE
                    val day = when (event.repeatType) {
                        Event.REPEAT_DAILY -> res.getQuantityString(R.plurals.days, event.repeatEvery)
                        Event.REPEAT_WEEKLY -> res.getQuantityString(R.plurals.weeks, event.repeatEvery)
                        else -> res.getQuantityString(R.plurals.months, event.repeatEvery)
                    }
                    h.dateRepeatView.text = res.getString(R.string.event_repeat_on, event.repeatEvery, day)
                }
            }
        }

    }

}
