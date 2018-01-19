package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.ISchedule
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Schedule(
        @Exclude
        override var id: String? = null,
        override var name: String? = null,
        override var author: String? = null,
        override var description: String? = null,
        override var timestamp: Long = 0) : ISchedule {

    companion object {
        @JvmField
        val CREATOR = createParcel { Schedule(it) }

        fun clone(schedule: Schedule?) = schedule?.copy()

        fun from(snapshot: DocumentSnapshot): Schedule? {
            return if (snapshot.exists()) {
                Schedule(id = snapshot.id).apply {
                    name = snapshot.getString("name")
                    author = snapshot.getString("author")
                    description = snapshot.getString("description")
                }
            } else null
        }

        fun from(snapshot: DataSnapshot): Schedule? {
            return Schedule(id = snapshot.key).apply {
                // Info
                snapshot.child("info").takeIf { it.exists() }?.let {
                    name = it.child("name").value as String?
                    author = it.child("author").value as String?
                    description = it.child("description").value as String?
                }
                // Generated
                snapshot.child("generated").takeIf { it.exists() }?.let {
                    timestamp = it.child("timestamp").value as? Long? ?: 0
                }
            }
        }

        fun delta(a: Schedule, b: Schedule): HashMap<String, Any?> {
            return HashMap<String, Any?>().apply {
                if (a.name != b.name) put("name", a.name)
                if (a.author != b.author) put("author", a.author)
                if (a.description != b.description) put("description", a.description)
                if (a.timestamp != b.timestamp) put("timestamp", a.timestamp)
            }
        }

    }

    constructor(id: String?) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            name = src.readString(),
            author = src.readString(),
            description = src.readString(),
            timestamp = src.readLong()
    )

    override fun getKey(): String = id!!

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(name)
            writeString(author)
            writeString(description)
            writeLong(timestamp)
        }
    }

}