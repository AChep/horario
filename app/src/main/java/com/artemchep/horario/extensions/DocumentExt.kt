package com.artemchep.horario.extensions

import com.google.firebase.firestore.DocumentSnapshot

fun DocumentSnapshot.getInt(field: String, default: Long = 0) = getLong(field, default).toInt()

fun DocumentSnapshot.getLong(field: String, default: Long = 0) = get(field) as? Long ?: default

fun <T> DocumentSnapshot.getSafely(field: String, clazz: Class<T>): T? {
    return takeIf { contains(field) }
            ?.get(field)
            ?.takeIf { clazz.isAssignableFrom(it::class.java) }
            ?.let { it as? T }
}

fun <T> DocumentSnapshot.get(field: String, getter: DocumentSnapshot.(String) -> T): T? {
    return try {
        takeIf { contains(field) }?.let { getter.invoke(it, field) }
    } catch (e: TypeCastException) {
        null
    }
}
