package com.artemchep.horario.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.R

/**
 * @author Artem Chepurnoy
 */
class MainHomeTasksFragment : FragmentMain() {

    companion object {
        private const val TAG = "MainHomeTasksFragment"
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.___fragment_main_home_tasks, container, false)
    }

}