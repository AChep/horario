package com.artemchep.horario.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.RectF
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alamkanak.weekview.MonthLoader
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.artemchep.horario.Heart
import com.artemchep.horario.R
import com.artemchep.horario.database.DbHelper
import com.artemchep.horario.database.SqlEventSubject
import com.artemchep.horario.database.data.Event
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.*
import com.google.firebase.auth.FirebaseAuth
import org.joda.time.LocalDate
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.*

/**
 * Displays all events in a week-view table.
 *
 * @author Artem Chepurnoy
 */
class MainScheduleFragment : FragmentBase(),
        WeekView.EventClickListener,
        MonthLoader.MonthChangeListener {

    companion object {
        private const val TAG = "MainScheduleFragment"
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
        events.run {
            clear()
            addAll(it)
        }

        // Update displayed data
        weekView.notifyDatasetChanged()
    }

    private val events: MutableList<SqlEventSubject> = ArrayList()

    private lateinit var weekView: WeekView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_main_schedule, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weekView = view.findViewById<WeekView>(R.id.week).apply {
            setOnEventClickListener(this@MainScheduleFragment)
            monthChangeListener = this@MainScheduleFragment
        }
    }

    override fun onStart() {
        super.onStart()
        startLoader()

        // Listen for our sync service
        val intentFilter = IntentFilter(Heart.INTENT_DB_EVENTS_UPDATED)
        val manager = LocalBroadcastManager.getInstance(context!!)
        manager.registerReceiver(localBroadcastReceiver, intentFilter)
    }

    /** Starts update thread. */
    private fun startLoader() {
        Timber.tag(TAG).i("Execute update-thread.")

        loader?.cancel(false)
        loader = Loader(context!!, loaderCallback).apply {
            // Start loader in separate thread.
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        }
    }

    override fun onStop() {
        val manager = LocalBroadcastManager.getInstance(context!!)
        manager.unregisterReceiver(localBroadcastReceiver)

        loader?.cancel(false)
        super.onStop()
    }

    override fun onMonthChange(newYear: Int, newMonth: Int): MutableList<out WeekViewEvent> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = 0
            set(Calendar.YEAR, newYear)
            set(Calendar.MONTH, newMonth - 1)
        }

        val curStart = LocalDate(calendar).plusDays(-1)
        val curEnd = LocalDate(calendar.apply {
            add(Calendar.MONTH, 1)
        }.timeInMillis - 1)

        val list = ArrayList<WeekViewEvent>()
        events.forEach {
            var x = LocalDate(it.dateStart)
            var y = LocalDate(it.dateEnd)

            if (it.repeatType == Event.REPEAT_OFF) {
                if (curStart.isBefore(x) && curEnd.isAfter(x)
                        || curStart.isBefore(y) && curEnd.isAfter(y)) {
                    // Add event
                    val moment = Moment(it,
                            calendarStart = Calendar.getInstance().apply {
                                timeInMillis = x.toDate().time
                                set(Calendar.HOUR_OF_DAY, it.timeStart / 60)
                                set(Calendar.MINUTE, it.timeStart % 60)
                            },
                            calendarEnd = Calendar.getInstance().apply {
                                timeInMillis = x.toDate().time
                                set(Calendar.HOUR_OF_DAY, it.timeEnd / 60)
                                set(Calendar.MINUTE, it.timeEnd % 60)
                            }
                    )
                    list.add(moment)
                }
                return@forEach
            } else if (x.isAfter(curEnd)) {
                // Event starts in the future
                return@forEach
            }

            val modifier: (date: LocalDate) -> LocalDate = when (it.repeatType) {
                Event.REPEAT_MONTHLY -> { date -> date.plusMonths(it.repeatEvery) }
                Event.REPEAT_WEEKLY -> { date -> date.plusWeeks(it.repeatEvery) }
                Event.REPEAT_DAILY -> { date -> date.plusDays(it.repeatEvery) }
                else -> return@forEach
            }

            while (!x.isAfter(curEnd)) {
                if (curStart.isBefore(x) && curEnd.isAfter(x)
                        || curStart.isBefore(y) && curEnd.isAfter(y)) {
                    val moment = Moment(it,
                            calendarStart = Calendar.getInstance().apply {
                                timeInMillis = x.toDate().time
                                set(Calendar.HOUR_OF_DAY, it.timeStart / 60)
                                set(Calendar.MINUTE, it.timeStart % 60)
                            },
                            calendarEnd = Calendar.getInstance().apply {
                                timeInMillis = x.toDate().time
                                set(Calendar.HOUR_OF_DAY, it.timeEnd / 60)
                                set(Calendar.MINUTE, it.timeEnd % 60)
                            }
                    )
                    list.add(moment)
                }

                x = modifier(x)
                y = modifier(y)
            }
        }

        return list
    }

    override fun onEventClick(event: WeekViewEvent?, eventRect: RectF?) {
        val moment = event as? Moment ?: return
        val model = moment.event
        val args = Bundle().apply {
            putString(EXTRA_USER_ID, FirebaseAuth.getInstance().uid ?: "userId") // TODO
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

    /**
     * @author Artem Chepurnoy
     */
    private class Moment(
            val event: SqlEventSubject,
            calendarStart: Calendar,
            calendarEnd: Calendar
    ) : WeekViewEvent(
            0,
            // Name of the event is its title or
            // subject name.
            event.title?.takeUnless { it.isBlank() } ?: event.subjectName,
            calendarStart, calendarEnd) {
        override fun getColor(): Int = event.subjectColor
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Loader(
            context: Context,
            callback: (Collection<SqlEventSubject>) -> Unit
    ) :
            AsyncTask<Unit, Unit, Collection<SqlEventSubject>>() {

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

}