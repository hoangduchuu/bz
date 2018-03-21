package com.ping.android.presentation.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.ping.android.activity.CallActivity;
import com.ping.android.activity.NicknameActivity;
import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.dagger.loggedin.conversationdetail.pvp.ConversationDetailPVPComponent;
import com.ping.android.dagger.loggedin.conversationdetail.pvp.ConversationDetailPVPModule;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.service.ServiceManager;
import com.ping.android.utils.UiUtils;

import javax.inject.Inject;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationPVPDetailFragment extends BaseFragment
        implements View.OnClickListener, ConversationPVPDetailPresenter.View {
    public static final String EXTRA_USER_IMAGE = "EXTRA_USER_IMAGE";
    public static final String EXTRA_USER_NAME = "EXTRA_USER_NAME";

    private ImageView userProfile;
    private TextView userName;
    private Switch swNotification;
    private Switch swMask;
    private Switch swPuzzle;
    private Switch swBlock;

    private Conversation conversation;
    private String conversationId;

    @Inject
    ConversationPVPDetailPresenter presenter;
    ConversationDetailPVPComponent component;

    public static ConversationPVPDetailFragment newInstance(Bundle extras) {
        ConversationPVPDetailFragment fragment = new ConversationPVPDetailFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        if (getArguments() != null) {
            conversationId = getArguments().getString(ConversationDetailActivity.CONVERSATION_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation_pvpdetail, container, false);
        bindViews(view);
        presenter.create();
        presenter.initConversation(conversationId);
        return view;
    }

    @Override
    public ConversationPVPDetailPresenter getPresenter() {
        return presenter;
    }

    private void bindViews(View view) {
        userProfile = view.findViewById(R.id.user_profile_image);
        userName = view.findViewById(R.id.user_profile_name);

        swNotification = view.findViewById(R.id.user_profile_notification);
        swNotification.setOnClickListener(this);
        swMask = view.findViewById(R.id.user_profile_mask);
        swMask.setOnClickListener(this);
        swPuzzle = view.findViewById(R.id.user_profile_puzzle);
        swPuzzle.setOnClickListener(this);
        swBlock = view.findViewById(R.id.user_profile_block);
        swBlock.setOnClickListener(this);

        view.findViewById(R.id.user_profile_back).setOnClickListener(this);
        view.findViewById(R.id.user_profile_message).setOnClickListener(this);
        view.findViewById(R.id.user_profile_voice).setOnClickListener(this);
        view.findViewById(R.id.user_profile_video).setOnClickListener(this);
        view.findViewById(R.id.profile_nickname).setOnClickListener(this);
    }

    private void bindConversationSetting(Conversation conversation) {
        swNotification.setChecked(ServiceManager.getInstance().getNotificationsSetting(conversation.notifications));
        swMask.setChecked(ServiceManager.getInstance().getMaskSetting(conversation.maskMessages));
        swPuzzle.setChecked(ServiceManager.getInstance().getPuzzleSetting(conversation.puzzleMessages));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
                getActivity().onBackPressed();
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
        presenter.handleNicknameClicked();
    }

    private void moveToFragment(BaseFragment fragment) {
        ((ConversationDetailActivity) getActivity()).getNavigator().moveToFragment(fragment);
    }

    private void onMessage() {
        getActivity().onBackPressed();
    }

    private void onVoiceCall() {
        CallActivity.start(getContext(), conversation.opponentUser, false);
    }

    private void onVideoCall() {
        CallActivity.start(getContext(), conversation.opponentUser, true);
    }

    private void onPuzzleSetting() {
        boolean isEnable = swPuzzle.isChecked();
        presenter.togglePuzzle(isEnable);
    }

    private void onMaskSetting() {
        boolean isEnable = swMask.isChecked();
        presenter.toggleMask(isEnable);
    }

    private void onNotificationSetting() {
        boolean isEnable = swNotification.isChecked();
        presenter.toggleNotification(isEnable);
    }

    private void onBlock() {
        showLoading();
        presenter.toggleBlockUser(conversation.opponentUser.key, swBlock.isChecked());
    }

    @Override
    public void updateConversation(Conversation conversation) {
        this.conversation = conversation;
        bindConversationSetting(conversation);
        userName.setText(conversation.opponentUser.getDisplayName());


        UiUtils.displayProfileImage(getContext(), userProfile, conversation.opponentUser);
    }

    @Override
    public void updateNotification(boolean isEnable) {
        swNotification.setChecked(isEnable);
    }

    @Override
    public void updateMask(boolean isEnable) {
        swMask.setChecked(isEnable);
    }

    @Override
    public void updatePuzzlePicture(boolean isEnable) {
        swPuzzle.setChecked(isEnable);
    }

    @Override
    public void updateBlockStatus(User user) {
        swBlock.setChecked(user.blocks.containsKey(conversation.opponentUser.key));
    }

    @Override
    public void openNicknameScreen(Conversation conversation) {
        Intent intent = new Intent(getContext(), NicknameActivity.class);
        intent.putExtra(NicknameActivity.CONVERSATION_KEY, conversation);
        startActivity(intent);
    }

    public ConversationDetailPVPComponent getComponent() {
        if (component == null) {
            component = getComponent(ConversationDetailComponent.class)
                    .provideConversationDetailPVPComponent(new ConversationDetailPVPModule(this));
        }
        return component;
    }
}
