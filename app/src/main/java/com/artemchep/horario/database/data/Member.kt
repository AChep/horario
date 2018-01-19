package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IMember
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude

/**
 * @author Artem Chepurnoy
 */
data class Member(
        @Exclude
        override var id: String? = null,
        override var name: String? = null,
        override var email: String? = null,
        override var biography: String? = null,
        override var avatarUrl: String? = null,
        override var role: Int = Role.REQUEST) : IMember {

    companion object {

        const val TYPE_MEMBER = 0
        const val TYPE_ADMIN = 1
        const val TYPE_OWNER = 2

        @JvmField
        val CREATOR = createParcel { Member(it) }

        fun from(snapshot: DataSnapshot): Member? {
            val user = Member(id = snapshot.key)
            user.name = snapshot.child("name").getValue(String::class.java)
            return user
        }

    }

    constructor(id: String) : this(id, null)

    constructor(src: Parcel) : this(
            src.readString(), // id
            src.readString(), // name
            src.readString(), // email
            src.readString(), // biography
            src.readString(), // avatar url
            src.readInt()     // role
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
            writeInt(role)
        }
    }

}
