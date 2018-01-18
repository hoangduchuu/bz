package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.adapter.GroupProfileAdapter;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.BzzzStorage;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.GroupRepository;
import com.ping.android.service.firebase.MessageRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupProfileActivity extends CoreActivity implements View.OnClickListener, GroupProfileAdapter.ClickListener{
    public static final String EXTRA_IMAGE_KEY = "EXTRA_IMAGE_KEY";
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
    private MessageRepository messageRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_profile);
        postponeEnterTransition();
        groupID = getIntent().getStringExtra(Constant.START_ACTIVITY_GROUP_ID);
        currentUser = UserManager.getInstance().getUser();

        bindViews();

        bzzzStorage = new BzzzStorage();
        groupRepository = new GroupRepository();
        conversationRepository = new ConversationRepository();
        userRepository = new UserRepository();

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                group = Group.from(dataSnapshot);
                if (conversation == null) {
                    initConversationData();
                } else {
                    bindMemberData();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        DatabaseReference groupReference = groupRepository.getDatabaseReference().child(groupID);
        groupReference.addValueEventListener(listener);
        databaseReferences.put(groupReference, listener);
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
        conversationRepository.getConversation(group.conversationID, (error, data) -> {
            if (error == null) {
                conversation = (Conversation) data[0];
                messageRepository = MessageRepository.from(conversation.key);
                bindData();
                registerConversationListener(conversation.key);
            }
        });
    }

    private void registerConversationListener(String conversationId) {
        DatabaseReference nickNameReference = conversationRepository.getDatabaseReference().child(conversationId).child("nickNames");
        ValueEventListener nickNameEvent = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    HashMap<String, String> nickNames = (HashMap<String, String>) dataSnapshot.getValue();
                    if (conversation != null) {
                        conversation.nickNames = nickNames;

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        nickNameReference.addValueEventListener(nickNameEvent);
        databaseReferences.put(nickNameReference, nickNameEvent);
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
            case R.id.profile_nickname:
                onNickNameClicked();
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
                List<User> selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                ArrayList<String> ret = new ArrayList<>();
                for (User user : selectedUsers) {
                    if (!group.memberIDs.containsKey(user.key)
                            || CommonMethod.isTrueValue(group.deleteStatuses, user.key)) {
                        ret.add(user.key);
                    }
                }
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onAddMemberResult(ret);
                    }
                }, 500);
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

    @Override
    public void onBackPressed() {
        onBack();
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
        findViewById(R.id.profile_nickname).setOnClickListener(this);

        String transitionName = getIntent().getStringExtra(EXTRA_IMAGE_KEY);
        groupProfile.setTransitionName(transitionName);
    }

    private void bindData() {
        adapter = new GroupProfileAdapter(this, this);
        rvListMember.setAdapter(adapter);
        rvListMember.setLayoutManager(mLinearLayoutManager);
        groupName.setText(group.groupName);
        UiUtils.displayProfileAvatar(groupProfile, group.groupAvatar, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                startPostponedEnterTransition();
            }
        });
        swNotification.setChecked(ServiceManager.getInstance().getNotificationsSetting(conversation.notifications));
        swMask.setChecked(ServiceManager.getInstance().getMaskSetting(conversation.maskMessages));
        cbPuzzle.setChecked(ServiceManager.getInstance().getPuzzleSetting(conversation.puzzleMessages));
        bindMemberData();
    }

    private void bindMemberData() {
        userRepository.initMemberList(group.memberIDs, (error, data) -> {
            List<User> users = (List<User>) data[0];
            group.members = new ArrayList<>();
            for (User user : users) {
                if (CommonMethod.isTrueValue(group.deleteStatuses, user.key)) continue;
                group.members.add(user);
            }
            adapter.initContact(group.members);
        });
    }

    private void onBack() {
        if(updateGroupName()) {
            supportFinishAfterTransition();
        }
    }

    private void onNickNameClicked() {
        conversation.members = group.members;
        Intent intent = new Intent(this, NicknameActivity.class);
        intent.putExtra(NicknameActivity.CONVERSATION_KEY, conversation);
        startActivity(intent);
    }

    private boolean updateGroupName() {
        if(group == null || groupName == null)
            return false;

        if(StringUtils.isEmpty(groupName.getText())) {
            Toast.makeText(getApplicationContext(), "Please input group name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(!group.groupName.equals(groupName.getText().toString())) {
            ServiceManager.getInstance().renameGroup(group, groupName.getText().toString());
        }
        return true;
    }

    private void openPicker() {
        String profileFileFolder = getExternalFilesDir(null).getAbsolutePath() + File.separator +
                "profile" + File.separator + currentUser.key;
        double timestamp = System.currentTimeMillis() / 1000d;
        String profileFileName = "" + timestamp + "-" + currentUser.key + ".png";
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
        showLoading();
        boolean isEnable = swNotification.isChecked();
        conversationRepository.updateNotificationSetting(conversation.key, currentUser.key, isEnable, (error, data) -> {
            if (error != null) {
                // Revert state if something went wrong
                swNotification.setChecked(!isEnable);
            }
            hideLoading();
        });
    }

    private void onMaskSetting() {
        showLoading();
        boolean isEnable = swMask.isChecked();
        conversationRepository.changeMaskConversation(conversation, isEnable, (error, data) -> {
            if (error != null) {
                // Revert state if something went wrong
                swMask.setChecked(!isEnable);
            }
            hideLoading();
        });
    }

    private void onPuzzleSetting() {
        showLoading();
        boolean isEnable = cbPuzzle.isChecked();
        conversationRepository.changePuzzleConversation(conversation, isEnable, (error, data) -> {
            if (error != null) {
                // Revert state if something went wrong
                cbPuzzle.setChecked(!isEnable);
            }
            hideLoading();
        });
    }

    private void onAddMember() {
        Intent i = new Intent(this, NewChatActivity.class);
        //i.putExtra("SELECTED_ID", TextUtils.join(", ", group.memberIDs.keySet()));
        i.putExtra("ADD_MEMBER", true);
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void onAddMemberResult(ArrayList<String> selectContacts) {
        showLoading();
        for (String userId : selectContacts) {
            group.memberIDs.put(userId, true);
        }
        //conversation.memberIDs = group.memberIDs;
        groupRepository.addNewMembersToGroup(group, conversation, selectContacts, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                hideLoading();
                if (error == null) {
                    bindMemberData();
                }
            }
        });
    }

    private void enableUsersToSeeMessages(ArrayList<String> selectContacts) {
        if (messageRepository == null) return;
        messageRepository.getDatabaseReference().child(conversation.key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Map<String, Object> updateValue = new HashMap<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void onLeaveGroup() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.warning_leave_group)
                .setCancelable(false)
                .setPositiveButton(R.string.Warning_leave_group_leave, (dialog, whichButton) -> {
                    showLoading();
                    groupRepository.leaveGroup(group, (error, data) -> {
                        hideLoading();
                        Intent intent = new Intent(GroupProfileActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    });
                })
                .setNegativeButton(R.string.gen_cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }
}
