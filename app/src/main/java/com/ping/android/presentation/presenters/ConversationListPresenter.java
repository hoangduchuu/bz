package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;

import java.util.List;

/**
 * Created by tuanluong on 1/28/18.
 */

public interface ConversationListPresenter extends BasePresenter {
    void getConversations();
    void deleteConversations(List<Conversation> conversations);

    interface View extends BaseView {
        void addConversation(Conversation conversation);
        void updateConversation(Conversation conversation);
        void deleteConversation(Conversation data);

        void updateGroupConversation(Group data);
    }
}
