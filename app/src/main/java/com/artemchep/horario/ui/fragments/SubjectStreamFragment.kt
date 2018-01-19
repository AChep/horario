package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Post
import com.artemchep.horario.database.models.IPost
import com.artemchep.horario.extensions.inverted
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.ui.widgets.PrettyTimeView
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.sufficientlysecure.htmltextview.HtmlTextView
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class SubjectStreamFragment : FragmentStore<IPost>(), AdapterBase.OnItemClickListener, SubjectActivity.Page {

    private var color: Int = 0
    private lateinit var userId: String
    private lateinit var subjectId: String

    /** Reference to a list of the posts of this subject */
    private lateinit var postsQuery: Query

    override val filter: (IPost) -> Boolean = { true }
    override val comparator: Comparator<IPost> = compareBy<IPost>(
            { it.timestamp },
            { it.id }
    ).inverted()

    override fun onAttach(context: Context?) {
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            color = getInt(EXTRA_COLOR, Palette.UNKNOWN)
        }

        val firestore = FirebaseFirestore.getInstance()
        postsQuery = firestore.collection("subjects/$subjectId/posts")
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<IPost>): Adapter = Adapter(context!!, list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_stream, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).run {
            // Show two columns of posts in
            // `large` interfaces
            layoutManager = if (isLarge) {
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else LinearLayoutManager(activity)
        }
    }

    override fun onItemClick(view: View, model: IPost) {
        super.onItemClick(view, model)

        val args = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putString(EXTRA_POST_ID, model.getKey())
            putParcelable(EXTRA_POST, model)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectPostFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun onFabClick(view: View) {
        val args = Bundle().apply {
            putInt(EXTRA_COLOR, color)
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
        }

        val host = activity as FragmentHost
        host.fragmentShow(SubjectPostEditFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun hasFab(): Int = R.drawable.ic_plus_white_24dp

    override fun getQuery(): Query = postsQuery

    override fun getModel(snapshot: DocumentSnapshot): IPost = Post.from(snapshot)!!

    /**
     * @author Artem Chepurnoy
     */
    class Adapter(private val context: Context, list: List<IPost>) : AdapterBase<IPost, Adapter.ViewHolder>(list) {

        /**
         * @author Artem Chepurnoy
         */
        class ViewHolder(adapter: Adapter, v: View) : AdapterBase.ViewHolder<Adapter>(adapter, v) {
            internal val commentTextView: TextView = v.findViewById(R.id.info)
            internal val titleTextView: TextView = v.findViewById(R.id.title)
            internal val typeTextView: TextView = v.findViewById(R.id.type)
            internal val textTextView: HtmlTextView = v.findViewById(R.id.text)
            internal val timeView: PrettyTimeView = v.findViewById(R.id.time)
            internal val userView: UserView = v.findViewById(R.id.user_view)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_post, parent, false)
            return ViewHolder(this, v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = getItem(position)
            val res = context.resources

            if (post.text != null) {
                holder.textTextView.setHtml(post.text!!)
                holder.textTextView.movementMethod = null
            } else holder.textTextView.text = null

            holder.timeView.setTime(post.timestamp)
            holder.userView.modelId = post.author

            holder.typeTextView.setTextExclusive(when (post.type) {
                Post.TYPE_ANNOUNCEMENT -> res.getString(R.string.announcement)
                Post.TYPE_QUESTION -> res.getString(R.string.question)
                else -> null
            })

            holder.titleTextView.setTextExclusive(post.title)
            holder.commentTextView.setTextExclusive(post.commentsCounter
                    .takeIf { post.commentsCounter > 0 }
                    ?.let { res.getQuantityString(R.plurals.comments_counter, it, it) }
            )
        }

    }

}
