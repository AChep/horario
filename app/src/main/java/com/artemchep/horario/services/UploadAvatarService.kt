package com.artemchep.horario.services

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import es.dmoral.toasty.Toasty
import timber.log.Timber
import java.io.File

/**
 * @author Artem Chepurnoy
 */
class UploadAvatarService : Service() {

    companion object {
        const val TAG = "UploadService"
    }

    private val storage = FirebaseStorage.getInstance()
    private val binder = LocalBinder()

    private val map: MutableMap<String, Pair<UploadTask, OnCompleteListener<UploadTask.TaskSnapshot>>> = HashMap()

    /**
     * @author Artem Chepurnoy
     */
    inner class LocalBinder : Binder() {
        fun getService(): UploadAvatarService = this@UploadAvatarService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onDestroy() {
        Timber.tag(TAG).d("Service destroyed: count=" + map.size)

        map.forEach { it.value.first.removeOnCompleteListener(it.value.second) }
        map.clear()

        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder = binder

    fun upload(path: String, file: File) {
        val uri = Uri.fromFile(file)
        val ref = storage.getReference(path)

        val listener = OnCompleteListener<UploadTask.TaskSnapshot> { task ->
            if (task.isSuccessful) {
                Toasty.success(this@UploadAvatarService, "Success").show()
            } else Toasty.error(this@UploadAvatarService, "Failed").show()

            map.remove(path)
            if (map.isEmpty()) stopSelf()
        }
        val task = ref.putFile(uri).apply { addOnCompleteListener(listener) }
        map[path] = Pair(task, listener)
    }

}