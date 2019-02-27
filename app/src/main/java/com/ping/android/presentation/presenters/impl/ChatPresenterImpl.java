package com.ping.android.presentation.presenters.impl;

import android.app.Activity;
import android.text.TextUtils;

import com.bzzzchat.cleanarchitecture.DefaultObserver;
import com.bzzzchat.videorecorder.view.PhotoItem;
import com.ping.android.data.entity.ChildData;
import com.ping.android.data.repository.FaceIdStatusRepository;
import com.ping.android.domain.usecase.ObserveCurrentUserUseCase;
import com.ping.android.domain.usecase.ObserveUserStatusUseCase;
import com.ping.android.domain.usecase.RemoveUserBadgeUseCase;
import com.ping.android.domain.usecase.conversation.GetConversationValueUseCase;
import com.ping.android.domain.usecase.conversation.ObserveConversationBackgroundUseCase;
import com.ping.android.domain.usecase.conversation.ObserveConversationColorUseCase;
import com.ping.android.domain.usecase.conversation.ObserveConversationValueFromExistsConversationUseCase;
import com.ping.android.domain.usecase.conversation.ObserveNicknameConversationUseCase;
import com.ping.android.domain.usecase.conversation.ObserveTypingEventUseCase;
import com.ping.android.domain.usecase.conversation.ToggleConversationTypingUseCase;
import com.ping.android.domain.usecase.conversation.UpdateConversationReadStatusUseCase;
import com.ping.android.domain.usecase.conversation.UpdateConversationUseCase;
import com.ping.android.domain.usecase.conversation.UpdateMaskOutputConversationUseCase;
import com.ping.android.domain.usecase.group.ObserveGroupValueUseCase;
import com.ping.android.domain.usecase.message.DeleteMessagesUseCase;
import com.ping.android.domain.usecase.message.GetLastMessagesUseCase;
import com.ping.android.domain.usecase.message.GetUpdatedMessagesUseCase;
import com.ping.android.domain.usecase.message.LoadMoreMessagesUseCase;
import com.ping.android.domain.usecase.message.ObserveLastMessageUseCase;
import com.ping.android.domain.usecase.message.ObserveMessageChangeUseCase;
import com.ping.android.domain.usecase.message.ResendMessageUseCase;
import com.ping.android.domain.usecase.message.SendAudioMessageUseCase;
import com.ping.android.domain.usecase.message.SendGameMessageUseCase;
import com.ping.android.domain.usecase.message.SendGifsMessageUseCase;
import com.ping.android.domain.usecase.message.SendGroupGameMessageUseCase;
import com.ping.android.domain.usecase.message.SendGroupImageMessageUseCase;
import com.ping.android.domain.usecase.message.SendImageMessageUseCase;
import com.ping.android.domain.usecase.message.SendMessageUseCase;
import com.ping.android.domain.usecase.message.SendNewStickerMessageUseCase;
import com.ping.android.domain.usecase.message.SendStickerMessageUseCase;
import com.ping.android.domain.usecase.message.SendTextMessageUseCase;
import com.ping.android.domain.usecase.message.SendVideoMessageUseCase;
import com.ping.android.domain.usecase.message.UpdateMaskChildMessagesUseCase;
import com.ping.android.domain.usecase.message.UpdateMaskMessagesUseCase;
import com.ping.android.domain.usecase.message.UpdateMessageStatusUseCase;
import com.ping.android.domain.usecase.notification.SendMessageNotificationUseCase;
import com.ping.android.domain.usecase.user.CheckPasswordUseCase;
import com.ping.android.domain.usecase.user.ObserveBadgeCountUseCase;
import com.ping.android.model.Conversation;
import com.ping.android.model.Group;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.Color;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.presentation.presenters.ChatPresenter;
import com.ping.android.presentation.view.activity.ChatActivity;
import com.ping.android.presentation.view.flexibleitem.messages.MessageBaseItem;
import com.ping.android.presentation.view.flexibleitem.messages.MessageHeaderItem;
import com.ping.android.utils.ActivityLifecycle;
import com.ping.android.utils.CommonMethod;
import com.ping.android.utils.bus.BusProvider;
import com.ping.android.utils.bus.events.BadgeCountUpdateEvent;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import androidx.annotation.NonNull;

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
    ObserveLastMessageUseCase observeLastMessageUseCase;
    @Inject
    ObserveMessageChangeUseCase observeMessageChangeUseCase;
    @Inject
    ObserveGroupValueUseCase observeGroupValueUseCase;
    @Inject
    GetLastMessagesUseCase getLastMessagesUseCase;
    @Inject
    GetUpdatedMessagesUseCase getUpdatedMessagesUseCase;
    @Inject
    LoadMoreMessagesUseCase loadMoreMessagesUseCase;
    @Inject
    SendTextMessageUseCase sendTextMessageUseCase;
    @Inject
    SendImageMessageUseCase sendImageMessageUseCase;
    @Inject
    SendStickerMessageUseCase sendStickerMessageUseCase;
    @Inject
    SendGroupImageMessageUseCase sendGroupImageMessageUseCase;
    @Inject
    SendGameMessageUseCase sendGameMessageUseCase;
    @Inject
    SendGroupGameMessageUseCase sendGroupGameMessageUseCase;
    @Inject
    SendAudioMessageUseCase sendAudioMessageUseCase;
    @Inject
    SendVideoMessageUseCase sendVideoMessageUseCase;
    @Inject
    ResendMessageUseCase resendMessageUseCase;
    @Inject
    UpdateConversationUseCase updateConversationUseCase;
    @Inject
    DeleteMessagesUseCase deleteMessagesUseCase;
    @Inject
    UpdateMaskMessagesUseCase updateMaskMessagesUseCase;
    @Inject
    UpdateMaskChildMessagesUseCase updateMaskChildMessagesUseCase;
    @Inject
    UpdateMaskOutputConversationUseCase updateMaskOutputConversationUseCase;
    @Inject
    ObserveConversationValueFromExistsConversationUseCase observeConversationValueFromExistsConversationUseCase;
    @Inject
    ObserveTypingEventUseCase observeTypingEventUseCase;
    @Inject
    UpdateConversationReadStatusUseCase updateConversationReadStatusUseCase;
    @Inject
    UpdateMessageStatusUseCase updateMessageStatusUseCase;
    @Inject
    ToggleConversationTypingUseCase toggleConversationTypingUseCase;
    @Inject
    RemoveUserBadgeUseCase removeUserBadgeUseCase;
    @Inject
    SendMessageNotificationUseCase sendMessageNotificationUseCase;
    @Inject
    ObserveNicknameConversationUseCase observeNicknameConversationUseCase;
    @Inject
    ObserveConversationColorUseCase observeConversationColorUseCase;
    @Inject
    ObserveConversationBackgroundUseCase observeConversationBackgroundUseCase;

    @Inject
    SendNewStickerMessageUseCase sendNewStickerMessageUseCase;

    @Inject
    SendGifsMessageUseCase sendGifsMessageUseCase;

    @Inject
    FaceIdStatusRepository faceIdStatusRepository;

    // TODO need time to create Usecase to execute FaceIdStatusRepository in stead call directly repository over here!

    @Inject
    ObserveBadgeCountUseCase observeBadgeCountUseCase;

    @Inject
    CheckPasswordUseCase checkPasswordUseCase;

    // region Use cases for PVP conversation
    @Inject
    ObserveUserStatusUseCase observeUserStatusUseCase;
    // endregion
    Conversation conversation;


    @Inject
    BusProvider busProvider;

    /**
     * Collections that contains new message update when chat screen come background
     */
    private List<ChildData<Message>> messagesInBackground;
    private AtomicBoolean isInBackground;
    private TreeMap<Long, MessageHeaderItem> headerItemMap;
    private Map<String, String> localCacheFile;

    User currentUser;
    private Color currentColor;

    @Inject
    public ChatPresenterImpl() {
        isInBackground = new AtomicBoolean(false);
        messagesInBackground = new ArrayList<>();
        headerItemMap = new TreeMap<>();
        localCacheFile = new HashMap<>();
    }

    @Override
    public void create() {
        observeCurrentUser();
    }

    @Override
    public void resume() {
        //observeMessageUpdate();
        isInBackground.set(false);
        for (ChildData<Message> message : messagesInBackground) {
            handleMessageData(message);
        }
        messagesInBackground.clear();

    }

    @Override
    public void pause() {
//        observeLastMessageUseCase.unsubscribe();
//        observeMessageChangeUseCase.unsubscribe();
        isInBackground.set(true);
    }

    private void observeCurrentUser() {
        observeCurrentUserUseCase.execute(new DefaultObserver<User>() {
            @Override
            public void onNext(User user) {
                currentUser = user;
                updateUnreadMessageCount();
            }
        }, null);
    }

    @Override
    public void initConversationData(String conversationId) {
        view.showLoading();
        getConversationValueUseCase.execute(new DefaultObserver<Conversation>() {
            @Override
            public void onNext(Conversation conv) {
                handleConversationUpdate(conv);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
                if (exception instanceof TimeoutException){
                    view.showTimeoutNotification();
                }
            }

            @Override
            public void onComplete() {
                super.onComplete();
                view.hideLoading();
            }
        }, conversationId);
    }

    @Override
    public void observeMessageUpdate() {
        if (conversation == null) return;
        observeLastMessageUseCase.execute(new DefaultObserver<ChildData<Message>>() {
            @Override
            public void onNext(ChildData<Message> messageChildData) {
                handleMessageData(messageChildData);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new ObserveLastMessageUseCase.Params(conversation, currentUser));
        observeMessageChangeUseCase.execute(new DefaultObserver<ChildData<Message>>() {
            @Override
            public void onNext(ChildData<Message> messageChildData) {
                handleMessageData(messageChildData);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new ObserveMessageChangeUseCase.Params(conversation, currentUser));
    }

    @Override
    public void loadMoreMessage(double oldestTimestamp) {
        double endTimestamp = oldestTimestamp - 0.001;
        loadMoreMessagesUseCase.execute(new DefaultObserver<LoadMoreMessagesUseCase.Output>() {
            @Override
            public void onNext(LoadMoreMessagesUseCase.Output output) {
                appendHistoryMessages(output.messages, output.canLoadMore);
                view.hideRefreshView();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                super.onError(exception);
                view.hideRefreshView();
            }
        }, new LoadMoreMessagesUseCase.Params(conversation, endTimestamp));
    }

    private void updateReadStatus(Message message) {
        if (!message.senderId.equals(currentUser.key)) {
            if (message.messageStatusCode == Constant.MESSAGE_STATUS_SENT) {
                updateMessageStatusUseCase.execute(new DefaultObserver<>(),
                        new UpdateMessageStatusUseCase.Params(conversation.key, Constant.MESSAGE_STATUS_READ, message.key, message.type));
            }
        }
    }

    private boolean canUpdateStatustoReadRule(Message message) {
        return Integer.parseInt(String.valueOf(message.status.get(message.senderId))) == Constant.MESSAGE_STATUS_DELIVERED;
    }

    private void handleMessageData(ChildData<Message> messageChildData) {
        if (isInBackground.get()) {
            messagesInBackground.add(messageChildData);
            return;
        }
        updateReadStatus(messageChildData.getData());
        updateConversationReadStatus();
        prepareMessageStatus(messageChildData.getData());
        String senderNickname = conversation.nickNames.get(messageChildData.getData().senderId);
        if (!TextUtils.isEmpty(senderNickname)){
            messageChildData.getData().senderName = senderNickname;
        }
        switch (messageChildData.getType()) {
            case CHILD_ADDED:
                // Check error message
//                checkMessageError(messageChildData.getData());
                addMessage(messageChildData.getData());
                break;
            case CHILD_REMOVED:
                MessageHeaderItem headerItem = headerItemMap.get(messageChildData.getData().days);
                if (headerItem != null) {
                    MessageBaseItem item = headerItem.getChildItem(messageChildData.getData());
                    if (item != null) {
                        view.removeMessage(headerItem, item);
                    }
                }
                break;
            case CHILD_CHANGED:
                addMessage(messageChildData.getData());
                break;
        }
    }

    private void appendHistoryMessages(List<Message> messages, boolean canLoadMore) {
        MessageHeaderItem headerItem;
        for (Message message : messages) {
            prepareMessageStatus(message);
            message.opponentUser = conversation.opponentUser;
            headerItem = headerItemMap.get(message.days);
            if (headerItem == null) {
                headerItem = new MessageHeaderItem();
                headerItem.setKey(message.days);
                headerItemMap.put(message.days, headerItem);
            }

            MessageBaseItem item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
            headerItem.addNewItem(item);
            //messageBaseItems.add(item);
        }
        //headerItemMap = CommonMethod.sortByKeys(headerItemMap);
        List<MessageHeaderItem> headerItems = new ArrayList<>(headerItemMap.values());
        view.appendHistoryMessages(headerItems, canLoadMore);
    }

    private void updateLastMessages(List<Message> messages, boolean canLoadMore) {
        MessageHeaderItem headerItem;
        for (Message message : messages) {
            prepareMessageStatus(message);
            message.opponentUser = conversation.opponentUser;
            headerItem = headerItemMap.get(message.days);
            if (headerItem == null) {
                headerItem = new MessageHeaderItem();
                headerItem.setKey(message.days);
                headerItemMap.put(message.days, headerItem);
            }

            MessageBaseItem item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
            headerItem.addNewItem(item);
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
            headerItem.setKey(message.days);
            headerItemMap.put(message.days, headerItem);
        }
        Map.Entry<Long, MessageHeaderItem> entry = headerItemMap.higherEntry(message.days);
        MessageHeaderItem higherHeaderItem = null;
        if (entry != null) {
            higherHeaderItem = entry.getValue();
        }
        MessageBaseItem item = null;

        if (message.type == MessageType.IMAGE_GROUP) {
            if (!message.isCached) {
                for (Message child: message.childMessages) {
                    child.localFilePath = localCacheFile.get(child.key);
                }
            } else {
                item = headerItem.getChildItem(message);
                if (item != null) {
                    item.message.childMessages = message.childMessages;
                }
            }
        } else if (message.type == MessageType.IMAGE
                || message.type == MessageType.GAME
                || message.type == MessageType.VOICE) {
            if (!message.isCached) {
                message.localFilePath = localCacheFile.get(message.key);
            }
        }

        if (item == null) {
            message.opponentUser = conversation.opponentUser;
            item = MessageBaseItem.from(message, currentUser.key, conversation.conversationType);
        }
        boolean added = headerItem.addChildItem(item);
        view.updateMessage(item, headerItem, higherHeaderItem, added);
    }

    @Override
    public void sendTextMessage(String message, boolean markStatus) {
        if (!isAbleToSendMessage()) return;
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
                sendNotification(conversation, message1.key, message1.message, MessageType.TEXT);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendImageMessage(String photoUrl, String thumbUrl, boolean markStatus) {
        if (!isAbleToSendMessage()) return;
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
                    message.days = (long) (message.timestamp * 1000 / Constant.MILLISECOND_PER_30_MIN);
                    localCacheFile.put(message.key, message.localFilePath);
                    addMessage(message);
                } else {
                    sendNotification(conversation, message.key, message.message, MessageType.IMAGE);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendImagesMessage(List<PhotoItem> items, boolean markStatus) {
        if (!isAbleToSendMessage()) return;
        SendGroupImageMessageUseCase.Params params = new SendGroupImageMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.markStatus = markStatus;
        params.items = items;
        sendGroupImageMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (!message.isCached) {
                    message.message = "" + items.size();
                    sendNotification(conversation, message.key, message.message, MessageType.IMAGE_GROUP);
                }
                if (message.childMessages != null) {
                    for (Message child : message.childMessages) {
                        localCacheFile.put(child.key, child.localFilePath);
                    }
                }
                // Add trick here to keep cache data
                message.isCached = true;
                addMessage(message);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendGameMessage(String gameUrl, GameType gameType, boolean markStatus) {
        if (!isAbleToSendMessage()) return;
        SendGameMessageUseCase.Params params = new SendGameMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = gameUrl;
        params.gameType = gameType;
        params.markStatus = markStatus;
        sendGameMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    addMessage(message);
                } else {
                    sendNotification(conversation, message.key, message.message, MessageType.GAME);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendGameMessages(@NotNull List<PhotoItem> items, GameType gameType, boolean isMask) {
        SendGroupGameMessageUseCase.Params params = new SendGroupGameMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.gameType = gameType;
        params.markStatus = isMask;
        params.items = items;
        sendGroupGameMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    addMessage(message);
                }
            }

            @Override
            public void onComplete() {
                //sendNotification(conversation, "" + params.items.size(), MessageType.GAME_GROUP);
            }
        }, params);
    }

    @Override
    public void sendAudioMessage(String audioUrl, VoiceType voiceType) {
        if (!isAbleToSendMessage()) return;
        SendAudioMessageUseCase.Params params = new SendAudioMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = audioUrl;
        params.messageType = MessageType.VOICE;
        params.voiceType = voiceType;
        sendAudioMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    addMessage(message);
                    localCacheFile.put(message.key, message.localFilePath);
                } else {
                    sendNotification(conversation, message.key, message.message, message.type);
                }
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    @Override
    public void sendVideoMessage(String videoPath) {
        if (!isAbleToSendMessage()) return;
        SendVideoMessageUseCase.Params params = new SendVideoMessageUseCase.Params();
        params.conversation = conversation;
        params.currentUser = currentUser;
        params.filePath = videoPath;
        params.messageType = MessageType.VIDEO;
        sendVideoMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                super.onNext(message);
                if (!message.isCached) {
                    sendNotification(conversation, message.key, message.message, MessageType.VIDEO);
                } else {
                    ChildData<Message> childData = new ChildData<>(message, ChildData.Type.CHILD_CHANGED);
                    handleMessageData(childData);
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
     *
     * @param lastMessage Last message in conversation. Null if there is no message in conversation
     */
    @Override
    public void updateConversationLastMessage(@Nullable Message lastMessage) {
        Map<String, Boolean> maskStatus = new HashMap<>();
        maskStatus.put(currentUser.key, lastMessage != null && lastMessage.isMask);
        Conversation conversation = new Conversation(this.conversation.conversationType,
                lastMessage != null ? lastMessage.type.ordinal() : Constant.MSG_TYPE_TEXT,
                lastMessage != null ? lastMessage.callType : 0,
                lastMessage != null ? lastMessage.message : "",
                this.conversation.groupID, this.currentUser.key, this.conversation.memberIDs,
                maskStatus,
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
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.hideLoading();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                view.hideLoading();
            }
        }, params);
    }

    @Override
    public void updateMaskMessages(List<Message> messages, boolean isLastMessage, boolean isMask, boolean updateChild) {
        UpdateMaskMessagesUseCase.Params params = new UpdateMaskMessagesUseCase.Params();
        params.conversationId = conversation.key;
        params.isLastMessage = isLastMessage;
        params.isMask = isMask;
        //only unmask if face id not enabled or face id recognized
        ArrayList<Message> filtered = new ArrayList<>();
        for (Message m:messages) {
            if (isMask || !faceIdStatusRepository.isFaceIdEnabled() || faceIdStatusRepository.getFaceIdRecognitionStatus().get()){
                filtered.add(m);
            }

        }
        params.setMessages(filtered);
        updateMaskMessagesUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.hideLoading();
            }
        }, params);

        // Update child messages if possible
        if (updateChild) {
            List<Message> childToUpdate = new ArrayList<>();
            for (Message message : filtered) {
                if (message.type == MessageType.IMAGE_GROUP) {
                    if (message.childMessages != null) {
                        childToUpdate.addAll(message.childMessages);
                    }
                }
            }
            updateMaskChildMessages(childToUpdate, isMask);
        }
    }

    private void getLastMessages(Conversation conversation) {
        getLastMessagesUseCase.execute(new DefaultObserver<GetLastMessagesUseCase.Output>() {
            @Override
            public void onNext(GetLastMessagesUseCase.Output output) {
                view.hideLoading();
                if (output.isCached) {
                    //Collections.reverse(output.messages);
                    updateLastMessages(output.messages, output.canLoadMore);
                    return;
                }
                //updateLastMessages(output.messages, output.canLoadMore);
                for (Message message : output.messages) {
                    prepareMessageStatus(message);
                    addMessage(message);
                    updateReadStatus(message);
                }
                observeMessageUpdate();
                observeTypingEvent();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
                view.updateLastMessages(new ArrayList<>(), false);
                observeMessageUpdate();
                observeTypingEvent();
                view.hideLoading();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                view.hideLoading();
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
                                 conv.deleteTimestamp  = conversation.timesstamps;
                                 conversation = conv;

                             }
                         },
                        new ObserveConversationValueFromExistsConversationUseCase.Params(conversation, currentUser));
        observeNicknameConversationUseCase.execute(new DefaultObserver<Map<String, String>>() {
            @Override
            public void onNext(Map<String, String> nickNames) {
                if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
                    String opponentUserId = conversation.opponentUser.key;
                    String nickName = nickNames.get(opponentUserId);
                    conversation.opponentUser.nickName = nickName;
                    String title = TextUtils.isEmpty(nickName) ? conversation.opponentUser.getDisplayName() : nickName;
                    view.updateConversationTitle(title);
                    view.refreshMessages();
                } else {
                    view.updateNickNames(nickNames);
                }
            }
        }, new ObserveNicknameConversationUseCase.Params(conversation.key, currentUser.key));
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
        observeConversationBackgroundUseCase.execute(new DefaultObserver<String>() {
                                                         @Override
                                                         public void onNext(String s) {
                                                             view.updateBackground(s);
                                                         }
                                                     },
                new ObserveConversationBackgroundUseCase.Params(conversation.key, currentUser.key));
    }

    private void handleConversationUpdate(Conversation conversation) {
        if (currentUser == null) return;

        this.conversation = conversation;
        view.updateConversation(conversation);
        updateUnreadMessageCount();
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

    @Override
    public void updateMaskOutput(boolean checked) {
        UpdateMaskOutputConversationUseCase.Params params = new UpdateMaskOutputConversationUseCase.Params(
                conversation.key, conversation.memberIDs, checked
        );
        updateMaskOutputConversationUseCase.execute(new DefaultObserver<>(), params);
    }

    @Override
    public void updateMaskChildMessages(List<Message> messages, boolean maskStatus) {
        UpdateMaskChildMessagesUseCase.Params params = new UpdateMaskChildMessagesUseCase.Params();
        params.conversationId = conversation.key;
        params.isMask = maskStatus;
        params.messages = messages;
        updateMaskChildMessagesUseCase.execute(new DefaultObserver<>(), params);
    }

    @Override
    public void getUpdatedMessages(double timestamp) {
        getUpdatedMessagesUseCase.execute(new DefaultObserver<List<? extends Message>>() {
                                              @Override
                                              public void onNext(List<? extends Message> messages) {
                                                  for (Message message : messages) {
                                                      addMessage(message);
                                                  }
                                              }
                                          },
                new GetUpdatedMessagesUseCase.Params(conversation, timestamp, currentUser));
    }

    @Override
    public void sendSticker(@NotNull File file, boolean isMask) {
        SendStickerMessageUseCase.Params params = new SendStickerMessageUseCase.Params(
                file.getAbsolutePath(), conversation, currentUser, isMask
        );
        sendStickerMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message) {
                if (message.isCached) {
                    message.days = (long) (message.timestamp * 1000 / Constant.MILLISECOND_PER_30_MIN);
                    localCacheFile.put(message.key, message.localFilePath);
                    addMessage(message);
                } else {
                    sendNotification(conversation, message.key, message.message, MessageType.STICKER);
                }
            }
        }, params);

    }

    @Override
    public void userRecognized() {
//        faceIdStatusRepository.setFaceIdRecognitionStatus(true);
        // TODO should refresh chat screen
    }
    public void sendSticker(String stickerPath) {
        if (!isAbleToSendMessage()) return;
        String url = stickerPath.replace("stickers/","").replace("/","_");
        SendMessageUseCase.Params params = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.STICKER)
                .setConversation(conversation)
                .setCurrentUser(currentUser)
                .setFileUrl(url)
                .setMarkStatus(false)
                .build();
        sendNewStickerMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message1) {
                sendNotification(conversation, message1.key, message1.message, MessageType.STICKER);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }


    @Override
    public void sendGifs(String url) {
        if (!isAbleToSendMessage()) return;
        SendMessageUseCase.Params params = new SendMessageUseCase.Params.Builder()
                .setMessageType(MessageType.GIF)
                .setConversation(conversation)
                .setCurrentUser(currentUser)
                .setFileUrl(url)
                .setMarkStatus(false)
                .build();
        sendGifsMessageUseCase.execute(new DefaultObserver<Message>() {
            @Override
            public void onNext(Message message1) {
                sendNotification(conversation, message1.key, message1.message, MessageType.GIF);
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, params);
    }

    private void sendNotification(Conversation conversation, String messageId, String message, MessageType messageType) {
        sendMessageNotificationUseCase.execute(new DefaultObserver<>(),
                new SendMessageNotificationUseCase.Params(conversation, messageId, message, messageType));
    }

    private boolean isAbleToSendMessage() {
        if (conversation.conversationType == Constant.CONVERSATION_TYPE_INDIVIDUAL) {
            if (currentUser.blocks.containsKey(conversation.opponentUser.key)) {
                String username = conversation.opponentUser.firstName;
                view.showErrorUserBlocked(username);
                return false;
            }
        }
        return true;
    }

    private void updateUnreadMessageCount() {
        // huulog unBad usecase overhere

        observeBadgeCountUseCase.execute(new DefaultObserver<Map<String, ? extends Integer>>() {
            @Override
            public void onNext(Map<String, ? extends Integer> stringMap) {
                if (stringMap.containsKey("refreshMock")) {
                    stringMap.remove("refreshMock");
                }
                Number missedCall = 0;
                if (stringMap.containsKey("missed_call")) {
                    missedCall = stringMap.get("missed_call");
                }
                stringMap.remove("missed_call");
                int messageCount = 0;
                Number count = 0;
                String currentChat = "";
                Activity activity = ActivityLifecycle.getInstance().getForegroundActivity();
                if (activity instanceof ChatActivity) {
                    currentChat = ((ChatActivity)activity).getConversationId();
                }
                stringMap.remove(currentChat);
                for (String key : stringMap.keySet()) {
                    count = stringMap.get(key);
                    messageCount += count.intValue();
                }
                busProvider.post(new BadgeCountUpdateEvent(new HashMap<>(stringMap), messageCount, missedCall.intValue()));
                view.updateUnreadMessageCount(messageCount);

            }

            @Override
            public void onError(@NotNull Throwable exception) {
                exception.printStackTrace();
            }
        }, new Object());

    }


    @Override
    public void destroy() {
        view = null;
        observeCurrentUserUseCase.dispose();
        getConversationValueUseCase.dispose();
        observeLastMessageUseCase.dispose();
        observeMessageChangeUseCase.dispose();
        observeTypingEventUseCase.dispose();
        observeConversationValueFromExistsConversationUseCase.dispose();
        getLastMessagesUseCase.dispose();
        deleteMessagesUseCase.dispose();
        updateMaskMessagesUseCase.dispose();
        updateMaskOutputConversationUseCase.dispose();
        toggleConversationTypingUseCase.dispose();
        observeConversationColorUseCase.dispose();
        observeConversationBackgroundUseCase.dispose();
        observeNicknameConversationUseCase.dispose();

//        sendTextMessageUseCase.dispose();
//        sendImageMessageUseCase.dispose();
//        sendGameMessageUseCase.dispose();
//        sendAudioMessageUseCase.dispose();
//        sendVideoMessageUseCase.dispose();
//        sendGroupGameMessageUseCase.dispose();
//        sendGroupImageMessageUseCase.dispose();
    }

    private void prepareMessageStatus(Message message) {
        int status = message.messageStatusCode;
        String messageStatus = "";
        if (TextUtils.equals(message.senderId, currentUser.key)) {
            if (message.type != MessageType.GAME) {
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
            if (message.type == MessageType.GAME) {
                messageStatus = "Game";
            }
        }
        message.messageStatus = messageStatus;
    }


    @Override
    public void checkPassword(@NotNull String password) {
        view.showLoading();
        checkPasswordUseCase.execute(new DefaultObserver<Boolean>() {
            @Override
            public void onNext(Boolean aBoolean) {
                faceIdStatusRepository.setFaceIdEnabled(false);
                view.hideLoading();
            }

            @Override
            public void onError(@NotNull Throwable exception) {
                view.displayConfirmPasswordError(exception.getLocalizedMessage());
                view.hideLoading();
            }

            @Override
            public void onComplete() {
                super.onComplete();
                view.hideLoading();
            }
        }, password);
    }

}
