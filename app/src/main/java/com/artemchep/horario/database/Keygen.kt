package com.artemchep.horario.database

import android.os.Build

private const val RADIX = 26

private fun generateSalt(): String = (Math.random() * 800).toInt().toString(RADIX)

fun generateKey(): String {
    val time = System.currentTimeMillis().toString(RADIX)
    val device = (generateSalt() + Build.FINGERPRINT).hashCode().toString(RADIX)
    val random = generateSalt()
    return "$time-$device-$random"
}