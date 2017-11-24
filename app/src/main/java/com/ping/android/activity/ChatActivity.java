package com.ping.android.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ping.android.adapter.ChatAdapter;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.NotificationService;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Log;
import com.ping.android.utils.Toaster;
import com.ping.android.view.RecorderVisualizerView;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends CoreActivity implements View.OnClickListener, ChatAdapter.ClickListener {

    private final String TAG = "Ping: " + this.getClass().getSimpleName();
    private final int REPEAT_INTERVAL = 40;
    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;

    //Views UI
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private RelativeLayout layoutVoice, layoutBottomMenu;
    private LinearLayout layoutText, layoutMsgType;
    private ImageView btBack;
    private Button btSendRecord;
    private Button tbRecord;
    private CheckBox tgMarkOut;
    private TextView tvChatStatus;
    private Button btMask, btUnMask, btDelete, btEdit, btCancelEdit;
    private ImageButton btVoiceCall, btVideoCall;
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

    private String originalText = "";
    private int selectPosition = 0;
    private boolean visibleStatus;

    private SharedPreferences prefs;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;
    private TextWatcher textWatcher;

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

        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("CONVERSATION_ID", conversationID);
        startService(intent);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        bindViews();
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("CONVERSATION_ID", conversationID);
        startService(intent);

        visibleStatus = true;

        int messageCount = prefs.getInt(Constant.PREFS_KEY_MESSAGE_COUNT, 0);
        updateMessageCount(messageCount);
        prefs.registerOnSharedPreferenceChangeListener(listener);
        initConversationData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        isTyping = false;
        updateConversationTyping(false);

        Intent intent = new Intent(this, NotificationService.class);
        intent.putExtra("CONVERSATION_ID", "");
        startService(intent);

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
        mDatabase.child("messages").child(conversationID).removeEventListener(observeChatEvent);
        if (orginalConversation != null && orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            mDatabase.child("users").child(orginalConversation.opponentUser.key).child("loginStatus").removeEventListener(observeStatusEvent);
        }

//        mDatabase.child("conversations").child(conversationID).child("typingIndicator").removeEventListener(observeTypingEvent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
        }
    }

    @Override
    public void onSelect(List<Message> selectMessages) {
        updateEditAllMode();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constant.IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendImageFirebase(selectedImageUri, Constant.MSG_TYPE_IMAGE);
                }
            }
        } else if (requestCode == Constant.GAME_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendImageFirebase(selectedImageUri, Constant.MSG_TYPE_GAME);
                }
            }
        }
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);
        tvChatName = (TextView) findViewById(R.id.chat_person_name);
        tvChatName.setOnClickListener(this);
        tvChatStatus = (TextView) findViewById(R.id.chat_person_status);
        recycleChatView = (RecyclerView) findViewById(R.id.chat_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        findViewById(R.id.chat_person_name).setOnClickListener(this);

        findViewById(R.id.chat_text_btn).setOnClickListener(this);
        findViewById(R.id.chat_image_btn).setOnClickListener(this);
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

        findViewById(R.id.chat_header_center).setOnClickListener(this);

        onSetMessageMode(Constant.MESSAGE_TYPE.TEXT);
        onUpdateEditMode();
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        storage = FirebaseStorage.getInstance();

        fromUserID = auth.getCurrentUser().getUid();

        RECORDING_PATH = this.getExternalFilesDir(null).getAbsolutePath();
        //RECORDING_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

        messages = new ArrayList<>();
        fromUser = ServiceManager.getInstance().getCurrentUser();

        initConversationData();

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
                Message message = new Message(dataSnapshot);
                if (ServiceManager.getInstance().getCurrentDeleteStatus(message.deleteStatuses)) {
                    return;
                }
                ServiceManager.getInstance().getUser(message.senderId, new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        if (error == null) {
                            message.sender = (User) data[0];
                            updateMessageMarkStatus(message);
                            updateMessageStatus(message);
                            adapter.addOrUpdate(message);
                            recycleChatView.scrollToPosition(adapter.getItemCount() - 1);
                            updateConversationReadStatus();
                        }
                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Message message = new Message(dataSnapshot);
                if (ServiceManager.getInstance().getCurrentDeleteStatus(message.deleteStatuses)) {
                    adapter.deleteMessage(message.key);
                    return;
                }
                ServiceManager.getInstance().getUser(message.senderId, new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        if (error == null) {
                            message.sender = (User) data[0];
                            updateMessageStatus(message);
                            adapter.addOrUpdate(message);
                            updateConversationReadStatus();
                        }
                    }
                });
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
//                Log.i(TAG, String.format("conversations.%s.%s addValueEventListener onDataChange key: %s",
//                        conversationID, "typingIndicator", dataSnapshot.getKey()));
//                Map<String, Boolean> typingIndicator =  (Map<String, Boolean>) dataSnapshot.getValue();
//                if (typingIndicator != null && typingIndicator.containsKey(toUserID)) {
//                    Boolean typing = typingIndicator.get(toUserID);
//                    showTyping(typing);
//                }
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
                if(status)
                    tvChatStatus.setText("Online");
                else
                    tvChatStatus.setText("Offline");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

    }

    private void initConversationData() {
        mDatabase.child("users").child(fromUserID).child("conversations").child(conversationID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orginalConversation = new Conversation(dataSnapshot);
                ServiceManager.getInstance().initMembers(orginalConversation.memberIDs, new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        orginalConversation.members = (List<User>) data[0];
                        for (User user : orginalConversation.members) {
                            if (!user.key.equals(fromUserID)) {
                                orginalConversation.opponentUser = user;
                                if(adapter != null)
                                    adapter.setOrginalConversation(orginalConversation);
                                break;
                            }
                        }
                    }
                });
                startChat();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
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
        if (!StringUtils.isEmpty(sendNewMsg)) {
            onSentMessage(sendNewMsg);
            sendNewMsg = "";
        }
    }

    private void bindConversationSetting() {
        tgMarkOut.setChecked(ServiceManager.getInstance().getMaskOutputSetting(orginalConversation.maskOutputs));
    }

    private void observeChats() {
        adapter = new ChatAdapter(conversationID, auth.getCurrentUser().getUid(), messages, this, this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);
        recycleChatView.setAdapter(adapter);
        mLinearLayoutManager.setStackFromEnd(true);
        mDatabase.child("messages").child(conversationID).addChildEventListener(observeChatEvent);
        adapter.setEditMode(isEditMode);
        adapter.setOrginalConversation(orginalConversation);
    }

    private void observeStatus() {
        if (orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            mDatabase.child("users").child(orginalConversation.opponentUser.key).child("loginStatus").addValueEventListener(observeStatusEvent);
        }
        else {
            tvChatStatus.setVisibility(View.GONE);
        }
    }

    private void observeTyping() {
//        mDatabase.child("conversations").child(conversationID).child("typingIndicator").addValueEventListener(observeTypingEvent);
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

                if (StringUtils.isNotEmpty(edMessage.getText()) && !isTyping) {
                    isTyping = true;
                    updateConversationTyping(isTyping);
                } else if (StringUtils.isEmpty(edMessage.getText()) && isTyping) {
                    isTyping = false;
                    updateConversationTyping(isTyping);
                }
            }
        };
        edMessage.addTextChangedListener(textWatcher);
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
            btMask.setText("MASK ALL");
            btUnMask.setText("UNMASK ALL");
        } else {
            btDelete.setEnabled(true);
            btMask.setText("MASK");
            btUnMask.setText("UNMASK");
        }
    }

    private void onOpenProfile() {
        if (orginalConversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            Intent intent = new Intent(this, UserProfileActivity.class);
            intent.putExtra(Constant.START_ACTIVITY_USER_ID, orginalConversation.opponentUser.key);
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
            ServiceManager.getInstance().updateMessageMark(conversationID, messages, mask);
        } else {
            ServiceManager.getInstance().updateMessageMark(conversationID, adapter.getSelectMessage(), mask);
        }
    }

    private void onSetMessageMode(Constant.MESSAGE_TYPE type) {
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

        if(type != Constant.MESSAGE_TYPE.TEXT) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private void setRecordMode(boolean isRecording) {
        if(isRecording) {
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
        if (StringUtils.isEmpty(text)) {
            Toast.makeText(getApplicationContext(), "Please input message", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if( checkBlocked()){
            return;
        }

        edMessage.setText(null);
        Double timestamp = System.currentTimeMillis() / 1000D;
        Message message = Message.createTextMessage(text, fromUser.key, fromUser.pingID,
                timestamp, getStatuses(), getMessageMarkStatuses(), getMessageDeleteStatuses());

        Conversation conversation = new Conversation(orginalConversation.conversationType, Constant.MSG_TYPE_TEXT,
                text, orginalConversation.groupID, fromUserID, getMemberIDs(), getMessageMarkStatuses(),
                getMessageReadStatuses(), getMessageDeleteStatuses(), timestamp, orginalConversation);

        //Create or Update Conversation
        mDatabase.child("conversations").child(conversationID).updateChildren(conversation.toMap());


        //Create fragment_message on Message by ConversationID was created before
        String messageKey = mDatabase.child("messages").child(conversationID).push().getKey();
        mDatabase.child("messages").child(conversationID).child(messageKey).setValue(message);

        for (User toUser : orginalConversation.members) {
            if (checkMessageBlocked(toUser)) continue;
            mDatabase.child("users").child(toUser.key).child("conversations").child(conversationID).updateChildren(conversation.toMap());
        }
    }

    private void onSendImage() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if( checkBlocked()){
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.IMAGE_GALLERY_REQUEST);
    }

    private void onSendGame() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if( checkBlocked()){
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.GAME_GALLERY_REQUEST);
    }

    private void onStartRecord() {
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
        if( checkBlocked()){
            return;
        }

        Double timestamp = System.currentTimeMillis() / 1000D;

        //Create fragment_message on Message by ConversationID was created before
        String messageKey = mDatabase.child("messages").child(conversationID).push().getKey();

        File audioFile = new File(currentOutFile);
        String audioName = audioFile.getName();
        String pathAudio = fromUser.key + "/" + timestamp + "/" + audioName;
        StorageReference photoRef = storage.getReferenceFromUrl(Constant.URL_STORAGE_REFERENCE).child(pathAudio);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(audioFile));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageMetadata metadata = taskSnapshot.getMetadata();
                if (metadata != null) {
                    String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + metadata.getPath();
                    Message message = Message.createAudioMessage(downloadUrl,
                            fromUser.key, fromUser.pingID, timestamp, getStatuses(), null, getMessageDeleteStatuses());

                    Conversation conversation = new Conversation(orginalConversation.conversationType, Constant.MSG_TYPE_VOICE,
                            downloadUrl, orginalConversation.groupID, fromUserID, getMemberIDs(), null, getMessageReadStatuses(),
                            getMessageDeleteStatuses(), timestamp, orginalConversation);

                    //Create or Update Conversation
                    mDatabase.child("conversations").child(conversationID).updateChildren(conversation.toMap());
                    mDatabase.child("messages").child(conversationID).child(messageKey).updateChildren(message.toMap());
                    for (User toUser : orginalConversation.members) {
                        if (checkMessageBlocked(toUser)) continue;
                        mDatabase.child("users").child(toUser.key).child("conversations").child(conversationID).updateChildren(conversation.toMap());
                    }
                }
            }
        });

        //btSendRecord.setEnabled(false);
        visualizerView.clear();
        setRecordMode(false);
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
        //TODO update out mapping flag
