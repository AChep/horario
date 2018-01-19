package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.View
import com.artemchep.horario.R
import com.artemchep.horario.database.models.INote
import com.artemchep.horario.extensions.inverted
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.NotesAdapter
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentChange
import java.util.*

/**
 * @author Artem Chepurnoy
 */
abstract class FragmentStoreNotes<T : INote> : FragmentStore<T>(), AdapterBase.OnItemClickListener {

    companion object {
        const val TAG = "FragmentStoreNotes"
    }

    private lateinit var userId: String

    override val filter: (T) -> Boolean = { true }
    override val comparator: Comparator<T> = compareBy<T>(
            { it.timestamp },
            { it.id }
    ).inverted()

    private lateinit var recyclerView: RecyclerView

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<T>): AdapterBase<T, *> = NotesAdapter(context!!, list)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        }
    }

    abstract fun getReference(): CollectionReference

}