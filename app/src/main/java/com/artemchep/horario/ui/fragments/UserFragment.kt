package com.artemchep.horario.ui.fragments

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.artemchep.horario.R
import com.artemchep.horario.database.data.User
import com.artemchep.horario.database.models.IUser
import com.artemchep.horario.extensions.setTextExclusive
import com.artemchep.horario.extensions.withAll
import com.artemchep.horario.extensions.withOnClick
import com.artemchep.horario.interfaces.FragmentHost
import com.artemchep.horario.ui.utils.EXTRA_PERSON
import com.artemchep.horario.ui.utils.EXTRA_PERSON_ID
import com.artemchep.horario.ui.utils.EXTRA_USER
import com.artemchep.horario.ui.utils.EXTRA_USER_ID
import com.artemchep.horario.ui.widgets.UserView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.jackandphantom.circularprogressbar.CircleProgressbar
import es.dmoral.toasty.Toasty
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import pl.tajchert.nammu.Nammu
import pl.tajchert.nammu.PermissionCallback
import timber.log.Timber
import java.io.File

/**
 * @author Artem Chepurnoy
 */
class UserFragment : FragmentDocument(), Toolbar.OnMenuItemClickListener, View.OnClickListener {

    companion object {
        private val TAG = "UserFragment"

        /**
         * Create the bundle of required arguments for this
         * fragment.
         * @param userId id of the current user
         * @param person model of the person, that will be temporarily used until we
         * fetch upstream model
         */
        fun args(userId: String,
                 personId: String,
                 person: IUser? = null
        ): Bundle = Bundle().apply {
            putString(EXTRA_USER_ID, userId)
            putString(EXTRA_PERSON_ID, personId)
            putParcelable(EXTRA_PERSON, person)
        }
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var userId: String
    private lateinit var personId: String

    private lateinit var person: FireDocument<IUser>

    private var uploadTask: UploadTask? = null

    private lateinit var fab: FloatingActionButton
    private lateinit var appBar: AppBarLayout
    private lateinit var toolbar: Toolbar
    private lateinit var editMenuItem: MenuItem
    private lateinit var editAvatarMenuItem: MenuItem
    private lateinit var userView: UserView
    private lateinit var userAvatarProgressView: CircleProgressbar
    /** Container for [emailTextView] and others */
    private lateinit var paperView: View
    private lateinit var cancelView: View
    private lateinit var emailTextView: TextView

    override fun onAttach(context: Context?) {
        firestore = FirebaseFirestore.getInstance()
        arguments!!.apply {
            userId = getString(EXTRA_USER_ID)
            personId = getString(EXTRA_PERSON_ID)
        }

        super.onAttach(context)
    }

    override fun onLinkDatabase(manager: FireManager) {
        person = FireDocument<IUser>(
                firestore.document("users/$personId"), "person",
                { snapshot -> User(id = snapshot.id).apply { inflateUser(snapshot) } }
        ).also {
            it.value = arguments!!.getParcelable(EXTRA_PERSON)
            manager.link(it)
        }
    }

    override fun onCreateContentView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.___fragment_user, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = view.findViewById<FloatingActionButton>(R.id.fab).withOnClick(this)

        appBar = view.findViewById(R.id.appbar)
        toolbar = appBar.findViewById<Toolbar>(R.id.toolbar).apply {
            setNavigationOnClickListener { _ ->
                val host = activity as FragmentHost
                host.fragmentFinish(this@UserFragment)
            }

            inflateMenu(R.menu.user)
            setOnMenuItemClickListener(this@UserFragment)

            menu.run {
                editMenuItem = findItem(R.id.action_edit)
                editAvatarMenuItem = findItem(R.id.action_upload_avatar)
            }
        }

        paperView = view.findViewById(R.id.paper)
        userView = view.findViewById(R.id.user)
        userAvatarProgressView = view.findViewById<CircleProgressbar>(R.id.user_avatar_progress).apply {
            val density = resources.displayMetrics.density
            backgroundProgressWidth = Math.round(6 * density)
            foregroundProgressWidth = Math.round(6 * density)
        }
        cancelView = view.findViewById<View>(R.id.cancel).withOnClick(this)
        emailTextView = view.findViewById<TextView>(R.id.email).withOnClick(this)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        person.state.follow { state ->
            // Show an options to edit the user after successfully
            // loading current data.
            val editable = state == FireManager.SUCCESS && userId == personId
            withAll({ isVisible = editable }, editMenuItem, editAvatarMenuItem)
        }

        person.observer.follow { p ->
            Timber.tag(TAG).d("Person BIND person=$p")

            // Bind basic information:
            // avatar, name and biography
            userView.state = person.state.value
            userView.model = p

            // Bind additional information
            paperView.visibility = if (p != null && !p.email.isNullOrEmpty()) {
                emailTextView.setTextExclusive(p.email)

                View.VISIBLE // Show paper container
            } else View.GONE // Hide paper container
        }
    }

