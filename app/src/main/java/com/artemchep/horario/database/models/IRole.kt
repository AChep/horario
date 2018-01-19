package com.artemchep.horario.database.models

import android.os.Parcelable
import com.artemchep.horario.database.data.Role
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
interface IRole : Parcelable {
    var role: Int

    fun inflateRole(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.getString("role")?.let {
            role = when (it) {
                "1" -> Role.MEMBER
                "10" -> Role.ADMIN
                "100" -> Role.OWNER
                else -> Role.NONE
            }
        }
    }
}