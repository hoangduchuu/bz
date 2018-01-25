package com.ping.android.presentation.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jakewharton.rxbinding2.widget.RxTextView;
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.CoreActivity;
import com.ping.android.activity.R;
import com.ping.android.activity.SelectContactActivity;
import com.ping.android.adapter.SelectContactAdapter;
import com.ping.android.dagger.loggedin.SearchUserModule;
import com.ping.android.dagger.loggedin.newchat.NewChatComponent;
import com.ping.android.dagger.loggedin.newchat.NewChatModule;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.NewChatPresenter;
import com.ping.android.presentation.presenters.SearchUserPresenter;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.GroupRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.Toaster;
import com.ping.android.view.ChipsEditText;
import com.bzzzchat.cleanarchitecture.UIThread;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class NewChatActivity extends CoreActivity implements View.OnClickListener, NewChatPresenter.NewChatView, SearchUserPresenter.View {
    private final String TAG = NewChatActivity.class.getSimpleName();
    //Views UI
    private RecyclerView recycleChatView;
    private LinearLayoutManager mLinearLayoutManager;
    private TextView tvTitle;
    private ImageView btBack, btSelectContact;
    private Button btSendMessage;
    private ChipsEditText edtTo;
    private EditText edMessage;
    private LinearLayout noResultsView;
    private AVLoadingIndicatorView avi;
    private Button btnDone;

    private User fromUser;

    private ConversationRepository conversationRepository;
    private GroupRepository groupRepository;
    private UserRepository userRepository;

    private TextWatcher textWatcher;
    private SelectContactAdapter adapter;
    private ArrayList<User> selectedUsers = new ArrayList<>();
    private Map<String, User> userList = new HashMap<>();

    private boolean isAddMember = false;

    @Inject
    public NewChatPresenter presenter;
    @Inject
    public SearchUserPresenter searchUserPresenter;
    private NewChatComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_chat);
        getComponent().inject(this);
        presenter.create();
        searchUserPresenter.create();
        isAddMember = getIntent().getBooleanExtra("ADD_MEMBER", false);
        bindViews();
        init();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
        searchUserPresenter.destroy();
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
            case R.id.btn_done:
                handleDonePress();
                break;
        }
    }

    private void handleDonePress() {
        Intent returnIntent = new Intent();
        returnIntent.putParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY, selectedUsers);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
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
        btnDone.setEnabled(selectedUsers.size() > 0);
    }

    private void bindViews() {
        tvTitle = findViewById(R.id.new_chat_to);
        btnDone = findViewById(R.id.btn_done);
        edtTo = findViewById(R.id.edt_to);
        btBack = findViewById(R.id.chat_back);
        btBack.setOnClickListener(this);

        btSelectContact = findViewById(R.id.new_chat_select_contact);
        btSelectContact.setOnClickListener(this);

        recycleChatView = findViewById(R.id.chat_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);

        edMessage = findViewById(R.id.chat_message_tv);
        btSendMessage = findViewById(R.id.chat_send_message_btn);
        btSendMessage.setOnClickListener(this);

        avi = findViewById(R.id.avi);
        noResultsView = findViewById(R.id.no_results);

        LinearLayout bottomLayout = findViewById(R.id.chat_layout_text);
        bottomLayout.setVisibility(isAddMember ? View.GONE : View.VISIBLE);
        tvTitle.setText(isAddMember ? "ADD MEMBER" : "NEW CHAT");
        btnDone.setVisibility(isAddMember ? View.VISIBLE : View.GONE);
        btnDone.setOnClickListener(this);
        btnDone.setEnabled(selectedUsers.size() > 0);

        edMessage.setOnFocusChangeListener((view, b) -> {
            if (b) {
                adapter.updateData(new ArrayList<>());
            }
        });
        registerEvent(RxTextView
                .afterTextChangeEvents(edMessage)
                .subscribe(textViewAfterTextChangeEvent -> checkReadySend()));
        registerEvent(edtTo.chipEventObservable()
                .observeOn(new UIThread().getScheduler())
                .subscribe(chipEvent -> {
                    switch (chipEvent.type) {
                        case SEARCH:
                            if (TextUtils.isEmpty(chipEvent.text)) {
                                adapter.updateData(new ArrayList<>());
                            } else {
                                searchUserPresenter.searchUsers(chipEvent.text);
                            }
                            break;
                        case DELETE:
                            for (User user : selectedUsers) {
                                if (user.getDisplayName().equals(chipEvent.text)) {
                                    selectedUsers.remove(user);
                                    adapter.setSelectPingIDs(getSelectedPingId());
                                    btnDone.setEnabled(selectedUsers.size() > 0);
                                    break;
                                }
                            }
                            break;
                    }
                }));
        checkReadySend();
    }

    private List<String> getSelectedPingId() {
        List<String> selectedPingId = new ArrayList<>();
        for (User user : selectedUsers) {
            selectedPingId.add(user.pingID);
        }
        return selectedPingId;
    }

    private void checkReadySend() {
        if (TextUtils.isEmpty(edMessage.getText().toString().trim()) || selectedUsers.size() <= 0) {
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
                        conversationRepository.updateNotificationSetting(conversationID, fromUser.key, true, null);
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
        double timestamp = System.currentTimeMillis() / 1000d;
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

    @Override
    public void showSearching() {
        avi.post(() -> {
            avi.setVisibility(View.VISIBLE);
            avi.show();
        });
    }

    @Override
    public void hideSearching() {
        avi.post(() -> {
            avi.hide();
            avi.setVisibility(View.GONE);
        });
    }

    @Override
    public void showNoResults() {
        noResultsView.post(() -> noResultsView.setVisibility(View.VISIBLE));
    }

    @Override
    public void hideNoResults() {
        noResultsView.post(() -> noResultsView.setVisibility(View.GONE));
    }

    private void onSendMessage(String conversationID, String msg) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
        intent.putExtra("SEND_MESSAGE", msg);
        startActivity(intent);
        finish();
    }

    public NewChatComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent()
                    .provideNewChatComponent(
                            new NewChatModule(this),
                            new SearchUserModule(this)
                    );
        }
        return component;
    }

    @Override
    public void displaySearchResult(List<User> users) {
        adapter.updateData(new ArrayList<>(users));
    }
}
