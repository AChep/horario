package com.artemchep.horario.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Post
import com.artemchep.horario.database.models.IPost
import com.artemchep.horario.extensions.setHtmlExclusive
import com.artemchep.horario.extensions.setTextExclusive
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import org.sufficientlysecure.htmltextview.HtmlTextView

/**
 * @author Artem Chepurnoy
 */
class PostView : ModelView<IPost> {

    var titleTextView: TextView? = null
        private set
    var textTextView: HtmlTextView? = null
        private set
    var timeView: PrettyTimeView? = null
        private set

    private lateinit var subjectView: SubjectView

    override var modelId: String? = null
        set(key) {
            if (field == key) {
                return
            }

            super.modelId = key

            if (key == null) {
                subjectView.modelId = null
            } else {
                val parts = key.split(',')
                if (parts.isEmpty()) {
                } else {
                    when (parts[0]) {
                        "subject" -> {
                            if (parts.size == 3) {
                                val subjectId = parts[1]
                                val postId = parts[2]
                                subjectView.modelId = subjectId
                            }
                        }
                        else -> {
                        }
                    }
                }
            }
        }

    /**
     * Binds given user to this view
     */
    override fun bind(model: IPost?) {
        if (model == null) {
            // Post can be null if document of it is not
            // created yet, or not loaded yet: see STATE
            // variable

            timeView?.setTime(0)
            textTextView?.text = null
            titleTextView?.setTextExclusive(when (state) {
                SUCCESS -> {
                    // Post document can be un-existent and
                    // in this cause we show `delete` label
                    modelId?.let { "Post removed" }
                }
                LOADING -> UiHelper.TEXT_PLACEHOLDER
                else -> null
            })
        } else {
            timeView?.setTime(model.timestamp)
            titleTextView?.setTextExclusive(model.title)
            textTextView?.run {
                setHtmlExclusive(model.text)
                movementMethod = null
            }
        }
    }

    override fun getModel(snapshot: DocumentSnapshot): IPost {
        return Post(id = snapshot.id).apply { inflatePost(snapshot) }
    }

    override fun getDocument(firestore: FirebaseFirestore, id: String): DocumentReference {
        val parts = id.split(',')
        if (parts.isEmpty()) {
        } else {
            when (parts[0]) {
                "subject" -> {
                    if (parts.size == 3) {
                        val subjectId = parts[1]
                        val postId = parts[2]
                        return firestore.document("subjects/$subjectId/posts/$postId")
                    }
                }
                else -> {
                }
            }
        }
        return firestore.document("errors/error")
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onFinishInflate() {
        super.onFinishInflate()
        titleTextView = findViewById(R.id.title)
        textTextView = findViewById(R.id.text)
        timeView = findViewById(R.id.time)
        subjectView = findViewById(R.id.subject)
    }

}