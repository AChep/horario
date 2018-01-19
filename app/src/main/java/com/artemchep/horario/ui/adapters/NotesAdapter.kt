package com.artemchep.horario.ui.adapters

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v4.view.ViewCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.models.INote
import com.artemchep.horario.extensions.limit
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.extensions.transitionNameCompat
import com.github.nitrico.fontbinder.FontBinder

/**
 * @author Artem Chepurnoy
 */
open class NotesAdapter<out T : INote>(
        private val context: Context,
        list: List<T>
) : AdapterBase<T, NotesAdapter.ViewHolder>(list) {

    val defaultTextSize: Int = 16

    /**
     * @author Artem Chepurnoy
     */
//    class ViewHolder(adapter: NotesAdapter<INote>, v: View) : AdapterBase.ViewHolder<*>(adapter, v) {
    class ViewHolder(adapter: NotesAdapter<INote>, v: View) : AdapterBase.ViewHolder<NotesAdapter<INote>>(adapter, v) {
        internal val textTextView: TextView = v.findViewById(R.id.text)
        internal val cardView: View = v.findViewById(R.id.card)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.___item_note, parent, false)
        return ViewHolder(this, v).apply {
            textTextView.typeface = FontBinder["RobotoSlab-Regular"]
        }
    }

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        val note = getItem(position)
        h.textTextView.setTextExclusive(note.text)

        note.text?.let {
            var r = 35f / it.length
            r = r.limit(1f, 2.1f)
            h.textTextView.textSize = r * defaultTextSize
        }
    }

}