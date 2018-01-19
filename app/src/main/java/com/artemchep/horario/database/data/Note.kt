package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.INote
import com.artemchep.horario.extensions.toBoolean
import com.artemchep.horario.extensions.toInt
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Note(
        @Exclude
        override var id: String? = null,
        override var text: String? = null,
        override var timestamp: Long = 0,
        override var date: Int = 0,
        override var archived: Boolean = false) : INote {

    companion object {
        @JvmField
        val CREATOR = createParcel { Note(it) }

        fun from(snapshot: DocumentSnapshot): Note? {
            return if (snapshot.exists()) Note(id = snapshot.id).apply { inflateNote(snapshot) } else null
        }
    }

    constructor(id: String) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            text = src.readString(),
            timestamp = src.readLong(),
            date = src.readInt(),
            archived = src.readInt().toBoolean()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(text)
            writeLong(timestamp)
            writeInt(date)
            writeInt(archived.toInt())
        }
    }

}