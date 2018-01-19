package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.annotation.ColorInt
import android.support.design.widget.AppBarLayout
import android.support.v4.app.NavUtils
import android.support.v4.app.TaskStackBuilder
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Post
import com.artemchep.horario.database.data.Role
import com.artemchep.horario.database.models.IPost
import com.artemchep.horario.database.models.IRole
import com.artemchep.horario.extensions.setHtmlExclusive
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.activities.ChildActivity
import com.artemchep.horario.ui.utils.*
import com.artemchep.horario.ui.widgets.PrettyTimeView
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.firestore.*
import org.sufficientlysecure.htmltextview.HtmlTextView

/**
 * @author Artem Chepurnoy
 */
class SubjectPostFragment : FragmentStoreComments(),
        Toolbar.OnMenuItemClickListener,
        View.OnClickListener {

    private var color: Int = 0
    private lateinit var userId: String
    private lateinit var subjectId: String
    private lateinit var postId: String

    private lateinit var commentsRef: CollectionReference
    private lateinit var post: FireDocument<IPost>
    private lateinit var member: FireDocument<IRole>

    private lateinit var appBar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var titleTextView: TextView
    private lateinit var textTextView: HtmlTextView
    private lateinit var statusTextView: PrettyTimeView
    private lateinit var timeView: PrettyTimeView
    private lateinit var userView: UserView

    private lateinit var editMenuItem: MenuItem
    private lateinit var removeMenuItem: MenuItem

    override fun onAttach(context: Context?) {
        arguments!!.run {
            color = retrieveColor()
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
            postId = getString(EXTRA_POST_ID)
        }

        val firestore = FirebaseFirestore.getInstance()
        commentsRef = firestore.collection("subjects/$subjectId/posts/$postId/comments")

        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        super.onLinkDatabase(manager)

        val firestore = FirebaseFirestore.getInstance()
        post = FireDocument<IPost>(
                firestore.document("subjects/$subjectId/posts/$postId"), "post",
                { snapshot -> Post(id = snapshot.id).apply { inflatePost(snapshot) } }
        ).also {
            it.value = arguments!!.getParcelable(EXTRA_POST)
            manager.link(it)
        }

        member = FireDocument<IRole>(
                firestore.document("subjects/$subjectId/members/$userId"), "member",
                { snapshot -> Role().apply { inflateRole(snapshot) } }
        ).also {
            manager.link(it)
        }
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBar = view.findViewById(R.id.appbar)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putString(EXTRA_SUBJECT_ID, subjectId)
                }

                val intent = Intent(activity, SubjectActivity::class.java)
                intent.putExtras(args)

                // Navigate back to the subject activity
                if (NavUtils.shouldUpRecreateTask(activity!!, intent) || activity!!.isTaskRoot) {
                    TaskStackBuilder.create(activity!!)
                            .addNextIntentWithParentStack(intent)
                            .startActivities()
                } else {
                    NavUtils.navigateUpTo(activity!!, intent)
                }
            }

            inflateMenu(R.menu.___subject_schedule)
            setOnMenuItemClickListener(this@SubjectPostFragment)
            editMenuItem = menu.findItem(R.id.action_edit)!!
            removeMenuItem = menu.findItem(R.id.action_remove)!!
        }

        userView = view.findViewById<UserView>(R.id.user).apply {
            nameView?.setOnClickListener(this@SubjectPostFragment)
        }

        titleTextView = view.findViewById(R.id.title)
        textTextView = view.findViewById(R.id.text)
        statusTextView = view.findViewById<PrettyTimeView>(R.id.status).apply {
            setPrefix("Last edited ")
        }
        timeView = view.findViewById(R.id.time)

        setupColorAccent(color)
    }

    private fun setupColorAccent(@ColorInt color: Int) {
        appBar.setBackgroundColor(color)
        UiHelper.updateToolbarTitle(toolbar, color)
        UiHelper.updateToolbarBackIcon(toolbar, color)
        UiHelper.updateToolbarMenuIcons(toolbar, color, R.id.action_edit)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        post.observer.follow { post ->
            removeMenuItem.isVisible = post?.author == userId

            if (post != null) {
                timeView.setTime(post.timestamp)
                toolbar.title = when (post.type) {
                    Post.TYPE_POST -> getString(R.string.post)
                    Post.TYPE_ANNOUNCEMENT -> getString(R.string.announcement)
                    Post.TYPE_QUESTION -> getString(R.string.question)
                    else -> "Unknown"
                }

                if (post.timestampEdit <= 0) {
                    statusTextView.visibility = View.GONE
                    statusTextView.setTime(0)
                } else {
                    statusTextView.visibility = View.VISIBLE
                    statusTextView.setTime(post.timestampEdit)
                }
            } else {
                timeView.setTime(0)
                statusTextView.setTime(0)
                toolbar.title = null
            }

            titleTextView.setTextExclusive(post?.title)
            textTextView.setHtmlExclusive(post?.text)
            userView.modelId = post?.author
        }

        manager.state.follow { state ->
            editMenuItem.isVisible = state == FireManager.SUCCESS && post.value?.author == userId
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> post.value?.let {
                val args = Bundle().apply {
                    putInt(EXTRA_COLOR, color)
                    putString(EXTRA_USER_ID, userId)
                    putString(EXTRA_SUBJECT_ID, subjectId)
                    putString(EXTRA_POST_ID, postId)
                    putParcelable(EXTRA_POST, it)
                }

                // Start edit activity

                val intent = ChildActivity.makeFor(context!!, SubjectPostEditFragment::class.java, args)
                startActivity(intent)
            }
            R.id.action_remove -> {
                val md = MaterialDialog.Builder(context!!)
                        .content(R.string.post_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            post.ref.delete()

                            // Finish the fragment
                            val host = activity as FragmentHost
                            host.fragmentFinish(this)
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
            else -> return false
        }
        return true
    }

    override fun onClick(v: View) {
        when (v.id) {
        // Opens user's profile
            R.id.user_name -> userView.modelId?.let {
                val args = UserFragment.args(
                        userId = userId,
                        personId = it,
                        person = userView.model
                )

                // Show user fragment
                val host = activity as FragmentHost
                host.fragmentShow(UserFragment::class.java, args, 0)
            }
        }
    }

    override fun getReference(): CollectionReference = commentsRef

    override fun getQuery(): Query = commentsRef

}
