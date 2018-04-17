package com.ping.android.presentation.view.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.ping.android.activity.R;
import com.ping.android.dagger.loggedin.conversationdetail.ConversationDetailComponent;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupComponent;
import com.ping.android.dagger.loggedin.conversationdetail.group.ConversationDetailGroupModule;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationGroupDetailPresenter;
import com.ping.android.presentation.view.activity.ConversationDetailActivity;
import com.ping.android.presentation.view.activity.CoreActivity;
import com.ping.android.presentation.view.activity.MainActivity;
import com.ping.android.presentation.view.activity.NewChatActivity;
import com.ping.android.presentation.view.activity.NicknameActivity;
import com.ping.android.presentation.view.activity.SelectContactActivity;
import com.ping.android.presentation.view.adapter.ColorAdapter;
import com.ping.android.presentation.view.adapter.GroupProfileAdapter;
import com.ping.android.ultility.Constant;
import com.ping.android.utils.DataProvider;
import com.ping.android.utils.ImagePickerHelper;
import com.ping.android.utils.Navigator;
import com.ping.android.utils.UiUtils;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConversationGroupDetailFragment extends BaseFragment
        implements ConversationGroupDetailPresenter.View, View.OnClickListener {
    public static final String EXTRA_IMAGE_KEY = "EXTRA_IMAGE_KEY";
    private ImageView groupProfile;
    private EditText groupName;
    private RecyclerView rvListMember;
    private LinearLayoutManager mLinearLayoutManager;
    private Switch swNotification;
    private Switch swMask;
    private Switch cbPuzzle;
    private BottomSheetDialog colorPickerBottomSheetDialog;

    private String conversationId;
    private GroupProfileAdapter adapter;
    private ColorAdapter colorAdapter;

    private ImagePickerHelper imagePickerHelper;
    private File groupProfileImage;

    @Inject
    ConversationGroupDetailPresenter presenter;
    @Inject
    Navigator navigator;
    private ConversationDetailGroupComponent component;
    private String profileImageKey;
    private boolean isFirstLoad = true;

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
            profileImageKey = getArguments().getString(EXTRA_IMAGE_KEY);
        } else {
            throw new NullPointerException("Must set extras data");
        }
        presenter.create();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (imagePickerHelper != null) {
            imagePickerHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onBackPress() {
        onBack();
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.group_profile_back:
                onBack();
                break;
            case R.id.group_profile_add_member:
                onAddMember();
                break;
            case R.id.group_profile_leave_group:
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
                presenter.handleGroupProfileImagePress();
                break;
            case R.id.profile_nickname:
                onNickNameClicked();
                break;
            case R.id.profile_background:
                onBackgroundClicked();
                break;
            case R.id.group_profile_color:
                onColorClicked();
                break;
            case R.id.profile_gallery:
                presenter.handleGalleryClicked();
                break;
        }
    }

    private void onBackgroundClicked() {
        presenter.handleBackgroundClicked();
    }

    private void onColorClicked() {
        colorPickerBottomSheetDialog.show();
    }

    private void onNickNameClicked() {
        presenter.handleNicknameClicked();
    }

    private void onPuzzleSetting() {
        boolean isEnable = cbPuzzle.isChecked();
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

    private void onLeaveGroup() {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.warning_leave_group)
                .setCancelable(false)
                .setPositiveButton(R.string.Warning_leave_group_leave, (dialog, whichButton) -> {
                    presenter.leaveGroup();
                })
                .setNegativeButton(R.string.gen_cancel, (dialog, which) -> {
                    dialog.dismiss();
                }).show();
    }

    private void onBack() {
        if (TextUtils.isEmpty(groupName.getText())) {
            Toast.makeText(getContext(), "Please input group name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getActivity() != null && getActivity() instanceof CoreActivity) {
            if (((CoreActivity) getActivity()).networkStatus != Constant.NETWORK_STATUS.CONNECTED) {
                navigateBack();
                return;
            }
        }

        presenter.updateGroupName(groupName.getText().toString());
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
        groupProfile.setTransitionName(profileImageKey);
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
        view.findViewById(R.id.profile_background).setOnClickListener(this);
        view.findViewById(R.id.group_profile_color).setOnClickListener(this);

        adapter = new GroupProfileAdapter();
        rvListMember.setAdapter(adapter);
        rvListMember.setLayoutManager(mLinearLayoutManager);

        View colorPickerView = LayoutInflater.from(view.getContext()).inflate(R.layout.bottom_sheet_color_picker, null);
        colorPickerBottomSheetDialog = new BottomSheetDialog(view.getContext());
        colorPickerBottomSheetDialog.setContentView(colorPickerView);
        RecyclerView recyclerView = colorPickerView.findViewById(R.id.color_list);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(), 5));
        colorAdapter = new ColorAdapter(DataProvider.getDefaultColors());
        colorAdapter.setListener(color -> {
            presenter.updateColor(color.getCode());
        });
        recyclerView.setAdapter(colorAdapter);
    }

    private void bindData(Conversation conversation) {
        groupName.setText(conversation.conversationName);
        if (getActivity() != null) {
            UiUtils.displayProfileAvatar(groupProfile, conversation.conversationAvatarUrl,
                    (error, data) -> {
                        if (isFirstLoad) {
                            isFirstLoad = false;
                            getActivity().startPostponedEnterTransition();
                        }
                    });
        }

        adapter.initContact(conversation.members);
        adapter.updateNickNames(conversation.nickNames);
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

    @Override
    public void updateGroupMembers(List<User> users) {
        adapter.initContact(users);
    }

    @Override
    public void navigateToMain() {
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
        cbPuzzle.setChecked(isEnable);
    }

    @Override
    public void openNicknameScreen(Conversation conversation) {
        Intent intent = new Intent(getContext(), NicknameActivity.class);
        intent.putExtra(NicknameActivity.CONVERSATION_KEY, conversation);
        startActivity(intent);
    }

    @Override
    public void navigateBack() {
        if (getActivity() != null) {
            getActivity().finishAfterTransition();
        }
    }

    @Override
    public void initProfileImagePath(String key) {
        if (getContext() == null) return;
//        String profileFileFolder = getContext().getExternalFilesDir(null).getAbsolutePath() + File.separator +
//                "profile" + File.separator + key;
//        double timestamp = System.currentTimeMillis() / 1000d;
//        String profileFileName = "" + timestamp + "-" + key + ".png";
//        String profileFilePath = profileFileFolder + File.separator + profileFileName;
        imagePickerHelper = ImagePickerHelper.from(this)
//                .setFilePath(profileFilePath)
                .setCrop(true)
                .setListener(new ImagePickerHelper.ImagePickerListener() {
                    @Override
                    public void onImageReceived(File file) {

                    }

                    @Override
                    public void onFinalImage(File... files) {
                        groupProfileImage = files[0];
                        UiUtils.displayProfileAvatar(groupProfile, groupProfileImage);
                        presenter.uploadGroupProfile(groupProfileImage.getAbsolutePath());
                    }
                });
    }

    @Override
    public void openPicker() {
        imagePickerHelper.openPicker();
    }

    @Override
    public void moveToSelectBackground(Conversation conversation) {
        navigator.moveToFragment(BackgroundFragment.newInstance(conversation));
    }

    @Override
    public void moveToGallery(Conversation conversation) {
        navigator.moveToFragment(GridGalleryFragment.newInstance(conversation));
    }
}
