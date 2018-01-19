package com.artemchep.horario.database.models

import com.artemchep.horario.database.data.Post
import com.artemchep.horario.extensions.getInt
import com.artemchep.horario.extensions.getSafely
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

/**
 * @author Artem Chepurnoy
 */
interface ITask : IModel {
    var author: String?
    var title: String?
    var text: String?
    var type: Int
    var timestamp: Long
    var timestampEdit: Long
    var commentsCounter: Int

    fun inflateTask(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            title = it.getString("title")
            author = it.getString("author")
            text = it.getString("text")
            type = it.getInt("type", Post.TYPE_POST.toLong())

            val date: Date? = it.getSafely("timestamp", Date::class.java)
            timestamp = date?.time ?: 0L
        }
    }

    fun deflateTask(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("title", title),
                Pair("author", author),
                Pair("text", text),
                Pair("type", type)
        )
    }
}