package com.ping.android.fragment;


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
import com.ping.android.activity.ChatActivity;
import com.ping.android.activity.ConversationDetailActivity;
import com.ping.android.activity.R;
import com.ping.android.managers.UserManager;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.service.ServiceManager;
import com.ping.android.service.firebase.ConversationRepository;
import com.ping.android.service.firebase.UserRepository;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.UiUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationPVPDetailFragment extends BaseFragment implements View.OnClickListener {
    private ImageView userProfile;
    private TextView userName;
    private Switch swNotification;
    private Switch swMask;
    private Switch swPuzzle;
    private Switch swBlock;

    private User user, currentUser;
    private Conversation conversation;
    private ConversationRepository conversationRepository;
    private UserRepository userRepository;

    private Callback userUpdated;

    public static ConversationPVPDetailFragment newInstance(Bundle extras) {
        ConversationPVPDetailFragment fragment = new ConversationPVPDetailFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            user = getArguments().getParcelable(ConversationDetailActivity.USER_KEY);
            conversation = getArguments().getParcelable(ConversationDetailActivity.CONVERSATION_KEY);
        }
        userRepository = new UserRepository();
        conversationRepository = new ConversationRepository();
        currentUser = UserManager.getInstance().getUser();

        userUpdated = (error, data) -> {
            if (error == null) {
                currentUser = (User) data[0];
                swBlock.setChecked(currentUser.blocks.containsKey(user.key));
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation_pvpdetail, container, false);
        bindViews(view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        UserManager.getInstance().addUserUpdated(userUpdated);
    }

    @Override
    public void onPause() {
        super.onPause();
        UserManager.getInstance().removeUserUpdated(userUpdated);
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

        if (user != null) {
            userName.setText(user.getDisplayName());
            swBlock.setChecked(currentUser.blocks.containsKey(user.key));

            UiUtils.displayProfileImage(getContext(), userProfile, user);
        }
        if (conversation != null) {
            bindConversationSetting();
        }
    }

    private void bindConversationSetting() {
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
                break;
        }
    }

    private void moveToFragment(BaseFragment fragment) {
        ((ConversationDetailActivity)getActivity()).getNavigator().moveToFragment(fragment);
    }

    private void onMessage() {
        getActivity().onBackPressed();
    }

    private void onVoiceCall() {
        CallActivity.start(getContext(), user, false);
    }

    private void onVideoCall() {
        CallActivity.start(getContext(), user, true);
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
}
