package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.conversation.ObserveConversationValueUseCase;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.conversation.ToggleMaskIncomingUseCase;
import com.ping.android.domain.usecase.conversation.ToggleNotificationSettingUseCase;
import com.ping.android.domain.usecase.conversation.TogglePuzzlePictureUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ConversationPVPDetailPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/31/18.
 */

public class ConversationPVPDetailPresenterImpl implements ConversationPVPDetailPresenter {
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ObserveConversationValueUseCase observeConversationValueUseCase;
    @Inject
    ToggleNotificationSettingUseCase toggleNotificationSettingUseCase;
    @Inject
    ToggleMaskIncomingUseCase toggleMaskIncomingUseCase;
    @Inject
    TogglePuzzlePictureUseCase togglePuzzlePictureUseCase;
    @Inject
    ConversationPVPDetailPresenter.View view;

    private Conversation conversation;
    private User currentUser;

    @Inject
    public ConversationPVPDetailPresenterImpl() {}

    @Override
    public void initConversation(String conversationId) {
        observeConversationValueUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation data) {
                conversation = data;
                observeUserUpdate();
                view.updateConversation(data);
            }
        }, conversationId);
    }

    private void observeUserUpdate() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
                view.updateBlockStatus(user);
            }
        }, null);
    }

    @Override
    public void toggleNotification(boolean isEnable) {
        view.showLoading();
        toggleNotificationSettingUseCase.execute(new DefaultObserver<Boolean>(){
            @Override
            public void onNext(Boolean aBoolean) {
                if (aBoolean) {
                    view.updateNotification(isEnable);
                }
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new ToggleNotificationSettingUseCase.Params(conversation.key, isEnable));
    }

    @Override
    public void toggleMask(boolean isEnable) {
        view.showLoading();
        toggleMaskIncomingUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                if (aBoolean) {
                    view.updateMask(isEnable);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new ToggleMaskIncomingUseCase.Params(conversation.key, new ArrayList<>(conversation.memberIDs.keySet()), isEnable));
    }

    @Override
    public void togglePuzzle(boolean isEnable) {
        view.showLoading();
        togglePuzzlePictureUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
                if (aBoolean) {
                    view.updatePuzzlePicture(isEnable);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, new TogglePuzzlePictureUseCase.Params(conversation.key, new ArrayList<>(conversation.memberIDs.keySet()), isEnable));
    }

    @Override
    public void handleNicknameClicked() {
        view.openNicknameScreen(conversation);
    }

    @Override
    public void destroy() {
        observeConversationValueUseCase.dispose();
        toggleNotificationSettingUseCase.dispose();
        toggleMaskIncomingUseCase.dispose();
        togglePuzzlePictureUseCase.dispose();
        observeCurrentUserUseCase.dispose();
    }
}
