package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 3/14/18.
 */

public interface UserDetailPresenter extends BasePresenter {
    void sendMessageToUser(User user);

    void observeFriendStatus(String friendId);

    void toggleBlockUser(String userId, boolean checked);

    void deleteContact(String key);

    void addContact(String key);

    void handleVoiceCallPress(User otherUser);

    void handleVideoCallPress(User otherUser);

    interface View extends BaseView {

        void toggleBlockUser(User user);

        void openConversation(String s);

        void updateFriendStatus(boolean isFriend);

        void openCallScreen(User currentUser, User otherUser, boolean isVideoCall);
    }
}
