package com.artemchep.horario.ui.fragments

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.DbHelper
import com.artemchep.horario.database.SqlAlarm
import com.artemchep.horario.extensions.setTextExclusive

/**
 * @author Artem Chepurnoy
 */
class DatabaseFragment : FragmentBase() {

    private val list = ArrayList<SqlAlarm>()

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_database, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(context)
            adapter = Adapter(list)
            setHasFixedSize(true)
        }
    }

    override fun onStart() {
        super.onStart()

        val db = DbHelper.getInstance(context!!)
        val alarms = db.getAll()
        list.run {
            clear()
            addAll(alarms)
        }

        recyclerView.adapter.notifyDataSetChanged()
    }

    /**
     * @author Artem Chepurnoy
     */
    private class Adapter(
            private val list: MutableList<SqlAlarm>
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        /**
         * @author Artem Chepurnoy
         */
        private class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            internal val titleTextView: TextView = view.findViewById(R.id.title)
            internal val textTextView: TextView = view.findViewById(R.id.text)
        }

        override fun getItemCount(): Int = list.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_database, parent, false)
            return Adapter.ViewHolder(v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val model = list[position]
            holder.titleTextView.setTextExclusive(model.title)
            holder.textTextView.setTextExclusive(model.text)
        }
    }

}