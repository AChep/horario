package com.artemchep.horario.database.models

import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
interface IUser : IModel {
    var name: String?
    var email: String?
    var biography: String?
    var avatarUrl: String?

    fun inflateUser(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            name = it.getString("name")
            email = it.getString("email")
            biography = it.getString("biography")
            avatarUrl = it.getString("avatar_url")
        }
    }

    fun deflateUser(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("name", name),
                Pair("email", email),
                Pair("biography", biography),
                Pair("avatar_url", avatarUrl)
        )
    }
}
