package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.ping.android.adapter.SelectContactAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.GroupRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Toaster;
import com.ping.android.view.ChipsEditText;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewChatActivity extends CoreActivity implements View.OnClickListener {
    private final String TAG = NewChatActivity.class.getSimpleName();
    //Views UI
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btBack, btSelectContact;
    private Button btSendMessage;
    private ChipsEditText edtTo;
    private EditText edMessage;
    private User fromUser;

    private ConversationRepository conversationRepository;
    private GroupRepository groupRepository;
    private UserRepository userRepository;

    private TextWatcher textWatcher;
    private SelectContactAdapter adapter;
    private ArrayList<User> selectedUsers = new ArrayList<>();
    private Map<String, User> userList = new HashMap<>();

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

    private void init() {
        userRepository = new UserRepository();
        conversationRepository = new ConversationRepository();
        groupRepository = new GroupRepository();

        fromUser = UserManager.getInstance().getUser();

        adapter = new SelectContactAdapter(this, new ArrayList<>(), (contact, isSelected) -> {
            if (isSelected) {
                selectedUsers.add(contact);
                updateChips();
            } else {
                for (User user : selectedUsers) {
                    if (user.key.equals(contact.key)) {
                        selectedUsers.remove(user);
                        updateChips();
                        break;
                    }
                }
            }
        });
        recycleChatView.setAdapter(adapter);
    }

    private void updateChips() {
        StringBuilder builder = new StringBuilder();
        for (User user : selectedUsers) {
            builder.append(user.getDisplayName());
            builder.append(",");
        }
        edtTo.updateText(builder.toString());
    }

    private void bindViews() {
        edtTo = findViewById(R.id.edt_to);
        btBack = (ImageView) findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);

        btSelectContact = (ImageView) findViewById(R.id.new_chat_select_contact);
        btSelectContact.setOnClickListener(this);

        recycleChatView = (RecyclerView) findViewById(R.id.chat_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);

        edMessage = (EditText) findViewById(R.id.chat_message_tv);
        btSendMessage = (Button) findViewById(R.id.chat_send_message_btn);
        btSendMessage.setOnClickListener(this);

        edMessage.setOnFocusChangeListener((view, b) -> {
            if (b) {
                adapter.updateData(new ArrayList<>());
            }
        });
        edtTo.setListener(new ChipsEditText.ChipsListener() {
            @Override
            public void onSearchText(String text) {
                if (!TextUtils.isEmpty(text)) {
                    searchUsers(text);
                } else {
                    recycleChatView.post(() -> adapter.updateData(new ArrayList<>()));
                }
            }

            @Override
            public void onDeleteChip(String text) {
                for (User user : selectedUsers) {
                    if (user.getDisplayName().equals(text)) {
                        selectedUsers.remove(user);
                        adapter.setSelectPingIDs(getSelectedPingId());
                        break;
                    }
                }
            }
        }, 300);

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

        edMessage.addTextChangedListener(textWatcher);
        checkReadySend();
    }

    private void searchUsers(String text) {
        userList.clear();
        userRepository.searchUsersWithText(text, "ping_id", (error, data) -> {
            if (error == null) {
                DataSnapshot snapshot = (DataSnapshot) data[0];
                handleUsersData(snapshot);
            }
        });
        userRepository.searchUsersWithText(text, "phone", (error, data) -> {
            if (error == null) {
                DataSnapshot snapshot = (DataSnapshot) data[0];
                handleUsersData(snapshot);
            }
        });
    }

    private void handleUsersData(DataSnapshot dataSnapshot) {
        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            if (!userList.containsKey(snapshot.getKey())
                    && !snapshot.getKey().equals(fromUser.key)) {
                User user = new User(snapshot);
                userList.put(snapshot.getKey(), user);
            }
        }

        adapter.setSelectPingIDs(getSelectedPingId());
        adapter.updateData(new ArrayList<>(userList.values()));
    }

    private List<String> getSelectedPingId() {
        List<String> selectedPingId = new ArrayList<>();
        for (User user : selectedUsers) {
            selectedPingId.add(user.pingID);
        }
        return selectedPingId;
    }

    private void checkReadySend() {
        if (StringUtils.isEmpty(edMessage.getText().toString().trim()) || selectedUsers.size() <= 0) {
            btSendMessage.setEnabled(false);
        } else {
            btSendMessage.setEnabled(true);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                updateChips();
                adapter.setSelectPingIDs(getSelectedPingId());
            }
        }
    }

    private void selectContact() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putParcelableArrayListExtra("SELECTED_USERS", selectedUsers);
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void sendNewMessage() {
        if (selectedUsers.size() <= 0) {
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
        if (selectedUsers.size() > 1) {
            // TODO create group then send message
            List<User> toUsers = new ArrayList<>(selectedUsers.size());
            for (User user : selectedUsers) {
                toUsers.add(user);
            }
            toUsers.add(fromUser);
            createGroup(toUsers, edMessage.getText().toString());
        } else {
            User toUser = selectedUsers.get(0);
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
