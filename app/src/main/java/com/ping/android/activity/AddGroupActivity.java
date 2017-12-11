package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
import com.ping.android.fragment.LoadingDialog;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.BzzzStorage;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.GroupRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.Log;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UiUtils;
import com.ping.android.view.ChipsEditText;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddGroupActivity extends CoreActivity implements View.OnClickListener {
    private LinearLayoutManager mLinearLayoutManager;
    private EditText etGroupName, edMessage;
    private Button btSave, btSendMessage;
    private ImageView btBack;
    private ImageView groupAvatar;
    private ChipsEditText edtTo;
    private RecyclerView recycleChatView;

    private User fromUser;

    private TextWatcher textWatcher;

    private BzzzStorage bzzzStorage;
    private GroupRepository groupRepository;
    private ConversationRepository conversationRepository;
    private UserRepository userRepository;

    private ImagePickerHelper imagePickerHelper;
    private File groupProfileImage = null;

    private SelectContactAdapter adapter;
    private ArrayList<User> selectedUsers = new ArrayList<>();
    private Map<String, User> userList = new HashMap<>();
    private String textToSearch = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        bindViews();
        init();
    }

    private void bindViews() {
        edtTo = findViewById(R.id.edt_to);
        etGroupName = (EditText) findViewById(R.id.new_group_name);
        btBack = (ImageView) findViewById(R.id.new_group_back);
        btBack.setOnClickListener(this);
        btSave = (Button) findViewById(R.id.new_group_save);
        btSave.setOnClickListener(this);
        groupAvatar = findViewById(R.id.profile_image);
        groupAvatar.setOnClickListener(this);

        edMessage = (EditText) findViewById(R.id.new_group_message_tv);
        btSendMessage = (Button) findViewById(R.id.new_group_send_message_btn);
        btSendMessage.setOnClickListener(this);

        findViewById(R.id.new_group_select_contact).setOnClickListener(this);

        recycleChatView = (RecyclerView) findViewById(R.id.chat_list_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        recycleChatView.setLayoutManager(mLinearLayoutManager);

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
        etGroupName.addTextChangedListener(textWatcher);
        checkReadySend();
    }

    private void searchUsers(String text) {
        textToSearch = text;
        userList.clear();
        Callback searchCallback = (error, data) -> {
            if (error == null && text.equals(textToSearch)) {
                DataSnapshot snapshot = (DataSnapshot) data[0];
                handleUsersData(snapshot);
            }
        };
        localSearch(text);
        userRepository.matchUserWithText(text, "first_name", searchCallback);
    }

    private void localSearch(String text) {
        for (User user : fromUser.friendList) {
            if (CommonMethod.isContain(CommonMethod.getSearchString(user), text)) {
                if (!userList.containsKey(user.key)) {
                    userList.put(user.key, user);
                }
            }
        }
        recycleChatView.post(() -> {
            adapter.setSelectPingIDs(getSelectedPingId());
            adapter.updateData(new ArrayList<>(userList.values()));
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

    private void init() {
        userRepository = new UserRepository();
        bzzzStorage = new BzzzStorage();
        groupRepository = new GroupRepository();
        conversationRepository = new ConversationRepository();
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.new_group_save:
                onCreateGroup("");
                break;
            case R.id.new_group_back:
                onCancelGroup();
                break;
            case R.id.new_group_select_contact:
                selectContact();
                break;
            case R.id.new_group_send_message_btn:
                onCreateGroup(edMessage.getText().toString());
                break;
            case R.id.profile_image:
                openPicker();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                updateChips();
                adapter.setSelectPingIDs(getSelectedPingId());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void checkReadySend() {
        if (StringUtils.isEmpty(edMessage.getText().toString().trim())
                || selectedUsers.size() <= 0
                || TextUtils.isEmpty(etGroupName.getText().toString().trim()) ){
            btSendMessage.setEnabled(false);
        } else {
            btSendMessage.setEnabled(true);
        }
    }

    private void selectContact() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putParcelableArrayListExtra("SELECTED_USERS", selectedUsers);
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void onCreateGroup(String msg) {
        String groupNames = etGroupName.getText().toString().trim();
        if (StringUtils.isEmpty(groupNames)) {
            Toaster.shortToast("Name this group.");
            return;
        }
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toaster.shortToast("Please check network connection.");
            return;
        }

        List<User> toUsers = new ArrayList<>(selectedUsers);
//        List<String> unknownPingID = new ArrayList<>();
//        List<String> toUserID = new ArrayList<>();
//        for (String id : toUserPingID) {
//            id = id.trim();
//            if (StringUtils.isEmpty(id)) {
//                continue;
//            }
//            User contact = getUserByAnyID(id);
//            if (contact == null) {
//                unknownPingID.add(id);
//            } else {
//                toUsers.add(contact);
//                toUserID.add(contact.key);
//            }
//        }
//
//        if (!CollectionUtils.isEmpty(unknownPingID)) {
//            String message = getString(R.string.validate_invalid_user);
//            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if (CollectionUtils.isEmpty(toUserID)) {
//            Toaster.shortToast("Please input members of group");
//            return;
//        }

        toUsers.add(fromUser);
        String groupKey = groupRepository.generateKey();

        showLoading();
        if (groupProfileImage != null) {
            bzzzStorage.uploadGroupAvatar(groupKey, groupProfileImage, (error, data) -> {
                String profileImage = "";
                if (error == null) {
                    profileImage = (String) data[0];
                }
                createGroup(toUsers, groupKey, msg, profileImage);
            });
        } else {
            createGroup(toUsers, groupKey, msg, "");
        }

    }

    private void createGroup(List<User> toUsers, String groupKey, String msg, String profileImage) {
        double timestamp = System.currentTimeMillis() / 1000L;
        Group group = new Group();
        group.timestamp = timestamp;
        group.groupName = etGroupName.getText().toString().trim();
        group.groupAvatar = profileImage;

        for (User user : toUsers) {
            group.memberIDs.put(user.key, true);
        }
        group.key = groupKey;
        groupRepository.createGroup(groupKey, group, (error, data) -> {
            if (error == null) {
                Conversation conversation = Conversation.createNewGroupConversation(fromUser.key, group);
                String conversationKey = conversationRepository.generateKey();
                conversation.key = conversationKey;
                conversationRepository.createConversation(conversationKey, conversation, (error1, data1) -> {
                    hideLoading();
                    if (error1 == null) {
                        groupRepository.updateConversationId(group, conversationKey);
                        group.conversationID = conversationKey;
                        onSendMessage(group, msg);
                    }
                });
            } else {
                hideLoading();
            }
        });
    }

    private void onCancelGroup() {
        finish();
    }

    private void openPicker() {
        String profileFileFolder = getExternalFilesDir(null).getAbsolutePath() + File.separator +
                "profile" + File.separator + fromUser.key;
        double timestamp = System.currentTimeMillis() / 1000L;
        String profileFileName = "" + timestamp + "-" + fromUser.key + ".png";
        String profileFilePath = profileFileFolder + File.separator + profileFileName;
        imagePickerHelper = ImagePickerHelper.from(this)
                .setFilePath(profileFilePath)
                .setCrop(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {

                    }

                    @Override
                    public void onFinalImage(File... files) {
                        groupProfileImage = files[0];
                        UiUtils.displayProfileAvatar(groupAvatar, groupProfileImage);
                    }
                });
        imagePickerHelper.openPicker();
    }

    private void onSendMessage(Group group, String msg) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", group.conversationID);
        intent.putExtra("SEND_MESSAGE", msg);
        startActivity(intent);
        finish();
    }

    DialogFragment loadingDialog;

    private void showLoading() {
        loadingDialog = new LoadingDialog();
        loadingDialog.show(getSupportFragmentManager(), "LOADING");
    }

    private void hideLoading() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }
}
