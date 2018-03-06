package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveUserStatusUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.domain.usecase.group.ObserveGroupValueUseCase;
import com.ping.android.domain.usecase.message.GetLastMessagesUseCase;
import com.ping.android.domain.usecase.message.LoadMoreMessagesUseCase;
import com.ping.android.domain.usecase.message.ObserveMessageUseCase;
import com.ping.android.domain.usecase.message.ResendMessageUseCase;
import com.ping.android.domain.usecase.message.SendAudioMessageUseCase;
import com.ping.android.domain.usecase.message.SendGameMessageUseCase;
import com.ping.android.domain.usecase.message.SendImageMessageUseCase;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Inject
    GetLastMessagesUseCase getLastMessagesUseCase;
    @Inject
    LoadMoreMessagesUseCase loadMoreMessagesUseCase;
    @Inject
    SendTextMessageUseCase sendTextMessageUseCase;
    @Inject
    SendImageMessageUseCase sendImageMessageUseCase;
    @Inject
    SendGameMessageUseCase sendGameMessageUseCase;
    @Inject
    SendAudioMessageUseCase sendAudioMessageUseCase;
    @Inject
    ResendMessageUseCase resendMessageUseCase;
    // region Use cases for PVP conversation
    @Inject
    ObserveUserStatusUseCase observeUserStatusUseCase;
    // endregion
    Conversation conversation;
    /**
     * Collections that contains new message update when chat screen come background
     */
    private List<ChildData<Message>> messagesInBackground;
    private AtomicBoolean isInBackground;

    User currentUser;

    @Inject
    public ChatPresenterImpl() {
        isInBackground = new AtomicBoolean(false);
        messagesInBackground = new ArrayList<>();
    }

    @Override
    public void create() {
        observeCurrentUser();
    }

    @Override
    public void resume() {
        //observeMessageUpdate();
        isInBackground.set(false);
        for (ChildData<Message> message: messagesInBackground) {
            switch (message.type) {
                case CHILD_ADDED:
                    view.addNewMessage(message.data);
                    break;
                case CHILD_REMOVED:
                    view.removeMessage(message.data);
                    break;
                case CHILD_CHANGED:
                    view.updateMessage(message.data);
                    break;
            }
        }
        messagesInBackground.clear();
    }

    @Override
    public void pause() {
        //observeMessageUseCase.unsubscribe();
        isInBackground.set(true);
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
                if (isInBackground.get()) {
                    messagesInBackground.add(messageChildData);
                    return;
                }
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
        }, new ObserveMessageUseCase.Params(conversation, currentUser));
    }

    @Override
    public void loadMoreMessage(double oldestTimestamp) {
        double endTimestamp = oldestTimestamp - 0.001;
        loadMoreMessagesUseCase.execute(new DefaultObserver<LoadMoreMessagesUseCase.Output>() {
            @Override
            public void onNext(LoadMoreMessagesUseCase.Output output) {
                view.updateLastMessages(output.messages, output.canLoadMore);
            }
        }, new LoadMoreMessagesUseCase.Params(conversation, endTimestamp));
    }

    @Override
    public void sendTextMessage(String message, boolean markStatus) {
        SendMessageUseCase.Params params = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.TEXT)
                .setConversation(conversation)
                .setCurrentUser(currentUser)
                .setText(message)
                .setMarkStatus(markStatus)
                .build();
        sendTextMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message1) {
                view.sendNotification(conversation, message1);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendImageMessage(String photoUrl, String thumbUrl, boolean markStatus) {
        SendImageMessageUseCase.Params params = new SendImageMessageUseCase.Params();
        params.filePath = photoUrl;
        params.thumbFilePath = thumbUrl;
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.markStatus = markStatus;
        params.messageType = MessageType.IMAGE;
        sendImageMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    view.addCacheMessage(message);
                } else {
                    view.sendNotification(conversation, message);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendGameMessage(String gameUrl, GameType gameType, boolean markStatus) {
        SendGameMessageUseCase.Params params = new SendGameMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = gameUrl;
        params.gameType = gameType;
        params.markStatus  = markStatus;
        params.messageType = MessageType.GAME;
        sendGameMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    view.addCacheMessage(message);
                } else {
                    view.sendNotification(conversation, message);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendAudioMessage(String audioUrl) {
        SendAudioMessageUseCase.Params params = new SendAudioMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = audioUrl;
        params.messageType = MessageType.AUDIO;
        sendAudioMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                view.sendNotification(conversation, message);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void setConversation(Conversation originalConversation) {
        this.conversation = originalConversation;
    }

    @Override
    public void resendMessage(Message message) {
        ResendMessageUseCase.Params params = new ResendMessageUseCase.Params();
        params.conversationId = conversation.key;
        params.currentUserId = currentUser.key;
        params.message = message;
        resendMessageUseCase.execute(new DefaultObserver<>(), params);
    }

    private void getLastMessages(Conversation conversation) {
        getLastMessagesUseCase.execute(new DefaultObserver<GetLastMessagesUseCase.Output>() {
            @Override
            public void onNext(GetLastMessagesUseCase.Output output) {
                view.updateLastMessages(output.messages, output.canLoadMore);
                observeMessageUpdate();
            }
        }, conversation);
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

    private void observeUserStatus(String userId) {
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
        getLastMessages(conversation);

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
            observeUserStatus(opponentUserId);
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
        getLastMessagesUseCase.dispose();
        sendTextMessageUseCase.dispose();
        sendImageMessageUseCase.dispose();
        sendGameMessageUseCase.dispose();
        sendAudioMessageUseCase.dispose();
        resendMessageUseCase.dispose();
    }
}
