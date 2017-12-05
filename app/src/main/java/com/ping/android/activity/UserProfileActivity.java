package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

public class UserProfileActivity extends CoreActivity implements View.OnClickListener{
    public final static String CONVERSATION_ID_KEY = "CONVERSATION_ID";
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        userID = getIntent().getStringExtra(Constant.START_ACTIVITY_USER_ID);
        String conversationId = getIntent().getStringExtra(CONVERSATION_ID_KEY);
        bindViews();

        conversationRepository = new ConversationRepository();

        currentUser = ServiceManager.getInstance().getCurrentUser();

        ServiceManager.getInstance().getUser(userID, (error, data) -> {
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
            }
        });
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
        }
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
    }

    private void bindData() {
        userName.setText(user.getDisplayName());
        swBlock.setChecked(ServiceManager.getInstance().isBlock(userID));

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
                        intent.putExtra("CONVERSATION_ID", conversationID);
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
        conversationRepository.updateNotificationSetting(conversation.key, currentUser.key, swNotification.isChecked());
    }

    private void onMaskSetting() {
        ServiceManager.getInstance().changeMaskConversation(conversation, swMask.isChecked());
    }

    private void onPuzzleSetting() {
        ServiceManager.getInstance().changePuzzleConversation(conversation, swPuzzle.isChecked());
    }

    private void onBlock() {
        ServiceManager.getInstance().updateBlock(user, swBlock.isChecked());
    }


    private void onBack() {
        finish();
    }
}
