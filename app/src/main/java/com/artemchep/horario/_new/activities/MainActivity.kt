package com.artemchep.horario._new.activities

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.artemchep.horario.Config
import com.artemchep.horario.R
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.DialogHelper
import com.artemchep.horario.ui.activities.AuthActivity
import com.artemchep.horario.ui.fragments.*
import com.artemchep.horario.ui.utils.EXTRA_PERSON
import com.artemchep.horario.ui.utils.EXTRA_PERSON_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.auth.FirebaseAuth
import com.roughike.bottombar.BottomBar

private const val EMPTY_USER_ID: String = "logged-out"

/**
 * @author Artem Chepurnoy
 */
class MainActivity : MultiPaneActivity(),
        NavigationView.OnNavigationItemSelectedListener,
        FirebaseAuth.AuthStateListener, Toolbar.OnMenuItemClickListener {

    companion object {
        const val CATEGORY_HOME = "category_home"
        const val CATEGORY_SCHEDULE = "category_schedule"
        const val CATEGORY_SUBJECTS = "category_subjects"
        const val CATEGORY_NOTES = "category_notes"
        const val CATEGORY_CHATS = "category_chats"
    }

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()

    private var fragment: Fragment? = null

    private lateinit var appBar: AppBarLayout
    private lateinit var userView: UserView
    private lateinit var drawer: DrawerLayout
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navView: NavigationView

    lateinit var toolbar: Toolbar
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply current theme
        val theme: Int = Config[Config.KEY_UI_THEME]
        when (theme) {
            Config.THEME_LIGHT -> setTheme(R.style.AppThemeLight_NoActionBar)
            Config.THEME_DARK -> setTheme(R.style.AppTheme_NoActionBar)
            Config.THEME_BLACK -> setTheme(R.style.AppThemeBlack_NoActionBar)
        }

        setContentView(R.layout.___activity_main)

        auth = FirebaseAuth.getInstance()
        auth.addAuthStateListener(this)
        if (auth.currentUser == null) {
            startActivity(Intent(this, AuthActivity::class.java))
        }

        // Views
        appBar = findViewById(R.id.appbar)
        toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            setOnMenuItemClickListener(this@MainActivity)
        }

        // Setup drawer
        drawer = findViewById(R.id.drawer)
        drawerToggle = ActionBarDrawerToggle(this, drawer, toolbar, 0, 0)
        drawerToggle.isDrawerIndicatorEnabled = true
        drawer.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        navView = drawer.findViewById(R.id.nav)
        navView.setNavigationItemSelectedListener(this)

        // Setup user view
        userView = navView.getHeaderView(0).findViewById(R.id.user)
        userView.run {
            findViewById<View>(R.id.logout).setOnClickListener { auth.signOut() }
            avatarView?.setOnClickListener { _ ->
                userView.modelId?.let {
                    val args = Bundle().apply {
                        putString(EXTRA_USER_ID, it)
                        putString(EXTRA_PERSON_ID, it)
                        putParcelable(EXTRA_PERSON, userView.model)
                    }

                    fragmentShow(UserFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
                }

                drawer.closeDrawer(navView)
            }
        }

        val bottomBar = findViewById<BottomBar>(R.id.bottomBar)
        val bottomBarColor = when (theme) {
            Config.THEME_LIGHT -> 0xFFFFFF or Color.BLACK
            else -> 0x222222/*424242*/ or Color.BLACK
        }
        for (i in 0 until bottomBar.tabCount) {
            val tab = bottomBar.getTabAtPosition(i)
            tab.barColorWhenSelected = bottomBarColor
        }

        bottomBar.setDefaultTab(retrieveStartupItem())
        bottomBar.onRestoreInstanceState(savedInstanceState)
        bottomBar.setOnTabSelectListener { tabId ->
            when (tabId) {
                R.id.tab_home -> showFragment(MainHomeFragment::class.java)
                R.id.tab_schedule -> showFragment(MainScheduleFragment::class.java)
            /*{
                val intent = Intent(this, SyncSubjectsService::class.java)
                startService(intent)
            }*/
                R.id.tab_classes -> showFragment(MainSubjectsFragment::class.java)
                R.id.tab_contacts -> showFragment(MainNotesFragment::class.java)
            }
        }

        fragment = supportFragmentManager.findFragmentByTag(TAG_FRAGMENT)
    }

    fun getToolbarMain(): Toolbar {
        return toolbar
    }

    private fun retrieveStartupItem(): Int {
        val intent = intent

        if (intent != null && intent.categories != null) {
            val cat = intent.categories
            when {
                cat.contains(CATEGORY_HOME) -> return R.id.tab_home
                cat.contains(CATEGORY_SCHEDULE) -> return R.id.tab_schedule
                cat.contains(CATEGORY_SUBJECTS) -> return R.id.tab_classes
                cat.contains(CATEGORY_NOTES) -> return R.id.tab_contacts
                cat.contains(CATEGORY_CHATS) -> return R.id.tab_chat
            }
        }

        return R.id.tab_home
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        drawerToggle.syncState()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return fragment?.takeIf { it is Toolbar.OnMenuItemClickListener }?.let {
            val listener = fragment as Toolbar.OnMenuItemClickListener
            listener.onMenuItemClick(item)
        } ?: false
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        navView.setCheckedItem(id)

        // Handle item selection
        if (id == R.id.nav_notifications
                || id == R.id.nav_attendance
                || id == R.id.nav_contacts
                || id == R.id.nav_database
                || id == R.id.nav_schedules
                || id == R.id.nav_settings
                || id == R.id.nav_donate) {
            val clazz: Class<out Fragment> = when (id) {
                R.id.nav_notifications -> NotificationsFragment::class.java
                R.id.nav_attendance -> NotificationsFragment::class.java
                R.id.nav_contacts -> ContactsFragment::class.java
                R.id.nav_database -> DatabaseFragment::class.java
                R.id.nav_schedules -> SchedulesFragment::class.java
                R.id.nav_settings -> SettingsFragment::class.java
                else -> return false
            }

            val args = Bundle().apply {
                val uid = auth.currentUser?.uid ?: EMPTY_USER_ID
                putString(EXTRA_USER_ID, uid)
            }

            // Show selected fragment
            fragmentShow(clazz, args, 0)
        } else if (id == R.id.nav_feedback) {
            DialogHelper.showFeedbackDialog(this)
        } else if (id == R.id.nav_privacy_policy) {
            DialogHelper.showPrivacyDialog(this)
        } else if (id == R.id.nav_about) {
            DialogHelper.showAboutDialog(this)
        } else if (id == R.id.nav_holidays_mode) {
            DialogHelper.showHolidayModeDialog(this)
        }

        drawer.closeDrawer(navView)
        return false
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        drawerToggle.onConfigurationChanged(newConfig)
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        userView.modelId = auth.currentUser?.uid
    }

    override fun onDestroy() {
        auth.removeAuthStateListener(this)
        super.onDestroy()
    }

    private fun showFragment(clazz: Class<out Fragment>) {
        val fragment = try {
            clazz.newInstance()
        } catch (ignored: Exception) {
            return
        }

        // Set arguments
        fragment.arguments = Bundle().apply {
            val id = auth.currentUser?.uid ?: EMPTY_USER_ID
            putString(EXTRA_USER_ID, id)
        }

        // Replace the fragment
        this.fragment = fragment
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment, fragment, TAG_FRAGMENT)
                .commit()
    }

}
