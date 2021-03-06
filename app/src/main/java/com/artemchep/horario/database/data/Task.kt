package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.ITask
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Task(
        @Exclude
        override var id: String? = null,
        override var author: String? = null,
        override var title: String? = null,
        override var text: String? = null,
        override var type: Int = 0,
        override var timestamp: Long = 0,
        override var timestampEdit: Long = 0,
        override var commentsCounter: Int = 0) : ITask {

    companion object {
        const val TYPE_ANNOUNCEMENT: Int = 1
        const val TYPE_QUESTION: Int = 2

        @JvmField
        val CREATOR = createParcel { Task(it) }

        fun clone(post: Task?) = post?.copy()

        fun from(snapshot: DocumentSnapshot): Task? {
            return if (snapshot.exists()) Task(id = snapshot.id).apply {
                title = snapshot.getString("title")
                author = snapshot.getString("author")
                text = snapshot.getString("text")
            } else null
        }

        fun from(snapshot: DataSnapshot): Task? {
            return Task(id = snapshot.key).apply {
                snapshot.child("info").takeIf { it.exists() }?.let {
                    title = it.child("name").value as String?
                    author = it.child("author").value as String?
                    text = it.child("text").value as String?
                }
            }
        }

    }

    constructor(id: String) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            author = src.readString(),
            title = src.readString(),
            text = src.readString(),
            type = src.readInt(),
            timestamp = src.readLong(),
            timestampEdit = src.readLong(),
            commentsCounter = src.readInt()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(author)
            writeString(title)
            writeString(text)
            writeInt(type)
            writeLong(timestamp)
            writeLong(timestampEdit)
            writeInt(commentsCounter)
        }
    }

}