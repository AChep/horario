package com.artemchep.horario.database.models

import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
interface ISchedule : IModel {
    var name: String?
    var author: String?
    var description: String?
    var timestamp: Long

    fun inflateSchedule(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            name = it.getString("name")
            author = it.getString("author")
            description = it.getString("description")
        }
    }

    fun deflateSchedule(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("name", name),
                Pair("author", author),
                Pair("description", description)
        )
    }
}
