package com.artemchep.horario.extensions

fun <T> withAll(block: T.() -> Unit, vararg receiver: T) {
    receiver.forEach { it.block() }
}

fun <T> Comparator<T>.inverted(): Comparator<T> = Comparator({ a, b -> -compare(a, b) })
