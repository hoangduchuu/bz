package com.ping.android.presentation.view.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.bzzzchat.flexibleadapter.FlexibleAdapter1;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.ping.android.activity.CallActivity;
import com.ping.android.activity.CoreActivity;
import com.ping.android.activity.GameActivity;
import com.ping.android.activity.GameMemoryActivity;
import com.ping.android.activity.GameTicTacToeActivity;
import com.ping.android.activity.PuzzleActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.UserDetailActivity;
import com.ping.android.presentation.view.adapter.ChatAdapter;
import com.ping.android.dagger.loggedin.chat.ChatComponent;
import com.ping.android.dagger.loggedin.chat.ChatModule;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.presentation.view.adapter.ChatMessageAdapter;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.service.BadgesHelper;
import com.ping.android.service.NotificationHelper;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.MessageRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.BadgeHelper;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.KeyboardHelpers;
import com.ping.android.utils.Log;
import com.ping.android.utils.Toaster;
import com.ping.android.view.RecorderVisualizerView;
import com.vanniktech.emoji.EmojiPopup;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class ChatActivity extends CoreActivity implements ChatPresenter.View, HasComponent<ChatComponent>, View.OnClickListener, ChatAdapter.ClickListener, MessageBaseItem.MessageListener {
    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private final int REPEAT_INTERVAL = 40;

    public static final String CONVERSATION_ID = "CONVERSATION_ID";

    //Views UI
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private RelativeLayout layoutVoice, layoutBottomMenu;
    private LinearLayout layoutText, layoutMsgType;
    private ImageView btBack, btLoadMoreChat;
    private Button btSendRecord;
    private Button tbRecord;
    private CheckBox tgMarkOut;
    private TextView tvChatStatus;
    private Button btMask, btUnMask, btDelete, btEdit, btCancelEdit;
    private ImageButton btVoiceCall, btVideoCall, btEmoji;
    private EditText edMessage;
    private TextView tvChatName, tvNewMsgCount;
    private Button btnSend;
    private Button btCancelRecord;
    private BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetDialog chatGameMenu;

    private String conversationID, fromUserID;
    private User fromUser;
    private Conversation originalConversation;
    private ChatAdapter adapter;
    private ChatMessageAdapter messagesAdapter;

    private List<Message> messages;
    private String RECORDING_PATH, currentOutFile;
    private MediaRecorder myAudioRecorder;
    private boolean isRecording = false, isTyping = false, isEditMode = false, isEditAllMode = true;
    private RecorderVisualizerView visualizerView;
    private AtomicBoolean isSettingStackFromEnd = new AtomicBoolean(false);
    private String originalText = "";
    private int selectPosition = 0;
    private boolean visibleStatus;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private TextWatcher textWatcher;

    private ImagePickerHelper imagePickerHelper;
    private MessageRepository messageRepository;
    private ConversationRepository conversationRepository;
    private UserRepository userRepository;

    private boolean isScrollToTop = false, isEndOfConvesation = false;
    private EmojiPopup emojiPopup;

    private Handler handler = new Handler(); // Handler for updating the visualizer
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            if (isRecording) // if we are already recording
            {
                // get the current amplitude
                int x = myAudioRecorder.getMaxAmplitude();
                visualizerView.addAmplitude(x); // update the VisualizeView
                visualizerView.invalidate(); // refresh the VisualizerView
                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL);
            }
        }
    };
    Message cacheMessage = null;
    private Message selectedMessage;
    private boolean shouldDispatchOnTouch = true;
    private BadgeHelper badgeHelper;

    @Inject
    ChatPresenter presenter;
    ChatComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getComponent().inject(this);

        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        badgeHelper = new BadgeHelper(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bindViews();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            originalConversation = bundle.getParcelable("CONVERSATION");
        }
        init();
        initView();
        presenter.create();
        //initConversationData();
        presenter.initConversationData(conversationID);
    }

    private void initView() {
        notifyTyping();
        messages = new ArrayList<>();
        adapter = new ChatAdapter(conversationID, fromUser.key, messages, this, this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);
        messagesAdapter = new ChatMessageAdapter();
        messagesAdapter.setMessageListener(this);
        recycleChatView.setAdapter(messagesAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        bindViews();

        init();
        // FIXME
        //initConversationData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibleStatus = true;

        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        badgeHelper.read(conversationID);
        setButtonsState(0);
        fromUser = UserManager.getInstance().getUser();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isTyping = false;
        updateConversationTyping(false);

        visibleStatus = false;

        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isTyping = false;
        updateConversationTyping(false);
    }

    @Override
    protected BasePresenter getPresenter() {
        return presenter;
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        setButtonsState(viewId);
        switch (viewId) {
            case R.id.chat_header_center:
            case R.id.chat_person_name:
                onOpenProfile();
                break;
            case R.id.chat_edit:
                isEditMode = true;
                onUpdateEditMode();
                break;
            case R.id.chat_cancel_edit:
                isEditMode = false;
                onUpdateEditMode();
                break;
            case R.id.chat_back:
                onExitChat();
                break;
            case R.id.chat_delete:
                onDeleteMessage();
                break;
            case R.id.chat_mask:
                onUpdateMaskMessage(true);
                break;
            case R.id.chat_unmask:
                onUpdateMaskMessage(false);
                break;
            case R.id.chat_text_btn:
                onSetMessageMode(Constant.MESSAGE_TYPE.TEXT);
                break;
            case R.id.chat_camera_btn:
                onSendCamera();
                break;
            case R.id.chat_image_btn:
                onSendImage();
                break;
            case R.id.chat_voice_btn:
                onSetMessageMode(Constant.MESSAGE_TYPE.VOICE);
                break;
            case R.id.chat_game_btn:
                onGameClicked();
                break;
            case R.id.chat_send_message_btn:
                onSentMessage(originalText);
                break;
            case R.id.chat_start_record:
                onStartRecord();
                break;
            case R.id.chat_cancel_record:
                onStopRecord();
                break;
            case R.id.chat_send_record:
                onSendRecord();
                break;
            case R.id.chat_tgl_outcoming:
                onChangeTypingMark();
                break;
            case R.id.chat_voice_call_btn:
                onVoiceCall();
                break;
            case R.id.chat_video_call_btn:
                onVideoCall();
                break;
            case R.id.chat_emoji_btn:
                showEmojiEditor();
                break;
            case R.id.load_more:
                loadMoreChats();
                isScrollToTop = true;
                break;
            case R.id.btn_copy:
                onCopyText(selectedMessage);
                break;
            case R.id.btn_delete:
                onDeleteMessage(selectedMessage);
                break;
            case R.id.puzzle_game:
                onSendGame(GameType.PUZZLE);
                break;
            case R.id.memory_game:
                onSendGame(GameType.MEMORY);
                break;
            case R.id.tic_tac_toe_game:
                onSendGame(GameType.TIC_TAC_TOE);
                break;
            case R.id.btn_cancel_game_selection:
                hideGameSelection();
                break;
        }
    }

    private void hideGameSelection() {
        chatGameMenu.hide();
        setButtonsState(0);
    }

    private void setButtonsState(int selectedViewId) {
        int[] buttonIDs = new int[]{R.id.chat_camera_btn, R.id.chat_emoji_btn, R.id.chat_game_btn, R.id.chat_image_btn
                , R.id.chat_text_btn, R.id.chat_voice_btn, R.id.chat_video_call_btn, R.id.chat_voice_call_btn};
        if (!ArrayUtils.contains(buttonIDs, selectedViewId) && selectedViewId != 0) {
            return;
        }
        for (int viewId : buttonIDs) {
            ImageButton imageButton = findViewById(viewId);
            imageButton.setSelected(viewId == selectedViewId);
        }

    }

    @Override
    public void onSelect(List<Message> selectMessages) {
        updateEditAllMode();
    }

    @Override
    public void onDoubleTap(Message message, boolean maskStatus) {
        if (message.messageType == Constant.MSG_TYPE_TEXT) {
            Message lastMessage = adapter.getLastMessage();
            boolean isLastMessage = lastMessage != null && TextUtils.equals(lastMessage.key, message.key);
            List<Message> messages = new ArrayList<>(1);
            messages.add(message);
            messageRepository.updateMessageMask(messages, conversationID, fromUser.key, isLastMessage, maskStatus);
        } else if (message.messageType == Constant.MSG_TYPE_IMAGE || message.messageType == Constant.MSG_TYPE_GAME) {
            List<Message> messages = new ArrayList<>(1);
            messages.add(message);
            messageRepository.updateMessageMask(messages, conversationID, fromUser.key, false, maskStatus);
        }
    }

    @Override
    public void onLongPress(Message message) {
        KeyboardHelpers.hideSoftInputKeyboard(this);
        shouldDispatchOnTouch = false;
        selectedMessage = message;
        findViewById(R.id.btn_copy).setVisibility(message.messageType == Constant.MSG_TYPE_TEXT ? View.VISIBLE : View.GONE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            shouldDispatchOnTouch = true;
        }, 800);
    }

    @Override
    public void openImage(String messageKey, String imageUrl, String localImage, boolean isPuzzled, Pair<View, String>... sharedElements) {
        Intent intent = new Intent(this, PuzzleActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
        intent.putExtra("MESSAGE_ID", messageKey);
        intent.putExtra("IMAGE_URL", imageUrl);
        intent.putExtra("LOCAL_IMAGE", localImage);
        intent.putExtra("PUZZLE_STATUS", isPuzzled);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void openGameMessage(Message message) {
        Intent intent = null;
        if (message.gameType == GameType.MEMORY.ordinal()) {
            intent = new Intent(this, GameMemoryActivity.class);
        } else if (message.gameType == GameType.TIC_TAC_TOE.ordinal()) {
            intent = new Intent(this, GameTicTacToeActivity.class);
        } else {
            intent = new Intent(this, GameActivity.class);
        }
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
        intent.putExtra("CONVERSATION", originalConversation);
        intent.putExtra("SENDER", message.sender);
        intent.putExtra("MESSAGE_ID", message.key);
        intent.putExtra("IMAGE_URL", message.gameUrl);
        startActivity(intent);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (shouldDispatchOnTouch) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                if (selectedMessage != null) {
                    //adapter.addOrUpdate(selectedMessage);
                    updateMessage(selectedMessage);
                }
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                //return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        if (requestCode == 111) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onStartRecord();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isMicroPermissionGrant() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public String getConversationId() {
        return conversationID;
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);
        tvChatName = (TextView) findViewById(R.id.chat_person_name);
        tvChatName.setOnClickListener(this);
        tvChatStatus = (TextView) findViewById(R.id.chat_person_status);
        recycleChatView = (RecyclerView) findViewById(R.id.chat_list_view);
        ((SimpleItemAnimator) recycleChatView.getItemAnimator()).setSupportsChangeAnimations(false);
        recycleChatView.setOnTouchListener((view, motionEvent) -> {
            KeyboardHelpers.hideSoftInputKeyboard(ChatActivity.this);
            return false;
        });
        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false) {
            @Override
            public void onLayoutCompleted(RecyclerView.State state) {
                super.onLayoutCompleted(state);
                if (isSettingStackFromEnd.get()) return;

                int contentView = recycleChatView.computeVerticalScrollRange();
                int listHeight = recycleChatView.getMeasuredHeight();
                if (contentView > listHeight) {
                    if (mLinearLayoutManager.getStackFromEnd()) return;
                    setLinearStackFromEnd(true);
                } else {
                    if (!mLinearLayoutManager.getStackFromEnd()) return;
                    isSettingStackFromEnd.set(true);
                    setLinearStackFromEnd(false);
                }
            }
        };
        recycleChatView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int pastVisibleItems = mLinearLayoutManager.findFirstCompletelyVisibleItemPosition();
                boolean loadMoreVisibility = pastVisibleItems <= 3 && pastVisibleItems >= 0 && !isEndOfConvesation;
                updateLoadMoreButtonStatus(loadMoreVisibility);
                isScrollToTop = pastVisibleItems == 0;
            }
        });

        findViewById(R.id.chat_person_name).setOnClickListener(this);
        findViewById(R.id.chat_text_btn).setOnClickListener(this);
        findViewById(R.id.chat_image_btn).setOnClickListener(this);
        findViewById(R.id.chat_camera_btn).setOnClickListener(this);
        findViewById(R.id.chat_voice_btn).setOnClickListener(this);
        findViewById(R.id.chat_game_btn).setOnClickListener(this);
        btnSend = findViewById(R.id.chat_send_message_btn);
        btnSend.setOnClickListener(this);
        btnSend.setEnabled(false);

        btVoiceCall = (ImageButton) findViewById(R.id.chat_voice_call_btn);
        btVoiceCall.setOnClickListener(this);
        btVideoCall = (ImageButton) findViewById(R.id.chat_video_call_btn);
        btVideoCall.setOnClickListener(this);

        btMask = (Button) findViewById(R.id.chat_mask);
        btMask.setOnClickListener(this);
        btUnMask = (Button) findViewById(R.id.chat_unmask);
        btUnMask.setOnClickListener(this);
        btDelete = (Button) findViewById(R.id.chat_delete);
        btDelete.setOnClickListener(this);
        btEdit = (Button) findViewById(R.id.chat_edit);
        btEdit.setOnClickListener(this);
        btCancelEdit = (Button) findViewById(R.id.chat_cancel_edit);
        btCancelEdit.setOnClickListener(this);
        btLoadMoreChat = (ImageView) findViewById(R.id.load_more);
        btLoadMoreChat.setOnClickListener(this);

        layoutText = (LinearLayout) findViewById(R.id.chat_layout_text);
        layoutVoice = (RelativeLayout) findViewById(R.id.chat_layout_voice);
        layoutBottomMenu = (RelativeLayout) findViewById(R.id.chat_bottom_menu);

        edMessage = (EditText) findViewById(R.id.chat_message_tv);
        //edMessage.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);


        visualizerView = (RecorderVisualizerView) findViewById(R.id.visualizer);
        btCancelRecord = (Button) findViewById(R.id.chat_cancel_record);
        btCancelRecord.setOnClickListener(this);
        tbRecord = (Button) findViewById(R.id.chat_start_record);
        tbRecord.setOnClickListener(this);
        btSendRecord = (Button) findViewById(R.id.chat_send_record);
        btSendRecord.setOnClickListener(this);

        tgMarkOut = (CheckBox) findViewById(R.id.chat_tgl_outcoming);
        tgMarkOut.setOnClickListener(this);

        tvNewMsgCount = (TextView) findViewById(R.id.chat_new_message_count);
        layoutMsgType = (LinearLayout) findViewById(R.id.chat_layout_msg_type);
        //emoji
        findViewById(R.id.chat_header_center).setOnClickListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.contentRoot)).build(findViewById(R.id.chat_message_tv));
        btEmoji = findViewById(R.id.chat_emoji_btn);
        btEmoji.setOnClickListener(this);

        IconicsDrawable drawable = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_content_copy)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, R.color.colorAccent));
        IconicsImageView copyImageView = findViewById(R.id.img_copy);
        copyImageView.setIcon(drawable);

        IconicsDrawable deleteDrawable = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_delete)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, R.color.colorAccent));
        IconicsImageView deleteImageView = findViewById(R.id.img_delete);
        deleteImageView.setIcon(deleteDrawable);

        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_chat_game_menu, null);
        chatGameMenu = new BottomSheetDialog(this);
        chatGameMenu.setContentView(view);
        chatGameMenu.setOnDismissListener(dialogInterface -> setButtonsState(0));

        view.findViewById(R.id.puzzle_game).setOnClickListener(this);
        view.findViewById(R.id.memory_game).setOnClickListener(this);
        view.findViewById(R.id.tic_tac_toe_game).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel_game_selection).setOnClickListener(this);

        findViewById(R.id.btn_copy).setOnClickListener(this);
        findViewById(R.id.btn_delete).setOnClickListener(this);

        onSetMessageMode(Constant.MESSAGE_TYPE.TEXT);
        onUpdateEditMode();
    }

    private void setLinearStackFromEnd(boolean value) {
        isSettingStackFromEnd.set(true);
        // Set stack from end
        recycleChatView.post(() -> {
            mLinearLayoutManager.setStackFromEnd(value);
            isSettingStackFromEnd.set(false);
        });
    }

    private Message getLastMessage() {
        if (adapter == null || adapter.getItemCount() < 2) {
            return null;
        }

        // The first item is padding. So we should get item at pos 1
        return adapter.getItem(1);
    }

    private void init() {
        messageRepository = MessageRepository.from(conversationID);
        conversationRepository = new ConversationRepository();
        userRepository = new UserRepository();

        RECORDING_PATH = this.getExternalFilesDir(null).getAbsolutePath();
        //RECORDING_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

        messages = new ArrayList<>();
        fromUser = UserManager.getInstance().getUser();
        fromUserID = fromUser.key;

        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if (key.equals(Constant.PREFS_KEY_MESSAGE_COUNT)) {
                    int messageCount = prefs.getInt(key, 0);
                    updateMessageCount(messageCount);
                }
            }
        };
    }

    private void updateSendButtonStatus(boolean isEnable) {
        btnSend.setEnabled(isEnable);
    }

    private void loadMoreChats() {
        Message lastMessage = getLastMessage();
        if (lastMessage == null) return;
        presenter.loadMoreMessage(lastMessage.timestamp);
    }

    private void startChat() {
        observeChats();
        updateConversationReadStatus();
        //bindConversationSetting();
        BadgesHelper.getInstance().removeCurrentUserBadges(conversationID);
    }

    private void observeChats() {
        adapter.setEditMode(isEditMode);
        adapter.setOrginalConversation(originalConversation);

        initConversationListeners();
    }

    private void initConversationListeners() {
        ValueEventListener maskMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    Map<String, Boolean> maskMessages = (Map<String, Boolean>) dataSnapshot.getValue();
                    originalConversation.maskMessages = maskMessages;
                    presenter.setConversation(originalConversation);
                    adapter.setOrginalConversation(originalConversation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener puzzleMessageListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    Map<String, Boolean> puzzleMessages = (Map<String, Boolean>) dataSnapshot.getValue();
                    originalConversation.puzzleMessages = puzzleMessages;
                    presenter.setConversation(originalConversation);
                    adapter.setOrginalConversation(originalConversation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener memberIdsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Boolean> memberIds = (Map<String, Boolean>) dataSnapshot.getValue();
                    originalConversation.memberIDs = memberIds;
                    userRepository.initMemberList(memberIds, (error, data) -> {
                        if (error == null) {
                            List<User> users = (List<User>) data[0];
                            originalConversation.members = users;
                            presenter.setConversation(originalConversation);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener observeTypingEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Boolean> typingIndicator = (Map<String, Boolean>) dataSnapshot.getValue();
                boolean isTyping = false;
                if (typingIndicator != null) {
                    for (String key : typingIndicator.keySet()) {
                        if (!key.equals(fromUser.key) && typingIndicator.get(key)
                                && !fromUser.blocks.containsKey(key)
                                && !fromUser.blockBys.containsKey(key)) {
                            isTyping = true;
                            break;
                        }
                    }
                }
                showTyping(isTyping);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        ValueEventListener notificationsEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, Boolean> notifications = (HashMap<String, Boolean>) dataSnapshot.getValue();
                    originalConversation.notifications = notifications;
                    presenter.setConversation(originalConversation);
                    adapter.setOrginalConversation(originalConversation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener deleteStatusesEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    originalConversation.deleteStatuses = (Map<String, Boolean>) dataSnapshot.getValue();
                } else {
                    originalConversation.deleteStatuses = new HashMap<>();
                }
                presenter.setConversation(originalConversation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener deleteTimestampsEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    originalConversation.deleteTimestamps = (Map<String, Double>) dataSnapshot.getValue();
                    presenter.setConversation(originalConversation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener nickNameEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, String> nickNames = (HashMap<String, String>) dataSnapshot.getValue();
                    originalConversation.nickNames = nickNames;
                    if (originalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                        updateTitle();
                    } else {
                        adapter.updateNickNames(nickNames);
                    }
                    presenter.setConversation(originalConversation);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference maskReference = conversationRepository.getDatabaseReference().child(conversationID).child("maskMessages");
        maskReference.addValueEventListener(maskMessageListener);
        databaseReferences.put(maskReference, maskMessageListener);

        DatabaseReference puzzleReference = conversationRepository.getDatabaseReference().child(conversationID).child("puzzleMessages");
        puzzleReference.addValueEventListener(puzzleMessageListener);
        databaseReferences.put(puzzleReference, puzzleMessageListener);

        DatabaseReference memberIdsReference = conversationRepository.getDatabaseReference().child(conversationID).child("memberIDs");
        memberIdsReference.addValueEventListener(memberIdsListener);
        databaseReferences.put(memberIdsReference, memberIdsListener);

        DatabaseReference typingReference = conversationRepository.getDatabaseReference().child(conversationID).child("typingIndicator");
        typingReference.addValueEventListener(observeTypingEvent);
        databaseReferences.put(typingReference, observeTypingEvent);

        DatabaseReference notificationReference = conversationRepository.getDatabaseReference().child(conversationID).child("notifications");
        notificationReference.addValueEventListener(notificationsEvent);
        databaseReferences.put(notificationReference, notificationsEvent);

        DatabaseReference deleteStatusReference = conversationRepository.getDatabaseReference().child(conversationID).child("deleteStatuses");
        deleteStatusReference.addValueEventListener(deleteStatusesEvent);
        databaseReferences.put(deleteStatusReference, deleteStatusesEvent);

        DatabaseReference deleteTimestampsReference = conversationRepository.getDatabaseReference().child(conversationID).child("deleteTimestamps");
        deleteTimestampsReference.addValueEventListener(deleteTimestampsEvent);
        databaseReferences.put(deleteTimestampsReference, deleteTimestampsEvent);

        DatabaseReference nickNameReference = conversationRepository.getDatabaseReference().child(conversationID).child("nickNames");
        nickNameReference.addValueEventListener(nickNameEvent);
        databaseReferences.put(nickNameReference, nickNameEvent);
    }

    private void updateTitle() {
        if (originalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            String opponentUserId = originalConversation.opponentUser.key;
            String nickName = originalConversation.nickNames.get(opponentUserId);
            if (TextUtils.isEmpty(nickName)) {
                tvChatName.setText(originalConversation.opponentUser.getDisplayName());
            } else {
                tvChatName.setText(nickName);
            }
        }
    }

    private void notifyTyping() {
        // Tuan - just init textWatcher once
        if (textWatcher != null) return;
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!tgMarkOut.isChecked()) {
                    return;
                }
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                updateSendButtonStatus(!TextUtils.isEmpty(charSequence.toString()));
                if (!tgMarkOut.isChecked()) {
                    return;
                }

                String newOriginalText = "";
                String displayText = charSequence.toString();
                if (TextUtils.equals(displayText, ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText))) {
                    return;
                }

                String encodedStart = "", encodedEnd = "", originalStart = "", originalEnd = "", displayStart = "";
                int originalStartIdx = 0;
                int displayTextLength = displayText.length();
                boolean startFound = false, endFound = false;

                for (int index = 0; index < originalText.length(); index++) {
                    encodedStart = encodedStart + ServiceManager.getInstance().encodeMessage(getApplicationContext(),
                            originalText.substring(index, index + 1));

                    if (displayTextLength >= encodedStart.length()) {
                        displayStart = displayText.substring(0, encodedStart.length());
                    }
                    if (TextUtils.equals(displayStart, encodedStart)) {
                        startFound = true;
                        originalStartIdx = index + 1;
                        originalStart += originalText.substring(index, index + 1);
                    } else {
                        break;
                    }
                }
                encodedStart = ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalStart);

                if (displayTextLength > encodedStart.length()) {
                    for (int index = originalText.length() - 1; index >= originalStartIdx; index--) {
                        encodedEnd = ServiceManager.getInstance().encodeMessage(getApplicationContext(),
                                originalText.substring(index, index + 1)) + encodedEnd;
                        if (TextUtils.equals(displayText.substring(displayTextLength - encodedEnd.length()), encodedEnd)) {
                            endFound = true;
                            originalEnd = originalText.substring(index, index + 1) + originalEnd;
                        } else {
                            break;
                        }
                    }
                }
                newOriginalText = originalStart;
                int middleStartIdx = startFound ? encodedStart.length() : 0;
                int middleEndIdx = endFound ? displayText.lastIndexOf(
                        ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalEnd)) : displayTextLength;
                String originalMiddle = middleEndIdx > middleStartIdx ? displayText.substring(middleStartIdx, middleEndIdx) : "";
                String encodedMiddle = ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalMiddle);
                newOriginalText = newOriginalText + originalMiddle + originalEnd;


                selectPosition = endFound ? encodedStart.length() + encodedMiddle.length()
                        : encodedStart.length() + encodedMiddle.length() + encodedEnd.length();
                originalText = newOriginalText;
            }

            @Override
            public void afterTextChanged(Editable editable) {

                //send typing status
                if (!TextUtils.isEmpty(edMessage.getText()) && !isTyping) {
                    isTyping = true;
                    updateConversationTyping(isTyping);
                } else if (TextUtils.isEmpty(edMessage.getText()) && isTyping) {
                    isTyping = false;
                    updateConversationTyping(isTyping);
                }

                if (!tgMarkOut.isChecked()) {
                    originalText = editable.toString();
                    return;
                }

                String encodeText = ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText);
                if (TextUtils.equals(edMessage.getText(), encodeText)) {
                    return;
                }

                edMessage.removeTextChangedListener(textWatcher);
                edMessage.setText(encodeText);
                if (selectPosition > 0 && selectPosition <= encodeText.length()) {
                    edMessage.setSelection(selectPosition);
                } else {
                    edMessage.setSelection(encodeText.length());
                }
                edMessage.addTextChangedListener(textWatcher);
            }
        };
        edMessage.addTextChangedListener(textWatcher);
        edMessage.setOnTouchListener((view, motionEvent) -> {
            setButtonsState(R.id.chat_text_btn);
            return false;
        });
    }

    private void updateMessageCount(int messageCount) {
        if (messageCount == 0) {
            tvNewMsgCount.setVisibility(View.GONE);
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutUserInfo.getLayoutParams();
//            params.setMargins(200, 0, 60, 0);
//            layoutUserInfo.setLayoutParams(params);
        } else {
            tvNewMsgCount.setVisibility(View.VISIBLE);
            tvNewMsgCount.setText("" + messageCount);
//            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) layoutUserInfo.getLayoutParams();
//            params.setMargins(570, 0, 60, 0);
//            layoutUserInfo.setLayoutParams(params);
        }
    }

    private void onUpdateEditMode() {
        // Hide keyboard
        KeyboardHelpers.hideSoftInputKeyboard(this);
        if (isEditMode) {
            layoutBottomMenu.setVisibility(View.VISIBLE);
            btDelete.setVisibility(View.VISIBLE);
            btEdit.setVisibility(View.GONE);
            btCancelEdit.setVisibility(View.VISIBLE);
            btBack.setVisibility(View.GONE);
            tvNewMsgCount.setVisibility(View.GONE);
            layoutText.setVisibility(View.GONE);
            layoutVoice.setVisibility(View.GONE);
            layoutMsgType.setVisibility(View.GONE);
            updateEditAllMode();
        } else {
            layoutBottomMenu.setVisibility(View.GONE);
            btDelete.setVisibility(View.GONE);
            btEdit.setVisibility(View.VISIBLE);
            btCancelEdit.setVisibility(View.GONE);
            btBack.setVisibility(View.VISIBLE);
            tvNewMsgCount.setVisibility(View.VISIBLE);
            layoutMsgType.setVisibility(View.VISIBLE);
            onSetMessageMode(Constant.MESSAGE_TYPE.TEXT);
            int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
            updateMessageCount(messageCount);
        }
        if (adapter != null) {
            adapter.setEditMode(isEditMode);
        }
        if (messagesAdapter != null) {
            messagesAdapter.updateEditMode(isEditMode);
        }
    }

    private void updateEditAllMode() {
        if (CollectionUtils.isEmpty(adapter.getSelectMessage())) {
            isEditAllMode = true;
        } else {
            isEditAllMode = false;
        }
        if (isEditAllMode) {
            btDelete.setEnabled(false);
            btMask.setText(R.string.chat_mask_all);
            btUnMask.setText(R.string.chat_unmask_all);
        } else {
            btDelete.setEnabled(true);
            btMask.setText(R.string.chat_mask);
            btUnMask.setText(R.string.chat_unmask);
        }
    }

    private void onOpenProfile() {
        Intent intent = new Intent(this, ConversationDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, originalConversation.key);
        extras.putInt(ConversationDetailActivity.CONVERSATION_TYPE_KEY, originalConversation.conversationType);
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void onDeleteMessage() {
        if (isEditAllMode) {
            messageRepository.deleteMessage(conversationID, messages);
        } else {
            messageRepository.deleteMessage(conversationID, adapter.getSelectMessage());
        }
    }

    private void onCopyText(Message selectedMessage) {
        hideBottomSheet();
        if (selectedMessage == null) return;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("message", selectedMessage.message);
        clipboardManager.setPrimaryClip(clipData);
    }

    private void onDeleteMessage(Message selectedMessage) {
        hideBottomSheet();
        if (selectedMessage == null) return;
        List<Message> messagesToDelete = new ArrayList<>();
        messagesToDelete.add(selectedMessage);
        messageRepository.deleteMessage(conversationID, messagesToDelete);
    }

    private void updateConversationLastMessage() {
        Message message = adapter.getLastMessage();
        if (message != null) {
            Conversation conversation = new Conversation(originalConversation.conversationType, message.messageType,
                    message.message, originalConversation.groupID, fromUserID, getMemberIDs(), getMessageMarkStatuses(),
                    getMessageReadStatuses(), message.timestamp, originalConversation);
            conversation.key = originalConversation.key;
            HashMap<String, Boolean> allowance = new HashMap<>();
            allowance.put(fromUser.key, true);
            conversationRepository.updateConversation(conversation.key, conversation, allowance);
        }
    }

    private void hideBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        selectedMessage = null;
    }

    private void onUpdateMaskMessage(boolean mask) {
        if (isEditAllMode) {
            messageRepository.updateMessageMask(messages, conversationID, fromUser.key, true, mask);
        } else {
            List<Message> selectedMessages = adapter.getSelectMessage();
            Message lastMessage = adapter.getLastMessage();
            boolean isLastMessage = false;
            for (Message msg : selectedMessages) {
                if (msg.key.equals(lastMessage.key)) {
                    isLastMessage = true;
                    break;
                }
            }
            messageRepository.updateMessageMask(adapter.getSelectMessage(), conversationID, fromUser.key, isLastMessage, mask);
        }
    }

    private void onSetMessageMode(Constant.MESSAGE_TYPE type) {
        if (emojiPopup.isShowing()) {
            emojiPopup.toggle();
        }
        if (type == Constant.MESSAGE_TYPE.TEXT) {
            layoutText.setVisibility(View.VISIBLE);
            layoutVoice.setVisibility(View.GONE);
            edMessage.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(edMessage, InputMethodManager.SHOW_IMPLICIT);
        } else if (type == Constant.MESSAGE_TYPE.VOICE) {
            layoutText.setVisibility(View.GONE);
            layoutVoice.setVisibility(View.VISIBLE);
            //btSendRecord.setEnabled(false);
            setRecordMode(false);
        }

        if (type != Constant.MESSAGE_TYPE.TEXT) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void showEmojiEditor() {
        layoutText.setVisibility(View.VISIBLE);
        layoutVoice.setVisibility(View.GONE);
        if (!emojiPopup.isShowing()) {
            emojiPopup.toggle();
        }
    }

    private void setRecordMode(boolean isRecording) {
        if (isRecording) {
            btCancelRecord.setVisibility(View.VISIBLE);
            btSendRecord.setVisibility(View.VISIBLE);
        } else {
            btCancelRecord.setVisibility(View.GONE);
            btSendRecord.setVisibility(View.GONE);
        }
    }

    private void onChangeTypingMark() {
        ServiceManager.getInstance().changeMaskOutputConversation(originalConversation, tgMarkOut.isChecked());
        edMessage.removeTextChangedListener(textWatcher);
        int select = edMessage.getSelectionStart();
        if (tgMarkOut.isChecked()) {
            edMessage.setText(ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText));
            select = ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText.substring(0, select)).length();
        } else {
            edMessage.setText(originalText);
        }
        try {
            if (select > 0 && select <= edMessage.getText().length()) {
                edMessage.setSelection(select);
            } else {
                edMessage.setSelection(edMessage.getText().length());

            }
        } catch (IndexOutOfBoundsException ex) {
            Log.e(ex);
        }

        edMessage.addTextChangedListener(textWatcher);
    }

    private void onExitChat() {
        finish();
    }

    private void onSentMessage(String text) {
        //loadMoreChats();
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(getApplicationContext(), "Please input message", Toast.LENGTH_SHORT).show();
            return;
        }
