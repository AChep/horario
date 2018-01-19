package com.artemchep.horario.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.ui.drawables.CircleDrawable

/**
 * @author Artem Chepurnoy
 */
open class SubjectsAdapter<out T : ISubject>(list: List<T>) : AdapterBase<T, SubjectsAdapter.ViewHolder>(list) {

    /**
     * @author Artem Chepurnoy
     */
    open class ViewHolder(adapter: SubjectsAdapter<ISubject>, v: View) : AdapterBase.ViewHolder<SubjectsAdapter<ISubject>>(adapter, v) {
        internal val colorDrawable = CircleDrawable()
        internal val nameTextView: TextView = v.findViewById(R.id.name)
        internal val colorView: View = v.findViewById(R.id.color)

        init {
            colorView.background = colorDrawable
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.___item_subject, parent, false)
        return ViewHolder(this, v)
    }

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        val subject = getItem(position)
        h.nameTextView.text = subject.name
        h.colorDrawable.color = subject.color
    }

}