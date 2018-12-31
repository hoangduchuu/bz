package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.data.mappers.UserMapper;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.conversation.NewCreatePVPConversationUseCase;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.domain.usecase.notification.SendMessageNotificationUseCase;
import com.ping.android.domain.usecase.user.GetUsersProfileFromUserIdsUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.presenters.NewChatPresenter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 1/22/18.
 */

public class NewChatPresenterImpl implements NewChatPresenter {
    @Inject
    public NewChatView view;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    CreateGroupUseCase createGroupUseCase;
    @Inject
    NewCreatePVPConversationUseCase createPVPConversationUseCase;
    private User currentUser;

    @Inject
    SendMessageNotificationUseCase sendMessageNotificationUseCase;

    @Inject
    GetUsersProfileFromUserIdsUseCase getUsersProfileFromUserIdsUseCase;

    @Inject
    SendTextMessageUseCase sendTextMessageUseCase;

    @Inject
    UserMapper userMapper;

    @Inject
    public NewChatPresenterImpl() {
    }

    @Override
    public void create() {
        getCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
            }
        }, null);
    }

    @Override
    public void createGroup(List<User> toUsers, String message) {
        toUsers.add(currentUser);
        List<String> displayNames = new ArrayList<>();
        ArrayList<String> usersIds = new ArrayList<>();
        for (User user : toUsers) {
            usersIds.add(user.key);
            displayNames.add(user.getDisplayName());
        }

        getUsersProfileFromUserIdsUseCase.execute(new DefaultObserver<List<User>>(){
            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
            }

            @Override
            public void onNext(List<User> users) {
                super.onNext(users);
                CreateGroupUseCase.Params params = new CreateGroupUseCase.Params();
                params.users = users;
                params.groupName = TextUtils.join(", ", displayNames);
                params.groupProfileImage = "";
                params.message = message;
                view.showLoading();
                createGroupUseCase.execute(new DefaultObserver<Conversation>() {
                    @Override
                    public void onNext(Conversation conversation) {
                        view.hideLoading();
                        view.moveToChatScreen(conversation.key);
                    conversation.members = users;
                        for (User s : users) {
                            sendJoinedMessage(userMapper.getUserDisPlay(s, conversation), conversation);
                        }
                    sendNotification(conversation,conversation.key,params.message,MessageType.TEXT);
                    }

                    @Override
                    public void onError(@NotNull Throwable exception) {
                        exception.printStackTrace();
                        view.hideLoading();
                    }
                }, params);

            }

            @Override
            public void onComplete() {
                super.onComplete();
            }
        },new GetUsersProfileFromUserIdsUseCase.Params(usersIds));


    }

    @Override
    public void createPVPConversation(NewCreatePVPConversationUseCase.Params params) {
        view.showLoading();
        createPVPConversationUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation conversation) {
                view.hideLoading();
                view.moveToChatScreen(conversation.key);
                if (!params.message.isEmpty()){
                    sendNotification(conversation,conversation.lastedMessageIdWhenCreateNewChat,params.message,MessageType.TEXT);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void destroy() {
        view = null;
        getCurrentUserUseCase.dispose();
        createGroupUseCase.dispose();
        createPVPConversationUseCase.dispose();
    }

    /**
     *
     * @param conversation
     * @param messageId
     * @param message
     * @param messageType
     */
    private void sendNotification(Conversation conversation, String messageId, String message, MessageType messageType) {
        sendMessageNotificationUseCase.execute(new DefaultObserver<>(),
                new SendMessageNotificationUseCase.Params(conversation, messageId, message, messageType));
    }

    private void sendJoinedMessage(String joinedUser, Conversation conversation) {

        String message = joinedUser + " has joined ";
        SendMessageUseCase.Params params = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.SYSTEM)
                .setConversation(conversation)
                .setCurrentUser(currentUser)
                .setText(message)
                .setMarkStatus(false)
                .build();

        sendTextMessageUseCase.execute(new DefaultObserver<Message>() {
            // override methods if needed
        }, params);
    }

}