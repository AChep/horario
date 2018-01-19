package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.SubjectsAdapter
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID

/**
 * @author Artem Chepurnoy
 */
abstract class FragmentStoreSubjects<T : ISubject> : FragmentStore<T>(), AdapterBase.OnItemClickListener {

    companion object {
        const val TAG = "FragmentStoreSubjects"
    }

    private lateinit var userId: String

    override val filter: (T) -> Boolean = { true }
    override val comparator: Comparator<T> = compareBy(
            { it.name },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<T>): AdapterBase<T, *> = SubjectsAdapter(list)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onItemClick(view: View, model: T) {
        super.onItemClick(view, model)
        val intent = Intent(context, SubjectActivity::class.java).apply {
            putExtra(EXTRA_USER_ID, userId)
            putExtra(EXTRA_SUBJECT, model)
            putExtra(EXTRA_SUBJECT_ID, model.id!!)
        }

        startActivity(intent)
    }

}