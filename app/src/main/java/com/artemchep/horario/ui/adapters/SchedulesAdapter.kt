package com.artemchep.horario.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.models.ISchedule
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.setHtmlExclusive
import com.artemchep.horario.ui.drawables.CircleDrawable
import com.artemchep.horario.ui.fragments.SubjectSchedulesFragment
import com.artemchep.horario.ui.widgets.UserView
import org.sufficientlysecure.htmltextview.HtmlTextView

/**
 * @author Artem Chepurnoy
 */
open class SchedulesAdapter<out T : ISchedule>(list: List<T>) : AdapterBase<T, SchedulesAdapter.ViewHolder>(list) {

    /**
     * @author Artem Chepurnoy
     */
    open class ViewHolder(adapter: SchedulesAdapter<ISchedule>, v: View) : AdapterBase.ViewHolder<SchedulesAdapter<ISchedule>>(adapter, v) {
        internal val nameTextView: TextView = v.findViewById(R.id.name)
        internal val textHtmlView: HtmlTextView = v.findViewById(R.id.text)
        internal val userView: UserView = v.findViewById(R.id.user_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.___item_schedule, parent, false)
        return ViewHolder(this, v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val schedule = getItem(position)
        holder.userView.modelId = schedule.author
        holder.nameTextView.text = schedule.name
        holder.textHtmlView.setHtmlExclusive(schedule.description)
    }

}