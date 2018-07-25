package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.message.UpdateMaskMessagesUseCase;
import com.ping.android.domain.usecase.message.UpdateMessageStatusUseCase;
import com.ping.android.domain.usecase.notification.SendGameStatusNotificationUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.presenters.GamePresenter;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/22/18.
 */

public class GamePresenterImpl implements GamePresenter {
    @Inject
    SendGameStatusNotificationUseCase sendGameStatusNotificationUseCase;
    @Inject
    UpdateMessageStatusUseCase updateMessageStatusUseCase;
    @Inject
    UpdateMaskMessagesUseCase updateMaskMessagesUseCase;

    @Inject
    public GamePresenterImpl() {}

    @Override
    public void sendGameStatus(Conversation conversation, boolean isPass) {
        sendGameStatusNotificationUseCase.execute(new DefaultObserver<>(),
                new SendGameStatusNotificationUseCase.Params(conversation, conversation.opponentUser, isPass));
    }

    @Override
    public void updateMessageStatus(String conversationId, String messageID, int status) {
        updateMessageStatusUseCase.execute(new DefaultObserver<Boolean>() {
                                               @Override
                                               public void onError(@NotNull Throwable exception) {
                                                   exception.printStackTrace();
                                               }
                                           },
                new UpdateMessageStatusUseCase.Params(conversationId, status, messageID, MessageType.GAME));
    }

    @Override
    public void updateMessageMask(String conversationId, String messageId, boolean isMask) {
        UpdateMaskMessagesUseCase.Params params = new UpdateMaskMessagesUseCase.Params();
        params.conversationId = conversationId;
        params.isLastMessage = false;
        params.isMask = isMask;
        params.setMessageId(messageId);
        updateMaskMessagesUseCase.execute(new DefaultObserver<>(), params);
    }
}
