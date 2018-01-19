package com.artemchep.horario.database.data

import android.os.Parcel
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IModel
import com.google.firebase.database.Exclude

/**
 * @author Artem Chepurnoy
 */
data class Occurrence(
        @Exclude
        override var id: String? = null,
        var timeStart: Long = 0,
        var timeEnd: Long = 0) : IModel {

    companion object {
        @JvmField
        val CREATOR = createParcel { Occurrence(it) }
    }

    constructor(src: Parcel) : this(
            id = src.readString(),
            timeStart = src.readLong(),
            timeEnd = src.readLong()
    )

    override fun getKey(): String = id!!

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeLong(timeStart)
            writeLong(timeEnd)
        }
    }

}