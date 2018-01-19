package com.artemchep.horario.database.models

import com.artemchep.horario.extensions.getInt
import com.artemchep.horario.extensions.getLong
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
interface IEvent : IModel {

    companion object {
        const val REPEAT_OFF = 0
        const val REPEAT_WEEKLY = 1
        const val REPEAT_DAILY = 3
    }

    var title: String?
    var author: String?
    var subject: String?
    var teacher: String?
    var place: String?
    var info: String?

    /**
     * {@code 0} means no type set, {@code 1} -> lecture,
     * {@code 2} -> practice, {@code 3} -> seminar, {@code 4} -> lab.
     */
    var type: Int

    var repeatType: Int
    var repeatEvery: Int

    var dateStart: Long
    var timeStart: Int
    var dateEnd: Long
    var timeEnd: Int

    fun inflateEvent(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            title = it.getString("title")
            author = it.getString("author")
            teacher = it.getString("teacher")
            place = it.getString("place")
            info = it.getString("info")

            type = it.getInt("type", 0)
            repeatType = it.getInt("repeatType", 0)
            repeatEvery = it.getInt("repeatEvery", 0)

            dateStart = it.getLong("dateStart", 0)
            timeStart = it.getInt("timeStart", 0)
            dateEnd = it.getLong("dateEnd", 0)
            timeEnd = it.getInt("timeEnd", 0)
        }
    }

    fun deflateEvent(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("title", title),
                Pair("author", author),
                Pair("teacher", teacher),
                Pair("place", place),
                Pair("info", info),

                Pair("type", type),
                Pair("repeatType", repeatType),
                Pair("repeatEvery", repeatEvery),

                Pair("dateStart", dateStart),
                Pair("timeStart", timeStart),
                Pair("dateEnd", dateEnd),
                Pair("timeEnd", timeEnd)
        )
    }
}