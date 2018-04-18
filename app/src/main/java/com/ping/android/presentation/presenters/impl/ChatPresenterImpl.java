package com.ping.android.presentation.presenters.impl;

import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveUserStatusUseCase;
import com.ping.android.domain.usecase.RemoveUserBadgeUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.domain.usecase.conversation.ObserveConversationColorUseCase;
import com.ping.android.domain.usecase.conversation.ObserveConversationValueFromExistsConversationUseCase;
import com.ping.android.domain.usecase.conversation.ObserveTypingEventUseCase;
import com.ping.android.domain.usecase.conversation.ToggleConversationTypingUseCase;
import com.ping.android.domain.usecase.conversation.UpdateConversationReadStatusUseCase;
import com.ping.android.domain.usecase.conversation.UpdateConversationUseCase;
import com.ping.android.domain.usecase.group.ObserveGroupValueUseCase;
import com.ping.android.domain.usecase.message.DeleteMessagesUseCase;
import com.ping.android.domain.usecase.message.GetLastMessagesUseCase;
import com.ping.android.domain.usecase.message.LoadMoreMessagesUseCase;
import com.ping.android.domain.usecase.message.ObserveMessageUseCase;
import com.ping.android.domain.usecase.message.ResendMessageUseCase;
import com.ping.android.domain.usecase.message.SendAudioMessageUseCase;
import com.ping.android.domain.usecase.message.SendGameMessageUseCase;
import com.ping.android.domain.usecase.message.SendImageMessageUseCase;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.domain.usecase.message.UpdateMaskMessagesUseCase;
import com.ping.android.domain.usecase.notification.SendMessageNotificationUseCase;
import com.ping.android.model.ChildData;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.Color;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;
import com.ping.android.ultility.CommonMethod;
import com.ping.android.ultility.Constant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;

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
    @Inject
    UpdateConversationUseCase updateConversationUseCase;
    @Inject
    DeleteMessagesUseCase deleteMessagesUseCase;
    @Inject
    UpdateMaskMessagesUseCase updateMaskMessagesUseCase;
    @Inject
    ObserveConversationValueFromExistsConversationUseCase observeConversationValueFromExistsConversationUseCase;
    @Inject
    ObserveTypingEventUseCase observeTypingEventUseCase;
    @Inject
    UpdateConversationReadStatusUseCase updateConversationReadStatusUseCase;
    @Inject
    ToggleConversationTypingUseCase toggleConversationTypingUseCase;
    @Inject
    RemoveUserBadgeUseCase removeUserBadgeUseCase;
    @Inject
    SendMessageNotificationUseCase sendMessageNotificationUseCase;
    @Inject
    ObserveConversationColorUseCase observeConversationColorUseCase;
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
    private TreeMap<Long, MessageHeaderItem> headerItemMap;

    User currentUser;
    private Color currentColor;

    @Inject
    public ChatPresenterImpl() {
        isInBackground = new AtomicBoolean(false);
        messagesInBackground = new ArrayList<>();
        headerItemMap = new TreeMap<>();
    }

    @Override
    public void create() {
        observeCurrentUser();
    }

    @Override
    public void resume() {
        observeMessageUpdate();
        isInBackground.set(false);
        for (ChildData<Message> message : messagesInBackground) {
            handleMessageData(message);
        }
        messagesInBackground.clear();
    }

    @Override
    public void pause() {
        observeMessageUseCase.unsubscribe();
        isInBackground.set(true);
    }

    private void observeCurrentUser() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
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
        if (conversation == null) return;
        observeMessageUseCase.execute(new DefaultObserver<ChildData<Message>>() {
            @Override
            public void onNext(ChildData<Message> messageChildData) {
                handleMessageData(messageChildData);
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
                updateLastMessages(output.messages, output.canLoadMore);
            }
        }, new LoadMoreMessagesUseCase.Params(conversation, endTimestamp));
    }

    private void handleMessageData(ChildData<Message> messageChildData) {
        updateMessageStatus(messageChildData.data);
        if (isInBackground.get()) {
            messagesInBackground.add(messageChildData);
            return;
        }
        switch (messageChildData.type) {
            case CHILD_ADDED:
                // Check error message
                checkMessageError(messageChildData.data);
                updateConversationReadStatus();
                addMessage(messageChildData.data);
                break;
            case CHILD_REMOVED:
                MessageHeaderItem headerItem = headerItemMap.get(messageChildData.data.days);
                if (headerItem != null) {
                    MessageBaseItem item = headerItem.getChildItem(messageChildData.data);
                    if (item != null) {
                        view.removeMessage(headerItem, item);
                    }
                }
                break;
            case CHILD_CHANGED:
                addMessage(messageChildData.data);
                break;
        }
    }

    private void updateLastMessages(List<Message> messages, boolean canLoadMore) {
        MessageHeaderItem headerItem;
        for (Message message : messages) {
            headerItem = headerItemMap.get(message.days);
            if (headerItem == null) {
                headerItem = new MessageHeaderItem();
                headerItemMap.put(message.days, headerItem);
            }
            MessageBaseItem item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
            headerItem.addChildItem(item);
            //messageBaseItems.add(item);
        }
        //headerItemMap = CommonMethod.sortByKeys(headerItemMap);
        List<MessageHeaderItem> headerItems = new ArrayList<>(headerItemMap.values());
        view.updateLastMessages(headerItems, canLoadMore);
    }

    private void addMessage(Message message) {
        MessageHeaderItem headerItem = headerItemMap.get(message.days);
        if (headerItem == null) {
            headerItem = new MessageHeaderItem();
            headerItemMap.put(message.days, headerItem);
        }
        MessageBaseItem item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
        boolean added = headerItem.addChildItem(item);
        view.updateMessage(item, headerItem, added);
    }

    @Override
    public void sendTextMessage(String message, boolean markStatus) {
        if (!beAbleToSendMessage()) return;
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
                sendNotification(conversation, message1);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendImageMessage(String photoUrl, String thumbUrl, boolean markStatus) {
        if (!beAbleToSendMessage()) return;
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
                    MessageBaseItem item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
                    view.addCacheMessage(item);
                } else {
                    sendNotification(conversation, message);
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
        if (!beAbleToSendMessage()) return;
        SendGameMessageUseCase.Params params = new SendGameMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = gameUrl;
        params.gameType = gameType;
        params.markStatus = markStatus;
        params.messageType = MessageType.GAME;
        sendGameMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    MessageBaseItem item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
                    view.addCacheMessage(item);
                } else {
                    sendNotification(conversation, message);
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
        if (!beAbleToSendMessage()) return;
        SendAudioMessageUseCase.Params params = new SendAudioMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = audioUrl;
        params.messageType = MessageType.AUDIO;
        sendAudioMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (!message.isCached) {
                    sendNotification(conversation, message);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void resendMessage(Message message) {
        ResendMessageUseCase.Params params = new ResendMessageUseCase.Params();
        params.conversationId = conversation.key;
        params.currentUserId = currentUser.key;
        params.message = message;
        resendMessageUseCase.execute(new DefaultObserver<>(), params);
    }

    /**
     * Update conversation with last message. This method will be called when user delete message in conversation.
     * @param lastMessage Last message in conversation. Null if there is no message in conversation
     */
    @Override
    public void updateConversationLastMessage(@Nullable Message lastMessage) {
        Conversation conversation = new Conversation(this.conversation.conversationType,
                lastMessage != null ? lastMessage.messageType : Constant.MSG_TYPE_TEXT,
                lastMessage != null ? lastMessage.message : "",
                this.conversation.groupID, this.currentUser.key, this.conversation.memberIDs,
                lastMessage != null ? lastMessage.markStatuses : new HashMap<>(),
                this.conversation.readStatuses,
                lastMessage != null ? lastMessage.timestamp : System.currentTimeMillis() / 1000L,
                this.conversation);
        conversation.key = this.conversation.key;
        HashMap<String, Boolean> allowance = new HashMap<>();
        allowance.put(currentUser.key, true);

        updateConversationUseCase.execute(new DefaultObserver<Boolean>() {},
                new UpdateConversationUseCase.Params(conversation, allowance));
    }

    @Override
    public void deleteMessages(List<Message> messages) {
        DeleteMessagesUseCase.Params params = new DeleteMessagesUseCase.Params();
        params.messages = messages;
        params.conversationId = conversation.key;
        view.showLoading();
        deleteMessagesUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                super.onNext(aBoolean);
                view.hideLoading();
                view.switchOffEditMode();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void updateMaskMessages(List<Message> messages, boolean isLastMessage, boolean isMask) {
        UpdateMaskMessagesUseCase.Params params = new UpdateMaskMessagesUseCase.Params();
        params.conversationId = conversation.key;
        params.isLastMessage = isLastMessage;
        params.isMask = isMask;
        params.setMessages(messages);
        //view.showLoading();
        updateMaskMessagesUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                //view.hideLoading();
                view.switchOffEditMode();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                //view.hideLoading();
            }
        }, params);
    }

    private void getLastMessages(Conversation conversation) {
        getLastMessagesUseCase.execute(new DefaultObserver<GetLastMessagesUseCase.Output>() {
            @Override
            public void onNext(GetLastMessagesUseCase.Output output) {
                updateLastMessages(output.messages, output.canLoadMore);
                observeMessageUpdate();
                observeTypingEvent();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.updateLastMessages(new ArrayList<>(), false);
                observeMessageUpdate();
                observeTypingEvent();
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

    private void observeTypingEvent() {
        observeTypingEventUseCase.execute(
                new DefaultObserver<Map<String, Boolean>>() {
                    @Override
                    public void onNext(Map<String, Boolean> map) {
                        boolean isTyping = false;
                            for (String key : map.keySet()) {
                                if (!key.equals(currentUser.key) && map.get(key)
                                        && !currentUser.blocks.containsKey(key)
                                        && !currentUser.blockBys.containsKey(key)) {
                                    isTyping = true;
                                    break;
                                }
                        }
                        view.toggleTyping(isTyping);
                    }

                    @Override
                    public void onError(@NotNull Throwable exception) {
                        exception.printStackTrace();
                        view.toggleTyping(false);
                    }
                },
                new ObserveTypingEventUseCase.Params(conversation.key, currentUser.key));
    }

    private void observeConversationUpdate() {
        observeConversationValueFromExistsConversationUseCase
                .execute(new DefaultObserver<Conversation>() {
                             @Override
                             public void onNext(Conversation conv) {
                                 conversation = conv;
                                 if (conv.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                                     String opponentUserId = conv.opponentUser.key;
                                     String nickName = conv.nickNames.get(opponentUserId);
                                     String title = TextUtils.isEmpty(nickName) ? conv.opponentUser.getDisplayName() : nickName;
                                     view.updateConversationTitle(title);
                                 } else {
                                     view.updateNickNames(conv.nickNames);
                                 }
                             }
                         },
                        new ObserveConversationValueFromExistsConversationUseCase.Params(conversation, currentUser));
        observeConversationColorUseCase.execute(new DefaultObserver<Integer>() {
                                                    @Override
                                                    public void onNext(Integer integer) {
                                                        Color color = Color.from(integer);
                                                        if (currentColor != color) {
                                                            currentColor = color;
                                                            view.changeTheme(color);
                                                        }
                                                    }
                                                },
                new ObserveConversationColorUseCase.Params(conversation.key, currentUser.key));
    }

    private void handleConversationUpdate(Conversation conversation) {
        if (currentUser == null) return;

        this.conversation = conversation;
        view.updateConversation(conversation);
        observeConversationUpdate();
        updateConversationReadStatus();
        //observeMessageUpdate();
        getLastMessages(conversation);
        removeConversationBadge(conversation.key);

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

    private void removeConversationBadge(String key) {
        removeUserBadgeUseCase.execute(new DefaultObserver<>(), key);
    }

    @Override
    public void updateConversationReadStatus() {
        boolean isRead = CommonMethod.getBooleanFrom(conversation.readStatuses, currentUser.key);
        if (!isRead && !isInBackground.get()) {
            updateConversationReadStatusUseCase.execute(new DefaultObserver<>(), conversation);
        }
    }

    @Override
    public void handleUserTypingStatus(boolean typing) {
        if (conversation != null) {
            toggleConversationTypingUseCase.execute(new DefaultObserver<>(),
                    new ToggleConversationTypingUseCase.Params(currentUser.key, conversation, typing));
        }
    }

    @Override
    public void handleVideoCallPress() {
        view.openCallScreen(currentUser, conversation.opponentUser, true);
    }

    @Override
    public void handleVoiceCallPress() {
        view.openCallScreen(currentUser, conversation.opponentUser, false);
    }

    @Override
    public void initThemeColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    private void sendNotification(Conversation conversation, Message message) {
        sendMessageNotificationUseCase.execute(new DefaultObserver<>(),
                new SendMessageNotificationUseCase.Params(conversation, message));
    }

    private void checkMessageError(Message message) {
        int status = CommonMethod.getCurrentStatus(currentUser.key, message.status);
        if (message.senderId.equals(currentUser.key)) {
            if (status == Constant.MESSAGE_STATUS_ERROR && message.messageType == Constant.MSG_TYPE_TEXT) {
                resendMessage(message);
            }
        }
    }

    private boolean beAbleToSendMessage() {
        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            if (currentUser.blocks.containsKey(conversation.opponentUser.key)) {
                String username = conversation.opponentUser.firstName;
                view.showErrorUserBlocked(username);
                return false;
            }
        }
        return true;
    }


    @Override
    public void destroy() {
        observeCurrentUserUseCase.dispose();
        getConversationValueUseCase.dispose();
        observeMessageUseCase.dispose();
        observeTypingEventUseCase.dispose();
        observeConversationValueFromExistsConversationUseCase.dispose();
        getLastMessagesUseCase.dispose();
        deleteMessagesUseCase.dispose();
        updateMaskMessagesUseCase.dispose();
        toggleConversationTypingUseCase.dispose();
        observeConversationColorUseCase.dispose();
//        sendTextMessageUseCase.dispose();
//        sendImageMessageUseCase.dispose();
//        sendGameMessageUseCase.dispose();
//        sendAudioMessageUseCase.dispose();
//        resendMessageUseCase.dispose();
    }

    private void updateMessageStatus(Message message) {
        int status = Constant.MESSAGE_STATUS_SENT;
        for (String userId : conversation.memberIDs.keySet()) {
            status = CommonMethod.getIntFrom(message.status, userId);
            if (status == Constant.MESSAGE_STATUS_READ) {
                break;
            }
        }
        if (status != Constant.MESSAGE_STATUS_READ) {
            status = CommonMethod.getIntFrom(message.status, currentUser.key);
            if (status == -1) {
                status = Constant.MESSAGE_STATUS_SENT;
            }
        }
        String messageStatus = "";
        if (TextUtils.equals(message.senderId, currentUser.key)) {
            if (message.messageType != Constant.MSG_TYPE_GAME) {
                switch (status) {
                    case Constant.MESSAGE_STATUS_SENT:
                        messageStatus = "";
                        break;
                    case Constant.MESSAGE_STATUS_DELIVERED:
                        messageStatus = "Delivered";
                        break;
                    case Constant.MESSAGE_STATUS_ERROR:
                        messageStatus = "Undelivered";
                        break;
                    case Constant.MESSAGE_STATUS_READ:
                        messageStatus = "Read";
                        break;
                    default:
                        messageStatus = "";
                }
            } else {
                if (!TextUtils.isEmpty(conversation.groupID)) {
                    int passedCount = 0, failedCount = 0;
                    for (Map.Entry<String, Integer> entry : message.status.entrySet()) {
                        if (TextUtils.equals(entry.getKey(), currentUser.key)) {
                            continue;
                        }
                        if (entry.getValue() == Constant.MESSAGE_STATUS_GAME_PASS) {
                            passedCount += 1;
                        }
                        if (entry.getValue() == Constant.MESSAGE_STATUS_GAME_FAIL) {
                            failedCount += 1;
                        }
                    }
                    if (status == Constant.MESSAGE_STATUS_ERROR) {
                        messageStatus = "Game Undelivered";
                    } else if (status == Constant.MESSAGE_STATUS_SENT) {
                        messageStatus = "";
                    } else if (passedCount == 0 && failedCount == 0) {
                        if (status == Constant.MESSAGE_STATUS_READ) {
                            messageStatus = "Read";
                        } else {
                            messageStatus = "Game Delivered";
                        }
                    } else {
                        messageStatus = String.format("%s Passed, %s Failed", passedCount, failedCount);
                    }
                } else {
                    int oponentStatus = conversation.opponentUser != null && message.status.containsKey(conversation.opponentUser.key) ?
                            message.status.get(conversation.opponentUser.key) : Constant.MESSAGE_STATUS_GAME_DELIVERED;
                    if (oponentStatus == Constant.MESSAGE_STATUS_GAME_PASS
                            || oponentStatus == Constant.MESSAGE_STATUS_GAME_FAIL) {
                        status = oponentStatus;
                    }
                    switch (status) {
                        case Constant.MESSAGE_STATUS_GAME_PASS:
                            messageStatus = "Game Passed";
                            break;
                        case Constant.MESSAGE_STATUS_GAME_FAIL:
                            messageStatus = "Game Failed";
                            break;
                        case Constant.MESSAGE_STATUS_ERROR:
                            messageStatus = "Game Undelivered";
                            break;
                        case Constant.MESSAGE_STATUS_SENT:
                            messageStatus = "";
                            break;
                        case Constant.MESSAGE_STATUS_READ:
                            messageStatus = "Read";
                            break;
                        default:
                            messageStatus = "Game Delivered";

                    }
                }
            }
        } else {
            if (message.messageType == Constant.MSG_TYPE_GAME) {
                messageStatus = "Game";
            }
        }
        message.messageStatus = messageStatus;
        message.messageStatusCode = status;
    }
}
