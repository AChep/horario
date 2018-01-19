package com.artemchep.horario.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Comment
import com.artemchep.horario.database.generateKey
import com.artemchep.horario.database.models.IComment
import com.artemchep.horario.extensions.withItems
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.artemchep.horario.ui.widgets.MessageBar
import com.artemchep.horario.ui.widgets.PrettyTimeView
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import es.dmoral.toasty.Toasty
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Artem Chepurnoy
 */
abstract class FragmentStoreComments : FragmentStore<IComment>(),
        MessageBar.Callback<IComment>,
        AdapterBase.OnItemClickListener {

    companion object {
        const val TAG = "FragmentStoreComments"
    }

    private lateinit var userId: String

    private var commentBarView: MessageBar<IComment>? = null

    override val filter: (IComment) -> Boolean = { true }
    override val comparator: Comparator<IComment> = compareBy(
            { it.timestamp },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<IComment>): AdapterBase<IComment, *> = Adapter(context!!, list)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        commentBarView = view.findViewById<MessageBar<IComment>>(R.id.chat)?.apply {
            callback = this@FragmentStoreComments
        }
        view.findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onItemClick(view: View, model: IComment) {
        super.onItemClick(view, model)
        when (view.id) {
            R.id.user_avatar -> model.author?.let {
                val args = UserFragment.args(
                        userId = userId,
                        personId = it
                )

                // Show user fragment
                val host = activity as FragmentHost
                host.fragmentShow(UserFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            else -> {
                val options: MutableList<Pair<String, () -> Any?>> = ArrayList()
                options.add(Pair("Copy text", {
                    // Copy text to clipboard
                    val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Comment text", model.text)
                    clipboard.primaryClip = clip

                    // Show toast message
                    val msg = getString(R.string.details_text_clipboard, model.text)
                    Toasty.normal(context!!, msg).show()
                }))

                if (model.author == userId) {
                    options.add(Pair("Edit", {
                        commentBarView!!.setMode(MessageBar.Mode.EDIT, model)
                    }))
                    options.add(Pair("Remove", {
                        getReference().document(model.id!!).delete()
                    }))
                }

                MaterialDialog.Builder(context!!)
                        .title("Comment options")
                        .withItems(options)
                        .show()
            }
        }
    }

    override fun onAction(view: MessageBar<IComment>, mode: MessageBar.Mode, obj: IComment?): Boolean {
        when (mode) {
            MessageBar.Mode.MESSAGE -> {
                val key = generateKey()
                val comment = Comment().apply {
                    text = view.message
                    author = userId
                }

                // Client-side data rules
                if (comment.text.isNullOrBlank()) {
                    return false
                }

                val map = comment.deflateComment().apply {
                    put("timestamp", FieldValue.serverTimestamp())
                }.filter { it.value != null }

                getReference().document(key).set(map)
            }
            MessageBar.Mode.EDIT -> obj?.let {
                val text = view.message
                if (text.isBlank()) {
                    return false
                }

                getReference().document(it.id!!).update("text", text)
            }
            else -> return false
        }
        return true
    }

    abstract fun getReference(): CollectionReference

    override fun getModel(snapshot: DocumentSnapshot): IComment = Comment.from(snapshot)

    /**
     * @author Artem Chepurnoy
     */
    class Adapter(private val context: Context, list: List<IComment>) : AdapterBase<IComment, Adapter.ViewHolder>(list) {

        /**
         * @author Artem Chepurnoy
         */
        class ViewHolder(adapter: Adapter, v: View) : AdapterBase.ViewHolder<Adapter>(adapter, v) {
            internal val userView: UserView = v.findViewById(R.id.user)
            internal val timeView: PrettyTimeView = v.findViewById(R.id.time)
            internal val textTextView: TextView = v.findViewById(R.id.text)

            init {
                userView.avatarView!!.withOnClick(this)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_comment, parent, false)
            return ViewHolder(this, v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val comment = getItem(position)
            holder.userView.modelId = comment.author
            holder.textTextView.text = comment.text

            if (comment.timestamp > 0) {
                holder.timeView.setTime(comment.timestamp)
            } else {
                // This can happen only if user has written
                // comment but server didn't approve it yet.
                holder.timeView.setTime(0)
                holder.timeView.text = UiHelper.TEXT_PLACEHOLDER
            }
        }

    }

}