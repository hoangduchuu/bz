package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.notification.SendGameStatusNotificationUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.presentation.presenters.GamePresenter;

import javax.inject.Inject;

/**
 * Created by tuanluong on 3/22/18.
 */

public class GamePresenterImpl implements GamePresenter {
    @Inject
    SendGameStatusNotificationUseCase sendGameStatusNotificationUseCase;

    @Inject
    public GamePresenterImpl() {}

    @Override
    public void sendGameStatus(Conversation conversation, boolean isPass) {
        sendGameStatusNotificationUseCase.execute(new DefaultObserver<>(),
                new SendGameStatusNotificationUseCase.Params(conversation, conversation.opponentUser, isPass));
    }
}
