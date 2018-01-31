package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationPresenter extends BasePresenter {
    void getConversations();

    interface View extends BaseView {
        void addConversation(Conversation conversation);
        void updateConversation(Conversation conversation);
        void deleteConversation(Conversation data);

        void updateGroupConversation(Group data);
    }
}
