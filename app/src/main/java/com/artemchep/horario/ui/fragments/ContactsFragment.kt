package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.artemchep.horario.R
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * @author Artem Chepurnoy
 */
class ContactsFragment : FragmentStoreUsers<IUser>() {

    private lateinit var userId: String

    private lateinit var usersQuery: Query

    override fun getQuery(): Query = usersQuery

    override fun getModel(snapshot: DocumentSnapshot): IUser = User(id = snapshot.id)

    override fun getModel(snapshot: DocumentSnapshot, user: IUser): IUser? = User.from(snapshot)

    override fun onAttach(context: Context?) {
        arguments!!.run {
            userId = getString(EXTRA_USER_ID)
        }

        val firestore = FirebaseFirestore.getInstance()
        usersQuery = firestore.collection("users/$userId/contacts")

        super.onAttach(context)
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_contacts, container, false)
    }

}
