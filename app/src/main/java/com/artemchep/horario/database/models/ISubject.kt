package com.artemchep.horario.database.models

import com.artemchep.horario.Palette
import com.google.firebase.firestore.DocumentSnapshot

/**
 * @author Artem Chepurnoy
 */
interface ISubject : IModel {
    var name: String?
    var author: String?
    var description: String?
    var archived: Boolean
    var color: Int

    fun inflateSubject(snapshot: DocumentSnapshot) {
        snapshot.takeIf { it.exists() }?.let {
            name = it.getString("name")
            author = it.getString("author")
            description = it.getString("description")
            color = snapshot.getLong("color")?.toInt() ?: Palette.GREY
        }
    }

    fun deflateSubject(): MutableMap<String, Any?> {
        return hashMapOf(
                Pair("name", name),
                Pair("author", author),
                Pair("description", description),
                Pair("color", color)
        )
    }
}