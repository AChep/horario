package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IComment
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Comment(
        @Exclude
        override var id: String? = null,
        override var text: String? = null,
        override var author: String? = null,
        override var parent: String? = null,
        override var timestamp: Long = 0) : IComment {

    companion object {

        @JvmField
        val CREATOR = createParcel { Comment(it) }

        fun from(snapshot: DocumentSnapshot): Comment {
            return Comment(id = snapshot.id).apply {
                inflateComment(snapshot)
            }
        }

    }

    constructor(id: String?) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            text = src.readString(),
            author = src.readString(),
            parent = src.readString(),
            timestamp = src.readLong()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(text)
            writeString(author)
            writeString(parent)
            writeLong(timestamp)
        }
    }

}