//        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
//            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (!beAbleToSendMessage()) {
            return;
        }

        edMessage.setText(null);
        presenter.sendTextMessage(text, tgMarkOut.isChecked());
    }

    private void sendMessage(String messageKey, Message message) {
        messageRepository.updateMessage(messageKey, message, (error, data) -> {
            if (error == null) {
                messageRepository.updateMessageStatus(message.key, originalConversation.memberIDs.keySet(), Constant.MESSAGE_STATUS_DELIVERED);
            } else {
                messageRepository.updateMessageStatus(message.key, originalConversation.memberIDs.keySet(), Constant.MESSAGE_STATUS_ERROR);
            }
        });
    }

    private void onSendCamera() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!beAbleToSendMessage()) {
            return;
        }

        imagePickerHelper = ImagePickerHelper.from(this)
                .setCrop(false)
                .setScale(true)
                .setGenerateThumbnail(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {
                        // FIXME: should improve this way
//                        cacheMessage = sendImageMessage("", "", Constant.MSG_TYPE_IMAGE, null);
//                        cacheMessage.localImage = file.getAbsolutePath();
//                        adapter.addOrUpdate(cacheMessage);
                    }

                    @Override
                    public void onFinalImage(File... files) {
                        File file = files[0];
                        File thumbnail = files[1];
                        sendImageFirebase(file, thumbnail);
                    }
                });
        imagePickerHelper.openCamera();
    }

    private void onSendImage() {
//        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
//            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (!beAbleToSendMessage()) {
            return;
        }

        imagePickerHelper = ImagePickerHelper.from(this)
                .setCrop(false)
                .setScale(true)
                .setGenerateThumbnail(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {
//                        cacheMessage = sendImageMessage("", "", Constant.MSG_TYPE_IMAGE, null);
//                        cacheMessage.localImage = file.getAbsolutePath();
//                        adapter.addOrUpdate(cacheMessage);
                    }

                    @Override
                    public void onFinalImage(File... files) {
                        File file = files[0];
                        File thumbnail = files[1];
                        sendImageFirebase(file, thumbnail);
                    }
                });
        imagePickerHelper.openPicker();
    }

    private void onGameClicked() {
        chatGameMenu.show();
    }

    private void onSendGame(GameType gameType) {
        chatGameMenu.hide();
//        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
//            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (!beAbleToSendMessage()) {
            return;
        }

        imagePickerHelper = ImagePickerHelper.from(this)
                .setCrop(false)
                .setScale(true)
                .setGenerateThumbnail(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {

                    }

                    @Override
                    public void onFinalImage(File... files) {
                        File file = files[0];
                        sendGameFirebase(file, gameType);
                    }
                });
        imagePickerHelper.openPicker();
    }

    private void onStartRecord() {
        if (!isMicroPermissionGrant()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 111);
            return;
        }
        if (isRecording) {
            return;
        }
        setRecordMode(true);
        visualizerView.clear();
        //btSendRecord.setEnabled(false);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss");
        String currentTimeStamp = dateFormat.format(new Date());

        currentOutFile = RECORDING_PATH + "/recording_" + currentTimeStamp + ".3gp";
        CommonMethod.createFolder(RECORDING_PATH);

        myAudioRecorder = new MediaRecorder();
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        myAudioRecorder.setOutputFile(currentOutFile);
        try {
            myAudioRecorder.prepare();
            myAudioRecorder.start();
            //btSendRecord.setEnabled(true);
            isRecording = true;
            handler.post(updateVisualizer);
        } catch (Exception e) {
            Log.e(e);
            //btSendRecord.setEnabled(false);
            isRecording = false;
        }
    }

    private void onStopRecord() {
        try {
            if (null != myAudioRecorder) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
            }
            //btSendRecord.setEnabled(true);
        } catch (Exception e) {
            Log.e(e);
        }
        //btSendRecord.setEnabled(true);
        isRecording = false;

        handler.removeCallbacks(updateVisualizer);
        visualizerView.clear();
        visualizerView.invalidate();
        setRecordMode(false);
    }

    private void onSendRecord() {
        onStopRecord();
//        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
//            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
//            return;
//        }
        if (!beAbleToSendMessage()) {
            return;
        }
        File audioFile = new File(currentOutFile);
        presenter.sendAudioMessage(audioFile.getAbsolutePath());
        visualizerView.clear();
        setRecordMode(false);
        //setButtonsState(0);
    }

    private void onVoiceCall() {
        CallActivity.start(this, originalConversation.opponentUser, false);
    }

    private void onVideoCall() {
        CallActivity.start(this, originalConversation.opponentUser, true);
    }

    private Map<String, Boolean> getMemberIDs() {
        Map<String, Boolean> memberIDs = new HashMap<>();
        for (User toUser : originalConversation.members) {
            memberIDs.put(toUser.key, true);
        }
        return memberIDs;
    }

    private Map<String, Boolean> getMessageMarkStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        if (originalConversation.maskMessages != null) {
            markStatuses.putAll(originalConversation.maskMessages);
        }
        markStatuses.put(fromUser.key, tgMarkOut.isChecked());
        return markStatuses;
    }

    private Map<String, Boolean> getMessageReadStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        for (User toUser : originalConversation.members) {
            markStatuses.put(toUser.key, false);
        }
        markStatuses.put(fromUser.key, true);
        return markStatuses;
    }

    private void sendImageFirebase(File file, File thumbnail) {
        presenter.sendImageMessage(file.getAbsolutePath(), thumbnail.getAbsolutePath(), tgMarkOut.isChecked());
    }

    private void sendGameFirebase(File file, GameType gameType) {
        presenter.sendGameMessage(file.getAbsolutePath(), gameType, tgMarkOut.isChecked());
    }

    private void updateMessageStatus(Message message) {
        int status = CommonMethod.getCurrentStatus(fromUserID, message.status);
        if (!message.senderId.equals(fromUserID)) {
            if (status != Constant.MESSAGE_STATUS_READ) {
                messageRepository.updateMessageStatus(message.key, originalConversation.memberIDs.keySet(), Constant.MESSAGE_STATUS_READ);
            }
            return;
        } else {
            if (status == Constant.MESSAGE_STATUS_ERROR && message.messageType == Constant.MSG_TYPE_TEXT) {
                presenter.resendMessage(message);
            }
        }
    }

    private void updateConversationReadStatus() {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        if (!visibleStatus) {
            return;
        }
        conversationRepository.updateUserReadStatus(conversationID, fromUser.key, true);
    }

    private void updateMessageMarkStatus(Message message) {
        if (message.markStatuses == null || !message.markStatuses.containsKey(fromUser.key)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, message.key,
                    (ServiceManager.getInstance().getMaskSetting(originalConversation.maskMessages)));
        }
    }

    private void showTyping(boolean typing) {
        adapter.showTyping(typing);
        recycleChatView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void updateConversationTyping(boolean typing) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        conversationRepository.updateTypingIndicatorForUser(originalConversation, fromUserID, typing);
    }

    private boolean beAbleToSendMessage() {
        if (originalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            if (fromUser.blocks.containsKey(originalConversation.opponentUser.key)) {
                String username = originalConversation.opponentUser.firstName;
                Toaster.shortToast(String.format(getApplicationContext().getString(R.string.msg_account_msg_blocked), username, username));
                return false;
            }
        }
        return true;
    }

    private void updateLoadMoreButtonStatus(boolean isShow) {
        btLoadMoreChat.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateConversation(Conversation conv) {
        this.originalConversation = conv;
        this.adapter.setOrginalConversation(conv);
        if (conv.conversationType == Constant.CONVERSATION_TYPE_GROUP) {
            btVideoCall.setVisibility(View.GONE);
            btVoiceCall.setVisibility(View.GONE);
        }
        // FIXME
        startChat();
    }

    @Override
    public void updateConversationTitle(String title) {
        tvChatName.setText(title);
    }

    @Override
    public void updateMaskSetting(boolean isEnable) {
        tgMarkOut.setChecked(isEnable);
    }

    @Override
    public void updateUserStatus(boolean isOnline) {
        tvChatStatus.setText(isOnline ? "Online" : "Offline");
    }

    @Override
    public void hideUserStatus() {
        tvChatStatus.setVisibility(View.GONE);
    }

    @Override
    public void onCurrentUser(User user) {
        fromUser = user;
    }

    @Override
    public void addNewMessage(Message data) {
        //adapter.addOrUpdate(data);
        FlexibleItem item = MessageBaseItem.from(data, fromUserID, originalConversation.conversationType);
        if (item instanceof MessageBaseItem) {
            messagesAdapter.addOrUpdate((MessageBaseItem) item);
        }
        // FIXME why should update mark status
        updateMessageMarkStatus(data);
        updateMessageStatus(data);
        updateConversationReadStatus();
        recycleChatView.scrollToPosition(recycleChatView.getAdapter().getItemCount() - 1);
    }

    @Override
    public void removeMessage(Message data) {
        //adapter.deleteMessage(data.key);
        messagesAdapter.deleteMessage(data.key);
        this.updateConversationLastMessage();
    }

    @Override
    public void updateMessage(Message data) {
        FlexibleItem item = MessageBaseItem.from(data, fromUserID, originalConversation.conversationType);
        if (item instanceof MessageBaseItem) {
            ((MessageBaseItem) item).setMessageListener(this);
            messagesAdapter.addOrUpdate((MessageBaseItem) item);
        }
    }

    @Override
    public void updateLastMessages(List<Message> messages, boolean canLoadMore) {
        adapter.appendHistoryItems(messages);
        if (!canLoadMore) {
            isEndOfConvesation = true;
            updateLoadMoreButtonStatus(false);
        }
    }

    @Override
    public void sendNotification(Conversation conversation, Message message) {
        NotificationHelper.getInstance().sendNotificationForConversation(conversation, message);
    }

    @Override
    public void addCacheMessage(Message message) {
        //adapter.addOrUpdate(message);
        FlexibleItem item = MessageBaseItem.from(message, fromUserID, originalConversation.conversationType);
        messagesAdapter.addOrUpdate((MessageBaseItem) item);
        //recycleChatView.scrollToPosition(recycleChatView.getAdapter().getItemCount() - 1);
    }

    @Override
    public ChatComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideChatComponent(new ChatModule(this));
        }
        return component;
    }

    @Override
    public void handleProfileImagePress(User user, Pair<View, String>[] sharedElements) {
        Intent intent = new Intent(this, UserDetailActivity.class);
        intent.putExtra(Constant.START_ACTIVITY_USER_ID, user.key);
        intent.putExtra(UserDetailActivity.EXTRA_USER, user);
        intent.putExtra(UserDetailActivity.EXTRA_USER_IMAGE, sharedElements[0].second);
        intent.putExtra(UserDetailActivity.EXTRA_USER_NAME, "");
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, sharedElements
        );
        startActivity(intent, options.toBundle());
    }

    @Override
    public void updateMessageMask(Message message, boolean maskStatus, boolean lastItem) {
        List<Message> messages = new ArrayList<>(1);
        messages.add(message);
        messageRepository.updateMessageMask(messages, conversationID, fromUser.key, lastItem, maskStatus);
    }
}
