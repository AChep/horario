package com.artemchep.horario.extensions

import com.afollestad.materialdialogs.MaterialDialog

fun MaterialDialog.Builder.withItems(l: MutableList<Pair<String, () -> Any?>>): MaterialDialog.Builder {
    return this
            .items(l.map { it.first })
            .itemsCallback { _, _, position, _ ->
                l[position].second()
            }
}