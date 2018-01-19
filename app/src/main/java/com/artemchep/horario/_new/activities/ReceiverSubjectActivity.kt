package com.artemchep.horario._new.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import com.artemchep.horario.Binfo
import com.artemchep.horario.Config
import com.artemchep.horario.R
import com.artemchep.horario.ui.fragments.FragmentBase
import com.artemchep.horario.ui.fragments.SubjectPreviewFragment
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class ReceiverSubjectActivity : MultiPaneActivity() {

    private var mFragment: FragmentBase? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val user = FirebaseAuth.getInstance().currentUser
        if (intent == null
                || intent.data == null
                || intent.action != Intent.ACTION_VIEW
                || user == null) {
            return
        }

        val data: Uri = intent.data
        val path = data.path

        Timber.d("path=$path")

        val subjectId = data.getQueryParameter("key")
        if (subjectId == null) {
            return
        }

        setupTheme()
        setupContent()

        if (savedInstanceState == null) {
            val args = Bundle().apply {
                putString(EXTRA_USER_ID, user.uid)
                putString(EXTRA_SUBJECT_ID, subjectId)
            }

            mFragment = SubjectPreviewFragment()
            mFragment?.arguments = args
            supportFragmentManager.beginTransaction()
                    .add(R.id.content, mFragment, TAG_FRAGMENT)
                    .commit()
        } else {
            mFragment = supportFragmentManager.getFragment(savedInstanceState, TAG_FRAGMENT) as FragmentBase
        }

        /*
        ShareHelper helper = new ShareHelper();
        helper.decode(path.substring(PATH_HORARIO_SUBJECT.length()));

        if (!helper.isCorrect()) {
            return;
        }

        setupTheme();
        setupContent();

        if (savedInstanceState == null) {
            Bundle args = new Bundle();
            args.putString(EXTRA_USER_ID, user.getUid());
            args.putString(EXTRA_SUBJECT_ID, helper.getArgs()[0]);

            mFragment = new SubjectPreviewFragment();
            mFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mFragment, TAG_FRAGMENT)
                    .commit();
        } else {
            mFragment = (FragmentBase) getSupportFragmentManager()
                    .getFragment(savedInstanceState, TAG_FRAGMENT);
        }
        */
    }

    private fun setupTheme() {
        val theme = Config.get<Int>(Config.KEY_UI_THEME)
        when (theme) {
            Config.THEME_LIGHT -> setTheme(R.style.AppThemeLight_Dialog)
            Config.THEME_DARK -> setTheme(R.style.AppTheme_Dialog)
            Config.THEME_BLACK -> setTheme(R.style.AppThemeBlack_Dialog)
        }

        val windowManager = window.attributes
        windowManager.dimAmount = 0.62f
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun setupContent() {
        setContentView(R.layout.___activity_dialog)
    }

    companion object {

        private val PATH_HORARIO_SUBJECT = "/horario/subject"

        private val TAG_FRAGMENT = "fragment::master"
    }

}
