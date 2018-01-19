package com.artemchep.horario.ui.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcel
import android.support.design.widget.AppBarLayout
import android.support.v4.app.NavUtils
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.TextView
import com.artemchep.horario.Palette
import com.artemchep.horario.R
import com.artemchep.horario._new.UiHelper
import com.artemchep.horario._new.activities.SubjectActivity
import com.artemchep.horario.database.createParcel
import com.artemchep.horario.database.data.Subject
import com.artemchep.horario.database.models.IModel
import com.artemchep.horario.database.models.ISubject
import com.artemchep.horario.extensions.*
import com.artemchep.horario.ui.DialogHelper
import com.artemchep.horario.ui.drawables.CircleDrawable
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT
import com.artemchep.horario.ui.utils.EXTRA_SUBJECT_ID
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.google.firebase.database.Exclude
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.dmoral.toasty.Toasty
import timber.log.Timber

/**
 * @author Artem Chepurnoy
 */
class SubjectConfigureFragment : FragmentDocument(),
        View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    companion object {
        private val TAG = "SubjectModerateFragment"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var subjectId: String

    private lateinit var subject: FireDocument<ISubject>
    private lateinit var config: FireDocument<Config>

    private lateinit var toolbar: Toolbar
    private lateinit var appBar: AppBarLayout
    private lateinit var colorDrawable: CircleDrawable
    private lateinit var nameTextView: TextView
    private lateinit var subscribeSwitch: SwitchCompat
    private lateinit var subscribeContainer: View

    private var broadcasting: Boolean = false

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

                colorDrawable.color = color
            }

            field = color
        }

    override fun onAttach(context: Context?) {
        firestore = FirebaseFirestore.getInstance()
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
            subjectId = getString(EXTRA_SUBJECT_ID)
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
        config = FireDocument(
                firestore.document("subjects/$subjectId/configs/$userId"), "config",
                { Config.from(it) }
        ).also {
            manager.link(it)
        }
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_subject_configure, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        appBar = view.findViewById(R.id.appbar)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener {
                val intent = Intent(activity, SubjectActivity::class.java).apply {
                    putExtra(EXTRA_USER_ID, userId)
                    putExtra(EXTRA_SUBJECT_ID, subjectId)
                }

                NavUtils.navigateUpTo(activity!!, intent)
            }
        }

        view.findViewById<View>(R.id.name_container).withOnClick(this).run {
            nameTextView = view.findViewById(R.id.name)
            colorDrawable = CircleDrawable().also {
                view.findViewById<View>(R.id.color).background = it
            }
        }

        subscribeContainer = view.findViewById<View>(R.id.subscribe_container).apply {
            subscribeSwitch = findViewById(R.id.subscribe)
            subscribeSwitch.setOnCheckedChangeListener(this@SubjectConfigureFragment)
            setOnClickListener { subscribeSwitch.toggle() }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        subject.observer.follow { subject ->
            color = subject?.color ?: Palette.UNKNOWN
            nameTextView.text = subject?.name
        }

        config.observer.follow { config ->
            broadcasting = true

            setSubsEnabled(config != null)
            subscribeSwitch.isChecked = config?.enabled ?: false

            broadcasting = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.name_container -> subject.value?.let {
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, userId)
                    putParcelable(EXTRA_SUBJECT, it)
                }

                // Show `Edit subject locally` dialog
                val activity = activity as AppCompatActivity
                DialogHelper.showSubjectLocalDialog(activity, args)
            }
        }
    }

    override fun onCheckedChanged(btn: CompoundButton?, checked: Boolean) {
        if (broadcasting) {
            return
        }

        setSubsEnabled(false)
        firestore.batch().apply {
            val map: MutableMap<String, Any?> = hashMapOf(Pair("enabled", checked))
            withAll({ this@apply.set(this, map, SetOptions.merge()) },
                    firestore.document("subjects/$subjectId/configs/$userId"),
                    firestore.document("users/$userId/subjects/$subjectId"))
        }.commit().addOnCompleteListener(activity!!, { task ->
            setSubsEnabled(config.value != null)

            if (task.isSuccessful.not()) {
                Toasty.error(context!!, "Failed to set value").show()
            }
        })
    }

    private fun setSubsEnabled(enabled: Boolean) {
        subscribeSwitch.isEnabled = enabled
        subscribeContainer.isEnabled = enabled
    }

}

/**
 * @author Artem Chepurnoy
 */
data class Config(
        @Exclude
        override var id: String? = null,
        var enabled: Boolean = false) : IModel {

    companion object {

        @JvmField
        val CREATOR = createParcel { UserSubject(it) }

        fun from(snapshot: DocumentSnapshot): Config? {
            return if (snapshot.exists()) {
                Config(id = snapshot.id).apply {
                    enabled = snapshot.get("enabled", DocumentSnapshot::getBoolean) ?: false
                }
            } else null
        }

    }

    constructor(src: Parcel) : this(
            id = src.readString(),
            enabled = src.readInt().toBoolean()
    )

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.apply {
            writeString(id)
            writeInt(enabled.toInt())
        }
    }

}