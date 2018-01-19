package com.artemchep.horario.ui.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Role
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.IRole
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.setHtmlExclusive
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.drawables.CircleDrawable
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.crash.FirebaseCrash
import es.dmoral.toasty.Toasty
import org.sufficientlysecure.htmltextview.HtmlTextView
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectAboutFragment : FragmentDocument(), SubjectActivity.Page, View.OnClickListener {

    companion object {
        private const val TAG = "SubjectAboutFragment"
    }

    private lateinit var userId: String
    private lateinit var subjectId: String
    private lateinit var link: CharSequence

    private lateinit var subjectDoc: FireDocument<ISubject>
    private lateinit var memberDoc: FireDocument<IRole>

    private lateinit var infoTextView: HtmlTextView
    private lateinit var nameContainerView: View
    private lateinit var nameCircleDrawable: CircleDrawable
    private lateinit var nameTextView: TextView
    private lateinit var editButton: View
    private lateinit var userView: UserView

    override fun onAttach(context: Context?) {
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
        }
        link = "https://artemchep.com/horario/subject?key=$subjectId"

        Timber.tag(TAG).d("Attach: user_id=$userId subject_id=$subjectId")
        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        subjectDoc = FireDocument<ISubject>("subjects/$subjectId", "subject", {
            Subject(id = it.id).apply { inflateSubject(it) }
        }).also {
            it.value = arguments!!.getParcelable(EXTRA_SUBJECT)
            manager.link(it)
        }

        memberDoc = FireDocument<IRole>("subjects/$subjectId/members/$userId", "member", {
            Role().apply { inflateRole(it) }
        }).also(manager::link)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.share).setOnClickListener(this)
        view.findViewById<View>(R.id.link_container).withOnClick(this).run {
            val tv = findViewById<TextView>(R.id.link)
            tv.text = link
        }

        nameContainerView = view.findViewById<View>(R.id.name_container).apply {
            nameCircleDrawable = CircleDrawable()
            nameTextView = findViewById(R.id.name)
            editButton = findViewById<View>(R.id.edit).withOnClick(this@SubjectAboutFragment)

            val v = findViewById<View>(R.id.color)
            v.background = nameCircleDrawable
        }

        userView = view.findViewById<UserView>(R.id.user).withOnClick(this)
        infoTextView = view.findViewById(R.id.info)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        /**
         * Refreshes the state of the
         * [editButton][edit button].
         */
        val refreshEditButton: (ISubject?, IRole?) -> Unit = { subject, member ->
            val editable = subject != null && member?.role == Role.OWNER
            editButton.visibility = if (editable) View.VISIBLE else View.GONE
        }

        subjectDoc.observer.follow { subject ->
            userView.apply {
                modelId = subject?.author
                visibility = if (modelId.isNullOrEmpty()) {
                    View.GONE
                } else View.VISIBLE
            }

            infoTextView.setHtmlExclusive(subject?.description)
            nameTextView.setTextExclusive(subject?.name, nameContainerView)
            nameCircleDrawable.color = subject?.color ?: Palette.UNKNOWN

            refreshEditButton(subject, memberDoc.value)
        }

        memberDoc.observer.follow { member -> refreshEditButton(subjectDoc.value, member) }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.user -> userView.modelId?.let {
                val host = activity as FragmentHost
                val args = UserFragment.args(userId = userId, personId = it, person = userView.model)
                host.fragmentShow(UserFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }

            R.id.link_container -> link.let {
                // Copy link to clipboard
                val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Link to the subject", it)
                clipboard.primaryClip = clip

                // Show toast message
                val msg = getString(R.string.details_link_clipboard, it)
                Toasty.normal(context!!, msg).show()
            }
            R.id.share -> link.let {
                val name = subjectDoc.observer.value?.name ?: getString(R.string.__subject_about__join_subject)
                val text = getString(R.string.__subject_about__join_text, name, it)

                try {
                    val i = Intent(Intent.ACTION_SEND)
                    i.type = "description/plain"
                    i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    i.putExtra(Intent.EXTRA_TEXT, text)
                    startActivity(Intent.createChooser(i, getString(R.string.share_link_via)))
                } catch (e: Exception) {
                    FirebaseCrash.report(Exception("Failed to share the subject!", e))
                }
            }

            R.id.edit -> subjectDoc.value?.let {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putParcelable(EXTRA_SUBJECT, it)
                }

                val host = activity as FragmentHost
                host.fragmentShow(SubjectEditFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
        }
    }

    override fun onFabClick(view: View) = Unit

    override fun hasFab(): Int = 0

}
