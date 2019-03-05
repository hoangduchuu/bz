package com.ping.android.presentation.view.activity

import android.Manifest
import android.animation.ValueAnimator
import android.content.*
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.SharedElementCallback
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionManager.beginDelayedTransition
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bzzzchat.cleanarchitecture.BasePresenter
import com.bzzzchat.configuration.GlideApp
import com.bzzzchat.extensions.dp
import com.bzzzchat.extensions.px
import com.bzzzchat.videorecorder.view.*
import com.bzzzchat.videorecorder.view.facerecognition.HiddenCamera
import com.bzzzchat.videorecorder.view.facerecognition.RecognitionCallback
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.storage.FirebaseStorage
import com.ping.android.R
import com.ping.android.data.repository.FaceIdStatusRepository
import com.ping.android.data.repository.TutorialHelper
import com.ping.android.device.hiddenCameraEvent.HiddenCameraListener
import com.ping.android.device.hiddenCameraEvent.PhoneDegreeManager
import com.ping.android.device.impl.ShakeEventManager
import com.ping.android.managers.UserManager
import com.ping.android.model.Conversation
import com.ping.android.model.Message
import com.ping.android.model.User
import com.ping.android.model.enums.Color
import com.ping.android.model.enums.GameType
import com.ping.android.model.enums.VoiceType
import com.ping.android.presentation.presenters.ChatPresenter
import com.ping.android.presentation.view.adapter.ChatMessageAdapter
import com.ping.android.presentation.view.custom.*
import com.ping.android.presentation.view.custom.facerecogloading.FaceRecognizeIndicator
import com.ping.android.presentation.view.custom.media.MediaPickerListener
import com.ping.android.presentation.view.custom.media.MediaPickerView
import com.ping.android.presentation.view.custom.revealable.RevealableViewRecyclerView
import com.ping.android.presentation.view.flexibleitem.messages.GroupImageMessageBaseItem
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem
import com.ping.android.utils.*
import com.ping.android.utils.bus.BusProvider
import com.ping.android.utils.bus.events.GifTapEvent
import com.ping.android.utils.bus.events.GroupImagePositionEvent
import com.ping.android.utils.bus.events.StickerTapEvent
import com.ping.android.utils.configs.Constant
import dagger.android.AndroidInjection
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.view_chat_bottom.*
import kotlinx.android.synthetic.main.view_chat_top.*
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.collections.ArrayList

