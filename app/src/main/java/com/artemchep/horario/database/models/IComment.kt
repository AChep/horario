package com.artemchep.horario.database.models

import com.artemchep.horario.extensions.getSafely
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

/**
 * @author Artem Chepurnoy
 */
interface IComment : IModel {
    var text: String?
    var author: String?
    var parent: String?
    var timestamp: Long

    fun inflateComment(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            text = it.getString("text")
            author = it.getString("author")
            parent = it.getString("parent")

            val date: Date? = it.getSafely("timestamp", Date::class.java)
            timestamp = date?.time ?: 0L
        }
    }

    fun deflateComment(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("text", text),
                Pair("author", author),
                Pair("parent", parent)
        )
    }
}