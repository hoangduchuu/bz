package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.model.Conversation;

/**
 * Created by tuanluong on 1/31/18.
 */

public interface ConversationPVPDetailPresenter extends BasePresenter {
    void initConversation(String conversationId);

    interface View extends BaseView {

        void updateConversation(Conversation conversation);
    }
}
