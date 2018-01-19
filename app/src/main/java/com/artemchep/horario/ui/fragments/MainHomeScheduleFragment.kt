package com.artemchep.horario.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.Heart
import com.artemchep.horario.R
import com.artemchep.horario.database.DbHelper
import com.artemchep.horario.database.SqlEventSubject
import com.artemchep.horario.database.data.Event
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.drawables.CircleDrawable
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.utils.DateUtilz
import com.google.firebase.auth.FirebaseAuth
import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder
import timber.log.Timber
import java.lang.ref.WeakReference

/**
 * @author Artem Chepurnoy
 */
class MainHomeScheduleFragment : FragmentMain(),
        View.OnClickListener,
        AdapterBase.OnItemClickListener {

    companion object {
        private const val TAG = "MainHomeScheduleFragment"

        private const val STATE_OFFSET = "state::offset"
        private const val STATE_DATETIME = "state::datetime"

        private val DATE_FORMATTER = DateTimeFormatterBuilder()
                .appendDayOfWeekText()
                .appendLiteral(", ")
                .appendMonthOfYearShortText()
                .appendLiteral(" ")
                .appendDayOfMonth(1)
                .toFormatter()
    }

    private lateinit var userId: String

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_DATE_CHANGED -> {
                    if (offset == 0) {
                        // Date changed but we should keep displaying
                        updateRecyclerViewData()
                    } else {
                        // Keep displaying current day
                        val now = System.currentTimeMillis()
                        offset -= Period(offsetTime, now).days
                        offsetTime = now
                        updateHeaderView()
                    }
                }
            }
        }
    }

    private val localBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Heart.INTENT_DB_EVENTS_UPDATED -> startLoader() // refresh data on local database update
            }
        }
    }

    private var loader: Loader? = null
    private val loaderCallback: (Collection<SqlEventSubject>) -> Unit = {
        // callback
        progressView.visibility = View.GONE
        events.run {
            clear()
            addAll(it)
        }

        // Update displayed data
        updateRecyclerViewData()
    }

    private var offsetTime: Long = System.currentTimeMillis()
    private var offset: Int = 0

    private val events: MutableList<SqlEventSubject> = ArrayList()
    private val moments: MutableList<SqlEventSubject> = ArrayList()

    private lateinit var recyclerView: RecyclerView
    private lateinit var todayTextView: TextView
    private lateinit var todayBtn: View
    private lateinit var progressView: View
    private lateinit var emptyView: View
    private lateinit var decorView: View

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        userId = FirebaseAuth.getInstance().uid ?: "userId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore instance state
        savedInstanceState?.run {
            offsetTime = System.currentTimeMillis()
            offset = getInt(STATE_OFFSET).let {
                if (it != 0) {
                    // Check if the day has changed in period of
                    // saving the fragment state and restoring it.
                    val then = getLong(STATE_DATETIME)
                    val now = System.currentTimeMillis()
                    it - Period(then, now).days
                } else it
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_main_home_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler_lessons).apply {
            setHasFixedSize(false)
            adapter = Adapter(moments).apply {
                onItemClickListener = this@MainHomeScheduleFragment
            }
            layoutManager = LinearLayoutManager(context)
        }

        progressView = view.findViewById(R.id.progress)
        emptyView = view.findViewById(R.id.empty_schedule)
        decorView = view.findViewById(R.id.recycler_lessons_decor)
        todayTextView = view.findViewById(R.id.card_schedule_category)
        todayBtn = view.findViewById<TextView>(R.id.day_today).withOnClick(this)
        view.findViewById<View>(R.id.day_previous).withOnClick(this)
        view.findViewById<View>(R.id.day_forward).withOnClick(this)

        updateHeaderView()
    }

    override fun onStart() {
        super.onStart()
        startLoader()

        registerReceiver(true)
        registerReceiverLocal(true)
    }

    private fun registerReceiver(register: Boolean) {
        with(context!!) {
            if (register) {
                val filter = IntentFilter(Intent.ACTION_DATE_CHANGED)
                registerReceiver(broadcastReceiver, filter)
            } else unregisterReceiver(broadcastReceiver)
        }
    }

    private fun registerReceiverLocal(register: Boolean) {
        with(LocalBroadcastManager.getInstance(context!!)) {
            if (register) {
                val filter = IntentFilter(Heart.INTENT_DB_EVENTS_UPDATED)
                registerReceiver(broadcastReceiver, filter)
            } else unregisterReceiver(broadcastReceiver)
        }
    }

    /**
     * Shows [loader view][progressView] and starts update thread.
     */
    private fun startLoader() {
        Timber.tag(TAG).i("Execute update-thread.")

        progressView.visibility = View.VISIBLE
        loader?.cancel(false)
        loader = Loader(context!!, loaderCallback).apply {
            // Start loader in separate thread.
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    override fun onStop() {
        registerReceiver(false)
        registerReceiverLocal(false)

        loader?.cancel(false)
        super.onStop()
    }

    override fun onItemClick(view: View, pos: Int) {
        val adapter = recyclerView.adapter as Adapter
        val model = adapter.getItem(pos)
        val args = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, model.subjectId)
            putString(EXTRA_SCHEDULE_ID, model.scheduleId)
            putString(EXTRA_EVENT_ID, model.id)

            // Pass the color of the subject if it
            // exists.
            model.subjectColor.takeIf { it != 0 }?.let { putInt(EXTRA_COLOR, it) }
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectEventFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.day_today -> offset = 0
            R.id.day_previous -> offset--
            R.id.day_forward -> offset++
        }

        when (view.id) {
            R.id.day_today, R.id.day_previous, R.id.day_forward -> {
                offsetTime = System.currentTimeMillis()
                updateRecyclerViewData()
                updateHeaderView()
            }
        }
    }

    private fun updateRecyclerViewData() {
        moments.run {
            val new = occurrences(events)
            clear()
            addAll(new)
        }
        (recyclerView.adapter as Adapter).run {
            refreshCache()
            notifyDataSetChanged()
        }

        decorView.visibility = if (moments.isEmpty()) View.GONE else View.VISIBLE
        emptyView.visibility = if (moments.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateHeaderView() {
        if (offset == 0) {
            todayTextView.text = getString(R.string.today)
            todayBtn.visibility = View.INVISIBLE
        } else {
            todayBtn.visibility = View.VISIBLE
            todayTextView.text = when (offset) {
                1 -> getString(R.string.tomorrow)
                -1 -> getString(R.string.yesterday)
                else -> {
                    val date = DateTime.now().plusDays(offset)
                    date.toString(DATE_FORMATTER)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.run {
            putInt(STATE_OFFSET, offset)
            putLong(STATE_DATETIME, offsetTime)
        }
    }

    private fun occurrences(events: Collection<SqlEventSubject>): List<SqlEventSubject> {
        val now = LocalDate.now().plusDays(offset)
        val list = ArrayList<SqlEventSubject>()

        events.forEach {
            var x = LocalDate(it.dateStart)
            var y = LocalDate(it.dateEnd)

            if (it.repeatType == Event.REPEAT_OFF) {
                if (!now.isAfter(y) && !now.isBefore(x)) {
                    list.add(it.copy())
                }
                return@forEach
            } else if (x.isAfter(now)) {
                // Event starts in the future
                return@forEach
            }

            val modifier: (date: LocalDate) -> LocalDate = when (it.repeatType) {
                Event.REPEAT_MONTHLY -> { date -> date.plusMonths(it.repeatEvery) }
                Event.REPEAT_WEEKLY -> { date -> date.plusWeeks(it.repeatEvery) }
                Event.REPEAT_DAILY -> { date -> date.plusDays(it.repeatEvery) }
                else -> return@forEach
            }

            while (!x.isAfter(now)) {
                if (!now.isAfter(y) && !now.isBefore(x)) {
                    list.add(it.copy())
                }

                x = modifier(x)
                y = modifier(y)
            }
        }
        list.sortWith(compareBy(
                { it.timeStart },
                { it.timeEnd }
        ))

        return list
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Loader(
            context: Context,
            callback: (Collection<SqlEventSubject>) -> Unit
    ) : AsyncTask<Unit, Unit, Collection<SqlEventSubject>>() {

        val contextRef = WeakReference(context)
        val callbackRef = WeakReference(callback)

        override fun doInBackground(vararg args: Unit?): List<SqlEventSubject> {
            val list = ArrayList<SqlEventSubject>()

            contextRef.get()?.run {
                val helper = DbHelper.getInstance(applicationContext)
                list.addAll(helper.eventSubject.getAll())
            }

            return list
        }

        override fun onPostExecute(result: Collection<SqlEventSubject>?) {
            super.onPostExecute(result)
            callbackRef.get()?.run { invoke(result!!) }
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    private class Adapter(l: List<SqlEventSubject>) : AdapterBase<SqlEventSubject, Adapter.ViewHolder>(l) {

        companion object {
            private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatterBuilder()
                    .appendDayOfWeekShortText()
                    .appendLiteral(", ")
                    .toFormatter()
        }

        /** [DateTime] that represents current <b>day</b> */
        private var now = DateTime.now()

        /** Cached map of [texts][Entry] ready to be bound to views */
        private val cacheMap: MutableMap<String, Entry> = HashMap()

        private data class Entry(val title: String, val summary: String)

        /**
         * @author Artem Chepurnoy
         */
        private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            internal var circleDrawable: CircleDrawable = CircleDrawable()
            internal var titleTextView: TextView = itemView.findViewById(R.id.name)
            internal var summaryTextView: TextView = itemView.findViewById(R.id.time)

            init {
                itemView.setOnClickListener(this)
                itemView.findViewById<View>(R.id.color).apply {
                    background = circleDrawable
                }
            }

            override fun onClick(v: View) {
                adapterPosition.takeIf {
                    it != RecyclerView.NO_POSITION
                }?.also {
                    onItemClickListener?.onItemClick(v, it)
                }
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_class_lesson, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val model = getItem(position)

            var entry = cacheMap[model.id]
            if (entry == null) {
                // Form title of the event:
                // title > subject name > `placeholder`
                val title = model.title?.takeUnless {
                    it.isBlank()
                } ?:
                        // Take subject name as title if exists
                        model.subjectName?.takeUnless {
                            it.isBlank()
                        } ?: "Event"

                // Formats date time in readable format
                val formatDateTime: (Long, Int) -> String = { date, time ->
                    val d = DateTime(date)
                            .takeIf { it.dayOfWeek() != now.dayOfWeek() }
                            ?.toString(DATE_FORMAT) ?: ""
                    val t = DateUtilz.formatx(time)
                    d + t
                }

                val start = formatDateTime(model.dateStart, model.timeStart)
                val end = formatDateTime(model.dateEnd, model.timeEnd)
                val summary = "$start - $end  ${model.place ?: ""}"

                entry = Entry(title = title, summary = summary)
            }

            // Bind to views
            holder.circleDrawable.color = model.subjectColor
            holder.titleTextView.text = entry.title
            holder.summaryTextView.text = entry.summary
        }

        fun refreshCache() {
            cacheMap.clear()
            now = DateTime.now()
        }

    }

}