    override fun onStop() {
        uploadTask?.cancel()
        super.onStop()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.email -> person.value?.email?.let {
                // Copy link to clipboard
                val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Email", it)
                clipboard.primaryClip = clip

                // Show toast message
                val msg = getString(R.string.details_email_clipboard, it)
                Toasty.info(context!!, msg).show()
            }
            R.id.fab -> {
                // TODO: Open chat fragment
                Toasty.normal(context!!, "Not implemented yet").show()
            }
            R.id.cancel -> uploadTask?.cancel()
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_upload_avatar -> {
                withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, {
                    EasyImage.openChooserWithDocuments(this@UserFragment, "Choose avatar", 0)
                }, {
                    Toasty.warning(context!!, "Cannot open picker").show()
                })
            }
            R.id.action_edit -> {
                val user = person.value ?: User(id = userId)
                val args = Bundle().apply {
                    putString(EXTRA_USER_ID, user.id!!)
                    putParcelable(EXTRA_USER, user)
                }

                val host = activity as FragmentHost
                host.fragmentShow(UserEditFragment::class.java, args, FragmentHost.FLAG_AS_DIALOG)
            }
            else -> return false
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, activity, object : DefaultCallback() {
            override fun onImagePickerError(e: Exception, source: EasyImage.ImageSource, type: Int) {
                Toasty.error(context!!, "Failed to pick image").show()
            }

            override fun onImagePicked(imageFile: File?, source: EasyImage.ImageSource?, type: Int) {
                imageFile?.let { initMeh(it) }
            }

            override fun onCanceled(source: EasyImage.ImageSource, type: Int) {
                // Delete temporary filed,
                // created by a camera
                if (source == EasyImage.ImageSource.CAMERA) {
                    EasyImage.lastlyTakenButCanceledPhoto(activity)?.delete()
                }
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Nammu.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun initMeh(file: File) {
        val uri = Uri.fromFile(file)
        val ref = FirebaseStorage.getInstance().getReference("users/$userId/avatar")

        cancelView.visibility = View.VISIBLE
        uploadTask = ref.putFile(uri).apply {
            addOnProgressListener(activity!!, { snapshot: UploadTask.TaskSnapshot ->
                val progress = 100f * snapshot.bytesTransferred / snapshot.totalByteCount
                userAvatarProgressView.progress = progress
            })

            addOnCompleteListener(activity!!, { task ->
                // Hide progress view by resetting its
                // progress.
                userAvatarProgressView.progress = 0f
                cancelView.visibility = View.GONE

                if (task.isSuccessful) {
                    Toasty.success(context!!, "Avatar updated").show()

                    // Update avatar url
                    val url = task.result.downloadUrl.toString()
                    val map: MutableMap<String, Any?> = hashMapOf(Pair("avatar_url", url))
                    firestore.document("/users/$userId").set(map, SetOptions.merge())
                } else Toasty.error(context!!, "Failed to update avatar").show()

                uploadTask = null
            })
        }
    }

    /**
     * Starts something with permissions check.
     */
    private fun withPermission(permission: String, success: () -> Unit, error: () -> Unit) {
        if (Nammu.hasPermission(activity, permission)) {
            success.invoke()
            return
        }

        Nammu.askForPermission(this, permission, object : PermissionCallback {
            override fun permissionGranted() {
                success.invoke()
            }

            override fun permissionRefused() {
                error.invoke()
            }
        })
    }

}
