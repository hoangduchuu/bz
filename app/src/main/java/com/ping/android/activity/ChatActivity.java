package com.ping.android.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.adapter.ChatAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.NotificationHelper;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.BzzzStorage;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.MessageRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
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

public class ChatActivity extends CoreActivity implements View.OnClickListener, ChatAdapter.ClickListener {
    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private final int REPEAT_INTERVAL = 40;

    public static final String CONVERSATION_KEY = "CONVERSATION_KEY";

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

    private String conversationID, fromUserID, sendNewMsg;
    private User fromUser;
    private Conversation orginalConversation;
    private ChatAdapter adapter;
    private List<Message> messages;
    private ChildEventListener observeChatEvent;
    private ValueEventListener observeTypingEvent, observeStatusEvent;
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
    private BzzzStorage bzzzStorage;
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
    private Button btCancelRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        conversationID = getIntent().getStringExtra("CONVERSATION_ID");
        sendNewMsg = getIntent().getStringExtra("SEND_MESSAGE");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bindViews();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            orginalConversation = bundle.getParcelable(CONVERSATION_KEY);
        }

        init();
//        if (orginalConversation != null) {
//            startChat();
//        }
        initConversationData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibleStatus = true;

        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
        prefs.registerOnSharedPreferenceChangeListener(listener);
        //initConversationData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setButtonsState(0);
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
    protected void onDestroy() {
        super.onDestroy();
        messageRepository.getDatabaseReference().removeEventListener(observeChatEvent);
        if (orginalConversation != null && orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            userRepository.getDatabaseReference().child(orginalConversation.opponentUser.key)
                    .child("loginStatus").removeEventListener(observeStatusEvent);
        }

        conversationRepository.getDatabaseReference().child(conversationID).child("typingIndicator").removeEventListener(observeTypingEvent);
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
                onSendGame();
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
        }
    }

    private void setButtonsState(int selectedViewId) {
        int[] buttonIDs = new int[]{R.id.chat_camera_btn, R.id.chat_emoji_btn, R.id.chat_game_btn, R.id.chat_image_btn
                , R.id.chat_text_btn, R.id.chat_voice_btn, R.id.chat_video_call_btn, R.id.chat_voice_call_btn};
        if (!ArrayUtils.contains(buttonIDs, selectedViewId) && selectedViewId != 0)
        {
            return;
        }
        for (int viewId: buttonIDs) {
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
            boolean isLastMessage = lastMessage != null && lastMessage.key.equals(message.key);
            List<Message> messages = new ArrayList<>(1);
            messages.add(message);
            messageRepository.updateMessageMask(messages, conversationID, fromUser.key, isLastMessage, maskStatus);
        } else if (message.messageType == Constant.MSG_TYPE_IMAGE) {
            List<Message> messages = new ArrayList<>(1);
            messages.add(message);
            messageRepository.updateMessageMask(messages, conversationID, fromUser.key, false, maskStatus);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
//        if (requestCode == Constant.IMAGE_GALLERY_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Uri selectedImageUri = data.getData();
//                if (selectedImageUri != null) {
//                    sendImageFirebase(selectedImageUri, Constant.MSG_TYPE_IMAGE);
//                }
//            }
//        } else if (requestCode == Constant.GAME_GALLERY_REQUEST) {
//            if (resultCode == RESULT_OK) {
//                Uri selectedImageUri = data.getData();
//                if (selectedImageUri != null) {
//                    sendImageFirebase(selectedImageUri, Constant.MSG_TYPE_GAME);
//                }
//            }
//        }
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

    private void initConversationData() {
        conversationRepository.getDatabaseReference().child(conversationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orginalConversation = Conversation.from(dataSnapshot);
                userRepository.initMemberList(orginalConversation.memberIDs, (error, data) -> {
                    if (error == null) {
                        orginalConversation.members = (List<User>) data[0];
                        for (User user : orginalConversation.members) {
                            if (!user.key.equals(fromUserID)) {
                                orginalConversation.opponentUser = user;
                                if (adapter != null)
                                    adapter.setOrginalConversation(orginalConversation);
                                break;
                            }
                        }
                        startChat();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);
        tvChatName = (TextView) findViewById(R.id.chat_person_name);
        tvChatName.setOnClickListener(this);
        tvChatStatus = (TextView) findViewById(R.id.chat_person_status);
        recycleChatView = (RecyclerView) findViewById(R.id.chat_list_view);
        ((SimpleItemAnimator) recycleChatView.getItemAnimator()).setSupportsChangeAnimations(false);
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
        findViewById(R.id.chat_send_message_btn).setOnClickListener(this);

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

    private User getSender(String senderId) {
        for (User user : orginalConversation.members) {
            if (user.key.equals(senderId)) {
                return user;
            }
        }
        return null;
    }

    private void loadMoreChats() {
        Message lastMessage = getLastMessage();
        if (lastMessage == null) return;
        messageRepository.getDatabaseReference()
                .orderByChild("timestamp")
                .endAt(lastMessage.timestamp)
                .limitToLast(Constant.LOAD_MORE_MESSAGE_AMOUNT + 1)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            List<Message> messages = new ArrayList<>();
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                //processAddChild(child);
                                Message message = Message.from(child);
                                if (message.key.equals(lastMessage.key) || ServiceManager.getInstance().getCurrentDeleteStatus(message.deleteStatuses)) {
                                    continue;
                                }
                                message.sender = getSender(message.senderId);
                                messages.add(message);
                            }
                            adapter.appendHistoryItems(messages);
                        }

                        if (dataSnapshot.getChildrenCount() < Constant.LOAD_MORE_MESSAGE_AMOUNT) {
                            isEndOfConvesation = true;
                            updateLoadMoreButtonStatus(false);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void init() {
        bzzzStorage = new BzzzStorage();
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

        observeChatEvent = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                processAddChild(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Message message = Message.from(dataSnapshot);
                if (ServiceManager.getInstance().getCurrentDeleteStatus(message.deleteStatuses)) {
                    adapter.deleteMessage(message.key);
                    return;
                }
                message.sender = getUser(message.senderId);
                updateMessageMarkStatus(message);
                updateMessageStatus(message);
                adapter.addOrUpdate(message);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        observeTypingEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Boolean> typingIndicator = (Map<String, Boolean>) dataSnapshot.getValue();
                boolean isTyping = false;
                if (typingIndicator != null) {
                    for (String key : typingIndicator.keySet()) {
                        if (!key.equals(fromUser.key) && typingIndicator.get(key)) {
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

        observeStatusEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Boolean status = CommonMethod.getBooleanOf(dataSnapshot.getValue());
                if (status == null) status = false;
                if (status)
                    tvChatStatus.setText("Online");
                else
                    tvChatStatus.setText("Offline");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
    }

    private void processAddChild(DataSnapshot dataSnapshot) {
        //Message message = new Message(dataSnapshot);
        Message message = Message.from(dataSnapshot);

        if (message == null || ServiceManager.getInstance().getCurrentDeleteStatus(message.deleteStatuses)) {
            return;
        }

        userRepository.getUser(message.senderId, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                if (error == null) {
                    message.sender = (User) data[0];
                    updateMessageMarkStatus(message);
                    updateMessageStatus(message);
                    adapter.addOrUpdate(message);
                    updateConversationReadStatus();
                    recycleChatView.scrollToPosition(recycleChatView.getAdapter().getItemCount() - 1);
                }
            }
        });
    }

    private void startChat() {
        observeChats();
        observeStatus();
        observeTyping();
        notifyTyping();
        updateConversationReadStatus();
        bindConversationSetting();

        if (orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            tvChatName.setText(orginalConversation.opponentUser.getDisplayName());
        } else {
            btVideoCall.setVisibility(View.GONE);
            btVoiceCall.setVisibility(View.GONE);
            ServiceManager.getInstance().getGroup(orginalConversation.groupID, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    orginalConversation.group = (Group) data[0];
                    tvChatName.setText(orginalConversation.group.groupName);
                }
            });
        }
        if (!TextUtils.isEmpty(sendNewMsg)) {
            onSentMessage(sendNewMsg);
            sendNewMsg = "";
        }
    }

    private void bindConversationSetting() {
        conversationRepository.getMaskOutput(conversationID, fromUser.key, (error, data) -> {
            if (error == null) {
                tgMarkOut.setChecked((boolean) data[0]);
            }
        });
        conversationRepository.getMaskMessageSetting(conversationID, new Callback() {
            @Override
            public void complete(Object error, Object... data) {

            }
        });
    }

    private User getUser(String userId) {
        for (User user : orginalConversation.members) {
            if (user.key.equals(userId)) {
                return user;
            }
        }
        return null;
    }

    private void observeChats() {
        adapter = new ChatAdapter(conversationID, fromUser.key, messages, this, this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);
        recycleChatView.setAdapter(adapter);
        // Load data for first time
        messageRepository.getDatabaseReference().orderByChild("timestamp")
                .limitToLast(Constant.LATEST_RECENT_MESSAGES)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getChildrenCount() > 0) {
                            List<Message> messages = new ArrayList<>();
                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                Message message = Message.from(child);

                                if (message == null
                                        || ServiceManager.getInstance().getCurrentDeleteStatus(message.deleteStatuses)) {
                                    continue;
                                }
                                message.sender = getUser(message.senderId);
                                messages.add(message);
//                                processAddChild(child);
                            }
                            adapter.appendHistoryItems(messages);
                        }

                        if (dataSnapshot.getChildrenCount() < Constant.LATEST_RECENT_MESSAGES) {
                            isEndOfConvesation = true;
                            updateLoadMoreButtonStatus(false);
                        }

                        // Listen for data changes
                        messageRepository.getDatabaseReference()
                                .orderByChild("timestamp")
                                .limitToLast(Constant.LATEST_RECENT_MESSAGES)
                                .addChildEventListener(observeChatEvent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        adapter.setEditMode(isEditMode);
        adapter.setOrginalConversation(orginalConversation);
        conversationRepository.getDatabaseReference().child(conversationID)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Conversation conversation = Conversation.from(dataSnapshot);
                        conversation.members = orginalConversation.members;
                        conversation.opponentUser = orginalConversation.opponentUser;
                        orginalConversation = conversation;
                        if (adapter != null) {
                            adapter.setOrginalConversation(conversation);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        conversationRepository.getDatabaseReference().child(conversationID).child("maskMessages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            Map<String, Boolean> maskMessages = (Map<String, Boolean>) dataSnapshot.getValue();
                            orginalConversation.maskMessages = maskMessages;
                            adapter.setOrginalConversation(orginalConversation);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
        conversationRepository.getDatabaseReference().child(conversationID).child("puzzleMessages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                            Map<String, Boolean> puzzleMessages = (Map<String, Boolean>) dataSnapshot.getValue();
                            orginalConversation.puzzleMessages = puzzleMessages;
                            adapter.setOrginalConversation(orginalConversation);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void observeStatus() {
        if (orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            userRepository.getDatabaseReference().child(orginalConversation.opponentUser.key)
                    .child("loginStatus").addValueEventListener(observeStatusEvent);
        } else {
            tvChatStatus.setVisibility(View.GONE);
        }
    }

    private void observeTyping() {
        conversationRepository.getDatabaseReference()
                .child(conversationID)
                .child("typingIndicator")
                .addValueEventListener(observeTypingEvent);
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
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (!tgMarkOut.isChecked()) {
                    return;
                }
                String newOriginalText = "";
                String displayText = charSequence.toString();
                if (i1 < i2) {
                    newOriginalText += originalText.substring(0, i + i1);
                    newOriginalText += displayText.substring(i + i1, i + i2);
                    if (originalText.length() >= i + i1) {
                        newOriginalText += originalText.substring(i + i1);
                    }
                } else {
                    newOriginalText += originalText.substring(0, i + i2);
                    newOriginalText += originalText.substring(i + i1);
                }
                selectPosition = i + i2;
                originalText = newOriginalText;
            }

            @Override
            public void afterTextChanged(Editable editable) {
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
        edMessage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                setButtonsState(R.id.chat_text_btn);
                return false;
            }
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
        if (orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(Constant.START_ACTIVITY_USER_ID, orginalConversation.opponentUser.key);
            intent.putExtra(UserProfileActivity.CONVERSATION_ID_KEY, conversationID);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, GroupProfileActivity.class);
            intent.putExtra(Constant.START_ACTIVITY_GROUP_ID, orginalConversation.group.key);
            startActivity(intent);
        }
    }

    private void onDeleteMessage() {
        if (isEditAllMode) {
            ServiceManager.getInstance().deleteMessage(conversationID, messages);
        } else {
            ServiceManager.getInstance().deleteMessage(conversationID, adapter.getSelectMessage());
        }
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
        ServiceManager.getInstance().changeMaskOutputConversation(orginalConversation, tgMarkOut.isChecked());
        edMessage.removeTextChangedListener(textWatcher);
        int select = edMessage.getSelectionStart();
        if (tgMarkOut.isChecked()) {
            edMessage.setText(ServiceManager.getInstance().encodeMessage(getApplicationContext(), originalText));
        } else {
            edMessage.setText(originalText);
        }
        try {
            if (select > 0 && select <= originalText.length()) {
                edMessage.setSelection(select);
            } else {
                edMessage.setSelection(originalText.length());

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
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkBlocked()) {
            return;
        }

        edMessage.setText(null);
        double timestamp = System.currentTimeMillis() / 1000L;
        Message message = Message.createTextMessage(text, fromUser.key, fromUser.pingID,
                timestamp, getStatuses(), getMessageMarkStatuses(), getMessageDeleteStatuses());

        Conversation conversation = new Conversation(orginalConversation.conversationType, Constant.MSG_TYPE_TEXT,
                text, orginalConversation.groupID, fromUserID, getMemberIDs(), getMessageMarkStatuses(),
                getMessageReadStatuses(), getMessageDeleteStatuses(), timestamp, orginalConversation);
        conversation.members = orginalConversation.members;
        String messageKey = messageRepository.generateKey();
        messageRepository.updateMessage(messageKey, message);

        conversationRepository.updateConversation(conversationID, conversation, null);
        NotificationHelper.getInstance().sendNotificationForConversation(conversation, message);
    }

    Message cacheMessage = null;

    private void onSendCamera() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkBlocked()) {
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
                        cacheMessage = sendImageMessage(Constant.IMAGE_PREFIX, Constant.IMAGE_PREFIX + file.getAbsolutePath(), Constant.MSG_TYPE_IMAGE);
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
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkBlocked()) {
            return;
        }

        imagePickerHelper = ImagePickerHelper.from(this)
                .setCrop(false)
                .setScale(true)
                .setGenerateThumbnail(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {
                        cacheMessage = sendImageMessage(Constant.IMAGE_PREFIX, Constant.IMAGE_PREFIX + file.getAbsolutePath(), Constant.MSG_TYPE_IMAGE);
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

    private void onSendGame() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkBlocked()) {
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
                        sendGameFirebase(file);
                    }
                });
        imagePickerHelper.openPicker();
//        Intent intent = new Intent();
//        intent.setType("image/*");
//        intent.setAction(Intent.ACTION_GET_CONTENT);
//        startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.GAME_GALLERY_REQUEST);
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
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkBlocked()) {
            return;
        }

        double timestamp = System.currentTimeMillis() / 1000L;

        File audioFile = new File(currentOutFile);
        String audioName = audioFile.getName();
        String pathAudio = fromUser.key + "/" + timestamp + "/" + audioName;
        bzzzStorage.uploadFile(pathAudio, audioFile, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                if (error == null) {
                    String downloadUrl = (String) data[0];
                    Message message = Message.createAudioMessage(downloadUrl,
                            fromUser.key, fromUser.pingID, timestamp, getStatuses(), null, getMessageDeleteStatuses());

                    Conversation conversation = new Conversation(orginalConversation.conversationType, Constant.MSG_TYPE_VOICE,
                            downloadUrl, orginalConversation.groupID, fromUserID, getMemberIDs(), null, getMessageReadStatuses(),
                            getMessageDeleteStatuses(), timestamp, orginalConversation);
                    conversation.members = orginalConversation.members;
                    String messageKey = messageRepository.generateKey();
                    message.key = messageKey;
                    //Create or Update Conversation
                    messageRepository.updateMessage(messageKey, message);
                    conversationRepository.updateConversation(conversationID, conversation, fromUserID);
                    NotificationHelper.getInstance().sendNotificationForConversation(conversation, message);
                }
            }
        });
        //btSendRecord.setEnabled(false);
        visualizerView.clear();
        setRecordMode(false);
        //setButtonsState(0);
    }

    private void onVoiceCall() {
        CallActivity.start(this, orginalConversation.opponentUser, false);
    }

    private void onVideoCall() {
        CallActivity.start(this, orginalConversation.opponentUser, true);
    }

    private Map<String, Boolean> getMemberIDs() {
        Map<String, Boolean> memberIDs = new HashMap<>();
        for (User toUser : orginalConversation.members) {
            memberIDs.put(toUser.key, true);
        }
        return memberIDs;
    }

    private Map<String, Boolean> getMessageMarkStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        if (orginalConversation.maskMessages != null) {
            markStatuses.putAll(orginalConversation.maskMessages);
        }
        markStatuses.put(fromUser.key, tgMarkOut.isChecked());
        return markStatuses;
    }

    private Map<String, Boolean> getImageMarkStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        if (orginalConversation.puzzleMessages != null) {
            markStatuses.putAll(orginalConversation.puzzleMessages);
        }
        markStatuses.put(fromUser.key, tgMarkOut.isChecked());
        return markStatuses;
    }

    private Map<String, Boolean> getMessageReadStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        for (User toUser : orginalConversation.members) {
            markStatuses.put(toUser.key, false);
        }
        markStatuses.put(fromUser.key, true);
        return markStatuses;
    }

    private Map<String, Boolean> getMessageDeleteStatuses() {
        Map<String, Boolean> deleteStatuses = new HashMap<>();
        for (User toUser : orginalConversation.members) {
            deleteStatuses.put(toUser.key, false);
        }
        deleteStatuses.put(fromUser.key, false);
        return deleteStatuses;
    }

    private Map<String, Long> getStatuses() {
        Map<String, Long> deleteStatuses = new HashMap<>();
        for (User toUser : orginalConversation.members) {
            deleteStatuses.put(toUser.key, Constant.MESSAGE_STATUS_SENT);
        }
        deleteStatuses.put(fromUser.key, Constant.MESSAGE_STATUS_SENT);
        return deleteStatuses;
    }

    private void sendImageFirebase(File file, File thumbnail) {
        if (cacheMessage == null) {
            cacheMessage = sendImageMessage("PPhtotoMessageIdentifier", "PPhtotoMessageIdentifier", Constant.MSG_TYPE_IMAGE);
        }
        //adapter.addOrUpdate(message);
        // upload thumbnail first first
        bzzzStorage.uploadImageForConversation(conversationID, thumbnail, (error, data) -> {
            if (error != null) {
                // TODO handle error when uploading image
                return;
            }
            String thumbnailUrl = (String) data[0];
            messageRepository.updateThumbnailUrl(cacheMessage.key, thumbnailUrl);
            // Upload image
            bzzzStorage.uploadImageForConversation(conversationID, file, (error1, data1) -> {
                String imageUrl = thumbnailUrl;
                if (error1 == null) {
                    imageUrl = (String) data1[0];
                }
                messageRepository.updatePhotoUrl(cacheMessage.key, imageUrl);
                //sendImageMessage(imageUrl, thumbnailUrl, Constant.MSG_TYPE_IMAGE);
            });
        });
    }

    private void sendGameFirebase(File file) {
        bzzzStorage.uploadImageForConversation(conversationID, file, (error1, data1) -> {
            if (error1 == null) {
                String imageUrl = (String) data1[0];
                sendImageMessage(imageUrl, imageUrl, Constant.MSG_TYPE_GAME);
            }
        });
    }

    private Message sendImageMessage(String imageUrl, String thumbnailUrl, int msgType) {
        String messageKey = messageRepository.generateKey();
        double timestamp = System.currentTimeMillis() / 1000;
        Message message = null;
        if (msgType == Constant.MSG_TYPE_IMAGE) {
            message = Message.createImageMessage(imageUrl, thumbnailUrl,
                    fromUser.key, fromUser.pingID, timestamp, getStatuses(), getImageMarkStatuses(), getMessageDeleteStatuses());
        } else if (msgType == Constant.MSG_TYPE_GAME) {
            message = Message.createGameMessage(imageUrl,
                    fromUser.key, fromUser.pingID, timestamp, getStatuses(), getImageMarkStatuses(), getMessageDeleteStatuses());
        }
        if (message == null) throw new NullPointerException("Message must not be null " + msgType);
        message.key = messageKey;

        Conversation conversation = new Conversation(orginalConversation.conversationType, msgType, imageUrl,
                orginalConversation.groupID, fromUserID, getMemberIDs(), getImageMarkStatuses(),
                getMessageReadStatuses(), getMessageDeleteStatuses(), timestamp, orginalConversation);
        conversation.members = orginalConversation.members;
        //Create or Update Conversation
        messageRepository.updateMessage(messageKey, message);
        conversationRepository.updateConversation(conversationID, conversation, fromUserID);
        NotificationHelper.getInstance().sendNotificationForConversation(conversation, message);
        return message;
    }

    private void updateMessageStatus(Message message) {
        if (message.senderId.equals(fromUserID)) {
            return;
        }
        long status = CommonMethod.getCurrentStatus(fromUserID, message.status);
        if (status == Constant.MESSAGE_STATUS_SENT) {
            status = Constant.MESSAGE_STATUS_DELIVERED;
            messageRepository.updateMessageStatus(message.key, orginalConversation.members, status);
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
                    (ServiceManager.getInstance().getMaskSetting(orginalConversation.maskMessages)));
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
        conversationRepository.updateTypingIndicatorForUser(conversationID, fromUserID, typing);
    }

    private boolean checkBlocked() {
        boolean isBlocked = false;
        if (orginalConversation.members.size() == 2) {
            for (User toUser : orginalConversation.members) {
                if (toUser.key != fromUser.key && ServiceManager.getInstance().isBlock(toUser.key)) {
                    String username = ServiceManager.getInstance().getFirstName(toUser);
                    Toaster.shortToast(String.format(getApplicationContext().getString(R.string.msg_account_msg_blocked), username, username));
                    isBlocked = true;
                }
            }
        }
        return isBlocked;
    }

    private boolean checkMessageBlocked(User toUser) {
        boolean isBlocked = false;

        if (toUser.key != fromUser.key && ServiceManager.getInstance().isBlockBy(toUser)) {
            isBlocked = true;
        }
        return isBlocked;
    }

    private void updateLoadMoreButtonStatus(boolean isShow) {
        btLoadMoreChat.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }
}
