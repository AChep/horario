package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IRole
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
data class Role(override var role: Int = Role.NONE) : IRole {

    companion object {
        const val NONE = 0
        const val REQUEST = 1

        const val MEMBER = 2
        const val ADMIN = 3
        const val OWNER = 4

        @JvmField
        val CREATOR = createParcel { Role(it) }

        fun from(snapshot: DocumentSnapshot): Role? {
            return if (snapshot.exists()) {
                Role().apply { inflateRole(snapshot) }
            } else null
        }
    }

    constructor(src: Parcel) : this(
            role = src.readInt()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeInt(role)
        }
    }

}