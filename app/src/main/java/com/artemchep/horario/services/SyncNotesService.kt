package com.artemchep.horario.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.artemchep.horario.database.DbHelper
import com.artemchep.horario.database.SqlAlarm
import com.artemchep.horario.database.data.Note
import com.artemchep.horario.database.models.IEvent
import com.artemchep.horario.database.models.INote
import com.artemchep.horario.database.sql.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SyncNotesService : Service() {

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
        firestore.collection("users/$userId/notes")
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

                    val notes = task.result.map {
                        Note.from(it)!!
                    }
                    Timber.tag(TAG).i("notes=" + notes.toString())
                    syncNotesWithLocalDb(notes)
                }
    }

    private fun syncNotesWithLocalDb(notes: List<INote>) {
        val helper = DbHelper.getInstance(applicationContext)

        /**
         * List of new notes to add into
         * a local database.
         */
        val listAdd = ArrayList<INote>()
        var listModify = ArrayList<Pair<INote, SqlAlarm>>()
        /** List of notes to remove from local database */
        val listRemove = helper.getAll(SqlAlarm.CATEGORY_NOTE).toMutableList()

        val isModified: (INote, SqlAlarm) -> Boolean = { n, s ->
            n.text != s.text
        }

        notes.forEach { n ->
            listRemove.listIterator().run {
                while (hasNext()) {
                    val s = next()

                    if (n.id == s.idFirestore) {
                        if (isModified(n, s)) {
                            listModify.add(Pair(n, s))
                        }
                        // Otherwise those two notes are equal and
                        // should not be updated.

                        remove()
                        return@forEach
                    }
                }
            }

            listAdd.add(n)
        }

       /* helper.insert(listAdd.map {
            SqlAlarm(0, it.id!!, SqlAlarm.CATEGORY_NOTE, it.title, it.text, 0)
        })*/
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

}