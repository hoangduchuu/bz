package com.ping.android.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle

import androidx.recyclerview.widget.LinearLayoutManager

import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.configuration.GlideRequests

import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import com.jakewharton.rxbinding2.widget.RxTextView
import com.ping.android.R
import com.ping.android.domain.usecase.conversation.NewCreatePVPConversationUseCase
import com.ping.android.model.User
import com.ping.android.presentation.presenters.NewChatPresenter
import com.ping.android.presentation.presenters.SearchUserPresenter
import com.ping.android.presentation.view.adapter.SelectContactAdapter
import com.ping.android.utils.Toaster
import com.ping.android.utils.configs.Constant

import java.util.ArrayList

import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_new_chat.*
import javax.inject.Inject

class NewChatActivity : CoreActivity(), View.OnClickListener, NewChatPresenter.NewChatView, SearchUserPresenter.View {
    private val TAG = NewChatActivity::class.java.simpleName
    //Views UI
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var btBack: ImageView? = null
    private var btSelectContact: ImageView? = null
    private var noResultsView: LinearLayout? = null
    private var btnDone: Button? = null
    private var loading: View? = null

    private lateinit var adapter: SelectContactAdapter
    private var selectedUsers = ArrayList<User>()

    private var isAddMember = false

    private lateinit var glide: RequestManager

    @Inject
    lateinit var presenter: NewChatPresenter
    @Inject
    lateinit var searchUserPresenter: SearchUserPresenter

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
        setContentView(R.layout.activity_new_chat)
        AndroidInjection.inject(this)
        isAddMember = intent.getBooleanExtra("ADD_MEMBER", false)
        bindViews()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.destroy()
        searchUserPresenter.destroy()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.chat_back -> onBackPressed()
            R.id.new_chat_select_contact -> selectContact()
            R.id.btnSend -> sendNewMessage()
            R.id.btn_done -> {
                if (isAddMember){
                    handleDonePress()
                }else{
                    sendNewMessage()
                }
            }
        }
    }

    private fun handleDonePress() {
        val returnIntent = Intent()
        returnIntent.putParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY, selectedUsers)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
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
        recyclerView.adapter = adapter

        registerEvent(RxTextView
                .afterTextChangeEvents(edMessage)
                .subscribe { textViewAfterTextChangeEvent -> checkReadySend() })
        registerEvent(RxTextView.textChangeEvents(edtTo!!)
                .subscribe({ textViewTextChangeEvent ->
                    val text = textViewTextChangeEvent.text().toString()
                    searchUserPresenter.searchUsers(text)
                }, { throwable -> throwable.printStackTrace() }))
        checkReadySend()

        presenter.create()
        searchUserPresenter.create()
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
            if (selectedUsers.size == 0) {
                edtTo.hint = "Username"
            }
        }
        checkReadySend()
    }

    private fun addContact(contact: User) {
        if (selectedUsers.contains(contact)) return
        selectedUsers.add(contact)
        checkReadySend()
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
                    .into(object : SimpleTarget<Drawable>() {
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
        // Scroll to bottom
        recipientsContainer.visibility = View.VISIBLE
        recipientsContainer.postDelayed({ recipientsContainer.fullScroll(View.FOCUS_DOWN) }, 500)

        // Clear result
        edtTo.setText("")
        edtTo.hint = ""
    }

    private fun bindViews() {
        btnDone = findViewById(R.id.btn_done)
        btBack = findViewById(R.id.chat_back)
        loading = findViewById(R.id.spin_kit)

        btBack!!.setOnClickListener(this)
        btnSend.setOnClickListener(this)

        btSelectContact = findViewById(R.id.new_chat_select_contact)
        btSelectContact!!.setOnClickListener(this)

        mLinearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLinearLayoutManager

        noResultsView = findViewById(R.id.no_results)

        val bottomLayout = findViewById<LinearLayout>(R.id.chat_layout_text)
        bottomLayout.visibility = if (isAddMember) View.GONE else View.VISIBLE
        tvTitle.text = if (isAddMember) "ADD MEMBER" else "NEW CHAT"
        btnDone!!.setOnClickListener(this)
        btnDone!!.isEnabled = selectedUsers.size > 0

        edMessage.setOnFocusChangeListener { view, b ->
            if (b) {
                adapter.updateData(ArrayList())
                edtTo.setText("")
            }
        }
    }

    private fun checkReadySend() {
        btnDone?.isEnabled = !selectedUsers.isEmpty()
        btnSend.setTextColor(getSendButtonColor())

    }

    private fun getSendButtonColor() : Int {
      return  if (selectedUsers.isEmpty()) getColorAccent() else{
            resources.getColor(R.color.orange)
        }
    }

    /**
     * get current color accent
     */
    private fun getColorAccent(): Int {
        val typedValue = TypedValue()

        val typeArray = obtainStyledAttributes(typedValue.data, intArrayOf(R.attr.colorAccent))
        val color = typeArray.getColor(0, 0)

        typeArray.recycle()

        return color
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
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

    private fun selectContact() {
        val i = Intent(this, SelectContactActivity::class.java)
        i.putParcelableArrayListExtra("SELECTED_USERS", selectedUsers)
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST)
    }

    private fun sendNewMessage() {
        if (selectedUsers.size <= 0) {
            Toaster.shortToast("Please select recipients.")
            return
        }

        if (!isNetworkAvailable) {
            Toaster.shortToast("Please check network connection.")
            return
        }
        if (selectedUsers.size > 1) {
            // TODO create group then send message
            val toUsers = ArrayList<User>()
            toUsers.addAll(selectedUsers)
            presenter.createGroup(toUsers, edMessage.text.toString())
        } else {
            val toUser = selectedUsers[0]
            val params = NewCreatePVPConversationUseCase.Params()
            params.toUser = toUser
            params.message = edMessage.text.toString()
            presenter.createPVPConversation(params)
        }
    }

    override fun showSearching() {
        loading!!.visibility = View.VISIBLE
    }

    override fun hideSearching() {
        loading!!.visibility = View.GONE
    }

    override fun showNoResults() {
        noResultsView!!.visibility = View.VISIBLE
    }

    override fun hideNoResults() {
        noResultsView!!.visibility = View.INVISIBLE
    }

    override fun displaySearchResult(users: List<User>) {
        adapter.updateData(ArrayList(users))
    }

    override fun moveToChatScreen(conversationId: String) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId)
        startActivity(intent)
        finish()
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }
}
