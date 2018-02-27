package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;

/**
 * Created by tuanluong on 1/22/18.
 */

public interface NewChatPresenter extends BasePresenter {
    void createGroup(CreateGroupUseCase.Params params);
    void createPVPConversation(CreatePVPConversationUseCase.Params params);

    interface NewChatView extends BaseView {
        void moveToChatScreen(String conversationId);
    }
}
