package com.ping.android.presentation.view.activity;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.transition.Slide;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.scopes.HasComponent;
import com.bzzzchat.flexibleadapter.FlexibleItem;
import com.bzzzchat.videorecorder.view.VideoPlayerActivity;
import com.bzzzchat.videorecorder.view.VideoRecorderActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.ping.android.R;
import com.ping.android.dagger.loggedin.chat.ChatComponent;
import com.ping.android.dagger.loggedin.chat.ChatModule;
import com.ping.android.device.impl.ShakeEventManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.Color;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.presentation.view.adapter.ChatMessageAdapter;
import com.ping.android.presentation.view.custom.VoiceRecordView;
import com.ping.android.presentation.view.custom.VoiceRecordViewListener;
import com.ping.android.presentation.view.custom.revealable.RevealableViewRecyclerView;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.configs.Constant;
import com.ping.android.utils.BadgeHelper;
import com.bzzzchat.configuration.GlideApp;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.KeyboardHelpers;
import com.ping.android.utils.Log;
import com.ping.android.utils.ThemeUtils;
import com.ping.android.utils.Toaster;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class ChatActivity extends CoreActivity implements ChatPresenter.View, HasComponent<ChatComponent>,
        View.OnClickListener, ChatMessageAdapter.ChatMessageListener {
    private static final int CAMERA_REQUEST_CODE = 12345;

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    public static final String EXTRA_CONVERSATION_NAME = "EXTRA_CONVERSATION_NAME";
    public static final String EXTRA_CONVERSATION_TRANSITION_NAME = "EXTRA_CONVERSATION_TRANSITION_NAME";
    public static final String EXTRA_CONVERSATION_COLOR = "EXTRA_CONVERSATION_COLOR";

    public static final String CONVERSATION_ID = "CONVERSATION_ID";

    //Views UI
    private ImageView backgroundImage;
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private RelativeLayout layoutBottomMenu;
    private VoiceRecordView layoutVoice;
    private LinearLayout layoutText, layoutMsgType;
    private ImageView btBack;
    private AppCompatCheckBox tgMarkOut;
    private TextView tvChatStatus;
    private Button btMask, btUnMask, btDelete, btEdit, btCancelEdit;
    private ImageButton btVoiceCall, btVideoCall, btEmoji;
    private EmojiEditText edMessage;
    private TextView tvChatName, tvNewMsgCount;
    private TextView tvInstruction;
    private Button btnSend;
    private BottomSheetDialog chatGameMenu;
    private BottomSheetDialog messageActions;

    private LinearLayout copyContainer;
    private SwipeRefreshLayout swipeRefreshLayout;

    private String conversationID;
    private Conversation originalConversation;
    private ChatMessageAdapter messagesAdapter;

    private boolean isRecording = false, isTyping = false, isEditMode = false;
    private AtomicBoolean isSettingStackFromEnd = new AtomicBoolean(false);
    private String originalText = "";
    private int selectPosition = 0;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private TextWatcher textWatcher;

    private ImagePickerHelper imagePickerHelper;
    private ShakeEventManager shakeEventManager;

    private boolean isScrollToTop = false;
    private EmojiPopup emojiPopup;

    private Handler handler = new Handler(); // Handler for updating the visualizer
    private MessageBaseItem selectedMessage;
    private BadgeHelper badgeHelper;

    @Inject
    ChatPresenter presenter;
    ChatComponent component;
    private boolean isVisible = false;
    private List<Integer> actionButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            originalConversation = bundle.getParcelable("CONVERSATION");
            Color currentColor = Color.DEFAULT;
            if (bundle.containsKey(EXTRA_CONVERSATION_COLOR)) {
                int color = bundle.getInt(EXTRA_CONVERSATION_COLOR);
                currentColor = Color.from(color);
                ThemeUtils.onActivityCreateSetTheme(this, currentColor);
            }
            presenter.initThemeColor(currentColor);
        }
        setContentView(R.layout.activity_chat);

        conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        badgeHelper = new BadgeHelper(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bindViews();

        init();
        initView();
        presenter.create();
        //initConversationData();
        presenter.initConversationData(conversationID);
    }

    private void initView() {
        int[] buttonIDs = new int[]{R.id.chat_camera_btn, R.id.chat_emoji_btn, R.id.chat_game_btn, R.id.chat_image_btn
                , R.id.chat_text_btn, R.id.chat_voice_btn, R.id.chat_video_call_btn, R.id.chat_voice_call_btn};
        actionButtons = new ArrayList<>(buttonIDs.length);
        for (int buttonId : buttonIDs) {
            actionButtons.add(buttonId);
        }
        notifyTyping();
        recycleChatView.setLayoutManager(mLinearLayoutManager);
        messagesAdapter = new ChatMessageAdapter();
        //messagesAdapter.setHasStableIds();
        messagesAdapter.setMessageListener(this);
        recycleChatView.setAdapter(messagesAdapter);
        ((RevealableViewRecyclerView) recycleChatView).setCallback(messagesAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String conversationID = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        // FIXME
        if (!this.conversationID.equals(conversationID)) {
            presenter.initConversationData(conversationID);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        badgeHelper.read(conversationID);
        setButtonsState(0);
        isVisible = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isTyping = false;
        updateConversationTyping(false);

        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isTyping = false;
        messagesAdapter.pause();
        updateConversationTyping(false);
        isVisible = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (this.shakeEventManager != null) {
            this.shakeEventManager.unregister();
        }
        messagesAdapter.destroy();
        layoutVoice.release();
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
                onDeleteMessages();
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
                handleRecordVoice();
                break;
            case R.id.chat_game_btn:
                onGameClicked();
                break;
            case R.id.chat_send_message_btn:
                onSentMessage(originalText);
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
            case R.id.btn_copy:
                onCopySelectedMessageText();
                break;
            case R.id.btn_delete:
                onDeleteSelectedMessage();
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
        if (!actionButtons.contains(selectedViewId) && selectedViewId != 0) {
            return;
        }
        for (int viewId : actionButtons) {
            ImageButton imageButton = findViewById(viewId);
            imageButton.setSelected(viewId == selectedViewId);
        }

    }

    @Override
    public void onLongPress(MessageBaseItem message) {
        KeyboardHelpers.hideSoftInputKeyboard(this);
        selectedMessage = message;
        copyContainer.setVisibility(message.message.messageType == Constant.MSG_TYPE_TEXT ? View.VISIBLE : View.GONE);
        messageActions.show();
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
    public void updateMessageSelection(int size) {
        if (size == 0) {
            btDelete.setEnabled(false);
            btMask.setEnabled(false);
            btUnMask.setEnabled(false);
        } else {
            btDelete.setEnabled(true);
            btMask.setEnabled(true);
            btUnMask.setEnabled(true);
        }
    }

    @Override
    public void updateLastConversationMessage(Message lastMessage) {
        presenter.updateConversationLastMessage(lastMessage);
    }

    @Override
    public void openVideo(String videoUrl) {
        Intent intent = new Intent(this, VideoPlayerActivity.class);
        intent.putExtra(VideoPlayerActivity.VIDEO_PATH_EXTRA_KEY, videoUrl);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            String imagePath = data.getStringExtra(VideoRecorderActivity.IMAGE_EXTRA_KEY);
            String videoPath = data.getStringExtra(VideoRecorderActivity.VIDEO_EXTRA_KEY);
            if (!TextUtils.isEmpty(imagePath)) {
                presenter.sendImageMessage(imagePath, imagePath, tgMarkOut.isChecked());
            } else if (!TextUtils.isEmpty(videoPath)) {
                presenter.sendVideoMessage(videoPath);
            }
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
                handleRecordVoice();
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
        String conversationName = getIntent().getStringExtra(EXTRA_CONVERSATION_NAME);
        String conversationTransionName = getIntent().getStringExtra(EXTRA_CONVERSATION_TRANSITION_NAME);

        btBack = findViewById(R.id.chat_back);
        tvChatName = findViewById(R.id.chat_person_name);
        tvChatStatus = findViewById(R.id.chat_person_status);
        tvInstruction = findViewById(R.id.instruction);
        recycleChatView = findViewById(R.id.chat_list_view);
        backgroundImage = findViewById(R.id.backgroundImage);
        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        btnSend = findViewById(R.id.chat_send_message_btn);
        btVoiceCall = findViewById(R.id.chat_voice_call_btn);
        btVideoCall = findViewById(R.id.chat_video_call_btn);
        btMask = findViewById(R.id.chat_mask);
        btUnMask = findViewById(R.id.chat_unmask);
        btDelete = findViewById(R.id.chat_delete);
        btEdit = findViewById(R.id.chat_edit);
        btCancelEdit = findViewById(R.id.chat_cancel_edit);
        layoutText = findViewById(R.id.chat_layout_text);
        layoutVoice = findViewById(R.id.chat_layout_voice);
        tgMarkOut = findViewById(R.id.chat_tgl_outcoming);
        tvNewMsgCount = findViewById(R.id.chat_new_message_count);
        layoutMsgType = findViewById(R.id.chat_layout_msg_type);
        btEmoji = findViewById(R.id.chat_emoji_btn);

        btBack.setOnClickListener(this);
        tvChatName.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnSend.setEnabled(false);
        btVoiceCall.setOnClickListener(this);
        btVideoCall.setOnClickListener(this);
        btMask.setOnClickListener(this);
        btUnMask.setOnClickListener(this);
        btDelete.setOnClickListener(this);
        btEdit.setOnClickListener(this);
        btCancelEdit.setOnClickListener(this);
        tgMarkOut.setOnClickListener(this);
        findViewById(R.id.chat_person_name).setOnClickListener(this);
        findViewById(R.id.chat_text_btn).setOnClickListener(this);
        findViewById(R.id.chat_image_btn).setOnClickListener(this);
        findViewById(R.id.chat_camera_btn).setOnClickListener(this);
        findViewById(R.id.chat_voice_btn).setOnClickListener(this);
        findViewById(R.id.chat_game_btn).setOnClickListener(this);
        findViewById(R.id.chat_header_center).setOnClickListener(this);

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

                int lastVisibleItem = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                isScrollToTop = lastVisibleItem == mLinearLayoutManager.getItemCount() - 1;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(this::loadMoreChats);

        tvChatName.setText(conversationName);
        tvChatName.setTransitionName(conversationTransionName);
        layoutVoice.setListener(new VoiceRecordViewListener() {
            @Override
            public void sendVoice(@NotNull String outputFile, @NotNull VoiceType selectedVoice) {
                presenter.sendAudioMessage(outputFile, selectedVoice);
            }

            @Override
            public void showInstruction(@NotNull String instruction) {
                tvInstruction.setVisibility(View.VISIBLE);
                tvInstruction.setText(instruction);
            }

            @Override
            public void hideInstruction() {
                tvInstruction.setVisibility(View.GONE);
            }
        });
        layoutBottomMenu = findViewById(R.id.chat_bottom_menu);

        edMessage = findViewById(R.id.chat_message_tv);
        //edMessage.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);


//        visualizerView = findViewById(R.id.visualizer);
//        btCancelRecord = findViewById(R.id.chat_cancel_record);
//        btCancelRecord.setOnClickListener(this);
//        tbRecord = findViewById(R.id.chat_start_record);
//        tbRecord.setOnClickListener(this);
//        btSendRecord = findViewById(R.id.chat_send_record);
//        btSendRecord.setOnClickListener(this);

        //emoji
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.contentRoot)).build(edMessage);
        btEmoji.setOnClickListener(this);

//        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
//        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


        // Bottom message action
        View messageActionsView = getLayoutInflater().inflate(R.layout.bottom_sheet_message_actions, null);
        copyContainer = messageActionsView.findViewById(R.id.btn_copy);
        copyContainer.setOnClickListener(this);
        messageActionsView.findViewById(R.id.btn_delete).setOnClickListener(this);
        messageActions = new BottomSheetDialog(this);
        messageActions.setContentView(messageActionsView);
        messageActions.setOnDismissListener(dialog -> hideSelectedMessage());

        // Bottom chat menu
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_chat_game_menu, null);
        chatGameMenu = new BottomSheetDialog(this);
        chatGameMenu.setContentView(view);
        chatGameMenu.setOnDismissListener(dialogInterface -> setButtonsState(0));
        view.findViewById(R.id.puzzle_game).setOnClickListener(this);
        view.findViewById(R.id.memory_game).setOnClickListener(this);
        view.findViewById(R.id.tic_tac_toe_game).setOnClickListener(this);
        view.findViewById(R.id.btn_cancel_game_selection).setOnClickListener(this);

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
        // TODO
        if (messagesAdapter == null || messagesAdapter.getItemCount() < 2) {
            return null;
        }

        // The first item is padding. So we should get item at pos 1
        FlexibleItem item = messagesAdapter.getItem(1);
        if (item instanceof MessageBaseItem) {
            return ((MessageBaseItem) item).message;
        } else if (item instanceof MessageHeaderItem) {
            return ((MessageHeaderItem) item).getChildItems().get(0).message;
        }
        return null;
    }

    private void init() {
        listener = (prefs, key) -> {
            if (key.equals(Constant.PREFS_KEY_MESSAGE_COUNT)) {
                int messageCount = prefs.getInt(key, 0);
                updateMessageCount(messageCount);
            }
        };

        shakeEventManager = new ShakeEventManager(this);
        registerEvent(shakeEventManager.getShakeEvent()
                .debounce(700, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    handleShakePhone();
                }));
    }

    private void handleShakePhone() {
        // Find visible items
        Log.d("handleShakePhone");
        int firstVisible = mLinearLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = mLinearLayoutManager.findLastVisibleItemPosition();
        List<Message> visibleMessages = messagesAdapter.findMessages(firstVisible, lastVisible);
        boolean isMask = false;
        for (Message message : visibleMessages) {
            if (message.messageType == Constant.MSG_TYPE_TEXT || message.messageType == Constant.MSG_TYPE_IMAGE) {
                if (!message.isMask) {
                    isMask = true;
                    break;
                }
            }
        }
        presenter.updateMaskMessages(visibleMessages, lastVisible == messagesAdapter.getItemCount() - 1, isMask);
    }

    private void updateSendButtonStatus(boolean isEnable) {
        btnSend.setEnabled(isEnable);
    }

    private void loadMoreChats() {
        Message lastMessage = getLastMessage();
        if (lastMessage == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }
        presenter.loadMoreMessage(lastMessage.timestamp);
    }

    private void notifyTyping() {
        // Tuan - just init textWatcher once
        if (textWatcher != null) return;
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
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
            // FIXME: call onSetMessageMode make keyboard show up
            //onSetMessageMode(Constant.MESSAGE_TYPE.TEXT);
            layoutText.setVisibility(View.VISIBLE);
            layoutVoice.setVisibility(View.GONE);

            int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
            updateMessageCount(messageCount);
        }
        if (messagesAdapter != null) {
            messagesAdapter.updateEditMode(isEditMode);
        }
    }

    private void updateEditAllMode() {
        btDelete.setEnabled(false);
        btMask.setText(R.string.chat_mask);
        btUnMask.setText(R.string.chat_unmask);
        btMask.setEnabled(false);
        btUnMask.setEnabled(false);
    }

    private void onOpenProfile() {
        Intent intent = new Intent(this, ConversationDetailActivity.class);
        Bundle extras = new Bundle();
        extras.putString(ConversationDetailActivity.CONVERSATION_KEY, originalConversation.key);
        extras.putInt(ConversationDetailActivity.CONVERSATION_TYPE_KEY, originalConversation.conversationType);
        extras.putInt(ChatActivity.EXTRA_CONVERSATION_COLOR, originalConversation.currentColor.getCode());
        intent.putExtras(extras);
        startActivity(intent);
    }

    private void onDeleteMessages() {
        List<Message> messages = messagesAdapter.getSelectedMessages();
        presenter.deleteMessages(messages);
    }

    private void onCopySelectedMessageText() {
        messageActions.hide();
        if (selectedMessage == null) return;
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("message", selectedMessage.message.message);
        clipboardManager.setPrimaryClip(clipData);

        Toast.makeText(this, "Message copied", Toast.LENGTH_SHORT).show();
        hideSelectedMessage();
    }

    private void onDeleteSelectedMessage() {
        messageActions.hide();
        if (selectedMessage == null) return;

        List<Message> messagesToDelete = new ArrayList<>();
        messagesToDelete.add(selectedMessage.message);
        presenter.deleteMessages(messagesToDelete);
        hideSelectedMessage();
    }

    private void hideSelectedMessage() {
        messageActions.hide();
        if (selectedMessage != null) {
            selectedMessage.setEditMode(false);
            selectedMessage.setSelected(true);
            messagesAdapter.update(selectedMessage);
        }
        selectedMessage = null;
    }

    private void onUpdateMaskMessage(boolean mask) {
        List<Message> selectedMessages = messagesAdapter.getSelectedMessages();
        Message lastMessage = messagesAdapter.getLastMessage();
        boolean isLastMessage = false;
        if (lastMessage != null) {
            for (Message msg : selectedMessages) {
                if (msg.key.equals(lastMessage.key)) {
                    isLastMessage = true;
                    break;
                }
            }
        }
        showLoading();
        if (networkStatus != Constant.NETWORK_STATUS.CONNECTED) {
            handler.postDelayed(() -> switchOffEditMode(), 2000);
        }
        presenter.updateMaskMessages(selectedMessages, isLastMessage, mask);
    }

    private void onSetMessageMode(Constant.MESSAGE_TYPE type) {
        if (emojiPopup.isShowing()) {
            emojiPopup.toggle();
        }
        if (type == Constant.MESSAGE_TYPE.TEXT) {
            layoutText.setVisibility(View.VISIBLE);
            layoutVoice.setVisibility(View.GONE);
            edMessage.requestFocus();
            KeyboardHelpers.showKeyboard(this, edMessage);
        } else if (type == Constant.MESSAGE_TYPE.VOICE) {
            View view = findViewById(R.id.bottom_container);
            layoutText.setVisibility(View.GONE);
            TransitionManager.beginDelayedTransition((ViewGroup) view, new Slide());
            layoutVoice.setVisibility(View.VISIBLE);
        }

        if (type != Constant.MESSAGE_TYPE.TEXT) {
            KeyboardHelpers.hideSoftInputKeyboard(this);
        }
    }

    private void showEmojiEditor() {
        layoutText.setVisibility(View.VISIBLE);
        layoutVoice.setVisibility(View.GONE);
        if (!emojiPopup.isShowing()) {
            emojiPopup.toggle();
        }
    }

    private void onChangeTypingMark() {
        ServiceManager.getInstance().changeMaskOutputConversation(originalConversation, tgMarkOut.isChecked());
        edMessage.removeTextChangedListener(textWatcher);
        int select = edMessage.getSelectionStart();
        if (tgMarkOut.isChecked()) {
            updateMaskTintColor(true);
            edMessage.setText(ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText));
            select = ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText.substring(0, select)).length();
        } else {
            updateMaskTintColor(false);
            //int color = ContextCompat.getColor(this, R.color.gray_color);
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
        finishAfterTransition();
    }

    private void onSentMessage(String text) {
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(getApplicationContext(), "Please input message", Toast.LENGTH_SHORT).show();
            return;
        }
        edMessage.setText(null);
        presenter.sendTextMessage(text, tgMarkOut.isChecked());
    }

    private void onSendCamera() {
        // Should check permission here
        Intent intent = new Intent(this, VideoRecorderActivity.class);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);

        /*imagePickerHelper = ImagePickerHelper.from(this)
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
        imagePickerHelper.openCamera();*/
    }

    private void onSendImage() {
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

    private void handleRecordVoice() {
        if (!isMicroPermissionGrant()) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 111);
            return;
        }
        if (isRecording) {
            return;
        }
        onSetMessageMode(Constant.MESSAGE_TYPE.VOICE);
    }

    private void onVoiceCall() {
        presenter.handleVoiceCallPress();
    }

    private void onVideoCall() {
        presenter.handleVideoCallPress();
    }

    private void sendImageFirebase(File file, File thumbnail) {
        presenter.sendImageMessage(file.getAbsolutePath(), thumbnail.getAbsolutePath(), tgMarkOut.isChecked());
    }

    private void sendGameFirebase(File file, GameType gameType) {
        presenter.sendGameMessage(file.getAbsolutePath(), gameType, tgMarkOut.isChecked());
    }

    private void showTyping(boolean typing) {
        if (typing) {
            messagesAdapter.showTyping();
        } else {
            messagesAdapter.hideTypingItem();
        }
        if (isVisible) {
            recycleChatView.scrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    private void updateConversationTyping(boolean typing) {
        presenter.handleUserTypingStatus(typing);
    }

    @Override
    public void updateConversation(Conversation conv) {
        if (this.shakeEventManager != null) {
            this.shakeEventManager.register();
        }
        this.originalConversation = conv;
        if (conv.conversationType == Constant.CONVERSATION_TYPE_GROUP) {
            btVideoCall.setVisibility(View.GONE);
            btVoiceCall.setVisibility(View.GONE);
            findViewById(R.id.spacing_voice).setVisibility(View.GONE);
            findViewById(R.id.spacing_audio).setVisibility(View.GONE);
        } else {
            btVideoCall.setVisibility(View.VISIBLE);
            btVoiceCall.setVisibility(View.VISIBLE);
            findViewById(R.id.spacing_voice).setVisibility(View.VISIBLE);
            findViewById(R.id.spacing_audio).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateConversationTitle(String title) {
        tvChatName.setText(title);
    }

    @Override
    public void updateMaskSetting(boolean isEnable) {
        tgMarkOut.setChecked(isEnable);
        updateMaskTintColor(isEnable);
    }

    @Override
    public void updateUserStatus(boolean isOnline) {
        tvChatStatus.setText(isOnline ? "Active" : "Inactive");
    }

    @Override
    public void hideUserStatus() {
        tvChatStatus.setVisibility(View.GONE);
    }

    @Override
    public void removeMessage(MessageHeaderItem headerItem, MessageBaseItem data) {
        //adapter.deleteMessage(data.key);
        messagesAdapter.deleteMessage(headerItem, data);
        //this.updateConversationLastMessage();
    }

    @Override
    public void updateLastMessages(List<MessageHeaderItem> messages, boolean canLoadMore) {
        messagesAdapter.updateData(messages);
        if (!canLoadMore) {
            swipeRefreshLayout.setEnabled(false);
        }
    }

    @Override
    public void switchOffEditMode() {
        hideLoading();
        if (isEditMode) {
            isEditMode = false;
            onUpdateEditMode();
        }
    }

    @Override
    public void updateNickNames(Map<String, String> nickNames) {
        if (messagesAdapter != null) {
            messagesAdapter.updateNickNames(nickNames);
        }
    }

    @Override
    public void toggleTyping(boolean b) {
        showTyping(b);
    }

    @Override
    public void showErrorUserBlocked(String username) {
        Toaster.shortToast(String.format(getApplicationContext().getString(R.string.msg_account_msg_blocked), username, username));
    }

    @Override
    public void openCallScreen(User currentUser, User opponentUser, boolean isVideo) {
        CallActivity.start(this, currentUser, opponentUser, isVideo);
    }

    @Override
    public void updateMessage(MessageBaseItem item, MessageHeaderItem headerItem, boolean added) {
        messagesAdapter.handleNewMessage(item, headerItem, added);
        if (isScrollToTop) {
            recycleChatView.scrollToPosition(messagesAdapter.getItemCount() - 1);
        }
    }

    @Override
    public void changeTheme(Color from) {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            extras = new Bundle();
        }
        extras.putInt(EXTRA_CONVERSATION_COLOR, from.getCode());
        ThemeUtils.changeToTheme(this, extras);
    }

    @Override
    public void updateBackground(String s) {
        if (TextUtils.isEmpty(s) || !s.startsWith("gs://")) {
            backgroundImage.setImageDrawable(null);
            return;
        }
        if (isDestroyed()) return;
        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(s);
        GlideApp.with(this)
                .asBitmap()
                .load(storageReference)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(backgroundImage);
    }

    @Override
    public void hideRefreshView() {
        swipeRefreshLayout.setRefreshing(false);
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
        presenter.updateMaskMessages(messages, lastItem, maskStatus);
    }

    private void updateMaskTintColor(boolean isEnable) {
        if (isEnable) {
            int color = ContextCompat.getColor(this, originalConversation.currentColor.getColor());
            tgMarkOut.setButtonTintList(ColorStateList.valueOf(color));
        } else {
            tgMarkOut.setButtonTintList(null);
        }
    }
}
