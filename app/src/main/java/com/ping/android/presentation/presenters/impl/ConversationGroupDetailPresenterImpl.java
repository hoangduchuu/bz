package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.conversation.ObserveConversationUpdateUseCase;
import com.ping.android.domain.usecase.conversation.ToggleConversationNotificationSettingUseCase;
import com.ping.android.domain.usecase.conversation.ToggleMaskIncomingUseCase;
import com.ping.android.domain.usecase.conversation.TogglePuzzlePictureUseCase;
import com.ping.android.domain.usecase.conversation.UpdateConversationColorUseCase;
import com.ping.android.domain.usecase.group.AddGroupMembersUseCase;
import com.ping.android.domain.usecase.group.LeaveGroupUseCase;
import com.ping.android.domain.usecase.group.ObserveGroupValueUseCase;
import com.ping.android.domain.usecase.group.UpdateGroupNameUseCase;
import com.ping.android.domain.usecase.group.UploadGroupProfileImageUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationGroupDetailPresenter;
import com.ping.android.utils.CommonMethod;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/31/18.
 */

public class ConversationGroupDetailPresenterImpl implements ConversationGroupDetailPresenter {
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ObserveGroupValueUseCase observeGroupValueUseCase;
    @Inject
    ObserveConversationUpdateUseCase observeConversationUpdateUseCase;
    @Inject
    AddGroupMembersUseCase addGroupMembersUseCase;
    @Inject
    LeaveGroupUseCase leaveGroupUseCase;
    @Inject
    ToggleConversationNotificationSettingUseCase toggleConversationNotificationSettingUseCase;
    @Inject
    ToggleMaskIncomingUseCase toggleMaskIncomingUseCase;
    @Inject
    TogglePuzzlePictureUseCase togglePuzzlePictureUseCase;
    @Inject
    UploadGroupProfileImageUseCase uploadGroupProfileImageUseCase;
    @Inject
    UpdateGroupNameUseCase updateGroupNameUseCase;
    @Inject
    UpdateConversationColorUseCase updateConversationColorUseCase;
    @Inject
    View view;
    private Conversation conversation;
    private User currentUser;

    @Inject
    public ConversationGroupDetailPresenterImpl() {
    }

    @Override
    public void create() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
            }
        }, null);
    }

    @Override
    public void initConversation(String conversationId) {
        view.showLoading();
        observeConversationUpdateUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation data) {
                conversation = data;
                observeGroupValue();
                view.updateConversation(data);
                if (currentUser != null) {
                    view.updateNotification(CommonMethod.getBooleanFrom(data.notifications, currentUser.key));
                    view.updateMask(CommonMethod.getBooleanFrom(data.maskMessages, currentUser.key));
                    view.updatePuzzlePicture(CommonMethod.getBooleanFrom(data.puzzleMessages, currentUser.key));
                }
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, conversationId);
    }

    private void observeGroupValue() {
        observeGroupValueUseCase.execute(new DefaultObserver<Group>() {
            @Override
            public void onNext(Group group) {
                conversation.group = group;
            }
        }, conversation.groupID);
    }

    @Override
    public void addUsersToGroup(List<User> selectedUsers) {
        view.showLoading();
        List<String> ret = new ArrayList<>();
        for (User user : selectedUsers) {
            if (!conversation.group.memberIDs.containsKey(user.key)
                    || CommonMethod.isTrueValue(conversation.group.deleteStatuses, user.key)) {
                ret.add(user.key);
            }
        }
        addGroupMembersUseCase.execute(new DefaultObserver<List<User>>() {
            @Override
            public void onNext(List<User> users) {
                view.updateGroupMembers(users);
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
                view.hideLoading();
            }
        }, new AddGroupMembersUseCase.Params(conversation, ret));
    }

    @Override
    public void leaveGroup() {
        view.showLoading();
        leaveGroupUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                if (aBoolean) {
                    view.navigateToMain();
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, conversation.group);
    }

    @Override
    public void toggleNotification(boolean isEnable) {
        view.showLoading();
        toggleConversationNotificationSettingUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    view.updateNotification(isEnable);
                }
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new ToggleConversationNotificationSettingUseCase.Params(conversation.key,
                new ArrayList<>(conversation.memberIDs.keySet()), isEnable));
    }

    @Override
    public void toggleMask(boolean isEnable) {
        view.showLoading();
        toggleMaskIncomingUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                if (aBoolean) {
                    view.updateMask(isEnable);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new ToggleMaskIncomingUseCase.Params(conversation.key, new ArrayList<>(conversation.memberIDs.keySet()), isEnable));
    }

    @Override
    public void togglePuzzle(boolean isEnable) {
        view.showLoading();
        togglePuzzlePictureUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                if (aBoolean) {
                    view.updatePuzzlePicture(isEnable);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new TogglePuzzlePictureUseCase.Params(conversation.key, new ArrayList<>(conversation.memberIDs.keySet()), isEnable));
    }

    @Override
    public void uploadGroupProfile(String absolutePath) {
        view.showLoading();
        uploadGroupProfileImageUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new UploadGroupProfileImageUseCase.Params(conversation.groupID, conversation.key, absolutePath, new ArrayList<>(conversation.memberIDs.keySet())));
    }

    @Override
    public void handleNicknameClicked() {
        view.openNicknameScreen(conversation);
    }

    @Override
    public void updateGroupName(String name) {
        updateGroupNameUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.navigateBack();
            }
        }, new UpdateGroupNameUseCase.Params(conversation.groupID, conversation.key, name, new ArrayList<>(conversation.memberIDs.keySet())));
    }

    @Override
    public void handleGroupProfileImagePress() {
        view.initProfileImagePath(conversation.key);
        view.openPicker();
    }

    @Override
    public void updateColor(int color) {
        UpdateConversationColorUseCase.Params params =
                new UpdateConversationColorUseCase.Params(currentUser.key, conversation, color);
        updateConversationColorUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.navigateBack();
            }
        }, params);
    }

    @Override
    public void handleBackgroundClicked() {
        view.moveToSelectBackground(conversation);
    }

    @Override
    public void handleGalleryClicked() {
        view.moveToGallery(conversation);
    }

    @Override
    public void destroy() {
        view = null;
        observeCurrentUserUseCase.dispose();
        observeGroupValueUseCase.dispose();
        observeConversationUpdateUseCase.dispose();
        addGroupMembersUseCase.dispose();
        leaveGroupUseCase.dispose();
        toggleConversationNotificationSettingUseCase.dispose();
        toggleMaskIncomingUseCase.dispose();
        togglePuzzlePictureUseCase.dispose();
        uploadGroupProfileImageUseCase.dispose();
        updateGroupNameUseCase.dispose();
        updateConversationColorUseCase.dispose();
    }
}
