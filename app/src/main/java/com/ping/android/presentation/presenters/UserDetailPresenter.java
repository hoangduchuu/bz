package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 3/14/18.
 */

public interface UserDetailPresenter extends BasePresenter {
    void init(String otherUserId);

    void sendMessageToUser();

    void observeFriendStatus(String friendId);

    void toggleBlockUser(boolean checked);

    void deleteContact();

    void addContact();

    void handleVoiceCallPress();

    void handleVideoCallPress();

    interface View extends BaseView {

        void toggleBlockUser(boolean isBlocked);

        void openConversation(String s);

        void updateFriendStatus(boolean isFriend);

        void openCallScreen(User currentUser, User otherUser, boolean isVideoCall);

        void updateUI(User otherUser);
    }
}
