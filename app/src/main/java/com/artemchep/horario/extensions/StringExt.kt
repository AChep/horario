package com.artemchep.horario.extensions

fun String.escape(char: Char = '\''): String {
    val a = char.toString()
    val b = "\\" + a
    return this.replace(a, b)
}
