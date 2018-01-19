package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class User(
        @Exclude
        override var id: String? = null,
        override var name: String? = null,
        override var email: String? = null,
        override var biography: String? = null,
        override var avatarUrl: String? = null) : IUser {

    companion object {
        @JvmField
        val CREATOR = createParcel { User(it) }

        fun clone(user: User?) = user?.copy()

        fun from(snapshot: DocumentSnapshot): User? {
            return if (snapshot.exists()) User(id = snapshot.id).apply {
                name = snapshot.getString("name")
                email = snapshot.getString("email")
                biography = snapshot.getString("biography")
                avatarUrl = snapshot.getString("avatar_url")
            } else null
        }

        fun from(snapshot: DataSnapshot): User? {
            return User(id = snapshot.key).apply {
                name = snapshot.child("name").value as String?
                email = snapshot.child("email").value as String?
                biography = snapshot.child("biography").value as String?
                avatarUrl = snapshot.child("avatar_url").value as String?
            }
        }
    }

    constructor(id: String) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            name = src.readString(),
            email = src.readString(),
            biography = src.readString(),
            avatarUrl = src.readString()
    )

    override fun getKey(): String = id!!

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(name)
            writeString(email)
            writeString(biography)
            writeString(avatarUrl)
        }
    }

}
