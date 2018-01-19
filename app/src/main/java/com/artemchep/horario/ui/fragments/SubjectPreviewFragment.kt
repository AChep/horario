package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Role
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.IRole
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.setHtmlExclusive
import com.artemchep.horario.extensions.withAll
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.firestore.FirebaseFirestore
import es.dmoral.toasty.Toasty
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout
import org.sufficientlysecure.htmltextview.HtmlTextView
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectPreviewFragment : FragmentDocument(), View.OnClickListener {

    companion object {
        private const val TAG = "SubjectPreviewFragment"
        // State
        private const val STATE_SUBJECT = "state::subject"
        private const val STATE_ROLE = "state::role"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String

    private lateinit var subject: FireDocument<ISubject>
    private lateinit var member: FireDocument<IRole>
    private lateinit var request: FireDocument<IRole>

    private lateinit var appBar: AppBarLayout
    private lateinit var toolBar: Toolbar
    private lateinit var toolBarCollapsing: CollapsingToolbarLayout
    private lateinit var footerView: View
    private lateinit var userView: UserView
    private lateinit var descriptionHtmlView: HtmlTextView
    private lateinit var descriptionContainerView: View

    private lateinit var mStatusTextView: TextView
    private lateinit var mActionBtn: TextView

    /**
     * Interface color setter; Setting this actually changes the
     * accent color of the user interface.
     */
    private var color: Int = 0
        set(color) {
            if (field != color) {
                appBar.setBackgroundColor(color)
                toolBarCollapsing.setContentScrimColor(color)
                UiHelper.updateToolbarCloseIcon(toolBar, color)
                UiHelper.updateCollapsingToolbarTitle(toolBarCollapsing, color)
            }

            field = color
        }

    override fun onAttach(context: Context?) {
        firestore = FirebaseFirestore.getInstance()
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
        }

        Timber.tag(TAG).i("Attach: user_id=$userId subject_id=$subjectId")
        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        //
        // SUBJECT
        //
        subject = FireDocument<ISubject>("subjects/$subjectId", "subject", { snapshot ->
            Subject().apply { inflateSubject(snapshot) }
        }).also {
            it.value = arguments!!.getParcelable(EXTRA_SUBJECT)
            manager.link(it)
        }

        //
        // MEMBER
        //
        member = FireDocument<IRole>("subjects/$subjectId/members/$userId", "member", {
            Role()
        }).also(manager::link)

        //
        // REQUEST
        //
        request = FireDocument<IRole>("subjects/$subjectId/__requests/$userId", "request", {
            Role()
        }).also(manager::link)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject__preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appBar = view.findViewById(R.id.appbar)
        toolBarCollapsing = appBar.findViewById(R.id.toolbar_collapsing)
        toolBar = toolBarCollapsing.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { _ ->
                // This fragment has no logical upper task: finish
                // fragment.
                val host = activity as FragmentHost
                host.fragmentFinish(this@SubjectPreviewFragment)
            }
        }
        footerView = view.findViewById<View>(R.id.footer)
        userView = view.findViewById<UserView>(R.id.user).withOnClick(this)

        descriptionContainerView = view.findViewById(R.id.description_container)
        descriptionHtmlView = descriptionContainerView.findViewById(R.id.description)

        mStatusTextView = view.findViewById(R.id.status)
        mActionBtn = view.findViewById<TextView>(R.id.btn).withOnClick(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        subject.observer.follow { subject ->
            color = subject?.color ?: Palette.GREY
            toolBarCollapsing.title = subject?.name
            descriptionHtmlView.setHtmlExclusive(subject?.description, descriptionContainerView)
            userView.apply {
                modelId = subject?.author
                visibility = if (subject?.author.isNullOrEmpty()) {
                    View.GONE
                } else View.VISIBLE
            }
        }

        /**
         * Refreshes the state of the
         * [footer view][footerView].
         */
        val refreshFooter: (Int, IRole?, IRole?) -> Unit = { state, member, request ->
            footerView.visibility = when (state) {
                FireManager.SUCCESS -> {
                    when {
                        member != null -> {
                            mActionBtn.text = "Open"
                            mStatusTextView.text = "Joined"
                        }
                        request != null -> {
                            mActionBtn.text = "Remove"
                            mStatusTextView.text = "Request sent"
                        }
                        else -> {
                            mActionBtn.text = "Send request"
                            mStatusTextView.text = null
                        }
                    }

                    View.VISIBLE
                }
                else -> View.GONE
            }
        }

        withAll({
            follow {
                refreshFooter(manager.state.value, member.value, request.value)
            }
        }, member.observer, request.observer, manager.state)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.user -> userView.modelId?.let {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, it)
                    putParcelable(EXTRA_USER, userView.model)
                }

                // Start user activity
                val host = activity as FragmentHost
                host.fragmentShow(UserFragment::class.java, args, 0)
            }
            R.id.btn -> {
                when {
                    member.value != null -> {
                        val intent = Intent(context, SubjectActivity::class.java).apply {
                            putExtra(EXTRA_USER_ID, userId)
                            putExtra(EXTRA_SUBJECT_ID, subjectId)
                        }

                        // Start subject activity
                        startActivity(intent)
                    }
                    request.value != null -> dbRemoveRequest()
                    else -> dbCreateRequest()
                }
            }
        }
    }

    // --------------------------
    // -- DATABASE --------------
    // --------------------------

    private fun dbRemoveRequest() {
        Timber.tag(TAG).i("Db [remove_request]: user_id=$userId subject_key=$subjectId")

        // Commit request removal and show result
        // to user in a toast.
        request.ref.delete().addOnCompleteListener(activity!!, { task ->
            if (task.isSuccessful) {
                Toasty.normal(context!!, "Request removed").show()
            } else Toasty.error(context!!, "Failed to remove request").show()
        })
    }

    private fun dbCreateRequest() {
        Timber.tag(TAG).i("Db [create_request]: user_id=$userId subject_key=$subjectId")

        // Commit request sending and show result
        // to user in a toast.
        val map: MutableMap<String, Any?> = hashMapOf(Pair("tmp", true))
        request.ref.set(map).addOnCompleteListener(activity!!, { task ->
            if (task.isSuccessful) {
                Toasty.normal(context!!, "Request sent").show()
            } else Toasty.error(context!!, "Failed to send request").show()
        })
    }

}
