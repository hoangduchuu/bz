package com.ping.android.domain.usecase.message;

import com.bzzzchat.cleanarchitecture.PostExecutionThread;
import com.bzzzchat.cleanarchitecture.ThreadExecutor;
import com.bzzzchat.cleanarchitecture.UseCase;
import com.ping.android.data.entity.MessageEntity;
import com.ping.android.data.mappers.MessageMapper;
import com.ping.android.domain.repository.CommonRepository;
import com.ping.android.domain.repository.ConversationRepository;
import com.ping.android.domain.repository.MessageRepository;
import com.ping.android.domain.repository.UserRepository;
import com.ping.android.model.Conversation;
import com.ping.android.model.Message;
import com.ping.android.model.User;
import com.ping.android.model.enums.GameType;
import com.ping.android.model.enums.MessageCallType;
import com.ping.android.model.enums.MessageType;
import com.ping.android.model.enums.VoiceType;
import com.ping.android.utils.configs.Constant;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import io.reactivex.Observable;

/**
 * Created by tuanluong on 2/8/18.
 */

public class SendMessageUseCase extends UseCase<Message, SendMessageUseCase.Params> {
    @Inject
    ConversationRepository conversationRepository;
    @Inject
    CommonRepository commonRepository;
    @Inject
    MessageRepository messageRepository;
    @Inject
    UserRepository userRepository;
    @Inject
    MessageMapper messageMapper;

    @Inject
    public SendMessageUseCase(@NotNull ThreadExecutor threadExecutor, @NotNull PostExecutionThread postExecutionThread) {
        super(threadExecutor, postExecutionThread);
    }

