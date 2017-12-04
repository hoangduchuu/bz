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
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.GroupRepository;
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
    //Views UI
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private MultiAutoCompleteTextView suggestContactView;
    private LinearLayout layoutAddress;
    private ImageView btBack, btSelectContact;
    private Button btSendMessage;
    private TextView tvToDisplay;
    private EditText edMessage;
    private User fromUser;
    private ArrayList<User> allUsers;
    private List<ToInfo> toInfos;
    private ChatAdapter adapter;
    private List<Message> messages;

    private ConversationRepository conversationRepository;
    private GroupRepository groupRepository;

    private TextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        bindViews();
        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.chat_back:
                onBackPressed();
                break;
            case R.id.new_chat_select_contact:
                selectContact();
                break;
            case R.id.chat_send_message_btn:
                sendNewMessage();
                break;
        }
    }

    @Override
    public void onSelect(List<Message> selectMessages) {
    }

    private void init() {
        conversationRepository = new ConversationRepository();
        groupRepository = new GroupRepository();

        messages = new ArrayList<>();
        fromUser = ServiceManager.getInstance().getCurrentUser();
        allUsers = ServiceManager.getInstance().getAllUsers();
        observeChats();
        ContactAutoCompleteAdapter autoCompleteAdapter = new ContactAutoCompleteAdapter(this, R.layout.item_auto_complete_contact, fromUser.friendList);
        suggestContactView.setAdapter(autoCompleteAdapter);
        suggestContactView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
    }

    private void observeChats() {
        adapter = new ChatAdapter(null, fromUser.key, messages, this, this);
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

        edMessage = (EditText) findViewById(R.id.chat_message_tv);
        btSendMessage = (Button) findViewById(R.id.chat_send_message_btn);
        btSendMessage.setOnClickListener(this);

        tvToDisplay = (TextView) findViewById(R.id.new_chat_to);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> selectContacts = data.getStringArrayListExtra("SELECT_CONTACT_PING_IDS");
                addToContact(selectContacts);
            }
        }
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
        if (toInfos.size() > 1) {
            // TODO create group then send message
            List<User> toUsers = new ArrayList<>(toInfos.size());
            for (ToInfo toInfo : toInfos) {
                toUsers.add(toInfo.toUser);
            }
            toUsers.add(fromUser);
            createGroup(toUsers, edMessage.getText().toString());
        } else {
            User toUser = toInfos.get(0).toUser;
            List<User> members = new ArrayList<>();
            members.add(fromUser);
            members.add(toUser);
            String conversationID = fromUser.key.compareTo(toUser.key) > 0 ? fromUser.key + toUser.key : toUser.key + fromUser.key;
            conversationRepository.getConversation(conversationID, (error, data) -> {
                if (error == null) {
                    if (data.length > 0) {
                        Conversation conversation = (Conversation) data[0];
                        conversation.opponentUser = toUser;
                        conversation.members = members;
                        // Turn notifications on for this user
                        conversationRepository.updateNotificationSetting(conversationID, fromUser.key, true);
                        onSendMessage(conversationID, edMessage.getText().toString());
                    } else {
                        Conversation conversation = Conversation.createNewConversation(fromUser.key, toUser.key);
                        conversation.opponentUser = toUser;
                        conversation.members = members;
                        conversationRepository.createConversation(conversationID, conversation, new Callback() {
                            @Override
                            public void complete(Object error, Object... data) {
                                if (error == null) {
                                    //startConversation(conversation);
                                    onSendMessage(conversationID, edMessage.getText().toString());
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    private void createGroup(List<User> toUsers, String msg) {
        List<String> displayNames = new ArrayList<>();
        for (User user : toUsers) {
            displayNames.add(user.getDisplayName());
        }
        displayNames.add(fromUser.getDisplayName());
        double timestamp = System.currentTimeMillis() / 1000L;
        Group group = new Group();
        group.timestamp = timestamp;
        group.groupName = TextUtils.join(", ", displayNames);
        group.groupAvatar = "";

        for (User user : toUsers) {
            group.memberIDs.put(user.key, true);
        }
        String groupKey = groupRepository.generateKey();
        group.key = groupKey;
        groupRepository.createGroup(groupKey, group, (error, data) -> {
            if (error == null) {
                Conversation conversation = Conversation.createNewGroupConversation(fromUser.key, group);
                String conversationKey = conversationRepository.generateKey();
                conversation.key = conversationKey;
                conversationRepository.createConversation(conversationKey, conversation, (error1, data1) -> {
                    if (error1 == null) {
                        groupRepository.updateConversationId(group, conversationKey);
                        group.conversationID = conversationKey;
                        onSendMessage(group.conversationID, msg);
                    }
                });
            }
        });
    }

    private void onSendMessage(String conversationID, String msg) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", conversationID);
        intent.putExtra("SEND_MESSAGE", msg);
        startActivity(intent);
        finish();
    }
}
