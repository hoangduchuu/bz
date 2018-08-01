package com.ping.android.presentation.view.activity

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetDialog
import android.support.transition.Slide
import android.support.transition.TransitionManager
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.app.SharedElementCallback
import android.support.v4.content.ContextCompat
import android.support.v4.util.Pair
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.cleanarchitecture.scopes.HasComponent
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.videorecorder.view.PhotoItem
import com.bzzzchat.videorecorder.view.VideoPlayerActivity
import com.bzzzchat.videorecorder.view.VideoRecorderActivity
import com.bzzzchat.videorecorder.view.withDelay
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.dagger.loggedin.chat.ChatComponent
import com.ping.android.dagger.loggedin.chat.ChatModule
import com.ping.android.device.impl.ShakeEventManager
import com.ping.android.managers.UserManager
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import com.ping.android.model.User
import com.ping.android.model.enums.Color
import com.ping.android.model.enums.GameType
import com.ping.android.model.enums.MessageType
import com.ping.android.model.enums.VoiceType
import com.ping.android.presentation.presenters.ChatPresenter
import com.ping.android.presentation.view.adapter.ChatMessageAdapter
import com.ping.android.presentation.view.custom.VoiceRecordView
import com.ping.android.presentation.view.custom.VoiceRecordViewListener
import com.ping.android.presentation.view.custom.media.MediaPickerListener
import com.ping.android.presentation.view.custom.media.MediaPickerPopup
import com.ping.android.presentation.view.custom.revealable.RevealableViewRecyclerView
import com.ping.android.presentation.view.flexibleitem.messages.GroupImageMessageBaseItem
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem
import com.ping.android.utils.*
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GroupImagePositionEvent
import com.ping.android.utils.configs.Constant
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiPopup
import kotlinx.android.synthetic.main.view_chat_bottom.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class ChatActivity : CoreActivity(), ChatPresenter.View, HasComponent<ChatComponent>, View.OnClickListener, ChatMessageAdapter.ChatMessageListener {

    private val TAG = "Ping: " + this.javaClass.simpleName

    //Views UI
    private var backgroundImage: ImageView? = null
    private var recycleChatView: RecyclerView? = null
    private var mLinearLayoutManager: LinearLayoutManager? = null
    private var topLayoutContainer: ViewGroup? = null
    private var topLayoutChat: ViewGroup? = null
    private var topLayoutEditMode: ViewGroup? = null
    private var bottomLayoutContainer: ViewGroup? = null
    private var bottomLayoutChat: ViewGroup? = null
    private var bottomMenuEditMode: ViewGroup? = null
    private var layoutVoice: VoiceRecordView? = null
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var tvChatStatus: TextView? = null
    private var btVoiceCall: ImageButton? = null
    private var btVideoCall: ImageButton? = null
    private var btnMask: Button? = null
    private var btnUnmask: Button? = null
    private var btnDelete: Button? = null
    private var edMessage: EmojiEditText? = null
    private var tvChatName: TextView? = null
    private var tvNewMsgCount: TextView? = null
    private var btnSend: ImageView? = null

    private val chatGameMenu: BottomSheetDialog by lazy {
        // Bottom chat menu
        val view = layoutInflater.inflate(R.layout.bottom_sheet_chat_game_menu, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.setOnDismissListener { dialogInterface -> setButtonsState(0) }
        view.findViewById<View>(R.id.puzzle_game).setOnClickListener(this)
        view.findViewById<View>(R.id.memory_game).setOnClickListener(this)
        view.findViewById<View>(R.id.tic_tac_toe_game).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_cancel_game_selection).setOnClickListener(this)
        dialog
    }

    private var copyContainer: LinearLayout? = null
    private val messageActions: BottomSheetDialog by lazy {
        // Bottom message action
        val messageActionsView = layoutInflater.inflate(R.layout.bottom_sheet_message_actions, null)
        copyContainer = messageActionsView.findViewById(R.id.btn_copy)
        copyContainer?.setOnClickListener(this)
        messageActionsView.findViewById<View>(R.id.btn_delete).setOnClickListener(this)
        messageActionsView.findViewById<View>(R.id.btn_edit).setOnClickListener(this)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(messageActionsView)
        dialog.setOnDismissListener { hideSelectedMessage() }
        dialog
    }

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val listener: SharedPreferences.OnSharedPreferenceChangeListener by lazy {
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == Constant.PREFS_KEY_MESSAGE_COUNT) {
                val messageCount = prefs.getInt(key, 0)
                updateMessageCount(messageCount)
            }
        }
    }
    private var textWatcher: TextWatcher? = null

    private val shakeEventManager: ShakeEventManager by lazy {
        ShakeEventManager(this)
    }
    private val permissionsChecker: PermissionsChecker by lazy {
        PermissionsChecker.from(this)
    }

    var conversationId: String? = null
        private set
    private var originalConversation: Conversation? = null
    private lateinit var messagesAdapter: ChatMessageAdapter

    private var isTyping = false
    private val isSettingStackFromEnd = AtomicBoolean(false)
    private var originalText = ""
    private var selectPosition = 0

    private var isScrollToTop = false
    private var emojiPopup: EmojiPopup? = null
    private var mediaPickerPopup: MediaPickerPopup? = null

    private var selectedMessage: MessageBaseItem<*>? = null
    private val badgeHelper: BadgeHelper by lazy {
        BadgeHelper(this)
    }

    @Inject
    lateinit var presenter: ChatPresenter
    @Inject
    lateinit var userManager: UserManager
    @Inject
    lateinit var busProvider: BusProvider

    override val component: ChatComponent
        get() = loggedInComponent.provideChatComponent(ChatModule(this))
    private var isScreenVisible = false
    private var actionButtons: MutableList<Int>? = null
    private var isEditMode = false
    private var groupImagePositionEvent: GroupImagePositionEvent? = null
    private var groupImageViewHolder: GroupImageMessageBaseItem.ViewHolder? = null
    private var selectedGame: GameType = GameType.UNKNOWN

    private val lastMessage: Message?
        get() {
            if (messagesAdapter.itemCount < 1) {
                return null
            }

            val item = messagesAdapter.getItem(0)
            if (item is MessageBaseItem<*>) {
                return item.message
            } else if (item is MessageHeaderItem) {
                return item.childItems[0].message
            }
            return null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        component.inject(this)
        val bundle = intent.extras
        if (bundle != null) {
            //originalConversation = bundle.getParcelable("CONVERSATION");
            var currentColor = Color.DEFAULT
            if (bundle.containsKey(EXTRA_CONVERSATION_COLOR)) {
                val color = bundle.getInt(EXTRA_CONVERSATION_COLOR)
                currentColor = Color.from(color)
                ThemeUtils.onActivityCreateSetTheme(this, currentColor)
            }
            presenter.initThemeColor(currentColor)
        }
        setContentView(R.layout.activity_chat)

        conversationId = intent.getStringExtra(ChatActivity.CONVERSATION_ID)
        bindViews()
        initView()
        init()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID)
        // FIXME
        if (this.conversationId != conversationID) {
            presenter.initConversationData(conversationID)
        }
    }

    override fun onStart() {
        super.onStart()
        val messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0)
        updateMessageCount(messageCount)
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onResume() {
        super.onResume()
        badgeHelper.read(conversationId)
        setButtonsState(0)
        isScreenVisible = true
    }

    override fun onStop() {
        super.onStop()
        if (isTyping) {
            isTyping = false
            updateConversationTyping(false)
        }

        prefs.unregisterOnSharedPreferenceChangeListener(listener)
    }

    override fun onPause() {
        super.onPause()
        isTyping = false
        messagesAdapter.pause()
        updateConversationTyping(false)
        isScreenVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this.shakeEventManager != null) {
            this.shakeEventManager.unregister()
        }
        messagesAdapter.destroy()
        if (layoutVoice != null) {
            layoutVoice!!.release()
        }
    }

    override fun getPresenter(): BasePresenter? {
        return presenter
    }

    override fun onClick(view: View) {
        val viewId = view.id
        setButtonsState(viewId)
        when (viewId) {
            R.id.chat_header_center, R.id.chat_person_name -> onOpenProfile()
            R.id.chat_back -> onExitChat()
            R.id.chat_camera_btn -> onSendCamera()
            R.id.chat_image_btn -> handleImageButtonPress()
            R.id.chat_game_btn -> onGameClicked()
            R.id.btn_send -> if (btnSend!!.isSelected) {
                onSentMessage(originalText)
            } else {
                handleRecordVoice()
            }
            R.id.tgMarkOut -> onChangeTypingMark()
            R.id.chat_voice_call -> onVoiceCall()
            R.id.chat_video_call -> onVideoCall()
            R.id.chat_emoji_btn -> handleEmojiPressed()
            R.id.btn_copy -> onCopySelectedMessageText()
            R.id.btn_delete -> onDeleteSelectedMessage()
            R.id.btn_delete_messages -> onDeleteSelectedMessages()
            R.id.btn_edit -> handleEditMessages()
            R.id.btn_cancel_edit -> toggleEditMode(false)
            R.id.chat_mask -> onUpdateMaskMessage(true)
            R.id.chat_unmask -> onUpdateMaskMessage(false)
            R.id.puzzle_game -> onSendGame(GameType.PUZZLE)
            R.id.memory_game -> onSendGame(GameType.MEMORY)
            R.id.tic_tac_toe_game -> onSendGame(GameType.TIC_TAC_TOE)
            R.id.btn_cancel_game_selection -> hideGameSelection()
            R.id.btn_grid -> handleGridMediaPickerPress()
        }
    }

    private fun handleGridMediaPickerPress() {
        // TODO start grid media picker
        KeyboardHelpers.hideSoftInputKeyboard(this)
        mediaPickerPopup?.toggle()
        withDelay(300) {
            val intent = Intent(this, GridMediaPickerActivity::class.java)
            intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation!!.currentColor.code)
            intent.putExtra(GridMediaPickerActivity.MAX_SELECTED_ITEM_COUNT, 10)
            startActivityForResult(intent, REQUEST_CODE_MEDIA_PICKER)
        }
    }

    private fun handleEditMessages() {
        toggleEditMode(true)
        // there is 1 message that is the current selected one
        updateMessageSelection(1)
        messageActions.dismiss()
    }

    private fun toggleEditMode(b: Boolean) {
        isEditMode = b

        val slide = Slide(Gravity.BOTTOM)
        slide.duration = 300
        TransitionManager.beginDelayedTransition(bottomLayoutContainer!!, slide)
        bottomMenuEditMode!!.visibility = if (b) View.VISIBLE else View.GONE
        bottomLayoutChat!!.visibility = if (b) View.GONE else View.VISIBLE
        slide.slideEdge = Gravity.TOP
        TransitionManager.beginDelayedTransition(topLayoutContainer!!, slide)
        topLayoutEditMode!!.visibility = if (b) View.VISIBLE else View.GONE
        topLayoutChat!!.visibility = if (b) View.GONE else View.VISIBLE

        //TransitionManager.beginDelayedTransition(cha/t);
        messagesAdapter.updateEditMode(b)
    }

    private fun hideGameSelection() {
        chatGameMenu.hide()
        setButtonsState(0)
    }

    private fun setButtonsState(selectedViewId: Int) {
        if (emojiPopup != null && emojiPopup!!.isShowing && selectedViewId != R.id.chat_emoji_btn) {
            emojiPopup!!.dismiss()
        }
        if (selectedViewId != R.id.chat_image_btn) {
            hideMediaPickerView()
        }
        for (viewId in actionButtons!!) {
            val imageButton = findViewById<ImageButton>(viewId)
            imageButton.isSelected = viewId == selectedViewId
        }
    }

    override fun onLongPress(message: MessageBaseItem<*>, allowCopy: Boolean) {
        KeyboardHelpers.hideSoftInputKeyboard(this)
        selectedMessage = message
        messageActions.show()
        copyContainer?.visibility = if (allowCopy) View.VISIBLE else View.GONE
    }

    override fun openImage(message: Message, isPuzzled: Boolean, vararg sharedElements: Pair<View, String>) {
        GroupImageGalleryActivity.start(this, originalConversation!!.key, arrayListOf(message), 0, false, sharedElements[0])
    }

    override fun openGameMessage(message: Message) {
        val intent = when {
            message.gameType == GameType.MEMORY.ordinal -> Intent(this, GameMemoryActivity::class.java)
            message.gameType == GameType.TIC_TAC_TOE.ordinal -> Intent(this, GameTicTacToeActivity::class.java)
            else -> Intent(this, GameActivity::class.java)
        }
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationId)
        intent.putExtra("CONVERSATION", originalConversation)
        intent.putExtra("MESSAGE_ID", message.key)
        intent.putExtra("MESSAGE", message)
        intent.putExtra("IMAGE_URL", message.mediaUrl)
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation!!.currentColor.code)
        startActivity(intent)
    }

    override fun updateMessageSelection(size: Int) {
        btnDelete!!.isEnabled = size != 0
        btnMask!!.isEnabled = size != 0
        btnUnmask!!.isEnabled = size != 0
    }

    override fun updateLastConversationMessage(lastMessage: Message) {
        presenter.updateConversationLastMessage(lastMessage)
    }

    override fun openVideo(videoUrl: String) {
        val intent = Intent(this, VideoPlayerActivity::class.java)
        intent.putExtra(VideoPlayerActivity.VIDEO_PATH_EXTRA_KEY, videoUrl)
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation!!.currentColor.code)
        startActivity(intent)
    }

    override fun onCall(isVideo: Boolean) {
        if (isVideo) {
            presenter.handleVideoCallPress()
        } else {
            presenter.handleVoiceCallPress()
        }
    }

    override fun onGroupImageItemPress(viewHolder: GroupImageMessageBaseItem.ViewHolder, data: List<Message>, position: Int, sharedElements: Array<Pair<View, String>>) {
        this.groupImageViewHolder = viewHolder
        GroupImageGalleryActivity.start(this, originalConversation!!.key, data, position, false, sharedElements[0])
    }

    override fun updateChildMessageMask(message: Message, maskStatus: Boolean) {
        val childsToUpdate = listOf(message)
        presenter.updateMaskChildMessages(childsToUpdate, maskStatus)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imagePath = data.getStringExtra(VideoRecorderActivity.IMAGE_EXTRA_KEY)
            val videoPath = data.getStringExtra(VideoRecorderActivity.VIDEO_EXTRA_KEY)
            if (!TextUtils.isEmpty(imagePath)) {
                presenter.sendImageMessage(imagePath, imagePath, tgMarkOut.isSelected)
            } else if (!TextUtils.isEmpty(videoPath)) {
                presenter.sendVideoMessage(videoPath)
            }
        }
        if (requestCode == REQUEST_CODE_MEDIA_PICKER && resultCode == RESULT_OK) {
            val items = data.getParcelableArrayListExtra<PhotoItem>("data")
            val size = items.size
            if (size > 0) {
                if (selectedGame == GameType.UNKNOWN) {
                    if (size == 1) {
                        presenter.sendImageMessage(items[0].imagePath, items[0].thumbnailPath, tgMarkOut.isSelected)
                    } else {
                        presenter.sendImagesMessage(items, tgMarkOut.isSelected)
                    }
                } else {
                    presenter.sendGameMessages(items, selectedGame, tgMarkOut.isSelected)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsChecker.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 111) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                handleRecordVoice()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun bindViews() {
        val conversationName = intent.getStringExtra(EXTRA_CONVERSATION_NAME)
        val conversationTransionName = intent.getStringExtra(EXTRA_CONVERSATION_TRANSITION_NAME)

        val btBack = findViewById<ImageView>(R.id.chat_back)
        tvChatName = findViewById(R.id.chat_person_name)
        tvChatStatus = findViewById(R.id.chat_person_status)
        recycleChatView = findViewById(R.id.chat_list_view)
        backgroundImage = findViewById(R.id.backgroundImage)
        swipeRefreshLayout = findViewById(R.id.swiperefresh)
        btnSend = findViewById(R.id.btn_send)
        btVoiceCall = findViewById(R.id.chat_voice_call)
        btVideoCall = findViewById(R.id.chat_video_call)
        btnMask = findViewById(R.id.chat_mask)
        btnUnmask = findViewById(R.id.chat_unmask)
        btnDelete = findViewById(R.id.btn_delete_messages)
        //        btEdit = findViewById(R.id.chat_video_call);
        //        btCancelEdit = findViewById(R.id.chat_cancel_edit);
        //layoutText = findViewById(R.id.chat_layout_text);
        topLayoutContainer = findViewById(R.id.top_layout_container)
        topLayoutChat = findViewById(R.id.top_chat_layout)
        topLayoutEditMode = findViewById(R.id.top_menu_edit_mode)
        bottomLayoutContainer = findViewById(R.id.bottom_layout_container)
        bottomLayoutChat = findViewById(R.id.bottom_layout_chat)
        bottomMenuEditMode = findViewById(R.id.bottom_menu_edit_mode)

        tvNewMsgCount = findViewById(R.id.chat_new_message_count)
        val btEmoji = findViewById<ImageButton>(R.id.chat_emoji_btn)

        btBack.setOnClickListener(this)
        tvChatName!!.setOnClickListener(this)
        btnSend!!.setOnClickListener(this)
        btVoiceCall!!.setOnClickListener(this)
        btVideoCall!!.setOnClickListener(this)
        tgMarkOut.setOnClickListener(this)
        btnMask!!.setOnClickListener(this)
        btnUnmask!!.setOnClickListener(this)
        findViewById<View>(R.id.chat_person_name).setOnClickListener(this)
        findViewById<View>(R.id.chat_image_btn).setOnClickListener(this)
        findViewById<View>(R.id.chat_camera_btn).setOnClickListener(this)
        findViewById<View>(R.id.chat_game_btn).setOnClickListener(this)
        findViewById<View>(R.id.chat_header_center).setOnClickListener(this)
        findViewById<View>(R.id.btn_cancel_edit).setOnClickListener(this)
        findViewById<View>(R.id.btn_delete_messages).setOnClickListener(this)

        (recycleChatView!!.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        recycleChatView!!.setOnTouchListener { view, motionEvent ->
            KeyboardHelpers.hideSoftInputKeyboard(this@ChatActivity)
            setButtonsState(0)
            hideVoiceRecordView()
            false
        }

        swipeRefreshLayout?.setOnRefreshListener { this.loadMoreChats() }

        tvChatName!!.text = conversationName
        tvChatName!!.transitionName = conversationTransionName

        edMessage = findViewById(R.id.chat_message_tv)
        btEmoji.setOnClickListener(this)
    }

    private fun hideVoiceRecordView() {
        if (layoutVoice != null && layoutVoice!!.visibility == View.VISIBLE) {
            layoutVoice!!.visibility = View.GONE
        }
    }

    private fun hideMediaPickerView() {
        mediaPickerPopup?.let {
            if (it.isShowing()) {
                it.toggle()
            }
        }
    }

    private fun setupMediaPickerView() {
        if (mediaPickerPopup == null) {
            mediaPickerPopup = MediaPickerPopup(this, findViewById(R.id.contentRoot), edMessage!!) {
                // Dismiss
                setButtonsState(0)
            }
            mediaPickerPopup?.setListener(object : MediaPickerListener {
                override fun openGridMediaPicker() {
                    handleGridMediaPickerPress()
                }

                override fun sendImage(item: PhotoItem) {
                    if (selectedGame != GameType.UNKNOWN) {
                        presenter.sendGameMessage(item.imagePath, selectedGame, tgMarkOut.isSelected)
                    } else {
                        presenter.sendImageMessage(item.imagePath, item.thumbnailPath, tgMarkOut.isSelected)
                    }
                }
            })
        }
    }

    private fun initView() {
        val buttonIDs = intArrayOf(R.id.chat_camera_btn, R.id.chat_emoji_btn, R.id.chat_game_btn, R.id.chat_image_btn)
        actionButtons = ArrayList(buttonIDs.size)
        for (buttonId in buttonIDs) {
            actionButtons!!.add(buttonId)
        }
        btnSend!!.isSelected = false
        initTextWatcher()

        mLinearLayoutManager = object : LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutCompleted(state: RecyclerView.State?) {
                super.onLayoutCompleted(state)
                Log.e("state $state")
                if (state != null) {
                    if (state.itemCount <= 0) {
                        return
                    }
                }
                if (isSettingStackFromEnd.get()) return

                val contentView = recycleChatView!!.computeVerticalScrollRange()
                val listHeight = recycleChatView!!.measuredHeight
                if (contentView > listHeight) {
                    if (mLinearLayoutManager!!.stackFromEnd) return
                    setLinearStackFromEnd(true)
                } else {
                    if (!mLinearLayoutManager!!.stackFromEnd) return
                    setLinearStackFromEnd(false)
                }
            }
        }
        mLinearLayoutManager?.stackFromEnd = true
        recycleChatView!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val lastVisibleItem = mLinearLayoutManager!!.findLastCompletelyVisibleItemPosition()
                isScrollToTop = lastVisibleItem == mLinearLayoutManager!!.itemCount - 1
            }
        })
        recycleChatView!!.layoutManager = mLinearLayoutManager
        recycleChatView!!.isNestedScrollingEnabled = false
        messagesAdapter = ChatMessageAdapter(GlideApp.with(this))
        messagesAdapter.setMessageListener(this)
        recycleChatView!!.adapter = messagesAdapter
        (recycleChatView as? RevealableViewRecyclerView)?.setCallback(messagesAdapter)
    }

    private fun setLinearStackFromEnd(value: Boolean) {
        isSettingStackFromEnd.set(true)
        // Set stack from end
        recycleChatView!!.post {
            mLinearLayoutManager!!.stackFromEnd = value
            isSettingStackFromEnd.set(false)
        }
    }

    private fun init() {
        registerEvent(shakeEventManager.getShakeEvent()
                .debounce(700, TimeUnit.MILLISECONDS)
                .subscribe { handleShakePhone() })

        registerEvent(busProvider.events
                .subscribe { o ->
                    if (o is GroupImagePositionEvent) {
                        this.groupImagePositionEvent = o
                    }
                })

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String>?, sharedElements: MutableMap<String, View>?) {
                super.onMapSharedElements(names, sharedElements)
                if (groupImagePositionEvent != null) {
                    val position = groupImagePositionEvent!!.position
                    groupImagePositionEvent = null
                    if (groupImageViewHolder != null && groupImageViewHolder!!.itemView != null) {
                        val sharedView = groupImageViewHolder!!.getShareElementForPosition(position)
                        val name = if (names!!.isNotEmpty()) names[0] else null
                        if (name != null && sharedView != null) {
                            sharedElements!![name] = sharedView
                        }
                    }
                }
            }
        })
        // Delay conversation initialize to make smooth UI transition
        //withDelay(700) {
            presenter.create()
            presenter.initConversationData(conversationId)
        //}
    }

    private fun handleShakePhone() {
        // Find visible items
        Log.d("handleShakePhone")
        val firstVisible = mLinearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisible = mLinearLayoutManager!!.findLastVisibleItemPosition()
        val visibleMessages = messagesAdapter.findMessages(firstVisible, lastVisible)
        var isMask = false
        for (message in visibleMessages) {
            if (message.maskable && !message.isMask) {
                isMask = true
                break
            }
        }
        presenter.updateMaskMessages(visibleMessages, lastVisible == messagesAdapter.itemCount - 1, isMask, true)
    }

    private fun updateSendButtonStatus(isEnable: Boolean) {
        btnSend!!.isSelected = isEnable
    }

    private fun loadMoreChats() {
        val lastMessage = lastMessage
        if (lastMessage == null) {
            swipeRefreshLayout!!.isRefreshing = false
            return
        }
        presenter.loadMoreMessage(lastMessage.timestamp)
    }

    private fun initTextWatcher() {
        if (textWatcher != null) return
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                updateSendButtonStatus(!TextUtils.isEmpty(charSequence.toString()))
                if (!tgMarkOut.isSelected) {
                    return
                }

                var newOriginalText = ""
                val displayText = charSequence.toString()
                if (TextUtils.equals(displayText, userManager.encodeMessage(originalText))) {
                    return
                }

                var encodedStart: String? = ""
                var encodedEnd = ""
                var originalStart = ""
                var originalEnd = ""
                var displayStart = ""
                var originalStartIdx = 0
                val displayTextLength = displayText.length
                var startFound = false
                var endFound = false

                for (index in 0 until originalText.length) {
                    encodedStart = encodedStart!! + userManager.encodeMessage(originalText.substring(index, index + 1))!!

                    if (displayTextLength >= encodedStart.length) {
                        displayStart = displayText.substring(0, encodedStart.length)
                    }
                    if (TextUtils.equals(displayStart, encodedStart)) {
                        startFound = true
                        originalStartIdx = index + 1
                        originalStart += originalText.substring(index, index + 1)
                    } else {
                        break
                    }
                }
                encodedStart = userManager.encodeMessage(originalStart)

                if (displayTextLength > encodedStart!!.length) {
                    for (index in originalText.length - 1 downTo originalStartIdx) {
                        encodedEnd = userManager.encodeMessage(originalText.substring(index, index + 1))!! + encodedEnd
                        if (TextUtils.equals(displayText.substring(displayTextLength - encodedEnd.length), encodedEnd)) {
                            endFound = true
                            originalEnd = originalText.substring(index, index + 1) + originalEnd
                        } else {
                            break
                        }
                    }
                }
                newOriginalText = originalStart
                val middleStartIdx = if (startFound) encodedStart.length else 0
                val middleEndIdx = if (endFound)
                    displayText.lastIndexOf(
                            userManager.encodeMessage(originalEnd)!!)
                else
                    displayTextLength
                val originalMiddle = if (middleEndIdx > middleStartIdx) displayText.substring(middleStartIdx, middleEndIdx) else ""
                val encodedMiddle = userManager.encodeMessage(originalMiddle)
                newOriginalText = newOriginalText + originalMiddle + originalEnd


                selectPosition = if (endFound)
                    encodedStart.length + encodedMiddle!!.length
                else
                    encodedStart.length + encodedMiddle!!.length + encodedEnd.length
                originalText = newOriginalText
            }

            override fun afterTextChanged(editable: Editable) {

                //send typing status
                if (!TextUtils.isEmpty(edMessage!!.text) && !isTyping) {
                    isTyping = true
                    updateConversationTyping(isTyping)
                } else if (TextUtils.isEmpty(edMessage!!.text) && isTyping) {
                    isTyping = false
                    updateConversationTyping(isTyping)
                }

                if (!tgMarkOut.isSelected) {
                    originalText = editable.toString()
                    return
                }

                val encodeText = userManager.encodeMessage(originalText)
                if (TextUtils.equals(edMessage!!.text, encodeText)) {
                    return
                }

                edMessage!!.removeTextChangedListener(textWatcher)
                edMessage!!.setText(encodeText)
                if (selectPosition > 0 && selectPosition <= encodeText!!.length) {
                    edMessage!!.setSelection(selectPosition)
                } else {
                    edMessage!!.setSelection(encodeText!!.length)
                }
                edMessage!!.addTextChangedListener(textWatcher)
            }
        }
        edMessage!!.addTextChangedListener(textWatcher)
        edMessage!!.setOnTouchListener { view, motionEvent ->
            setButtonsState(0)
            false
        }
        edMessage!!.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                if (emojiPopup != null) emojiPopup!!.dismiss()
                hideVoiceRecordView()
                hideMediaPickerView()
                KeyboardHelpers.showKeyboard(this, edMessage)
            }
        }
    }

    private fun updateMessageCount(messageCount: Int) {
        if (messageCount == 0) {
            tvNewMsgCount!!.visibility = View.GONE
        } else {
            tvNewMsgCount!!.visibility = View.VISIBLE
            tvNewMsgCount!!.text = messageCount.toString()
        }
    }

    // region router

    private fun onOpenProfile() {
        val intent = Intent(this, ConversationDetailActivity::class.java)
        val extras = Bundle()
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, originalConversation!!.key)
        extras.putInt(ConversationDetailActivity.CONVERSATION_TYPE_KEY, originalConversation!!.conversationType)
        extras.putInt(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation!!.currentColor.code)
        intent.putExtras(extras)
        startActivity(intent)
    }

    private fun onExitChat() {
        finishAfterTransition()
    }

    // endregion

    private fun onCopySelectedMessageText() {
        messageActions.hide()
        if (selectedMessage == null) return
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("message", selectedMessage!!.message.message)
        clipboardManager.primaryClip = clipData

        Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show()
        hideSelectedMessage()
    }

    private fun onDeleteSelectedMessage() {
        messageActions.hide()
        if (selectedMessage == null) return

        val messagesToDelete = ArrayList<Message>()
        messagesToDelete.add(selectedMessage!!.message)
        presenter.deleteMessages(messagesToDelete)
        hideSelectedMessage()
    }

    private fun onDeleteSelectedMessages() {
        val selectedMessages = messagesAdapter.selectedMessages
        presenter.deleteMessages(selectedMessages)
        toggleEditMode(false)
    }

    private fun hideSelectedMessage() {
        //messageActions.hide();
        if (!isEditMode && selectedMessage != null) {
            selectedMessage!!.setEditMode(false)
            selectedMessage!!.setSelected(true)
            messagesAdapter.update(selectedMessage)
        }
        selectedMessage = null
    }

    private fun onUpdateMaskMessage(mask: Boolean) {
        val selectedMessages = messagesAdapter.selectedMessages
        val lastMessage = messagesAdapter.lastMessage
        var isLastMessage = false
        if (lastMessage != null) {
            for (msg in selectedMessages) {
                if (msg.key == lastMessage.key) {
                    isLastMessage = true
                    break
                }
            }
        }
        showLoading()
        if (!isNetworkAvailable) {
            Handler().postDelayed({ this.hideLoading() }, 2000)
        }
        presenter.updateMaskMessages(selectedMessages, isLastMessage, mask, true)
        toggleEditMode(false)
    }

    private fun handleEmojiPressed() {
        if (emojiPopup == null) {
            emojiPopup = EmojiPopup.Builder
                    .fromRootView(findViewById(R.id.contentRoot))
                    .build(edMessage!!)
        }
        hideVoiceRecordView()
        if (!emojiPopup!!.isShowing) {
            emojiPopup!!.toggle()
        }
    }

    private fun onChangeTypingMark() {
        tgMarkOut.isSelected = !tgMarkOut.isSelected
        presenter.updateMaskOutput(tgMarkOut.isSelected)
        edMessage!!.removeTextChangedListener(textWatcher)
        var select = edMessage!!.selectionStart
        if (tgMarkOut.isSelected) {
            updateMaskTintColor(true)
            edMessage!!.setText(userManager.encodeMessage(originalText))
            select = userManager.encodeMessage(originalText.substring(0, select))!!.length
        } else {
            updateMaskTintColor(false)
            //int color = ContextCompat.getColor(this, R.color.gray_color);
            edMessage!!.setText(originalText)
        }
        try {
            if (select > 0 && select <= edMessage!!.text.length) {
                edMessage!!.setSelection(select)
            } else {
                edMessage!!.setSelection(edMessage!!.text.length)

            }
        } catch (ex: IndexOutOfBoundsException) {
            Log.e(ex)
        }

        edMessage!!.addTextChangedListener(textWatcher)
    }

    private fun onSentMessage(text: String) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(applicationContext, "Please input message", Toast.LENGTH_SHORT).show()
            return
        }
        edMessage!!.text = null
        presenter.sendTextMessage(text, tgMarkOut.isSelected)
    }

    private fun onSendCamera() {
        // Should check permission here
        val intent = Intent(this, VideoRecorderActivity::class.java)
        val extras = Bundle()
        val outputPath = externalCacheDir.toString() + File.separator + "conversations" + File.separator + conversationId
        val outputFolder = File(outputPath)
        if (outputFolder.exists()) {
            outputFolder.mkdirs()
        }
        extras.putString(VideoRecorderActivity.OUTPUT_FOLDER_EXTRA_KEY, outputFolder.absolutePath)
        intent.putExtras(extras)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun handleImageButtonPress() {
        selectedGame = GameType.UNKNOWN
        openMediaPicker()
    }

    private fun onSendGame(gameType: GameType) {
        selectedGame = gameType
        chatGameMenu.hide()
        withDelay(500) {
            openMediaPicker()
        }
    }

    private fun openMediaPicker() {
        permissionsChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { isGranted ->
                    if (isGranted) {
                        setupMediaPickerView()
                        if (mediaPickerPopup!!.isShowing()) return@subscribe

                        hideVoiceRecordView()
                        mediaPickerPopup?.toggle()
                    }
                }
    }

    private fun onGameClicked() {
        chatGameMenu.show()
    }

    private fun handleRecordVoice() {
        KeyboardHelpers.hideSoftInputKeyboard(this)
        setButtonsState(0)
        permissionsChecker.check(Manifest.permission.RECORD_AUDIO)
                .subscribe { isGranted ->
                    if (isGranted) {
                        if (layoutVoice == null) {
                            findViewById<View>(R.id.stub_import_voice).visibility = View.VISIBLE
                            layoutVoice = findViewById(R.id.chat_layout_voice)
                            layoutVoice?.setConversationId(originalConversation!!.key)
                            val listener = object : VoiceRecordViewListener {
                                override fun sendVoice(outputFile: String, selectedVoice: VoiceType) {
                                    presenter.sendAudioMessage(outputFile, selectedVoice)
                                }
                            }
                            layoutVoice?.setListener(listener)
                        }
                        layoutVoice?.visibility = View.VISIBLE
                        layoutVoice?.prepare()
                    }
                }
    }

    private fun onVoiceCall() {
        presenter.handleVoiceCallPress()
    }

    private fun onVideoCall() {
        presenter.handleVideoCallPress()
    }

    private fun showTyping(typing: Boolean) {
        if (typing) {
            messagesAdapter.showTyping()
        } else {
            messagesAdapter.hideTypingItem()
        }
        if (isScreenVisible) {
            recycleChatView!!.scrollToPosition(messagesAdapter.itemCount - 1)
        }
    }

    private fun updateConversationTyping(typing: Boolean) {
        presenter.handleUserTypingStatus(typing)
    }

    override fun updateConversation(conv: Conversation) {
        this.shakeEventManager.register()
        this.originalConversation = conv
        if (conv.conversationType == Constant.CONVERSATION_TYPE_GROUP) {
            btVideoCall!!.visibility = View.GONE
            btVoiceCall!!.visibility = View.GONE
        } else {
            btVideoCall!!.visibility = View.VISIBLE
            btVoiceCall!!.visibility = View.VISIBLE
        }
    }

    override fun updateConversationTitle(title: String) {
        tvChatName!!.text = title
    }

    override fun updateMaskSetting(isEnable: Boolean) {
        tgMarkOut.isSelected = isEnable
        updateMaskTintColor(isEnable)
    }

    override fun updateUserStatus(isOnline: Boolean) {
        tvChatStatus!!.text = if (isOnline) "Active" else "Inactive"
    }

    override fun hideUserStatus() {
        tvChatStatus!!.visibility = View.GONE
    }

    override fun removeMessage(headerItem: MessageHeaderItem, data: MessageBaseItem<*>) {
        messagesAdapter.deleteMessage(headerItem, data)
    }

    override fun updateLastMessages(messages: List<MessageHeaderItem>, canLoadMore: Boolean) {
//        if (!mLinearLayoutManager!!.stackFromEnd) {
//            if (messages.size > 20) {
//                mLinearLayoutManager!!.stackFromEnd = true
//            }
//        }
        messagesAdapter.updateData(messages)
        if (!canLoadMore) {
            swipeRefreshLayout!!.isEnabled = false
        }
    }

    override fun appendHistoryMessages(messages: List<MessageHeaderItem>?, canLoadMore: Boolean) {
        messagesAdapter.appendMessages(messages)
        if (!canLoadMore) {
            swipeRefreshLayout!!.isEnabled = false
        }
    }

    override fun updateNickNames(nickNames: Map<String, String>) {
        messagesAdapter.updateNickNames(nickNames)
    }

    override fun toggleTyping(b: Boolean) {
        showTyping(b)
    }

    override fun showErrorUserBlocked(username: String) {
        Toaster.shortToast(String.format(applicationContext.getString(R.string.msg_account_msg_blocked), username, username))
    }

    override fun openCallScreen(currentUser: User, opponentUser: User, isVideo: Boolean) {
        CallActivity.start(this, currentUser, opponentUser, isVideo)
    }

    override fun updateMessage(item: MessageBaseItem<*>, headerItem: MessageHeaderItem, added: Boolean) {
        messagesAdapter.handleNewMessage(item, headerItem, added)
        if (isScrollToTop) {
            recycleChatView!!.scrollToPosition(messagesAdapter.itemCount - 1)
        }
    }

    override fun changeTheme(from: Color) {
        var extras = intent.extras
        if (extras == null) {
            extras = Bundle()
        }
        extras.putInt(EXTRA_CONVERSATION_COLOR, from.code)
        ThemeUtils.changeToTheme(this, extras)
    }

    override fun updateBackground(s: String) {
        if (TextUtils.isEmpty(s) || !s.startsWith("gs://")) {
            backgroundImage!!.setImageDrawable(null)
            return
        }
        if (isDestroyed) return
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(s)
        GlideApp.with(this)
                .asBitmap()
                .load(storageReference)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(backgroundImage!!)
    }

    override fun hideRefreshView() {
        swipeRefreshLayout!!.isRefreshing = false
    }

    override fun refreshMessages() {
        if (messagesAdapter.itemCount > 0) {
            messagesAdapter.notifyDataSetChanged()
        }
    }

    override fun handleProfileImagePress(senderId: String, sharedElements: Array<Pair<View, String>>) {
        val intent = Intent(this, UserDetailActivity::class.java)
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, senderId)
        //intent.putExtra(UserDetailActivity.EXTRA_USER, user)
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second)
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, "")
        intent.putExtra(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation!!.currentColor.code)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, *sharedElements
        )
        startActivity(intent, options.toBundle())
    }

    override fun updateMessageMask(message: Message, maskStatus: Boolean, lastItem: Boolean) {
        val messages = ArrayList<Message>(1)
        messages.add(message)
        presenter.updateMaskMessages(messages, lastItem, maskStatus, false)
    }

    private fun updateMaskTintColor(isEnable: Boolean) {
        if (isEnable) {
            val color = ContextCompat.getColor(this, originalConversation!!.currentColor.color)
            tgMarkOut.backgroundTintList = ColorStateList.valueOf(color)
        } else {
            tgMarkOut.backgroundTintList = null
        }
    }

    override fun showLoading() {
        super<CoreActivity>.showLoading()
    }

    override fun hideLoading() {
        super<CoreActivity>.hideLoading()
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 12345
        const val REQUEST_CODE_MEDIA_PICKER = 1111
        const val EXTRA_CONVERSATION_NAME = "EXTRA_CONVERSATION_NAME"
        const val EXTRA_CONVERSATION_TRANSITION_NAME = "EXTRA_CONVERSATION_TRANSITION_NAME"
        const val EXTRA_CONVERSATION_COLOR = "EXTRA_CONVERSATION_COLOR"

        const val CONVERSATION_ID = "CONVERSATION_ID"
    }
}
