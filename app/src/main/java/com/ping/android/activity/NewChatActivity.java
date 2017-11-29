package com.ping.android.activity;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.ping.android.adapter.ChatAdapter;
import com.ping.android.adapter.ContactAutoCompleteAdapter;
import com.ping.android.form.ToInfo;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewChatActivity extends CoreActivity implements View.OnClickListener, ChatAdapter.ClickListener {

    private final String TAG = NewChatActivity.class.getSimpleName();
    private final int REPEAT_INTERVAL = 40;
    private FirebaseAuth auth;
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    //Views UI
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private MultiAutoCompleteTextView suggestContactView;
    private RelativeLayout layoutVoice;
    private LinearLayout layoutText, layoutAddress;
    private ImageView btBack, btSelectContact;
    private Button btSendMessage;
    private TextView tvToDisplay;
    private ImageView btSendRecord;
    private ToggleButton tgRecord;
    private EditText edMessage;
    private User fromUser;
    private ArrayList<User> allUsers;
    private List<ToInfo> toInfos;
    private ChatAdapter adapter;
    private List<Message> messages;
    private String RECORDING_PATH;

    private String currentOutFile;
    private MediaRecorder myAudioRecorder;
    private boolean isRecording;
    private RecorderVisualizerView visualizerView;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        bindViews();
        init();
//        onTyping();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_back:
                exitChat();
                break;
            case R.id.new_chat_select_contact:
                selectContact();
                break;
            case R.id.chat_text_btn:
                setMessageMode(Constant.MESSAGE_TYPE.TEXT);
                break;
            case R.id.chat_image_btn:
                sendImage();
                break;
            case R.id.chat_voice_btn:
                setMessageMode(Constant.MESSAGE_TYPE.VOICE);
                break;
            case R.id.chat_game_btn:
                sendGame();
                break;
            case R.id.chat_send_message_btn:
                sendNewMessage();
                break;
            case R.id.chat_start_record:
                if (tgRecord.isChecked()) {
                    startRecord();
                } else {
                    stopRecord();
                }
                break;

            case R.id.chat_cancel_record:
                stopRecord();
                break;
            case R.id.chat_send_record:
                sendNewRecord();
                break;
        }
    }

    @Override
    public void onSelect(List<Message> selectMessages) {
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();
        storage = FirebaseStorage.getInstance();

        RECORDING_PATH = this.getExternalFilesDir(null).getAbsolutePath();
        //RECORDING_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
        messages = new ArrayList<>();
        fromUser = ServiceManager.getInstance().getCurrentUser();
        allUsers = ServiceManager.getInstance().getAllUsers();
        observeChats();
        ContactAutoCompleteAdapter autoCompleteAdapter = new ContactAutoCompleteAdapter(this, R.layout.item_auto_complete_contact, fromUser.friendList);
        suggestContactView.setAdapter(autoCompleteAdapter);
        suggestContactView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private void observeChats() {
        adapter = new ChatAdapter(null, auth.getCurrentUser().getUid(), messages, this, this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);
        recycleChatView.setAdapter(adapter);
        mLinearLayoutManager.setStackFromEnd(true);
    }

    private void bindViews() {
        btBack = (ImageView) findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);

        btSelectContact = (ImageView) findViewById(R.id.new_chat_select_contact);
        btSelectContact.setOnClickListener(this);

        recycleChatView = (RecyclerView) findViewById(R.id.chat_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);

        suggestContactView = (MultiAutoCompleteTextView) findViewById(R.id.new_chat_suggest_view);


        layoutAddress = (LinearLayout) findViewById(R.id.new_chat_address_layout);
        layoutText = (LinearLayout) findViewById(R.id.chat_layout_text);
        layoutVoice = (RelativeLayout) findViewById(R.id.chat_layout_voice);

        edMessage = (EditText) findViewById(R.id.chat_message_tv);
        btSendMessage = (Button) findViewById(R.id.chat_send_message_btn);
        btSendMessage.setOnClickListener(this);

        visualizerView = (RecorderVisualizerView) findViewById(R.id.visualizer);
        tgRecord = (ToggleButton) findViewById(R.id.chat_start_record);
        tgRecord.setOnClickListener(this);
        btSendRecord = (ImageView) findViewById(R.id.chat_send_record);
        btSendRecord.setOnClickListener(this);

        tvToDisplay = (TextView) findViewById(R.id.new_chat_to);

        setMessageMode(Constant.MESSAGE_TYPE.TEXT);

        textWatcher = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkReadySend();
            }
        };

        suggestContactView.addTextChangedListener(textWatcher);
        edMessage.addTextChangedListener(textWatcher);
        checkReadySend();
    }

    private void checkReadySend() {
        if (StringUtils.isEmpty(edMessage.getText().toString().trim()) || StringUtils.isEmpty(suggestContactView.getText().toString().trim())) {
            btSendMessage.setEnabled(false);
        } else {
            btSendMessage.setEnabled(true);
        }
    }

    private void setMessageMode(Constant.MESSAGE_TYPE type) {
        if (type == Constant.MESSAGE_TYPE.TEXT) {
            layoutText.setVisibility(View.VISIBLE);
            layoutVoice.setVisibility(View.GONE);
        } else if (type == Constant.MESSAGE_TYPE.VOICE) {
            layoutText.setVisibility(View.GONE);
            layoutVoice.setVisibility(View.VISIBLE);
            btSendRecord.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constant.IMAGE_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendNewImageFirebase(selectedImageUri, Constant.MSG_TYPE_IMAGE);
                }
            }
        } else if (requestCode == Constant.GAME_GALLERY_REQUEST) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    sendNewImageFirebase(selectedImageUri, Constant.MSG_TYPE_GAME);
                }
            }
        } else if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> selectContacts = data.getStringArrayListExtra("SELECT_CONTACT_PING_IDS");
                addToContact(selectContacts);
            }
        }
    }

    private void exitChat() {
        finish();
    }

    private void selectContact() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putExtra("SELECTED_ID", suggestContactView.getText().toString().trim());
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void addToContact(ArrayList<String> selectContacts) {
        String addContact = TextUtils.join(", ", selectContacts);
        String currentTo = suggestContactView.getText().toString().trim();

        if (StringUtils.isEmpty(currentTo)) {
            currentTo = addContact;
        } else if (currentTo.endsWith(",")) {
            currentTo = currentTo + " " + addContact;
        } else {
            currentTo = currentTo + ", " + addContact;
        }

        if (StringUtils.isNotEmpty(addContact)) {
            currentTo += ", ";
        }
        suggestContactView.setText(currentTo);
        suggestContactView.setSelection(currentTo.length());
    }

    private void getToContact() {
        if (toInfos != null) {
            return;
        }
        List<User> toUsers = new ArrayList<>();
        List<String> toUserPingID = Arrays.asList(suggestContactView.getText().toString().trim().split(","));
        List<String> unknownPingID = new ArrayList<>();
        List<String> blockedPingID = new ArrayList<>();
        for (String id : toUserPingID) {
            id = id.trim();
            User contact = getUserByAnyID(id);
            if (contact == null) {
                unknownPingID.add(id);
            } else {
                toUsers.add(contact);
                if(ServiceManager.getInstance().isBlockBy(contact)){
                    blockedPingID.add(id);
                }
            }
        }

        if (!CollectionUtils.isEmpty(unknownPingID) || !CollectionUtils.isEmpty(blockedPingID)) {
            String message = getString(R.string.validate_invalid_user);
            Toaster.shortToast(message);
            return;
        }

        double timestamp = System.currentTimeMillis() / 1000L;
        toInfos = new ArrayList<>();
        ArrayList<String> displayNames = new ArrayList<>();
        for (User contact : toUsers) {
            displayNames.add(contact.getDisplayName());
            ToInfo toInfo = new ToInfo();
            toInfo.toUser = contact;
            toInfo.timestamp = timestamp;
            toInfos.add(toInfo);
        }
        tvToDisplay.setText(TextUtils.join(", ", displayNames));
        layoutAddress.setVisibility(View.GONE);
        btSelectContact.setEnabled(false);
        suggestContactView.setEnabled(false);
    }

    private User getUserByAnyID(String id) {
        for (User contact : allUsers) {
            if (contact.pingID.equals(id)) {
                return contact;
            }
            if (contact.email.equals(id)) {
                return contact;
            }
            if (StringUtils.isNotEmpty(contact.phone) && contact.phone.equals(id)) {
                return contact;
            }
        }
        return null;
    }

    private void continueExistingConversation() {
        if (toInfos != null && toInfos.size() == 1) {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("CONVERSATION_ID", toInfos.get(0).conversationID);
            startActivity(intent);
            finish();
        }
    }

    private void sendNewMessage() {
        if (StringUtils.isEmpty(suggestContactView.getText().toString())) {
            return;
        }

        String text = edMessage.getText().toString();
        if (StringUtils.isEmpty(text)) {
            Toaster.shortToast("Please enter message.");
            return;
        }

        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toaster.shortToast("Please check network connection.");
            return;
        }

        getToContact();
        if (toInfos == null) {
            return;
        }
        double timestamp = toInfos.get(0).timestamp;
        Message displayMessage = Message.createTextMessage(text, fromUser.key, fromUser.pingID,
                timestamp, getStatuses(toInfos.get(0).toUser), getMessageMarkStatuses(toInfos.get(0).toUser),
                getMessageDeleteStatuses(toInfos.get(0).toUser));
        adapter.addMessage(displayMessage);
        edMessage.setText(null);
        for (ToInfo toInfo : toInfos) {
            ServiceManager.getInstance().createConversationIDForPVPChat(fromUser.key, toInfo.toUser.key, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    toInfo.conversationID = data[0].toString();
                    sentMessage(text, toInfo);
                    continueExistingConversation();
                }
            });
        }
    }

    private void sentMessage(String text, ToInfo toInfo) {
        Message message = Message.createTextMessage(text, fromUser.key, fromUser.pingID,
                toInfo.timestamp, getStatuses(toInfo.toUser), getMessageMarkStatuses(toInfo.toUser),
                getMessageDeleteStatuses(toInfo.toUser));
        Conversation conversation = new Conversation(Constant.CONVERSATION_TYPE_INDIVIDUAL, Constant.MSG_TYPE_TEXT,
                text, "", fromUser.key, getMemberIDs(toInfo.toUser), getMessageMarkStatuses(toInfo.toUser),
                getMessageReadStatuses(toInfo.toUser), getMessageDeleteStatuses(toInfo.toUser), toInfo.timestamp, null);

        //Create or Update Conversation
        mDatabase.child("conversations").child(toInfo.conversationID).setValue(conversation.toMap());

        //Create fragment_message on Message by ConversationID was created before
        String messageKey = mDatabase.child("messages").child(toInfo.conversationID).push().getKey();
        toInfo.messageID = messageKey;
        mDatabase.child("messages").child(toInfo.conversationID).child(messageKey).setValue(message);

        //Update Conversation to Users (from-user, to-user)
        mDatabase.child("users").child(fromUser.key).child("conversations").child(toInfo.conversationID).setValue(conversation);
        mDatabase.child("users").child(toInfo.toUser.key).child("conversations").child(toInfo.conversationID).setValue(conversation);
    }

    private Map<String, Boolean> getMemberIDs(User toUser) {
        Map<String, Boolean> memberIDs = new HashMap<>();
        memberIDs.put(fromUser.key, true);
        memberIDs.put(toUser.key, true);
        return memberIDs;
    }

    private Map<String, Boolean> getMessageMarkStatuses(User toUser) {
        Map<String, Boolean> markStatuses = new HashMap<>();
        markStatuses.put(fromUser.key, false);
        //TODO update out mapping flag
        markStatuses.put(toUser.key, true);
        return markStatuses;
    }

    private Map<String, Boolean> getMessageReadStatuses(User toUser) {
        Map<String, Boolean> markStatuses = new HashMap<>();
        markStatuses.put(fromUser.key, true);
        markStatuses.put(toUser.key, false);
        return markStatuses;
    }

    private Map<String, Boolean> getMessageDeleteStatuses(User toUser) {
        Map<String, Boolean> deleteStatuses = new HashMap<>();
        deleteStatuses.put(fromUser.key, false);
        deleteStatuses.put(toUser.key, false);
        return deleteStatuses;
    }

    private Map<String, Boolean> getImageMarkStatuses(User toUser) {
        Map<String, Boolean> markStatuses = new HashMap<>();
        markStatuses.put(fromUser.key, false);
        //TODO update out mapping flag
        markStatuses.put(toUser.key, true);
        return markStatuses;
    }

    private Map<String, Long> getStatuses(User toUser) {
        Map<String, Long> markStatuses = new HashMap<>();
        markStatuses.put(fromUser.key, Constant.MESSAGE_STATUS_SENT);
        markStatuses.put(toUser.key, Constant.MESSAGE_STATUS_SENT);
        return markStatuses;
    }

    private void sendImage() {
        if (StringUtils.isEmpty(suggestContactView.getText().toString())) {
            Toaster.shortToast("No recipients.");
            return;
        }

        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toaster.shortToast("Please check network connection.");
            return;
        }

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.IMAGE_GALLERY_REQUEST);
    }

    private void sendGame() {
        if (StringUtils.isEmpty(suggestContactView.getText().toString())) {
            Toaster.shortToast("Please input to information.");
            return;
        }
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toaster.shortToast("Please check network connection.");
            return;
        }
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Get photo"), Constant.GAME_GALLERY_REQUEST);
    }

    private void sendNewImageFirebase(final Uri uri, int msgType) {
        getToContact();
        if (toInfos == null) {
            return;
        }
        final String imageName = getFileNameFromURI(uri);

        double timestamp = toInfos.get(0).timestamp;

        String pathFirebaseImage = fromUser.key + "/" + timestamp + "/" + imageName;
        String pathLocalImage = getExternalFilesDir(null).getAbsolutePath() + File.separator
                + fromUser.key + File.separator + timestamp;
        CommonMethod.createFolder(pathLocalImage);
        pathLocalImage = pathLocalImage + File.separator + imageName;

        prepareImage(uri, pathLocalImage);

        StorageReference photoRef = storage.getReferenceFromUrl(Constant.URL_STORAGE_REFERENCE).child(pathFirebaseImage);
        UploadTask uploadTask = photoRef.putFile(Uri.fromFile(new File(pathLocalImage)));
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                double timestamp = toInfos.get(0).timestamp;
                String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + taskSnapshot.getMetadata().getPath();
                Message messageDisplay = null;
                if (msgType == Constant.MSG_TYPE_IMAGE) {
                    messageDisplay = Message.createImageMessage(downloadUrl, downloadUrl,
                            fromUser.key, fromUser.pingID, timestamp, getStatuses(toInfos.get(0).toUser),
                            getImageMarkStatuses(toInfos.get(0).toUser), getMessageDeleteStatuses(toInfos.get(0).toUser));
                } else if (msgType == Constant.MSG_TYPE_GAME) {
                    messageDisplay = Message.createGameMessage(downloadUrl,
                            fromUser.key, fromUser.pingID, timestamp, getStatuses(toInfos.get(0).toUser),
                            getImageMarkStatuses(toInfos.get(0).toUser), getMessageDeleteStatuses(toInfos.get(0).toUser));
                }
                adapter.addMessage(messageDisplay);

                for (ToInfo toInfo : toInfos) {
                    ServiceManager.getInstance().createConversationIDForPVPChat(fromUser.key, toInfo.toUser.key, new Callback() {
                        @Override
                        public void complete(Object error, Object... data) {
                            toInfo.conversationID = data[0].toString();
                            sendImageFirebase(downloadUrl, msgType, toInfo);
                            continueExistingConversation();
                        }
                    });
                }
            }
        });
    }

    private void sendImageFirebase(String downloadUrl, int msgType, ToInfo toInfo) {
        Message message = null;
        if (msgType == Constant.MSG_TYPE_IMAGE) {
            message = Message.createImageMessage(downloadUrl, downloadUrl,
                    fromUser.key, fromUser.pingID, toInfo.timestamp, getStatuses(toInfo.toUser),
                    getImageMarkStatuses(toInfo.toUser), getMessageDeleteStatuses(toInfo.toUser));
        } else if (msgType == Constant.MSG_TYPE_GAME) {
            message = Message.createGameMessage(downloadUrl,
                    fromUser.key, fromUser.pingID, toInfo.timestamp, getStatuses(toInfo.toUser),
                    getImageMarkStatuses(toInfo.toUser), getMessageDeleteStatuses(toInfo.toUser));
        }
        Conversation conversation = new Conversation(Constant.CONVERSATION_TYPE_INDIVIDUAL, msgType, downloadUrl, "", fromUser.key,
                getMemberIDs(toInfo.toUser), getImageMarkStatuses(toInfo.toUser),
                getMessageReadStatuses(toInfo.toUser), getMessageDeleteStatuses(toInfo.toUser), toInfo.timestamp, null);

        //Create or Update Conversation
        mDatabase.child("conversations").child(toInfo.conversationID).setValue(conversation.toMap());

        //Create fragment_message on Message by ConversationID was created before
        String messageKey = mDatabase.child("messages").child(toInfo.conversationID).push().getKey();
        toInfo.messageID = messageKey;
        mDatabase.child("messages").child(toInfo.conversationID).child(messageKey).setValue(message);
        mDatabase.child("users").child(fromUser.key).child("conversations").child(toInfo.conversationID).setValue(conversation);
        mDatabase.child("users").child(toInfo.toUser.key).child("conversations").child(toInfo.conversationID).setValue(conversation);
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

    private void startRecord() {
        visualizerView.clear();
        btSendRecord.setEnabled(false);
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
            btSendRecord.setEnabled(false);
            isRecording = true;
            handler.post(updateVisualizer);
        } catch (Exception e) {
            e.printStackTrace();
            btSendRecord.setEnabled(true);
            isRecording = false;
        }
    }

    private void stopRecord() {
        try {
            if (null != myAudioRecorder) {
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
            }
            btSendRecord.setEnabled(true);
        } catch (Exception e) {
            Log.e(e);
        }
        btSendRecord.setEnabled(true);
        isRecording = false;
        handler.removeCallbacks(updateVisualizer);
    }

    private void sendNewRecord() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        getToContact();
        if (toInfos == null) {
            return;
        }
        double timestamp = toInfos.get(0).timestamp;
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
                String downloadUrl = Constant.URL_STORAGE_REFERENCE + "/" + taskSnapshot.getMetadata().getPath();
                Message messageDisplay = Message.createAudioMessage(downloadUrl,
                        fromUser.key, fromUser.pingID, toInfos.get(0).timestamp, getStatuses(toInfos.get(0).toUser), null
                        , getMessageDeleteStatuses(toInfos.get(0).toUser));
                adapter.addMessage(messageDisplay);

                for (ToInfo toInfo : toInfos) {
                    ServiceManager.getInstance().createConversationIDForPVPChat(fromUser.key, toInfo.toUser.key, new Callback() {
                        @Override
                        public void complete(Object error, Object... data) {
                            toInfo.conversationID = data[0].toString();
                            sendRecord(downloadUrl, toInfo);
                            continueExistingConversation();
                        }
                    });
                }
            }
        });

        btSendRecord.setEnabled(false);
        visualizerView.clear();
    }

    private void sendRecord(String downloadUrl, ToInfo toInfo) {

        Message message = Message.createAudioMessage(downloadUrl,
                fromUser.key, fromUser.pingID, toInfo.timestamp, getStatuses(toInfo.toUser), null,
                getMessageDeleteStatuses(toInfo.toUser));

        Conversation conversation = new Conversation(Constant.CONVERSATION_TYPE_INDIVIDUAL, Constant.MSG_TYPE_VOICE,
                downloadUrl, "", fromUser.key, getMemberIDs(toInfo.toUser), null, getMessageReadStatuses(toInfo.toUser),
                getMessageDeleteStatuses(toInfo.toUser), toInfo.timestamp, null);

        //Create or Update Conversation
        mDatabase.child("conversations").child(toInfo.conversationID).setValue(conversation.toMap());

        //Create or Update Member by ConversationID was created before

        //mDatabase.child("members").child(toInfo.conversationID).child(fromUser.key).setValue(true);
        //mDatabase.child("members").child(toInfo.conversationID).child(toInfo.toUser.key).setValue(true);

        //Create fragment_message on Message by ConversationID was created before
        String messageKey = mDatabase.child("messages").child(toInfo.conversationID).push().getKey();
        toInfo.messageID = messageKey;
        mDatabase.child("messages").child(toInfo.conversationID).child(messageKey).setValue(message);
        mDatabase.child("users").child(fromUser.key).child("conversations").child(toInfo.conversationID).setValue(conversation);
        mDatabase.child("users").child(toInfo.toUser.key).child("conversations").child(toInfo.conversationID).setValue(conversation);
    }

}
