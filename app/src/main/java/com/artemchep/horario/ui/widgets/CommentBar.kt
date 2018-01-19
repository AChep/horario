package com.artemchep.horario.ui.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.models.IComment

/**
 * @author Artem Chepurnoy
 */
class CommentBar : MessageBar<IComment> {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onExcractMessage(obj: IComment?): String? = obj?.text

    override fun onCreateReplyView(vg: ViewGroup, obj: IComment): View = createCommentView(vg, obj)

    override fun onCreateEditView(vg: ViewGroup, obj: IComment): View = createCommentView(vg, obj)

    private fun createCommentView(vg: ViewGroup, model: IComment): View {
        return LayoutInflater.from(context).inflate(R.layout.___item_comment, vg, false).apply {
            findViewById<UserView>(R.id.user).modelId = model.author
            findViewById<TextView>(R.id.text).text = model.text
            findViewById<PrettyTimeView>(R.id.time).setTime(model.timestamp)
        }
    }

}