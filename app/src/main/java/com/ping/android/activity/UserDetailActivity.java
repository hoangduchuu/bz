package com.ping.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ping.android.managers.UserManager;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

public class UserDetailActivity extends CoreActivity implements View.OnClickListener{

    private ImageView ivAvatar;
    private TextView userName;
    private TextView tvDisplayName;
    private Switch swUserBlock;
    private LinearLayout layoutSaveContact, layoutDeleteContact;

    private String userID;
    private User user, currentUser;
    private UserRepository userRepository;
    private Callback userUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);
        bindViews();
        userID = getIntent().getStringExtra(Constant.START_ACTIVITY_USER_ID);
        ViewCompat.setTransitionName(ivAvatar, userID);

        postponeEnterTransition();
        currentUser = UserManager.getInstance().getUser();
        userRepository = new UserRepository();
        userRepository.getUser(userID, (error, data) -> {
            if (error == null) {
                user = (User) data[0];
                bindData();
            }
        });
        userUpdated = (error, data) -> {
            if (error == null) {
                currentUser = (User) data[0];
                swUserBlock.setChecked(currentUser.blocks.containsKey(userID));
            }
        };
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
            case R.id.user_detail_message:
                onMessage();
                break;
            case R.id.user_detail_voice:
                onVoiceCall();
                break;
            case R.id.user_detail_video:
                onVideoCall();
                break;
            case R.id.user_detail_back:
                onBack();
                break;
            case R.id.user_detail_block:
                onBlock();
                break;
            case R.id.layout_save_contact:
                addContact();
                break;
            case R.id.layout_delete_contact:
                deleteContact();
                break;
        }
    }

    private void addContact() {
        ServiceManager.getInstance().addContact(user);
        updateContactLayout();
    }

    private void deleteContact() {
        ServiceManager.getInstance().deleteContact(user);
        updateContactLayout();
    }

    private void bindViews() {
        ivAvatar = (ImageView) findViewById(R.id.user_detail_image);
        userName = (TextView) findViewById(R.id.user_detail_name);
        tvDisplayName = (TextView) findViewById(R.id.tv_display_name);

        swUserBlock = (Switch) findViewById(R.id.user_detail_block);
        swUserBlock.setOnClickListener(this);

        layoutSaveContact = (LinearLayout) findViewById(R.id.layout_save_contact);
        layoutSaveContact.setOnClickListener(this);
        layoutDeleteContact = (LinearLayout) findViewById(R.id.layout_delete_contact);
        layoutDeleteContact.setOnClickListener(this);

        findViewById(R.id.user_detail_message).setOnClickListener(this);
        findViewById(R.id.user_detail_voice).setOnClickListener(this);
        findViewById(R.id.user_detail_video).setOnClickListener(this);
        findViewById(R.id.user_detail_back).setOnClickListener(this);
    }

    private void bindData() {
        tvDisplayName.setText(user.getDisplayName());
        userName.setText(user.pingID);
        swUserBlock.setChecked(ServiceManager.getInstance().isBlock(userID));

        updateContactLayout();

        UiUtils.displayProfileImage(ivAvatar, user, (error, data) -> startPostponedEnterTransition());
    }

    private void updateContactLayout() {
        if (user.typeFriend != null && user.typeFriend == Constant.TYPE_FRIEND.IS_FRIEND) {
            layoutSaveContact.setVisibility(View.GONE);
            layoutDeleteContact.setVisibility(View.VISIBLE);
        } else {
            layoutSaveContact.setVisibility(View.VISIBLE);
            layoutDeleteContact.setVisibility(View.GONE);
        }
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
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        CallActivity.start(this, user, false);
    }

    private void onVideoCall() {
        if (!ServiceManager.getInstance().getNetworkStatus(this)) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        CallActivity.start(this, user, true);
    }

    private void onBlock() {
        showLoading();
        userRepository.toggleBlockUser(user.key, swUserBlock.isChecked(), new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                hideLoading();
            }
        });
    }

    private void onBack() {
        finish();
    }
}
