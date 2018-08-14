package com.ping.android.presentation.view.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle

import androidx.recyclerview.widget.LinearLayoutManager

import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout

import com.google.android.material.chip.Chip
import com.jakewharton.rxbinding2.widget.RxTextView
import com.ping.android.R
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase
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
        presenter.create()
        searchUserPresenter.create()
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
            R.id.btn_done -> handleDonePress()
        }
    }

    private fun handleDonePress() {
        val returnIntent = Intent()
        returnIntent.putParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY, selectedUsers)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }

    private fun init() {
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
                    if (TextUtils.isEmpty(text)) {
                        adapter!!.updateData(ArrayList())
                    } else {
                        searchUserPresenter.searchUsers(text)
                    }
                }, { throwable -> throwable.printStackTrace() }))
        checkReadySend()
    }

    private fun removeContact(contact: User) {
        val index = selectedUsers.indexOfFirst {
            it.key === contact.key
        }
        if (index >= 0) {
            chipGroup.removeViewAt(index)
            selectedUsers.removeAt(index)
            adapter.notifyDataSetChanged()
        }
    }

    private fun addContact(contact: User) {
        if (selectedUsers.contains(contact)) return
        selectedUsers.add(contact)
        val chip = Chip(this)
        chip.text = contact.displayName
        //chip.setChipIcon();
        chip.isCloseIconVisible = true
        chip.isCheckable = false
        chip.setOnCloseIconClickListener { v ->
            removeContact(contact)
        }
        chipGroup.addView(chip)
    }

    private fun updateChips() {
        val builder = StringBuilder()
        for (user in selectedUsers) {
            builder.append(user.displayName)
            builder.append(",")
        }
        //edtTo.updateText(builder.toString());
        btnDone!!.isEnabled = selectedUsers.size > 0
    }

    private fun bindViews() {
        btnDone = findViewById(R.id.btn_done)
        btBack = findViewById(R.id.chat_back)
        loading = findViewById(R.id.spin_kit)

        btBack!!.setOnClickListener(this)

        btSelectContact = findViewById(R.id.new_chat_select_contact)
        btSelectContact!!.setOnClickListener(this)

        mLinearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = mLinearLayoutManager

        noResultsView = findViewById(R.id.no_results)

        val bottomLayout = findViewById<LinearLayout>(R.id.chat_layout_text)
        bottomLayout.visibility = if (isAddMember) View.GONE else View.VISIBLE
        tvTitle.text = if (isAddMember) "ADD MEMBER" else "NEW CHAT"
        btnDone!!.visibility = if (isAddMember) View.VISIBLE else View.GONE
        btnDone!!.setOnClickListener(this)
        btnDone!!.isEnabled = selectedUsers.size > 0

        edMessage.setOnFocusChangeListener { view, b ->
            if (b) {
                adapter.updateData(ArrayList())
                edtTo.setText("")
            }
        }
        //        registerEvent(edtTo.chipEventObservable()
        //                .observeOn(new UIThread().getScheduler())
        //                .subscribe(chipEvent -> {
        //                    switch (chipEvent.type) {
        //                        case SEARCH:
        //                            if (TextUtils.isEmpty(chipEvent.text)) {
        //                                adapter.updateData(new ArrayList<>());
        //                            } else {
        //                                searchUserPresenter.searchUsers(chipEvent.text);
        //                            }
        //                            break;
        //                        case DELETE:
        //                            for (User user : selectedUsers) {
        //                                if (user.getDisplayName().equals(chipEvent.text)) {
        //                                    selectedUsers.remove(user);
        //                                    adapter.setSelectPingIDs(getSelectedPingId());
        //                                    btnDone.setEnabled(selectedUsers.size() > 0);
        //                                    break;
        //                                }
        //                            }
        //                            break;
        //                    }
        //                }));
    }

    private fun checkReadySend() {
        btnSend.isEnabled = !(TextUtils.isEmpty(edMessage.text.toString().trim { it <= ' ' }) || selectedUsers.size <= 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                selectedUsers = data!!.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY)
                updateChips()
                adapter!!.setSelectPingIDs(selectedPingId)
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
            return
        }

        val text = edMessage.text.toString()
        if (TextUtils.isEmpty(text)) {
            Toaster.shortToast("Please enter message.")
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
            val params = CreatePVPConversationUseCase.Params()
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
        //recycleChatView.setVisibility(View.INVISIBLE);
        noResultsView!!.visibility = View.VISIBLE
        //        recycleChatView.post(()-> recycleChatView.setVisibility(View.INVISIBLE));
        //        noResultsView.post(() -> noResultsView.setVisibility(View.VISIBLE));
    }

    override fun hideNoResults() {
        //recycleChatView.setVisibility(View.VISIBLE);
        noResultsView!!.visibility = View.INVISIBLE
        //        recycleChatView.post(()->recycleChatView.setVisibility(View.VISIBLE));
        //        noResultsView.post(() -> noResultsView.setVisibility(View.INVISIBLE));
    }

    override fun displaySearchResult(users: List<User>) {
        adapter!!.updateData(ArrayList(users))
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
