package com.artemchep.horario.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.artemchep.horario.database.models.IModel
import com.artemchep.horario.ui.fragments.FragmentDocument
import com.google.firebase.crash.FirebaseCrash
import com.google.firebase.firestore.*

/**
 * @author Artem Chepurnoy
 */
abstract class ModelView<T : IModel> : RelativeLayout {

    companion object {
        const val LOADING = FragmentDocument.FireManager.LOADING
        const val ERROR = FragmentDocument.FireManager.ERROR
        const val SUCCESS = FragmentDocument.FireManager.SUCCESS
    }

    private var isAttached: Boolean = false

    open var modelId: String? = null
        set(key) {
            if (field == key) {
                return
            }

            // Remove previous listener if exists
            modelListenerReg?.remove()
            modelListenerReg = null

            // Current user
            state = if (key != null) LOADING else SUCCESS
            model = null
            field = key

            if (key != null) {
                val firestore = FirebaseFirestore.getInstance()
                modelDoc = getDocument(firestore, key)

                // Register current listener
                if (isAttached) {
                    modelListenerReg = modelDoc!!.addSnapshotListener(modelListener)
                }
            } else modelDoc = null
        }

    var model: T? = null
        set(value) {
            field = value
            bind(value)
        }

    var state: Int = LOADING

    private var modelDoc: DocumentReference? = null
    private var modelListenerReg: ListenerRegistration? = null
    private val modelListener = EventListener<DocumentSnapshot> { snapshot, e ->
        if (e != null) {
            state = ERROR
            model = null

            FirebaseCrash.log("FragmentStore: " + e.message)
            return@EventListener
        }

        state = SUCCESS
        model = snapshot.takeIf { it.exists() }?.let { getModel(it) }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isAttached = true
        modelListenerReg = modelDoc?.addSnapshotListener(modelListener)
    }

    override fun onDetachedFromWindow() {
        isAttached = false
        modelListenerReg?.remove()
        super.onDetachedFromWindow()
    }

    abstract fun bind(model: T?)

    abstract fun getModel(snapshot: DocumentSnapshot): T

    abstract fun getDocument(firestore: FirebaseFirestore, id: String): DocumentReference

}