package com.artemchep.horario._new.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.NavUtils
import android.support.v4.graphics.ColorUtils
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.DialogAction
import com.afollestad.materialdialogs.MaterialDialog
import com.artemchep.horario.Device
import com.artemchep.horario.Config
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario.database.data.Role
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.withAll
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.fragments.*
import com.artemchep.horario.ui.utils.EXTRA_COLOR
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.FirebaseFirestore
import com.ogaclejapan.smarttablayout.SmartTabLayout
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems
import com.thebluealliance.spectrum.internal.ColorUtil
import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout

/**
 * @author Artem Chepurnoy
 */
class SubjectActivity : MultiPaneActivity(),
        ViewPager.OnPageChangeListener,
        Toolbar.OnMenuItemClickListener,
        View.OnClickListener {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String

    private lateinit var memberDoc: FragmentDocument.FireDocument<Role>
    private lateinit var subjectUser: FragmentDocument.FireDocument<ISubject>
    private val manager = FragmentDocument.FireManager()

    private val adapterArgs = Bundle()

    private var pagePosition = -1 // invalid page as initial value

    private lateinit var fab: FloatingActionButton
    private lateinit var appBar: AppBarLayout
    private lateinit var tabLayout: SmartTabLayout
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarCollapsing: CollapsingToolbarLayout
    private lateinit var refreshMenuItem: MenuItem
    private lateinit var moderateMenuItem: MenuItem
    private lateinit var viewPager: ViewPager
    private lateinit var adapter: FragmentPagerItemAdapter

    /**
     * @author Artem Chepurnoy
     */
    interface Page {

        fun onFabClick(view: View) {}

        /**
         * @return `true` if this page should have
         * [fab][android.support.design.widget.FloatingActionButton] shown,
         * `false` otherwise
         */
        @DrawableRes
        fun hasFab(): Int = 0
    }

    override var primaryColor: Int? = Palette.GREY
        get() = color

    /**
     * Interface color setter; Setting this actually changes the
     * accent color of the user interface.
     */
    private var color: Int = 0
        set(color) {
            if (field != color) {
                appBar.setBackgroundColor(color)
                toolbarCollapsing.setContentScrimColor(color)
                UiHelper.updateToolbarBackIcon(toolbar, color)
                UiHelper.updateToolbarMenuIcons(toolbar, color)
                UiHelper.updateCollapsingToolbarTitle(toolbarCollapsing, color)

                if (Device.hasLollipopApi()) {
                    window.statusBarColor = ColorUtils.blendARGB(color, Color.BLACK, 0.6f)
                }

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply current theme
        when (Config.get<Int>(Config.KEY_UI_THEME)) {
            Config.THEME_LIGHT -> setTheme(R.style.AppThemeLight_NoActionBar)
            Config.THEME_DARK -> setTheme(R.style.AppTheme_NoActionBar)
            Config.THEME_BLACK -> setTheme(R.style.AppThemeBlack_NoActionBar)
        }

        intent.extras!!.run {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
        }

        firestore = FirebaseFirestore.getInstance()
        setupDatabase(savedInstanceState)

        setContentView(R.layout.___activity_subject)

        fab = findViewById<FloatingActionButton>(R.id.fab).apply {
            setOnClickListener(this@SubjectActivity)
        }
        appBar = findViewById(R.id.appbar)
        tabLayout = appBar.findViewById(R.id.tabs)
        toolbarCollapsing = appBar.findViewById(R.id.toolbar_collapsing)
        toolbar = toolbarCollapsing.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener {
                val activity = this@SubjectActivity
                val intent = Intent(activity, MainActivity::class.java)
                intent.addCategory(MainActivity.CATEGORY_SUBJECTS)
                NavUtils.navigateUpTo(activity, intent)
            }
            // Setup menu
            inflateMenu(R.menu.___subject)
            setOnMenuItemClickListener(this@SubjectActivity)
            refreshMenuItem = menu.findItem(R.id.action_refresh)!!
            moderateMenuItem = menu.findItem(R.id.action_moderate)!!
        }

        adapterArgs.putString(EXTRA_USER_ID, userId)
        adapterArgs.putString(EXTRA_SUBJECT_ID, subjectId)
        adapter = object : FragmentPagerItemAdapter(
                supportFragmentManager, FragmentPagerItems.with(this)
                .add(R.string.__subject_stream, SubjectStreamFragment::class.java)
                .add(R.string.__subject_tasks, SubjectTasksFragment::class.java)
                .add(R.string.__subject_schedule, SubjectSchedulesFragment::class.java)
                .add(R.string.__subject_students, SubjectMembersFragment::class.java)
                .add(R.string.__subject_about, SubjectAboutFragment::class.java)
                .create()) {

            override fun instantiateItem(container: ViewGroup, position: Int): Any {
                val item = super.instantiateItem(container, position)
                if (item is Fragment) try {
                    item.arguments = adapterArgs
                } catch (ignored: IllegalStateException) {
                }
                return item
            }

            override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
                super.setPrimaryItem(container, position, `object`)
                onPageSelected(position)
            }

        }

        viewPager = findViewById<ViewPager>(R.id.viewpager).apply {
            adapter = this@SubjectActivity.adapter
            addOnPageChangeListener(this@SubjectActivity)
        }

        setupObservers()
    }

    private fun setupDatabase(savedInstanceState: Bundle?) {
        memberDoc = FragmentDocument.FireDocument(
                "subjects/$subjectId/members/$userId",
                "member", {
            Role().apply { inflateRole(it) }
        }).also(manager::link)

        val extras = intent.extras!!
        subjectUser = FragmentDocument.FireDocument<ISubject>(
                "users/$userId/subjects/$subjectId",
                "subject_user", { snapshot ->
            Subject(id = snapshot.id).apply { inflateSubject(snapshot) }
        }).also {
            it.value = extras.getParcelable(EXTRA_SUBJECT)
            manager.link(it)
        }

        // Restore data
        savedInstanceState?.run {
            memberDoc.value = getParcelable(memberDoc.tag)
            subjectUser.value = getParcelable(subjectUser.tag)
        }
    }

    private fun setupObservers() {
        manager.state.follow { bind() }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.fab -> {
                // Redirect click event to current
                // page fragment
                val pos = viewPager.currentItem
                val page = adapter.getPage(pos) as Page
                page.onFabClick(v)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_refresh -> manager.refresh()
            R.id.action_moderate -> {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putString(EXTRA_SUBJECT_ID, subjectId)
                }

                // Start subject moderate activity
                val host = activity as FragmentHost
                host.fragmentShow(SubjectModerateFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            R.id.action_configure -> {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putString(EXTRA_SUBJECT_ID, subjectId)
                }

                // Start subject configure activity
                val host = activity as FragmentHost
                host.fragmentShow(SubjectConfigureFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            R.id.action_leave -> {
                val md = MaterialDialog.Builder(this)
                        .content("Leave subject? This will not remove your contributions")
                        .positiveText(R.string.action_leave)
                        .negativeText(android.R.string.cancel)
                        .onPositive { _, _ ->
                            firestore.batch().apply {
                                delete(memberDoc.ref)

                                // Do the job of our trigger functions: remove this subject from
                                // user directly.
                                delete(firestore.document("users/$userId/subjects/$subjectId"))
                            }.commit()
                            supportFinishAfterTransition()
                        }
                        .build()

                md.getActionButton(DialogAction.POSITIVE).setTextColor(Palette.RED)
                md.show()
            }
            else -> return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        manager.start()
    }

    override fun onStop() {
        manager.stop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        withAll({
            outState?.putParcelable(tag, value)
        }, memberDoc, subjectUser)
    }

    @SuppressLint("NewApi")
    private fun bind() {
        val state = manager.state.value
        val title: String
        when (state) {
            FragmentDocument.FireManager.ERROR -> {
                color = Palette.RED
                title = "Something went wrong"
            }
            else -> {
                val sub: ISubject? = subjectUser.value
                color = sub?.color ?: Palette.GREY
                title = sub?.name ?: if (state == FragmentDocument.FireManager.LOADING) {
                    UiHelper.TEXT_PLACEHOLDER
                } else "Subject not found"
            }
        }

        toolbarCollapsing.title = title
        refreshMenuItem.isVisible = state == FragmentDocument.FireManager.ERROR
        moderateMenuItem.isVisible = memberDoc.value?.role?.let { it >= Role.ADMIN } == true
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        if (pagePosition == position) {
            return
        } else pagePosition = position

        val page = adapter.getPage(position) as? Page
        if (page != null) {
            val icon = page.hasFab()
            if (icon != 0) {
                fab.setImageResource(icon)
                fab.show()
            } else fab.hide()
        }
    }

    override fun onPageScrollStateChanged(state: Int) {}

}