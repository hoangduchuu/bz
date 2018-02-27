package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveUserStatusUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.domain.usecase.group.ObserveGroupValueUseCase;
import com.ping.android.domain.usecase.message.ObserveMessageUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/26/18.
 */

public class ChatPresenterImpl implements ChatPresenter {
    @Inject
    ChatPresenter.View view;
    @Inject
    GetConversationValueUseCase getConversationValueUseCase;
    @Inject
    ObserveCurrentUserUseCase observeCurrentUserUseCase;
    @Inject
    ObserveMessageUseCase observeMessageUseCase;
    @Inject
    ObserveGroupValueUseCase observeGroupValueUseCase;
    // region Use cases for PVP conversation
    @Inject
    ObserveUserStatusUseCase observeUserStatusUseCase;
    // endregion
    Conversation conversation;
    User currentUser;

    @Inject
    public ChatPresenterImpl() {
    }

    @Override
    public void create() {
        observeCurrentUser();
    }

    private void observeCurrentUser() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
                view.onCurrentUser(user);
            }
        }, null);
    }

    @Override
    public void initConversationData(String conversationId) {
        getConversationValueUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation conv) {
                handleConversationUpdate(conv);
            }
        }, conversationId);
    }

    @Override
    public void observeMessageUpdate() {
        observeMessageUseCase.execute(new DefaultObserver<ChildData<Message>>() {
            @Override
            public void onNext(ChildData<Message> messageChildData) {
                switch (messageChildData.type) {
                    case CHILD_ADDED:
                        view.addNewMessage(messageChildData.data);
                        break;
                    case CHILD_REMOVED:
                        view.removeMessage(messageChildData.data);
                        break;
                    case CHILD_CHANGED:
                        view.updateMessage(messageChildData.data);
                        break;
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, conversation);
    }

    @Override
    public void sendTextMessage() {

    }

    private void observeGroupChange(String groupId) {
        observeGroupValueUseCase.execute(new DefaultObserver<Group>() {
            @Override
            public void onNext(Group group) {
                super.onNext(group);
                view.updateConversationTitle(group.groupName);
            }
        }, groupId);
    }

    private void obserUserStatus(String userId) {
        observeUserStatusUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.updateUserStatus(aBoolean);
            }
        }, userId);
    }

    private void handleConversationUpdate(Conversation conversation) {
        if (currentUser == null) return;

        this.conversation = conversation;
        view.updateConversation(conversation);
        //observeMessageUpdate();

        boolean isEnable = CommonMethod.getBooleanFrom(conversation.maskOutputs, currentUser.key);
        view.updateMaskSetting(isEnable);
        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            String opponentUserId = conversation.opponentUser.key;
            String nickName = conversation.nickNames.get(opponentUserId);
            String title = nickName;
            if (TextUtils.isEmpty(nickName)) {
                title = conversation.opponentUser.getDisplayName();
            }
            view.updateConversationTitle(title);
            obserUserStatus(opponentUserId);
        } else if (conversation.group != null) {
            view.updateConversationTitle(conversation.group.groupName);
            view.hideUserStatus();
            observeGroupChange(conversation.groupID);
        }
    }

    @Override
    public void destroy() {
        observeCurrentUserUseCase.dispose();
        getConversationValueUseCase.dispose();
        observeMessageUseCase.dispose();
    }
}
