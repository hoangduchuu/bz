package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.bzzzchat.cleanarchitecture.BaseView;
import com.ping.android.domain.usecase.conversation.CreatePVPConversationUseCase;
import com.ping.android.domain.usecase.conversation.NewCreatePVPConversationUseCase;
import com.ping.android.model.User;

import java.util.List;

/**
 * Created by tuanluong on 1/22/18.
 */

public interface NewChatPresenter extends BasePresenter {
    void createGroup(List<User> toUsers, String message);
    void createPVPConversation(NewCreatePVPConversationUseCase.Params params);

    interface NewChatView extends BaseView {
        void moveToChatScreen(String conversationId);
    }
}
