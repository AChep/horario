package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.EXTRA_COLOR
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.FirebaseFirestore
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.thebluealliance.spectrum.internal.ColorUtil
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectModerateFragment : FragmentDocument(), Toolbar.OnMenuItemClickListener {

    companion object {
        private val TAG = "SubjectModerateFragment"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String

    private lateinit var subject: FireDocument<ISubject>

    private val adapterArgs = Bundle()

    private lateinit var toolbar: Toolbar
    private lateinit var appBar: AppBarLayout
    private lateinit var tabLayout: SmartTabLayout
    private lateinit var viewPager: ViewPager
    private lateinit var editMenuItem: MenuItem
    private lateinit var removeMenuItem: MenuItem

    /**
     * Interface color setter; Setting this actually changes the
     * accent color of the user interface.
     */
    private var color: Int = 0
        set(color) {
            if (field != color) {
                appBar.setBackgroundColor(color)
                UiHelper.updateToolbarTitle(toolbar, color)
                UiHelper.updateToolbarBackIcon(toolbar, color)
                UiHelper.updateToolbarMenuIcons(toolbar, color, R.id.action_edit)

                val isColorDark = ColorUtil.isColorDark(color)
                val tabColor = if (isColorDark) Color.WHITE else Color.BLACK
                tabLayout.apply {
                    setDefaultTabTextColor(tabColor)
                    setSelectedIndicatorColors(tabColor)
                    setViewPager(viewPager)
                }

                adapterArgs.putInt(EXTRA_COLOR, color)
            }

            field = color
        }

    override fun onAttach(context: Context?) {
        firestore = FirebaseFirestore.getInstance()
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
        }
        adapterArgs.apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_SUBJECT_ID, subjectId)
            putInt(EXTRA_COLOR, Palette.GREY)
        }

        Timber.tag(TAG).i("Attach: user_id=$userId subject_id=$subjectId")

        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        val firestore = FirebaseFirestore.getInstance()
        subject = FireDocument<ISubject>(
                firestore.document("subjects/$subjectId"), "subject",
                { snapshot -> Subject(id = snapshot.id).apply { inflateSubject(snapshot) } }
        ).also {
            it.value = arguments!!.getParcelable(EXTRA_SUBJECT)
            manager.link(it)
        }
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_moderate, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBar = view.findViewById(R.id.appbar)
        tabLayout = appBar.findViewById(R.id.tabs)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener {
                val intent = Intent(activity!!, SubjectActivity::class.java)
                NavUtils.navigateUpTo(activity!!, intent)
            }
            // Setup menu
            inflateMenu(R.menu.___subject_moderate)
            editMenuItem = menu.findItem(R.id.action_edit)
            removeMenuItem = menu.findItem(R.id.action_remove)
            setOnMenuItemClickListener(this@SubjectModerateFragment)
        }

        viewPager = view.findViewById(R.id.viewpager)
        viewPager.adapter = object : FragmentPagerItemAdapter(
                childFragmentManager, FragmentPagerItems.with(context)
                .add(R.string.__subject_members, SubjectModerateMembersFragment::class.java)
                .add(R.string.__subject_requests, SubjectModerateRequestsFragment::class.java)
                .create()) {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val item = super.instantiateItem(container, position)
                if (item is Fragment) try {
                    item.arguments = adapterArgs
                } catch (ignored: IllegalStateException) {
                }
                return item
            }

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        subject.observer.follow { subject ->
            color = subject?.color ?: Palette.UNKNOWN
            editMenuItem.isVisible = subject?.author == userId
            removeMenuItem.isVisible = subject?.author == userId
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> subject.value?.let {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putParcelable(EXTRA_SUBJECT, it)
                }

                // Start subject edit activity
                val host = activity as FragmentHost
                host.fragmentShow(SubjectEditFragment::class.java, args, 0)
            }
            R.id.action_remove -> {
                val md = MaterialDialog.Builder(context!!)
                        .content(R.string.subject_remove_q)
                        .positiveText(R.string.action_remove)
                        .negativeText(android.R.string.cancel)
                        .onPositive { dialog, which ->
                            // TODO: Not supported yet
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
            else -> return false
        }
        return true

    }
}