    @NotNull
    @Override
    public Observable<Message> buildUseCaseObservable(Params params) {
        MessageEntity message = params.getMessage();
        Conversation conversation = params.getConversation();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                handleMessageError(conversation, message)
                        .subscribe();
            }
        };
        Timer timer = new Timer();
        timer.schedule(task, 5000);
        Conversation newConversation = params.getNewConversation();
        return commonRepository.getConnectionState()
                .map(aBoolean -> {
                    if (!aBoolean) {
                        message.photoUrl = params.filePath;
                    }
                    return message;
                })
                .flatMap(msg -> conversationRepository.sendMessage(newConversation, msg)
                        .flatMap(message1 -> {
                            if (timer != null) {
                                timer.cancel();
                            }
                            return messageRepository.updateMessageStatus(newConversation.key, message1.key, message1.senderId, Constant.MESSAGE_STATUS_DELIVERED)
                                    .map(aBoolean -> messageMapper.transform(message1, params.user));
                        }));
    }

    private Observable<Boolean> handleMessageError(Conversation conversation, MessageEntity message) {
        if (message != null) {
            //messageRepository.handleMessageError(conversation.key, message.key, message.senderId, messageStatus);
            HashMap<String, Object> updateValue = new HashMap<>();
            for (String userId : conversation.memberIDs.keySet()) {
                //messageRepository.handleMessageError(conversation.key, message.key, userId, messageStatus);
                updateValue.put(String.format("messages/%s/%s/status/%s", conversation.key, message.key, userId), Constant.MESSAGE_STATUS_ERROR);
            }
            updateValue.put(String.format("media/%s/%s", conversation.key, message.key), message.toMap());
            return commonRepository.updateBatchData(updateValue);
        }
        return Observable.empty();
    }

    public static class Params {
        private Conversation conversation;
        private Conversation newConversation;
        private MessageEntity message;
        private User user;
        private String filePath;

        public MessageEntity getMessage() {
            return message;
        }

        public Conversation getConversation() {
            return conversation;
        }

        public Conversation getNewConversation() {
            return newConversation;
        }

        public static class Builder {
            private Conversation conversation;
            private boolean markStatus;
            private User currentUser;
            private String text;
            private MessageType messageType;
            private GameType gameType;
            private VoiceType voiceType;
            private MessageCallType callType;
            private String fileUrl;
            private String thumbUrl;
            private String messageKey;
            private String cacheImage;
            private double timestamp;
            private double callDuration;
            private int childCount = 0;

            public Builder() {
                timestamp = System.currentTimeMillis() / 1000d;
            }

            public Builder setCurrentUser(User currentUser) {
                this.currentUser = currentUser;
                return this;
            }

            public Builder setConversation(Conversation conversation) {
                this.conversation = conversation;
                return this;
            }

            public Builder setMarkStatus(boolean markStatus) {
                this.markStatus = markStatus;
                return this;
            }

            public Builder setMessageType(MessageType messageType) {
                this.messageType = messageType;
                return this;
            }

            public Builder setMessageKey(String messageKey) {
                this.messageKey = messageKey;
                return this;
            }

            // region text message
            public Builder setText(String text) {
                this.text = text;
                return this;
            }
            // endregion

            // region Game message
            public Builder setGameType(GameType gameType) {
                this.gameType = gameType;
                return this;
            }

            // endregion

            // region image message

            public Builder setFileUrl(String fileUrl) {
                this.fileUrl = fileUrl;
                this.text = fileUrl;
                return this;
            }

            public Builder setThumbUrl(String thumbUrl) {
                this.thumbUrl = thumbUrl;
                return this;
            }

            // endregion

            public Builder setVoiceType(VoiceType voiceType) {
                this.voiceType = voiceType;
                return this;
            }

            public Builder setCallType(MessageCallType callType) {
                this.callType = callType;
                return this;
            }

            public Builder setChildCount(int childCount) {
                this.childCount = childCount;
                return this;
            }

            public SendMessageUseCase.Params build() {
                Params params = new Params();
                MessageEntity message = null;
                switch (messageType) {
                    case TEXT:
                        message = buildMessage(currentUser, text);
                        break;
                    case IMAGE:
                        message = buildImageMessage(currentUser, fileUrl, thumbUrl);
                        break;
                    case GAME:
                        message = buildGameMessage(currentUser, fileUrl, gameType);
                        break;
                    case VOICE:
                        message = buildAudioMessage(currentUser, fileUrl, voiceType);
                        break;
                    case VIDEO:
                        message = buildVideoMessage(currentUser, fileUrl);
                        break;
                    case CALL:
                        message = buildCallMessage(currentUser, messageType, callType);
                        break;
                    case IMAGE_GROUP:
                        message = buildGroupImageMessage(currentUser);
                        break;
                }
                message.key = messageKey;
                params.message = message;
                params.conversation = conversation;
                params.newConversation = conversationFrom(message);
                params.user = currentUser;
                params.filePath = fileUrl;
                return params;
            }

            private MessageEntity buildGroupImageMessage(User currentUser) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createGroupImageMessage(currentUser.key, currentUser.getDisplayName(), messageType,
                        timestamp, getStatuses(), getMessageMaskStatuses(), getMessageDeleteStatuses(), allowance, childCount);
            }

            private MessageEntity buildCallMessage(User currentUser, MessageType messageType, MessageCallType callType) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createCallMessage(currentUser.key, currentUser.getDisplayName(), messageType,
                        timestamp, getStatuses(), getMessageMaskStatuses(), getMessageDeleteStatuses(), allowance, callType.ordinal(), callDuration);
            }

            private MessageEntity buildVideoMessage(User currentUser, String fileUrl) {
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createVideoMessage(fileUrl,
                        currentUser.key, currentUser.getDisplayName(), timestamp,
                        getStatuses(), getAudioMaskStatuses(voiceType),
                        getMessageDeleteStatuses(), allowance);
            }

            private MessageEntity buildAudioMessage(User currentUser, String audioUrl, VoiceType voiceType) {
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createAudioMessage(audioUrl,
                        currentUser.key, currentUser.getDisplayName(), timestamp, getStatuses(), getAudioMaskStatuses(voiceType),
                        getMessageDeleteStatuses(), allowance, voiceType.ordinal());
            }

            private MessageEntity buildMessage(User currentUser, String text) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createTextMessage(text, currentUser.key, currentUser.getDisplayName(),
                        timestamp, getStatuses(), getMessageMaskStatuses(), getMessageDeleteStatuses(), allowance);
            }

            private MessageEntity buildImageMessage(User currentUser, String photoUrl, String thumbUrl) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createImageMessage(photoUrl, thumbUrl, currentUser.key, currentUser.getDisplayName(),
                        timestamp, getStatuses(), getImageMaskStatuses(), getMessageDeleteStatuses(), allowance);
            }

            private MessageEntity buildGameMessage(User currentUser, String imageUrl, GameType gameType) {
                this.currentUser = currentUser;
                Map<String, Boolean> allowance = getAllowance();
                return MessageEntity.createGameMessage(imageUrl,
                        currentUser.key, currentUser.getDisplayName(), timestamp, getStatuses(), getImageMaskStatuses(),
                        getMessageDeleteStatuses(), allowance, gameType.ordinal());
            }

            private Conversation conversationFrom(MessageEntity message) {
                return new Conversation(conversation.conversationType, message.messageType, message.callType,
                        text, conversation.groupID, currentUser.key, getMemberIDs(), getMessageMaskStatuses(),
                        getMessageReadStatuses(), message.timestamp, conversation);
            }

            private Map<String, Boolean> getMemberIDs() {
                Map<String, Boolean> memberIDs = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    memberIDs.put(toUser, true);
                }
                return memberIDs;
            }

            private Map<String, Boolean> getMessageReadStatuses() {
                Map<String, Boolean> markStatuses = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    markStatuses.put(toUser, false);
                }
                markStatuses.put(currentUser.key, true);
                return markStatuses;
            }

            private Map<String, Boolean> getAllowance() {
                Map<String, Boolean> ret = new HashMap<>();
                ret.put(currentUser.key, true);
                for (String toUser : conversation.memberIDs.keySet()) {
                    if (toUser.equals(currentUser.key)) {
                        continue;
                    }
                    if (currentUser.blocks.containsKey(toUser)
                            || currentUser.blockBys.containsKey(toUser)) {
                        ret.put(toUser, false);
                    } else {
                        ret.put(toUser, true);
                    }
                }
                return ret;
            }

            private Map<String, Integer> getStatuses() {
                Map<String, Integer> deleteStatuses = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    deleteStatuses.put(toUser, Constant.MESSAGE_STATUS_SENT);
                }
                deleteStatuses.put(currentUser.key, Constant.MESSAGE_STATUS_SENT);
                return deleteStatuses;
            }

            private Map<String, Boolean> getAudioMaskStatuses(VoiceType voiceType) {
                Map<String, Boolean> maskStatuses = new HashMap<>();
                if (voiceType == VoiceType.DEFAULT) {
                    return maskStatuses;
                }
                for (String userId : conversation.memberIDs.keySet()) {
                    maskStatuses.put(userId, true);
                }
                return maskStatuses;
            }

            private Map<String, Boolean> getMessageMaskStatuses() {
                Map<String, Boolean> markStatuses = new HashMap<>();
                if (conversation.maskMessages != null) {
                    markStatuses.putAll(conversation.maskMessages);
                }
                markStatuses.put(currentUser.key, markStatus);
                return markStatuses;
            }

            private Map<String, Boolean> getImageMaskStatuses() {
                Map<String, Boolean> maskStatuses = new HashMap<>();
                if (conversation.puzzleMessages != null) {
                    maskStatuses.putAll(conversation.puzzleMessages);
                }
                maskStatuses.put(currentUser.key, markStatus);
                return maskStatuses;
            }

            private Map<String, Boolean> getMessageDeleteStatuses() {
                Map<String, Boolean> deleteStatuses = new HashMap<>();
                for (String toUser : conversation.memberIDs.keySet()) {
                    deleteStatuses.put(toUser, false);
                }
                deleteStatuses.put(currentUser.key, false);
                return deleteStatuses;
            }

            public Builder setCacheImage(String filePath) {
                this.cacheImage = filePath;
                return this;
            }

            public Builder setCallDuration(double callDuration) {
                this.callDuration = callDuration;
                return this;
            }
        }

        public void setConversation(Conversation conversation) {
            this.conversation = conversation;
        }

        public void setMessageKey(String key) {
            message.key = key;
        }

        public String getFilePath() {
            return filePath;
        }
    }
}
