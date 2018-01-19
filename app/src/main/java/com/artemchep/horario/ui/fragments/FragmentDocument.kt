package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import com.artemchep.horario.R
import com.google.firebase.firestore.*
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
abstract class FragmentDocument : FragmentBase() {

    protected val manager = FireManager()

    private lateinit var errorView: View
    private lateinit var contentView: View
    private lateinit var progressView: View

    @LayoutRes
    protected var rootViewResource: Int = R.layout.___fragment_document

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        onLinkDatabase(manager)
    }

    abstract fun onLinkDatabase(manager: FireManager)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore the value of the
        // documents if present
        savedInstanceState?.apply {
            manager.list.forEach { (it as? FireDocument<*>)?.restoreInstanceState(this) }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(rootViewResource, container, false).apply {
            val vg = this as ViewGroup
            contentView = onCreateContentView(inflater, vg, savedInstanceState)
            vg.addView(contentView, 0)
        }
    }

    abstract fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressView = view.findViewById(R.id.progress) ?: view.findViewById<ViewStub>(R.id.progress_stub).inflate()
        errorView = view.findViewById<View>(R.id.error).apply {
            findViewById<View>(R.id.error_recover).setOnClickListener { manager.refresh() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        manager.state.follow { state ->
            errorView.visibility = if (state == FireManager.ERROR) View.VISIBLE else View.GONE
            contentView.visibility = if (state != FireManager.ERROR) View.VISIBLE else View.GONE
            progressView.visibility = if (state == FireManager.LOADING) View.VISIBLE else View.GONE
        }
    }

    override fun onStart() {
        super.onStart()
        manager.start()
    }

    override fun onStop() {
        manager.stop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save the state of documents to
        // restore later
        manager.list.forEach { (it as? FireDocument<*>)?.saveInstanceState(outState) }
    }

    /**
     * @author Artem Chepurnoy
     */
    class Observable<T>(v: T) {
        private val list: ArrayList<(T) -> Unit> = ArrayList()

        var value: T = v
            set(v) {
                if (field != v) {
                    field = v
                    list.forEach { it.invoke(v) }
                }
            }

        fun follow(sub: (T) -> Unit) {
            sub.invoke(value)
            list.add(sub)
        }

        fun clear() {
            list.clear()
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    open class FireObject<out T : Parcelable, H>(
            /**
             * String tag used to save/restore object to/from
             * saved instance state; to log events.
             */
            val tag: String,
            val inflater: (DocumentSnapshot) -> T?
    ) {
        var state: Observable<Int> = Observable(FireManager.LOADING)
            private set

        internal var listener: EventListener<H>? = null
        internal var registration: ListenerRegistration? = null
    }

    /**
     * @author Artem Chepurnoy
     */
    open class FireDocument<T : Parcelable>(
            val ref: DocumentReference,
            tag: String,
            inflater: (DocumentSnapshot) -> T?
    ) : FireObject<T, DocumentSnapshot>(tag, inflater), EventListener<DocumentSnapshot> {

        companion object {
            private const val TAG = "FireDocument"
        }

        val observer: Observable<T?> = Observable(null)
        var value: T?
            get() = observer.value
            set(value) {
                observer.value = value
            }

        constructor(
                path: String,
                tag: String,
                inflater: (DocumentSnapshot) -> T?
        ) : this(FirebaseFirestore.getInstance().document(path), tag, inflater)

        override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
            observer.value = if (e == null) {
                snapshot?.takeIf { it.exists() }?.let(inflater)?.also {
                    Timber.tag(TAG).d("Document updated: $tag=$it")
                }
            } else null
        }

        /**
         * Restores current [value] from bundle by
         * the [tag] name.
         */
        fun restoreInstanceState(state: Bundle?) {
            state?.run {
                value = getParcelable(tag)
            }
        }

        /**
         * Saves current [value] by [tag] name.
         */
        fun saveInstanceState(state: Bundle) {
            state.putParcelable(tag, value)
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    open class FireCollection<out T : Parcelable>(
            val ref: Query,
            tag: String,
            inflater: (DocumentSnapshot) -> T?
    ) : FireObject<T, QuerySnapshot>(tag, inflater), EventListener<QuerySnapshot> {
        override fun onEvent(snapshot: QuerySnapshot?, error: FirebaseFirestoreException?) {
        }
    }

    /**
     * @author Artem Chepurnoy
     */
    class FireManager {

        companion object {
            private const val TAG = "FireManager"

            const val LOADING = 0
            const val ERROR = 1
            const val SUCCESS = 3
        }

        var state: Observable<Int> = Observable(LOADING)
            private set

        private var started: Boolean = false
        internal val list: MutableList<FireObject<*, *>> = ArrayList()

        fun link(obj: FireObject<*, *>) {
            val stateListener: (FirebaseFirestoreException?) -> Unit = { e ->
                obj.state.value = if (e != null) {
                    obj.registration?.remove()
                    obj.registration = null

                    ERROR
                } else SUCCESS

                refreshState()
            }

            when (obj) {
                is FireCollection<*> -> obj.listener = EventListener { snapshot, e ->
                    val tag = obj.tag
                    Timber.tag(TAG).d("Event occurred [col]: $tag; error=$e")

                    obj.onEvent(snapshot, e)
                    stateListener(e)
                }
                is FireDocument<*> -> obj.listener = EventListener { snapshot, e ->
                    Timber.tag(TAG).d("Event occurred " +
                            "[doc]: ${obj.tag}; " +
                            "path=${obj.ref.path}; " +
                            "error=$e")

                    obj.onEvent(snapshot, e)
                    stateListener(e)
                }
                else -> throw IllegalArgumentException()
            }

            list.add(obj)
        }

        fun start() {
            started = true
            list.forEach {
                it.state.value = LOADING
                // Register listener
                it.registration = when (it) {
                    is FireDocument<*> -> it.ref.addSnapshotListener(it.listener!!)
                    is FireCollection<*> -> it.ref.addSnapshotListener(it.listener!!)
                    else -> throw IllegalStateException()
                }
            }

            state.value = LOADING
        }

        fun stop() {
            started = false
            // Remove all registrations
            list.forEach {
                it.registration?.remove()
                it.registration = null
            }
        }

        fun refresh() {
            if (started) {
                list.forEach {
                    it.registration ?: run {
                        it.state.value = LOADING
                        // Register listener
                        it.registration = when (it) {
                            is FireDocument<*> -> it.ref.addSnapshotListener(it.listener!!)
                            is FireCollection<*> -> it.ref.addSnapshotListener(it.listener!!)
                            else -> null
                        }
                    }
                }

                refreshState()
            }
        }

        private fun refreshState() {
            var x = SUCCESS
            list.forEach { x = x.and(it.state.value) }
            state.value = x
        }

    }

}