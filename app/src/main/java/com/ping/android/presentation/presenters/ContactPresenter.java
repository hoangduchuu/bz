package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 3/14/18.
 */

public interface ContactPresenter extends BasePresenter {
    void handleSendMessage(User user);

    interface View extends BaseView {

        void addFriend(User data);

        void removeFriend(String key);

        void openConversation(String conversationId);
    }
}
