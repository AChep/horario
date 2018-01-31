package com.artemchep.horario.ui.fragments

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.artemchep.horario.Device
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.get
import com.artemchep.horario.extensions.toBoolean
import com.artemchep.horario.extensions.toInt
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.adapters.AdapterBase
import com.artemchep.horario.ui.adapters.SubjectsAdapter
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

/**
 * @author Artem Chepurnoy
 */
class SubjectsFragment : FragmentStoreSubjects<UserSubject>(),
        AdapterBase.OnItemClickListener,
        View.OnClickListener {

    private lateinit var userId: String

    /** Query of a collection of user's subjects including both
     * enabled  and disabled ones */
    private lateinit var query: Query

    private lateinit var fab: FloatingActionButton
    private lateinit var appBar: AppBarLayout

    override val comparator: Comparator<UserSubject> = compareBy(
            { it.enabled.not() }, // enabled first
            { it.name },
            { it.id }
    )

    override fun onAttach(context: Context?) {
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
        }

        val firestore = FirebaseFirestore.getInstance()
        query = firestore.collection("users/$userId/subjects")
        super.onAttach(context)
    }

    override fun onCreateAdapter(list: MutableList<UserSubject>): Adapter = Adapter(list)

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subjects, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fab = view.findViewById<FloatingActionButton>(R.id.fab).withOnClick(this)
        appBar = view.findViewById(R.id.appbar)

        view.findViewById<RecyclerView>(R.id.recycler).run {
            addOnScrollListener(AppBarShadow(appBar))
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fab -> {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                }

                val host = activity as FragmentHost
                host.fragmentShow(SubjectEditFragment::class.java, args, 0)
            }
        }
    }

    override fun getQuery(): Query = query

    override fun getModel(snapshot: DocumentSnapshot): UserSubject = UserSubject.from(snapshot)!!

    /**
     * @author Artem Chepurnoy
     */
    class AppBarShadow(private val view: View): RecyclerView.OnScrollListener() {

        private var elevated: Boolean = false

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val offset = recyclerView.computeVerticalScrollOffset()
            if (offset > 1f) {
                setElevated(true)
            } else setElevated(false)
        }

        private fun setElevated(elevated: Boolean) {
            if (this.elevated != elevated) {
                this.elevated = elevated
            } else return

            if (Device.hasLollipopApi()) {
                view.elevation = if (elevated) {
                    14f
                } else 0f
            }
        }

    }

    /**
     * @author Artem Chepurnoy
     */
    class Adapter(list: List<UserSubject>) : SubjectsAdapter<UserSubject>(list) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val v = inflater.inflate(R.layout.___item_subject, parent, false)
            return ViewHolder(this, v)
        }

        override fun onBindViewHolder(h: SubjectsAdapter.ViewHolder, position: Int) {
            super.onBindViewHolder(h, position)
            val subject = getItem(position)
            h.nameTextView.alpha = if (subject.enabled) 1f else 0.4f
        }

    }
}

/**
 * @author Artem Chepurnoy
 */
data class UserSubject(
        @Exclude
        override var id: String? = null,
        override var name: String? = null,
        override var author: String? = null,
        override var description: String? = null,
        override var archived: Boolean = false,
        override var color: Int = 0,
        var enabled: Boolean = false) : ISubject {

    companion object {

        @JvmField
        val CREATOR = createParcel { UserSubject(it) }

        fun from(snapshot: DocumentSnapshot): UserSubject? {
            return if (snapshot.exists()) {
                UserSubject(id = snapshot.id).apply {

                    name = snapshot.getString("name")
                    author = snapshot.getString("author")
                    description = snapshot.getString("description")
                    color = snapshot.getLong("color")?.toInt() ?: Palette.GREY
                    enabled = snapshot.get("enabled", DocumentSnapshot::getBoolean) ?: false
                }
            } else null
        }

    }

    constructor(id: String?) : this(id, null)

    constructor(src: Parcel) : this(
            id = src.readString(),
            name = src.readString(),
            author = src.readString(),
            description = src.readString(),
            archived = src.readInt().toBoolean(),
            enabled = src.readInt().toBoolean(),
            color = src.readInt()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeString(name)
            writeString(author)
            writeString(description)
            writeInt(archived.toInt())
            writeInt(enabled.toInt())
            writeInt(color)
        }
    }

}
