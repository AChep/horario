package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IEvent
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Event(
        @Exclude
        override var id: String? = null,

        override var author: String? = null,
        override var title: String? = null,
        override var subject: String? = null,
        override var teacher: String? = null,
        override var place: String? = null,
        override var info: String? = null,

        /**
         * {@code 0} means no type set, {@code 1} -> lecture,
         * {@code 2} -> practice, {@code 3} -> seminar, {@code 4} -> lab.
         */
        override var type: Int = 0,

        override var repeatType: Int = 0,
        override var repeatEvery: Int = 0,

        override var dateStart: Long = 0,
        override var timeStart: Int = 0,
        override var dateEnd: Long = 0,
        override var timeEnd: Int = 0) : IEvent {

    companion object {
        const val TYPE_LECTURE = 1
        const val TYPE_PRACTICE = 2
        const val TYPE_SEMINAR = 3
        const val TYPE_LAB = 4

        const val REPEAT_OFF = 0
        const val REPEAT_DAILY = 10
        const val REPEAT_WEEKLY = 20
        const val REPEAT_MONTHLY = 30

        @JvmField
        val CREATOR = createParcel { Event(it) }

        fun from(snapshot: DocumentSnapshot): Event? {
            return if (snapshot.exists()) {
                Event(id = snapshot.id).apply { inflateEvent(snapshot) }
            } else null
        }

        fun clone(event: Event?) = event?.copy()

        fun from(snapshot: DataSnapshot): Event? {
            val user = snapshot.getValue(Event::class.java)
            user?.id = snapshot.key
            return user
        }

    }

    constructor(id: String) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            title = src.readString(),
            author = src.readString(),
            subject = src.readString(),
            teacher = src.readString(),
            place = src.readString(),
            info = src.readString(),

            type = src.readInt(),

            repeatType = src.readInt(),
            repeatEvery = src.readInt(),

            dateStart = src.readLong(),
            timeStart = src.readInt(),
            dateEnd = src.readLong(),
            timeEnd = src.readInt()
    )

    override fun getKey(): String = id!!

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(title)
            writeString(author)
            writeString(subject)
            writeString(teacher)
            writeString(place)
            writeString(info)

            writeInt(type)

            writeInt(repeatType)
            writeInt(repeatEvery)

            writeLong(dateStart)
            writeInt(timeStart)
            writeLong(dateEnd)
            writeInt(timeEnd)
        }
    }

}