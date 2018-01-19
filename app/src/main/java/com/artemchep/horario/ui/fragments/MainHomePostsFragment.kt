package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.support.design.widget.BaseTransientBottomBar
import android.support.design.widget.Snackbar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.IModel
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.ui.widgets.PostView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

/**
 * @author Artem Chepurnoy
 */
class MainHomePostsFragment : FragmentStore<UserPost>() {

    companion object {
        private const val TAG = "MainHomePostsFragment"
    }

    private lateinit var userId: String
    private lateinit var query: Query
    private lateinit var ref: CollectionReference

    override val filter: (UserPost) -> Boolean = { true }
    override val comparator: Comparator<UserPost> = compareBy(
            { it.time },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        val args = arguments
        userId = args?.getString(EXTRA_USER_ID) ?: FirebaseAuth.getInstance().currentUser?.uid ?: "come on"

        val firestore = FirebaseFirestore.getInstance()
        ref = firestore.collection("users/$userId/posts")
        query = ref.orderBy("time").limit(20)

        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<UserPost>): Adapter = Adapter(context!!, list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_main_home_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<RecyclerView>(R.id.recycler).run {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun onItemClick(view: View, model: UserPost) {
        super.onItemClick(view, model)

        if (view.id == R.id.close) {
            val key = model.id!!
            removeModels(setOf(key), {
                FirebaseFirestore.getInstance().batch().apply {
                    it.forEach { delete(ref.document(it)) }
                }.commit()
            })
            return
        }

        val parts = model.id!!.split(',')
        if (parts.isEmpty()) {
        } else {
            when (parts[0]) {
                "subject" -> {
                    if (parts.size == 3) {
                        val subjectId = parts[1]
                        val postId = parts[2]

                        val args = Bundle().apply {
                            putInt(EXTRA_COLOR, Palette.UNKNOWN)
                            putString(EXTRA_USER_ID, userId)
                            putString(EXTRA_SUBJECT_ID, subjectId)
                            putString(EXTRA_POST_ID, postId)

                            val postView = view.findViewById<PostView>(R.id.post)
                            if (postView != null) {
                                putParcelable(EXTRA_POST, postView.model)
                            }
                        }

                        val host = activity as FragmentHost
                        host.fragmentShow(SubjectPostFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
                    }
                }
                else -> {
                }
            }
        }
    }

    override fun getQuery(): Query = query

    override fun getModel(snapshot: DocumentSnapshot): UserPost = UserPost.from(snapshot)

    /**
     * @author Artem Chepurnoy
     */
    class Adapter(private val context: Context, list: List<UserPost>) : AdapterBase<UserPost, Adapter.ViewHolder>(list) {

        /**
         * @author Artem Chepurnoy
         */
        class ViewHolder(adapter: Adapter, v: View) : AdapterBase.ViewHolder<Adapter>(adapter, v) {
            internal val postView: PostView = v.findViewById(R.id.post)

            init {
                v.findViewById<View>(R.id.close).withOnClick(this)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_class_post, parent, false)
            return ViewHolder(this, v)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val post = getItem(position)
            holder.postView.modelId = post.id
        }

    }

}

/**
 * @author Artem Chepurnoy
 */
data class UserPost(
        @Exclude
        override var id: String? = null,
        var time: Long = 0) : IModel {

    companion object {
        @JvmField
        val CREATOR = createParcel { UserPost(it) }

        fun from(snapshot: DocumentSnapshot): UserPost {
            return UserPost(id = snapshot.id).apply {
            }
        }
    }

    constructor(src: Parcel) : this(
            id = src.readString(),
            time = src.readLong()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeLong(time)
        }
    }

}