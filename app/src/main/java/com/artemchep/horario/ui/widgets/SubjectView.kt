package com.artemchep.horario.ui.widgets

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.setTextExclusive
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.thebluealliance.spectrum.internal.ColorUtil

/**
 * @author Artem Chepurnoy
 */
class SubjectView : ModelView<ISubject> {

    var titleTextView: TextView? = null
        private set

    /**
     * Binds given user to this view
     */
    override fun bind(model: ISubject?) {
        if (model == null) {
            // Subject can be null if document of it is not
            // created yet, or not loaded yet: see STATE
            // variable

            titleTextView?.visibility = View.GONE
        } else {
            titleTextView?.run {
                setTextExclusive(model.name)
                setBackgroundColor(model.color)
                setTextColor(if (ColorUtil.isColorDark(model.color)) Color.WHITE else Color.BLACK)
            }
        }
    }

    override fun getModel(snapshot: DocumentSnapshot): ISubject {
        return Subject(id = snapshot.id).apply { inflateSubject(snapshot) }
    }

    override fun getDocument(firestore: FirebaseFirestore, id: String): DocumentReference {
        return firestore.document("subjects/$id")
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        titleTextView = findViewById(R.id.subject_title)
    }

}