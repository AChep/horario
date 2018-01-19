package com.artemchep.horario.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import com.artemchep.horario.Heart
import com.artemchep.horario.database.DbHelper
import com.artemchep.horario.database.FireBatch
import com.artemchep.horario.database.SqlEvent
import com.artemchep.horario.database.SqlSubject
import com.artemchep.horario.database.data.Event
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.IEvent
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.database.sql.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SyncSubjectsService : Service() {

    companion object {
        private const val TAG = "SyncNotesService"
        private const val NID = 1235
    }

    private val fireauth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private lateinit var userId: String

    private lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()

        val map = HashMap<String, List<IEvent>?>()

        // Get current logged-in user, or stop
        // service if no one.
        fireauth.currentUser?.uid?.also {
            fetchNotes(it)
        } ?: run {
            Timber.tag(TAG).w("Finish service: user is not logged in!")
            // Finish service
            stopSelf()
        }
    }

    private fun fetchNotes(userId: String) {
        firestore.collection("users/$userId/schedules")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful.not()) {
                        Timber.tag(TAG).i("Failed to fetch schedules: uid=$userId")
                        Timber.tag(TAG).i("Stopping service...")
                        stopSelf()
                        return@addOnCompleteListener
                    }

                    Timber.tag(TAG).i("Gotcha!")
                    stopSelf()

                    Timber.d("FUCK TEST")
                    val events = ArrayList<IEvent>()
                    val subjects = ArrayList<ISubject>()

                    FireBatch().apply {
                        run {
                            val src = task.result.map {
                                val subjectId = it.getString("subject")
                                val scheduleId = it.getString("schedule")
                                "subjects/$subjectId/schedules/$scheduleId/events"
                            }

                            batch({ snapshot ->
                                Event(id = snapshot.id).apply {
                                    inflateEvent(snapshot)
                                    // Parse the path of the schedule to get
                                    // schedule id and subject id.
                                    var subjectId: String? = "subjects"
                                    var scheduleId: String? = "schedules"
                                    val path = snapshot.reference.path.split('/')
                                    path.forEach {
                                        when (it) {
                                            subjectId -> subjectId = null
                                            scheduleId -> scheduleId = null
                                            else -> {
                                                if (subjectId == null) {
                                                    subjectId = it
                                                } else if (scheduleId == null) {
                                                    scheduleId = it
                                                }
                                            }
                                        }
                                    }
                                    subject = subjectId
                                    info = scheduleId
                                }
                            }, src, events, "collection")
                        }

                        run {
                            val k = task.result.map { "users/$userId/subjects/" + it.getString("subject") }.toSet()
                            batch({ snapshot ->
                                Subject(id = snapshot.id).apply { inflateSubject(snapshot) }
                            }, k, subjects, "document")
                        }
                    }.go { success ->
                        syncSubjectsWithLocalDb(subjects)
                        syncEventsWithLocalDb(events)
                        if (success) {
                            val intent = Intent(Heart.INTENT_DB_EVENTS_UPDATED)
                            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

                            Timber.d("FUCK YEAH")
                        } else Timber.d("FUCK NO")
                    }

//
//
//                    Timber.tag(TAG).i("notes=" + notes.toString())
//                    syncEventsWithLocalDb(notes)
                }
    }

    private fun syncSubjectsWithLocalDb(subjects: List<ISubject>) {
        val helper = DbHelper.getInstance(applicationContext)
        val isModified: SqlSubject.(ISubject) -> Boolean = {
            name != it.name || color != it.color
        }

        /** List of new subjects to add into local database */
        val listAdd = ArrayList<ISubject>()
        /** List of subjects to update in local database */
        val listModify = ArrayList<ISubject>()
        /** List of subjects to remove from local database */
        val listRemove = helper.subject.getAll().toMutableList()

        subjects.forEach { fire ->
            listRemove.listIterator().run {
                while (hasNext()) {
                    val sql = next()

                    if (fire.id == sql.id) {
                        if (sql.isModified(fire)) {
                            listModify.add(fire)
                        }
                        // Otherwise those two notes are equal and
                        // should not be updated.

                        remove()
                        return@forEach
                    }
                }
            }

            listAdd.add(fire)
        }

        helper.subject.run {
            // Transforms normal subject to shortened
            // sql one
            val toSqlSubject: ISubject.() -> SqlSubject = { SqlSubject(id!!, name, color) }

            // Perform operations on local
            // database.
            insert(listAdd.map { it.toSqlSubject() })
            update(listModify.map { it.toSqlSubject() })
            delete(listRemove.map { it.id })
        }
    }

    private fun syncEventsWithLocalDb(subjects: List<IEvent>) {
        val helper = DbHelper.getInstance(applicationContext)

        /** List of new events to add into local database */
        val listAdd = ArrayList<IEvent>()
        /** List of events to update in local database */
        val listModify = ArrayList<IEvent>()
        /** List of events to remove from local database */
        val listRemove = helper.events.getAll().toMutableList()

        val isModified: SqlEvent.(IEvent) -> Boolean = {
            title != it.title
                    || repeatType != it.repeatType
                    || repeatEvery != it.repeatEvery
                    || dateStart != it.dateStart
                    || timeStart != it.timeStart
                    || dateEnd != it.dateEnd
                    || timeEnd != it.timeEnd
        }

        subjects.forEach { fire ->
            listRemove.listIterator().run {
                while (hasNext()) {
                    val sql = next()

                    if (fire.id == sql.id) {
                        if (sql.isModified(fire)) {
                            listModify.add(fire)
                        }
                        // Otherwise those two notes are equal and
                        // should not be updated.

                        remove()
                        return@forEach
                    }
                }
            }

            listAdd.add(fire)
        }

        helper.events.run {
            // Transforms normal event to shortened
            // sql one
            val toSqlEvent: IEvent.() -> SqlEvent = {
                SqlEvent(
                        id = id!!,
                        subjectId = subject + "",
                        scheduleId = info + "",
                        title = title,
                        place = place,
                        repeatType = repeatType,
                        repeatEvery = repeatEvery,
                        dateStart = dateStart,
                        timeStart = timeStart,
                        dateEnd = dateEnd,
                        timeEnd = timeEnd
                )
            }

            // Perform operations on local
            // database.
            insert(listAdd.map { it.toSqlEvent() })
            update(listModify.map { it.toSqlEvent() })
            delete(listRemove.map { it.id })
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

}