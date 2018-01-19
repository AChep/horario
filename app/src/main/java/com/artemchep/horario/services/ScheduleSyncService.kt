package com.artemchep.horario.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.artemchep.horario.Heart
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.MainActivity
import com.artemchep.horario.database.models.IEvent
import com.artemchep.horario.database.sql.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import timber.log.Timber


/**
 * @author Artem Chepurnoy
 */
class ScheduleSyncService : Service() {

    companion object {
        private const val TAG = "ScheduleSyncService"
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
            userId = it
            fetchSchedules()
        } ?: run {
            Timber.tag(TAG).w("Finish service: user is not logged in!")
            // Finish service
            stopSelf()
        }
    }

    private fun fetchSchedules() {
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

                    task.result.forEach {

                    }
                }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = buildForegroundNotification()
        startForeground(NID, notification)

        return Service.START_STICKY
    }

    private fun buildForegroundNotification(): Notification {
        val pi = PendingIntent.getActivity(applicationContext, 0,
                Intent(this, MainActivity::class.java).setAction(Intent.ACTION_MAIN),
                PendingIntent.FLAG_UPDATE_CURRENT)
        return NotificationCompat.Builder(this, Heart.CHANNEL_SERVICE)
                .setContentIntent(pi)
                .setOngoing(true)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Syncing schedule...")
                .setSmallIcon(R.drawable.ic_account_white_24dp)
                .setWhen(System.currentTimeMillis())
                .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    /*
            Timber.tag(TAG).e("START: map=$map ")
            firestore.collection("users/$userId/schedules").get().addOnCompleteListener { task ->
                if (task.isSuccessful && !task.result.metadata.isFromCache) {
                    task.result.forEach { snapshot ->
                        val id = snapshot.id
                        val subject = snapshot.getString("subject")
                        val schedule = snapshot.getString("schedule")
                        Timber.tag(TAG).e("OHMY: id=$id map=$map ")

                        // Mark as required
                        map.put(id, null)

                        firestore.collection("subjects/$subject/schedules/$schedule/events").get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful && !task.result.metadata.isFromCache) {
                                        val list = ArrayList<IEvent>()
                                        task.result.forEach { snapshot ->
                                            assert(snapshot.exists()) // otherwise why would it be here?
                                            list.add(Event().apply { inflateEvent(snapshot) })
                                        }

                                        map.put(id, list)

                                        if (!map.values.contains(null)) {
                                            Timber.tag(TAG).e("COMPLETE: map=$map")
                                            stopForeground(true)
                                            stopSelf()
                                        }
                                    } else {
                                        Timber.tag(TAG).e("FAILED: map=$map ")
                                        stopForeground(true)
                                        stopSelf()
                                    }
                                }
                    }
                } else {
                    Timber.tag(TAG).e("FAILED2222: map=$map ")
                    stopForeground(true)
                    stopSelf()
                }
            }
     */

}