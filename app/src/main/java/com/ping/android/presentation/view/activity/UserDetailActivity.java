package com.ping.android.presentation.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.ping.android.R;
import com.ping.android.dagger.loggedin.userdetail.UserDetailComponent;
import com.ping.android.dagger.loggedin.userdetail.UserDetailModule;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.UserDetailPresenter;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.configs.Constant;
import com.ping.android.utils.UiUtils;

import javax.inject.Inject;

public class UserDetailActivity extends CoreActivity implements View.OnClickListener, UserDetailPresenter.View {
    public static final String EXTRA_USER = "EXTRA_USER";
    public static final String EXTRA_USER_IMAGE = "EXTRA_USER_IMAGE";
    public static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";

    private ImageView ivAvatar;
    private TextView userName;
    private TextView tvDisplayName;
    private Switch swUserBlock;
    private LinearLayout layoutSaveContact, layoutDeleteContact;

    private String userID;
    private User user;

    @Inject
    UserDetailPresenter presenter;
    UserDetailComponent component;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        presenter.create();
        setContentView(R.layout.activity_user_detail);
        bindViews();
        userID = getIntent().getStringExtra(Constant.START_ACTIVITY_USER_ID);
        user = getIntent().getParcelableExtra(EXTRA_USER);
        String imageName = getIntent().getStringExtra(EXTRA_USER_IMAGE);
        ivAvatar.setTransitionName(imageName);
        String userName = getIntent().getStringExtra(EXTRA_USER_NAME);
        tvDisplayName.setTransitionName(userName);
        //ViewCompat.setTransitionName(ivAvatar, imageName);
        presenter.observeFriendStatus(userID);
        bindData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public UserDetailPresenter getPresenter() {
        return presenter;
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
        presenter.addContact(user.key);
    }

    private void deleteContact() {
        presenter.deleteContact(user.key);
    }

    private void bindViews() {
        ivAvatar = findViewById(R.id.user_detail_image);
        userName = findViewById(R.id.user_detail_name);
        tvDisplayName = findViewById(R.id.tv_display_name);

        swUserBlock = findViewById(R.id.user_detail_block);
        swUserBlock.setOnClickListener(this);

        layoutSaveContact = findViewById(R.id.layout_save_contact);
        layoutSaveContact.setOnClickListener(this);
        layoutDeleteContact = findViewById(R.id.layout_delete_contact);
        layoutDeleteContact.setOnClickListener(this);

        findViewById(R.id.user_detail_message).setOnClickListener(this);
        findViewById(R.id.user_detail_voice).setOnClickListener(this);
        findViewById(R.id.user_detail_video).setOnClickListener(this);
        findViewById(R.id.user_detail_back).setOnClickListener(this);
    }

    private void bindData() {
        tvDisplayName.setText(user.getDisplayName());
        userName.setText(user.pingID);

        UiUtils.displayProfileImage(ivAvatar, user, null);
    }

    private void onMessage() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.sendMessageToUser(user);
    }

    private void onVoiceCall() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.handleVoiceCallPress(user);
    }

    private void onVideoCall() {
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Please check network connection", Toast.LENGTH_SHORT).show();
            return;
        }
        presenter.handleVideoCallPress(user);
    }

    private void onBlock() {
        showLoading();
        presenter.toggleBlockUser(user.key, swUserBlock.isChecked());
    }

    private void onBack() {
        supportFinishAfterTransition();
    }

    public UserDetailComponent getComponent() {
        if (component == null) {
            component = getLoggedInComponent().provideUserDetailComponent(new UserDetailModule(this));
        }
        return component;
    }

    @Override
    public void toggleBlockUser(User user) {
        swUserBlock.setChecked(user.blocks.containsKey(userID));
    }

    @Override
    public void openConversation(String s) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(ChatActivity.CONVERSATION_ID, s);
        startActivity(intent);
    }

    @Override
    public void updateFriendStatus(boolean isFriend) {
        if (isFriend) {
            layoutSaveContact.setVisibility(View.GONE);
            layoutDeleteContact.setVisibility(View.VISIBLE);
        } else {
            layoutSaveContact.setVisibility(View.VISIBLE);
            layoutDeleteContact.setVisibility(View.GONE);
        }
    }

    @Override
    public void openCallScreen(User currentUser, User otherUser, boolean isVideoCall) {
        CallActivity.start(this, currentUser, otherUser, isVideoCall);
    }
}
