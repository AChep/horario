package com.artemchep.horario.database.models

import com.artemchep.horario.extensions.getInt
import com.artemchep.horario.extensions.getSafely
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
interface INote : IModel {
    var text: String?
    var timestamp: Long
    var date: Int
    var archived: Boolean

    fun inflateNote(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            text = it.getString("text")
            date = it.getInt("date", 0L)

            val then: Long? = it.getSafely("timestamp", Long::class.java)
            timestamp = then ?: 0L
        }
    }

    fun deflateNote(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("text", text),
                Pair("timestamp", timestamp),
                Pair("date", date),
                Pair("archived", archived)
        )
    }

}