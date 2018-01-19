package com.artemchep.horario.ui.adapters

import android.support.v7.widget.RecyclerView
import android.view.View

/**
 * @author Artem Chepurnoy
 */
abstract class AdapterBase<out T, H : RecyclerView.ViewHolder>(l: List<T>) : RecyclerView.Adapter<H>() {

    /**
     * @author Artem Chepurnoy
     */
    interface OnItemClickListener {
        fun onItemClick(view: View, pos: Int)
    }

    open class ViewHolder<out T : AdapterBase<Any, out RecyclerView.ViewHolder>>(val adapter: T, v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val listener = adapter.onItemClickListener
            if (listener != null) {
                val pos = adapterPosition
                if (pos != -1) listener.onItemClick(view, pos)
            }
        }
    }

    val list: List<T> = l
    var onItemClickListener: OnItemClickListener? = null

    override fun getItemCount(): Int = list.size

    /**
     * @return element of the list at position
     * @throws IndexOutOfBoundsException
     */
    open fun getItem(position: Int): T = list[position]

}
