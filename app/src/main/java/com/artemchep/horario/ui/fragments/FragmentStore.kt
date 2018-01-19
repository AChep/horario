package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import com.artemchep.horario.R
import com.artemchep.horario.aggregator.Aggregator
import com.artemchep.horario.interfaces.Unique
import com.artemchep.horario.ui.adapters.AdapterBase
import com.google.firebase.firestore.*
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
abstract class FragmentStore<T> : FragmentDocument(),
        EventListener<QuerySnapshot>,
        AdapterBase.OnItemClickListener
        where T : Unique, T : Parcelable {

    companion object {
        const val STATE_LIST = "base__state::list"
    }

    abstract val filter: (T) -> Boolean
    abstract val comparator: Comparator<T>
    lateinit var aggregator: Aggregator<T>

    private var emptyView: View? = null

    private lateinit var recyclerView: RecyclerView

    private val temp: MutableList<T> = ArrayList()
    private val removedSet: MutableList<String> = ArrayList()
    private val disabledSet: HashMap<String, DocumentChange.Type> = HashMap()

    private lateinit var firequery: FireCollection<T>

    protected lateinit var adapter: AdapterBase<T, *>

    private val listener = EventListener<QuerySnapshot> { snapshot, e ->
        this@FragmentStore.onEvent(snapshot, e) // redirect event

        if (e != null) {
            Timber.w("FragmentStore: " + e.message)
            return@EventListener
        }

        snapshot.documentChanges.forEach {
            val key = it.document.id
            when (it.type) {
                DocumentChange.Type.REMOVED -> performRemoveModel(key)

                DocumentChange.Type.ADDED -> {
                    val index = Unique.Utils.indexOf(temp, key)
                    if (index >= 0) {
                        temp.removeAt(index)
                    }

                    val model = getModel(it.document)
                    performPutModel(model)
                }

                DocumentChange.Type.MODIFIED -> {
                    val model = getModel(it.document)
                    performPutModel(model)
                }
            }
        }

        temp.apply {
            forEach { performRemoveModel(it.getKey()) }
            clear()
        }
    }

    private val handler: Handler = Handler()

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        aggregator = Aggregator({
            !removedSet.contains(it.getKey()) && filter(it)
        }, comparator)
        // Attach a listener that updates the adapter of
        // recycler view.
        aggregator.registerListener(object : Aggregator.Observer<T> {
            override fun add(model: T, i: Int) = adapter.notifyItemInserted(i)
            override fun set(model: T, i: Int) = adapter.notifyItemChanged(i)
            override fun remove(model: T, i: Int) = adapter.notifyItemRemoved(i)

            override fun move(model: T, from: Int, to: Int) {
                adapter.notifyItemRemoved(from)
                adapter.notifyItemInserted(to)
            }

            override fun avalanche() = adapter.notifyDataSetChanged()
        })
        // Show/hide empty view
        aggregator.registerListener(object : Aggregator.Observer<T> {
            override fun add(model: T, i: Int) = updateEmptyView()
            override fun set(model: T, i: Int) = updateEmptyView()
            override fun remove(model: T, i: Int) = updateEmptyView()

            override fun move(model: T, from: Int, to: Int) {
            }

            override fun avalanche() = updateEmptyView()

            fun updateEmptyView() {
                emptyView?.visibility = if (aggregator.models.isEmpty()) {
                    View.VISIBLE
                } else View.GONE
            }
        })
    }

    override fun onLinkDatabase(manager: FireManager) {
        firequery = object : FireCollection<T>(getQuery(), "__models", { snapshot ->
            getModel(snapshot)
        }) {
            override fun onEvent(snapshot: QuerySnapshot?, error: FirebaseFirestoreException?) {
                super.onEvent(snapshot, error)
                this@FragmentStore.listener.onEvent(snapshot, error)
            }
        }.also {
            manager.link(it)
        }
    }

    abstract fun onCreateAdapter(list: MutableList<T>): AdapterBase<T, *>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = onCreateAdapter(aggregator.models).apply {
            onItemClickListener = this@FragmentStore
        }

        recyclerView = view.findViewById(R.id.recycler)
        recyclerView.adapter = adapter

        emptyView = view.findViewById(R.id.empty)
        emptyView?.visibility = if (aggregator.models.isEmpty()) {
            View.VISIBLE
        } else View.GONE
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.run {
            // Replace all models with a restored ones; we will need to filter-out
            // old ones after first fetch.
            getParcelableArrayList<T>(STATE_LIST).let {
                aggregator.replaceAll(it)
            }
        }
    }

    override fun onStart() {
        temp.apply {
            clear()
            addAll(aggregator.modelsAll)
        }

        super.onStart()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putParcelableArrayList(STATE_LIST, aggregator.modelsAll)
        }
    }

    override fun onItemClick(view: View, pos: Int) = onItemClick(view, adapter.getItem(pos))

    /**
     * Called on click on the item and contains clicked view and
     * its data.
     */
    open fun onItemClick(view: View, model: T) {}

    protected open fun performPutModel(model: T): T? {
        return aggregator.put(model)
    }

    protected open fun performRemoveModel(key: String) {
        aggregator.remove(key)
    }

    abstract fun getQuery(): Query

    abstract fun getModel(snapshot: DocumentSnapshot): T

    override fun onEvent(snapshot: QuerySnapshot?, error: FirebaseFirestoreException?) {
    }

    fun removeModels(keys: Set<String>, modifier: (Set<String>) -> Unit) {
        Snackbar.make(view!!, "Action applied", Snackbar.LENGTH_SHORT)
                .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        if (event != BaseTransientBottomBar.BaseCallback.DISMISS_EVENT_ACTION) {
                            removedSet.removeAll(keys)
                            modifier.invoke(keys)
                        }
                    }

                    override fun onShown(transientBottomBar: Snackbar?) {
                        removedSet.addAll(keys)
                        aggregator.refilter()
                    }
                })
                .setAction(R.string.snackbar_undo) {
                    removedSet.removeAll(keys)
                    aggregator.refilter()
                }
                .show()
    }

}
