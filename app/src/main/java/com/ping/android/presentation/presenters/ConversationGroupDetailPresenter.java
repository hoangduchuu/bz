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

    void addUsersToGroup(List<User> selectedUsers);

    interface View extends BaseView {
        void updateConversation(Conversation conversation);
    }
}