class ChatActivity : CoreActivity(),
        ChatPresenter.View,
        View.OnClickListener,
        ChatMessageAdapter.ChatMessageListener,
        KeyboardHeightObserver, StickerEmmiter, GiftEmmiter,
        HiddenCameraListener{
    private val TAG = "Ping: " + this.javaClass.simpleName

    //Views UI
    private var backgroundImage: ImageView? = null
    private var recycleChatView: androidx.recyclerview.widget.RecyclerView? = null
    private var mLinearLayoutManager: androidx.recyclerview.widget.LinearLayoutManager? = null
    private var layoutVoice: VoiceRecordView? = null
    private var layoutMediaPicker: MediaPickerView? = null
    private var swipeRefreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? = null
    private var tvChatStatus: TextView? = null
    private var btVoiceCall: ImageButton? = null
    private var btVideoCall: ImageButton? = null
    private var btnMask: Button? = null
    private var btnUnmask: Button? = null
    private var btnDelete: Button? = null
    private lateinit var edMessage: EmojiGifEditText
    private var tvChatName: TextView? = null
    private var tvNewMsgCount: TextView? = null
    private var btnSend: ImageView? = null
    private var faceIdIndicator : FaceRecognizeIndicator ? = null
    private var tutoView : SpinKitView? = null

    /**
     * state of face recognize is enable or not
     */
    private var isEnabledFaceRecognize: Boolean = false

    /**
     * state of Initialized hidden camera or not
     *
     */
    private var isHiddenCameraInitialized = AtomicBoolean(false)

    /**
     * password dialog opening
     */
    private var isPasswordDialogOpening = AtomicBoolean(false)

    /**
     * CompositeDisposable of Timer
     */
    private var disposableTimer = CompositeDisposable()


    @Inject
    lateinit var faceIdStatusRepository: FaceIdStatusRepository


    @Inject
    lateinit var tutorialHelper: TutorialHelper

    private val chatGameMenu: BottomSheetDialog by lazy {
        // Bottom chat menu
        val view = layoutInflater.inflate(R.layout.bottom_sheet_chat_game_menu, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(view)
        dialog.setOnDismissListener { resetButtonState() }
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

    private var textWatcher: TextWatcher? = null

    private var toast: Toast? = null

    private val hiddenCamera: HiddenCamera by lazy {
        HiddenCamera(this, object: RecognitionCallback {
            override fun onRecognitionSuccess() {
                handleOnRecognitionSuccess()
            }
            override fun onRecognizingError() {
                if (!faceIdStatusRepository.faceIdRecognitionStatus.get()) {
                    faceIdIndicator?.showError()
                }
            }
        })
    }
    private val motionDetector: MotionDetector by lazy {
        val callback = object: MotionCallback {
            override fun onExtraInfo(extraInfo: ExtraInfo) {

            }

            override fun onTable() {

            }

            override fun pickedUp() {

            }

        }
        MotionDetector(this, callback)
    }
    private val shakeEventManager: ShakeEventManager by lazy {
        ShakeEventManager(this)
    }

    private val degreeEventManager: PhoneDegreeManager by lazy {
        PhoneDegreeManager(this,this,busProvider,this)
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

    private var isScrollToBottom = false
    private var emojiContainerView: EmojiContainerView? = null

    private var selectedMessage: MessageBaseItem<*>? = null
    private val keyboardHeightProvider: KeyboardHeightProvider by lazy { KeyboardHeightProvider(this) }
    private var currentBottomHeight: Int = SharedPrefsHelper.getInstance().get("keyboardHeight", 250.px)
    @Inject
    lateinit var presenter: ChatPresenter
    @Inject
    lateinit var userManager: UserManager
    @Inject
    lateinit var busProvider: BusProvider

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
        AndroidInjection.inject(this)
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

    override fun onResume() {
        super.onResume()
        shakeEventManager.register()
        degreeEventManager.register()
        if (isEnabledFaceRecognize && isHiddenCameraInitialized.get()) {
            hiddenCamera.onResume()
        }
        keyboardHeightProvider.setKeyboardHeightObserver(this)
        resetButtonState()
        hideAllBottomViews()
        isScreenVisible = true

        setupTutorial()
    }

    override fun onStop() {
        super.onStop()
        if (isTyping) {
            isTyping = false
            updateConversationTyping(false)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isEnabledFaceRecognize && isHiddenCameraInitialized.get()){
            hiddenCamera.onPause()
        }
        shakeEventManager.unregister()
        degreeEventManager.unregister()
        keyboardHeightProvider.setKeyboardHeightObserver(null)
        isTyping = false
        messagesAdapter.pause()
        updateConversationTyping(false)
        isScreenVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isEnabledFaceRecognize && isHiddenCameraInitialized.get()) {
            hiddenCamera.onDestroy()
        }
        keyboardHeightProvider.close()
        messagesAdapter.destroy()
        if (layoutVoice != null) {
            layoutVoice?.release()
        }
    }

    override fun onBackPressed() {
        if (isEditMode) {
            toggleEditMode(!isEditMode)
            return
        }
        if (bottom_view_container.layoutParams.height > 100) {
            hideBottomView()
            return
        }
        super.onBackPressed()
    }

    override fun getPresenter(): BasePresenter? {
        return presenter
    }

    override fun onClick(view: View) {
        val viewId = view.id
        when (viewId) {
            R.id.chat_header_center, R.id.chat_person_name ->{
                onOpenProfile()
                if (!tutorialHelper.isTutorial07ChatNameClicked()){
                    tutorialHelper.markTutorial07ChatNameClicked();
                }
            }
            R.id.chat_back -> onExitChat()
            R.id.chat_camera_btn -> {
                setButtonsState(viewId)
                shouldHideBottomView = true
                KeyboardHelpers.hideSoftInputKeyboard(this)
                hideAllBottomViews()
                onSendCamera()
            }
            R.id.chat_image_btn -> {
                setButtonsState(viewId)
                hideEmojiView()
                hideVoiceRecordView()
                shouldHideBottomView = false
                KeyboardHelpers.hideSoftInputKeyboard(this)
                handleImageButtonPress()
            }
            R.id.chat_game_btn -> {
                setButtonsState(viewId)
                shouldHideBottomView = true
                KeyboardHelpers.hideSoftInputKeyboard(this)
                hideAllBottomViews()
                onGameClicked()
            }
            R.id.btn_send -> if (btnSend!!.isSelected) {
                onSendMessage(originalText)
            } else {
                setButtonsState(viewId)
                hideEmojiView()
                hideMediaPickerView()
                shouldHideBottomView = false
                KeyboardHelpers.hideSoftInputKeyboard(this)
                handleRecordVoice()
            }
            R.id.chat_emoji_btn -> {
                setButtonsState(viewId)
                hideMediaPickerView()
                hideVoiceRecordView()
                shouldHideBottomView = false
                KeyboardHelpers.hideSoftInputKeyboard(this)
                handleEmojiPressed()
            }
            R.id.tgMarkOut -> onChangeTypingMark()
            R.id.chat_voice_call -> onVoiceCall()
            R.id.chat_video_call -> onVideoCall()
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
        hideMediaPickerView()
        hideBottomView()
        KeyboardHelpers.hideSoftInputKeyboard(this)
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
        TransitionManager.beginDelayedTransition(chat_bottom_layout, slide)
        bottom_menu_edit_mode.visibility = if (b) View.VISIBLE else View.GONE
        chat_bottom_input.visibility = if (b) View.GONE else View.VISIBLE
        slide.slideEdge = Gravity.TOP
        beginDelayedTransition(top_layout_container!!, slide)
        top_menu_edit_mode.visibility = if (b) View.VISIBLE else View.GONE
        top_chat_layout.visibility = if (b) View.GONE else View.VISIBLE

        //TransitionManager.beginDelayedTransition(cha/t);
        messagesAdapter.updateEditMode(b)

    }

    private fun hideGameSelection() {
        chatGameMenu.hide()
        resetButtonState()
    }

    private fun setButtonsState(selectedViewId: Int) {
        for (viewId in actionButtons!!) {
            val imageButton = findViewById<ImageButton>(viewId)
            imageButton.isSelected = viewId == selectedViewId
        }
    }

    private fun resetButtonState() {
        for (viewId in actionButtons!!) {
            val imageButton = findViewById<ImageButton>(viewId)
            imageButton.isSelected = false
        }
    }

    private fun hideAllBottomViews() {
        hideEmojiView()
        hideMediaPickerView()
        hideVoiceRecordView()
        hideBottomView()
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
            else -> Intent(this, GamePuzzleActivity::class.java)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            val imagePath = data!!.getStringExtra(VideoRecorderActivity.IMAGE_EXTRA_KEY)
            val videoPath = data.getStringExtra(VideoRecorderActivity.VIDEO_EXTRA_KEY)
            if (!TextUtils.isEmpty(imagePath)) {
                presenter.sendImageMessage(imagePath, imagePath, tgMarkOut.isSelected)
            } else if (!TextUtils.isEmpty(videoPath)) {
                presenter.sendVideoMessage(videoPath)
            }
        }
        if (requestCode == REQUEST_CODE_MEDIA_PICKER && resultCode == RESULT_OK) {
            val items = data!!.getParcelableArrayListExtra<PhotoItem>("data")
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
        tutoView = findViewById(R.id.tutorial_dot_7_name)

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
            resetButtonState()
            hideAllBottomViews()
            false
        }

        swipeRefreshLayout?.setOnRefreshListener { this.loadMoreChats() }

        tvChatName!!.text = conversationName
        tvChatName!!.transitionName = conversationTransionName

        edMessage = findViewById(R.id.chat_message_tv)
        edMessage.listener = object : MediaSelectionListener {
            override fun onMediaSelected(uri: Uri, description: ClipDescription) {
                var fileName = uri.lastPathSegment
                val fileExtension = MimeTypeMap.getSingleton()
                        .getExtensionFromMimeType(description.getMimeType(0))
                if (uri.authority == "com.google.android.inputmethod.latin.inputcontent") {
                    uri.getQueryParameter("fileName")?.let {
                        fileName = it.split("/").last() + "." + fileExtension
                    }
                }
                if (TextUtils.isEmpty(fileName)) {
                    fileName = "${System.currentTimeMillis()}.$fileExtension"
                }
                val file = File(externalCacheDir.absoluteFile, fileName)
                Utils.writeToFileFromContentUri(this@ChatActivity, file, uri)
                presenter.sendSticker(file, tgMarkOut.isSelected)
            }
        }
        btEmoji.setOnClickListener(this)

        setupTutorial()
    }

    private fun initView() {
        faceIdIndicator = findViewById(R.id.pbFaceId)

        val buttonIDs = intArrayOf(R.id.chat_camera_btn, R.id.chat_emoji_btn, R.id.chat_game_btn, R.id.chat_image_btn)
        actionButtons = ArrayList(buttonIDs.size)
        for (buttonId in buttonIDs) {
            actionButtons!!.add(buttonId)
        }
        btnSend!!.isSelected = false
        initTextWatcher()

        mLinearLayoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(this, androidx.recyclerview.widget.LinearLayoutManager.VERTICAL, false) {
            override fun onLayoutCompleted(state: androidx.recyclerview.widget.RecyclerView.State?) {
                super.onLayoutCompleted(state)
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
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItem = mLinearLayoutManager!!.findLastCompletelyVisibleItemPosition()
                isScrollToBottom = lastVisibleItem == mLinearLayoutManager!!.itemCount - 1
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
        degreeEventManager.setupSensor()
        registerEvent(shakeEventManager.getShakeEvent()
                .debounce(700, TimeUnit.MILLISECONDS)
                .subscribe { handleShakePhone() })

        registerEvent(busProvider.events
                .subscribe { o ->
                    if (o is GroupImagePositionEvent) {
                        this.groupImagePositionEvent = o
                    }
                    if (o is StickerTapEvent){
                        sendSticker(o.path)
                    }
                    if (o is GifTapEvent){
                       sendGifs(o.gifUrl)
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
        //withDelay(300) {
        presenter.create()
        presenter.initConversationData(conversationId)
        //}
        container.post {
            keyboardHeightProvider.start()
        }

        isEnabledFaceRecognize = (faceIdStatusRepository.isFaceIdEnabled() && faceIdStatusRepository.isFaceIdTrained())
    }

    /**
     * Send Sticker Message
     */
    private fun sendSticker(path: String?) {
        presenter.sendSticker(path)

    }

    /**
     * send Gif Message
     */

    private fun sendGifs(url : String){
        presenter.sendGifs(url)
    }

    private fun handleShakePhone() {
        // Find visible items
        Log.e("$TAG handleShakePhone")
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

    private var messageBeforeChange = ""

    private fun initTextWatcher() {
        if (textWatcher != null) return
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                messageBeforeChange = charSequence.toString()
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                updateSendButtonStatus(!TextUtils.isEmpty(charSequence.toString()))
                if (!tgMarkOut.isSelected) {
                    return
                }
                val isAppending = count > 0
                val isAppendingEnd = isAppending && (start + count == charSequence.length)
                val isAppendingStart = isAppending && start == 0

                if (isAppending) {
                    val newCharacter = charSequence.subSequence(start, start + count)
                    if (isAppendingEnd) {
                        originalText += newCharacter
                        val newMessageEncoded = userManager.encodeMessage(originalText)
                        selectPosition = newMessageEncoded.length
                    } else if (isAppendingStart) {
                        originalText = "$newCharacter$originalText"
                        val newCharacterEncoded = userManager.encodeMessage(newCharacter.toString())
                        selectPosition = newCharacterEncoded.length - 1
                    } else {
                        val newSubSequence = charSequence.dropLast(charSequence.length - start).toString()
                        val stringBuffer = StringBuffer()
                        val size = originalText.toCharArray().size
                        for (i in 0 until size) {
                            val cha = originalText[i]
                            stringBuffer.append(userManager.encodeMessage(cha.toString()))
                            if (!newSubSequence.contains(stringBuffer.toString(), false)) {
                                originalText = originalText.substring(0, i) + newCharacter + originalText.substring(i, originalText.length)
                                break
                            } else {
                                selectPosition = stringBuffer.length
                            }
                        }
                    }
                } else {
                    // Deleting
                    val newSubSequence = charSequence.dropLast(charSequence.length - start).toString()
                    val stringBuffer = StringBuffer()
                    val size = originalText.toCharArray().size
                    for (i in 0 until size) {
                        val cha = originalText[i]
                        stringBuffer.append(userManager.encodeMessage(cha.toString()))
                        if (!newSubSequence.contains(stringBuffer.toString(), false)) {
                            originalText = originalText.substring(0, i) + originalText.substring(i + 1, originalText.length)
                            break
                        } else {
                            selectPosition = stringBuffer.length
                        }
                    }
                }
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
                if (selectPosition > 0 && selectPosition + 1 <= encodeText!!.length) {
                    edMessage!!.setSelection(selectPosition + 1)
                } else {
                    edMessage!!.setSelection(encodeText!!.length)
                }
                edMessage!!.addTextChangedListener(textWatcher)
            }
        }
        edMessage!!.addTextChangedListener(textWatcher)
        edMessage!!.setOnTouchListener { view, motionEvent ->
            resetButtonState()
            KeyboardHelpers.showKeyboard(this, edMessage)
            shouldHideBottomView = true
            hideEmojiView()
            hideMediaPickerView()
            hideVoiceRecordView()
            false
        }
        edMessage!!.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideVoiceRecordView()
                hideMediaPickerView()
                hideEmojiView()
                KeyboardHelpers.showKeyboard(this, edMessage)
            }
        }
    }

    override fun updateUnreadMessageCount(count: Int) {
        if (count == 0) {
            tvNewMsgCount!!.visibility = View.GONE
        } else {
            tvNewMsgCount!!.visibility = View.VISIBLE
            tvNewMsgCount!!.text = count.toString()
        }
    }

// region router

    private fun onOpenProfile() {
        val intent = Intent(this, ConversationDetailActivity::class.java)
        val extras = Bundle()
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, originalConversation!!.key)
        extras.putInt(ConversationDetailActivity.CONVERSATION_TYPE_KEY, originalConversation!!.conversationType)
        extras.putInt(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation!!.currentColor.code)
        extras.putString(ChatActivity.EXTRA_CONVERSATION_NAME, originalConversation?.conversationName)
        if (originalConversation?.group?.members !=null){
            val users = ArrayList( originalConversation?.group?.members!!)
            intent.putParcelableArrayListExtra(ChatActivity.USERS_IN_GROUP,users)
        }
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
            if (select > 0 && select <= edMessage!!.text!!.length) {
                edMessage!!.setSelection(select)
            } else {
                edMessage!!.setSelection(edMessage!!.text!!.length)

            }
        } catch (ex: IndexOutOfBoundsException) {
            Log.e(ex)
        }

        edMessage!!.addTextChangedListener(textWatcher)
    }

    private fun onSendMessage(text: String) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(applicationContext, "Please input message", Toast.LENGTH_SHORT).show()
            return
        }
        originalText = ""
        edMessage?.text?.clear()
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

    private fun onGameClicked() {
        chatGameMenu.show()
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
        this.initialized = true
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
        recycleChatView?.post {
            messagesAdapter.updateData(messages)
        }
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

    override fun updateMessage(item: MessageBaseItem<*>, headerItem: MessageHeaderItem, higherHeaderItem: MessageHeaderItem?, added: Boolean) {
        recycleChatView?.post {
            messagesAdapter.handleNewMessage(item, headerItem, higherHeaderItem, added)
            if (isScrollToBottom && added) {
                recycleChatView!!.scrollToPosition(messagesAdapter.itemCount - 1)
            }
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
        recycleChatView?.background = null
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
        recycleChatView?.post {
            if (messagesAdapter.itemCount > 0) {
                messagesAdapter.notifyDataSetChanged()
            }
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

    private var shouldHideBottomView: Boolean = true

    // region bottom views

    private fun hideVoiceRecordView() {
        //hideBottomView()
        layoutVoice?.let {
            if (it.visibility == View.VISIBLE) {
                it.visibility = View.GONE
            }
        }
    }

    private fun hideMediaPickerView() {
        layoutMediaPicker?.let {
            if (it.visibility == View.VISIBLE) {
                it.visibility = View.GONE
            }
        }
    }

    private fun hideEmojiView() {
        emojiContainerView?.dismiss()
    }

    private fun setupMediaPickerView() {
        if (layoutMediaPicker == null) {
            findViewById<View>(R.id.stub_media_picker).visibility = View.VISIBLE
            layoutMediaPicker = findViewById(R.id.chat_media_picker)
            layoutMediaPicker?.initProvider(this)
            layoutMediaPicker?.listener = object : MediaPickerListener {
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
            }
        }
        layoutMediaPicker?.layoutParams?.let {
            it.height = this.currentBottomHeight
        }
        layoutMediaPicker?.visibility = View.VISIBLE
        layoutMediaPicker?.refreshData()
    }

    private fun handleEmojiPressed() {

        if (emojiContainerView == null) {
            emojiContainerView = EmojiContainerView(this)
            registerEmitter()
            emojiContainerView?.show(currentBottomHeight, container, edMessage,this,busProvider)
            bottom_view_container.addView(emojiContainerView)
        }
        registerEmitter()
        emojiContainerView?.show(currentBottomHeight, container, edMessage, this, busProvider)
        hideVoiceRecordView()
        hideMediaPickerView()
        shouldHideBottomView = false
        KeyboardHelpers.hideSoftInputKeyboard(this)
        showBottomView()

    }

    private fun openMediaPicker() {
        shouldHideBottomView = false
        KeyboardHelpers.hideSoftInputKeyboard(this)
        hideVoiceRecordView()
        hideEmojiView()
        permissionsChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { isGranted ->
                    if (isGranted) {
                        setupMediaPickerView()
                        showBottomView()
                    }
                }
    }

    private fun handleRecordVoice() {
        val disposable = permissionsChecker.check(Manifest.permission.RECORD_AUDIO)
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
                        layoutVoice?.layoutParams?.let {
                            it.height = this.currentBottomHeight
                        }
                        layoutVoice?.visibility = View.VISIBLE
                        layoutVoice?.prepare()
                        showBottomView()
                    }
                }
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        Log.d("Keyboard height $height, ${height.dp}")
        if (height > 0) {
            this.currentBottomHeight = height
            SharedPrefsHelper.getInstance().save("keyboardHeight", height)
            showBottomView()
        }
        if (shouldHideBottomView) {
            if (height == 0) {
                hideBottomView()
            }
        } else {
            shouldHideBottomView = true
        }
    }

    private fun showBottomView() {
        bottom_view_container.post {
            val params = bottom_view_container.layoutParams
            val animator = ValueAnimator.ofInt(params.height, currentBottomHeight)
            animator.duration = 300
            animator.addUpdateListener {
                params.height = it.animatedValue as Int
                bottom_view_container.layoutParams = params
            }
            animator.start()
        }
    }

    private fun hideBottomView() {
        //beginDelayedTransition(bottom_view_container)
        bottom_view_container.post {
            val params = bottom_view_container.layoutParams
            val animator = ValueAnimator.ofInt(params.height, 1)
            animator.duration = 300
            animator.addUpdateListener {
                params.height = it.animatedValue as Int
                bottom_view_container.layoutParams = params
            }
            animator.start()
            shouldHideBottomView = true
        }
    }

    /**
     * callback while sticker selected
     */
    override fun onStickerSelected(stickerPath: String, position: Int) {
        // send sticker
        presenter.sendSticker(stickerPath)

    }

    /**
     * callback while Gift  selected
     */
    override fun onGiftSelected(gifId: String) {
        // send gif
        presenter.sendImageMessage(gifId, gifId, false)

    }

    /**
     * register emiter to hold data from  EmojiContainerView.kt
     */
    private fun registerEmitter() {
        emojiContainerView?.setGifsEmmiter(this)
        emojiContainerView?.setStickerEmmiter(this)




//        presenter.sendImageMessage()
    }

    // region face id
    /**
     * #FACEID
     * callback Based on Phone'Degrees to start or stop camera
     */
    override fun handleStartCamera() {
        /**
         * when user open chat screen, we check user have setup and enable FACE ID or not
         */
        if (isEnabledFaceRecognize && !faceIdStatusRepository.faceIdRecognitionStatus.get() && !isPasswordDialogOpening.get()) {
            if (!isHiddenCameraInitialized.get()) {
                hiddenCamera.initWithActivity(this)
                isHiddenCameraInitialized.set(true)
            }else{
                hiddenCamera.onResume()
            }
            faceIdIndicator?.showLoading()
            faceIdIndicator?.visibility =View.VISIBLE

            disposableTimer.clear()
            startCounterFaceIdProcess()
        }
    }

    /**
     * #FACEID
     * callback Based on Phone'Degrees to start or stop camera
     */
    override fun handleStopCamera() {
        //hide indicator
        //stop timer
        //stop camera
        faceIdIndicator?.visibility = View.GONE
        disposableTimer.clear()

        if (isHiddenCameraInitialized.get()){
            hiddenCamera.onPause()
        }
        if(faceIdStatusRepository.faceIdRecognitionStatus.getAndSet(false)){
            messagesAdapter.userRecognized(false)
        }
    }

    override fun isAnyVisibleMessageMasked():Boolean {
        val firstVisible = mLinearLayoutManager!!.findFirstVisibleItemPosition()
        val lastVisible = mLinearLayoutManager!!.findLastVisibleItemPosition()
        val visibleMessages = messagesAdapter.findMessages(firstVisible, lastVisible)
        for (message in visibleMessages) {
            if (message.maskable && message.isMask) {
                return true
            }
        }
        return false
    }

    /**
     * #FACEID
     * handleOnRecognitionSuccess
     */
    private fun handleOnRecognitionSuccess(){
        hiddenCamera.onPause()
        presenter.userRecognized()
        // FIXME: for now, update directly in adapter
        messagesAdapter.userRecognized(true)
        faceIdStatusRepository.faceIdRecognitionStatus.set(true)
        faceIdIndicator?.showSuccess()

    }

    /**
     * while start process faceID,
     */
    private fun startCounterFaceIdProcess(){
        disposableTimer.add(RxUtils.countDown(15)
                .doOnNext { t->BzzzLog.d(t.toString()) }
                .doOnComplete { handleFaceIdRecognitionTimeout() }
                .subscribe())

        registerEvent(disposableTimer)
    }

    /**
     * if after ten seconds process faceID not success, we open popup to authenticate with user and password
     */
    private fun handleFaceIdRecognitionTimeout() {
        disposableTimer.clear()
        if (!faceIdStatusRepository.faceIdRecognitionStatus.get()){
            // open popup over here
            showFaceDetectFailed()
            val handler = Handler()
            handler.postDelayed({
                faceIdIndicator?.visibility = View.GONE
            }, 200)
        }
    }

    override fun showFaceDetectFailed() {
        isPasswordDialogOpening.set(true)

        val promptsView = LayoutInflater.from(this).inflate(R.layout.dialog_faceid_falied, null)
        val btnTry = promptsView.findViewById<AppCompatButton>(R.id.btTryAgain)
        val btTurnOfFaceID = promptsView.findViewById<AppCompatButton>(R.id.btTurnOffFaceId)

        val dialog = AlertDialog.Builder(Objects.requireNonNull(this))
                .setTitle("")
                .setView(promptsView)
                .setOnCancelListener {
                    isPasswordDialogOpening.set(false)
                }
                .create()
         dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()

        /**
         * require password before turn of Face ID
         */
        btTurnOfFaceID.setOnClickListener {
            showRequirePasswordForm()
            dialog.dismiss()
        }
        btnTry.setOnClickListener {
            isPasswordDialogOpening.set(false)
            dialog.dismiss()
            faceIdIndicator?.visibility = View.VISIBLE
            handleStartCamera()
        }
    }

    override fun displayConfirmPasswordError(mesage: String ) {
        isPasswordDialogOpening.set(true)
        showDialogMessage(getString(R.string.chat_act_disable_faceid_failed),mesage)
    }

    private fun showDialogMessage(title: String, message: String) {
        if (this.dialogBuilder == null) {
            dialogBuilder = AlertDialog.Builder(this)
        }
        dialogBuilder.setTitle(title).setMessage(message)
                .setPositiveButton(getString(R.string.core_ok)) {
                    dialog, which -> dialog.dismiss()
                    isPasswordDialogOpening.set(false)

                }
                .setOnCancelListener{
                    isPasswordDialogOpening.set(false)

                }
        if (this.dialog == null) {
            dialog = dialogBuilder.create()
        }
        Handler().postDelayed({
            dialog.show()
            this.makePositiveButtonCenter()
        }, 200)
    }

    override fun showRequirePasswordForm() {
        isPasswordDialogOpening.set(true)
        disposableTimer.clear()
        runOnUiThread {
            val promptsView = LayoutInflater.from(this).inflate(R.layout.dialog_check_password, null)
            val password = promptsView.findViewById<EditText>(R.id.tvPassword)
            val dialog = AlertDialog.Builder(Objects.requireNonNull(this))
                    .setTitle("")
                    .setView(promptsView)
                    .setPositiveButton(getString(R.string.profile_send)) { dialog12, which -> presenter.checkPassword(password.text.toString().trim { it <= ' ' }) }
                    .setNegativeButton(getString(R.string.profile_cancel)) { dialog1, which -> dialog1.dismiss() }
                    .create()
            Objects.requireNonNull<Window>(dialog.window).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            dialog.show()
        }
    }

    // endregion

    override fun showTimeoutNotification() {
        timeOutNotification.visibility = View.VISIBLE
    }
    // endregion

    private var initialized = false

    override fun connectivityChanged(availableNow: Boolean) {
        if (availableNow && initialized) {
            recycleChatView?.post {
                messagesAdapter.lastMessage?.let {
                    presenter.getUpdatedMessages(it.timestamp)
                }
            }
        }
    }

    private fun setupTutorial() {
        if (!tutorialHelper.isTutorial07ChatNameClicked()) {
            tutoView?.visibility = View.VISIBLE
        } else {
            tutoView?.visibility = View.GONE

        }
    }

    companion object {
        const val CAMERA_REQUEST_CODE = 12345
        const val REQUEST_CODE_MEDIA_PICKER = 1111
        const val EXTRA_CONVERSATION_NAME = "EXTRA_CONVERSATION_NAME"
        const val EXTRA_CONVERSATION_TRANSITION_NAME = "EXTRA_CONVERSATION_TRANSITION_NAME"
        const val EXTRA_CONVERSATION_COLOR = "EXTRA_CONVERSATION_COLOR"

        const val CONVERSATION_ID = "CONVERSATION_ID"

        const val USERS_IN_GROUP = "USERS_IN_GROUP"
    }
}
