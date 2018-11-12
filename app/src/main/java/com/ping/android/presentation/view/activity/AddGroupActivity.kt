package com.ping.android.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.configuration.GlideRequests
import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.rxbinding2.widget.RxTextView
import com.ping.android.R
import com.ping.android.model.User
import com.ping.android.model.enums.NetworkStatus
import com.ping.android.presentation.presenters.AddGroupPresenter
import com.ping.android.presentation.presenters.SearchUserPresenter
import com.ping.android.presentation.view.adapter.SelectContactAdapter
import com.ping.android.utils.ImagePickerHelper
import com.ping.android.utils.Log
import com.ping.android.utils.Toaster
import com.ping.android.utils.UiUtils
import com.ping.android.utils.configs.Constant
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_add_group.*
import java.io.File
import java.util.*
import javax.inject.Inject

class AddGroupActivity : CoreActivity(), View.OnClickListener, SearchUserPresenter.View, AddGroupPresenter.View {
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var etGroupName: EditText? = null
    private var edMessage: EditText? = null
    private var btSave: TextView? = null
    private var btBack: ImageView? = null
    private var groupAvatar: ImageView? = null
    private var recycleChatView: RecyclerView? = null
    private var noResultsView: LinearLayout? = null
    private var loading: View? = null

    private var imagePickerHelper: ImagePickerHelper? = null
    private var groupProfileImage: File? = null

    private lateinit var adapter: SelectContactAdapter
    private var selectedUsers = ArrayList<User>()
    private lateinit var glide: RequestManager

    @Inject
    lateinit var searchPresenter: SearchUserPresenter
    @Inject
    lateinit var presenter: AddGroupPresenter

