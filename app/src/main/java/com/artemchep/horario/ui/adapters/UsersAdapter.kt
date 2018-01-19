package com.artemchep.horario.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.R
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.ui.widgets.UserView

/**
 * @author Artem Chepurnoy
 */
open class UsersAdapter<out T : IUser>(list: List<T>) : AdapterBase<T, UsersAdapter<T>.ViewHolder>(list) {

    var yourUserId: String? = null

    /**
     * @author Artem Chepurnoy
     */
    inner open class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        internal val userView: UserView = v.findViewById(R.id.user)

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val listener = onItemClickListener
            if (listener != null) {
                val pos = adapterPosition
                if (pos != -1) listener.onItemClick(view, pos)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.___item_user, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(h: ViewHolder, position: Int) {
        val user = getItem(position)
        h.userView.model = user
    }

}
