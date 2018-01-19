package com.artemchep.horario.extensions

/**
 * @return `1` if Boolean is `true`,
 * `0` otherwise
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * @return `false` if Int is `0`,
 * `true` otherwise
 */
fun Int.toBoolean(): Boolean = this != 0

fun Float.limit(from: Float, to: Float): Float = when {
    this > to -> to
    this < from -> from
    else -> this
}