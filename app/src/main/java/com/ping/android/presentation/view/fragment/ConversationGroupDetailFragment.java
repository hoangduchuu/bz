package com.ping.android.presentation.view.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.ping.android.activity.R;
import com.ping.android.activity.SelectContactActivity;
import com.ping.android.adapter.GroupProfileAdapter;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupComponent;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupModule;
import com.ping.android.fragment.BaseFragment;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationGroupDetailPresenter;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.presentation.view.activity.NewChatActivity;
import com.ping.android.service.ServiceManager;
import com.ping.android.ultility.Callback;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;
import static org.jivesoftware.smackx.privacy.packet.PrivacyItem.Type.group;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationGroupDetailFragment extends BaseFragment implements ConversationGroupDetailPresenter.View, View.OnClickListener {
    public static final String EXTRA_IMAGE_KEY = "EXTRA_IMAGE_KEY";
    private ImageView groupProfile;
    private EditText groupName;
    private RecyclerView rvListMember;
    private LinearLayoutManager mLinearLayoutManager;
    private Switch swNotification;
    private Switch swMask;
    private Switch cbPuzzle;

    private String conversationId;
    private GroupProfileAdapter adapter;

    private ImagePickerHelper imagePickerHelper;

    @Inject
    ConversationGroupDetailPresenter presenter;
    private ConversationDetailGroupComponent component;

    public static ConversationGroupDetailFragment newInstance(Bundle extras) {
        ConversationGroupDetailFragment fragment = new ConversationGroupDetailFragment();
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent().inject(this);
        if (getArguments() != null) {
            conversationId = getArguments().getString(ConversationDetailActivity.CONVERSATION_KEY);
        } else {
            throw new NullPointerException("Must set extras data");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_conversation_group_detail, container, false);
        setupView(view);
        presenter.initConversation(conversationId);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (requestCode == Constant.SELECT_CONTACT_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<User> selectedUsers = data.getParcelableArrayListExtra(SelectContactActivity.SELECTED_USERS_KEY);
                presenter.addUsersToGroup(selectedUsers);
            }
        }
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
//            case  R.id.group_profile_leave_group:
//                onLeaveGroup();
//                break;
//            case R.id.group_profile_notification:
//                onNotificationSetting();
//                break;
//            case R.id.group_profile_mask:
//                onMaskSetting();
//                break;
//            case R.id.group_profile_puzzle:
//                onPuzzleSetting();
//                break;
//            case R.id.group_profile_image:
//                openPicker();
//                break;
//            case R.id.profile_nickname:
//                onNickNameClicked();
//                break;
        }
    }

    private void onBack() {
        getActivity().onBackPressed();
    }

    private void onAddMember() {
        Intent i = new Intent(getContext(), NewChatActivity.class);
        //i.putExtra("SELECTED_ID", TextUtils.join(", ", group.memberIDs.keySet()));
        i.putExtra("ADD_MEMBER", true);
        startActivityForResult(i, Constant.SELECT_CONTACT_REQUEST);
    }

    private void setupView(View view) {
        groupProfile = view.findViewById(R.id.group_profile_image);
        groupProfile.setOnClickListener(this);
        groupName = view.findViewById(R.id.group_profile_name);
        rvListMember = view.findViewById(R.id.group_profile_list_member);
        mLinearLayoutManager = new LinearLayoutManager(getContext());

        swNotification = view.findViewById(R.id.group_profile_notification);
        swNotification.setOnClickListener(this);
        swMask = view.findViewById(R.id.group_profile_mask);
        swMask.setOnClickListener(this);
        cbPuzzle = view.findViewById(R.id.group_profile_puzzle);
        cbPuzzle.setOnClickListener(this);

        view.findViewById(R.id.group_profile_back).setOnClickListener(this);
        view.findViewById(R.id.group_profile_add_member).setOnClickListener(this);
        view.findViewById(R.id.group_profile_leave_group).setOnClickListener(this);
        view.findViewById(R.id.profile_nickname).setOnClickListener(this);

        // TODO
//        String transitionName = getArguments().getString(EXTRA_IMAGE_KEY);
//        groupProfile.setTransitionName(transitionName);

        adapter = new GroupProfileAdapter();
        rvListMember.setAdapter(adapter);
        rvListMember.setLayoutManager(mLinearLayoutManager);
    }

    private void bindData(Conversation conversation) {
        groupName.setText(conversation.group.groupName);
        UiUtils.displayProfileAvatar(groupProfile, conversation.group.groupAvatar, new Callback() {
            @Override
            public void complete(Object error, Object... data) {
                startPostponedEnterTransition();
            }
        });
        swNotification.setChecked(ServiceManager.getInstance().getNotificationsSetting(conversation.notifications));
        swMask.setChecked(ServiceManager.getInstance().getMaskSetting(conversation.maskMessages));
        cbPuzzle.setChecked(ServiceManager.getInstance().getPuzzleSetting(conversation.puzzleMessages));
        adapter.initContact(conversation.members);
    }

    public ConversationDetailGroupComponent getComponent() {
        if (component == null) {
            component = getComponent(ConversationDetailComponent.class)
                    .provideConversationDetailGroupComponent(new ConversationDetailGroupModule(this));
        }
        return component;
    }

    @Override
    public void updateConversation(Conversation conversation) {
        bindData(conversation);
    }
}
