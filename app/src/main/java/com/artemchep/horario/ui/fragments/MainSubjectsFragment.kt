package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.MainActivity
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.IModel
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.toBoolean
import com.artemchep.horario.extensions.toInt
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * @author Artem Chepurnoy
 */
class MainSubjectsFragment : FragmentStoreSubjects<ISubject>(),
        Toolbar.OnMenuItemClickListener,
        AdapterBase.OnItemClickListener {

    private lateinit var userId: String

    private lateinit var subjectsQuery: Query

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }

        val firestore = FirebaseFirestore.getInstance()
        subjectsQuery = firestore
                .collection("users/$userId/subjects")
                .whereEqualTo("enabled", true)
        super.onAttach(context)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_main_subjects, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity as MainActivity
        val toolbar = activity.getToolbarMain()
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.___main_subjects)
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                }

                val host = activity as FragmentHost
                host.fragmentShow(SubjectEditFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            R.id.action_all -> {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                }

                val host = activity as FragmentHost
                host.fragmentShow(SubjectsFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
        }
        return false
    }

    override fun getQuery(): Query = subjectsQuery

    override fun getModel(snapshot: DocumentSnapshot): ISubject = Subject.from(snapshot)!!

}
