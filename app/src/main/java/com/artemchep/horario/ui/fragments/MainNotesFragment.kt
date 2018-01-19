package com.artemchep.horario.ui.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.R
import com.artemchep.horario._new.activities.DialogActivity
import com.artemchep.horario._new.activities.MainActivity
import com.artemchep.horario.database.data.Note
import com.artemchep.horario.database.models.INote
import com.artemchep.horario.extensions.findMenuItemView
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.EXTRA_NOTE
import com.artemchep.horario.ui.utils.EXTRA_NOTE_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


/**
 * @author Artem Chepurnoy
 */
class MainNotesFragment : FragmentStoreNotes<INote>(),
        Toolbar.OnMenuItemClickListener {

    private lateinit var userId: String

    private lateinit var notesRef: CollectionReference

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }

        val firestore = FirebaseFirestore.getInstance()
        notesRef = firestore.collection("users/$userId/notes")
        super.onAttach(context)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_main_notes, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = activity as MainActivity
        val toolbar = activity.getToolbarMain()
        toolbar.menu.clear()
        toolbar.inflateMenu(R.menu.___main_notes)
    }

    @SuppressLint("RestrictedApi")
    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add -> {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                }

                val host = activity as FragmentHost
                host.fragmentShow(NoteFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            else -> return false
        }
        return true
    }

    override fun onItemClick(view: View, model: INote) {
        val args = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_NOTE_ID, model.id!!)
            putParcelable(EXTRA_NOTE, model)
        }

        val host = activity as FragmentHost
        host.fragmentShow(NoteFragment::class.java, args, FragmentHost.FLAG_AS_SECONDARY)
    }

    override fun getQuery(): Query = notesRef

    override fun getReference(): CollectionReference = notesRef

    override fun getModel(snapshot: DocumentSnapshot): INote = Note.from(snapshot)!!

}