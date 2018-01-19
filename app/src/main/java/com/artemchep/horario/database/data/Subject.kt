package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.Palette
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.toBoolean
import com.artemchep.horario.extensions.toInt
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Subject(
        @Exclude
        override var id: String? = null,
        override var name: String? = null,
        override var author: String? = null,
        override var description: String? = null,
        override var archived: Boolean = false,
        override var color: Int = 0) : ISubject {

    companion object {

        @JvmField
        val CREATOR = createParcel { Subject(it) }

        fun clone(subject: Subject?) = subject?.copy()

        fun from(snapshot: DocumentSnapshot): Subject? {
            return if (snapshot.exists()) {
                Subject(id = snapshot.id).apply {
                    name = snapshot.getString("name")
                    author = snapshot.getString("author")
                    description = snapshot.getString("description")
                    color = snapshot.getLong("color")?.toInt() ?: Palette.GREY
                }
            } else null
        }

        fun from(snapshot: DataSnapshot): Subject? {
            return Subject(id = snapshot.key).apply {
                name = snapshot.child("name").value as String?
                author = snapshot.child("author").value as String?
                description = snapshot.child("description").value as String?
                archived = snapshot.child("archived").value.let { it as? Boolean } == true
                color = snapshot.child("color").value.let { it as? Long }?.toInt() ?: 0
            }
        }

        fun delta(a: Subject, b: Subject): HashMap<String, Any?> {
            return HashMap<String, Any?>().apply {
                if (a.name != b.name) put("name", a.name)
                if (a.author != b.author) put("author", a.author)
                if (a.description != b.description) put("description", a.description)
                if (a.archived != b.archived) put("archived", a.archived)
                if (a.color != b.color) put("color", a.color)
            }
        }

    }

    constructor(id: String?) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            name = src.readString(),
            author = src.readString(),
            description = src.readString(),
            archived = src.readInt().toBoolean(),
            color = src.readInt()
    )

    override fun getKey(): String = id!!

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(name)
            writeString(author)
            writeString(description)
            writeInt(archived.toInt())
            writeInt(color)
        }
    }

}