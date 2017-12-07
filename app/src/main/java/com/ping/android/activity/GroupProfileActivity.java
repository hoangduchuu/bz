package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.ping.android.adapter.GroupProfileAdapter;
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
import com.ping.android.ultility.Constant;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroupProfileActivity extends CoreActivity implements View.OnClickListener, GroupProfileAdapter.ClickListener{

    private ImageView groupProfile;
    private EditText groupName;
    private RecyclerView rvListMember;
    private LinearLayoutManager mLinearLayoutManager;
    private Switch swNotification;
    private Switch swMask;
    private Switch cbPuzzle;

    private String groupID;
    private User currentUser;
    private Group group;
    private GroupProfileAdapter adapter;
    private Conversation conversation;
    private ImagePickerHelper imagePickerHelper;
    private File groupProfileImage;
    private BzzzStorage bzzzStorage;
    private GroupRepository groupRepository;
    private ConversationRepository conversationRepository;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        groupID = getIntent().getStringExtra(Constant.START_ACTIVITY_GROUP_ID);
        currentUser = UserManager.getInstance().getUser();

        bindViews();
        bzzzStorage = new BzzzStorage();
        groupRepository = new GroupRepository();
        conversationRepository = new ConversationRepository();
        userRepository = new UserRepository();

        groupRepository.loadGroup(groupID, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                group = (Group) data[0];
                initConversationData();
            }
        });
    }

    @Override
    protected void onPause() {
        updateGroupName();
        super.onPause();
    }

    private void initConversationData() {
        if (TextUtils.isEmpty(group.conversationID)) {
            Conversation conversation = Conversation.createNewGroupConversation(currentUser.key, group);
            String conversationKey = conversationRepository.generateKey();
            conversation.key = conversationKey;
            conversationRepository.createConversation(conversationKey, conversation, new Callback() {
                @Override
                public void complete(Object error, Object... data) {
                    groupRepository.updateConversationId(group, conversationKey);
                    group.conversationID = conversationKey;
                    initConversationSetting();
                }
            });
        } else {
            initConversationSetting();
        }
    }

    private void initConversationSetting() {
        ServiceManager.getInstance().getConversationData(group.conversationID, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                if (error == null) {
                    conversation = (Conversation) data[0];
                    bindData();
                }
            }
        });
    }

    @Override
    public void onClick(View view){
        switch(view.getId()){
            case R.id.group_profile_back:
                onBack();
                break;
            case R.id.group_profile_add_member:
                onAddMember();
                break;
            case  R.id.group_profile_leave_group:
                onLeaveGroup();
                break;
            case R.id.group_profile_notification:
                onNotificationSetting();
                break;
            case R.id.group_profile_mask:
                onMaskSetting();
                break;
            case R.id.group_profile_puzzle:
                onPuzzleSetting();
                break;
            case R.id.group_profile_image:
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
                ArrayList<String> selectContacts = data.getStringArrayListExtra("SELECT_CONTACT_USER_IDS");
                onAddMemberResult(selectContacts);
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

    private void bindViews() {
        groupProfile = (ImageView) findViewById(R.id.group_profile_image);
        groupProfile.setOnClickListener(this);
        groupName = (EditText) findViewById(R.id.group_profile_name);
        rvListMember = (RecyclerView) findViewById(R.id.group_profile_list_member);
        mLinearLayoutManager = new LinearLayoutManager(this);

        swNotification = (Switch) findViewById(R.id.group_profile_notification);
        swNotification.setOnClickListener(this);
        swMask = (Switch) findViewById(R.id.group_profile_mask);
        swMask.setOnClickListener(this);
        cbPuzzle = (Switch) findViewById(R.id.group_profile_puzzle);
        cbPuzzle.setOnClickListener(this);

        findViewById(R.id.group_profile_back).setOnClickListener(this);
        findViewById(R.id.group_profile_add_member).setOnClickListener(this);
        findViewById(R.id.group_profile_leave_group).setOnClickListener(this);
    }

    private void bindData() {
        adapter = new GroupProfileAdapter(this, this);
        rvListMember.setAdapter(adapter);
        rvListMember.setLayoutManager(mLinearLayoutManager);
        groupName.setText(group.groupName);
        UiUtils.displayProfileAvatar(groupProfile, group.groupAvatar);
        swNotification.setChecked(ServiceManager.getInstance().getNotificationsSetting(conversation.notifications));
        swMask.setChecked(ServiceManager.getInstance().getMaskSetting(conversation.maskMessages));
        cbPuzzle.setChecked(ServiceManager.getInstance().getPuzzleSetting(conversation.puzzleMessages));
        bindMemberData();
    }

    private void bindMemberData() {
        userRepository.initMemberList(group.memberIDs, (error, data) -> {
            group.members = (List<User>) data[0];
            adapter.initContact(group.members);
        });
    }

    private void onBack() {
        if(updateGroupName())
            finish();
    }

    private boolean updateGroupName() {
        if(group == null || groupName == null)
            return false;

        if(StringUtils.isEmpty(groupName.getText())) {
            Toast.makeText(getApplicationContext(), "Please input group name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!group.groupName.equals(groupName.getText().toString()))
            ServiceManager.getInstance().renameGroup(group, groupName.getText().toString());
        return true;
    }

    private void openPicker() {
        String profileFileFolder = getExternalFilesDir(null).getAbsolutePath() + File.separator +
                "profile" + File.separator + currentUser.key;
        double timestamp = System.currentTimeMillis() / 1000L;
        String profileFileName = "" + timestamp + "-" + currentUser.key + ".png";
        String profileFilePath = profileFileFolder + File.separator + profileFileName;
        imagePickerHelper = ImagePickerHelper.from(this)
                .setFilePath(profileFilePath)
                .setCrop(true)
                .setCallback((error, data) -> {
                    if (error == null) {
                        groupProfileImage = (File) data[0];
                        UiUtils.displayProfileAvatar(groupProfile, groupProfileImage);
                        bzzzStorage.uploadGroupAvatar(groupID, groupProfileImage, new Callback() {
                            @Override
                            public void complete(Object error, Object... data) {
                                if (error == null) {
                                    String profileImage = (String) data[0];
                                    ServiceManager.getInstance().updateGroupAvatar(groupID, group.memberIDs.keySet(), profileImage);
                                }
                            }
                        });
                    }
                });
        imagePickerHelper.openPicker();
    }

    private void onNotificationSetting() {
        ServiceManager.getInstance().changeNotificationConversation(conversation, swNotification.isChecked());
    }

    private void onMaskSetting() {
        ServiceManager.getInstance().changeMaskConversation(conversation, swMask.isChecked());
    }

    private void onPuzzleSetting() {
        ServiceManager.getInstance().changePuzzleConversation(conversation, cbPuzzle.isChecked());
    }

    private void onAddMember() {
        Intent i = new Intent(this, SelectContactActivity.class);
        i.putExtra("SELECTED_ID", TextUtils.join(", ", group.memberIDs.keySet()));
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void onAddMemberResult(ArrayList<String> selectContacts) {
        ServiceManager.getInstance().addMember(group, selectContacts);
        bindMemberData();
    }

    private void onLeaveGroup() {
        List<Group> groups = new ArrayList<Group>();
        groups.add(group);
        ServiceManager.getInstance().leaveGroup(groups);
        finish();
    }
}
