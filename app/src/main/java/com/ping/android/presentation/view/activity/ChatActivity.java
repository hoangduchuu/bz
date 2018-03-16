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
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import com.bzzzchat.flexibleadapter.FlexibleItem;
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
import com.ping.android.dagger.loggedin.chat.ChatComponent;
import com.ping.android.dagger.loggedin.chat.ChatModule;
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
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.BadgeHelper;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.KeyboardHelpers;
import com.ping.android.utils.Log;
import com.ping.android.utils.Toaster;
import com.ping.android.view.RecorderVisualizerView;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiPopup;

import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

public class ChatActivity extends CoreActivity implements ChatPresenter.View, HasComponent<ChatComponent>,
        View.OnClickListener, ChatMessageAdapter.ChatMessageListener {
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
    private EmojiEditText edMessage;
    private TextView tvChatName, tvNewMsgCount;
    private Button btnSend;
    private Button btCancelRecord;
    private BottomSheetDialog chatGameMenu;
    private BottomSheetDialog messageActions;

    private LinearLayout copyContainer;

    private String conversationID;
    private Conversation originalConversation;
    private ChatMessageAdapter messagesAdapter;

    private String RECORDING_PATH, currentOutFile;
    private MediaRecorder myAudioRecorder;
    private boolean isRecording = false, isTyping = false, isEditMode = false;
    private RecorderVisualizerView visualizerView;
    private AtomicBoolean isSettingStackFromEnd = new AtomicBoolean(false);
    private String originalText = "";
    private int selectPosition = 0;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private TextWatcher textWatcher;

    private ImagePickerHelper imagePickerHelper;

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
    private MessageBaseItem selectedMessage;
    private BadgeHelper badgeHelper;

    @Inject
    ChatPresenter presenter;
    ChatComponent component;
    private boolean isVisible = false;

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
        recycleChatView.setLayoutManager(mLinearLayoutManager);
        messagesAdapter = new ChatMessageAdapter();
        messagesAdapter.setMessageListener(this);
        recycleChatView.setAdapter(messagesAdapter);
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
        messagesAdapter.destroy();
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
        btBack = findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);
        tvChatName = findViewById(R.id.chat_person_name);
        tvChatName.setOnClickListener(this);
        tvChatStatus = findViewById(R.id.chat_person_status);
        recycleChatView = findViewById(R.id.chat_list_view);
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

        btVoiceCall = findViewById(R.id.chat_voice_call_btn);
        btVoiceCall.setOnClickListener(this);
        btVideoCall = findViewById(R.id.chat_video_call_btn);
        btVideoCall.setOnClickListener(this);

        btMask = findViewById(R.id.chat_mask);
        btMask.setOnClickListener(this);
        btUnMask = findViewById(R.id.chat_unmask);
        btUnMask.setOnClickListener(this);
        btDelete = findViewById(R.id.chat_delete);
        btDelete.setOnClickListener(this);
        btEdit = findViewById(R.id.chat_edit);
        btEdit.setOnClickListener(this);
        btCancelEdit = findViewById(R.id.chat_cancel_edit);
        btCancelEdit.setOnClickListener(this);
        btLoadMoreChat = findViewById(R.id.load_more);
        btLoadMoreChat.setOnClickListener(this);

        layoutText = findViewById(R.id.chat_layout_text);
        layoutVoice = findViewById(R.id.chat_layout_voice);
        layoutBottomMenu = findViewById(R.id.chat_bottom_menu);

        edMessage = findViewById(R.id.chat_message_tv);
        //edMessage.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);


        visualizerView = findViewById(R.id.visualizer);
        btCancelRecord = findViewById(R.id.chat_cancel_record);
        btCancelRecord.setOnClickListener(this);
        tbRecord = findViewById(R.id.chat_start_record);
        tbRecord.setOnClickListener(this);
        btSendRecord = findViewById(R.id.chat_send_record);
        btSendRecord.setOnClickListener(this);

        tgMarkOut = findViewById(R.id.chat_tgl_outcoming);
        tgMarkOut.setOnClickListener(this);

        tvNewMsgCount = findViewById(R.id.chat_new_message_count);
        layoutMsgType = findViewById(R.id.chat_layout_msg_type);
        //emoji
        findViewById(R.id.chat_header_center).setOnClickListener(this);
        emojiPopup = EmojiPopup.Builder.fromRootView(findViewById(R.id.contentRoot)).build(edMessage);
        btEmoji = findViewById(R.id.chat_emoji_btn);
        btEmoji.setOnClickListener(this);

