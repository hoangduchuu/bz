package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class UserProfileActivity extends CoreActivity implements View.OnClickListener{
    private ImageView userProfile;
    private TextView userName;
    private Switch swNotification;
    private Switch swMask;
    private Switch swPuzzle;
    private Switch swBlock;

    private String userID;
    private User user, currentUser;
    private Conversation conversation;
    private ConversationRepository conversationRepository;
    private UserRepository userRepository;

    private Callback userUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userID = getIntent().getStringExtra(Constant.START_ACTIVITY_USER_ID);
        String conversationId = getIntent().getStringExtra(ChatActivity.CONVERSATION_ID);
        bindViews();

        conversationRepository = new ConversationRepository();
        showLoading();
        currentUser = UserManager.getInstance().getUser();
        userRepository = new UserRepository();
        userRepository.getUser(userID, (error, data) -> {
            if (error == null) {
                user = (User) data[0];
                bindData();
            }
        });

        conversationRepository.getConversation(conversationId, (error, data) -> {
            if (error == null) {
                conversation = (Conversation) data[0];
                conversation.key = conversationId;
                bindConversationSetting();
                userRepository.initMemberList(conversation.memberIDs, (error1, data1) -> {
                    if (error1 == null) {
                        conversation.members = (List<User>) data1[0];
                        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                            String currentUserId = UserManager.getInstance().getUser().key;
                            for (User user : conversation.members) {
                                if (!user.key.equals(currentUserId)) {
                                    conversation.opponentUser = user;
                                }
                            }
                        }
                        registerConversationListener(conversationId);
                    }
                });
            }
            hideLoading();
        });
        userUpdated = (error, data) -> {
            if (error == null) {
                currentUser = (User) data[0];
                swBlock.setChecked(currentUser.blocks.containsKey(userID));
            }
        };
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
                        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                            String nickName = nickNames.get(conversation.opponentUser.key);
                            if (!TextUtils.isEmpty(nickName)) {
                                userName.setText(nickName);
                            }
                        }
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
    protected void onResume() {
        super.onResume();
        UserManager.getInstance().addUserUpdated(userUpdated);
    }

    @Override
    protected void onPause() {
        super.onPause();
        UserManager.getInstance().removeUserUpdated(userUpdated);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.user_profile_message:
                onMessage();
                break;
            case R.id.user_profile_voice:
                onVoiceCall();
                break;
            case R.id.user_profile_video:
                onVideoCall();
                break;
            case R.id.user_profile_back:
                onBack();
                break;
            case R.id.user_profile_notification:
                onNotificationSetting();
                break;
            case R.id.user_profile_mask:
                onMaskSetting();
                break;
            case R.id.user_profile_puzzle:
                onPuzzleSetting();
                break;
            case R.id.user_profile_block:
                onBlock();
                break;
            case R.id.profile_nickname:
                onNickNameClicked();
                break;
        }
    }

    private void onNickNameClicked() {
        Intent intent = new Intent(this, NicknameActivity.class);
        intent.putExtra(NicknameActivity.CONVERSATION_KEY, conversation);
        startActivity(intent);
    }

    private void bindViews() {
        userProfile = (ImageView) findViewById(R.id.user_profile_image);
        userName = (TextView) findViewById(R.id.user_profile_name);

        swNotification = (Switch) findViewById(R.id.user_profile_notification);
        swNotification.setOnClickListener(this);
        swMask = (Switch) findViewById(R.id.user_profile_mask);
        swMask.setOnClickListener(this);
        swPuzzle = (Switch) findViewById(R.id.user_profile_puzzle);
        swPuzzle.setOnClickListener(this);
        swBlock = (Switch) findViewById(R.id.user_profile_block);
        swBlock.setOnClickListener(this);

        findViewById(R.id.user_profile_back).setOnClickListener(this);
        findViewById(R.id.user_profile_message).setOnClickListener(this);
        findViewById(R.id.user_profile_voice).setOnClickListener(this);
        findViewById(R.id.user_profile_video).setOnClickListener(this);
        findViewById(R.id.profile_nickname).setOnClickListener(this);
    }

    private void bindData() {
        userName.setText(user.getDisplayName());
        swBlock.setChecked(currentUser.blocks.containsKey(userID));

        UiUtils.displayProfileImage(this, userProfile, user);
    }

    private void bindConversationSetting() {
        swNotification.setChecked(ServiceManager.getInstance().getNotificationsSetting(conversation.notifications));
        swMask.setChecked(ServiceManager.getInstance().getMaskSetting(conversation.maskMessages));
        swPuzzle.setChecked(ServiceManager.getInstance().getPuzzleSetting(conversation.puzzleMessages));
    }

    private void onMessage() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        ServiceManager.getInstance().createConversationIDForPVPChat(currentUser.key, user.key,
                new Callback() {
                    @Override
                    public void complete(Object error, Object... data) {
                        String conversationID = data[0].toString();
                        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                        intent.putExtra(ChatActivity.CONVERSATION_ID, conversationID);
                        startActivity(intent);
                    }
                });
    }

    private void onVoiceCall() {
        CallActivity.start(this, user, false);
    }

    private void onVideoCall() {
        CallActivity.start(this, user, true);
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
        boolean isEnable = swPuzzle.isChecked();
        conversationRepository.changePuzzleConversation(conversation, isEnable, (error, data) -> {
            if (error != null) {
                // Revert state if something went wrong
                swPuzzle.setChecked(!isEnable);
            }
            hideLoading();
        });
    }

    private void onBlock() {
        showLoading();
        userRepository.toggleBlockUser(user.key, swBlock.isChecked(), (error, data) -> hideLoading());
    }


    private void onBack() {
        finish();
    }
}
