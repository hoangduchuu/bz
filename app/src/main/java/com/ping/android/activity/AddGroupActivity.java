package com.ping.android.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.ping.android.adapter.ContactAutoCompleteAdapter;
import com.ping.android.adapter.SelectContactAdapter;
import com.ping.android.fragment.LoadingDialog;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.Toaster;
import com.ping.android.utils.UiUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddGroupActivity extends CoreActivity implements View.OnClickListener, SelectContactAdapter.ClickListener {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;

    private LinearLayoutManager mLinearLayoutManager;
    private EditText etGroupName, edMessage;
    private MultiAutoCompleteTextView suggestContactView;
    private Button btSave, btSendMessage;
    private ImageView btBack;
    private ImageView groupAvatar;

    private User fromUser;
    private ArrayList<User> allUsers;

    private TextWatcher textWatcher;

    private ImagePickerHelper imagePickerHelper;
    private File groupProfileImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);
        bindViews();
        init();
    }

    private void bindViews() {
        etGroupName = (EditText) findViewById(R.id.new_group_name);
        suggestContactView = (MultiAutoCompleteTextView) findViewById(R.id.new_group_suggest_view);
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

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);

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
        etGroupName.addTextChangedListener(textWatcher);
        checkReadySend();
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference();

        fromUser = ServiceManager.getInstance().getCurrentUser();
        allUsers = ServiceManager.getInstance().getAllUsers();
        ContactAutoCompleteAdapter autoCompleteAdapter = new ContactAutoCompleteAdapter(this, R.layout.item_auto_complete_contact, fromUser.friendList);
        suggestContactView.setAdapter(autoCompleteAdapter);
        suggestContactView.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        suggestContactView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String groupsStr = suggestContactView.getText().toString().trim();
                List<String> toUserPingID = Arrays.asList(suggestContactView.getText().toString().trim().split("\\s*,\\s*"));
            }
        });
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
                ArrayList<String> selectContacts = data.getStringArrayListExtra("SELECT_CONTACT_PING_IDS");
                addToContact(selectContacts);
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
                || StringUtils.isEmpty(suggestContactView.getText().toString().trim())
                || StringUtils.isEmpty(etGroupName.getText().toString().trim()) ){
            btSendMessage.setEnabled(false);
        } else {
            btSendMessage.setEnabled(true);
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

        List<User> toUsers = new ArrayList<>();
        List<String> toUserPingID = Arrays.asList(suggestContactView.getText().toString().trim().split(","));
        List<String> unknownPingID = new ArrayList<>();
        List<String> toUserID = new ArrayList<>();
        for (String id : toUserPingID) {
            id = id.trim();
            if (StringUtils.isEmpty(id)) {
                continue;
            }
            User contact = getUserByAnyID(id);
            if (contact == null) {
                unknownPingID.add(id);
            } else {
                toUsers.add(contact);
                toUserID.add(contact.key);
            }
        }

        if (!CollectionUtils.isEmpty(unknownPingID)) {
            String message = getString(R.string.validate_invalid_user);
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            return;
        }

        if (CollectionUtils.isEmpty(toUserID)) {
            Toaster.shortToast("Please input members of group");
            return;
        }

        toUsers.add(fromUser);
        toUserID.add(fromUser.key);
        String groupKey = mDatabase.child("groups").push().getKey();

        showLoading();
        if (groupProfileImage != null) {
            ServiceManager.getInstance().uploadGroupAvatar(groupKey, groupProfileImage, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    if (error == null) {
                        String profileImage = (String) data[0];
                        createGroup(toUsers, groupKey, msg, profileImage);
                    }
                }
            });
        } else {
            createGroup(toUsers, groupKey, msg, "");
        }

    }

    private void createGroup(List<User> toUsers, String groupKey, String msg, String profileImage) {
        Double timestamp = System.currentTimeMillis() / 1000D;
        Group group = new Group();
        group.timestamp = timestamp;
        group.groupName = etGroupName.getText().toString().trim();
        group.groupAvatar = profileImage;

        for (User user : toUsers) {
            group.memberIDs.put(user.key, true);
        }
        mDatabase.child("groups").child(groupKey).setValue(group.toMap());

        for (User user : toUsers) {
            mDatabase.child("users").child(user.key).child("groups").child(groupKey).setValue(group.toMap());
        }

        group.key = groupKey;
        ServiceManager.getInstance().createConversationForGroupChat(fromUser.key, group, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                group.conversationID = data[0].toString();
                onSendMessage(group, msg);
                hideLoading();
                finish();
            }
        });
    }

    private void onCancelGroup() {
        finish();
    }

    private void openPicker() {
        String profileFileFolder = getExternalFilesDir(null).getAbsolutePath() + File.separator +
                "profile" + File.separator + fromUser.key;
        long timestamp = System.currentTimeMillis() / 1000L;
        String profileFileName = "" + timestamp + "-" + fromUser.key + ".png";
        String profileFilePath = profileFileFolder + File.separator + profileFileName;
        imagePickerHelper = ImagePickerHelper.from(this)
                .setFilePath(profileFilePath)
                .setCallback((error, data) -> {
                    if (error == null) {
                        groupProfileImage = (File) data[0];
                        UiUtils.displayProfileAvatar(groupAvatar, groupProfileImage);
                    }
                });
        imagePickerHelper.openPicker();
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

    private void onSendMessage(Group group, String msg) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("CONVERSATION_ID", group.conversationID);
        intent.putExtra("SEND_MESSAGE", msg);
        startActivity(intent);
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

    @Override
    public void onSelect(User contact, Boolean isSelected) {
        String groupsStr = suggestContactView.getText().toString().trim();
        if (isSelected) {
            if (groupsStr.endsWith(",")) {
                suggestContactView.setText(groupsStr + " " + contact.pingID + ", ");
            } else if (StringUtils.isNotEmpty(groupsStr)) {
                suggestContactView.setText(groupsStr + ", " + contact.pingID + ", ");
            } else {
                suggestContactView.setText(contact.pingID + ", ");
            }
        } else {
            List<String> toUserPingID = Arrays.asList(groupsStr.split("\\s*,\\s*"));
            toUserPingID.remove(contact.pingID);
            String newGroupsStr = StringUtils.join(toUserPingID, ", ") + ", ";
            suggestContactView.setText(newGroupsStr);
        }
    }
}