//        LinearLayout llBottomSheet = findViewById(R.id.bottom_sheet);
//        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);
//        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);


        View messageActionsView = getLayoutInflater().inflate(R.layout.bottom_sheet_message_actions, null);
        copyContainer = messageActionsView.findViewById(R.id.btn_copy);
        copyContainer.setOnClickListener(this);
        messageActionsView.findViewById(R.id.btn_delete).setOnClickListener(this);

        IconicsDrawable drawable = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_content_copy)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, R.color.colorAccent));
        IconicsImageView copyImageView = messageActionsView.findViewById(R.id.img_copy);
        copyImageView.setIcon(drawable);

        IconicsDrawable deleteDrawable = new IconicsDrawable(this, CommunityMaterial.Icon.cmd_delete)
                .sizeDp(24)
                .color(ContextCompat.getColor(this, R.color.colorAccent));
        IconicsImageView deleteImageView = messageActionsView.findViewById(R.id.img_delete);
        deleteImageView.setIcon(deleteDrawable);
        messageActions = new BottomSheetDialog(this);
        messageActions.setContentView(messageActionsView);
        messageActions.setOnDismissListener(dialog -> hideSelectedMessage());

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
        }
        return null;
    }

    private void init() {
        RECORDING_PATH = this.getExternalFilesDir(null).getAbsolutePath();
        //RECORDING_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

        listener = (prefs, key) -> {
            if (key.equals(Constant.PREFS_KEY_MESSAGE_COUNT)) {
                int messageCount = prefs.getInt(key, 0);
                updateMessageCount(messageCount);
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
        //bindConversationSetting();
        BadgesHelper.getInstance().removeCurrentUserBadges(conversationID);
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
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(getApplicationContext(), "Please input message", Toast.LENGTH_SHORT).show();
            return;
        }
        edMessage.setText(null);
        presenter.sendTextMessage(text, tgMarkOut.isChecked());
    }

    private void onSendCamera() {
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

    private void updateLoadMoreButtonStatus(boolean isShow) {
        btLoadMoreChat.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    @Override
    public void updateConversation(Conversation conv) {
        this.originalConversation = conv;
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
    public void addNewMessage(MessageBaseItem data) {
        //adapter.addOrUpdate(data);
        data.setEditMode(isEditMode);
        messagesAdapter.addOrUpdate(data);
        if (!isEditMode && isVisible) {
            recycleChatView.scrollToPosition(recycleChatView.getAdapter().getItemCount() - 1);
        }
    }

    @Override
    public void removeMessage(Message data) {
        //adapter.deleteMessage(data.key);
        messagesAdapter.deleteMessage(data.key);
        //this.updateConversationLastMessage();
    }

    @Override
    public void updateMessage(MessageBaseItem data) {
        messagesAdapter.addOrUpdate(data);
    }

    @Override
    public void updateLastMessages(List<MessageBaseItem> messages, boolean canLoadMore) {
        messagesAdapter.appendHistoryItems(messages);
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
    public void addCacheMessage(MessageBaseItem message) {
        messagesAdapter.addOrUpdate(message);
        //recycleChatView.scrollToPosition(recycleChatView.getAdapter().getItemCount() - 1);
    }

    @Override
    public void switchOffEditMode() {
        hideLoading();
        isEditMode = false;
        onUpdateEditMode();
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
}
