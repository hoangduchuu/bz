package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;

import java.util.List;

/**
 * Created by tuanluong on 1/31/18.
 */

public interface ConversationGroupDetailPresenter extends BasePresenter {
    void initConversation(String conversationId);

    void addUsersToGroup(List<User> oldMembers, List<User> selectedUsers, String conversationName);

    void leaveGroup(String conversationName);

    void toggleNotification(boolean isEnable);

    void toggleMask(boolean isEnable);

    void togglePuzzle(boolean isEnable);

    void uploadGroupProfile(String absolutePath);

    void handleNicknameClicked();

    void updateGroupName(String name);

    void handleGroupProfileImagePress();

    void updateColor(int color);

    void handleBackgroundClicked();

    void handleGalleryClicked();

    interface View extends BaseView {
        void updateConversation(Conversation conversation);

        void updateGroupMembers(List<User> users);

        void navigateToMain();

        void updateNotification(boolean isEnable);

        void updateMask(boolean isEnable);

        void updatePuzzlePicture(boolean isEnable);

        void openNicknameScreen(Conversation conversation);

        void navigateBack();

        void initProfileImagePath(String key);

        void openPicker();

        void moveToSelectBackground(Conversation conversation);

        void moveToGallery(Conversation conversation);
    }
}
