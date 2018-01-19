package com.artemchep.horario.database.models

import android.os.Parcelable
import com.artemchep.horario.interfaces.Unique

/**
 * @author Artem Chepurnoy
 */
interface IModel : Unique, Parcelable {
    var id: String?

    override fun getKey(): String = id!!
}