package com.artemchep.horario._new.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import com.artemchep.horario.Binfo
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario.extensions.contains
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.activities.ActivityHorario
import com.artemchep.horario.ui.activities.ChildActivity
import com.artemchep.horario.ui.utils.EXTRA_COLOR_IMPORTANT

/**
 * @author Artem Chepurnoy
 */
open class MultiPaneActivity : ActivityHorario(), FragmentHost {

    companion object {

        val EXTRA_TABLET_UI = "mpa::extra::tablet_ui"
        val EXTRA_REQUEST_CODE = "mpa::extra::request_code"

        val TAG_FRAGMENT = "fragment::master"
        val TAG_FRAGMENT_2 = "sdfsdf"
    }

    open val isTabletUi: Boolean
        get() = Binfo.IS_TABLET

    open var primaryColor: Int? = null

    override fun fragmentShow(clazz: Class<out Fragment>, args: Bundle?, flags: Int) {
        val isTablet = isTabletUi
        if (isTablet && flags contains FragmentHost.FLAG_AS_SECONDARY) {
            if (primaryColor != null) {
                args?.putInt(EXTRA_COLOR_IMPORTANT, Palette.UNKNOWN)
            }

            val fragment: Fragment
            try {
                fragment = clazz.newInstance()
                fragment.arguments = args
            } catch (ignored: Exception) {
                return
            }

            supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_details, fragment, TAG_FRAGMENT_2)
                    .commit()
        } else if (isTablet && flags contains FragmentHost.FLAG_AS_DIALOG) {
            val intent = DialogActivity.makeFor(this, clazz, args)
            startActivity(intent)
        } else {
            val intent = ChildActivity.makeFor(this, clazz, args)
            startActivity(intent)
        }
    }

    override fun fragmentFinish(fragment: Fragment) {
        if (supportFragmentManager.findFragmentByTag(TAG_FRAGMENT) === fragment) {
            supportFinishAfterTransition()
        }
    }

}
