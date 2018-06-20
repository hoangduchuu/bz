package com.ping.android.presentation.presenters;

import com.bzzzchat.cleanarchitecture.BasePresenter;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;

/**
 * Created by tuanluong on 3/22/18.
 */

public interface GamePresenter extends BasePresenter {
    void sendGameStatus(Conversation conversation, boolean isPass);

    void updateMessageStatus(String conversationId, String messageID, int messageType, int status);

    void updateMessageMask(String conversationId, String messageId, boolean isMask);

    interface View {
    }
}