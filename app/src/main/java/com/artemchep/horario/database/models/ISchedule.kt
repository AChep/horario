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

    /**
     * Key of the subject this schedule belongs to.
     * {@code null} if not available.
     */
    var subject: String?

    fun inflateSchedule(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            name = it.getString("name")
            author = it.getString("author")
            description = it.getString("description")
            subject = it.getString("subject")
        }
    }

    fun deflateSchedule(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("name", name),
                Pair("author", author),
                Pair("description", description),
                Pair("subject", subject)
        )
    }
}