//        for (User toUser : orginalConversation.members) {
//            markStatuses.put(toUser.key, true);
//        }
        markStatuses.put(fromUser.key, tgMarkOut.isChecked());
        return markStatuses;
    }

    private Map<String, Boolean> getImageMarkStatuses() {
        Map<String, Boolean> markStatuses = new HashMap<>();
        //TODO update out mapping flag
        for (User toUser : orginalConversation.members) {
            markStatuses.put(toUser.key, true);
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

    private void sendImageFirebase(final Uri uri, Long msgType) {
        final String imageName = getFileNameFromURI(uri);
        Double timestamp = System.currentTimeMillis() / 1000D;

        String pathFirebaseImage = fromUser.key + "/" + timestamp + "/" + imageName;
        String pathLocalImage = this.getExternalFilesDir(null).getAbsolutePath() + File.separator
                + fromUser.key + File.separator + timestamp;
        CommonMethod.createFolder(pathLocalImage);
        pathLocalImage = pathLocalImage + File.separator + imageName;
        prepareImage(uri, pathLocalImage);

        //Create fragment_message on Message by ConversationID was created before
        String messageKey = mDatabase.child("messages").child(conversationID).push().getKey();
        StorageReference photoRef = storage.getReferenceFromUrl(Constant.URL_STORAGE_REFERENCE).child(pathFirebaseImage);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(new File(pathLocalImage)));

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + taskSnapshot.getMetadata().getPath();
                Message message = null;
                if (msgType == Constant.MSG_TYPE_IMAGE) {
                    message = Message.createImageMessage(downloadUrl, downloadUrl,
                            fromUser.key, fromUser.pingID, timestamp, getStatuses(), getImageMarkStatuses(), getMessageDeleteStatuses());
                } else if (msgType == Constant.MSG_TYPE_GAME) {
                    message = Message.createGameMessage(downloadUrl,
                            fromUser.key, fromUser.pingID, timestamp, getStatuses(), getImageMarkStatuses(), getMessageDeleteStatuses());
                }

                Conversation conversation = new Conversation(orginalConversation.conversationType, msgType, downloadUrl,
                        orginalConversation.groupID, fromUserID, getMemberIDs(), getImageMarkStatuses(),
                        getMessageReadStatuses(), getMessageDeleteStatuses(), timestamp, orginalConversation);

                //Create or Update Conversation
                mDatabase.child("conversations").child(conversationID).updateChildren(conversation.toMap());
                mDatabase.child("messages").child(conversationID).child(messageKey).updateChildren(message.toMap());
                for (User toUser : orginalConversation.members) {
                    if (checkMessageBlocked(toUser)) continue;
                    mDatabase.child("users").child(toUser.key).child("conversations").child(conversationID).updateChildren(conversation.toMap());
                }
            }
        });
    }

    private void prepareImage(Uri uri, String localPath) {
        FileOutputStream out = null;
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth, newHeight;
            out = new FileOutputStream(localPath);
            if (width > Constant.IMAGE_LIMIT_WIDTH) {
                float scale = 1f * Constant.IMAGE_LIMIT_WIDTH / width;
                newWidth = (int) (scale * width);
                newHeight = (int) (scale * height);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } else {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
        } catch (Exception e) {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e1) {
            }
        }
    }

    private String getFileNameFromURI(Uri uri) {
        String uriString = uri.toString();
        File file = new File(uriString);
        String imageName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = this.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    imageName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            imageName = file.getName();
        }
        return imageName;
    }

    private void updateMessageStatus(Message message) {
        if (message.senderId.equals(fromUserID)) {
            return;
        }
        Long status = ServiceManager.getInstance().getCurrentStatus(message.status);
        if (status == Constant.MESSAGE_STATUS_SENT) {
            status = Constant.MESSAGE_STATUS_DELIVERED;
            for(String userId : orginalConversation.memberIDs.keySet()) {
                mDatabase.child("messages").child(conversationID).child(message.key).child("status").
                        child(userId).setValue(status);
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
        mDatabase.child("conversations").child(conversationID).child("readStatuses").child(fromUser.key).setValue(true);
        mDatabase.child("users").child(fromUser.key).child("conversations").child(conversationID).child("readStatuses").child(fromUser.key).setValue(true);
    }

    private void updateMessageMarkStatus(Message message) {
        if(message.markStatuses == null || !message.markStatuses.containsKey(fromUser.key)) {
            ServiceManager.getInstance().updateMarkStatus(conversationID, message.key,
                    (ServiceManager.getInstance().getMaskSetting(orginalConversation.maskMessages)));
        }
    }

    private void showTyping(Boolean typing) {
        adapter.showTyping(typing);
        recycleChatView.scrollToPosition(adapter.getItemCount() - 1);
    }

    private void updateConversationTyping(Boolean typing) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        mDatabase.child("conversations").child(conversationID).child("typingIndicator").child(fromUserID).setValue(typing);
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
}