    private val selectedPingId: List<String>
        get() {
            val selectedPingId = ArrayList<String>()
            for (user in selectedUsers) {
                selectedPingId.add(user.pingID)
            }
            return selectedPingId
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)
        AndroidInjection.inject(this)
        searchPresenter.create()
        presenter.create()
        bindViews()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        searchPresenter.destroy()
        presenter.destroy()
    }

    private fun bindViews() {
        etGroupName = findViewById(R.id.new_group_name)
        btBack = findViewById(R.id.new_group_back)
        btBack!!.setOnClickListener(this)
        btSave = findViewById(R.id.new_group_save)
        btSave!!.setOnClickListener(this)
        groupAvatar = findViewById(R.id.profile_image)
        groupAvatar!!.setOnClickListener(this)

        noResultsView = findViewById(R.id.no_results)

        edMessage = findViewById(R.id.new_group_message_tv)
        new_group_send_message_btn.setOnClickListener(this)
        loading = findViewById(R.id.spin_kit)

        findViewById<View>(R.id.new_group_select_contact).setOnClickListener(this)

        recycleChatView = findViewById(R.id.chat_list_view)
        mLinearLayoutManager = LinearLayoutManager(this)
        recycleChatView!!.layoutManager = mLinearLayoutManager

        edMessage!!.setOnFocusChangeListener { view, b ->
            if (b) {
                adapter.updateData(ArrayList())
            }
        }

        recycleChatView!!.setOnClickListener { checkReadySend() }
    }

    private fun init() {
        glide = GlideApp.with(this.applicationContext)
        adapter = SelectContactAdapter(ArrayList()) { contact, isSelected ->
            if (isSelected!!) {
                addContact(contact)
            } else {
                for (user in selectedUsers) {
                    if (user.key == contact.key) {
                        removeContact(contact)
                        break
                    }
                }
            }
        }
        recycleChatView!!.adapter = adapter
        registerEvent(RxTextView.textChangeEvents(edtTo)
                .subscribe({ textViewTextChangeEvent ->
                    val text = textViewTextChangeEvent.text().toString()
                    searchPresenter.searchUsers(text)
                }, { throwable -> throwable.printStackTrace() }))

        registerEvent(RxTextView
                .afterTextChangeEvents(edMessage!!)
                .subscribe { textViewAfterTextChangeEvent -> checkReadySend() })
        registerEvent(RxTextView
                .afterTextChangeEvents(etGroupName!!)
                .subscribe { textViewAfterTextChangeEvent -> checkReadySend() })
        checkReadySend()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.new_group_save -> onCreateGroup()
            R.id.new_group_back -> onCancelGroup()
            R.id.new_group_select_contact -> selectContact()
            R.id.new_group_send_message_btn -> onCreateGroup()
            R.id.profile_image -> presenter.handlePickerPress()
        }
    }

    private fun removeContact(contact: User) {
        val index = selectedUsers.indexOfFirst {
            it.key == contact.key
        }
        if (index >= 0) {
            chipGroup.removeViewAt(index)
            selectedUsers.removeAt(index)
            adapter.setSelectPingIDs(selectedPingId)
            if (selectedUsers.size == 0) {
                recipientsContainer.visibility = View.GONE
            }
        }
        checkReadySend()
    }

    private fun addContact(contact: User) {
        if (selectedUsers.contains(contact)) return
        selectedUsers.add(contact)
        val chip = Chip(this)
        chip.text = contact.displayName
        //chip.setChipIcon();
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        if (contact.profile !== null && contact.profile.startsWith("gs://")) {
            val gsReference = FirebaseStorage.getInstance().getReferenceFromUrl(contact.profile)
            (this.glide as GlideRequests)
                    .load(gsReference)
                    .placeholder(R.drawable.ic_avatar_gray)
                    .error(R.drawable.ic_avatar_gray)
                    .profileImage()
                    .into(object: SimpleTarget<Drawable>() {
                        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                            chip.chipIcon = resource
                        }
                    })
        } else {
            chip.chipIcon = ContextCompat.getDrawable(this, R.drawable.ic_avatar_gray)
        }
        chip.setOnCloseIconClickListener { v ->
            val childIndex = chipGroup.indexOfChild(v)
            if (childIndex != -1 && childIndex < selectedUsers.size) {
                removeContact(selectedUsers[childIndex])
            }
        }
        chipGroup.addView(chip)
        checkReadySend()
        // Scroll to bottom
        recipientsContainer.visibility = View.VISIBLE
        recipientsContainer.postDelayed({ recipientsContainer.fullScroll(View.FOCUS_DOWN) }, 500)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (imagePickerHelper != null) {
            imagePickerHelper!!.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            checkReadySend()
            if (resultCode == Activity.RESULT_OK) {
                val contacts: ArrayList<User> = data!!.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY)
                chipGroup.removeAllViews()
                selectedUsers.clear()
                contacts.map {
                    addContact(it)
                }
                adapter.setSelectPingIDs(selectedPingId)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (imagePickerHelper != null) {
            imagePickerHelper!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkReadySend() {
        var isEnabled = selectedUsers.size >0 && !TextUtils.isEmpty(etGroupName?.text.toString().trim())
//        (TextUtils.isEmpty(edMessage!!.text.toString().trim { it <= ' ' })
//                || selectedUsers.size <= 0
//                || TextUtils.isEmpty(etGroupName!!.text.toString().trim { it <= ' ' }))
//        new_group_send_message_btn.isEnabled = isEnabled
//        isEnabled = selectedUsers.size >0 && TextUtils.isEmpty(etGroupName?.text.toString().trim())
        btSave?.isEnabled = isEnabled
    }

    private fun selectContact() {
        val i = Intent(this, SelectContactActivity::class.java)
        i.putParcelableArrayListExtra("SELECTED_USERS", selectedUsers)
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST)
    }

    private fun onCreateGroup() {
        val groupNames = etGroupName!!.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(groupNames)) {
            Toaster.shortToast("Name this group.")
            return
        }
        if (networkStatus !== NetworkStatus.CONNECTED) {
            Toaster.shortToast("Please check network connection.")
            return
        }
        presenter.createGroup(selectedUsers, groupNames,
                if (groupProfileImage != null) groupProfileImage!!.absolutePath else "",
                edMessage!!.text.toString())
    }

    private fun onCancelGroup() {
        finish()
    }

    override fun openPicker() {
        imagePickerHelper!!.openPicker()
    }

    override fun showSearching() {
        loading!!.visibility = View.VISIBLE
    }

    override fun hideSearching() {
        loading!!.visibility = View.GONE
    }

    override fun showNoResults() {
        adapter.updateData(ArrayList())
        noResultsView!!.post { noResultsView!!.visibility = View.VISIBLE }
    }

    override fun hideNoResults() {
        noResultsView!!.post { noResultsView!!.visibility = View.GONE }
    }

    override fun displaySearchResult(users: List<User>) {
        adapter.updateData(ArrayList(users))
    }

    override fun moveToChatScreen(conversationID: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID)
        startActivity(intent)
        finish()
    }

    override fun initProfileImagePath(key: String) {
        val parentPath = getExternalFilesDir(null)!!.absolutePath + File.separator + "profile" + File.separator + key
        val parent = File(parentPath)
        if (!parent.exists()) {
            parent.mkdirs()
        }
        val timestamp = System.currentTimeMillis() / 1000.0
        val profileFileName = "$timestamp-$key.jpeg"
        val profileFilePath = parentPath + File.separator + profileFileName
        imagePickerHelper = ImagePickerHelper.from(this)
                .setFilePath(profileFilePath)
                .setCrop(true)
                .setListener(object : ImagePickerHelper.ImagePickerListener {
                    override fun onImageReceived(file: File) {

                    }

                    override fun onFinalImage(vararg files: File) {
                        groupProfileImage = files[0]
                        UiUtils.displayProfileAvatar(groupAvatar, groupProfileImage)
                    }
                })
    }

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }
}
