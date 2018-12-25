package com.ping.android.presentation.presenters.impl;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.data.mappers.UserMapper;
import com.ping.android.domain.usecase.GetCurrentUserUseCase;
import com.ping.android.domain.usecase.group.CreateGroupUseCase;
import com.ping.android.domain.usecase.group.UploadGroupProfileImageUseCase;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.domain.usecase.notification.SendMessageNotificationUseCase;
import com.ping.android.domain.usecase.user.GetUsersProfileUseCase;
import com.ping.android.domain.usecase.user.GetUsersProfileUseCaseFromUserIds;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.presenters.AddGroupPresenter;
import com.ping.android.utils.BzLog;
import com.ping.android.utils.Log;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by tuanluong on 2/8/18.
 */

public class AddGroupPresenterImpl implements AddGroupPresenter {
    @Inject
    UploadGroupProfileImageUseCase uploadGroupProfileImageUseCase;
    @Inject
    CreateGroupUseCase createGroupUseCase;
    @Inject
    GetCurrentUserUseCase getCurrentUserUseCase;
    @Inject
    AddGroupPresenter.View view;
    private User currentUser;

    @Inject
    UserMapper userMapper;

    @Inject
    SendMessageNotificationUseCase sendMessageNotificationUseCase;

    @Inject
    SendTextMessageUseCase sendTextMessageUseCase;

    @Inject
    GetUsersProfileUseCaseFromUserIds getUsersProfileUseCaseFromUserIds;


    @Inject
    public AddGroupPresenterImpl() {
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
    public void createGroup(List<User> toUsers, String groupNames, String groupProfileImage, String s) {
        CreateGroupUseCase.Params params = new CreateGroupUseCase.Params();
        params.users = toUsers;
        params.groupName = groupNames;
        params.groupProfileImage = groupProfileImage;
        params.message = s;
        view.showLoading();
        createGroupUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation conversation) {
                view.hideLoading();

                List<String> list = new ArrayList<String>(conversation.memberIDs.keySet());

                getUsersProfileUseCaseFromUserIds.execute(new DefaultObserver<List<User>>(){
                    @Override
                    public void onComplete() {
                        super.onComplete();
                        view.moveToChatScreen(conversation.key);


                    }

                    @Override
                    public void onNext(List<User> users) {
                        super.onNext(users);
                        conversation.members = users;
                        for (User s: users){
                            sendJoinedMessage(userMapper.getUserDisPlay(s,conversation), conversation);
                            sendNotification(conversation,conversation.key, conversation.group.groupName + ": "+userMapper.getUserDisPlay(s,conversation) +" has joined ");

                        }

                    }

                    @Override
                    public void onError(@NotNull Throwable exception) {
                        super.onError(exception);
                    }
                },new GetUsersProfileUseCaseFromUserIds.Params((ArrayList<String>) list));


            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void handlePickerPress() {
        view.initProfileImagePath(currentUser.key);
        view.openPicker();
    }

    @Override
    public void destroy() {
        view = null;
        uploadGroupProfileImageUseCase.dispose();
        createGroupUseCase.dispose();
        getCurrentUserUseCase.dispose();
    }


    /**
     * @param conversation
     * @param messageId
     * @param notificationBody
     */
    private void sendNotification(Conversation conversation, String messageId, String notificationBody) {
        sendMessageNotificationUseCase.execute(new DefaultObserver<>(),
                new SendMessageNotificationUseCase.Params(conversation, messageId, notificationBody, MessageType.SYSTEM));
    }

    private void sendJoinedMessage(String joinedUser, Conversation conversationId){

        String mesessage = joinedUser + " has joined ";
        SendMessageUseCase.Params params = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.SYSTEM)
               //TODO remember .setConversation(conversation)
                .setConversation(conversationId)
                .setCurrentUser(currentUser)
                .setText(mesessage)
                .setMarkStatus(false)
                .build();

        sendTextMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message1) {
                //
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }
}
